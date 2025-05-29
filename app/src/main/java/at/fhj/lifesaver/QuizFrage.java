package at.fhj.lifesaver;

import java.util.List;

public class QuizFrage {
        private String question;
        private String[] options;
        private int correctIndex;
        private String explanation;

        public QuizFrage(String question, String[] options, int correctIndex, String explanation) {
            this.question = question;
            this.options = options;
            this.correctIndex = correctIndex;
            this.explanation = explanation;
        }

        public String getQuestion() {
            return question;
        }

        public String[] getOptions() {
            return options;
        }

        public int getCorrectIndex() {
            return correctIndex;
        }

        public String getExplanation() {
            return explanation;
        }
    }