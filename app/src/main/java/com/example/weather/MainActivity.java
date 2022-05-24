package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
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

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private int temperature=60;
    private int rain=60;
    private int humidity=60;

    ProgressBar progressBar,progressBar_Rain,progressBar_Humidity;
    Button btn1;
    TextView textView,textViewProgress,textView_RainValue,textView_humidityValue,textView_todayDate,title_rain,title_humidity;
    ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        updateProgressBar();

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProgressBar();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://jsonplaceholder.typicode.com/todos/1", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("API response", response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("API Error!",error.toString());
            }
        });

        requestQueue.add(jsonObjectRequest);

        updateProgressBar();
    }

    private void updateProgressBar() {
        progressBar.setProgress(temperature);
        progressBar_Rain.setProgress(rain);
        progressBar_Humidity.setProgress(humidity);
        textView_humidityValue.setText(rain+"%");
        textView_RainValue.setText(humidity+"%");
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
    }
}