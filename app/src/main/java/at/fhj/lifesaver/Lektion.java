package at.fhj.lifesaver;

public class Lektion {
    private String titel;
    private String dateiname;
    private int bildResId;

    public Lektion(String titel, String dateiname, int bildResId) {
        this.titel = titel;
        this.dateiname = dateiname;
        this.bildResId = bildResId;
    }

    public String getTitel() {
        return titel;
    }

    public String getDateiname() {
        return dateiname;
    }

    public int getBildResId() {
        return bildResId;
    }
}
