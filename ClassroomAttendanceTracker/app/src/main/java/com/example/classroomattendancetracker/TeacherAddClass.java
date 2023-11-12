package com.example.classroomattendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
    }

    View.OnClickListener ActivityClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.submit_class_teacher){
                addClassService();
            }
        }
    };

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
                .document(class_title.getText().toString() + "-"+user.getUid())
                .set(class_information)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Class Added :) " , Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), TeacherHomepage.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Class Failed :( " , Toast.LENGTH_SHORT).show();
                    }
                });
    }

}