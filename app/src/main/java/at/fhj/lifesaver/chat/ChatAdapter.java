package at.fhj.lifesaver.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import at.fhj.lifesaver.data.Message;
import at.fhj.lifesaver.R;

/**
 * Das ist der Adapter von der Chat Nachrichten Klasse in einem RecyclerView.
 * Er unterscheidet zwischen gesendeten und empfangenen Nachrichten.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<Message> messages = new ArrayList<>();
    private String currentUserEmail;

    /**
     * Erstellt einen neuen ChatAdapter.
     * @param context Kontext der Activity
     * @param currentUserEmail E-Mail-Adresse des aktuell eingeloggten Nutzers
     */
    public ChatAdapter(Context context, String currentUserEmail) {
        this.context = context;
        this.currentUserEmail = currentUserEmail;
    }

    /**
     * Setzt die Liste der Nachrichten und aktualisiert die Ansicht.
     * @param messages Liste der Nachrichten
     */
    public void setMessages(List<Message> messages) {
        if (messages == null) {
            this.messages = new ArrayList<>();
        } else {
            this.messages = messages;
        }
        notifyDataSetChanged();
    }

    @Override
    /**
     * Gibt den Typ der Ansicht zurück, abhängig davon, ob die Nachricht gesendet oder empfangen wurde.
     */
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSenderEmail().equals(currentUserEmail)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    /**
     * Erstellt die passende ViewHolder-Instanz basierend auf dem Nachrichtentyp.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return entweder eine Instanz der Klasse SentMessageHolder oder ReceivedMessageHolder
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = inflater.inflate(R.layout.item_sent_message, parent, false);
            return new SentMessageHolder(view);
        } else {
            view = inflater.inflate(R.layout.item_received_message, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    /**
     * Bindet die Nachrihctendaten an den entsprechenden ViewHolder.
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageHolder) holder).bind(message);
        } else {
            ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    /**
     * Gibt die Gesamtanzahl an Nachrichten zurück.
     * @return
     */
    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * ViewHolder für gesendete Nachrichten.
     */
    static class SentMessageHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
        }

        void bind(Message message) {
            try {
                messageText.setText(message.getText());
            } catch (Exception e) {
                messageText.setText("Nachricht konnte nicht geladen werden.");
            }
        }
    }

    /**
     * ViewHolder für empfangene Nachrichten.
     */
    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
        }

        void bind(Message message) {
            try {
                messageText.setText(message.getText());
            } catch (Exception e) {
                messageText.setText("Nachricht konnte nicht geladen werden.");
            }
        }
    }
}
