package at.fhj.lifesaver;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import java.io.IOException;
import java.io.InputStream;

public class LektionDetailActivity extends AppCompatActivity {

    TextView textViewTitel, textViewTheorie;
    ImageView imageViewTheorie;
    Button buttonQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lektion_detail);

        textViewTitel = findViewById(R.id.textViewTitel);
        textViewTheorie = findViewById(R.id.textViewTheorie);
        imageViewTheorie = findViewById(R.id.imageViewTheorie);
        buttonQuiz = findViewById(R.id.buttonQuiz);

        imageViewTheorie.setVisibility(View.GONE); // Placeholder – du kannst später Bilder nutzen

        String dateiname = getIntent().getStringExtra("DATEINAME");
        String titel = getIntent().getStringExtra("TITEL");

        textViewTitel.setText(titel);

        try {
            InputStream is = getAssets().open(dateiname);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String htmlText = new String(buffer, "UTF-8");
            textViewTheorie.setText(HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY));
            textViewTheorie.setMovementMethod(LinkMovementMethod.getInstance()); // Für klickbare Links
        } catch (IOException e) {
            textViewTheorie.setText("Fehler beim Laden des Theorietextes.");
        }

        buttonQuiz.setOnClickListener(v -> {
            // Hier später QuizActivity starten
        });
    }

    private String titelZuDateiname(String titel) {
        return titel.toLowerCase()
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss")
                .replaceAll("[^a-z0-9]", "") + ".html";
    }
}
