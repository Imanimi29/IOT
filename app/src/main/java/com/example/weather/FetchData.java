package com.example.weather;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

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

    private int counter = 0;

    private final int JOB_1 = 1;
    private final int JOB_1_RESPONSE = 2;

    private Messenger messenger = new Messenger(new IncomingHandler());

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);

        Handler mHandler = new Handler();
        Runnable mRunnableTask = new Runnable()
        {
            @Override
            public void run() {
                counter++;
                Log.d("FetchDataService","Counter is: "+counter);
                mHandler.postDelayed(this,5000);
            }
        };
        mHandler.postDelayed(mRunnableTask,5000);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    class IncomingHandler extends Handler{

        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == JOB_1){
                Message msgCopy = Message.obtain(msg);
                getData(JOB_1,msgCopy);
            }
            else {
                super.handleMessage(msg);
            }
        }
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

                    if(job == JOB_1){
                        sendDataToActivity(msg);
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

    private void sendDataToActivity(Message msg){
        Bundle bundle = new Bundle();
        Message message = android.os.Message.obtain(null,JOB_1_RESPONSE);

        bundle.putDouble("temperature",temperature);
        bundle.putDouble("humidity",humidity);
        bundle.putDouble("light",light);
        bundle.putInt("rain",rain);

        message.setData(bundle);

        try {
            msg.replyTo.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {

        Log.d("FetchDataService","Destroying FetchData Service");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, ServiceBroadcast.class);
        this.sendBroadcast(broadcastIntent);

        super.onDestroy();
    }
}