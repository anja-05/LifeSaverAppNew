package at.fhj.lifesaver.utils;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import at.fhj.lifesaver.data.User;

/**
 * Die Klasse {@code FirebaseSyncHelper} kapselt die Logik zur Synchronisation von Benutzerdaten
 * (insbesondere Standortinformationen) mit der Firebase Realtime Database.
 * Sie erlaubt das Hochladen eines einzelnen Benutzers sowie das asynchrone Abrufen aller Nutzer.
 */
public class FirebaseSyncHelper {
    private static final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

    /**
     * Aktualisiert einen Benutzer (inkl. Name und Standort) in der Firebase Realtime Database.
     * Der Benutzer wird anhand seiner E-Mail eindeutig identifiziert.
     * @param user der zu aktualisierende Benutzer
     */
    public static void updateUserInFirebase(User user) {
        if (user == null) {
            return;
        }
        String validEmailKey = user.getEmail().replace(".", "_").replace("@", "_at_");

        Map<String, Object> data = new HashMap<>();
        data.put("name", user.getName());
        data.put("latitude", user.getLatitude());
        data.put("longitude", user.getLongitude());

        try {
            usersRef.child(validEmailKey).setValue(data);
        } catch (Exception ignored) {

        }
    }

    /**
     * Ruft alle in Firebase gespeicherten Benutzer ab und liefert sie an den angegebenen Listener zurück.
     * @param listener {@link ValueEventListener}, der bei erfolgreicher oder fehlerhafter Antwort ausgelöst wird
     */
    public static void getAllUsers(ValueEventListener listener) {
        if (listener != null) {
            usersRef.addListenerForSingleValueEvent(listener);
        }
    }
}
