package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    Vibrator vibrator;

    Button loginButton;
    TextView student_ID_TextView, password_TextView, register_prompt_TextView;
    EditText student_ID_EditText, password_EditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Vibration object to give vibrate effect to buttons
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Initializing Buttons, TextViews and EditTexts
        loginButton = (Button) findViewById(R.id.loginButton);

        student_ID_TextView = (TextView) findViewById(R.id.student_ID_TextView);
        password_TextView = (TextView) findViewById(R.id.password_TextView);
        register_prompt_TextView = (TextView) findViewById(R.id.register_prompt_TextView);

        student_ID_EditText = (EditText) findViewById(R.id.student_ID_EditText);
        password_EditText = (EditText) findViewById(R.id.password_EditText);

        //Toolbar
        Toolbar login_toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(login_toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Login");

        //Toolbar items
        login_toolbar.showOverflowMenu();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        loginButton.setOnClickListener(Activity_Click_Listener);
        register_prompt_TextView.setOnClickListener(Activity_Click_Listener);
    }

    View.OnClickListener Activity_Click_Listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vibrator.vibrate(25);
            String tag = (String) v.getTag(); // tags for buttons are in their .xml
            switch(tag){
                case "login":
                    openNewActivity(MainActivity.class);
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
}