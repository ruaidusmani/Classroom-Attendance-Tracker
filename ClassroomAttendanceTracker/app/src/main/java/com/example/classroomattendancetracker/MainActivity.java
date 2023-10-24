package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openNewActivity(LoginActivity.class);
    }

    public void openNewActivity(Class activity){

        Intent intent = new Intent(getApplicationContext(), activity);
        startActivity(intent);
    }
}