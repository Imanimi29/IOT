package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private double temperature = 0;
    private int rain = 0;
    private double humidity = 0;
    private double light = 0;

    private Messenger messenger = null;

    private ProgressBar progressBar,progressBar_Rain,progressBar_Humidity;
    private Button btn1;
    private Button btn_setAlerts;
    private TextView textView,textViewProgress,textView_RainValue,textView_humidityValue,textView_todayDate,title_rain,title_humidity;
    private ConstraintLayout layout;
    private ProgressDialog nDialog;

    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        getData();

        Intent intent = new Intent(this,FetchData.class);
        startService(intent);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
            }
        });

        btn_setAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_ = new Intent(MainActivity.this,Alerts.class);
                startActivity(intent_);
            }
        });
    }

    private void updateProgressBar() {
        progressBar.setProgress((int) temperature);
        progressBar_Rain.setProgress(rain);
        progressBar_Humidity.setProgress((int) humidity);
        textView_humidityValue.setText(humidity+"%");
        textView_RainValue.setText(rain+"%");
        char degreesymbol = '\u00B0';
        textView.setText(temperature+" F");
        if(temperature>50)
        {
            layout.setBackgroundResource(R.drawable.sun);
        }
        else
        {
            layout.setBackgroundResource(R.drawable.rain);
        }

        nDialog.dismiss();
    }

    private void getData() {
        nDialog.show();

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://api.thingspeak.com/channels/1733817/feeds.json?api_key=R479FUYYPK4LX4I3&results=1", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("API response", response.toString());
                try {
                    temperature = Double.parseDouble(((JSONObject) response.getJSONArray("feeds").get(0)).get("field1").toString());
                    humidity = Double.parseDouble(((JSONObject) response.getJSONArray("feeds").get(0)).get("field2").toString());
                    rain = Integer.parseInt(((JSONObject) response.getJSONArray("feeds").get(0)).get("field3").toString());
                    light = Double.parseDouble(((JSONObject) response.getJSONArray("feeds").get(0)).get("field4").toString());
                    updateProgressBar();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("API Error!",error.toString());
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    private void initViews(){
        progressBar=findViewById(R.id.progress_bar);
        progressBar_Rain=findViewById(R.id.progress_bar2_rain);
        progressBar_Humidity=findViewById(R.id.progress_bar1_humidity);
        textView_RainValue=findViewById(R.id.text_view_progress2_rain);
        textView_humidityValue=findViewById(R.id.text_view_progress1_humidity);
        textView_todayDate=findViewById(R.id.tv_today_date);
        layout=(ConstraintLayout)findViewById(R.id.parent_layout);
        textView=findViewById(R.id.text_view_progress);

        title_rain=findViewById(R.id.text_view_rain);
        title_humidity=findViewById(R.id.text_view_humidity);
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(date);
        textView_todayDate.setText(formattedDate);
        title_rain.setText("Rain");
        title_humidity.setText("Humidity");
        btn1=findViewById(R.id.btn_refresh);
        btn_setAlerts = findViewById(R.id.btn_setAlerts);

        nDialog = new ProgressDialog(this);
        nDialog.setMessage("Updating");
        nDialog.setTitle("Updating");
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
    }
}