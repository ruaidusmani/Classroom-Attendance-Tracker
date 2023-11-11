package com.example.classroomattendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ScanNFCActivityCheckIn extends AppCompatActivity {
    PreferencesController preferencesController;

    ImageView imageViewError;
    ImageView imageViewSuccess;

    TextView textViewError;
    TextView textViewSuccess;

    FirebaseUser user;
    FirebaseAuth mAuth;
    String email;
    TextView textViewPersonInfo;

    boolean tried_to_sign_in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_nfc_check_in);
        Log.d("onCreate", "onCreate");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        preferencesController = new PreferencesController(getApplicationContext());
        imageViewError = findViewById(R.id.imageViewError);
        imageViewSuccess = findViewById(R.id.imageViewSuccess);
        textViewError = findViewById(R.id.textViewError);
        textViewSuccess = findViewById(R.id.textViewSuccess);
        textViewPersonInfo = findViewById(R.id.textViewPersonInfo);

        if(user != null){
            email = user.getEmail();
        }else{
            email = "null";
        }
        textViewPersonInfo.setText("You are currently logged in as: " + email + " | ID: " + preferencesController.getString("AndroidID") );
        Log.d("SCAN_NFC_EMAIL" , email);

        tried_to_sign_in = false;
        refresh();

        // Toolbar
        Toolbar check_in_toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(check_in_toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Classroom Check-in");

        //Toolbar items
        check_in_toolbar.showOverflowMenu();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //start the NFC Reading service
        Intent serviceIntent1 = new Intent(this, NFCHost.class);
        Log.d("Status Main: ", preferencesController.getString("NFCString"));
        preferencesController.setPreference("NFCString", "CI_" + preferencesController.getString("AndroidID") );
        if ("null".equals(preferencesController.getString("NFCString") ) ){ // TODO: remove this, not needed anymore, just send the payload from the menu directly.
            Log.d("NFCString", "Shared Pref is null");
            preferencesController.setPreference("NFCString", "CI_" + preferencesController.getString("AndroidID") );
        }
        serviceIntent1.putExtra("NFCString", preferencesController.getString("NFCString"));
        startService(serviceIntent1);
    }

    //when the activity is resumed, start the NFC Reading service
    @Override
    protected void onResume(){
        super.onResume();
        Log.d("onResume", "onResume");
        refresh();
        Intent serviceIntent1 = new Intent(this, NFCHost.class);
        preferencesController.setPreference("NFCString", "CI_" + preferencesController.getString("AndroidID") );
        if ("null".equals(preferencesController.getString("NFCString") ) ){
            Log.d("NFCString", "Shared Pref is null");
            preferencesController.setPreference("NFCString", "CI_" + preferencesController.getString("AndroidID") );
        }
        Log.d("Status Main: ", preferencesController.getString("NFCString"));
        serviceIntent1.putExtra("NFCString", "CI_" + preferencesController.getString("AndroidID")); // to change
        startService(serviceIntent1);
    }


    //stop the service when the activity is paused or destroyed
    @Override
    protected void onPause(){
        super.onPause();
        Log.d("onPause", "onPause");
        tried_to_sign_in = true;
        Intent serviceIntent1 = new Intent(this, NFCHost.class);
        stopService(serviceIntent1);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d("onDestroy", "onDestroy");
        Intent serviceIntent1 = new Intent(this, NFCHost.class);
        stopService(serviceIntent1);
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

    void refresh() {
        final FirebaseDatabase database = com.google.firebase.database.FirebaseDatabase.getInstance();
        //TODO: Change path of database to student's ID, should be dynamic as it check if a student is currently logged in
        //write code that checks if a specific ID is marked as present = true. the path is /Presence/room/ID/present

        String id = preferencesController.getString("AndroidID");
        DatabaseReference ref = database.getReference("/PRESENCE"); //to be replaced with student
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean present = false;
                try {
                    for (DataSnapshot roomNumberSnapshot : dataSnapshot.getChildren()) { // loop through each room
                        for (DataSnapshot idSnapshot : roomNumberSnapshot.getChildren()) { // loop through each ID
                            //print all the values
                            Log.d("Room Number", roomNumberSnapshot.getKey());
                            Log.d("ID", idSnapshot.getKey());
                            for (DataSnapshot presentSnapshot : idSnapshot.getChildren()) { // loop through each present status
                                if (idSnapshot.getKey().equals(id) && presentSnapshot.getKey().equals("present") && presentSnapshot.getValue(Boolean.class) != null && presentSnapshot.getValue(Boolean.class)) {
                                    Log.d("Updating Present", presentSnapshot.getValue().toString());
                                    present = true;
                                }
                                Log.d("Present", presentSnapshot.getValue().toString());
                            }
                        }
                    }
                    Log.d("PresentFinal", String.valueOf(present));
                    //if present, make elements in xml visible

                    if (tried_to_sign_in) {
                        if (present) {
                            textViewSuccess.setVisibility(View.VISIBLE);
                            textViewSuccess.setText("You have successfully signed in!");
                            imageViewSuccess.setVisibility(View.VISIBLE);
                            textViewError.setVisibility(View.INVISIBLE);
                            imageViewError.setVisibility(View.INVISIBLE);
                        } else {
                            textViewSuccess.setVisibility(View.INVISIBLE);
                            imageViewSuccess.setVisibility(View.INVISIBLE);
                            textViewError.setVisibility(View.VISIBLE);
                            textViewError.setText("Something Came Up! Login Failed!");
                            imageViewError.setVisibility(View.VISIBLE);
                        }
                    }
                    else{
                        if (present){
                            textViewSuccess.setVisibility(View.VISIBLE);
                            textViewSuccess.setText("You are already Signed into this class!");
                            imageViewSuccess.setVisibility(View.VISIBLE);
                            textViewError.setVisibility(View.INVISIBLE);
                            imageViewError.setVisibility(View.INVISIBLE);
                        }
                        else{
                            textViewSuccess.setVisibility(View.INVISIBLE);
                            imageViewSuccess.setVisibility(View.INVISIBLE);
                            textViewError.setVisibility(View.INVISIBLE);
                            imageViewError.setVisibility(View.INVISIBLE);
                        }
                    }

                } catch (Exception e) {
                    Log.d("ERROR", e.toString());
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}