package at.fhj.lifesaver.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Repräsentiert eine Chatnachricht zwischen zwei Benutzern.
 * Diese Entität wird in der lokalen Room-Datenbank in der Tabelle "messages" gespeichert.
 */
@Entity(tableName = "messages")
public class Message {
    /**
     * Eindeutige automatisch generierte ID der Nachricht.
     */
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String senderEmail;
    private String receiverEmail;
    private String text;
    private long timestamp;

    /**
     * Konstruktor für einen neue Nachricht
     * Der Zeitstempel wird automatisch gesetzt
     * @param senderEmail E-Mail-Adresse des Absenders
     * @param receiverEmail E-Mail-Adresse des Empfängers
     * @param text Nachrichteninhalt
     */
    public Message(String senderEmail, String receiverEmail, String text) {
        if (senderEmail == null || receiverEmail == null || text == null
                || senderEmail.trim().isEmpty()
                || receiverEmail.trim().isEmpty()
                || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Absender, Empfänger und Text dürfen nicht leer sein.");
        }
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.text = text;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Gibt die eindeutige ID der Nachricht zurück.
     */
    public int getId() { return id; }

    /**
     * Setzt die ID der Nachricht.
     * @param id Nachricht-ID
     */
    public void setId(int id) { this.id = id; }

    /**
     * Gibt den Inhalt der Nachricht zurück.
     */
    public String getText() { return text; }

    /**
     * Setzt den Inhalt der Nachricht.
     * @param text Nachrichtentext
     */
    public void setText(String text) { this.text = text; }

    /**
     * Gibt die E-Mail des Absenders zurück.
     * @return E-Mail des Senders
     */
    public String getSenderEmail() {
        return senderEmail;
    }

    /**
     * Setzt die E-Mail des Absenders.
     * @param senderEmail gültige E-Mail-Adress des Senders
     */
    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    /**
     * Gibt die E-Mail des Empfängers zurück.
     * @return E-Mail des Empfängers
     */
    public String getReceiverEmail() {
        return receiverEmail;
    }

    /**
     * Setzt die E-Mail des Empfängers.
     * @param receiverEmail gültige E-Mail-Adresse des Empfängers
     */
    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    /**
     * Gibt den Zeitstempel der Nachricht zurück.
     * @return Zeitstempel der Nachricht
     */
    public long getTimestamp() { return timestamp; }

    /**
     * Setzt den Zeitstempel der Nachricht.
     * @param timestamp Zeit in Millisekunden
     */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
