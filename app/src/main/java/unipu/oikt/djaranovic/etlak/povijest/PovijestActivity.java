package unipu.oikt.djaranovic.etlak.povijest;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import unipu.oikt.djaranovic.etlak.MainActivity;
import unipu.oikt.djaranovic.etlak.R;
import unipu.oikt.djaranovic.etlak.dijagnoza.DijagnozaTabsActivity;
import unipu.oikt.djaranovic.etlak.prijava.PrijavaActivity;
import unipu.oikt.djaranovic.etlak.model.KrvniTlak;
import unipu.oikt.djaranovic.etlak.unos.UnosActivity;
import unipu.oikt.djaranovic.etlak.unos.UnosMasaActivity;
import unipu.oikt.djaranovic.etlak.utils.ActivityHelper;
import unipu.oikt.djaranovic.etlak.utils.DataHelper;

public class PovijestActivity extends AppCompatActivity { // klasa vezana za zaslon sa svim unosima, povijest unosa

    // privatne varijable
    private DataHelper helper;
    private ArrayList<KrvniTlak> arrayList;
    private ArrayList<String> arrayListTv;
    ListView listView;
    ArrayAdapter<String> adapter;
    RequestQueue queue;
    AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_povijest);

        this.setTitle("Povijest");

        helper = new DataHelper(this);

        // kreiranje listView-a
        listView = (ListView) findViewById(R.id.listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LayoutInflater layoutInflater = LayoutInflater.from(PovijestActivity.this);
                View promptView = layoutInflater.inflate(R.layout.activity_povijest_dialog, null);

                // kreiranje AlertDialoga za prikaz informacija o izvršenim unosima
                dialog = new AlertDialog.Builder(PovijestActivity.this).create();

                // postavljanje teksta dijaloškog okvira
                TextView textview = (TextView) promptView.findViewById(R.id.tv_detalji);

                // pomoćna varijabla za odabrani krvni tlak
                final KrvniTlak tlak = arrayList.get(i);

                // postavljanje opisa
                textview.setText(tlak.getOpis());

                // postavljanje buttona za povratak i brisanje
                final Button buttonNatrag = (Button) promptView.findViewById(R.id.button_natrag);
                final Button buttonObrisi = (Button) promptView.findViewById(R.id.button_obrisi);

                buttonNatrag.setOnTouchListener(new View.OnTouchListener()  {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                buttonNatrag.getBackground().setColorFilter(Color.parseColor("#F7919D"), PorterDuff.Mode.SRC_ATOP);
                                // za izlaz iz dijaloškog okvira, povratak na popis unosa
                                dialog.cancel();
                                return true;
                            case MotionEvent.ACTION_UP:
                                buttonNatrag.getBackground().clearColorFilter();
                                return true;
                        }
                        return false;
                    }
                });

                buttonObrisi.setOnTouchListener(new View.OnTouchListener() { //efekt pritisnutog gumba
                    @Override
                    // metoda za brisanje odabranog unosa krvnog tlaka
                    public boolean onTouch(View view, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                buttonObrisi.getBackground().setColorFilter(Color.parseColor("#F7919D"), PorterDuff.Mode.SRC_ATOP);
                                // brisanje odabranog unosa krvnog tlaka
                                deleteEntry(tlak.getId());
                                return true;
                            case MotionEvent.ACTION_UP:
                                buttonObrisi.getBackground().clearColorFilter();
                                return true;
                        }
                        return false;
                    }
                });

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCancelable(false);
                dialog.setView(promptView);
                dialog.show();
            }
        });

        // čitanje i punjenje podataka
        setData();
    }


    // privatna metoda za postavljanje podataka, Volley
    private void setData() {

        // stvaranje podataka, ukoliko podaci postoje onda pražnjenje svih podataka
        arrayList = new ArrayList<>();
        arrayListTv = new ArrayList<>();

        queue = Volley.newRequestQueue(this);
        String url = DataHelper.BASE_URL + "/entries/reverse";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
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
                        arrayList.add(tlak);
                        arrayListTv.add(tlak.getVrijeme()+"   " + tlak.getSistolicki() + "/" + tlak.getDijastolicki() + " (" + tlak.getPuls() + ") " + tlak.getMasa_kg());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // nema podataka
                Toast toast = Toast.makeText(PovijestActivity.this, "Nema unosa u bazi!", Toast.LENGTH_LONG);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(28);
                toast.show();
            }

        }) {
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
                // postavljanje Adapter-a
                adapter = new ArrayAdapter<>(PovijestActivity.this, R.layout.activity_povijest_list_item, R.id.tv_item, arrayListTv);
                // povezivanje Adapter-a sa listView-om
                listView.setAdapter(adapter);
            }
        });
    }


    // privatna metoda za brisanje unosa, Volley
    private void deleteEntry(String id) {
        String url = DataHelper.BASE_URL + "/entries/" + id;

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // ukoliko je brisanje bilo uspješno, vraćanje JSON objekta s porukom
                String message = response.optString("message");
                if(message.equalsIgnoreCase("Obrisano!")) {
                    // uspješno brisanje, zatvaranje dijaloškog okvira i osvježenje podataka
                    dialog.cancel();
                    setData();
                    Toast toast = Toast.makeText(PovijestActivity.this, "Uspješno obrisan unos!", Toast.LENGTH_SHORT);
                    LinearLayout toastLayout = (LinearLayout) toast.getView();
                    TextView toastTV = (TextView) toastLayout.getChildAt(0);
                    toastTV.setTextSize(28);
                    toast.show();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // poruka ukoliko brisanje unosa nije bilo uspješno
                Toast toast = Toast.makeText(PovijestActivity.this, "Neuspješno brisanje unosa!", Toast.LENGTH_SHORT);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(28);
                toast.show();
            }

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", helper.getToken());
                return headers;
            }
        };

        queue.add(request);
    }


    // navigacija, izbornik kroz aplikaciju
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_povijest, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pocetni_menu:
                ActivityHelper.startActivity(PovijestActivity.this, MainActivity.class);
                return true;
            case R.id.unos_menu:
                ActivityHelper.startActivity(PovijestActivity.this, UnosActivity.class);
                return true;
            case R.id.masa_menu:
                ActivityHelper.startActivity(PovijestActivity.this, UnosMasaActivity.class);
                return true;
            case R.id.dijagnoza_menu:
                ActivityHelper.startActivity(PovijestActivity.this, DijagnozaTabsActivity.class);
                return true;
            case R.id.logout_menu:
                helper.logout();
                ActivityHelper.startActivity(PovijestActivity.this, PrijavaActivity.class);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}