package at.fhj.lifesaver;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LektionDetailActivity extends AppCompatActivity {

    TextView textViewTitel;
    WebView webViewTheorie;
    Button buttonQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lektion_detail);

        // UI-Elemente zuweisen
        textViewTitel = findViewById(R.id.textViewTitel);
        webViewTheorie = findViewById(R.id.webViewTheorie);
        buttonQuiz = findViewById(R.id.buttonQuiz);

        // Übergabewerte aus Intent
        String titel = getIntent().getStringExtra("TITEL");
        String dateiname = getIntent().getStringExtra("DATEINAME");

        // Titel setzen
        textViewTitel.setText(titel);

        // WebView konfigurieren
        webViewTheorie.setWebViewClient(new WebViewClient());
        webViewTheorie.getSettings().setJavaScriptEnabled(true); // falls du später JS brauchst

        // HTML-Datei aus assets laden
        if (dateiname != null && !dateiname.isEmpty()) {
            webViewTheorie.loadUrl("file:///android_asset/" + dateiname);
        } else {
            String fallbackHTML = "<html><body><h2>Fehler</h2><p>Die Lektion konnte nicht geladen werden.</p></body></html>";
            webViewTheorie.loadData(fallbackHTML, "text/html", "UTF-8");
        }

        // Quiz-Button (noch leer, vorbereiten)
        buttonQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuizActivity.class);
            startActivity(intent);
        });
    }
}
