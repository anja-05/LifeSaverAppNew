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
 * Die Aktivität {@code StabileSeitenlageActivity} zeigt eine Schritt-für-Schritt-Anleitung
 * zur Durchführung der stabilen Seitenlage. Jeder Schritt enthält einen Titel, ein Bild
 * und eine textliche Beschreibung.
 * Die Schritte werden dynamisch zur Benutzeroberfläche hinzugefügt.
 */
public class StabileSeitenlageActivity extends AppCompatActivity {

    /**
     * Repräsentiert einen einzelnen Schritt mit Titel, Beschreibung und Bild.
     */
    private static class Step {
        String title, description;
        int imageRes;

        /**
         * Erstellt ein neues Schritt-Objekt.
         * @param title Titel des Schrittes
         * @param description Beschreibung des Schrittes
         * @param imageRes Bildressource für diesen Schritt
         */
        Step(String title, String description, int imageRes) {
            this.title = title;
            this.description = description;
            this.imageRes = imageRes;
        }
    }

    /**
     * Wird beim Starten der Aktivität aufgerufen. Baut das Layout auf und fügt die Schritte ein.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stabile_seitenlage);

        LinearLayout container = findViewById(R.id.stepsContainer);

        Step[] steps = new Step[] {
                new Step(getString(R.string.stabile_step1_title), getString(R.string.stabile_step1_desc), R.drawable.stabile_seitenlage_schritt1),
                new Step(getString(R.string.stabile_step2_title), getString(R.string.stabile_step2_desc), R.drawable.stabile_seitenlage_schritt2),
                new Step(getString(R.string.stabile_step3_title), getString(R.string.stabile_step3_desc), R.drawable.stabile_seitenlage_schritt3),
                new Step(getString(R.string.stabile_step4_title), getString(R.string.stabile_step4_desc), R.drawable.stabile_seitenlage_schritt4),
                new Step(getString(R.string.stabile_step5_title), getString(R.string.stabile_step5_desc), R.drawable.stabile_seitenlage_schritt5)
        };

        LayoutInflater inflater = LayoutInflater.from(this);
        try {
            for (Step step : steps) {
                View card = inflater.inflate(R.layout.uebungen_steps, container, false);

                ((TextView) card.findViewById(R.id.stepTitle)).setText(step.title);
                ((TextView) card.findViewById(R.id.stepDescription)).setText(step.description);
                ((ImageView) card.findViewById(R.id.stepImage)).setImageResource(step.imageRes);

                container.addView(card, container.getChildCount() - 1);
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.stabile_steps_error), Toast.LENGTH_LONG).show();

        }
    }
}
