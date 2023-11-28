package com.example.classroomattendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class TeacherClassroomList extends AppCompatActivity implements ClassDateItemAdapter.OnItemClickListener{

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;



    Button download_csv;
    Vibrator vibrator;
    RecyclerView ClassDate_List;
    ArrayList<ClassDateItem> classDateItem_Array = new ArrayList<ClassDateItem>(); // holds class date to list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_classroom);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        download_csv = (Button) findViewById(R.id.download_csv);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // get Intent
        String class_name = getIntent().getStringExtra("CLASS_NAME");
        Log.d("Classroom List", "onCreate: " + class_name);

        getClassRoomService(class_name);

        download_csv.setOnClickListener(button_listener);
    }
        View.OnClickListener button_listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == download_csv.getId())
                {
                    Intent intent = new Intent(getApplicationContext(), DownloadCSVActivity.class);
                    String class_name = getIntent().getStringExtra("CLASS_NAME");
                    intent.putExtra("CLASS_NAME", class_name);
                    startActivity(intent);
                }
            }
        };


    public void getClassRoomService(String class_name){

        db.collection("COURSES")
                .whereEqualTo("OWNER", user.getEmail())
                //.whereEqualTo("OWNER", "teacher@test6.com")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //String date = "";
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document != null) {
                                if (document.getId().equals(class_name)) {
                                    for (String key : document.getData().keySet()) {
                                        Log.d("for 2 ", "onComplete: " + key + " " + document.getData().get(key));
                                    }
                                    Log.d("Test", "Passes first if");

                                    Map<String, Object> documentData = document.getData();
                                    Map<String, Object> PRESENT_MAP = (Map<String, Object>) documentData.get("PRESENT");
                                    if (PRESENT_MAP == null) {
                                        Toast.makeText(getApplicationContext(), "No dates exist for this section yet", Toast.LENGTH_SHORT).show();
                                    } else {

                                        Set<String> keys = PRESENT_MAP.keySet();
                                        for (String key : keys) {
                                            String date = key.replace('_', '/');
                                            classDateItem_Array.add(new ClassDateItem(date));
                                        }
                                        for (int i = 0; i < classDateItem_Array.size(); i++) {
                                            Log.d("Classroom ", "onComplete: " + classDateItem_Array.get(i).getDate());
                                            //                                        Log.d("Map ", "onComplete: " + PRESENT_MAP.size() + " " + PRESENT_MAP.get("11_11_2023"));
                                        }
                                    }
                                }
                            }
                        }
                        try{
                            Log.d("Classroom date: ", classDateItem_Array.get(0).getDate());
                            Toast.makeText(getApplicationContext(), "Fetching Classes", Toast.LENGTH_SHORT).show();
                            ClassDate_List = findViewById(R.id.recyclerView_ClassRoomItems);
                            ClassDateItemAdapter adapter = new ClassDateItemAdapter(classDateItem_Array, TeacherClassroomList.this);
                            adapter.notifyDataSetChanged();
                            ClassDate_List.setAdapter(adapter);
                            ClassDate_List.setLayoutManager(new LinearLayoutManager(TeacherClassroomList.this));
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

    @Override
    public void onItemClick(int position) {
        vibrator.vibrate(50);
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), ClassAttendanceList.class);
        intent.putExtra("CLASS_NAME", getIntent().getStringExtra("CLASS_NAME"));
        intent.putExtra("CLASS_DATE", classDateItem_Array.get(position).getDate());
        startActivity(intent);
    }
}
