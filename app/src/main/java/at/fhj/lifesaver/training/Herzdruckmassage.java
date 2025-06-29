package at.fhj.lifesaver.training;

import android.content.Context;
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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import at.fhj.lifesaver.R;

/**
 * Activity für das Herzdruckmassage-Training mit Sensor-Unterstützung.
 * Ermöglicht es dem Benutzer, Herzdruckmassage-Techniken zu üben und erhält
 * Echtzeit-Feedback über Kompressionsfrequenz und -qualität.
 */
public class Herzdruckmassage extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextToSpeech textToSpeech;
    private ToneGenerator toneGenerator;
    private Handler metronomeHandler;
    private Runnable metronomeRunnable;

    private CountDownTimer timer;
    private long startTime;
    private int compressionCount;
    private int currentBPM;
    private int currentQuality;
    private boolean isTrainingActive;

    private static final long FEEDBACK_INTERVAL = 10000;
    private long lastFeedbackTime;

    private static final float COMPRESSION_THRESHOLD = 12.0f;
    private boolean isCompressing = false;
    private long lastCompressionTime = 0;
    private final ArrayList<Long> compressionTimestamps = new ArrayList<>();

    private TextView textViewTitle, textViewInstructions, textViewTimer, textViewBPM,
            textViewQuality, textViewCompressionCount,
            textViewSummaryCompressions, textViewSummaryTime,
            textViewSummaryBPM, textViewSummaryQuality, textViewSummaryTip;
    private ProgressBar progressBarQuality;
    private Button buttonStartTraining, buttonEndTraining, buttonTryAgain, buttonBackToMenu;

    /**
     * Initialisiert die Activity und startet das Training.
     * Setzt Sensoren, Text-to-Speech und Metronom auf.
     * 
     * @param savedInstanceState Bundle mit gespeicherten Daten
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        initializeTextToSpeech();
        initializeMetronome();
        showInstructionsScreen();
    }

    /**
     * Zeigt den Anleitungsbildschirm an.
     * Setzt die UI-Elemente und den Start-Button.
     */
    private void showInstructionsScreen() {
        setContentView(R.layout.activity_herzdruckmassage_intro);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewInstructions = findViewById(R.id.textViewInstructions);
        buttonStartTraining = findViewById(R.id.buttonStartTraining);

        textViewTitle.setText(R.string.cpr_instruction_title);
        textViewInstructions.setText(R.string.cpr_instruction_text);

        buttonStartTraining.setOnClickListener(v -> startTraining());
    }

    /**
     * Zeigt den Trainingsbildschirm an.
     * Initialisiert alle UI-Elemente für das laufende Training.
     */
    private void showTrainingScreen() {
        setContentView(R.layout.activity_herzdruckmassage_training);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewTimer = findViewById(R.id.textViewTimer);
        textViewBPM = findViewById(R.id.textViewBPM);
        textViewCompressionCount = findViewById(R.id.textViewCompressionCount);
        textViewQuality = findViewById(R.id.textViewQuality);
        progressBarQuality = findViewById(R.id.progressBarQuality);
        buttonEndTraining = findViewById(R.id.buttonEndTraining);

        buttonEndTraining.setOnClickListener(v -> endTraining());
        updateTrainingDisplay();
    }

    /**
     * Zeigt den Zusammenfassungsbildschirm an.
     * Initialisiert alle UI-Elemente für die Trainingszusammenfassung.
     */
    private void showSummaryScreen() {
        setContentView(R.layout.activity_herzdruckmassage_summary);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewSummaryTime = findViewById(R.id.textViewSummaryTime);
        textViewSummaryCompressions = findViewById(R.id.textViewSummaryCompressions);
        textViewSummaryBPM = findViewById(R.id.textViewSummaryBPM);
        textViewSummaryQuality = findViewById(R.id.textViewSummaryQuality);
        textViewSummaryTip = findViewById(R.id.textViewSummaryTip);
        buttonTryAgain = findViewById(R.id.buttonTryAgain);
        buttonBackToMenu = findViewById(R.id.buttonBackToMenu);

        buttonTryAgain.setOnClickListener(v -> {
            resetTraining();
            showInstructionsScreen();
        });

        buttonBackToMenu.setOnClickListener(v -> finish());
        updateSummaryDisplay();
    }

    /**
     * Startet das Herzdruckmassage-Training.
     * Initialisiert alle Werte und startet Sensoren, Timer und Metronom.
     */
    private void startTraining() {
        compressionCount = 0;
        startTime = System.currentTimeMillis();
        lastFeedbackTime = startTime;
        currentBPM = 0;
        currentQuality = 0;
        isTrainingActive = true;

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        startTimer();
        startMetronome();
        showTrainingScreen();
    }

    /**
     * Beendet das Training und zeigt die Zusammenfassung an.
     * Stoppt alle Sensoren, Timer und das Metronom.
     */
    private void endTraining() {
        isTrainingActive = false;
        sensorManager.unregisterListener(this);
        if (timer != null) timer.cancel();
        stopMetronome();
        showSummaryScreen();
    }

    /**
     * Setzt das Training zurück.
     * Stoppt alle Sensoren und Timer und löscht alle gespeicherten Daten.
     */
    private void resetTraining() {
        isTrainingActive = false;
        sensorManager.unregisterListener(this);
        if (timer != null) timer.cancel();
        stopMetronome();
        compressionTimestamps.clear();
        lastCompressionTime = 0;
        currentBPM = 0;
        currentQuality = 0;
    }

    /**
     * Aktualisiert die Anzeige im Trainingsbildschirm.
     * Zeigt aktuelle BPM, Kompressionsanzahl und Qualität an.
     */
    private void updateTrainingDisplay() {
        textViewBPM.setText(getString(R.string.bpm_label, currentBPM));
        textViewCompressionCount.setText(String.valueOf(compressionCount));
        textViewQuality.setText(getString(R.string.quality_label, currentQuality));
        progressBarQuality.setProgress(currentQuality);
    }

    /**
     * Aktualisiert die Anzeige im Zusammenfassungsbildschirm.
     * Zeigt Trainingszeit, Kompressionsanzahl, BPM, Qualität und Tipps an.
     */
    private void updateSummaryDisplay() {
        long elapsed = System.currentTimeMillis() - startTime;
        String formatted = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(elapsed),
                TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60);

        textViewSummaryTime.setText(getString(R.string.training_time_label, formatted));
        textViewSummaryCompressions.setText(getString(R.string.compressions_label, compressionCount));
        
        if (currentBPM == 0) {
            textViewSummaryBPM.setText(getString(R.string.bpm_no_compressions));
            textViewSummaryQuality.setText(getString(R.string.quality_no_compressions));
            textViewSummaryTip.setText(getString(R.string.tip_no_compressions));
        } else {
            textViewSummaryBPM.setText(getString(R.string.bpm_avg_label, currentBPM));
            textViewSummaryQuality.setText(getString(R.string.quality_indicator_label, currentQuality));

            if (currentBPM < 100) {
                textViewSummaryTip.setText(getString(R.string.tip_too_slow));
            } else if (currentBPM > 120) {
                textViewSummaryTip.setText(getString(R.string.tip_too_fast));
            } else {
                textViewSummaryTip.setText(getString(R.string.tip_perfect));
            }
        }
    }

    /**
     * Initialisiert Text-to-Speech mit deutscher Sprache.
     * Falls Deutsch nicht verfügbar ist, wird auf Englisch zurückgegriffen.
     */
    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.GERMAN);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });
        textToSpeech.setSpeechRate(0.9f);
    }

    /**
     * Initialisiert den Metronom-Taktgeber.
     * Erstellt einen Metronom mit 130 BPM für das Training.
     */
    private void initializeMetronome() {
        metronomeHandler = new Handler(Looper.getMainLooper());
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        metronomeRunnable = () -> {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
            metronomeHandler.postDelayed(metronomeRunnable, 60000 / 115);
        };
    }

    /**
     * Startet den Metronom-Takt.
     */
    private void startMetronome() {
        metronomeHandler.post(metronomeRunnable);
    }

    /**
     * Stoppt das Metronom.
     */
    private void stopMetronome() {
        metronomeHandler.removeCallbacks(metronomeRunnable);
    }

    /**
     * Startet den Timer zur Anzeige der Trainingsdauer und Feedback-Taktung.
     * Aktualisiert die Zeitanzeige jede Sekunde und prüft alle 2 Sekunden den BPM-Reset.
     */
    private void startTimer() {
        timer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            public void onTick(long millisUntilFinished) {
                long elapsed = System.currentTimeMillis() - startTime;
                String time = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(elapsed),
                        TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60);
                textViewTimer.setText(time);

                if (elapsed % 2000 < 1000) {
                    checkAndResetBPM();
                }

                if (System.currentTimeMillis() - lastFeedbackTime >= FEEDBACK_INTERVAL) {
                    provideFeedback();
                    lastFeedbackTime = System.currentTimeMillis();
                }
            }
            public void onFinish() {
            }
        }.start();
    }

    /**
     * Prüft, ob der BPM zurückgesetzt werden soll.
     * Setzt BPM und Qualität auf 0, wenn seit mehr als 3 Sekunden keine Kompression erkannt wurde.
     */
    private void checkAndResetBPM() {
        if (lastCompressionTime != 0) {
            long timeSinceLastCompression = System.currentTimeMillis() - lastCompressionTime;
            if (timeSinceLastCompression > 3000) {
                currentBPM = 0;
                currentQuality = 0;
                updateTrainingDisplay();
            }
        }
    }

    /**
     * Gibt akustisches Feedback zur Kompressionsfrequenz.
     * Spricht entsprechende Anweisungen basierend auf der aktuellen BPM aus.
     */
    private void provideFeedback() {
        String tempo;
        if (currentBPM == 0) {
            tempo = getString(R.string.speak_start_compressions);
        } else if (currentBPM < 100) {
            tempo = getString(R.string.speak_faster);
        } else if (currentBPM > 120) {
            tempo = getString(R.string.speak_slower);
        } else {
            tempo = getString(R.string.speak_good);
        }
        speak(tempo);
    }

    /**
     * Spricht den übergebenen Text mittels TextToSpeech aus.
     * 
     * @param text der auszugebende Text
     */
    private void speak(String text) {
        if (textToSpeech != null && isTrainingActive) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    /**
     * Wird bei Sensoränderung aufgerufen. Misst Kompressionen und berechnet BPM.
     * Erkennt Kompressionen basierend auf Beschleunigungsschwellenwerten.
     * 
     * @param event das SensorEvent mit den Beschleunigungsdaten
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isTrainingActive || event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float x = event.values[0], y = event.values[1], z = event.values[2];
        float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

        if (!isCompressing && acceleration > COMPRESSION_THRESHOLD) {
            isCompressing = true;
            compressionCount++;
            long now = System.currentTimeMillis();

            if (lastCompressionTime != 0) {
                long interval = now - lastCompressionTime;
                compressionTimestamps.add(interval);
                
                if (compressionTimestamps.size() > 10) {
                    compressionTimestamps.remove(0);
                }
                
                if (!compressionTimestamps.isEmpty()) {
                    long sum = 0;
                    for (Long i : compressionTimestamps) sum += i;
                    long averageInterval = sum / compressionTimestamps.size();
                    currentBPM = (int) (60000 / averageInterval);
                }
            }

            lastCompressionTime = now;
            int bpmQ = 100 - Math.abs(currentBPM - 110) * 2;
            currentQuality = Math.max(0, Math.min(100, bpmQ));
            updateTrainingDisplay();
        } else if (isCompressing && acceleration < COMPRESSION_THRESHOLD - 2) {
            isCompressing = false;
        }
    }

    /**
     * Wird aufgerufen, wenn sich die Sensorgenauigkeit ändert.
     * Diese Methode wird in dieser Implementierung nicht verwendet.
     * 
     * @param sensor der Sensor, dessen Genauigkeit sich geändert hat
     * @param accuracy die neue Genauigkeit des Sensors
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Beendet das Training bei Pause der Activity.
     * Stoppt Sensoren und beendet das Training automatisch.
     */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (isTrainingActive) endTraining();
    }

    /**
     * Bereinigt Ressourcen bei Aktivitätszerstörung.
     * Stoppt TextToSpeech und gibt ToneGenerator frei.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (toneGenerator != null) toneGenerator.release();
    }
}
