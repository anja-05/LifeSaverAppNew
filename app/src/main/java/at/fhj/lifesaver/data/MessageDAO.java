package at.fhj.lifesaver.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object für den Zugriff auf Chatnachrichten in der lokalen Room-Datenbank.
 */
@Dao
public interface MessageDAO {
    /**
     * Gibt alle Nachrichten zwischen zwei E-Mail-Adressen in chronologischer Reihenfolge zurück.
     * @param email1 erste E-Mail-Adresse
     * @param email2 zweite E-Mail-Adresse
     * @return Liste aller Nachrichten zwischen beiden Benutzern, sortiert nach Zeit
     */
    @Query("SELECT * FROM messages WHERE (senderEmail = :email1 AND receiverEmail = :email2) OR (senderEmail = :email2 AND receiverEmail = :email1) ORDER BY timestamp ASC")
    List<Message> getMessagesBetweenUsers(String email1, String email2);

    /**
     * Fügt eine neue Nachricht in die lokale Datenbank ein.
     * @param message die zu speichernde Nachricht
     */
    @Insert
    void insertMessage(Message message);

    /**
     * Sucht eine Nachricht mit exakt übereinstimmendem Absender, Empfänger und Zeitstempel.
     * Wird zur Duplikatserkennung vor dem Einfügen verwendet.
     * @param senderEmail E-Mail des Absenders
     * @param receiverEmail E-Mail des Empfängers
     * @param timestamp Zeitstempel
     * @return ein bestehendes Nachricht-Objekt, wenn vorhanden; sonst {@code null}
     */
    @Query("SELECT * FROM messages WHERE senderEmail = :senderEmail AND receiverEmail = :receiverEmail AND timestamp = :timestamp LIMIT 1")
    Message findDuplicate(String senderEmail, String receiverEmail, long timestamp);
}
