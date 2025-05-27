package at.fhj.lifesaver;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;


public class QuizActivity extends AppCompatActivity {

    private List<QuizFrage> fragen;
    private int aktuelleFrage = 0;

    private TextView textQuestion, textFeedback;
    private RadioGroup optionsGroup;
    private Button buttonSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        textQuestion = findViewById(R.id.textQuestion);
        optionsGroup = findViewById(R.id.optionsGroup);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        textFeedback = findViewById(R.id.textFeedback);

        ladeFragenUndZeigeErste();
    }

    private void ladeFragenUndZeigeErste() {
        try {
            InputStream is = getAssets().open("allergischeReaktion.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            Gson gson = new Gson();
            QuizFrage[] fragenArray = gson.fromJson(reader, QuizFrage[].class);
            fragen = Arrays.asList(fragenArray);
            zeigeFrage();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Fehler beim Laden der Fragen", Toast.LENGTH_LONG).show();
        }
    }

    private void zeigeFrage() {
        QuizFrage frage = fragen.get(aktuelleFrage);
        textQuestion.setText(frage.question);

        ((RadioButton) optionsGroup.getChildAt(0)).setText(frage.options.get(0));
        ((RadioButton) optionsGroup.getChildAt(1)).setText(frage.options.get(1));
        ((RadioButton) optionsGroup.getChildAt(2)).setText(frage.options.get(2));
        ((RadioButton) optionsGroup.getChildAt(3)).setText(frage.options.get(3));

        optionsGroup.clearCheck();
        textFeedback.setVisibility(View.GONE);
        buttonSubmit.setText("Antwort überprüfen");

        buttonSubmit.setOnClickListener(v -> überprüfeAntwort(frage));
    }

    private void überprüfeAntwort(QuizFrage frage) {
        int checkedId = optionsGroup.getCheckedRadioButtonId();
        if (checkedId == -1) return;

        int selectedIndex = optionsGroup.indexOfChild(findViewById(checkedId));

        if (selectedIndex == frage.correctIndex) {
            textFeedback.setText("✅ Richtig!");
        } else {
            textFeedback.setText("❌ Falsch: " + frage.explanation);
        }

        textFeedback.setVisibility(View.VISIBLE);
        buttonSubmit.setText(aktuelleFrage == fragen.size() - 1 ? "Fertig" : "Nächste Frage");

        buttonSubmit.setOnClickListener(v -> {
            if (aktuelleFrage < fragen.size() - 1) {
                aktuelleFrage++;
                zeigeFrage();
            } else {
                finish();
            }
        });
    }
}