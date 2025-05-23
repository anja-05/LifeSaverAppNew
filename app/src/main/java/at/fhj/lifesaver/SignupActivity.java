package at.fhj.lifesaver;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

        nameInput = findViewById(R.id.editTextTextPersonName3);
        emailInput = findViewById(R.id.editTextTextPersonName2);
        passwordInput = findViewById(R.id.editTextTextPersonName);
        repasswordInput = findViewById(R.id.editTextTextPassword);
        signupButton = findViewById(R.id.button);
        backToLogin = findViewById(R.id.textViewCreateAccount);
        passwordToggle = findViewById(R.id.passwordToggle1);

        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        repasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        isPasswordVisible = false;

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

            UserDatabase db = UserDatabase.getInstance(this);
            if (db.userDao().findByEmail(email) != null) {
                Toast.makeText(this, "E-Mail ist bereits registriert", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User();
            user.name = name;
            user.email = email;
            user.password = password;
            db.userDao().insert(user);

            Toast.makeText(this, "Registrierung erfolgreich", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        backToLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }
}
