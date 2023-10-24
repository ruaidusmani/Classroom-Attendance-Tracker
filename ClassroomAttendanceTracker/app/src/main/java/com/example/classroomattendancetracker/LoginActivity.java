package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    Button loginButton;
    TextView student_ID_TextView, password_TextView;
    EditText student_ID_EditText, password_EditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.loginButton);
        student_ID_TextView = (TextView) findViewById(R.id.student_ID_TextView);
        password_TextView = (TextView) findViewById(R.id.password_TextView);
        student_ID_EditText = (EditText) findViewById(R.id.student_ID_EditText);
        password_EditText = (EditText) findViewById(R.id.password_EditText);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


    }


}