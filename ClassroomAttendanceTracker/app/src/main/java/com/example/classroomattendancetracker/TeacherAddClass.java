package com.example.classroomattendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherAddClass extends AppCompatActivity {
    Button monday_chip;
    Button tuesday_chip;
    Button wednesday_chip;
    Button thursday_chip;
    Button friday_chip;

    TimePicker time_picker_start;
    TimePicker time_picker_end;

    EditText class_title;
    EditText room_number;

    Button submit_class;

    ChipGroup chipGroup;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    boolean classValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_add_class);


        //Firebase init variables
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        monday_chip     = findViewById(R.id.monday_chip);
        tuesday_chip    = findViewById(R.id.tuesday_chip);
        wednesday_chip  = findViewById(R.id.wednesday_chip);
        thursday_chip= findViewById(R.id.thursday_chip);
        friday_chip     = findViewById(R.id.friday_chip);
        time_picker_start = findViewById(R.id.time_picker_start);
        time_picker_end= findViewById(R.id.time_picker_end);
        submit_class = findViewById(R.id.submit_class_teacher);
        class_title = findViewById(R.id.class_name_add_teacher);
        chipGroup = findViewById(R.id.chip_days_group);
        room_number = findViewById(R.id.room_number);


        //Adding Listeners
        submit_class.setOnClickListener(ActivityClickListener);
        Toolbar toolbar;
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Add Course ");

        toolbar.showOverflowMenu();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


    }

    View.OnClickListener ActivityClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.submit_class_teacher){

                if(validateInputs()){
                    validateClass();
                }
            }
        }
    };

    public boolean validateInputs(){
        if (class_title.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Please enter a class name" , Toast.LENGTH_SHORT).show();
            return false;
        }
        if (room_number.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Please enter a room number" , Toast.LENGTH_SHORT).show();
            return false;
        }
        if (chipGroup.getCheckedChipIds().size() == 0){
            Toast.makeText(getApplicationContext(), "Please select at least one day" , Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void validateClass(){
        //make sure that the class does not conflict with the time of another one
        db = FirebaseFirestore.getInstance();

        // Reference to your collection
        CollectionReference collectionReference = db.collection("COURSES");


        ArrayList<String> days_of_the_week_selected = new ArrayList<>();
        for (Integer id : chipGroup.getCheckedChipIds()){
            Chip chip = chipGroup.findViewById(id);
            days_of_the_week_selected.add(chip.getText().toString());
        }
        String selectedRoom = room_number.getText().toString();

        // Retrieve all documents in the collection
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                classValid = true;
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        // Access each document here
                        Log.d("Class", document.getId() + " => " + document.getData());
                        Map<String, Object> documentDict = document.getData();
                        if (selectedRoom != null && selectedRoom.equals(documentDict.get("ROOM_NUMBER"))){
                            Log.d("Class", "Room Number is the same");
                            //check if the days are the same
                            ArrayList<String> days_of_the_week = (ArrayList<String>) documentDict.get("DAYS");
                            for (String day : days_of_the_week_selected){
                                if (days_of_the_week.contains(day)){
//                                    Log.d("Class", "Looking at day: " + day);
//                                    Log.d("Class", d)
                                    Log.d("Class", "Days are the same");
                                    //check if the time is the same
                                    long start_hour_long = (long) documentDict.get("START_HOUR");
                                    int start_hour = Long.valueOf(start_hour_long).intValue();

                                    long start_min_long = (long) documentDict.get("START_MIN");
                                    int start_min = Long.valueOf(start_min_long).intValue();


                                    long end_hour_long = (long) documentDict.get("END_HOUR");
                                    int end_hour = Long.valueOf(end_hour_long).intValue();

                                    long end_min_long = (long) documentDict.get("END_MIN");
                                    int end_min = Long.valueOf(end_min_long).intValue();


                                    int start_hour_selected = time_picker_start.getHour();
                                    int start_min_selected = time_picker_start.getMinute();

                                    int end_hour_selected = time_picker_end.getHour();
                                    int end_min_selected = time_picker_end.getMinute();

                                    int start_sec = start_hour * 60 * 60 + start_min * 60;
                                    int end_sec = end_hour * 60 * 60 + end_min * 60;
                                    int start_sec_selected = start_hour_selected * 60 * 60 + start_min_selected * 60;
                                    int end_sec_selected = end_hour_selected * 60 * 60 + end_min_selected * 60;

                                    if (start_sec_selected >= start_sec && start_sec_selected <= end_sec){
                                        Log.d("Class", "Start hour is the same");
                                        Toast.makeText(getApplicationContext(), "Cannot add this class, as it conflicts with another class at the same time" , Toast.LENGTH_SHORT).show();
                                        classValid = false;
                                        return;
                                    }
                                    else if (end_sec_selected >= start_sec && end_sec_selected <= end_sec){
                                        Log.d("Class", "End hour is the same");
                                        Toast.makeText(getApplicationContext(), "Cannot add this class, as it conflicts with another class at the same time" , Toast.LENGTH_SHORT).show();
                                        classValid = false;
                                        return;
                                    }

                                }
                            }
                        }

                    }
                    if (classValid){
                        addClassService();
                    }
                } else {
                    Log.w("Class", "Error getting documents.", task.getException());
                }
            }
        });
    }

    public void addClassService(){

        Map<String,Object> class_information = new HashMap<>();
        String user_email = user.getEmail();

        List<Integer> ids = chipGroup.getCheckedChipIds();

        ArrayList<String> days_of_the_week = new ArrayList<>();
        for(Integer id:ids){
            Chip chip = chipGroup.findViewById(id);
               days_of_the_week.add(chip.getText().toString());
        }

        if(user_email == null){
            return;
        }

        class_information.put("DAYS", days_of_the_week);
        class_information.put("OWNER", user_email );
        int hour_start = time_picker_start.getHour();
        int min_start = time_picker_start.getMinute();

        int hour_end = time_picker_end.getHour();
        int min_end = time_picker_end.getMinute();

        class_information.put("START_HOUR", hour_start);
        class_information.put("START_MIN", min_start);

        class_information.put("END_HOUR", hour_end);
        class_information.put("END_MIN", min_end);

        class_information.put("ROOM_NUMBER", room_number.getText().toString());


        db.collection("COURSES")
                .document(class_title.getText().toString())
                .set(class_information)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Class Sucessfully Added !" , Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), TeacherHomepage.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to add Class " , Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        //Back button
        if (id == android.R.id.home){
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}