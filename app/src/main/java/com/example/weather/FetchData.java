package com.example.weather;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class FetchData extends Service {
    private double temperature;
    private double humidity;
    private int rain;
    private double light;

    private final int JOB_1 = 1;
    private final int JOB_1_RESPONSE = 2;

    private Messenger messenger = new Messenger(new IncomingHadnler());


    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    class IncomingHadnler extends Handler{

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
}