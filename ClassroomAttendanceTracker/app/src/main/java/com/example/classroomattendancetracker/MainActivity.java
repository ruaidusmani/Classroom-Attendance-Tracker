package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Vibrator vibrator;
    Button loginButton, registerButton;
    TextView app_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);
        app_title = (TextView) findViewById(R.id.app_title);

        loginButton.setOnClickListener(Activity_Click_Listener);
        registerButton.setOnClickListener(Activity_Click_Listener);
    }

    View.OnClickListener Activity_Click_Listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vibrator.vibrate(25);
            String tag = (String) v.getTag(); // tags for buttons are in their .xml
            switch(tag){
                case "login":
                    openNewActivity(LoginActivity.class);
                    break;
                case "register":
                    openNewActivity(RegisterActivity.class);
                    break;
            }
        }
    };

    public void openNewActivity(Class activity){
        Intent intent = new Intent(getApplicationContext(), activity);
        startActivity(intent);
    }
}