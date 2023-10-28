package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Vibrator vibrator;
    Button loginButton, registerButton, scanButton, editButton;
    TextView app_title;

    PreferencesController preferencesController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferencesController = new PreferencesController(getApplicationContext());

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);
        app_title = (TextView) findViewById(R.id.app_title);
        scanButton = (Button) findViewById(R.id.scanNFCButton);
        editButton = (Button) findViewById(R.id.editPayloadButton);

        loginButton.setOnClickListener(Activity_Click_Listener);
        registerButton.setOnClickListener(Activity_Click_Listener);
        scanButton.setOnClickListener(Activity_Click_Listener);
        editButton.setOnClickListener(Activity_Click_Listener);

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
                case "scanNFC":
                    openNewActivity(ScanNFCActivitySignIn.class);
                    break;
                case "editPayload":
                    Log.d("editPayload", "HERE");
                    openNewActivity(EditNFCPayloadActivity.class);
                    break;

            }
        }
    };

    //on resume


    public void openNewActivity(Class activity){
        Intent intent = new Intent(getApplicationContext(), activity);
        startActivity(intent);
    }

}