package com.example.classroomattendancetracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;

public class NFCHost extends HostApduService {

//    PreferencesController preferencesController = new PreferencesController(this.getApplicationContext());
    String NFCString;
//    Bundle bundle = intent.getExtras();
//    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    PreferencesController preferencesController;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("HCE", "NFC HCE Service Started");

        NFCString = intent.getStringExtra("NFCString");

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }


    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        preferencesController = new PreferencesController(this.getApplicationContext());

        //read NFCString from shared preferences
        NFCString = preferencesController.getString("NFCString");
        preferencesController.setPreference("NFCString", "null");

        if ("null".equals(NFCString)){
            Intent intent = new Intent(this, MainActivity.class);
            //pass extra to main activity
            intent.putExtra("Error Code", 300);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        Log.d("HCE", "Sending String to NFC: " + NFCString);
        byte[] test = NFCString.getBytes();
        return test;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d("HCE", "Deactivated: " + reason);
        Log.d("HCE", "NFCString: " + NFCString);
    }

}
