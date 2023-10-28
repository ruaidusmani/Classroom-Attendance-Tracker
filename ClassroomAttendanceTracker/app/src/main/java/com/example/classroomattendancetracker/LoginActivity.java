package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    Vibrator vibrator;

    Button loginButton;
    TextView student_ID_TextView, password_TextView, register_prompt_TextView;
    EditText student_ID_EditText, password_EditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        loginButton = (Button) findViewById(R.id.loginButton);
        student_ID_TextView = (TextView) findViewById(R.id.student_ID_TextView);
        password_TextView = (TextView) findViewById(R.id.password_TextView);
        register_prompt_TextView = (TextView) findViewById(R.id.register_prompt_TextView);
        student_ID_EditText = (EditText) findViewById(R.id.student_ID_EditText);
        password_EditText = (EditText) findViewById(R.id.password_EditText);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(50);
            }
        });

        register_prompt_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(50);
                openNewActivity(RegisterActivity.class);
            }
        });
    }

    public void openNewActivity(Class activity){
        Intent intent = new Intent(getApplicationContext(), activity);
        startActivity(intent);
    }
}