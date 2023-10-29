package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
// ...
// Initialize Firebase Auth



    Vibrator vibrator;

    Button loginButton;
    TextView student_ID_TextView, password_TextView, register_prompt_TextView;
    EditText student_ID_EditText, password_EditText;

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Vibration object to give vibrate effect to buttons
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Initializing Buttons, TextViews and EditTexts
        loginButton = (Button) findViewById(R.id.loginButton);

        student_ID_TextView = (TextView) findViewById(R.id.student_email_TextView);
        password_TextView = (TextView) findViewById(R.id.password_TextView);
        register_prompt_TextView = (TextView) findViewById(R.id.register_prompt_TextView);

        student_ID_EditText = (EditText) findViewById(R.id.student_email_EditText);
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

                    String password = ((EditText) findViewById(R.id.password_EditText)).getText().toString();
                    String email = ((EditText)findViewById(R.id.student_email_EditText)).getText().toString();

                    Log.d("PASSING EMAIL ", email);
                    Log.d("PASSING password", password);
                    loginService(email, password);

                    break;
                case "register":
                    openNewActivity(RegisterActivity.class);
                    break;
            }
        }
    };

    public void loginService(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("FIREBASE_AUTH_LOGIN", "signInWithEmail:success");
                            Toast.makeText(LoginActivity.this, "Login Success",
                                    Toast.LENGTH_SHORT).show();
                            user = mAuth.getCurrentUser();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FIREBASE_AUTH_LOGIN", "signInWithEmail:failure", task.getException());
                            //TODO: ADD LOGIN FAIL REASON
                            Toast.makeText(LoginActivity.this, "Login Failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

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