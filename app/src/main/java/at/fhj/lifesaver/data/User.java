package at.fhj.lifesaver.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Die Klasse User ist eine Entität in der lokalen Room-Datenbank.
 * Jeder Benutzer besteht aus einem eindeutigen ID-Wert (autogeneriert), einem Namen, einer E-Mail-Adresse, einem Passwort sowie Koordinaten und Firebase ID.
 * Die E-mail Adresse ist als eindeutig identifiziert.
 */
@Entity(tableName = "users", indices = {@Index(value = {"email"}, unique = true)})
public class User {
    /**
     * Eindeutige automatisch generierte ID des Benutzers.
     */
    @PrimaryKey(autoGenerate = true)
    public int id;

    /**
     * Name des Benutzers: Pflichtfeld
     */
    @NonNull
    public String name;

    /**
     * E-Mail-Adresse des Benutzers: Pflichtfeld
     */
    @NonNull
    public String email;

    /**
     * Passwort des Benutzers: Pflichtfeld
     */
    @NonNull
    public String password;

    public double latitude;
    public double longitude;

    public boolean isCurrentUser;

    private String firebaseId;

    /**
     * Leerer Konstruktor, der von Room benötigt wird.
     */
    public User(){

    }

    /**
     * Gibt die Benutzer-ID zurück.
     * @return eindeutige ID des Benutzers
     */
    public int getId() {
        return id;
    }

    /**
     * Setzt die Benutzer-ID.
     * @param id eindeutige ID des Benutzers
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gibt den Namen des Benutzers zurück.
     * @return Benutzername
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Setzt den Namen des Benutzers.
     * @param name Benutzername
     */
    public void setName(@NonNull String name) {
        this.name = name;
    }

    /**
     * Gibt die E-Mail-Adresse des Benutzers zurück.
     * @return E-Mail-Adresse
     */
    @NonNull
    public String getEmail() {
        return email;
    }

    /**
     * Setzt die E-Mail-Adresse des Benutzers.
     * @param email E-Mail-Adresse
     */
    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    /**
     * Gibt das Passwort des Benutzers zurück.
     * @return Passwort
     */
    @NonNull
    public String getPassword() {
        return password;
    }

    /**
     * Setzt das Passwort des Benutzers.
     * @param password Passwort
     */
    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    /**
     * Gibt den Breitengrad des Benutzers zurück.
     * @return Breitengrad
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Setzt den Breitengrad des Benutzers.
     * @param latitude Breitengrad
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gibt den Längengrad des Benutzers zurück.
     * @return Längengrad
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Setzt den Längengrad des Benutzers.
     * @param longitude Längengrad
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Gibt zurück, ob dieser Benutzer als aktuell angemeldet markiert ist.
     * @return {@code true}, wenn aktueller Benutzer; sonst {@code false}
     */
    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    /**
     * Setzt, ob dieser Benutzer der aktuell angemeldete Benutzer ist.
     * @param currentUser {@code true}, wenn aktuell aktiv
     */
    public void setCurrentUser(boolean currentUser) {
        isCurrentUser = currentUser;
    }

    /**
     * Gibt die Firebase-ID des Benutzers zurück.
     * @return Firebase-Benutzer-ID
     */
    public String getFirebaseId() {
        return firebaseId;
    }

    /**
     * Setzt die Firebase-ID des Benutzers
     * @param firebaseId Firebase-Benutzer-ID
     */
    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }
}
