package at.fhj.lifesaver;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import android.content.Intent;
import android.widget.ProgressBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.ArrayList;
import com.google.android.material.card.MaterialCardView;


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

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_quiz);

            Intent intent = getIntent();
            topicTitle = intent.getStringExtra("TOPIC_TITLE");
            if (topicTitle == null) {
                topicTitle = "Quiz";
            }

            initializeViews();

            initializeQuizData();

            setupListeners();

            loadQuestion(0);
        }

        private void initializeViews() {
            // Find all views by ID
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

            tvTopicTitle.setText("Quiz: " + topicTitle);
        }

        private void initializeQuizData() {
            // Create quiz questions
            quizQuestions = new ArrayList<>();

            // Add sample questions - in a real app, you might load these from a database or API
            quizQuestions.add(new QuizFrage(
                    "Was ist eine Variable in der Programmierung?",
                    new String[]{
                            "Ein festgelegter Wert, der nicht geändert werden kann",
                            "Ein Speicherplatz für Daten, dessen Wert sich ändern kann",
                            "Eine mathematische Gleichung",
                            "Ein Programmfehler"
                    },
                    1,
                    "Eine Variable ist ein Speicherplatz für Daten in einem Programm. Der Wert einer Variable kann während der Programmausführung geändert werden, was sie zu einem grundlegenden Konzept in der Programmierung macht."
            ));

            quizQuestions.add(new QuizFrage(
                    "Was ist eine Schleife in der Programmierung?",
                    new String[]{
                            "Ein Fehler im Code",
                            "Eine Methode zur Datenspeicherung",
                            "Ein Codeblock, der wiederholt ausgeführt wird",
                            "Eine Art von Variable"
                    },
                    2,
                    "Eine Schleife ist ein Kontrollstruktur, die es ermöglicht, einen Codeblock mehrmals auszuführen, solange eine bestimmte Bedingung erfüllt ist oder für eine festgelegte Anzahl von Durchläufen."
            ));

            quizQuestions.add(new QuizFrage(
                    "Was ist eine Bedingung (if-Statement) in der Programmierung?",
                    new String[]{
                            "Eine Anweisung, die Code nur ausführt, wenn eine bestimmte Bedingung erfüllt ist",
                            "Eine Methode zum Speichern von Daten",
                            "Ein Fehler im Programm",
                            "Eine Art von Schleife"
                    },
                    0,
                    "Eine Bedingung oder if-Statement ist eine Kontrollstruktur, die es ermöglicht, Code nur dann auszuführen, wenn eine bestimmte Bedingung als wahr ausgewertet wird. Dies ist grundlegend für die Entscheidungsfindung in Programmen."
            ));

            quizQuestions.add(new QuizFrage(
                    "Was ist eine Funktion in der Programmierung?",
                    new String[]{
                            "Ein mathematischer Ausdruck",
                            "Ein Datentyp",
                            "Ein wiederverwendbarer Codeblock, der eine bestimmte Aufgabe erfüllt",
                            "Eine Art von Variable"
                    },
                    2,
                    "Eine Funktion ist ein benannter, wiederverwendbarer Codeblock, der eine bestimmte Aufgabe erfüllt. Funktionen helfen, Code zu organisieren, zu strukturieren und Wiederholungen zu vermeiden."
            ));

            quizQuestions.add(new QuizFrage(
                    "Was ist ein Array in der Programmierung?",
                    new String[]{
                            "Ein Fehler im Code",
                            "Eine Sammlung von Elementen desselben Datentyps",
                            "Eine Art von Schleife",
                            "Eine Bedingung"
                    },
                    1,
                    "Ein Array ist eine Datenstruktur, die eine Sammlung von Elementen desselben Datentyps speichert. Arrays ermöglichen es, auf mehrere Werte über einen einzigen Variablennamen und einen Index zuzugreifen."
            ));

            answeredQuestions = new boolean[quizQuestions.size()];
            userAnswers = new int[quizQuestions.size()];
            for (int i = 0; i < userAnswers.length; i++) {
                userAnswers[i] = -1; // -1 means not answered
            }

            tvQuestionCounter.setText("Frage 1 von " + quizQuestions.size());
            tvScore.setText("Punkte: 0/" + quizQuestions.size());
        }

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
                    if (currentQuestionIndex > 0) {
                        loadQuestion(currentQuestionIndex - 1);
                    }
                }
            });

            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                card.setCardBackgroundColor(getResources().getColor(R.color.white));
                card.setStrokeColor(getResources().getColor(R.color.gray_300));
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

        private void updateButtonStates() {
            btnPrev.setEnabled(currentQuestionIndex > 0);

            if (currentQuestionIndex == quizQuestions.size() - 1) {
                btnNext.setText("Ergebnis anzeigen");
            } else {
                btnNext.setText("Weiter");
            }
        }

        private void showResults() {
            quizContainer.setVisibility(View.GONE);
            resultsContainer.setVisibility(View.VISIBLE);

            tvFinalScore.setText(score + "/" + quizQuestions.size());

            float percentage = (float) score / quizQuestions.size() * 100;
            if (percentage == 100) {
                tvFeedback.setText("Perfekt! Du hast alle Fragen richtig beantwortet!");
            } else if (percentage >= 80) {
                tvFeedback.setText("Sehr gut! Du hast die meisten Fragen richtig beantwortet!");
            } else if (percentage >= 60) {
                tvFeedback.setText("Gut gemacht! Du hast mehr als die Hälfte der Fragen richtig beantwortet.");
            } else if (percentage >= 40) {
                tvFeedback.setText("Du hast einige Fragen richtig beantwortet. Mit etwas Übung wirst du besser!");
            } else {
                tvFeedback.setText("Du hast noch Verbesserungspotential. Versuche es noch einmal!");
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

                tvQuestionTitle.setText("Frage " + (i + 1) + ": " + (isCorrect ? "✓" : "✗"));
                tvQuestionContent.setText(question.getQuestion());

                if (isCorrect) {
                    summaryItemView.setBackgroundColor(getResources().getColor(R.color.green_50));
                    tvQuestionTitle.setTextColor(getResources().getColor(R.color.green_800));
                    tvAnswerContent.setTextColor(getResources().getColor(R.color.green_700));
                    tvAnswerContent.setText("Richtige Antwort: " + question.getOptions()[question.getCorrectIndex()]);
                } else {
                    summaryItemView.setBackgroundColor(getResources().getColor(R.color.red_50));
                    tvQuestionTitle.setTextColor(getResources().getColor(R.color.red_800));
                    tvAnswerContent.setTextColor(getResources().getColor(R.color.red_700));
                    tvAnswerContent.setText("Deine Antwort: " + question.getOptions()[userAnswer] +
                            " | Richtige Antwort: " + question.getOptions()[question.getCorrectIndex()]);
                }
                questionsSummaryContainer.addView(summaryItemView);
            }
        }

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