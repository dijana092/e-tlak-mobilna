package unipu.oikt.djaranovic.etlak.utils;


import android.content.Context;
import android.content.SharedPreferences;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DataHelper { // pomoćna klasa vezana za bazu padataka

    // podaci baze podataka
    public static final String BASE_URL = "https://e-tlak.herokuapp.com/api"; // heroku api url
    private static final String PREFS = "prefs";

    public static final String USERID = "userId";
    public static final String USERNAME = "username";
    public static final String TOKEN = "token";
    public static final String HASENTRIES = "hasEntries";
    public static final String WEIGHT = "weight";
    public static final String LASTENTRYID = "lastEntryID";

    private Context context;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public DataHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        this.context = context;
    }


    // javne metode vezane za prijavu
    public boolean isLoggedIn() {
        return (prefs.contains("token"));
    }


    public void login(String userId, String username, String token) {
        editor = prefs.edit();

        // spremanje podataka lokalno
        editor.putString("userId", userId);
        editor.putString("username", username);
        editor.putString("token", token);

        editor.apply();
    }


    // javna metoda vezana za odjavu
    public void logout() {
        editor = prefs.edit();

        editor.remove(USERNAME);
        editor.remove(USERID);
        editor.remove(TOKEN);
        editor.remove(WEIGHT);
        editor.remove(HASENTRIES);
        editor.remove(LASTENTRYID);
        editor.apply();
    }


    // javne metode vezane za format datuma
    public static String convertDate(String rawDate) {
        // npr: "2017-11-18T18:07:32.283Z"
        Locale.setDefault(new Locale("hr", "HR"));

        if (rawDate.endsWith("Z")) rawDate = rawDate.replace("Z", "+0000");
        else rawDate = rawDate.replaceAll("([+-]\\d\\d):(\\d\\d)\\s*$", "$1$2");
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", new Locale("hr", "HR"));
        String result = null;
        try {
            String pattern = "dd.MM.yyyy. HH:mm";
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            result = formatter.format(df1.parse(rawDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static String convertDateMain(String rawDate) {
        // npr: "2017-11-18T18:07:32.283Z"
        Locale.setDefault(new Locale("hr", "HR"));

        if (rawDate.endsWith("Z")) rawDate = rawDate.replace("Z", "+0000");
        else rawDate = rawDate.replaceAll("([+-]\\d\\d):(\\d\\d)\\s*$", "$1$2");
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", new Locale("hr", "HR"));
        String result = null;
        try {
            String pattern = "dd.MM.  HH:mm";
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            result = formatter.format(df1.parse(rawDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }


    // javne metode vezano za korisnika
    public String getUsername() {
        return prefs.getString(USERNAME, "");
    }


    public String getToken() {
        return prefs.getString(TOKEN, "");
    }


    // javne metode vezane za tjelesnu masu
    public void updateWeight(float weight) {
        editor = prefs.edit();

        // spremanje zadnje tjelesne mase lokalno
        editor.putFloat(WEIGHT, weight);

        editor.apply();
    }


    public float getLastWeight() {
        // učitavanje zadnje unesene tjelesne mase
        return prefs.getFloat(WEIGHT, 0);
    }


    // javna metoda vezana za broj unosa
    public void setHasEntries(boolean hasEntries) {
        editor = prefs.edit();

        // spremanje broja unosa
        editor.putBoolean(HASENTRIES, hasEntries);

        editor.apply();
    }


    // javna metoda koja provjerava postoje li unosi, za validaciju prije unosa tjelesne mase
    public boolean hasEntries() {
        return prefs.getBoolean(HASENTRIES, false);
    }


    // javna metoda vezana za spremanje ID-a zadnjeg unosa (za potrebe mijenjanja kilaže zadnjem unosu)
    public void updateLastEntryID(String id) {
        editor = prefs.edit();
        editor.putString(LASTENTRYID, id);
        editor.apply();
    }


    // javna metoda za čitanje zadnjeg ID-a
    public String getLastentryid() {
        return prefs.getString(LASTENTRYID, "");
    }

}