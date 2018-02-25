package unipu.oikt.djaranovic.etlak.model;

public class KrvniTlak { // klasa vezana za krvni tlak

    // privatne varijable
    private String id;
    private String sistolicki;
    private String dijastolicki;
    private String puls;
    private String vrijeme;
    private String masa_kg;

    // konstruktor KrvniTlak
    public KrvniTlak(String id, String sistolicki, String dijastolicki, String puls, String vrijeme, String masa_kg) {
        this.id = id;
        this.sistolicki = sistolicki;
        this.dijastolicki = dijastolicki;
        this.puls = puls;
        this.vrijeme = vrijeme;
        this.masa_kg = masa_kg;
    }


    // javne metode dohvaćanja i postavljanja
    public String getId() {
        return id;
    }

    public String getSistolicki() {
        return sistolicki;
    }

    public String getDijastolicki() {
        return dijastolicki;
    }

    public String getPuls() {
        return puls;
    }

    public String getVrijeme() {
        return vrijeme;
    }

    public String getMasa_kg() {
        return masa_kg;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSistolicki(String sistolicki) {
        this.sistolicki = sistolicki;
    }

    public void setDijastolicki(String dijastolicki) {
        this.dijastolicki = dijastolicki;
    }

    public void setPuls(String puls) {
        this.puls = puls;
    }

    public void setVrijeme(String vrijeme) {
        this.vrijeme = vrijeme;
    }

    public void setMasa_kg(String masa_kg) {
        this.masa_kg = masa_kg;
    }

    public String getOpis() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vrijeme: ");
        sb.append("\t" + getVrijeme() + "\nSistolički: ");
        sb.append("\t" + getSistolicki() + " mmHg\nDijastolički: ");
        sb.append("\t" + getDijastolicki() + " mmHg\nPuls: ");
        sb.append("\t" + getPuls() + "/min\nMasa: ");
        sb.append("\t" + getMasa_kg() + " kg\n");
        return sb.toString();
    }

}