package unipu.oikt.djaranovic.etlak.unos;

import android.content.Context;

public class ValidacijaUnosa { // klasa vezana za validaciju, provjeru unosa krvnog tlaka i tjelesne mase

    // privatne varijable
    private Context context;
    private final int sistolickiMin = 1;
    private final int sistolickiMax = 200;
    private final int dijastolickiMin = 1;
    private final int dijastolickiMax = 200;
    private final int pulsMin = 1;
    private final int pulsMax = 200;
    private final float masaMin = 0;
    private final float masaMax = 1000;

    public ValidacijaUnosa(Context context) {
        this.context = context;
    }


    // javna metoda za provjeru unosa krvnog tlaka
    public boolean validirajTlak(int sistolicki, int dijastolicki, int puls) {
        // evidentiranje pogreške prilikom unosa krvnog tlaka
        if(sistolicki < sistolickiMin || sistolicki > sistolickiMax || dijastolicki < dijastolickiMin || dijastolicki > dijastolickiMax ||  puls < pulsMin || puls > pulsMax) {
            return false;
        }
        // ispravan unos
        return true;
    }


    // javna metoda za provjeru unosa tjelesne mase
    public boolean validirajMasu(float masa_kg) {
        // evidentiranje pogreške prilikom unosa tjelesne mase
        if(masa_kg < masaMin || masa_kg > masaMax){
            return false;
        }
        // ispravan unos
        return true;
    }

}