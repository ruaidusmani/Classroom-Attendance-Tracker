package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class ScanNFCActivityCheckOut extends AppCompatActivity {
    PreferencesController preferencesController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_nfcactivity);
        preferencesController = new PreferencesController(getApplicationContext());

        // Toolbar
        Toolbar checkout_toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(checkout_toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Classroom Checkout");

        //Toolbar items
        checkout_toolbar.showOverflowMenu();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        //Back button
        if (id == android.R.id.home){
            this.finish();
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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