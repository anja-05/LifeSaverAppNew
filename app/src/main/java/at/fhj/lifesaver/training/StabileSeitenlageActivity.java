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
                new Step("1. Ausgangsposition einnehmen", "Knie dich seitlich neben die bewusstlose Person. Achte darauf, dass sie flach auf dem Rücken liegt und ihre Beine ausgestreckt sind.\n" +
                        "Jetzt nimm den Arm, der dir am nächsten ist, und lege ihn angewinkelt neben den Kopf der Person. Die Handfläche soll nach oben zeigen.", R.drawable.stabile_seitenlage_schritt1),
                new Step("2. Arm und Bein positionieren", "Nimm nun den anderen Arm der Person am Handgelenk und lege ihn schräg über ihre Brust, sodass die Handoberfläche die gegenüberliegende Wange berührt. Halte die Hand dabei gut fest – sie darf nicht wegrutschen.\n" +
                        "Dann greifst du das Bein, das weiter von dir entfernt ist, ziehst es am Knie nach oben und stellst den Fuß auf den Boden. Das Bein ist jetzt angewinkelt.", R.drawable.stabile_seitenlage_schritt2),
                new Step("3. Person auf die Seite drehen", "Fasse jetzt mit deiner rechten Hand das angewinkelte Bein und ziehe es vorsichtig zu dir herüber. Dadurch dreht sich die Person automatisch auf die Seite.\n" +
                        "Achte darauf, dass das obenliegende Bein im rechten Winkel zur Hüfte liegt – es sollte wie ein „L“ geformt sein und stabil liegen.", R.drawable.stabile_seitenlage_schritt3),
                new Step("4. Mund öffnen", "Der Kopf der Person liegt jetzt auf ihrer eigenen Hand. Öffne den Mund leicht, damit Erbrochenes oder Flüssigkeiten abfließen können, und richte den Mund dabei leicht zum Boden aus.\n" +
                        "\n", R.drawable.stabile_seitenlage_schritt4),
                new Step("5. Kopf überstrecken", "Richte den Kopf der betroffenen Person so aus, dass er etwas überstreckt auf ihrer Hand liegt und permanent überstreckt bleibt.", R.drawable.stabile_seitenlage_schritt5)

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
            Toast.makeText(this, "Fehler beim Laden der Anleitung. Bitte App neu starten.", Toast.LENGTH_LONG).show();
        }
    }
}
