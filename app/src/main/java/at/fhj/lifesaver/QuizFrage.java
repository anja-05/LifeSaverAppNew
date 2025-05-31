package at.fhj.lifesaver;

import java.util.List;

/**
 * Die Klasse QuizFrage repräsentiert eine einzelne Frage im Multiple-Choice-Quiz.
 * Sie enthält den Fragetext, eine Liste von Antwortmöglichkeiten, den Index der richtigen Antwort
 * sowie eine Erklärung zur richtigen Lösung.
 *
 */
public class QuizFrage {
    private String question;
    private String[] options;
    private int correctIndex;
    private String explanation;

    /**
     * Erstellt eine neue Quizfrage mit Text, Antwortoptionen, richtiger Antwort und Erklärung.
     *
     * @param question     Der Fragetext
     * @param options      Array mit genau 4 Antwortoptionen
     * @param correctIndex Index der richtigen Antwort
     * @param explanation  Erklärung zur richtigen Lösung
     * @throws IllegalArgumentException bei ungültigen Parametern
     */
    public QuizFrage(String question, String[] options, int correctIndex, String explanation) {
        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("Frage darf nicht leer sein.");
        }
        if (options == null || options.length != 4) {
            throw new IllegalArgumentException("Es müssen genau 4 Antwortoptionen angegeben werden.");
        }
        if (correctIndex < 0 || correctIndex >= options.length) {
            throw new IllegalArgumentException("Der Index der richtigen Antwort ist ungültig.");
        }
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
        this.explanation = explanation;
    }

    /**
     * Gibt den Fragetext zurück.
     *
     * @return
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Gibt die vier Antwortoptionen zurück.
     * @return
     */
    public String[] getOptions() {
        return options;
    }

    /**
     * Gibt den Index der richtigen Antwort zurück.
     * @return
     */
    public int getCorrectIndex() {
        return correctIndex;
    }

    /**
     * Gibt die Erklärung zur richtigen Antwort zurück.
     * @return
     */
    public String getExplanation() {
        return explanation;
    }
}