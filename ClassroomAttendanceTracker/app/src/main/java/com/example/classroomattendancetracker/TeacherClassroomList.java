package com.example.classroomattendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TeacherClassroomList extends AppCompatActivity implements ClassRoomItemAdapter.OnItemClickListener{

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    Vibrator vibrator;


    RecyclerView ClassRoom_List;
    ArrayList<ClassRoomItem> ClassRoomItem_Array = new ArrayList<ClassRoomItem>(); // holds student profiles to list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_classroom);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // get Intent
        String class_name = getIntent().getStringExtra("CLASS_NAME");
        Log.d("Classroom ", "onCreate: " + class_name);

        getClassRoomService(class_name);
    }


    public void getClassRoomService(String class_name){

        db.collection("COURSES")
                .whereEqualTo("OWNER", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //String date = "";
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            if (document.getId().equals("test2")){
                                for (String key : document.getData().keySet()){
                                    Log.d("for 2 ", "onComplete: " + key + " " + document.getData().get(key));
                                }
                                Log.d("Test", "Passes first if");

                                Map<String, Object> documentData = document.getData();
                                Map<String,Object> PRESENT_MAP = (Map<String, Object>) documentData.get("PRESENT");
                                Set<String> keys = PRESENT_MAP.keySet();
                                for (String key : keys){
                                    String date = key.replace('_', '/');
                                    ClassRoomItem_Array.add(new ClassRoomItem(date));
                                }
                                for (int i = 0; i < ClassRoomItem_Array.size(); i++){
                                    Log.d("Classroom ", "onComplete: " + ClassRoomItem_Array.get(i).getDay_date());
                                }

                                Log.d("Map ", "onComplete: " + PRESENT_MAP.size() + " " + PRESENT_MAP.get("11_11_2023"));
                            }

//
//                            String result_classname = (String) document.getId();
//                            if (result_classname.equals(class_name)) {
//
//                                HashMap<String, ArrayList<String>> gypsy = new HashMap<String, ArrayList<String>>();
//                                gypsy = document.get("PRESENT", gypsy.getClass());
//                                gypsy.get("DATE");
//
//
//                                // ClassRoomItem
//                                ClassRoomItem_Array.add(new ClassRoomItem(document.getId(), (String) document.get("ROOM_NUMBER")));
//                            }

                        }
//                        Log.d("FIREQUERY ", "onComplete: " + ClassRoomItem_Array.size());


                        Log.d("Classroom date: ", ClassRoomItem_Array.get(0).getDay_date());
                        Toast.makeText(getApplicationContext(), "Fetching Classes", Toast.LENGTH_SHORT).show();

                        ClassRoom_List = findViewById(R.id.recyclerView_ClassRoomItems);
                        ClassRoomItemAdapter adapter = new ClassRoomItemAdapter(ClassRoomItem_Array, TeacherClassroomList.this);
                        adapter.notifyDataSetChanged(); // if toggle is set
                        ClassRoom_List.setAdapter(adapter);
                        ClassRoom_List.setLayoutManager(new LinearLayoutManager(TeacherClassroomList.this));
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
    }
}
