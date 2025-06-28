package at.fhj.lifesaver.training;

/**
 * Die Klasse {@code Uebung} repräsentiert eine Übungseinheit mit zugehörigem Bild, Titel und Beschreibung.
 * Sie wird verwendet, um Inhalte für Erste-Hilfe-Übungen strukturiert darzustellen, z. B. in RecyclerViews oder Kartenansichten.
 */
public class Uebung {
    private int bildResId;
    private String titel;
    private String beschreibung;

    /**
     * Erstellt eine neue Übung mit Bild, Titel und Beschreibung.
     * @param bildResId die Ressourcen-ID für das Bild
     * @param titel der Titel der Übung
     * @param beschreibung die ausführliche Beschreibung der Übung
     */
    public Uebung(int bildResId, String titel, String beschreibung) {
        this.bildResId = bildResId;
        this.titel = titel;
        this.beschreibung = beschreibung;
    }

    /**
     * Gibt die Ressourcen-ID des Bildes zurück.
     * @return Bild-Ressourcen-ID
     */
    public int getBildResId() {
        return bildResId;
    }

    /**
     * Gibt den Titel der Übung zurück.
     * @return Titel der Übung
     */
    public String getTitel() {
        return titel;
    }

    /**
     * Gibt die Beschreibung der Übung zurück.
     * @return Beschreibung der Übung
     */
    public String getBeschreibung() {
        return beschreibung;
    }
}
