package unipu.oikt.djaranovic.etlak;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import unipu.oikt.djaranovic.etlak.dijagnoza.DijagnozaTabsActivity;
import unipu.oikt.djaranovic.etlak.prijava.PrijavaActivity;
import unipu.oikt.djaranovic.etlak.model.KrvniTlak;
import unipu.oikt.djaranovic.etlak.povijest.PovijestActivity;
import unipu.oikt.djaranovic.etlak.unos.UnosActivity;
import unipu.oikt.djaranovic.etlak.unos.UnosMasaActivity;
import unipu.oikt.djaranovic.etlak.utils.ActivityHelper;
import unipu.oikt.djaranovic.etlak.utils.DataHelper;

public class MainActivity extends AppCompatActivity { // klasa vezana za početnu aktivnost, glavni zaslon

    // privatne varijable
    private TextView tvZadnjeMjerenje;
    private TextView tvSistolicki;
    private TextView tvDijastolicki;
    private TextView tvPuls;
    private ImageButton buttonNoviUnos;
    private ImageButton buttonDijagnoza;
    private ImageButton buttonUnosMase;

    // datum, vrijeme i vrijednosti u tablici
    private TextView tvDatum1;
    private TextView tvDatum2;
    private TextView tvDatum3;
    private TextView tvVrijeme1;
    private TextView tvVrijeme2;
    private TextView tvVrijeme3;
    private TextView tvVrijednost1;
    private TextView tvVrijednost2;
    private TextView tvVrijednost3;

    // helper
    DataHelper helper;
    ArrayList<KrvniTlak> list;

    // progress dialog
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // instanciranje pomoćne klase
        helper = new DataHelper(this);

        setTitle(helper.getUsername());

        // progress dialog, bar
        progressDialog =  ProgressDialog.show(this, null ,null, false, true);
        progressDialog.getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
        progressDialog.setContentView(R.layout.progress_bar);

        tvZadnjeMjerenje = (TextView) findViewById(R.id.tv_zadnje_mjerenje);
        tvSistolicki = (TextView)findViewById(R.id.tv_sistolicki);
        tvDijastolicki = (TextView)findViewById(R.id.tv_dijastolicki);
        tvPuls = (TextView)findViewById(R.id.tv_puls);

        tvDatum1 = (TextView) findViewById(R.id.tv_table_a1);
        tvDatum2 = (TextView) findViewById(R.id.tv_table_b1);
        tvDatum3 = (TextView) findViewById(R.id.tv_table_c1);

        tvVrijeme1 = (TextView) findViewById(R.id.tv_table_a2);
        tvVrijeme2 = (TextView) findViewById(R.id.tv_table_b2);
        tvVrijeme3 = (TextView) findViewById(R.id.tv_table_c2);

        tvVrijednost1 = (TextView) findViewById(R.id.tv_table_a3);
        tvVrijednost2 = (TextView) findViewById(R.id.tv_table_b3);
        tvVrijednost3 = (TextView) findViewById(R.id.tv_table_c3);

        // ImageButton-i za prijelaz na slijedeću aktivnost
        buttonNoviUnos = (ImageButton) findViewById(R.id.button_tlak);
        buttonDijagnoza = (ImageButton) findViewById(R.id.button_dijagnoza);
        buttonUnosMase = (ImageButton) findViewById(R.id.button_masa);

        // Listener uz efekt za ImageButton za NoviUnos
        buttonNoviUnos.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonNoviUnos.setColorFilter(Color.parseColor("#F7919D"), PorterDuff.Mode.SRC_ATOP);
                        ActivityHelper.startActivity(MainActivity.this, UnosActivity.class);
                        return true;
                    case MotionEvent.ACTION_UP:
                        buttonNoviUnos.clearColorFilter();
                        return true;
                }
                return false;
            }
        });

        // Listener uz efekt za ImageButton Dijagnoza
        buttonDijagnoza.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonDijagnoza.setColorFilter(Color.parseColor("#F7919D"), PorterDuff.Mode.SRC_ATOP);
                        ActivityHelper.startActivity(MainActivity.this, DijagnozaTabsActivity.class);
                        return true;
                    case MotionEvent.ACTION_UP:
                        buttonDijagnoza.clearColorFilter();
                        return true;
                }
                return false;
            }
        });

        // Listener uz efekt za Button za dodatni unos tjelesne mase
        buttonUnosMase.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonUnosMase.setColorFilter(Color.parseColor("#F7919D"), PorterDuff.Mode.SRC_ATOP);
                        ActivityHelper.startActivity(MainActivity.this, UnosMasaActivity.class);
                        return true;
                    case MotionEvent.ACTION_UP:
                        buttonUnosMase.clearColorFilter();
                        return true;
                }
                return false;
            }
        });
    }


    // javna metoda vezano za vraćanje na aktivnosti
    @Override
    protected void onResume() {
        super.onResume();
        // onResume metoda se automatski poziva nakon onCreate metode, vezano za ažuriranje učitanih unosa nakon vraćanja na aktivnost
        // učitavanje podataka i njihovo ispisivanje
        setData();
    }


    // privatna metoda za ispis
    private void ispisZadnjaTriUnosa(ArrayList<KrvniTlak> list) {
        // provjera ima li lista uopće unosa
        if(list.size() != 0) {
            // postavljanje podataka za prvi tlak
            KrvniTlak tlak1 = list.get(0);
            tvDatum1.setText(tlak1.getVrijeme());
            tvVrijeme1.setText(tlak1.getSistolicki() + "/" + tlak1.getDijastolicki());
            tvVrijednost1.setText(tlak1.getPuls());
        }
        // provjera ima li lista barem 2 unosa
        if(list.size() > 1) {
            // postavljanje podataka za drugi tlak
            KrvniTlak tlak2 = list.get(1);
            tvDatum2.setText(tlak2.getVrijeme());
            tvVrijeme2.setText(tlak2.getSistolicki() + "/" + tlak2.getDijastolicki());
            tvVrijednost2.setText(tlak2.getPuls());
        }
        // provjera ima li lista 3 unosa
        if(list.size() > 2) {
            // postavljanje podataka za treći tlak
            KrvniTlak tlak3 = list.get(2);
            tvDatum3.setText(tlak3.getVrijeme());
            tvVrijeme3.setText(tlak3.getSistolicki() + "/" + tlak3.getDijastolicki());
            tvVrijednost3.setText(tlak3.getPuls());
        }
    }


    // privatna metoda za prikaz zadnjih unosa
    private void displayZadnji(KrvniTlak tlak) {
        // čitanje iz prenesenog zadnjeg krvnog tlaka u odvojene varijable
        String sis = tlak.getSistolicki();
        String dia = tlak.getDijastolicki();
        String puls = tlak.getPuls();
        String date = tlak.getVrijeme();

        // umjesto append() treba koristiti setText() inače bi datum svaki put nakon vraćanja bio zalijepljen na kraj
        tvZadnjeMjerenje.setText("Zadnje mjerenje: " +date);

        // ispis zadnjeg krvnog tlaka
        tvSistolicki.setText(sis);
        tvDijastolicki.setText(dia);
        tvPuls.setText(puls);

        // postavljanje tjelesne mase na onu učitanu sa zadnjeg unosa
        float lastEnteredWeight = Float.parseFloat(tlak.getMasa_kg());
        float localWeight = helper.getLastWeight();

        // spremanje ID-a zadnjeg unosa u lokalnu pohranu
        helper.updateLastEntryID(tlak.getId());

        // nakon odjave, brisanje lokalno spremljene tjelesne mase, postavljanje na onu zadnjeg unosa ukoliko je 0
        if(localWeight == 0) helper.updateWeight(lastEnteredWeight);

        // skrivanje progress dialoga
        if(progressDialog.isShowing()) progressDialog.dismiss();
    }


    // privatna metoda za postavljanje podataka, Volley
    private void setData() {

        // čišćenje mogućih prethodnih unosa
        clearFields();

        // pripremanje Volley queue-a
        RequestQueue queue = Volley.newRequestQueue(this);
        // API ruta za zadnja 3 unosa
        String url = DataHelper.BASE_URL + "/entries/limit/3";

        list = new ArrayList<>();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // javljanje pomoćnoj klasi da ima unosa
                helper.setHasEntries(true);
                for (int i=0; i<response.length(); i++) {
                    try {
                        JSONObject object = response.getJSONObject(i);
                        String id, sys, dia, pulse, weight, date;
                        id = object.getString("_id");
                        sys = object.getString("sys");
                        dia = object.getString("dia");
                        pulse = object.getString("pulse");
                        weight = object.getString("weight");
                        date = DataHelper.convertDateMain(object.getString("date"));

                        KrvniTlak tlak = new KrvniTlak(id, sys, dia, pulse, date, weight);
                        list.add(tlak);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // preusmjeravanje korisnika na unos krvnog tlaka kada podaci nisu pronađeni
                Intent intent = new Intent(MainActivity.this, UnosActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                // javljanje da se radi o prvom unosu i da treba nakon krvnog tlaka unijeti i tjelesnu masu
                intent.putExtra("firstEntry", true);
                // javljanje pomoćnoj klasi da nema unosa
                helper.setHasEntries(false);
                startActivity(intent);
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
        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
            @Override
            public void onRequestFinished(Request<Object> request) {

                // skrivanje progress dialoga
                if(progressDialog.isShowing()) progressDialog.dismiss();

                if(list.isEmpty()) {
                    // nema podataka, unosa
                    Toast toast = Toast.makeText(MainActivity.this, "Nemate unosa!", Toast.LENGTH_LONG);
                    LinearLayout toastLayout = (LinearLayout) toast.getView();
                    TextView toastTV = (TextView) toastLayout.getChildAt(0);
                    toastTV.setTextSize(28);
                    toast.show();

                } else {
                    KrvniTlak last = list.get(0);
                    displayZadnji(last);
                    ispisZadnjaTriUnosa(list);
                }
            }
        });
    }


    // privatna metoda za brisanje podataka iz svih tekst polja
    private void clearFields() {
        tvZadnjeMjerenje.setText("");
        tvSistolicki.setText("");
        tvDijastolicki.setText("");
        tvPuls.setText("");
        tvDatum1.setText("");
        tvDatum2.setText("");
        tvDatum3.setText("");
        tvVrijeme1.setText("");
        tvVrijeme2.setText("");
        tvVrijeme3.setText("");
        tvVrijednost1.setText("");
        tvVrijednost2.setText("");
        tvVrijednost3.setText("");
    }


    // navigacija, izbornik kroz aplikaciju
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.unos_menu:
                ActivityHelper.startActivity(MainActivity.this, UnosActivity.class);
                return true;
            case R.id.masa_menu:
                ActivityHelper.startActivity(MainActivity.this, UnosMasaActivity.class);
                return true;
            case R.id.dijagnoza_menu:
                ActivityHelper.startActivity(MainActivity.this, DijagnozaTabsActivity.class);
                return true;
            case R.id.povijest_menu:
                ActivityHelper.startActivity(MainActivity.this, PovijestActivity.class);
                return true;
            case R.id.logout_menu:
                helper.logout();
                ActivityHelper.startActivity(MainActivity.this, PrijavaActivity.class);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}