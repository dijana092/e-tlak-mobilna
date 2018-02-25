package unipu.oikt.djaranovic.etlak.prijava;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import unipu.oikt.djaranovic.etlak.MainActivity;
import unipu.oikt.djaranovic.etlak.R;
import unipu.oikt.djaranovic.etlak.registracija.RegistracijaActivity;
import unipu.oikt.djaranovic.etlak.unos.UnosActivity;
import unipu.oikt.djaranovic.etlak.utils.ActivityHelper;
import unipu.oikt.djaranovic.etlak.utils.DataHelper;

public class PrijavaActivity extends AppCompatActivity { // klasa vezana za prijavu korisnika

    // privatne varijable
    private EditText usernameInput;
    private EditText passwordInput;
    private Button buttonLogin;
    private TextView link;
    RequestQueue requestQueue;

    // helper
    DataHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); // skrivanje status bar-a
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide(); // skrivanje title bar-a

        setContentView(R.layout.activity_prijava);

        requestQueue = Volley.newRequestQueue(this);

        // instanciranje pomoćne klase
        helper = new DataHelper(this);

        usernameInput = (EditText) findViewById(R.id.et_username_login);
        passwordInput = (EditText) findViewById(R.id.et_password_login);

        buttonLogin = (Button) findViewById(R.id.button_prijava);
        buttonLogin.getBackground().clearColorFilter();

        link = (TextView) findViewById(R.id.textViewLink);

        // Button za prijelaz na slijedeću aktivnost
        buttonLogin.setOnTouchListener(new View.OnTouchListener() { // efekt pritisnutog gumba
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonLogin.getBackground().setColorFilter(Color.parseColor("#F7919D"), PorterDuff.Mode.SRC_ATOP);
                        //ActivityHelper.startActivity(PrijavaActivity.this, MainActivity.class);
                        // umjesto prelaska na MainActivity, pokušaj login-a, prijave
                        tryLogin();
                        return true;
                    case MotionEvent.ACTION_UP:
                        buttonLogin.getBackground().clearColorFilter();
                        return true;
                }
                return false;
            }
        });

        // link na registraciju ukoliko korisnik nema račun
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityHelper.startActivity(PrijavaActivity.this, RegistracijaActivity.class);
            }
        });

        // 'pametno' brisanje
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    usernameInput.requestFocus();
                }
            }
        });
    }


    // privatna metoda za pokušaj login-a, prijave
    private void tryLogin() {
        // čitanje unešenih podataka i uklanjanje viška razmaka ukoliko postoje (trim)
        final String username, password;
        username = usernameInput.getText().toString().trim();
        password = passwordInput.getText().toString().trim();
        // izlaz u slučaju da su polja prazna
        if(username.isEmpty() || password.isEmpty()) {
            Toast toast = Toast.makeText(this, "Popunite sva polja!", Toast.LENGTH_LONG);
            LinearLayout toastLayout = (LinearLayout) toast.getView();
            TextView toastTV = (TextView) toastLayout.getChildAt(0);
            toastTV.setTextSize(28);
            toast.show();
            return;
        }

        // priprema putanje
        String url = DataHelper.BASE_URL + "/users/login";

        // priprema parametara
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        // parametri trebaju biti poslani u JSON obliku, stvaranje iz mape
        JSONObject paramJSON = new JSONObject(params);

        // stvaranje requesta
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, paramJSON, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // prošao request, otkrivanje što se dogodilo
                Log.d("RESPONSE", "onErrorResponse: " + response.toString());
                boolean success = false;
                String msg = "";
                try {
                    // true u slučaju uspješne prijave, false u slučaju neuspješne prijave
                    success = response.getBoolean("success");
                    // tekstualna poruka sa servera o (ne)uspjehu
                    msg = response.optString("msg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(success) {
                    // uspješna prijava, čitanje podataka
                    try {
                        String token = response.getString("token");
                        // podaci korisnika unutar user objetka u JSON-u
                        JSONObject user = response.getJSONObject("user");
                        String foundUserName = user.getString("username");
                        String foundUserId = user.getString("id");
                        // spremanje gornjih podataka lokalno u uređaj
                        DataHelper helper = new DataHelper(PrijavaActivity.this);
                        helper.login(foundUserId, foundUserName, token);
                        // provjera ima li korisnik unosa
                        tryEntries();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast toast = Toast.makeText(PrijavaActivity.this, msg, Toast.LENGTH_LONG);
                    LinearLayout toastLayout = (LinearLayout) toast.getView();
                    TextView toastTV = (TextView) toastLayout.getChildAt(0);
                    toastTV.setTextSize(28);
                    toast.show();
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // pogreška, obično zbog Heroku 'uspavanosti' ili nema Interneta
                Toast toast = Toast.makeText(PrijavaActivity.this, "Pokušajte ponovno!", Toast.LENGTH_LONG);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(28);
                toast.show();
                Log.d("ERROR", "onErrorResponse: " + error);
            }
        });

        requestQueue.add(request);
    }


    // privatna metoda za provjeru unosa, Volley
    private void tryEntries() {

        // API ruta za dohvaćanje svih unosa
        String url = DataHelper.BASE_URL + "/entries";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // zahtjev prošao bez greške, tj. ima unosa, javljanje pomoćnoj klasi da ima unosa
                helper.setHasEntries(true);
                // prebačaj na glavnu stranicu, MainActivity
                ActivityHelper.startActivity(PrijavaActivity.this, MainActivity.class);
                // onemogućavanje vraćanja na login
                finish();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // preusmjeravanje korisnika na unos krvnog tlaka kada podaci nisu pronađeni
                Intent intent = new Intent(PrijavaActivity.this, UnosActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                // javljanje da se radi o prvom unosu i da treba nakon krvnog tlaka unijeti i tjelesnu masu
                intent.putExtra("firstEntry", true);
                // javljanje pomoćnoj klasi da nema unosa
                helper.setHasEntries(false);
                startActivity(intent);
                // onemogućavanje vraćanja na login
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