package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private double temperature = 0;
    private int rain = 0;
    private double humidity = 0;
    private double light = 0;

    private final int JOB_1 = 1;
    private final int JOB_1_RESPONSE = 2;

    private Messenger messenger = null;

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

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            messenger = new Messenger(service);
            getData();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            messenger = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        initViews();

        Intent intent = new Intent(this,FetchData.class);
        startService(intent);
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);

        refresh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
            }
        });
    }

    private void updateProgressBar() {
        progressBar_temp.setProgress((int) temperature);
        progressBar_uv.setProgress((int) light);
        progressBar_Humidity.setProgress((int) humidity);
        humidity_percentage.setText(humidity+"%");
        //rain_value.setText(rain+"%");
        uv_percentage.setText(light+"%");
        if((int)rain == 1){
            title_rain.setText("Raining");
        }else{
            title_rain.setText("Not raining");
        }
        switchState = switch_temp.isChecked();
        if(switchState){
            temperature_tv.setText(temperature+"C");
        }else{
            temperature_tv.setText(temperature+"F");
        }

        char degreesymbol = '\u00B0';
        if(temperature>30)
        {
            layout.setBackgroundResource(R.drawable.clear_sky);
        }
        else
        {
            layout.setBackgroundResource(R.drawable.rainy_sky);
        }
        //setting small icon to check if cloudy
        if(light<100)
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

        Message msg = Message.obtain(null,JOB_1);
        msg.replyTo = new Messenger(new ResponseHandler());
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
        tv_today_date.setText(formattedDate);

        layout= (LinearLayout)findViewById(R.id.parent_layout);

        refresh_btn=findViewById(R.id.btn_refresh);

        nDialog = new ProgressDialog(this);
        nDialog.setMessage("Updating");
        nDialog.setTitle("Updating");
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
    }

    @Override
    protected void onStop() {
        unbindService(serviceConnection);
        messenger = null;
        super.onStop();

    }

    class ResponseHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == JOB_1_RESPONSE){
                temperature = msg.getData().getDouble("temperature");
                rain = msg.getData().getInt("rain");
                humidity = msg.getData().getDouble("humidity");
                light = msg.getData().getDouble("light");
                updateProgressBar();
            }
            else{
                super.handleMessage(msg);
            }
        }
    }
}