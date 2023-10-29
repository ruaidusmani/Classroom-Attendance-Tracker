package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.EnumMap;

public class RegisterActivity extends AppCompatActivity {
    Vibrator vibrator;
    Button registerButton;
    TextView student_ID_TextView, password_TextView;
    EditText student_ID_EditText, password_EditText, confirm_password_EditText;

    FirebaseUser user;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        registerButton = (Button) findViewById(R.id.registerButton);
        student_ID_TextView = (TextView) findViewById(R.id.student_email_TextView);
        password_TextView = (TextView) findViewById(R.id.password_TextView);
        student_ID_EditText = (EditText) findViewById(R.id.student_email_EditText);
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

        mAuth = FirebaseAuth.getInstance();



        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(50);

                String email =  ((EditText) findViewById(R.id.student_email_EditText)).getText().toString();
                String password = ((EditText) findViewById(R.id.password_EditText)).getText().toString();

                Log.d("EMAIL PASSING", email);
                Log.d("PASSWORD PASSING", password);



                registerService(email, password);

            }
        });
    }
 public void registerService(String email, String password){
     mAuth.createUserWithEmailAndPassword(email, password)
             .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(@NonNull Task<AuthResult> task) {
                     if (task.isSuccessful()) {
                         // Sign in success, update UI with the signed-in user's information
                         Log.d("FIREBASE_AUTH_REGISTER", "createUserWithEmail:success");
                         user = mAuth.getCurrentUser();
                         Toast.makeText(RegisterActivity.this, "Registration Succcess",
                                 Toast.LENGTH_SHORT).show();
                         startActivity(new Intent(getApplicationContext(), MainActivity.class));
                     } else {
                         // If sign in fails, display a message to the user.
                         Log.w("FIREBASE_ATUH_REGISTER", "createUserWithEmail:failure", task.getException());

                         //TODO : ADD REASON FOR FAILURE
                         Toast.makeText(RegisterActivity.this, "Registration Failed",
                                 Toast.LENGTH_SHORT).show();
                     }
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