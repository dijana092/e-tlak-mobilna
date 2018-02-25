package unipu.oikt.djaranovic.etlak;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

import unipu.oikt.djaranovic.etlak.prijava.PrijavaActivity;
import unipu.oikt.djaranovic.etlak.unos.UnosActivity;
import unipu.oikt.djaranovic.etlak.utils.ActivityHelper;
import unipu.oikt.djaranovic.etlak.utils.DataHelper;

public class SplashScreenActivity extends AppCompatActivity { // početni splashscreen

    // privatne varijable
    private final int splashDisplayLength = 3000;
    private DataHelper helper;
    private boolean isLoggedIn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); // skrivanje status bar-a
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide(); // skrivanje title bar-a

        setContentView(R.layout.activity_splash_screen);

        // pomoćna klasa
        helper = new DataHelper(this);

        // provjera da li je korisnik prijavljen
        if(helper.isLoggedIn()) {
            isLoggedIn = true;
        }

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                // usmjeravanje korisnika na odgovarajuću aktivnost ovisno o stanju prijave
                if(isLoggedIn) {
                    // ukoliko je korisnik prijavljen, provjera ima li korisnik unosa
                    tryEntries();
                } else {
                    ActivityHelper.startActivity(SplashScreenActivity.this, PrijavaActivity.class);
                    finish();
                }
            }
        }, splashDisplayLength);
    }


    // privatna metoda za provjeru unosa, Volley
    private void tryEntries() {

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // API ruta za dohvaćanje svih unosa
        String url = DataHelper.BASE_URL + "/entries";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // zahtjev prošao bez greške, tj. ima unosa, javljanje pomoćnoj klasi
                helper.setHasEntries(true);
                // prebačaj na glavnu stranicu, MainActivity
                ActivityHelper.startActivity(SplashScreenActivity.this, MainActivity.class);
                // onemogućavanje vraćanja na splashscreen
                finish();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // preusmjeravanje korisnika na unos krvnog tlaka kada podaci nisu pronađeni
                Intent intent = new Intent(SplashScreenActivity.this, UnosActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                // javljanje da se radi o prvom unosu i da treba nakon krvnog tlaka unijeti i tjelesnu masu
                intent.putExtra("firstEntry", true);
                // javljanje pomoćnoj klasi da nema unosa
                helper.setHasEntries(false);
                startActivity(intent);
                // onemogućavanje vraćanja na splashscreen
                finish();
            }

        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", helper.getToken());
                return headers;
            }
        };

        requestQueue.add(request);
    }
}