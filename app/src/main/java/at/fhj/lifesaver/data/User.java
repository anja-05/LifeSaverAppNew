package at.fhj.lifesaver.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Die Klasse User ist eine Entität in der lokalen Room-Datenbank.
 * Jeder Benutzer besteht aus einem eindeutigen ID-Wert (autogeneriert), einem Namen, einer E-Mail-Adresse und einem Passwort.
 */
@Entity(tableName = "users", indices = {@Index(value = {"email"}, unique = true)})
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String name;

    @NonNull
    public String email;

    @NonNull
    public String password;

    public double latitude;
    public double longitude;

    public boolean isCurrentUser;

    private String firebaseId;

    public User(){

    }

    public User(String name, double latitude, double longitude) {
        this.name = name;
        this.email = name.toLowerCase() + "@example.com"; // Dummy-Email
        this.password = "demo"; // Dummy-Passwort
        this.latitude = latitude;
        this.longitude = longitude;
        this.isCurrentUser = false;
    }

    public User(String name, String email, String password, double latitude, double longitude) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isCurrentUser = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        isCurrentUser = currentUser;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }
}
