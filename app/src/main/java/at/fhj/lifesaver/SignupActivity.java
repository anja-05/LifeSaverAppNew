package at.fhj.lifesaver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/**
 * Die Klasse SignupActivity ermöglicht neuen Benutzern die Registrierung in der Lifesaver-App.
 * Die Aktivität bietet Eingabefelder für Name, E-Mail, Passwort und Passwort-Wiederholung.
 * Sie prüft auf leere Felder, Passwortübereinstimmung und doppelte E-Mail-Adressen.
 * Erfolgreiche Registrierung speichert den Benutzer über Room in der lokalen Datenbank.
 * Die Registrierung erfolgt mit Eingabefeldern, die Passwortanzeige kann ein-udn ausgeblendet werden und
 * nach erfolgreicher Registrierung Übergang zur LoginActivity.
 */
public class SignupActivity extends AppCompatActivity {
    EditText nameInput, emailInput, passwordInput, repasswordInput;
    Button signupButton;
    TextView backToLogin;
    ImageView passwordToggle;
    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initViews();
        setupPasswordToggle();
        setupSignupButton();
        setupBackToLoginLink();
    }

    /**
     * Initialisiert die UI-Elemente.
     */
    private void initViews() {
        nameInput = findViewById(R.id.editTextTextPersonName3);
        emailInput = findViewById(R.id.editTextTextPersonName2);
        passwordInput = findViewById(R.id.editTextTextPersonName);
        repasswordInput = findViewById(R.id.editTextTextPassword);
        signupButton = findViewById(R.id.button);
        backToLogin = findViewById(R.id.textViewCreateAccount);
        passwordToggle = findViewById(R.id.passwordToggle1);

        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        repasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    /**
     * Richtet das Umschalten der Passwortsichtbarkeit ein.
     */
    private void setupPasswordToggle() {
        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                repasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                isPasswordVisible = false;
            } else {
                repasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                isPasswordVisible = true;
            }
            repasswordInput.setSelection(repasswordInput.getText().length());
        });
    }

    /**
     * Behandelt das Registrieren und die Validierung.
     */
    private void setupSignupButton() {
        signupButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String repassword = repasswordInput.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || repassword.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(repassword)) {
                Toast.makeText(this, "Passwörter stimmen nicht überein", Toast.LENGTH_SHORT).show();
                return;
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
                return;
            }

            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {

                new Thread(() -> {
                    UserDatabase db = UserDatabase.getInstance(this);
                    UserDAO dao = db.userDao();

                    if (dao.findByEmail(email) != null) {
                        runOnUiThread(() -> Toast.makeText(this, "E-Mail ist bereits registriert", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    User user = new User();
                    user.name = name;
                    user.email = email;
                    user.password = password;

                    // 📍 Standort setzen (entweder vom Gerät oder Dummy)
                    if (location != null) {
                        user.latitude = location.getLatitude();
                        user.longitude = location.getLongitude();
                    } else {
                        user.latitude = 0.0;
                        user.longitude = 0.0;
                    }

                    dao.insert(user);

                    // Nutzer mit generierter ID holen
                    User savedUser = dao.findByEmail(email);
                    if (savedUser != null) {
                        Log.d("FIREBASE", "→ Sende User an Firebase: " + savedUser.getName() + " ID: " + savedUser.getId());
                        FirebaseSyncHelper.updateUserInFirebase(savedUser);
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Registrierung erfolgreich", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    });
                }).start();
            });
        });
    }

    /**
     * Öffnet die LoginActivity bei Klick auf den Link.
     */
    private void setupBackToLoginLink() {
        backToLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Standortberechtigung erteilt. Bitte nochmal auf Registrieren klicken.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Standortberechtigung wird benötigt", Toast.LENGTH_SHORT).show();
            }
        }
    }
}