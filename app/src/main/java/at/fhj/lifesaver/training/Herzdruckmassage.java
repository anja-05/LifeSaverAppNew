package at.fhj.lifesaver.training;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        initializeTextToSpeech();
        initializeMetronome();

        showInstructionsScreen();
    }

    private void showInstructionsScreen() {
        setContentView(R.layout.activity_herzdruckmassage_intro);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewInstructions = findViewById(R.id.textViewInstructions);
        buttonStartTraining = findViewById(R.id.buttonStartTraining);

        textViewTitle.setText(R.string.cpr_instruction_title);
        textViewInstructions.setText(R.string.cpr_instruction_text);

        buttonStartTraining.setOnClickListener(v -> startTraining());
    }

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

    private void startTraining() {
        compressionCount = 0;
        startTime = System.currentTimeMillis();
        lastFeedbackTime = startTime;
        currentBPM = 110;
        currentQuality = 85;
        isTrainingActive = true;

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        startTimer();
        startMetronome();
        showTrainingScreen();
    }

    private void endTraining() {
        isTrainingActive = false;
        sensorManager.unregisterListener(this);
        if (timer != null) timer.cancel();
        stopMetronome();
        showSummaryScreen();
    }

    private void resetTraining() {
        isTrainingActive = false;
        sensorManager.unregisterListener(this);
        if (timer != null) timer.cancel();
        stopMetronome();
        compressionTimestamps.clear();
        lastCompressionTime = 0;
    }

    private void updateTrainingDisplay() {
        textViewBPM.setText(currentBPM + " BPM");
        textViewCompressionCount.setText(String.valueOf(compressionCount));
        textViewQuality.setText("Qualität: " + currentQuality + "%");
        progressBarQuality.setProgress(currentQuality);
    }

    private void updateSummaryDisplay() {
        long elapsed = System.currentTimeMillis() - startTime;
        String formatted = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(elapsed),
                TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60);

        textViewSummaryTime.setText("Trainingszeit: " + formatted + " Minuten");
        textViewSummaryCompressions.setText("Kompressionen: " + compressionCount);
        textViewSummaryBPM.setText("Durchschnittliches Tempo: " + currentBPM + " BPM");
        textViewSummaryQuality.setText("Qualitätsindikator: " + currentQuality + "%");

        if (currentBPM < 100) {
            textViewSummaryTip.setText("Versuche, das Tempo zu erhöhen. Ziel sind 100-120 Kompressionen pro Minute.");
        } else if (currentBPM > 120) {
            textViewSummaryTip.setText("Versuche, das Tempo etwas zu verlangsamen. Ziel sind 100-120 Kompressionen pro Minute.");
        } else {
            textViewSummaryTip.setText("Sehr gut! Achte weiterhin auf gleichmäßige Kompressionen und vollständige Entlastung.");
        }
    }

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

    private void initializeMetronome() {
        metronomeHandler = new Handler(Looper.getMainLooper());
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        metronomeRunnable = () -> {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
            metronomeHandler.postDelayed(metronomeRunnable, 60000 / 110);
        };
    }

    private void startMetronome() {
        metronomeHandler.post(metronomeRunnable);
    }

    private void stopMetronome() {
        metronomeHandler.removeCallbacks(metronomeRunnable);
    }

    private void startTimer() {
        timer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            public void onTick(long millisUntilFinished) {
                long elapsed = System.currentTimeMillis() - startTime;
                String time = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(elapsed),
                        TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60);
                textViewTimer.setText(time);

                if (System.currentTimeMillis() - lastFeedbackTime >= FEEDBACK_INTERVAL) {
                    provideFeedback();
                    lastFeedbackTime = System.currentTimeMillis();
                }
            }

            public void onFinish() {
            }
        }.start();
    }

    private void provideFeedback() {
        String tempo = currentBPM < 100 ? "Drücke schneller." : currentBPM > 120 ? "Drücke langsamer." : "Gut so. Halte das Tempo";
        speak(tempo);
    }

    private void speak(String text) {
        if (textToSpeech != null && isTrainingActive) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

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
                long sum = 0;
                for (Long i : compressionTimestamps) sum += i;
                if (!compressionTimestamps.isEmpty())
                    currentBPM = (int) (60000 / (sum / compressionTimestamps.size()));
            }

            lastCompressionTime = now;

            int bpmQ = 100 - Math.abs(currentBPM - 110) * 2;
            currentQuality = bpmQ;

            updateTrainingDisplay();
        } else if (isCompressing && acceleration < COMPRESSION_THRESHOLD - 2) {
            isCompressing = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (isTrainingActive) endTraining();
    }

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
