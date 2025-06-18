package at.fhj.lifesaver;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Herzdruckmassage extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "Herzdruckmassage";

    // UI-Elemente
    private TextView textViewTitle;
    private TextView textViewInstructions;
    private Button buttonStartTraining;
    private Button buttonEndTraining;
    private Button buttonTryAgain;
    private Button buttonBackToMenu;

    // Trainings-UI-Elemente
    private View layoutInstructions;
    private View layoutTraining;
    private View layoutSummary;

    private TextView textViewTimer;
    private TextView textViewBPM;
    private TextView textViewQuality;
    private TextView textViewCompressionCount;
    private ProgressBar progressBarDepth;
    private ProgressBar progressBarQuality;

    // Zusammenfassungs-UI-Elemente
    private TextView textViewSummaryCompressions;
    private TextView textViewSummaryTime;
    private TextView textViewSummaryBPM;
    private TextView textViewSummaryQuality;
    private TextView textViewSummaryTip;

    // Sensor-Manager und Sensoren
    private SensorManager sensorManager;
    private Sensor accelerometer;

    // Text-to-Speech für Audio-Feedback
    private TextToSpeech textToSpeech;

    // Timer und Handler
    private CountDownTimer timer;
    private Handler metronomeHandler;
    private Runnable metronomeRunnable;
    private ToneGenerator toneGenerator;

    // Variablen für das Training
    private long startTime;
    private int compressionCount;
    private int currentBPM;
    private int currentDepth;
    private int currentQuality;
    private boolean isTrainingActive;

    // Feedback-Intervall
    private static final long FEEDBACK_INTERVAL = 10000;
    private long lastFeedbackTime;

    // Sensor-Schwellenwerte
    private static final float COMPRESSION_THRESHOLD = 12.0f;
    private boolean isCompressing = false;
    private float lastAcceleration = 0;

    private long lastCompressionTime = 0;
    private ArrayList<Long> compressionTimestamps = new ArrayList<>();
    private float maxAcceleration = 0;
    private float minAcceleration = Float.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_herzdruckmassage);

        try {
            // Initialisiere UI-Elemente
            initializeViews();

            // Initialisiere Sensoren
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            // Initialisiere Text-to-Speech für Audio-Feedback
            initializeTextToSpeech();

            // Initialisiere Metronom
            initializeMetronome();

            // Setze Click-Listener für Buttons
            setButtonListeners();

            // Zeige Anweisungsbildschirm
            showInstructionsScreen();
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Initialisieren", e);
            Toast.makeText(this, "Fehler beim Initialisieren: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            finish(); // Beende die Activity bei einem Fehler
        }
    }

    private void initializeViews() {
        // Layouts
        layoutInstructions = findViewById(R.id.layoutInstructions);
        if (layoutInstructions == null) Log.e(TAG, "layoutInstructions nicht gefunden");

        layoutTraining = findViewById(R.id.layoutTraining);
        if (layoutTraining == null) Log.e(TAG, "layoutTraining nicht gefunden");

        layoutSummary = findViewById(R.id.layoutSummary);
        if (layoutSummary == null) Log.e(TAG, "layoutSummary nicht gefunden");

        // Textviews
        textViewTitle = findViewById(R.id.textViewTitle);
        if (textViewTitle == null) Log.e(TAG, "textViewTitle nicht gefunden");

        textViewInstructions = findViewById(R.id.textViewInstructions);
        if (textViewInstructions == null) Log.e(TAG, "textViewInstructions nicht gefunden");

        textViewTimer = findViewById(R.id.textViewTimer);
        if (textViewTimer == null) Log.e(TAG, "textViewTimer nicht gefunden");

        textViewBPM = findViewById(R.id.textViewBPM);
        if (textViewBPM == null) Log.e(TAG, "textViewBPM nicht gefunden");

        textViewQuality = findViewById(R.id.textViewQuality);
        if (textViewQuality == null) Log.e(TAG, "textViewQuality nicht gefunden");

        textViewCompressionCount = findViewById(R.id.textViewCompressionCount);
        if (textViewCompressionCount == null) Log.e(TAG, "textViewCompressionCount nicht gefunden");

        // Buttons
        buttonStartTraining = findViewById(R.id.buttonStartTraining);
        if (buttonStartTraining == null) Log.e(TAG, "buttonStartTraining nicht gefunden");

        buttonEndTraining = findViewById(R.id.buttonEndTraining);
        if (buttonEndTraining == null) Log.e(TAG, "buttonEndTraining nicht gefunden");

        buttonTryAgain = findViewById(R.id.buttonTryAgain);
        if (buttonTryAgain == null) Log.e(TAG, "buttonTryAgain nicht gefunden");

        buttonBackToMenu = findViewById(R.id.buttonBackToMenu);
        if (buttonBackToMenu == null) Log.e(TAG, "buttonBackToMenu nicht gefunden");

        // Progress Bars
        progressBarDepth = findViewById(R.id.progressBarDepth);
        if (progressBarDepth == null) Log.e(TAG, "progressBarDepth nicht gefunden");

        progressBarQuality = findViewById(R.id.progressBarQuality);
        if (progressBarQuality == null) Log.e(TAG, "progressBarQuality nicht gefunden");

        // Zusammenfassung
        textViewSummaryCompressions = findViewById(R.id.textViewSummaryCompressions);
        if (textViewSummaryCompressions == null) Log.e(TAG, "textViewSummaryCompressions nicht gefunden");

        textViewSummaryTime = findViewById(R.id.textViewSummaryTime);
        if (textViewSummaryTime == null) Log.e(TAG, "textViewSummaryTime nicht gefunden");

        textViewSummaryBPM = findViewById(R.id.textViewSummaryBPM);
        if (textViewSummaryBPM == null) Log.e(TAG, "textViewSummaryBPM nicht gefunden");

        textViewSummaryQuality = findViewById(R.id.textViewSummaryQuality);
        if (textViewSummaryQuality == null) Log.e(TAG, "textViewSummaryQuality nicht gefunden");

        textViewSummaryTip = findViewById(R.id.textViewSummaryTip);
        if (textViewSummaryTip == null) Log.e(TAG, "textViewSummaryTip nicht gefunden");
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.GERMAN);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback auf Englisch, falls Deutsch nicht verfügbar
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });
        textToSpeech.setSpeechRate(0.9f); // Etwas langsamer sprechen
    }

    private void initializeMetronome() {
        metronomeHandler = new Handler(Looper.getMainLooper());
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        metronomeRunnable = new Runnable() {
            @Override
            public void run() {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
                metronomeHandler.postDelayed(this, calculateMetronomeInterval());
            }
        };
    }

    private long calculateMetronomeInterval() {
        // 110 BPM = 60000ms / 110 = 545ms zwischen den Beats
        return 60000 / 110;
    }

    private void setButtonListeners() {
        try {
            if (buttonStartTraining != null) {
                buttonStartTraining.setOnClickListener(v -> {
                    startTraining();
                });
            } else {
                Log.e(TAG, "buttonStartTraining ist null");
            }

            if (buttonEndTraining != null) {
                buttonEndTraining.setOnClickListener(v -> {
                    endTraining();
                });
            } else {
                Log.e(TAG, "buttonEndTraining ist null");
            }

            if (buttonTryAgain != null) {
                buttonTryAgain.setOnClickListener(v -> {
                    resetTraining();
                    showInstructionsScreen();
                });
            } else {
                Log.e(TAG, "buttonTryAgain ist null");
            }

            if (buttonBackToMenu != null) {
                buttonBackToMenu.setOnClickListener(v -> {
                    finish(); // Zurück zur vorherigen Activity
                });
            } else {
                Log.e(TAG, "buttonBackToMenu ist null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Setzen der Button-Listener", e);
            Toast.makeText(this, "Fehler beim Setzen der Button-Listener: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showInstructionsScreen() {
        layoutInstructions.setVisibility(View.VISIBLE);
        layoutTraining.setVisibility(View.GONE);
        layoutSummary.setVisibility(View.GONE);

        textViewTitle.setText("CPR Training - Anleitung");
        textViewInstructions.setText("Platziere dein Smartphone auf deinem Handrücken oder am Handgelenk – mit dem Display nach oben. " +
                "So kann das Gerät deine Bewegungen beim Drücken optimal erfassen.");
    }

    private void showTrainingScreen() {
        layoutInstructions.setVisibility(View.GONE);
        layoutTraining.setVisibility(View.VISIBLE);
        layoutSummary.setVisibility(View.GONE);

        textViewTitle.setText("CPR Training läuft");
        updateTrainingDisplay();
    }

    private void showSummaryScreen() {
        layoutInstructions.setVisibility(View.GONE);
        layoutTraining.setVisibility(View.GONE);
        layoutSummary.setVisibility(View.VISIBLE);

        textViewTitle.setText("Zusammenfassung");
        updateSummaryDisplay();
    }

    private void startTraining() {
        // Zurücksetzen der Werte
        compressionCount = 0;
        startTime = System.currentTimeMillis();
        lastFeedbackTime = startTime;
        currentBPM = 110;
        currentDepth = 75;
        currentQuality = 85;
        isTrainingActive = true;

        // Starte Sensoren
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        // Starte Timer
        startTimer();

        // Starte Metronom
        startMetronome();

        // Zeige Trainingsbildschirm
        showTrainingScreen();
    }

    private void endTraining() {
        isTrainingActive = false;

        // Stoppe Sensoren
        sensorManager.unregisterListener(this);

        // Stoppe Timer
        if (timer != null) {
            timer.cancel();
        }

        // Stoppe Metronom
        stopMetronome();

        // Zeige Zusammenfassung
        showSummaryScreen();
    }

    private void resetTraining() {
        compressionCount = 0;
        isTrainingActive = false;

        // Stoppe Sensoren
        sensorManager.unregisterListener(this);

        // Stoppe Timer
        if (timer != null) {
            timer.cancel();
        }

        // Stoppe Metronom
        stopMetronome();
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimerDisplay();

                // Prüfe, ob Feedback gegeben werden soll
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastFeedbackTime >= FEEDBACK_INTERVAL) {
                    provideFeedback();
                    lastFeedbackTime = currentTime;
                }
            }

            @Override
            public void onFinish() {
                // Wird nie aufgerufen, da wir Long.MAX_VALUE verwenden
            }
        }.start();
    }

    private void updateTimerDisplay() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        String formattedTime = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
                TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60);
        textViewTimer.setText(formattedTime);
    }

    private void startMetronome() {
        metronomeHandler.post(metronomeRunnable);
    }

    private void stopMetronome() {
        metronomeHandler.removeCallbacks(metronomeRunnable);
    }

    private void updateTrainingDisplay() {
        // Aktualisiere Anzeigen
        textViewBPM.setText(currentBPM + " BPM");
        textViewQuality.setText("Qualität: " + currentQuality + "%");
        textViewCompressionCount.setText("" + compressionCount);

        // Aktualisiere Fortschrittsbalken
        progressBarDepth.setProgress(currentDepth);
        progressBarQuality.setProgress(currentQuality);
    }

    private void updateSummaryDisplay() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        String formattedTime = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
                TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60);

        textViewSummaryTime.setText("Trainingszeit: " + formattedTime + " Minuten");
        textViewSummaryCompressions.setText("Kompressionen: " + compressionCount);
        textViewSummaryBPM.setText("Durchschnittliches Tempo: " + currentBPM + " BPM");
        textViewSummaryQuality.setText("Qualitätsindikator: " + currentQuality + "%");

        // Setze Verbesserungstipp basierend auf der Performance
        if (currentBPM < 100) {
            textViewSummaryTip.setText("Versuche, das Tempo zu erhöhen. Ziel sind 100-120 Kompressionen pro Minute.");
        } else if (currentBPM > 120) {
            textViewSummaryTip.setText("Versuche, das Tempo etwas zu verlangsamen. Ziel sind 100-120 Kompressionen pro Minute.");
        } else if (currentDepth < 60) {
            textViewSummaryTip.setText("Achte auf eine ausreichende Drucktiefe von 5-6 cm bei jeder Kompression.");
        } else {
            textViewSummaryTip.setText("Sehr gut! Achte weiterhin auf gleichmäßige Kompressionen und vollständige Entlastung.");
        }
    }

    private void provideFeedback() {
        String feedback;
        if (currentBPM < 100) feedback = "Drücke schneller.";
        else if (currentBPM > 120) feedback = "Drücke langsamer.";
        else if (currentDepth < 60) feedback = "Drücke tiefer.";
        else if (currentDepth > 85) feedback = "Du drückst zu tief.";
        else if (currentQuality < 70) feedback = "Achte auf gleichmäßige Kompressionen.";
        else feedback = "Sehr gut – halte das Tempo!";
        speak(feedback);
    }

    private void speak(String text) {
        if (textToSpeech != null && isTrainingActive) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isTrainingActive || event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        // Berechne Gesamtbeschleunigung
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

        // Erkennung einer Kompression
        if (!isCompressing && acceleration > COMPRESSION_THRESHOLD) {
            isCompressing = true;
            compressionCount++;
            long currentTime = System.currentTimeMillis();

            if (lastCompressionTime != 0) {
                long interval = currentTime - lastCompressionTime;
                compressionTimestamps.add(interval);
                if (compressionTimestamps.size() > 5) compressionTimestamps.remove(0);
                long avgInterval = 0;
                for (Long i : compressionTimestamps) avgInterval += i;
                avgInterval /= compressionTimestamps.size();
                currentBPM = (int) (60000 / avgInterval);
            }
            lastCompressionTime = currentTime;

            float depth = maxAcceleration - minAcceleration;
            currentDepth = Math.min(100, Math.max(0, (int) (depth * 10)));
            maxAcceleration = 0;
            minAcceleration = Float.MAX_VALUE;

            int bpmQuality = 100 - Math.abs(currentBPM - 110) * 2;
            int depthQuality = 100 - Math.abs(currentDepth - 70);
            currentQuality = (bpmQuality + depthQuality) / 2;

            updateTrainingDisplay();
        } else if (isCompressing && acceleration < COMPRESSION_THRESHOLD - 2) {
            isCompressing = false;
        }

        maxAcceleration = Math.max(maxAcceleration, acceleration);
        minAcceleration = Math.min(minAcceleration, acceleration);
        lastAcceleration = acceleration;
    }
/*
    private void simulateSensorData() {
        // Simuliere BPM zwischen 100-120
        Random random = new Random();
        currentBPM = 110 + random.nextInt(21) - 10;

        // Simuliere Tiefe zwischen 65-85%
        currentDepth = 75 + random.nextInt(21) - 10;

        // Berechne Qualität basierend auf BPM und Tiefe
        int bpmQuality = 100 - Math.abs(currentBPM - 110) * 2;
        int depthQuality = 100 - Math.abs(currentDepth - 75) * 2;
        currentQuality = (bpmQuality + depthQuality) / 2;
    }*/

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nicht benötigt
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTrainingActive) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (isTrainingActive) {
            endTraining();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (toneGenerator != null) {
            toneGenerator.release();
        }
    }
}