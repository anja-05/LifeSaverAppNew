package at.fhj.lifesaver;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Die Klasse UserDatabase ist die zentrale SQLite-Datenbankklasse für die Benutzerverwaltung in der Lifesaver-App.
 * Sie verwendet das Room-Persistenz-Framework zur einfachen Datenbankintegration mit einer
 * definierten User Entity und dem zugehörigen UserDAO.
 */
@Database(entities = {User.class, Message.class}, version = 3, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {
    private static UserDatabase instance;

    /**
     * Gibt den zugehörigen Data Access Object (DAO) für die User-Tabelle zurück.
     * @return UserDAO-Instanz zur Datenmanipulation
     */
    public abstract UserDAO userDao();
    public abstract MessageDAO messageDao();

    /**
     * Erstellt oder gibt die Singleton-Instanz der UserDatabase zurück.
     * @param context Anwendungskontext
     * @return Instanz der UserDatabase
     */
    public static synchronized UserDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            UserDatabase.class, "user_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
