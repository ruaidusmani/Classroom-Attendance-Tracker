package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Vibrator;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {
    Vibrator vibrator;
    Button registerButton;
    TextView student_ID_TextView, password_TextView;
    EditText student_ID_EditText, password_EditText, confirm_password_EditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        registerButton = (Button) findViewById(R.id.registerButton);
        student_ID_TextView = (TextView) findViewById(R.id.student_ID_TextView);
        password_TextView = (TextView) findViewById(R.id.password_TextView);
        student_ID_EditText = (EditText) findViewById(R.id.student_ID_EditText);
        password_EditText = (EditText) findViewById(R.id.password_EditText);
        confirm_password_EditText = (EditText) findViewById(R.id.confirm_password_EditText);


        //Toolbar
        Toolbar register_toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(register_toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Registration");

        //Toolbar items
        register_toolbar.showOverflowMenu();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(50);
            }
        });
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