package at.fhj.lifesaver;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText emailInput, passwordInput;
    Button loginButton;
    TextView createAccountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.editTextTextPersonName);
        passwordInput = findViewById(R.id.editTextTextPassword);
        loginButton = findViewById(R.id.button);
        createAccountText = findViewById(R.id.textViewCreateAccount);

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Bitte E-Mail und Passwort eingeben", Toast.LENGTH_SHORT).show();
                return;
            }

            UserDatabase db = UserDatabase.getInstance(this);
            User user = db.userDao().login(email, password);
            if (user != null) {
                Toast.makeText(this, "Willkommen " + user.name, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Login fehlgeschlagen – falsche Daten", Toast.LENGTH_SHORT).show();
            }
        });

        createAccountText.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }
}
