package at.fhj.lifesaver;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

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
}
