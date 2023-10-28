package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ScanNFCActivitySignIn extends AppCompatActivity {
    PreferencesController preferencesController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_nfcactivity);
        preferencesController = new PreferencesController(getApplicationContext());

        //start the NFC Reading service
        Intent serviceIntent1 = new Intent(this, NFCHost.class);
        Log.d("Status Main: ", preferencesController.getString("NFCString"));
        preferencesController.setPreference("NFCString", "Sign-in");
        serviceIntent1.putExtra("NFCString", preferencesController.getString("NFCString"));
        startService(serviceIntent1);
    }

    //when the activity is resumed, start the NFC Reading service
    @Override
    protected void onResume(){
        super.onResume();
        Intent serviceIntent1 = new Intent(this, NFCHost.class);
        preferencesController.setPreference("NFCString", "Sign-in");
        Log.d("Status Main: ", preferencesController.getString("NFCString"));
        serviceIntent1.putExtra("NFCString", "Sign-in"); // to change
        startService(serviceIntent1);
    }


    //stop the service when the activity is paused or destroyed
    @Override
    protected void onPause(){
        super.onPause();
        Intent serviceIntent1 = new Intent(this, NFCHost.class);
        stopService(serviceIntent1);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Intent serviceIntent1 = new Intent(this, NFCHost.class);
        stopService(serviceIntent1);
    }
}