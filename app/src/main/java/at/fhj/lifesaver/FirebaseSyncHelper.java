package at.fhj.lifesaver;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class FirebaseSyncHelper {
    private static final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

    public static void updateUserInFirebase(User user) {
        if (user == null) {
            Log.e("FIREBASE", "⚠️ User ist null!");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", user.getName());
        data.put("latitude", user.getLatitude());
        data.put("longitude", user.getLongitude());

        Log.d("FIREBASE", "Firebase-ID: " + user.getId());
        usersRef.child(user.getEmail().replace(".", "_")).setValue(data)
                .addOnSuccessListener(aVoid -> Log.d("FIREBASE", "✅ Standort erfolgreich gespeichert"))
                .addOnFailureListener(e -> Log.e("FIREBASE", "❌ Fehler: " + e.getMessage()));

        Log.d("FIREBASE", "→ Sende an Firebase: " + data.toString());
    }

    public static void getAllUsers(ValueEventListener listener) {
        usersRef.addListenerForSingleValueEvent(listener);
    }
}
