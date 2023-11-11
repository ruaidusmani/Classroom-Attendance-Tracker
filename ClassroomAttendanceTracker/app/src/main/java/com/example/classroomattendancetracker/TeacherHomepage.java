package com.example.classroomattendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class TeacherHomepage extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    Button add_class;
    Button logout;
    Button buttonViewDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_homepage);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        add_class = findViewById(R.id.add_class_teacher_button);
        buttonViewDashboard = findViewById(R.id.buttonViewDashboard);
        add_class.setOnClickListener(Activity_Click_Listener);
        buttonViewDashboard.setOnClickListener(Activity_Click_Listener);
        logout = findViewById(R.id.logout_teacher_button);
        logout.setOnClickListener(Activity_Click_Listener);

        getClassesService();
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
            else if (v.getId() == R.id.buttonViewDashboard){
                startActivity(new Intent(getApplicationContext(), LiveStatsActivity.class));
            }
        }
    };

    public void getClassesService(){

        db.collection("COURSES")
                .whereEqualTo("OWNER", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        ArrayList<String > class_names = new ArrayList<>();
                        ArrayList<String > class_room_numbers = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("FIREQUERY ", document.getId() + " => " + document.getData());
                                class_names.add(document.getId());
                                class_room_numbers.add((String) document.get("ROOM_NUMBER"));
                            }



                        Toast.makeText(getApplicationContext(), "Fetching Classes", Toast.LENGTH_SHORT).show();
                            //From here we can access the room names and ids. Only inside this bloc of code.
                        // SO i think we have to update the views from here.
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Could not fetch classes", Toast.LENGTH_SHORT).show();
                    }
                });

    }



}