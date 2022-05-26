package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
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

    private Button btn_setAlerts;
    private ProgressBar progressBar_temp;
    private ProgressBar progressBar_uv;
    private ProgressBar progressBar_Humidity;
    private Button refresh_btn;
    private TextView tv_today_date;
    private TextView tv_location;
    private TextView temp_info;
    private TextView rain_value;
    private TextView humidity_percentage;
    private TextView uv_percentage;
    private TextView temperature_tv;
    private TextView title_rain;
    private TextView title_humidity;
    private TextView title_uv;
    private ImageView small_icon;
    private LinearLayout layout;

    private ProgressDialog nDialog;
    private Switch switch_temp;
    private Boolean switchState;

    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        initViews();
        getData();

        Intent intent = new Intent(this,FetchData.class);
        startService(intent);

        refresh_btn.setOnClickListener(new View.OnClickListener() {
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

        switch_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProgressBar();
            }
        });
    }

    private void updateProgressBar() {
        switchState = switch_temp.isChecked();

        progressBar_temp.setProgress((int) temperature);

        progressBar_uv.setProgress((int) light*100/1000);
        progressBar_Humidity.setProgress((int) humidity);
        humidity_percentage.setText(humidity+"%");
        //rain_value.setText(rain+"%");
        uv_percentage.setText((light*100/1000)+"%");
        if((int)rain == 1){
            title_rain.setText("Raining");
        }else{
            title_rain.setText("Not raining");
        }

        if(switchState){
            temperature_tv.setText(temperature+"C");
        }else{
            temperature_tv.setText(toFahrenheit(temperature)+"F");
        }

        char degreesymbol = '\u00B0';
        if(rain == 0)
        {
            layout.setBackgroundResource(R.drawable.clear_sky);
        }
        else
        {
            layout.setBackgroundResource(R.drawable.rainy_sky);
        }
        //setting small icon to check if cloudy
        if(light > 500)
        {
            temp_info.setText("Sunny  ");
            small_icon.setBackgroundResource(R.drawable.icons8_sun_48px);
        }
        else
        {
            temp_info.setText("Cloudy  ");
            small_icon.setBackgroundResource(R.drawable.icons8_partly_cloudy_day_48px);
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

    private double toFahrenheit(double cel){
        return (cel * (9.0/5.0)) + 32;
    }

    private void initViews(){
        tv_location=findViewById(R.id.tv_location);
        temp_info=findViewById(R.id.temp_info);
        small_icon=findViewById(R.id.small_icon);
        switch_temp=findViewById(R.id.switch_temp);
        switchState = switch_temp.isChecked();
        //temperature
        progressBar_temp=findViewById(R.id.progress_bar_temp);
        temperature_tv=findViewById(R.id.temp_tv);
        //humidity
        progressBar_Humidity=findViewById(R.id.progress_bar_humidity);
        humidity_percentage=findViewById(R.id.hum_percentage);
        title_humidity=findViewById(R.id.hum_tv);
        title_humidity.setText("Humidity");
        //uv
        progressBar_uv=findViewById(R.id.progress_bar_uv);
        uv_percentage=findViewById(R.id.uv_percentage);
        title_uv=findViewById(R.id.uv_tv);
        title_uv.setText("UV Index");
        //rain
        //rain_value=findViewById(R.id.rain_value);
        title_rain=findViewById(R.id.text_view_rain);

        //date
        tv_today_date=findViewById(R.id.tv_today_date);
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(date);


        //textView_todayDate.setText(formattedDate);
        title_rain.setText("Rain");
        title_humidity.setText("Humidity");
        btn_setAlerts = findViewById(R.id.alert_btn);

        tv_today_date.setText(formattedDate);

        layout= (LinearLayout)findViewById(R.id.parent_layout);

        refresh_btn=findViewById(R.id.btn_refresh);


        nDialog = new ProgressDialog(this);
        nDialog.setMessage("Updating");
        nDialog.setTitle("Updating");
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
    }
}