package unipu.oikt.djaranovic.etlak.registracija;

import android.annotation.SuppressLint;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import unipu.oikt.djaranovic.etlak.R;
import unipu.oikt.djaranovic.etlak.prijava.PrijavaActivity;
import unipu.oikt.djaranovic.etlak.utils.ActivityHelper;
import unipu.oikt.djaranovic.etlak.utils.DataHelper;

public class RegistracijaActivity extends AppCompatActivity { // klasa vezana za registraciju korisnika

    // privatne varijable
    private EditText usernameInput;
    private EditText passwordInput;
    private Button buttonRegistration;
    private TextView link;
    private RequestQueue requestQueue;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); // skrivanje status bar-a
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide(); // skrivanje title bar-a

        setContentView(R.layout.activity_registracija);

        requestQueue = Volley.newRequestQueue(this);

        usernameInput = (EditText) findViewById(R.id.et_username_reg);
        passwordInput = (EditText) findViewById(R.id.et_password_reg);

        buttonRegistration = (Button) findViewById(R.id.button_registracija);

        link = (TextView) findViewById(R.id.textViewLink);

        // Button za prijelaz na slijedeću aktivnost, PrijavaActivity
        buttonRegistration.setOnTouchListener(new View.OnTouchListener() { // efekt pritisnutog gumba
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonRegistration.getBackground().setColorFilter(Color.parseColor("#F7919D"), PorterDuff.Mode.SRC_ATOP);
                        //ActivityHelper.startActivity(RegistracijaActivity.this, PrijavaActivity.class);
                        // umjesto prelaska na PrijavaActivity, pokušaj registracije
                        tryRegister();
                        return true;
                    case MotionEvent.ACTION_UP:
                        buttonRegistration.getBackground().clearColorFilter();
                        return true;
                }
                return false;
            }
        });

        // link na prijavu ukoliko korisnik ima račun
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityHelper.startActivity(RegistracijaActivity.this, PrijavaActivity.class);
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


    // privatna metoda za pokušaj registracije
    private void tryRegister() {
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
        String url = DataHelper.BASE_URL + "/users/register";

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
                Log.d("RESPONSE", "onErrorResponse: " + response.toString());
                boolean success = false;
                String msg = "";
                try {
                    // true u slučaju uspješne registracije, false u slučaju neuspješne registracije
                    success = response.getBoolean("success");
                    // tekstualna poruka sa servera o (ne)uspjehu
                    msg = response.getString("msg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Toast toast = Toast.makeText(RegistracijaActivity.this, msg, Toast.LENGTH_LONG);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(28);
                toast.show();

                if(success) {
                    // uspješna registracija, prebačaj na PrijavaActivity
                    ActivityHelper.startActivity(RegistracijaActivity.this, PrijavaActivity.class);
                } else {
                    //todo: ukoliko je nešto pošlo po zlu, obavijestiti korisnika zajedno sa gornjim toast-om
                }
                
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // pogreška, obično zbog Heroku 'uspavanosti' ili nema Interneta
                Toast toast = Toast.makeText(RegistracijaActivity.this, "Pokušajte ponovno!", Toast.LENGTH_LONG);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(28);
                toast.show();
                Log.d("ERROR", "onErrorResponse: " + error);
            }
        });

        requestQueue.add(request);
    }
}