package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.text.Regex;

public class RegisterActivity extends AppCompatActivity {
    Vibrator vibrator;
    Button registerButton;
    EditText email_EditText, student_ID_EditText, password_EditText, confirm_password_EditText, last_name_EditText, first_name_EditText;
    Button user_type;
    FirebaseUser user;
    FirebaseAuth mAuth;


    boolean user_type_toggle = true;

    FirebaseFirestore db;

    PreferencesController preferencesController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        preferencesController = new PreferencesController(getApplicationContext());

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        registerButton = (Button) findViewById(R.id.registerButton);
        email_EditText= (EditText) findViewById(R.id.student_email_EditText);
        password_EditText = (EditText) findViewById(R.id.password_EditText);
        confirm_password_EditText = (EditText) findViewById(R.id.confirm_password_EditText);
        user_type= (Button) findViewById(R.id.user_type_switch);
        last_name_EditText= (EditText) findViewById(R.id.editTextLastName);
        first_name_EditText= (EditText) findViewById(R.id.editTextFirstName);
        student_ID_EditText = findViewById(R.id.editTextStudentID);

        //Toolbar
        Toolbar register_toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(register_toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Registration");

        //Toolbar items
        register_toolbar.showOverflowMenu();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        // Firebase variable init
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(50);

                String email =  ((EditText) findViewById(R.id.student_email_EditText)).getText().toString();
                String password = ((EditText) findViewById(R.id.password_EditText)).getText().toString();

                if(!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    Log.d("EMAIL PASSING", email);
                }else{
                    Log.d("EMAIL FAILED  : does not match email pattern", email);
                    Toast.makeText(getApplicationContext(), "Email is not a valid format", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!password.isEmpty() && password.length() >=6 )
                {
                    Log.d("PASSWORD PASSING", password);
                    if(!passwordRegexValidation(password)){
                        Log.d("PASSWORD REGEX FAILED " ,  " doesn't meet requirements");
                        Toast.makeText(getApplicationContext(), "Password format invalid ", Toast.LENGTH_LONG).show();
                        //Removed for testing purposes.
                        //return;
                    } else {
                        Log.d("PASSWORD PASSING", password);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Password must be 6 characters long", Toast.LENGTH_LONG).show();
                    return;
                }
                registerService(email, password);
            }
        });

        user_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(user_type_toggle) {
                    user_type.setText("Teacher");
                    student_ID_EditText.setVisibility(View.GONE);
                }else {
                    user_type.setText("Student");
                    student_ID_EditText.setVisibility(View.VISIBLE);
                }
                user_type_toggle = !user_type_toggle;

            }
        });
    }

public boolean passwordRegexValidation(String password ){

    Pattern pattern;
    Matcher matcher;

    final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";

    pattern = Pattern.compile(PASSWORD_PATTERN);
    matcher = pattern.matcher(password);

    return matcher.matches();
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
                         initialiseFirestoreDocument(user, user_type.getText().toString());
                     } else {
                         // If sign in fails, display a message to the user.
                         Log.w("FIREBASE_AUTH_REGISTER", "createUserWithEmail:failure", task.getException());

                         //TODO : ADD REASON FOR FAILURE
                         Toast.makeText(RegisterActivity.this, "Registration Failed",
                                 Toast.LENGTH_SHORT).show();
                     }
                 }
             });
 }


 public void initialiseFirestoreDocument(FirebaseUser user, String type){
        Map<String, Object> profile = new HashMap<>();
        assert(user.getEmail() != null);
        String email = user.getEmail();
        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        profile.put("android_id", android_id);
        profile.put("user_type", type);
        profile.put("last name" , last_name_EditText.getText().toString());
     profile.put("first name" , first_name_EditText.getText().toString());
     if(type.equals("Student")) {
         profile.put("Student ID", student_ID_EditText.getText().toString());
     }
        db.collection("USERS")
                .document(email)
                .set(profile)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(), "Registration & Profile created", Toast.LENGTH_LONG).show();
                        preferencesController.setPreference("USER_TYPE", type);
                        if(user.equals("Teacher")){
                            startActivity(new Intent(getApplicationContext(), TeacherHomepage.class));
                        }else {

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e) {

                        Toast.makeText(getApplicationContext(), "Profile creation fail", Toast.LENGTH_LONG).show();
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