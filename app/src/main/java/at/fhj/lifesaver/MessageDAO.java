package at.fhj.lifesaver;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MessageDAO {
    @Query("SELECT * FROM messages WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp ASC")
    List<Message> getMessagesBetweenUsers(int userId1, int userId2);

    @Insert
    void insertMessage(Message message);
}
