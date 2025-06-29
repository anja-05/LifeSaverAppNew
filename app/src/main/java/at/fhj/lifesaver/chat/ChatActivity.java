package at.fhj.lifesaver.chat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import at.fhj.lifesaver.utils.EncryptionHelper;

/**
 * Die ChatActivity Klasse ermöglicht den Austausch von Nachrichten zwischen zwei Benutzern.
 * Nachrichten werden lokal entschlüsselt gespcihert und verschöüsselt über Firebase gesendet.
 */
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
    private String encryptionPassword;

    /**
     * Initialisiert die Oberfläche des Chats und lädt alle notwendigen Benutzer- und Nachrihcteninformationen
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.messages_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        userNameText = findViewById(R.id.user_name);

        database = UserDatabase.getInstance(this);
        userDao = database.userDao();
        messageDao = database.messageDao();

        String chatPartnerEmail = getIntent().getStringExtra("USER_EMAIL");
        if (chatPartnerEmail == null) {
            Toast.makeText(this, getString(R.string.chat_no_email), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        AsyncTask.execute(() -> {
            chatPartner = userDao.findByEmail(chatPartnerEmail);
            currentUser = userDao.getCurrentUser();

            runOnUiThread(() -> {
                if (chatPartner == null || currentUser == null) {
                    Toast.makeText(this, getString(R.string.chat_user_not_found), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                encryptionPassword = currentUser.getPassword();

                userNameText.setText(chatPartner.getName());
                chatAdapter = new ChatAdapter(this, currentUser.getEmail());
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(chatAdapter);

                loadMessages();
                listenForMessages();
            });
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    /**
     * Diese Methode erstellt einen gemeinsamen Schlüssel basierend auf den E-Mail-Adressen der beiden Benutzer.
     */
    private String generateSharedKey() {
        String user1 = currentUser.getEmail();
        String user2 = chatPartner.getEmail();

        // Sortiere die E-Mails, damit beide Benutzer den gleichen Schlüssel erhalten
        if (user1.compareTo(user2) > 0) {
            return user1 + user2;
        } else {
            return user2 + user1;
        }
    }

    /**
     * Lädt Nachrichten aus der lokalen Datenbank zwischen den beiden Nutzern und aktualisiert die Anzeige.
     */
    private void loadMessages() {
        AsyncTask.execute(() -> {
            List<Message> messages = messageDao.getMessagesBetweenUsers(currentUser.getEmail(), chatPartner.getEmail());
            runOnUiThread(() -> {
                chatAdapter.setMessages(messages);
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            });
        });
    }

    /**
     * Sendet eine neue Nachricht: lokal unverschlüsselt speichern, Firebase verschlüsselt.
     */
    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {

            // Verschlüsselung der Nachricht mit dem gemeinsamen Schlüssel
            String sharedKey = generateSharedKey();  // Gemeinsamen Schlüssel generieren
            String encryptedText = EncryptionHelper.encrypt(this, sharedKey, messageText);

            // Unverschlüsselte Nachricht für den Absender (lokal speichern)
            Message message = new Message(currentUser.getEmail(), chatPartner.getEmail(), messageText);

            // Nachrichteneingabe zurücksetzen
            messageInput.setText("");

            // Asynchrone Aufgabe für die lokale Speicherung der Nachricht
            AsyncTask.execute(() -> {
                messageDao.insertMessage(message); // Speichern der unverschlüsselten Nachricht
                runOnUiThread(this::loadMessages); // Nachrichten aktualisieren
            });

            // Firebase: Verschlüsselte Nachricht speichern
            DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("messages");
            String conversationId = getConversationId(currentUser.getEmail(), chatPartner.getEmail());
            String key = messageRef.child(conversationId).push().getKey();

            if (key != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("text", encryptedText); // Verschlüsselte Nachricht speichern
                data.put("timestamp", message.getTimestamp());
                data.put("senderEmail", currentUser.getEmail());
                data.put("receiverEmail", chatPartner.getEmail());

                // Nachricht in Firebase speichern
                messageRef.child(conversationId).child(key).setValue(data);
            }
        }
    }

    /**
     * Hört auf neue Nachrichten in Firebase und speichert diese lokal entschlüsselt.
     */
    private void listenForMessages() {
        String conversationId = getConversationId(currentUser.getEmail(), chatPartner.getEmail());
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("messages").child(conversationId);

        messageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AsyncTask.execute(() -> {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        try {
                            String encryptedText = child.child("text").getValue(String.class);
                            Long timestamp = child.child("timestamp").getValue(Long.class);
                            String sender = child.child("senderEmail").getValue(String.class);
                            String receiver = child.child("receiverEmail").getValue(String.class);

                            // Gemeinsamen Schlüssel basierend auf den E-Mails generieren
                            String sharedKey = generateSharedKey();

                            // Entschlüsseln der verschlüsselten Nachricht
                            String decryptedText = EncryptionHelper.decrypt(ChatActivity.this, sharedKey, encryptedText);

                            // Überprüfen, ob die Entschlüsselung funktioniert hat
                            if (decryptedText == null || decryptedText.isEmpty()) {
                                decryptedText = "<Entschlüsselung fehlgeschlagen>";
                            }

                            // Wenn alle Werte gültig sind
                            if (decryptedText != null && sender != null && receiver != null && timestamp != null) {
                                boolean isCurrentChat = (sender.equals(currentUser.getEmail()) && receiver.equals(chatPartner.getEmail())) ||
                                        (sender.equals(chatPartner.getEmail()) && receiver.equals(currentUser.getEmail()));

                                if (isCurrentChat && messageDao.findDuplicate(sender, receiver, timestamp) == null) {
                                    // Nachricht entschlüsseln und lokal speichern
                                    Message msg = new Message(sender, receiver, decryptedText);
                                    msg.setTimestamp(timestamp);

                                    // Lokale Speicherung der entschlüsselten Nachricht
                                    messageDao.insertMessage(msg);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("ChatActivity", "Fehler beim Entschlüsseln der Nachricht: " + e.getMessage());
                        }
                    }

                    // UI mit neuen Nachrichten aktualisieren
                    runOnUiThread(ChatActivity.this::loadMessages);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, getString(R.string.chat_loading_error), Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * Gibt eine eindeutige Unterhaltungs-ID für zwei E-Mail-Adressen zurück.
     * @param email1 erste Email Adresse
     * @param email2 zweite Email Adresse
     * @return eindeutige ID für firebase
     */
    private String getConversationId(String email1, String email2) {
        return (email1.compareToIgnoreCase(email2) < 0 ? email1 + "_" + email2 : email2 + "_" + email1)
                .replace(".", "_")
                .replace("@", "_at_");
    }
}
