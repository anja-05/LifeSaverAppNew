package at.fhj.lifesaver.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        /*int chatPartnerId = getIntent().getIntExtra("USER_ID", -1);
        if (chatPartnerId != -1) {
            chatPartner = userDao.getUserById(chatPartnerId);
            userNameText.setText(chatPartner.getName());
        } else {
            finish(); // Beende die Aktivität, wenn kein gültiger Benutzer gefunden wurde
            return;
        }*/
        String chatPartnerEmail = getIntent().getStringExtra("USER_EMAIL");
        if (chatPartnerEmail != null) {
            chatPartner = userDao.findByEmail(chatPartnerEmail);
            if (chatPartner != null) {
                userNameText.setText(chatPartner.getName());
            } else {
                Toast.makeText(this, "Chat-Partner nicht gefunden.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(this, "Keine Chat-Partner-E-Mail übergeben.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUser = userDao.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Aktueller Benutzer nicht gefunden.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Initialisiere den RecyclerView
        chatAdapter = new ChatAdapter(this, currentUser.getEmail());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Lade Nachrichten
        loadMessages();

        listenForMessages();

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
        List<Message> messages = messageDao.getMessagesBetweenUsers(currentUser.getEmail(), chatPartner.getEmail());
        chatAdapter.setMessages(messages);
        recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            Message message = new Message(currentUser.getEmail(), chatPartner.getEmail(), messageText);
            messageDao.insertMessage(message);
            messageInput.setText("");
            loadMessages();

            DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("messages");
            String conversationId = getConversationId(currentUser.getEmail(), chatPartner.getEmail());
            String key = messageRef.child(conversationId).push().getKey();

            if (key != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("text", messageText);
                data.put("timestamp", message.getTimestamp());
                data.put("senderEmail", currentUser.getEmail());
                data.put("receiverEmail", chatPartner.getEmail());
                messageRef.child(conversationId).child(key).setValue(data);
            }
        }
    }

    private void listenForMessages() {
        String conversationId = getConversationId(currentUser.getEmail(), chatPartner.getEmail());
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("messages").child(conversationId);

        messageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        String text = child.child("text").getValue(String.class);
                        Long timestamp = child.child("timestamp").getValue(Long.class);
                        String sender = child.child("senderEmail").getValue(String.class);
                        String receiver = child.child("receiverEmail").getValue(String.class);

                        if (text != null && sender != null && receiver != null && timestamp != null) {
                            // Nur diese Konversation
                            if ((sender.equals(currentUser.getEmail()) && receiver.equals(chatPartner.getEmail())) ||
                                    (sender.equals(chatPartner.getEmail()) && receiver.equals(currentUser.getEmail()))) {

                                Message existing = messageDao.findDuplicate(sender, receiver, timestamp);
                                if (existing == null) {
                                    Message msg = new Message(sender, receiver, text);
                                    msg.setTimestamp(timestamp);
                                    messageDao.insertMessage(msg);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                loadMessages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.toException().printStackTrace();
            }
        });
    }

    private String getConversationId(String email1, String email2) {
        return (email1.compareToIgnoreCase(email2) < 0 ? email1 + "_" + email2 : email2 + "_" + email1)
                .replace(".", "_")
                .replace("@", "_at_");
    }
}
