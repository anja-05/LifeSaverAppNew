package at.fhj.lifesaver.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * UserDAO ist das Data Access Object für Benutzeroperationen in der lokalen Room-Datenbank.
 * Es definiert Methoden zur Benutzerregistrierung, Anmeldung und Abfrage anhand der E-Mail.
 */
@Dao
public interface UserDAO {
    /**
     * Fügt einen neuen Benutzer in die Datenbank ein.
     * @param user Benutzerobjekt mit E-Mail, Name und Passwort
     */
    @Insert
    void insert(User user);

    /**
     * Aktualisiert einen bestehenden Benutzer in der Datenbank.
     * Der Benutzer muss durch seine ID eindeutig identifizierbar sein.
     * @param user Benutzer mit aktualisierten Feldern
     */
    @Update
    void updateUser(User user);

    /**
     * Prüft Login-Daten anhand von E-Mail und Passwort.
     * @param email  Benutzer-E-Mail
     * @param password Passwort
     * @return Benutzerobjekt, falls Kombination existiert, sonst null
     */
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    /**
     * Findet einen Benutzer anhand der E-Mail.
     * @param email Benutzer-E-Mail
     * @return Benutzerobjekt, dalls vorhanden, sonst null
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    /**
     * Gibt den aktuell als "eingeloggt" markierten Benutzer zurück.
     * Nur ein Benutzer sollte diesen Flag gleichzeitig besitzen.
     * @return Aktuell eingeloggter Benutzer, oder {@code null} wenn keiner markiert ist
     */
    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    User getCurrentUser();

    /**
     * Gibt eine Liste aller Benutzer in der Datenbank zurück.
     * @return Liste aller Benutzer
     */
    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    /**
     * Setzt das Feld {@code isCurrentUser} für alle Benutzer auf {@code false}.
     * Sollte vor dem Login/Wechsel des aktuellen Benutzers ausgeführt werden.
     */
    @Query("UPDATE users SET isCurrentUser = 0")
    void clearCurrentUserFlag();
}
