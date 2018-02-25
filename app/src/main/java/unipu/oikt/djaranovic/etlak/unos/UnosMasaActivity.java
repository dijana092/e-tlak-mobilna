package unipu.oikt.djaranovic.etlak.unos;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import unipu.oikt.djaranovic.etlak.MainActivity;
import unipu.oikt.djaranovic.etlak.R;
import unipu.oikt.djaranovic.etlak.dijagnoza.DijagnozaTabsActivity;
import unipu.oikt.djaranovic.etlak.prijava.PrijavaActivity;
import unipu.oikt.djaranovic.etlak.povijest.PovijestActivity;
import unipu.oikt.djaranovic.etlak.utils.ActivityHelper;
import unipu.oikt.djaranovic.etlak.utils.DataHelper;

public class UnosMasaActivity extends AppCompatActivity { // klasa vezana za zaslon za unos tjelesne mase

    // privatne varijable
    private EditText etMasaKg;
    private Button buttonBrisanje;
    private Button buttonTocka;
    private Button buttonUnosMase;
    private DataHelper helper;
    private MediaPlayer sound;
    private boolean val = false;
    private boolean firstEntry = false;
    private int sys,dia,pulse;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unos_masa);

        this.setTitle("Unos tjelesne mase");

        // instanciranje pomoćne klase
        helper = new DataHelper(this);

        // provjera da li se radi o novom unosu
        firstEntry = getIntent().getBooleanExtra("firstEntry", false);
        if(firstEntry) {
            sys = getIntent().getIntExtra("sys", 0);
            dia = getIntent().getIntExtra("dia", 0);
            pulse = getIntent().getIntExtra("pulse", 0);
        }

        // zvuk vezan za uspješan unos
        sound = MediaPlayer.create(getApplicationContext(), R.raw.success);

        // provjera da li ima unosa, ne unositi tjelesnu masu prije krvnog tlaka
        validateDatabaseEntries();

        // EditText za unos vrijednosti tjelesne mase
        etMasaKg = (EditText) findViewById(R.id.et_masa_kg);

        // onemogućavanje unosa putem klasične android tipkovnice
        etMasaKg.setInputType(InputType.TYPE_NULL);

        // Button za brisanje vrijednosti tjelesne mase
        buttonBrisanje = (Button) findViewById(R.id.button_brisanje);

        // Button za decimalni zarez
        buttonTocka = (Button) findViewById(R.id.button_tocka);

        // Button za potvrdu unosa tjelesne mase
        buttonUnosMase = (Button) findViewById(R.id.button_unos);

        // Listener uz efekt za Button za brisanje
        buttonBrisanje.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonBrisanje.getBackground().setColorFilter(Color.parseColor("#F7919D"), PorterDuff.Mode.SRC_ATOP);
                        if (etMasaKg.isFocused()) {
                            if (etMasaKg.length() > 0)
                                etMasaKg.setText(etMasaKg.getText().toString().substring(0, etMasaKg.length() - 1));
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        buttonBrisanje.getBackground().clearColorFilter();
                        return true;
                }
                return false;
            }
        });

        // Listener uz efekt za Button za decimalni zarez
        buttonTocka.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonTocka.getBackground().setColorFilter(Color.parseColor("#F7919D"), PorterDuff.Mode.SRC_ATOP);
                        CharSequence zarez = ((Button) v).getText();
                        etMasaKg.append(zarez);
                        return true;
                    case MotionEvent.ACTION_UP:
                        buttonTocka.getBackground().clearColorFilter();
                        return true;
                }
                return false;
            }
        });

        // Listener uz efekt za Button za unos tjelesne mase
        buttonUnosMase.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonUnosMase.getBackground().setColorFilter(Color.parseColor("#F7919D"), PorterDuff.Mode.SRC_ATOP);
                        // ukoliko EditText nije prazan
                        if (!etMasaKg.getText().toString().equals("")) {

                            // Integer koji se unosi u bazu podataka
                            float masa_kg = Float.parseFloat(etMasaKg.getText().toString());

                            // instanciranje novog objekta tipa ValidacijaUnosa za provjeru unosa tjelesne mase
                            ValidacijaUnosa validacija = new ValidacijaUnosa(getApplicationContext());
                            val = validacija.validirajMasu(masa_kg);

                            // ukoliko je validacija uspješna
                            if (val) {
                                // dodavanje novog unosa u bazu
                                dodajNoviUnos(masa_kg);
                                View v2 = getWindow().getDecorView().findFocus();

                            } else {
                                // validacija nije uspješna zbog neispravno unesene vrijednosti tjelesne mase
                                Toast toast = Toast.makeText(getApplicationContext(), "Greška!", Toast.LENGTH_SHORT);
                                LinearLayout toastLayout = (LinearLayout) toast.getView();
                                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                                toastTV.setTextSize(28);
                                toast.show();
                            }
                        }
                        else {
                            // u slučaju da EditText nije ispunjen
                            Toast toast = Toast.makeText(getApplicationContext(), "Popunite polje!", Toast.LENGTH_SHORT);
                            LinearLayout toastLayout = (LinearLayout) toast.getView();
                            TextView toastTV = (TextView) toastLayout.getChildAt(0);
                            toastTV.setTextSize(28);
                            toast.show();
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        buttonUnosMase.getBackground().clearColorFilter();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }


    // privatna metoda ukoliko korisnik želi unijeti tjelesnu masu prije krvnog tlaka
    private void validateDatabaseEntries() {
        // nije prvi unos, ali nema spremljenih unosa u bazi (mogućnost otvorenog zaslona preko navigacije)
        if (!firstEntry && !helper.hasEntries() ) {
            Toast toast = Toast.makeText(getApplicationContext(), "Prvo unesite krvni tlak!", Toast.LENGTH_SHORT);
            LinearLayout toastLayout = (LinearLayout) toast.getView();
            TextView toastTV = (TextView) toastLayout.getChildAt(0);
            toastTV.setTextSize(28);
            toast.show();
            finish();
        }
    }


    // javna metoda
    public void onClickNumberButton(View v) {
        CharSequence broj = ((Button) v).getText();
        // unos u EditText vezan za tjelesnu masu
        if (etMasaKg.isFocused()) {
            if (etMasaKg.getText().length() > 5) {
                Toast toast = Toast.makeText(getApplicationContext(), "Greška!", Toast.LENGTH_SHORT);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(28);
                toast.show();
                return;
            }
            etMasaKg.append(broj);
        }
    }


    // privatna metoda za dodavanje novog unosa, Volley
    private void dodajNoviUnos(final float masa_kg) {

        // kod prvog unosa spremanje svih podataka u bazu
        if(firstEntry) {
            String url = DataHelper.BASE_URL + "/entries";

            // priprema parametara; podaci o tlaku su integer, no zbog tjelesne mase ih treba poslati u float obliku, tj. poslati sve u float obliku
            Map<String, Float> params = new HashMap<>();
            params.put("sys", (float) sys);
            params.put("dia", (float) dia);
            params.put("pulse", (float) pulse);
            params.put("weight", masa_kg);
            // slanje parametara u JSON obliku, stvaranje iz mape
            JSONObject paramJSON = new JSONObject(params);

            RequestQueue queue = Volley.newRequestQueue(UnosMasaActivity.this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, paramJSON, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    sound.start();
                    Toast toast = Toast.makeText(getApplicationContext(), "Uspješan unos tlaka i mase!", Toast.LENGTH_LONG);
                    LinearLayout toastLayout = (LinearLayout) toast.getView();
                    TextView toastTV = (TextView) toastLayout.getChildAt(0);
                    toastTV.setTextSize(28);
                    toast.show();

                    // javljanje pomoćnoj klasi da ima unosa
                    helper.setHasEntries(true);

                    // otvaranje glavnog, Main zaslona nakon uspješnog unosa
                    ActivityHelper.startActivity(UnosMasaActivity.this, MainActivity.class);
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // validacija nije uspješna zbog neispravno unesene vrijednosti
                    Toast toast = Toast.makeText(getApplicationContext(), "Greška!", Toast.LENGTH_SHORT);
                    LinearLayout toastLayout = (LinearLayout) toast.getView();
                    TextView toastTV = (TextView) toastLayout.getChildAt(0);
                    toastTV.setTextSize(28);
                    toast.show();
                }

            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", helper.getToken());
                    return headers;
                }
            };

            queue.add(request);

        } else {
            // nije prvi unos
            String url = DataHelper.BASE_URL + "/entries/" + helper.getLastentryid();

            // priprema parametara, mijenjanje samo tjelesne mase
            Map<String, Float> params = new HashMap<>();
            params.put("weight", masa_kg);
            // slanje parametara u JSON obliku, stvaranje iz mape
            JSONObject paramJSON = new JSONObject(params);

            RequestQueue queue = Volley.newRequestQueue(UnosMasaActivity.this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, paramJSON, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // lokalna promjena tjelesne mase
                    helper.updateWeight(masa_kg);

                    sound.start();
                    Toast toast = Toast.makeText(getApplicationContext(), "Ažurirali ste tjelesnu masu!", Toast.LENGTH_LONG);
                    LinearLayout toastLayout = (LinearLayout) toast.getView();
                    TextView toastTV = (TextView) toastLayout.getChildAt(0);
                    toastTV.setTextSize(28);
                    toast.show();

                    // otvaranje zaslona za unos krvnog tlaka nakon uspješnog unosa tjelesne mase
                    ActivityHelper.startActivity(UnosMasaActivity.this, MainActivity.class);
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // validacija nije uspješna zbog neispravno unesene vrijednosti
                    Toast toast = Toast.makeText(getApplicationContext(), "Greška!", Toast.LENGTH_SHORT);
                    LinearLayout toastLayout = (LinearLayout) toast.getView();
                    TextView toastTV = (TextView) toastLayout.getChildAt(0);
                    toastTV.setTextSize(28);
                    toast.show();
                }

            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", helper.getToken());
                    return headers;
                }
            };

            queue.add(request);
        }
    }


    // navigacija, izbornik kroz aplikaciju
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_masa, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pocetni_menu:
                ActivityHelper.startActivity(UnosMasaActivity.this, MainActivity.class);
                return true;
            case R.id.unos_menu:
                ActivityHelper.startActivity(UnosMasaActivity.this, UnosActivity.class);
                return true;
            case R.id.dijagnoza_menu:
                ActivityHelper.startActivity(UnosMasaActivity.this, DijagnozaTabsActivity.class);
                return true;
            case R.id.povijest_menu:
                ActivityHelper.startActivity(UnosMasaActivity.this, PovijestActivity.class);
                return true;
            case R.id.logout_menu:
                helper.logout();
                ActivityHelper.startActivity(UnosMasaActivity.this, PrijavaActivity.class);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}