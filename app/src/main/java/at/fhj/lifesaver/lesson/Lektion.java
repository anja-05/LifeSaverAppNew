package at.fhj.lifesaver.lesson;

/**
 * Die Klasse ist die Lerneinheit der App Lifesaver.
 * Sie besteht aus einem Titel, einem dazugehörigem HTML-Dateinamen und einer Bildresource.
 *
 */
public class Lektion {
    private String titel;
    private String dateiname;
    private int bildResId;

    /**
     * Die Klasse "Lektion" erstellt eine neue Lektion mit Titel, Dateiname und Bildressourcen-ID.
     * @param titel der Titel der Lektion, darf nicht null oder leer sein
     * @param dateiname der Dateiname der HTML-Datei, darf nicht null oder leer sein
     * @param bildResId ID der Bildressource muss positiv sein
     */
    public Lektion(String titel, String dateiname, int bildResId) {
        if (titel == null || titel.trim().isEmpty()) {
            throw new IllegalArgumentException("Titel darf nicht null oder leer sein.");
        }
        if (dateiname == null || dateiname.trim().isEmpty()) {
            throw new IllegalArgumentException("Dateiname darf nicht null oder leer sein.");
        }
        if (bildResId <= 0) {
            throw new IllegalArgumentException("Bildressourcen-ID muss positiv sein.");
        }

        this.titel = titel;
        this.dateiname = dateiname;
        this.bildResId = bildResId;
    }

    /**
     * Gibt den Titel der Lektion zurück.
     * @return Titel der Lektion
     */
    public String getTitel() {
        return titel;
    }

    /**
     *Gibt den HTML-Dateinamen der Lektion zurück.
     * @return HTML-Dateinamen
     */
    public String getDateiname() {
        return dateiname;
    }

    /**
     * Gibt die Bildressourcen-ID zurück.
     * @return Bildressourcen-ID
     */
    public int getBildResId() {
        return bildResId;
    }
}
