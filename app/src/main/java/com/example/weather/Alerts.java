package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class Alerts extends AppCompatActivity {

    private EditText tempLow;
    private EditText tempHigh;
    private EditText humidityLow;
    private EditText humidityHigh;
    private CheckBox rain;
    private Button btn_save;
    private Button btn_clear;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);
        initViews();

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveValues();
            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleClearButton();
            }
        });

        sharedPreferences = getSharedPreferences("alertData", MODE_PRIVATE);
    }

    private void initViews(){
        tempLow = findViewById(R.id.temp_low_input);
        tempHigh = findViewById(R.id.temp_high_input );
        humidityLow = findViewById(R.id.hum_low_input);
        humidityHigh = findViewById(R.id.hum_high_input);
        rain = findViewById(R.id.rain_checkBox);
        btn_save = findViewById(R.id.save_btn);
        btn_clear = findViewById(R.id.clear_btn);
    }

    private void saveValues(){
        String temp_low = tempLow.getText().toString();
        String temp_high = tempHigh.getText().toString();
        String humid_low = humidityLow.getText().toString();
        String humid_high = humidityHigh.getText().toString();
        boolean rain_check = rain.isChecked();

        SharedPreferences.Editor editor = sharedPreferences.edit();

        boolean anyThingToSave = false;

        if(!(temp_low.matches("") && temp_high.matches(""))) {
            editor.putLong("temp_low", Long.parseLong(temp_low));
            editor.putLong("temp_high", Long.parseLong(temp_high));
            anyThingToSave = true;
        }

        if (!(humid_low.matches("") && humid_high.matches(""))) {
            editor.putLong("humid_low", Long.parseLong(humid_low));
            editor.putLong("humid_high", Long.parseLong(humid_high));
            anyThingToSave = true;
        }

        if(anyThingToSave || rain_check) {
            editor.putBoolean("rain_check", rain_check);
            editor.putBoolean("alertSet",true);
            editor.commit();
        }
    }

    private void handleClearButton() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("alertSet",false);
        editor.putLong("prevT", -9999);
        editor.putLong("prevH",-9999);
        editor.putBoolean("prevR",false);
        editor.commit();
    }
}