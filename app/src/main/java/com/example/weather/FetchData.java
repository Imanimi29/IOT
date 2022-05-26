package com.example.weather;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class FetchData extends Service {
    private double temperature;
    private double humidity;
    private int rain;
    private double light;

    private Long temp_low = null;
    private Long temp_high = null;
    private Long humid_low = null;
    private Long humid_high = null;
    private Boolean rain_check = null;

    SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("WeatherAlert","Weather Alert",NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Monitoring Weather")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);

        Handler mHandler = new Handler();
        Runnable mRunnableTask = new Runnable()
        {
            @Override
            public void run() {
                sharedPreferences = getSharedPreferences("alertData", MODE_PRIVATE);

                temp_low = null;
                temp_high = null;
                humid_low = null;
                humid_high = null;
                rain_check = null;

                if(sharedPreferences.getBoolean("alertSet",false) == true){
                    if(sharedPreferences.contains("temp_low")){
                        temp_low = sharedPreferences.getLong("temp_low",0);
                        temp_high = sharedPreferences.getLong("temp_high",0);
                    }
                    if(sharedPreferences.contains("humid_low")){
                        humid_low = sharedPreferences.getLong("humid_low",0);
                        humid_high = sharedPreferences.getLong("humid_high",0);
                    }
                    if(sharedPreferences.contains("rain_check")){
                        rain_check = sharedPreferences.getBoolean("rain_check",false);
                    }
                    getData(3,null);
                }

                mHandler.postDelayed(this,5000);
            }
        };
        mHandler.postDelayed(mRunnableTask,5000);

        return START_STICKY;
    }

    private void checkForAlerts(){
        if (temp_low!=null && (temperature<temp_low || temperature>temp_high) && sharedPreferences.getLong("prevT",-9999) != (long)temperature){
            String str = "Temperature Alert! Temperature is "+temperature;
            generateNotification(str);
        }
        if (humid_low!=null && (humidity < humid_low || humidity > humid_high) && sharedPreferences.getLong("prevH",-9999)!= (long)humidity) {
            String str = "Humidity Alert! Humidity is " + humidity;
            generateNotification(str);
        }
        if(rain_check && rain==1 && sharedPreferences.getBoolean("prevR",false) != true){
            String str = "Rain Alert! Rain Detected";
            generateNotification(str);
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("prevT", (long)temperature);
        editor.putLong("prevH",(long)humidity);
        editor.putBoolean("prevR",(rain == 1) ? true : false);
        editor.commit();
    }

    private void getData(int job, Message msg) {
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

                    if (job == 3){
                        checkForAlerts();
                    }
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

    private void generateNotification(String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"WeatherAlert");
        builder.setContentTitle("Weather Alert!");
        builder.setContentText(message);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(1,builder.build());
    }

    public void onDestroy() {

        Log.d("FetchDataService","Destroying FetchData Service");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, ServiceBroadcast.class);
        this.sendBroadcast(broadcastIntent);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}