package at.fhj.lifesaver.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import at.fhj.lifesaver.R;
import at.fhj.lifesaver.data.User;
import at.fhj.lifesaver.data.UserDatabase;

/**
 * Die Klasse LoginActivity bietet die Benutzeroberfläche für die Anmeldung in der Lifesaver-App.
 * Sie prüft gespeicherte Anmeldedaten, authentifiziert neue Anmeldungen mit der lokalen Room-Datenbank
 * und leitet den Benutzer nach erfolgreichem Login in die Hauptansicht weiter.
 * Die Passwortanzeige kann ein- und ausgeblendet werden, es gibt eine einfache Validierung der Eingabefelder und es ertfolgt dei Weiterleitung zur SignupActivity bei Bedarf.
 */
public class LoginActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView createAccountText;
    private ImageView passwordToggle;
    private boolean isPasswordVisible = false;

    private static final String PREFS_NAME = "user_data";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    /**
     * Wird beim Start der Aktivität aufgerufen.
     * Prüft, ob der Benutzer bereits eingeloggt ist, und leitet ggf. direkt zur Hauptansicht weiter.
     * Initialisiert anschließend die Benutzeroberfläche und Funktionen.
     * @param savedInstanceState Zustand der Aktivität (nicht verwendet)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

        if (isLoggedIn) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        initViews();
        setupPasswordToggle();
        setupLoginButton(prefs);
        setupCreateAccountLink();
    }

    /**
     * Initialisiert die UI-Elemente.
     */
    private void initViews() {
        emailInput = findViewById(R.id.editTextTextPersonName);
        passwordInput = findViewById(R.id.editTextTextPassword);
        loginButton = findViewById(R.id.button);
        createAccountText = findViewById(R.id.textViewCreateAccount);
        passwordToggle = findViewById(R.id.passwordToggle);

        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    /**
     * Richtet das Umschalten der Passwortsichtbarkeit ein.
     */
    private void setupPasswordToggle() {
        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                isPasswordVisible = false;
            } else {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                isPasswordVisible = true;
            }
            passwordInput.setSelection(passwordInput.getText().length());
        });
    }

    /**
     * Richtet den Login-Button ein und behandelt die Authentifizierung.
     * @param prefs die SharedPreferences zur Speicherung von Login-Daten
     */
    private void setupLoginButton(SharedPreferences prefs) {
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Bitte E-Mail und Passwort eingeben", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                UserDatabase db = UserDatabase.getInstance(this);
                User user = db.userDao().login(email, password);

                if (user != null) {
                    db.userDao().clearCurrentUserFlag();
                    user.setCurrentUser(true);
                    db.userDao().updateUser(user);

                runOnUiThread(() -> {
                        prefs.edit()
                                .putString("user_name", user.name)
                                .putString("user_email", user.email)
                                .putBoolean(KEY_IS_LOGGED_IN, true)
                                .apply();

                        Toast.makeText(this, "Willkommen " + user.name, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                });
                    } else {
                        runOnUiThread(() ->
                        Toast.makeText(this, "Login fehlgeschlagen – falsche Daten", Toast.LENGTH_SHORT).show()
                         );
                    }
            }).start();
        });
    }

    /**
     * Öffnet die Registrierungsansicht.
     */
    private void setupCreateAccountLink() {
        createAccountText.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }
}