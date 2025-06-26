package at.fhj.lifesaver.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MessageDAO {
    @Query("SELECT * FROM messages WHERE (senderEmail = :email1 AND receiverEmail = :email2) OR (senderEmail = :email2 AND receiverEmail = :email1) ORDER BY timestamp ASC")
    List<Message> getMessagesBetweenUsers(String email1, String email2);

    @Insert
    void insertMessage(Message message);

    @Query("SELECT * FROM messages WHERE senderEmail = :senderEmail AND receiverEmail = :receiverEmail AND timestamp = :timestamp LIMIT 1")
    Message findDuplicate(String senderEmail, String receiverEmail, long timestamp);
}
