package at.fhj.lifesaver.training;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import at.fhj.lifesaver.R;

/**
 * Die Klasse RautekgriffActivity stellt eine Schritt-für-Schritt-Anleitung für den Rautekgriff dar.
 * Sie zeigt eine Serie von Bildern und Beschreibungen zur richtigen Durchführung.
 */
public class RautekgriffActivity extends AppCompatActivity {

    /**
     * Innere Hilfsklasse zur Darstellung eines Schrittes mit Titel, Bild und Beschreibung.
     */
    private static class Step {
        String title;
        int imageResId;
        String description;

        /**
         * Erstellt einen neuen Schritt für die Anzeige.
         * @param title Titel des Schrittes
         * @param imageResId Ressourcen-ID des Bildes
         * @param description Beschreibung des Schrittes
         */
        Step(String title, int imageResId, String description) {
            this.title = title;
            this.imageResId = imageResId;
            this.description = description;
        }
    }

    /**
     * Wird beim Erstellen der Aktivität aufgerufen. Baut die Schritt-für-Schritt-Anleitung dynamisch auf.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rautekgriff);

        LinearLayout container = findViewById(R.id.stepsContainer);

        Step[] steps = new Step[]{
                new Step("1. Aufrichten", R.drawable.rautek_schritt1,
                        "Stelle dich an das Kopfende der Person und greife mit beiden Händen weit unter ihren Kopf, Hals und die Schultern. Richte den Oberkörper dann mit ausreichend Schwung auf."),
                new Step("2. Abstützen", R.drawable.rautek_schritt2,
                        "Stütze den Körper in dieser Position mit deinem Bein ab, um ihn stabil und sicher zu halten."),
                new Step("3. Arm fassen", R.drawable.rautek_schritt3,
                        "Führe deine Arme unter den Achseln der Person hindurch und greife mit beiden Händen einen möglichst unverletzten Arm. Achte darauf, dass alle Finger – auch die Daumen – von oben zufassen. Halte die Hände dabei möglichst weit auseinander, um die Kraft gleichmäßig auf den Arm zu verteilen."),
                new Step("4. Ziehen", R.drawable.rautek_schritt4,
                        "Geh leicht in die Knie und zieh die Person mit Schwung auf dein Bein, um sie zu stabilisieren. Sobald die Person sicher auf deinem Bein liegt, zieh sie aus dem Gefahrenbereich."),
        };

        LayoutInflater inflater = LayoutInflater.from(this);

        try {
            for (Step step : steps) {
                View card = inflater.inflate(R.layout.uebungen_steps, container, false);
                ((TextView) card.findViewById(R.id.stepTitle)).setText(step.title);
                ((ImageView) card.findViewById(R.id.stepImage)).setImageResource(step.imageResId);
                ((TextView) card.findViewById(R.id.stepDescription)).setText(step.description);
                container.addView(card, container.getChildCount() - 1);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Fehler beim Anzeigen der Schritte. Bitte starte die Übung erneut.", Toast.LENGTH_LONG).show();
        }
    }
}
