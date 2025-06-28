package at.fhj.lifesaver.lesson;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import at.fhj.lifesaver.quiz.QuizActivity;
import at.fhj.lifesaver.R;

/**
 * Die LektionDetailActivity zeigt den Theorieteil einer Lektion an.
 * Sie liest den Titel und Dateinamen aus dem übergebenen Intent, zeigt den Titel
 * in einem TextView und lädt die zugehörige HTML-Datei aus dem assets-Ordner
 * in einer WebView.
 * Ein Button führt zum dazugehörigen Quiz.
 * Falls kein gültiger Dateiname übergeben wird, wird ein HTML-Fehlertext angezeigt.
 */
public class LektionDetailActivity extends AppCompatActivity {

    TextView textViewTitel;
    WebView webViewTheorie;
    Button buttonQuiz;

    /**
     * Wird beim Erstellen der Aktivität aufgerufen.
     * Initialisiert die UI-Elemente, liest die übergebenen Daten (Titel, Dateiname)
     * aus dem Intent aus und lädt die entsprechende HTML-Datei oder einen Fehlertext.
     * @param savedInstanceState Der gespeicherte Zustand der Aktivität (bei Konfigurationswechseln)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lektion_detail);

        textViewTitel = findViewById(R.id.textViewTitel);
        webViewTheorie = findViewById(R.id.webViewTheorie);
        buttonQuiz = findViewById(R.id.buttonQuiz);

        String titel = getIntent().getStringExtra("TITEL");
        String dateiname = getIntent().getStringExtra("DATEINAME");

        textViewTitel.setText(titel);
        webViewTheorie.setWebViewClient(new WebViewClient());

        if (dateiname != null && !dateiname.isEmpty()) {
            webViewTheorie.loadUrl("file:///android_asset/" + dateiname);
        } else {
            String fallbackHTML = "<html><body><h2>" + getString(R.string.lesson_load_error_title) + "</h2><p>+\n" + getString(R.string.lesson_load_error_message) + "</p></body></html>";
            webViewTheorie.loadData(fallbackHTML, "text/html", "UTF-8");
        }

        buttonQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra("TOPIC_TITLE", titel);
            intent.putExtra("DATEINAME", dateiname);
            startActivity(intent);
        });
    }
}