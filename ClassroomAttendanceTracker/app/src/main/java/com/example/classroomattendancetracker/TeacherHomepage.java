package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;

public class TeacherHomepage extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    Button add_class;
    Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_homepage);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        add_class = findViewById(R.id.add_class_teacher_button);
        add_class.setOnClickListener(Activity_Click_Listener);
        logout = findViewById(R.id.logout_teacher_button);
        logout.setOnClickListener(Activity_Click_Listener);
    }

    View.OnClickListener Activity_Click_Listener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if(v.getId() == R.id.add_class_teacher_button){
                startActivity(new Intent(getApplicationContext(), TeacherAddClass.class));
            }else if(v.getId() == R.id.logout_teacher_button){
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }



        }
    };

}