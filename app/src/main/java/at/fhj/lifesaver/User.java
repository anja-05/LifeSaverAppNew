package at.fhj.lifesaver;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Die Klasse User ist eine Entität in der lokalen Room-Datenbank.
 * Jeder Benutzer besteht aus einem eindeutigen ID-Wert (autogeneriert), einem Namen, einer E-Mail-Adresse und einem Passwort.
 */
@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String name;

    @NonNull
    public String email;

    @NonNull
    public String password;
}
