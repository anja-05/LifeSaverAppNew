package at.fhj.lifesaver;

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

public class LoginActivity extends AppCompatActivity {
    EditText emailInput, passwordInput;
    Button loginButton;
    TextView createAccountText;
    ImageView passwordToggle;
    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.editTextTextPersonName);
        passwordInput = findViewById(R.id.editTextTextPassword);
        loginButton = findViewById(R.id.button);
        createAccountText = findViewById(R.id.textViewCreateAccount);
        passwordToggle = findViewById(R.id.passwordToggle);

        isPasswordVisible = false;
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

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
                prefs.edit()
                        .putString("user_name", user.name)
                        .putString("user_email", user.email)
                        .putBoolean("is_logged_in", true)
                        .apply();

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
