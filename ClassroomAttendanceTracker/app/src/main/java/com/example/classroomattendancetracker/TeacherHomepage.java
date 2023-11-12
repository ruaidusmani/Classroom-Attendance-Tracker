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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TeacherHomepage extends AppCompatActivity implements ClassItemAdapter.OnItemClickListener {


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    Button add_class;
    Button logout;
    Button buttonViewDashboard;
    Vibrator vibrator;


    RecyclerView ClassItemList;
    ArrayList<ClassItem> ClassItem_Array = new ArrayList<ClassItem>(); // holds student profiles to list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_homepage);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

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
//        display_items();
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
                .whereArrayContains("DAYS", "Monday")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        ArrayList<String > class_names = new ArrayList<>();
                        ArrayList<String > class_room_numbers = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("FIREQUERY ", document.getId() + " => " + document.getData());
                                ClassItem_Array.add(new ClassItem(document.getId(), (String) document.get("ROOM_NUMBER")));
//                                class_names.add(document.getId());
//                                class_room_numbers.add((String) document.get("ROOM_NUMBER"));
                            }
                            Log.d("FIREQUERY ", "onComplete: " + ClassItem_Array.size());

                            for (int i = 0; i < ClassItem_Array.size(); i++) {
                                Log.d("FIREQUERY ", "oldonComplete: " + ClassItem_Array.get(i).getClass_name());
                            }



                        Toast.makeText(getApplicationContext(), "Fetching Classes", Toast.LENGTH_SHORT).show();

//                        dayOfWeekMap = new HashMap<>();
//
//                        // Add key-value pairs to the dictionary
//                        dayOfWeekMap.put(1, "Sunday");
//                        dayOfWeekMap.put(2, "Monday");
//                        dayOfWeekMap.put(3, "Tuesday");
//                        dayOfWeekMap.put(4, "Wednesday");
//                        dayOfWeekMap.put(5, "Thursday");
//                        dayOfWeekMap.put(6, "Friday");
//                        dayOfWeekMap.put(7, "Saturday");
//
//
//                        Calendar currentTime = Calendar.getInstance();
//                        int current_hour = (currentTime.get(Calendar.HOUR_OF_DAY));
//                        int current_minute = (currentTime.get(Calendar.MINUTE));
//                        int current_day_of_week = currentTime.get(Calendar.DAY_OF_WEEK);
//                        String current_day_of_week_string = dayOfWeekMap.get(current_day_of_week);
//
//                        current_day_of_week = 2;
//                        current_day_of_week_string = dayOfWeekMap.get(current_day_of_week);


                        Log.d("FIREQUERY ", "display_items: " + ClassItem_Array.size());
                        for (int i = 0; i < ClassItem_Array.size(); i++) {
                            Log.d("FIREQUERY ", "onComplete: " + ClassItem_Array.get(i).getClass_name());
                        }
                        // populate the list
                        ClassItemList = findViewById(R.id.recyclerView_ClassItems);
                        ClassItemAdapter adapter = new ClassItemAdapter(ClassItem_Array, TeacherHomepage.this);
                        adapter.notifyDataSetChanged(); // if toggle is set
                        ClassItemList.setAdapter(adapter);
                        ClassItemList.setLayoutManager(new LinearLayoutManager(TeacherHomepage.this));



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

    // displays the list of profiles
   // public void display_items(){}

    @Override
    public void onItemClick(int position) {
        vibrator.vibrate(50);
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), TeacherClassroomList.class);
        intent.putExtra("CLASS_NAME", ClassItem_Array.get(position).getClass_name());
        startActivity(intent);
    }
}