package com.example.classroomattendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.example.classroomattendancetracker.ClassDateItem;
import com.example.classroomattendancetracker.ClassDateItemAdapter;
import com.example.classroomattendancetracker.TeacherClassroomList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassAttendanceList extends AppCompatActivity {

    RecyclerView ClassAttendance_List;
    ArrayList<AttendedStudentItem> attendedStudentItem_Array = new ArrayList<AttendedStudentItem>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_attendance_list);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        String class_date = getIntent().getStringExtra("CLASS_DATE");
        Log.d("Classroom List 22", "onCreate: " + class_date);

        getAttendedStudentsService(class_date);

    }

    public void getAttendedStudentsService(String class_date){

        db.collection("COURSES")
                .whereEqualTo("OWNER", "teacher@test6.com")
                //.whereEqualTo("OWNER", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //String date = "";
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("Listing documents here", "onComplete: " + document.getId() + " " + document.getData());
                            if (document.getId().equals("COEN 212") && document != null ){
                                for (String key : document.getData().keySet()){
                                    Log.d("for 2 ", "onComplete: " + key + " " + document.getData().get(key));
                                }
                                Log.d("Testing classlist", "Passes first if");

                                Map<String, Object> documentData = document.getData();
                                Log.d("documentData", "onComplete: " + documentData);
                                Map<String,Object> PRESENT_MAP = (Map<String, Object>) documentData.get("PRESENT");
                                Log.d("PRESENT_MAP", "onComplete: " + PRESENT_MAP);

                                if (PRESENT_MAP == null){
                                    Toast.makeText(getApplicationContext(), "No dates exist for this section yet", Toast.LENGTH_SHORT).show();
                                }
                                else{

                                    Set<String> keys = PRESENT_MAP.keySet();
                                    for (String key : keys){
                                        String date = key.replace('_', '/');
                                        Log.d("Classroom Dates:", "String key: key " + date);
                                        if (date.equals(class_date)) {
                                            Log.d("date.equals()", "pass");
                                            ArrayList<String> students = (ArrayList<String>) documentData.get("students");
                                            Log.d("students", "onComplete: " + students);


                                            for (String student : students) {

                                                Log.d("current student", "onComplete: " + student);

                                                db.collection("USERS")
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                //String date = "";
                                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                                    Log.d("Listing USERS documents here", "onComplete: " + document.getId() + " " + document.getData());
                                                                    if (document.getId().equals("test@student30.com") && document != null ){
                                                                        attendedStudentItem_Array.add(new AttendedStudentItem
                                                                                (document.getString("first name") + " " + document.getString("last name"),
                                                                                        document.getString("Student ID"),
                                                                                        true));
                                                                    }
                                                                }
                                                                try{
//                            Log.d("Classroom date: ", attendedStudentItem_Array.get(0).getDate());
                                                                    Toast.makeText(getApplicationContext(), "Fetching Classes", Toast.LENGTH_SHORT).show();
                                                                    ClassAttendance_List = findViewById(R.id.recyclerView_AttendedStudentList);
                                                                    AttendedStudentItemAdapter adapter = new AttendedStudentItemAdapter(attendedStudentItem_Array);
                                                                    adapter.notifyDataSetChanged();
                                                                    ClassAttendance_List.setAdapter(adapter);
                                                                    ClassAttendance_List.setLayoutManager(new LinearLayoutManager(ClassAttendanceList.this));
                                                                }
                                                                catch (Exception e){
                                                                    Log.e("Classroom ", "onComplete: " + e.getMessage());
                                                                    Toast.makeText(getApplicationContext(), "No dates exist for this section yet", Toast.LENGTH_SHORT).show();
                                                                }

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
                                    }
                                }
                            }
                        }
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