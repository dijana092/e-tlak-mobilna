package unipu.oikt.djaranovic.etlak.unos;

import android.content.Intent;
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

public class UnosActivity extends AppCompatActivity { // klasa vezana za zaslon za unos krvnog tlaka

    // privatne varijable
    private EditText etSistolicki;
    private EditText etDijastolicki;
    private EditText etPuls;
    private Button buttonBrisanje;
    private Button buttonUnosTlaka;
    private DataHelper helper;
    private MediaPlayer sound;
    private boolean val = false;
    private boolean firstEntry = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unos);

        this.setTitle("Unos krvnog tlaka");

        // EditText-ovi za unos vrijednosti krvnog tlaka
        etSistolicki = (EditText)findViewById(R.id.et_sistolicki);
        etDijastolicki = (EditText)findViewById(R.id.et_dijastolicki);
        etPuls = (EditText)findViewById(R.id.et_puls);

        // onemogućavanje unosa putem klasične android tipkovnice
        etSistolicki.setInputType(InputType.TYPE_NULL);
        etDijastolicki.setInputType(InputType.TYPE_NULL);
        etPuls.setInputType(InputType.TYPE_NULL);

        // instanciranje pomoćne klase
        helper = new DataHelper(this);

        // provjera da li se radi o novom unosu
        firstEntry = getIntent().getBooleanExtra("firstEntry", false);

        // Button za brisanje vrijednosti krvnog tlaka
        buttonBrisanje = (Button)findViewById(R.id.button_brisanje);

        // Button za potvrdu unosa krvnog tlaka
        buttonUnosTlaka = (Button)findViewById(R.id.button_unos);

        // zvuk vezan za uspješan unos krvnog tlaka
        sound = MediaPlayer.create(getApplicationContext(), R.raw.success);

        // Listener uz efekt za Button za brisanje
        buttonBrisanje.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonBrisanje.getBackground().setColorFilter(Color.parseColor("#F7919D"), PorterDuff.Mode.SRC_ATOP);
                        // EditText za unos vrijednosti sistoličkog tlaka
                        if (etSistolicki.isFocused()) {
                            if(etSistolicki.length() > 0 )
                                etSistolicki.setText(etSistolicki.getText().toString().substring(0, etSistolicki.length()-1));
                        }
                        // EditText za unos vrijednosti dijastoličkog tlaka
                        else if (etDijastolicki.isFocused()) {
                            if(etDijastolicki.length() > 0 ) {
                                etDijastolicki.setText(etDijastolicki.getText().toString().substring(0, etDijastolicki.length() - 1));
                                if (etDijastolicki.length() == 0)
                                    etSistolicki.requestFocus();
                            }
                        }
                        // EditText za unos vrijednosti pulsa
                        else if (etPuls.isFocused()) {
                            if(etPuls.length() > 0 ) {
                                etPuls.setText(etPuls.getText().toString().substring(0, etPuls.length() - 1));
                            if(etPuls.length() == 0)
                                etDijastolicki.requestFocus();
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        buttonBrisanje.getBackground().clearColorFilter();
                        return true;
                    }
                return false;
            }
        });

        // Listener uz efekt za Button potvrde
        buttonUnosTlaka.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonUnosTlaka.getBackground().setColorFilter(Color.parseColor("#F7919D"), PorterDuff.Mode.SRC_ATOP);
                        // ukoliko EditText-ovi nisu prazni
                        if (!etSistolicki.getText().toString().equals("") &&
                            !etDijastolicki.getText().toString().equals("") &&
                            !etPuls.getText().toString().equals("")) {

                            // Integer-i koji se unose u bazu podataka
                            int sistolicki = Integer.parseInt(etSistolicki.getText().toString());
                            int diastolicki = Integer.parseInt(etDijastolicki.getText().toString());
                            int puls = Integer.parseInt(etPuls.getText().toString());

                            // instanciranje novog objekta tipa ValidacijaUnosa za provjeru unosa
                            ValidacijaUnosa validacija = new ValidacijaUnosa(getApplicationContext());
                            val = validacija.validirajTlak(sistolicki, diastolicki, puls);

                            // ukoliko je validacija uspješna
                            if (val) {
                                dodajNoviUnos(sistolicki, diastolicki, puls); // dodavanje novog unosa u bazu
                                View v = getWindow().getDecorView().findFocus();
                            } else {
                                // validacija nije uspješna zbog neispravno unesenih vrijednosti krvnog tlaka
                                Toast toast = Toast.makeText(getApplicationContext(), "Greška!", Toast.LENGTH_SHORT);
                                LinearLayout toastLayout = (LinearLayout) toast.getView();
                                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                                toastTV.setTextSize(28);
                                toast.show();
                            }
                        }
                        else {
                            // u slučaju da sva polja EditText-ova nisu popunjena
                            Toast toast = Toast.makeText(getApplicationContext(), "Popunite sva polja!", Toast.LENGTH_SHORT);
                            LinearLayout toastLayout = (LinearLayout) toast.getView();
                            TextView toastTV = (TextView) toastLayout.getChildAt(0);
                            toastTV.setTextSize(28);
                            toast.show();
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        buttonUnosTlaka.getBackground().clearColorFilter();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }


    // javna metoda
    public void onClickNumberButton(View v) {

        CharSequence broj = ((Button)v).getText();

        // unos u EditText vezan za sistolički tlak
        if (etSistolicki.isFocused()) {
            if(etSistolicki.getText().length() > 2) {
                Toast toast = Toast.makeText(getApplicationContext(), "Moguće je unijeti maksimalno 3 znamenke!", Toast.LENGTH_SHORT);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(28);
                toast.show();
                return;
            }
            etSistolicki.append(broj);
            if (etSistolicki.length() == 3)
                etDijastolicki.requestFocus();
        }
        // unos u EditText vezan za dijastolički tlak
        else if (etDijastolicki.isFocused()) {
            if(etDijastolicki.getText().length() > 2) {
                Toast toast = Toast.makeText(getApplicationContext(), "Moguće je unijeti maksimalno 3 znamenke!", Toast.LENGTH_SHORT);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(28);
                toast.show();
                return;
            }
            etDijastolicki.append(broj);
            if(etDijastolicki.getText().charAt(0) != '1' && etDijastolicki.getText().length() == 2)
                etPuls.requestFocus();
            if (etDijastolicki.length() == 3)
                etPuls.requestFocus();
        }
        // unos u EditText vezan za puls
        else if (etPuls.isFocused()) {
            if(etPuls.getText().length() > 2) {
                Toast toast = Toast.makeText(getApplicationContext(), "Moguće je unijeti maksimalno 3 znamenke!", Toast.LENGTH_SHORT);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(28);
                toast.show();
                return;
            }
            etPuls.append(broj);
        }
    }


    // privatna metoda za dodavanje novog unosa, Volley
    private void dodajNoviUnos(int sistolicki, int dijastolicki, int puls) {

        if(firstEntry) {
            // kod prvog unosa, prosljeđivanje unesenih podataka na unos dodatnog
            Intent intent = new Intent(UnosActivity.this, UnosMasaActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("sys", sistolicki);
            intent.putExtra("dia", dijastolicki);
            intent.putExtra("pulse", puls);
            intent.putExtra("firstEntry", true);
            startActivity(intent);

        } else {
            // ukoliko se ne radi o prvom unosu, učitavanje već unesene tjelesne mase iz lokalnog
            float weight = helper.getLastWeight();
            String url = DataHelper.BASE_URL + "/entries";

            // priprema parametara; podaci o tlaku su integer, no zbog tjelesne mase ih treba poslati u float obliku, tj. poslati sve u float obliku
            Map<String, Float> params = new HashMap<>();
            params.put("sys", (float) sistolicki);
            params.put("dia", (float) dijastolicki);
            params.put("pulse", (float) puls);
            params.put("weight", weight);
            // slanje parametara u JSON obliku, stvaranje iz mape
            JSONObject paramJSON = new JSONObject(params);

            RequestQueue queue = Volley.newRequestQueue(UnosActivity.this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, paramJSON, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    sound.start();
                    Toast toast = Toast.makeText(getApplicationContext(), "Uspješan unos!", Toast.LENGTH_LONG);
                    LinearLayout toastLayout = (LinearLayout) toast.getView();
                    TextView toastTV = (TextView) toastLayout.getChildAt(0);
                    toastTV.setTextSize(28);
                    toast.show();

                    // javljanje pomoćnoj klasi da ima unosa
                    helper.setHasEntries(true);

                    // otvaranje glavnog, Main zaslona nakon uspješnog unosa
                    ActivityHelper.startActivity(UnosActivity.this, MainActivity.class);
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // validacija nije uspješna zbog neispravno unesenih vrijednosti krvnog tlaka
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
        getMenuInflater().inflate(R.menu.menu_unos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pocetni_menu:
                ActivityHelper.startActivity(UnosActivity.this, MainActivity.class);
                return true;
            case R.id.masa_menu:
                ActivityHelper.startActivity(UnosActivity.this, UnosMasaActivity.class);
                return true;
            case R.id.dijagnoza_menu:
                ActivityHelper.startActivity(UnosActivity.this, DijagnozaTabsActivity.class);
                return true;
            case R.id.povijest_menu:
                ActivityHelper.startActivity(UnosActivity.this, PovijestActivity.class);
                return true;
            case R.id.logout_menu:
                helper.logout();
                ActivityHelper.startActivity(UnosActivity.this, PrijavaActivity.class);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}