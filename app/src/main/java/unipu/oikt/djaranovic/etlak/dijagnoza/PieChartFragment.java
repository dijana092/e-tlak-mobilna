package unipu.oikt.djaranovic.etlak.dijagnoza;

import android.graphics.Color;
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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import unipu.oikt.djaranovic.etlak.R;
import unipu.oikt.djaranovic.etlak.model.KrvniTlak;
import unipu.oikt.djaranovic.etlak.utils.DataHelper;

public class PieChartFragment extends Fragment { // prvi tab zaslona Dijagnoza, 'tortni' prikaz

    // privatne varijable, vrijednosti krvnog tlaka
    private float optimalan = 0;
    private float normalan = 0;
    private float povisen = 0;
    private float hipertenzija1 = 0;
    private float hipertenzija2 = 0;
    private float hipertenzija3 = 0;
    private float izoliranaSisHipertenzija = 0;

    private float brojac = 0;

    private DataHelper helper;
    //private DBHelper mojaBaza;

    private PieChart pieChart;

    public PieChartFragment() {
        // prazan javni konstruktor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle("Dijagnoza");

        helper = new DataHelper(getActivity());
        //mojaBaza = new DBHelper(getActivity());
        //mojaBaza.getAllData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // postavljanje izgleda fragmenta
        return inflater.inflate(R.layout.activity_dijagnoza_piechart_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        pieChart = (PieChart) getView().findViewById(R.id.chart);
        // poruka ukoliko nema podataka za grafikon
        pieChart.setNoDataText("");
    }


    // javna metoda vezano za vraćanje na aktivnosti
    @Override
    public void onResume() {
        super.onResume();

        // resetiranje brojača na nule
        optimalan = 0;
        normalan = 0;
        povisen = 0;
        hipertenzija1 = 0;
        hipertenzija2 = 0;
        hipertenzija3 = 0;
        izoliranaSisHipertenzija = 0;

        brojac = 0;

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
                inicijalizirajDijagnoze(list);
                inicijalizirajGraf(list);
            }
        });
    }


    // inicijaliziranje dijagnoza
    private void inicijalizirajDijagnoze(ArrayList<KrvniTlak> list) {
        // prolaz kroz podatake u bazi i izračun dijagnoza
        for(int i=0;i<list.size();i++) {
            KrvniTlak tlak = list.get(i);
            if (Integer.parseInt(tlak.getSistolicki()) < 120 & Integer.parseInt(tlak.getDijastolicki()) < 80) {
                optimalan++; //<120 | <80
                brojac++;
            }else if ((Integer.parseInt(tlak.getSistolicki()) >= 120 & Integer.parseInt(tlak.getSistolicki()) <= 129) & (Integer.parseInt(tlak.getDijastolicki()) >= 80 & Integer.parseInt(tlak.getDijastolicki()) <= 84)) {
                normalan++; //120-129 | 80-84
                brojac++;
            }else if ((Integer.parseInt(tlak.getSistolicki()) >= 130 & Integer.parseInt(tlak.getSistolicki()) <= 139) & (Integer.parseInt(tlak.getDijastolicki()) >= 85 & Integer.parseInt(tlak.getDijastolicki()) <= 89)) {
                povisen++; //130-139 | 85-89
                brojac++;
            }else if ((Integer.parseInt(tlak.getSistolicki()) >= 140 & Integer.parseInt(tlak.getSistolicki()) <= 159) & (Integer.parseInt(tlak.getDijastolicki()) >= 90 & Integer.parseInt(tlak.getDijastolicki()) <= 99)) {
                hipertenzija1++; //140-159 | 90-99
                brojac++;
            }else if ((Integer.parseInt(tlak.getSistolicki()) >= 160 & Integer.parseInt(tlak.getSistolicki()) <= 179) & (Integer.parseInt(tlak.getDijastolicki()) >= 100 & Integer.parseInt(tlak.getDijastolicki()) <= 109)) {
                hipertenzija2++; //160-179 | 100-109
                brojac++;
            }else if (Integer.parseInt(tlak.getSistolicki()) >= 180 & Integer.parseInt(tlak.getDijastolicki()) >= 110) {
                hipertenzija3++; //>=180 | >=110
                brojac++;
            }else if (Integer.parseInt(tlak.getSistolicki()) >= 140 & Integer.parseInt(tlak.getDijastolicki()) < 90) {
                izoliranaSisHipertenzija++; //>=140 | <90
                brojac++;
            }
        }
    }


    // inicijaliziranje PieChart grafikona
    private void inicijalizirajGraf(ArrayList<KrvniTlak> list) {
        // ArrayList sa nazivima vrijednosti krvnog tlaka
        ArrayList<String> labels = new ArrayList<>();
        // ArrayList sa instancama određenih izmjerenih vrijednosti krvnog tlaka
        ArrayList<Entry> entries = new ArrayList<>();

        if (optimalan > 0) {
            optimalan = (float) Math.floor(((optimalan / brojac) * 100)+0.5);
            entries.add(new Entry(optimalan, 0));
            labels.add("Optimalan");
        }

        if (normalan > 0) {
            normalan = (float) Math.floor(((normalan / brojac) * 100)+0.5);
            entries.add(new Entry(normalan, 1));
            labels.add("Normalan");
        }

        if (povisen > 0) {
            povisen = (float) Math.floor(((povisen / brojac) * 100)+0.5);
            entries.add(new Entry(povisen, 2));
            labels.add("Povišen");
        }

        if (hipertenzija1 > 0) {
            hipertenzija1 = (float) Math.floor(((hipertenzija1 / brojac) * 100)+0.5);
            entries.add(new Entry(hipertenzija1, 3));
            labels.add("Hipertenzija I");
        }

        if (hipertenzija2 > 0) {
            hipertenzija2 = (float) Math.floor(((hipertenzija2 / brojac) * 100)+0.5);
            entries.add(new Entry(hipertenzija2, 4));
            labels.add("Hipertenzija II");
        }

        if (hipertenzija3 > 0) {
            hipertenzija3 = (float) Math.floor(((hipertenzija3 / brojac) * 100)+0.5);
            entries.add(new Entry(hipertenzija3, 5));
            labels.add("Hipertenzija III");
        }

        if (izoliranaSisHipertenzija > 0) {
            izoliranaSisHipertenzija = (float) Math.floor(((izoliranaSisHipertenzija / brojac) * 100)+0.5);
            entries.add(new Entry(izoliranaSisHipertenzija, 6));
            labels.add("Izoliran");
        }

        if (entries.size() > 0) {
            // unos prikupljenih podataka u PieDataSet
            PieDataSet dataset = new PieDataSet(entries, "");

            // skup labela, vrijednosti za PieChart
            PieData data = new PieData(labels, dataset);

            // postavljanje podataka i opisa
            pieChart.setDrawSliceText(false);
            pieChart.setCenterText("%");
            pieChart.setCenterTextColor(Color.parseColor("#972C39"));
            pieChart.setCenterTextSize(25);
            pieChart.setDescription("");

            // postavljanje boja i stila grafikona
            dataset.setColors(new int[]{getResources().getColor(R.color.color_1), getResources().getColor(R.color.color_2),
                    getResources().getColor(R.color.color_3), getResources().getColor(R.color.color_4),
                    getResources().getColor(R.color.color_5), getResources().getColor(R.color.color_6),
                    getResources().getColor(R.color.color_7),});
            dataset.setValueTextSize(23);
            dataset.setValueTextColor(Color.parseColor("#66000000"));

            // postavljanje centralnog radijusa
            pieChart.setHoleRadius(15);
            pieChart.setTransparentCircleRadius(20);

            // postavljanje legende grafikona
            Legend legend = pieChart.getLegend();
            legend.setTextSize(23);
            legend.setTextColor(Color.parseColor("#972C39"));
            legend.setWordWrapEnabled(true);
            legend.setForm(Legend.LegendForm.SQUARE);
            legend.setFormSize(23);
            legend.setXEntrySpace(23);
            legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);

            // postavljanje podataka
            pieChart.setData(data);
            data.setValueFormatter(new DecimalRemover(new DecimalFormat("###,###,###")));

            // brža animacija
            pieChart.animateX(1000);
        }
    }
}