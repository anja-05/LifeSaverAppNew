package at.fhj.lifesaver;

public class Lektion {
    private String titel;
    private String dateiname;

    public Lektion(String titel, String dateiname) {
        this.titel = titel;
        this.dateiname = dateiname;
    }

    public String getTitel() {
        return titel;
    }

    public String getDateiname() {
        return dateiname;
    }
}
