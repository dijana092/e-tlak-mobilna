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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import unipu.oikt.djaranovic.etlak.R;
import unipu.oikt.djaranovic.etlak.model.KrvniTlak;
import unipu.oikt.djaranovic.etlak.utils.DataHelper;

public class ScatterChartFragment extends Fragment { // drugi tab zaslona Dijagnoza, 'raspršeni' prikaz

    // privatna varijabla
    private DataHelper helper;

    public ScatterChartFragment() {
        // prazan javni konstruktor
    }

    // dodavanje PointsGraphSeries, DataPoint tip
    PointsGraphSeries<DataPoint> xySeries;

    // kreiranje GraphView objekta
    GraphView mScatterPlot;

    // globalni xyValueArray
    ArrayList<XYValue> xyValueArray;

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
        mScatterPlot = (GraphView) getView().findViewById(R.id.graph);

        // postavljanje granica na y-osi
        mScatterPlot.getViewport().setYAxisBoundsManual(true);
        mScatterPlot.getViewport().setMaxY(190);
        mScatterPlot.getViewport().setMinY(60);

        // postavljanje granica na x-osi
        mScatterPlot.getViewport().setXAxisBoundsManual(true);
        mScatterPlot.getViewport().setMaxX(130);
        mScatterPlot.getViewport().setMinX(60);

        // ostala obilježja
        mScatterPlot.getGridLabelRenderer().setTextSize(50);
        mScatterPlot.getGridLabelRenderer().setVerticalLabelsColor(Color.parseColor("#972C39"));
        mScatterPlot.getGridLabelRenderer().setHorizontalLabelsColor(Color.parseColor("#972C39"));
        mScatterPlot.getGridLabelRenderer().setVerticalAxisTitle("SYS");
        mScatterPlot.getGridLabelRenderer().setVerticalAxisTitleTextSize(50);
        mScatterPlot.getGridLabelRenderer().setHorizontalAxisTitle("DIA");
        mScatterPlot.getGridLabelRenderer().setHorizontalAxisTitleTextSize(50);
        mScatterPlot.getGridLabelRenderer().setVerticalAxisTitleColor(Color.parseColor("#972C39"));
        mScatterPlot.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.parseColor("#972C39"));
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
                if (list.isEmpty()) return;
                inicijalizirajGraf(list);
            }
        });
    }


    // metoda za kreiranje 'raspršenog' prikaza
    private void createScatterPlot() {
        // postavljanje obilježja
        xySeries.setShape(PointsGraphSeries.Shape.POINT);
        xySeries.setColor(Color.parseColor("#ffffff"));
        xySeries.setSize(15);

        mScatterPlot.addSeries(xySeries);
    }


    // metoda za sortiranje ArrayList<XYValue> uzimajući u obzir x vrijednosti
    private ArrayList<XYValue> sortArray(ArrayList<XYValue> array){
        // sortiranje xy-vrijednosti uzlazno i njihovo pripremanje za PointsGraphSeries<DataSet>
        int factor = Integer.parseInt(String.valueOf(Math.round(Math.pow(array.size(),2))));
        int m = array.size()-1;
        int count = 0;

        while(true){
            m--;
            if(m <= 0){
                m = array.size() - 1;
            }
            try{
                double tempY = array.get(m-1).getY();
                double tempX = array.get(m-1).getX();
                if(tempX > array.get(m).getX() ){
                    array.get(m-1).setY(array.get(m).getY());
                    array.get(m).setY(tempY);
                    array.get(m-1).setX(array.get(m).getX());
                    array.get(m).setX(tempX);
                }
                else if(tempY == array.get(m).getY()){
                    count++;
                }
                else if(array.get(m).getX() > array.get(m-1).getX()){
                    count++;
                }
                if(count == factor ){
                    break;
                }
            }catch(ArrayIndexOutOfBoundsException e){
                break;
            }
        }
        return array;
    }


    // inicijalizacija ScatterChart grafikona, x-os: dijastolički tlak, y-os: sistolički tlak
    private void inicijalizirajGraf(ArrayList<KrvniTlak> list) {

        xySeries = new PointsGraphSeries<>();

        // generiranje liste s x i y-osi
        xyValueArray = new ArrayList<>();
        for(int i = 0; i<list.size(); i++){
            KrvniTlak tlak = list.get(i);
            xyValueArray.add( new XYValue(Integer.parseInt(tlak.getDijastolicki()), Integer.parseInt(tlak.getSistolicki())));
        }
        // sortiranje uzlaznim redoslijedom
        xyValueArray = sortArray(xyValueArray);
        // dodavanje podataka serijama
        for(int i = 0;i <xyValueArray.size(); i++){
            double x = xyValueArray.get(i).getX();
            double y = xyValueArray.get(i).getY();
            xySeries.appendData(new DataPoint(x,y),true, 1000);
        }

        createScatterPlot();
    }
}