package at.fhj.lifesaver.quiz;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import android.content.Intent;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import at.fhj.lifesaver.R;
import at.fhj.lifesaver.lesson.LektionDetailActivity;


/**
 * Die Klasse QuizActivity stellt ein interaktives Multiple-Choice-Quiz basierend auf einer JSON-Datei bereit.
 * Die Fragen werden aus den assets unter quiz Verzeichnis geladen, der Fortschritt gespeichert und
 * das Feedback visuell sowie textlich dargestellt.
 * Die Fragen werden dynamisch geladen (aus JSON-Datei je Thema, der Punktestand berechnet und visuell angezeigt,
 * die Benutzerauswahl visuell hervorgehoben und begründet, die Zusammenfassung am Ende mit richtiger/falscher Antwort gezeigt und
 * der Fortschritt gespeichert, falls alle Fragen korrekt beantwortet wurden.
 * Das Quiz verwendet als Datenmodell  "QuizFrage" mit Frage, Optionen, Erklärung. Der Dateiname wird dynamisch aus dem Thema gebildet.
 */
public class QuizActivity extends AppCompatActivity {

        private TextView tvTopicTitle, tvQuestionCounter, tvScore, tvQuestion, tvExplanation;
        private ProgressBar progressBar;
        private Button btnPrev, btnNext;
        private MaterialCardView option1, option2, option3, option4;
        private TextView tvOption1, tvOption2, tvOption3, tvOption4;
        private View explanationContainer;
        private ConstraintLayout quizContainer, resultsContainer;
        private TextView tvFinalScore, tvFeedback;
        private Button btnRestart, btnReturn;
        private LinearLayout questionsSummaryContainer;

        private List<QuizFrage> quizQuestions;
        private int currentQuestionIndex = 0;
        private int score = 0;
        private boolean[] answeredQuestions;
        private int[] userAnswers;
        private boolean questionAnswered = false;
        private String topicTitle;
        private String dateiname;

    /**
     * Wird beim Start der Aktivität aufgerufen.
     * Liest das Thema und den Dateinamen aus dem Intent, initialisiert die UI-Komponenten,
     * lädt die Quizdaten aus der passenden JSON-Datei und richtet Listener ein.
     * @param savedInstanceState Zustand der Aktivität bei Rekonstruktion
     *
     */
    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_quiz);

            Intent intent = getIntent();
            topicTitle = intent.getStringExtra("TOPIC_TITLE");
            dateiname = intent.getStringExtra("DATEINAME");
            if (topicTitle == null) {
                topicTitle = "Quiz";
            }

            initializeViews();

            initializeQuizData();

            setupListeners();

            loadQuestion(0);
        }

    /**
     * Initialisiert alle View-Elemente der Aktivität inklusive Buttons, TextViews und Layoutcontainer.
     * Außerdem wird der Titel entsprechend dem übergebenen Thema gesetzt.
     */
    private void initializeViews() {
            tvTopicTitle = findViewById(R.id.tv_topic_title);
            tvQuestionCounter = findViewById(R.id.tv_question_counter);
            tvScore = findViewById(R.id.tv_score);
            progressBar = findViewById(R.id.progress_bar);
            tvQuestion = findViewById(R.id.tv_question);

            option1 = findViewById(R.id.card_option_1);
            option2 = findViewById(R.id.card_option_2);
            option3 = findViewById(R.id.card_option_3);
            option4 = findViewById(R.id.card_option_4);

            tvOption1 = findViewById(R.id.tv_option_1);
            tvOption2 = findViewById(R.id.tv_option_2);
            tvOption3 = findViewById(R.id.tv_option_3);
            tvOption4 = findViewById(R.id.tv_option_4);

            explanationContainer = findViewById(R.id.explanation_container);
            tvExplanation = findViewById(R.id.tv_explanation);

            btnPrev = findViewById(R.id.btn_prev);
            btnNext = findViewById(R.id.btn_next);

            quizContainer = findViewById(R.id.quiz_container);
            resultsContainer = findViewById(R.id.results_container);

            tvFinalScore = findViewById(R.id.tv_final_score);
            tvFeedback = findViewById(R.id.tv_feedback);

            btnRestart = findViewById(R.id.btn_restart);
            btnReturn = findViewById(R.id.btn_return);

            questionsSummaryContainer = findViewById(R.id.questions_summary_container);

            tvTopicTitle.setText(getString(R.string.quiz_prefix) + topicTitle);
        }

    /**
     * Lädt die Quizfragen aus einer JSON-Datei im assets-Ordner anhand des aktuellen Themas.
     * Bei Fehlern (z.B. Datei nicht vorhanden oder leer) wird die Aktivität beendet.
     * Initialisiert auch Arrays zur Fortschrittsverfolgung.
     */
    private void initializeQuizData() {
            quizQuestions = new ArrayList<>();

            try {
                String fileName = "quiz/" + topicTitle.replaceAll("[^a-zA-Z0-9]", "") + ".json";
                InputStream is = getAssets().open(fileName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                Gson gson = new Gson();

                QuizFrage[] frageArray = gson.fromJson(reader, QuizFrage[].class);
                quizQuestions = Arrays.asList(frageArray);

            } catch (IOException e) {
                Toast.makeText(this, getString(R.string.quiz_loading_error, topicTitle), Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            if (quizQuestions.isEmpty()) {
                Toast.makeText(this, getString(R.string.quiz_no_questions, topicTitle), Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            answeredQuestions = new boolean[quizQuestions.size()];
            userAnswers = new int[quizQuestions.size()];
            for (int i = 0; i < userAnswers.length; i++) {
                userAnswers[i] = -1;
            }

            tvQuestionCounter.setText("Frage 1 von " + quizQuestions.size());
            tvScore.setText("Punkte: 0/" + quizQuestions.size());

            loadQuestion(0);
        }

    /**
     * Richtet alle ClickListener ein: Antwortmöglichkeiten, Navigation: Zurück, Weiter,Quiz zurücksetzen, Quiz verlassen
     */
    private void setupListeners() {
            MaterialCardView[] optionCards = {option1, option2, option3, option4};
            for (int i = 0; i < optionCards.length; i++) {
                final int optionIndex = i;
                optionCards[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleOptionClick(optionIndex);
                    }
                });
            }

            btnPrev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentQuestionIndex == 0) {
                        Intent backIntent = new Intent(QuizActivity.this, LektionDetailActivity.class);
                        backIntent.putExtra("TITEL", topicTitle);
                        backIntent.putExtra("DATEINAME", dateiname);
                        finish();
                    } else {
                        loadQuestion(currentQuestionIndex - 1);
                    }
                }
            });


            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!answeredQuestions[currentQuestionIndex]) {
                        Toast.makeText(QuizActivity.this, getString(R.string.select_answer_warning), Toast.LENGTH_SHORT).show();

                        return;
                    }

                    if (currentQuestionIndex < quizQuestions.size() - 1) {
                        loadQuestion(currentQuestionIndex + 1);
                    } else {
                        showResults();
                    }
                }
            });

            btnRestart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restartQuiz();
                }
            });

            btnReturn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

    /**
     * Lädt eine Quizfrage anhand des Index, zeigt Optionen, Fortschritt und ggf. Feedback an.
     * @param index Index der Frage in der Liste
     */
    private void loadQuestion(int index) {
            currentQuestionIndex = index;

            tvQuestionCounter.setText("Frage " + (index + 1) + " von " + quizQuestions.size());

            int progress = (int) (((float) (index + 1) / quizQuestions.size()) * 100);
            progressBar.setProgress(progress);

            QuizFrage question = quizQuestions.get(index);

            tvQuestion.setText(question.getQuestion());
            TextView[] optionTextViews = {tvOption1, tvOption2, tvOption3, tvOption4};
            String[] options = question.getOptions();
            for (int i = 0; i < optionTextViews.length; i++) {
                optionTextViews[i].setText(options[i]);
            }

            MaterialCardView[] optionCards = {option1, option2, option3, option4};
            for (MaterialCardView card : optionCards) {
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
                card.setStrokeColor(ContextCompat.getColor(this, R.color.gray_300));
                card.setEnabled(true);
            }


            explanationContainer.setVisibility(View.GONE);

            questionAnswered = answeredQuestions[index];
            if (questionAnswered) {
                int selectedIndex = userAnswers[index];
                int correctIndex = question.getCorrectIndex();

                for (MaterialCardView card : optionCards) {
                    card.setEnabled(false);
                }

                if (selectedIndex == correctIndex) {
                    optionCards[selectedIndex].setCardBackgroundColor(getResources().getColor(R.color.green_100));
                    optionCards[selectedIndex].setStrokeColor(getResources().getColor(R.color.green_500));
                } else {
                    optionCards[selectedIndex].setCardBackgroundColor(getResources().getColor(R.color.red_100));
                    optionCards[selectedIndex].setStrokeColor(getResources().getColor(R.color.red_500));

                    optionCards[correctIndex].setCardBackgroundColor(getResources().getColor(R.color.green_100));
                    optionCards[correctIndex].setStrokeColor(getResources().getColor(R.color.green_500));
                }

                tvExplanation.setText(question.getExplanation());
                explanationContainer.setVisibility(View.VISIBLE);
            }

            updateButtonStates();
        }

    /**
     * Verarbeitet die Benutzerantwort: Markiert die gewählte Option, vergleicht mit der korrekten Antwort, aktualisiert den Punktestand, zeigt Erklärung an.
     * @param optionIndex Index der angeklickten Antwortoption
     */
    private void handleOptionClick(int optionIndex) {
            if (questionAnswered) return;

            questionAnswered = true;
            answeredQuestions[currentQuestionIndex] = true;
            userAnswers[currentQuestionIndex] = optionIndex;

            QuizFrage question = quizQuestions.get(currentQuestionIndex);
            int correctIndex = question.getCorrectIndex();

            boolean isCorrect = (optionIndex == correctIndex);

            if (isCorrect) {
                score++;
                tvScore.setText("Punkte: " + score + "/" + quizQuestions.size());
            }

            MaterialCardView[] optionCards = {option1, option2, option3, option4};
            for (MaterialCardView card : optionCards) {
                card.setEnabled(false);
            }

            if (isCorrect) {
                optionCards[optionIndex].setCardBackgroundColor(getResources().getColor(R.color.green_100));
                optionCards[optionIndex].setStrokeColor(getResources().getColor(R.color.green_500));
            } else {
                optionCards[optionIndex].setCardBackgroundColor(getResources().getColor(R.color.red_100));
                optionCards[optionIndex].setStrokeColor(getResources().getColor(R.color.red_500));

                optionCards[correctIndex].setCardBackgroundColor(getResources().getColor(R.color.green_100));
                optionCards[correctIndex].setStrokeColor(getResources().getColor(R.color.green_500));
            }

            tvExplanation.setText(question.getExplanation());
            explanationContainer.setVisibility(View.VISIBLE);

            updateButtonStates();
        }

    /**
     * Passt den Text und Status der Buttons (Weiter/Ergebnis anzeigen) abhängig vom aktuellen Fortschritt an.
     */
    private void updateButtonStates() {
        btnPrev.setEnabled(true);

        btnNext.setText(currentQuestionIndex == quizQuestions.size() - 1
                ? getString(R.string.show_result_button)
                : getString(R.string.next_button));
        }

    /**
     * Zeigt das Ergebnis nach Beendigung des Quiz: Gesamtpunktzahl, individuelle Rückmeldung, Fragenzusammenfassung mit korrekter und gewählter Antwort, speichert Fortschritt bei 100 % richtigen Antworten.
     */
    private void showResults() {
            quizContainer.setVisibility(View.GONE);
            resultsContainer.setVisibility(View.VISIBLE);

            tvFinalScore.setText(score + "/" + quizQuestions.size());

            if (score == quizQuestions.size()) {
                SharedPreferences userPrefs = getSharedPreferences("user_data", MODE_PRIVATE);
                String userEmail = userPrefs.getString("user_email", "default");

                SharedPreferences progressPrefs = getSharedPreferences("progress_" + userEmail, MODE_PRIVATE);
                SharedPreferences.Editor editor = progressPrefs.edit();
                editor.putBoolean("lesson_" + topicTitle, true);
                editor.apply();
            }


            float percentage = (float) score / quizQuestions.size() * 100;
            if (percentage == 100) {
                tvFeedback.setText(getString(R.string.result_perfect));
            } else if (percentage >= 80) {
                tvFeedback.setText(getString(R.string.result_very_good));
            } else if (percentage >= 60) {
                tvFeedback.setText(getString(R.string.result_good));
            } else if (percentage >= 40) {
                tvFeedback.setText(getString(R.string.result_practice_needed));
            } else {
                tvFeedback.setText(getString(R.string.result_try_again));
            }

            questionsSummaryContainer.removeAllViews();
            for (int i = 0; i < quizQuestions.size(); i++) {
                QuizFrage question = quizQuestions.get(i);
                int userAnswer = userAnswers[i];
                boolean isCorrect = userAnswer == question.getCorrectIndex();

                View summaryItemView = getLayoutInflater().inflate(R.layout.item_question_summary, questionsSummaryContainer, false);

                TextView tvQuestionTitle = summaryItemView.findViewById(R.id.tv_question_title);
                TextView tvQuestionContent = summaryItemView.findViewById(R.id.tv_question_content);
                TextView tvAnswerContent = summaryItemView.findViewById(R.id.tv_answer_content);

                tvQuestionTitle.setText(getString(R.string.summary_question_title, i + 1, isCorrect ? "✓" : "✗"));
                tvQuestionContent.setText(question.getQuestion());

                if (isCorrect) {
                    summaryItemView.setBackgroundColor(getResources().getColor(R.color.green_50));
                    tvQuestionTitle.setTextColor(getResources().getColor(R.color.green_800));
                    tvAnswerContent.setTextColor(getResources().getColor(R.color.green_700));
                    tvAnswerContent.setText(getString(R.string.correct_answer, question.getOptions()[question.getCorrectIndex()]));
                } else {
                    summaryItemView.setBackgroundColor(getResources().getColor(R.color.red_50));
                    tvQuestionTitle.setTextColor(getResources().getColor(R.color.red_800));
                    tvAnswerContent.setTextColor(getResources().getColor(R.color.red_700));
                    tvAnswerContent.setText(getString(R.string.your_answer,
                            question.getOptions()[userAnswer],
                            question.getOptions()[question.getCorrectIndex()]));
                }
                questionsSummaryContainer.addView(summaryItemView);
            }
        }

    /**
     * Setzt das Quiz vollständig zurück: Punktestand, Antwortstatus, Benutzerauswahl.
     * Startet mit der ersten Frage neu.
     */
    private void restartQuiz() {
            currentQuestionIndex = 0;
            score = 0;
            questionAnswered = false;

            for (int i = 0; i < answeredQuestions.length; i++) {
                answeredQuestions[i] = false;
                userAnswers[i] = -1;
            }

            tvScore.setText("Punkte: 0/" + quizQuestions.size());

            resultsContainer.setVisibility(View.GONE);
            quizContainer.setVisibility(View.VISIBLE);

            loadQuestion(0);
        }
    }