package unipu.oikt.djaranovic.etlak.dijagnoza;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.numetriclabz.numandroidcharts.ChartData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unipu.oikt.djaranovic.etlak.R;
import unipu.oikt.djaranovic.etlak.model.KrvniTlak;
import unipu.oikt.djaranovic.etlak.utils.DataHelper;

public class ScatterChartFragment extends Fragment { // drugi tab zaslona Dijagnoza, 'raspršeni' prikaz

    // privatne varijable
    private ScatterChart scatterChart;
    private DataHelper helper;

    public ScatterChartFragment() {
        // prazan javni konstruktor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new DataHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // postavljanje izgleda fragmenta
        return inflater.inflate(R.layout.activity_dijagnoza_scatterchart_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        scatterChart = (ScatterChart) getView().findViewById(R.id.chart);
        // poruka ukoliko nema podataka za grafikon
        scatterChart.setNoDataText("");
    }


    // javna metoda vezano za vraćanje na aktivnosti
    @Override
    public void onResume() {
        super.onResume();
        setData();
    }


    // metoda za postavljanje podataka, Volley
    private void setData() {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = DataHelper.BASE_URL + "/entries";
        final ArrayList<KrvniTlak> list = new ArrayList<>();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject object = response.getJSONObject(i);
                        String id, sys, dia, pulse, weight, date;
                        id = object.getString("_id");
                        sys = object.getString("sys");
                        dia = object.getString("dia");
                        pulse = object.getString("pulse");
                        weight = object.getString("weight");
                        date = DataHelper.convertDate(object.getString("date"));

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
                // nema podataka
                /*Toast toast = Toast.makeText(getActivity(), "Neuspješan dohvat podataka!", Toast.LENGTH_LONG);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(28);
                toast.show();*/
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
                // ukoliko nema podataka, prekid
                if(list.isEmpty()) return;
                inicijalizirajGraf(list);
            }
        });
    }


    /*
     ScatterChart:
     x-os: dijastolički
     y-os: sistolički
    */

    // inicijalizacija ScatterChart grafikona
    private void inicijalizirajGraf(ArrayList<KrvniTlak> list) {
        /*DBHelper dbHelper = new DBHelper(getActivity());
        Cursor res = dbHelper.getAllDataByDiastolic();*/

        // ArrayList sa nazivima
        ArrayList<Entry> entries = new ArrayList<>();
        // ArrayList sa instancama
        ArrayList<String> labels = new ArrayList<>();

        /*int labelIndex = 0;
        while (res.moveToNext()) {
            entries.add(new Entry((float)res.getInt(1), labelIndex));
            labels.add(String.valueOf(res.getInt(2)));
            labelIndex++;
        }*/

        for(int i=0; i<list.size(); i++) {
            // prolazak kroz unose iz baze podataka i pripremanje za prikaz na grafikonu
            KrvniTlak tlak = list.get(i);
            Entry entry = new Entry(Float.parseFloat(tlak.getSistolicki()), i);
            entries.add(entry);
            labels.add(tlak.getDijastolicki());
        }

        if (entries.size() > 0) {
            // postavljanje opisa, boja, izgleda i animacija grafikona
            ScatterDataSet dataSet = new ScatterDataSet(entries, "");
            ScatterData data = new ScatterData(labels, dataSet);

            scatterChart.setDescription("x - DIA, y - SYS");
            scatterChart.setDescriptionTextSize(23);
            scatterChart.setDescriptionColor(Color.parseColor("#972C39"));
            scatterChart.setDescriptionPosition(625,1250);

            dataSet.setColor(Color.parseColor("#CD6155"));
            dataSet.setScatterShapeSize(15);
            dataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
            dataSet.setDrawValues(false);

            XAxis xAxis = scatterChart.getXAxis();
            xAxis.setTextSize(23);
            xAxis.setTextColor(Color.parseColor("#972C39"));
            YAxis leftAxis = scatterChart.getAxisLeft();
            YAxis rightAxis = scatterChart.getAxisRight();
            leftAxis.setTextSize(23);
            leftAxis.setTextColor(Color.parseColor("#972C39"));
            rightAxis.setTextSize(23);
            rightAxis.setTextColor(Color.parseColor("#972C39"));

            scatterChart.getLegend().setEnabled(false);
            scatterChart.animateY(1000);

            // učitavanje podataka nakon što su sve opcije podešene
            scatterChart.setData(data);
        }
    }
}