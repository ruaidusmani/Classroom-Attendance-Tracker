package com.example.classroomattendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Vibrator vibrator;
    Button loginButton, registerButton, buttonCheckIn, editButton, logoutButton, buttonCheckOut, buttonEnroll;
    TextView app_title;

    PreferencesController preferencesController;

    FirebaseUser user;
    FirebaseAuth mAuth;
    FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

//        Intent a = new Intent(getApplicationContext(), EnrollClassActivity.class);
//        startActivity(a);

        preferencesController = new PreferencesController(getApplicationContext());
        String android_id = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
        Log.d("Android ID", android_id);
        preferencesController.setPreference("AndroidID", android_id);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);
        app_title = (TextView) findViewById(R.id.app_title);
        buttonCheckIn = (Button) findViewById(R.id.buttonCheckIn);
        buttonCheckOut = (Button) findViewById(R.id.buttonCheckOut);
        editButton = (Button) findViewById(R.id.editPayloadButton);
        logoutButton = (Button) findViewById(R.id.logoutButtonMain);
        buttonEnroll = (Button) findViewById(R.id.buttonEnroll);


        loginButton.setOnClickListener(Activity_Click_Listener);
        registerButton.setOnClickListener(Activity_Click_Listener);
        buttonCheckIn.setOnClickListener(Activity_Click_Listener);
        buttonCheckOut.setOnClickListener(Activity_Click_Listener);
        editButton.setOnClickListener(Activity_Click_Listener);
        logoutButton.setOnClickListener(Activity_Click_Listener);
        buttonEnroll.setOnClickListener(Activity_Click_Listener);
        user = mAuth.getCurrentUser();


        handleUserType();




        //check for intent
        Intent intent = getIntent();
        if(intent.hasExtra("Error Code")){
            int message = intent.getIntExtra("Error Code", -1);
            Log.d("Error Code", String.valueOf(message));
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You cannot scan your Phone right now, You must select a login option first!").setTitle("Cannot Scan NFC");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    void handleUserType(){
        String USER_TYPE = preferencesController.getString("USER_TYPE");
        if(user != null)
        {
            if(USER_TYPE.equals("Teacher")){

                openNewActivity(TeacherHomepage.class);
            }
            loginButton.setVisibility(View.GONE);
            registerButton.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.VISIBLE);
            buttonCheckIn.setVisibility(View.VISIBLE);
            buttonCheckOut.setVisibility(View.VISIBLE);
            buttonEnroll.setVisibility(View.VISIBLE);
//            Toast.makeText(getApplicationContext(), "ALREADY LOGGED IN :)", Toast.LENGTH_LONG).show();
        }else{

            loginButton.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
            buttonCheckIn.setVisibility(View.GONE);
            buttonCheckOut.setVisibility(View.GONE);
            buttonEnroll.setVisibility(View.GONE);
//            Toast.makeText(getApplicationContext(), "NOT LOGGED IN :(", Toast.LENGTH_LONG).show();
        }
    }

    View.OnClickListener Activity_Click_Listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vibrator.vibrate(25);
            String tag = (String) v.getTag(); // tags for buttons are in their .xml
            Log.d("tag", tag);
            switch(tag){
                case "login":
                    openNewActivity(LoginActivity.class);
                    break;
                case "register":
                    openNewActivity(RegisterActivity.class);
                    break;
                case "checkIn":
                    openNewActivity(ScanNFCActivityCheckIn.class);
                    break;
                case "checkOut":
                    openNewActivity(ScanNFCActivityCheckOut.class);
                    break;
                case "editPayload":
                    Log.d("editPayload", "HERE");
                    openNewActivity(EditNFCPayloadActivity.class);
                    break;
                case "logout" :
                    loginButton.setVisibility(View.VISIBLE);
                    registerButton.setVisibility(View.VISIBLE);
                    logoutButton.setVisibility(View.GONE);
                    editButton.setVisibility(View.GONE);
                    buttonCheckIn.setVisibility(View.GONE);
                    buttonCheckOut.setVisibility(View.GONE);
                    buttonEnroll.setVisibility(View.GONE);
                    FirebaseAuth.getInstance().signOut();
                    preferencesController.setPreference("USER_TYPE", "");
                    break;
                case "enroll":
                    openNewActivity(EnrollClassActivity.class);
                    break;
            }
        }
    };
    //on resume

    
    public void openNewActivity(Class activity){
        Intent intent = new Intent(getApplicationContext(), activity);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleUserType();
    }

}
