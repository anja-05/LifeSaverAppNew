package at.fhj.lifesaver.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import at.fhj.lifesaver.data.Message;
import at.fhj.lifesaver.data.MessageDAO;
import at.fhj.lifesaver.R;
import at.fhj.lifesaver.data.User;
import at.fhj.lifesaver.data.UserDAO;
import at.fhj.lifesaver.data.UserDatabase;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText messageInput;
    private ImageButton sendButton;
    private TextView userNameText;

    private UserDatabase database;
    private UserDAO userDao;
    private MessageDAO messageDao;
    private User currentUser;
    private User chatPartner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialisiere Views
        recyclerView = findViewById(R.id.messages_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        userNameText = findViewById(R.id.user_name);

        // Initialisiere die Datenbank
        database = UserDatabase.getInstance(this);
        userDao = database.userDao();
        messageDao = database.messageDao();

        // Hole den aktuellen Benutzer
        currentUser = userDao.getCurrentUser();

        // Hole den Chat-Partner anhand der übergebenen ID
        int chatPartnerId = getIntent().getIntExtra("USER_ID", -1);
        if (chatPartnerId != -1) {
            chatPartner = userDao.getUserById(chatPartnerId);
            userNameText.setText(chatPartner.getName());
        } else {
            finish(); // Beende die Aktivität, wenn kein gültiger Benutzer gefunden wurde
            return;
        }

        currentUser = userDao.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Aktueller Benutzer nicht gefunden.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Initialisiere den RecyclerView
        chatAdapter = new ChatAdapter(this, currentUser.getId());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Lade Nachrichten
        loadMessages();

        // Sende-Button-Listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // Zurück-Button
        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadMessages() {
        List<Message> messages = messageDao.getMessagesBetweenUsers(currentUser.getId(), chatPartner.getId());
        chatAdapter.setMessages(messages);
        recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Erstelle und speichere die Nachricht
            Message message = new Message(currentUser.getId(), chatPartner.getId(), messageText);
            messageDao.insertMessage(message);

            // Leere das Eingabefeld
            messageInput.setText("");

            // Aktualisiere die Nachrichtenliste
            loadMessages();
        }
    }
}
