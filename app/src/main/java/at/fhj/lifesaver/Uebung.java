package at.fhj.lifesaver;

public class Uebung {
    private int bildResId;
    private String titel;
    private String beschreibung;

    public Uebung(int bildResId, String titel, String beschreibung) {
        this.bildResId = bildResId;
        this.titel = titel;
        this.beschreibung = beschreibung;
    }

    public int getBildResId() {
        return bildResId;
    }

    public String getTitel() {
        return titel;
    }

    public String getBeschreibung() {
        return beschreibung;
    }
}
