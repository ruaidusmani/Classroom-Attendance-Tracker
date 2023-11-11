package com.example.classroomattendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveStatsActivity extends AppCompatActivity {

    TextView cardViewTimeRemainingNum;
    TextView textViewLastStudentJoinedName;
    TextView textViewLastStudentJoinedTime;
    TextView textViewLastStudentJoinedID;
    TextView textViewLiveCountNum;
    TextView textViewLiveCountCurrentTimeNum;
    TextView textViewCourseName;
    TextView textViewRoomNumber;

    Thread timeThread;

    String Room = "H907";
    PreferencesController preferencesController;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String email;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser user;
    String courseName;

    int currentCountStudents = 0;
    Map<Integer, String> dayOfWeekMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_stats);
        preferencesController = new PreferencesController(getApplicationContext());

        cardViewTimeRemainingNum = findViewById(R.id.cardViewTimeRemainingNum);
        textViewLastStudentJoinedName = findViewById(R.id.textViewLastStudentJoinedName);
        textViewLastStudentJoinedTime = findViewById(R.id.textViewLastStudentJoinedTime);
        textViewLastStudentJoinedID = findViewById(R.id.textViewLastStudentJoinedID);
        textViewLiveCountNum = findViewById(R.id.textViewLiveCountNum);
        textViewLiveCountCurrentTimeNum = findViewById(R.id.textViewLiveCountCurrentTimeNum);
        textViewCourseName = findViewById(R.id.textViewCourseName);
        textViewRoomNumber = findViewById(R.id.textViewRoomNumber);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        email = user.getEmail();

        dayOfWeekMap = new HashMap<>();

        // Add key-value pairs to the dictionary
        dayOfWeekMap.put(1, "Sunday");
        dayOfWeekMap.put(2, "Monday");
        dayOfWeekMap.put(3, "Tuesday");
        dayOfWeekMap.put(4, "Wednesday");
        dayOfWeekMap.put(5, "Thursday");
        dayOfWeekMap.put(6, "Friday");
        dayOfWeekMap.put(7, "Saturday");




        //set class end time
        cardViewTimeRemainingNum.setText("9:10");

        //set class end hour minute and second


        //create a thread that will update the current time every second

        timeThread = new Thread(){
            @Override
            public void run(){
                try{
                    while(!isInterrupted()){
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Calendar currentTime = Calendar.getInstance();
                                //get current hour
                                //get hour from currentTime

                                int hour = (currentTime.get(Calendar.HOUR_OF_DAY));
                                int minute = (currentTime.get(Calendar.MINUTE));
                                int second = (currentTime.get(Calendar.SECOND));

//                                Log.d("time", hour + ":" + minute + ":" + second);

                                String hourString = Integer.toString(hour);
                                String minuteString = Integer.toString(minute);
                                String secondString = Integer.toString(second);

                                if (hour < 10){
                                    hourString = "0" + hourString;
                                }
                                if (minute < 10){
                                    minuteString = "0" + minuteString;
                                }
                                if (second < 10){
                                    secondString = "0" + secondString;
                                }

                                String currentTimeString = hourString + ":" + minuteString + ":" + secondString;
                                textViewLiveCountCurrentTimeNum.setText(currentTimeString);

                            }
                        });
                    }
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        };
        timeThread.start();
        refreshRoom();
        refreshMostRecentStudent();


    }

    void refreshMostRecentStudent(){
        //get most recent student from database
        //set textview to most recent student

        DatabaseReference ref = database.getReference("/PRESENCE/"+ Room); //to be replaced with student
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentCountStudents = 0;
                boolean foundOneStudent = false;
                int mostRecentHour = 23;
                int mostRecentMinute = 59;
                int mostRecentSecond = 59;
                String mostRecentStudentName = "";
                String mostRecentStudentID = "";
                //declare appendable list
                List<String> presentStudentList = new ArrayList<String>();
                try {
                        for (DataSnapshot idSnapshot : dataSnapshot.getChildren()) { // loop through each ID
                            //print each object

                            for (DataSnapshot presentSnapshot : idSnapshot.getChildren()) { // loop through each present status
                                Log.d("ID", idSnapshot.getKey());
                                if (presentSnapshot.getKey().equals("present") && presentSnapshot.getValue(Boolean.class) != null && presentSnapshot.getValue(Boolean.class)) {
                                    presentStudentList.add(idSnapshot.getKey());
                                    currentCountStudents++;
                                }
                                else if (presentSnapshot.getKey().equals("time_in") && presentSnapshot.getValue(String.class) != null && presentStudentList.contains(idSnapshot.getKey()))   {
                                    int hour = getHour(presentSnapshot.getValue().toString());
                                    int minute = getMinute(presentSnapshot.getValue().toString());
                                    int second = getSecond(presentSnapshot.getValue().toString());
                                    //check for most recent time
                                    if (hour < mostRecentHour) {
                                        mostRecentHour = hour;
                                        mostRecentMinute = minute;
                                        mostRecentSecond = second;
                                        mostRecentStudentName = idSnapshot.getKey();
                                        mostRecentStudentID = idSnapshot.getKey();
                                        foundOneStudent = true;
                                    } else if (hour == mostRecentHour && minute < mostRecentMinute) {
                                        mostRecentHour = hour;
                                        mostRecentMinute = minute;
                                        mostRecentSecond = second;
                                        mostRecentStudentName = idSnapshot.getKey();
                                        mostRecentStudentID = idSnapshot.getKey();
                                        foundOneStudent = true;
                                    } else if (hour == mostRecentHour && minute == mostRecentMinute && second < mostRecentSecond) {
                                        mostRecentHour = hour;
                                        mostRecentMinute = minute;
                                        mostRecentSecond = second;
                                        mostRecentStudentName = idSnapshot.getKey();
                                        mostRecentStudentID = idSnapshot.getKey();
                                        foundOneStudent = true;
                                    }
                                    Log.d("ID", mostRecentStudentID);
                                    Log.d("Name", mostRecentStudentName);
                                }
                            }
                        }
                        if (foundOneStudent) {
                            textViewLastStudentJoinedName.setText(mostRecentStudentName);
                            textViewLastStudentJoinedID.setText(mostRecentStudentID);
                            textViewLastStudentJoinedTime.setText(mostRecentHour + ":" + mostRecentMinute + ":" + mostRecentSecond);
                        }
                        else{
                            textViewLastStudentJoinedName.setText("No Student has joined yet");
                            textViewLastStudentJoinedID.setText("");
                            textViewLastStudentJoinedTime.setText("");
                        }
                        textViewLiveCountNum.setText(String.valueOf(currentCountStudents));

                } catch (Exception e) {
                    Log.d("ERROR", e.toString());
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void refreshRoom(){
        CollectionReference docRef = db.collection("COURSES");
        docRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Calendar currentTime = Calendar.getInstance();
                int current_hour = (currentTime.get(Calendar.HOUR_OF_DAY));
                int current_minute = (currentTime.get(Calendar.MINUTE));
                int current_day_of_week = currentTime.get(Calendar.DAY_OF_WEEK);
                String current_day_of_week_string = dayOfWeekMap.get(current_day_of_week);


                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        // Access the document data
                        String documentId = document.getId();
                        String owner = document.getString("OWNER");
                        List<String> daysOfWeek = (List<String>) document.get("DAYS");
                        String roomNumber = document.getString("ROOM_NUMBER");
                        int startHour = document.getLong("START_HOUR").intValue();
                        int startMinute = document.getLong("START_MIN").intValue();
                        int endHour = document.getLong("END_HOUR").intValue();
                        int endMinute = document.getLong("END_MIN").intValue();

                        //check if current time is within the class time
                        if (owner == email){
                            if (daysOfWeek.contains(current_day_of_week_string)){
                                if (current_hour >= startHour && current_hour <= endHour){
                                    if (current_minute >= startMinute && current_minute <= endMinute){
                                        Room = roomNumber;
                                        courseName = documentId;
                                        Log.d("Found class and room: ",   Room + " " + courseName);
                                        textViewCourseName.setText(courseName);
                                        textViewRoomNumber.setText(Room);
                                    }
                                }
                            }
                        }

                        Log.d("Info", "Document ID: " + documentId + "Owner " + owner + "Days of week " + daysOfWeek);
                    }
                } else {
                    Log.e("ERROR", "Error getting documents: ", task.getException());
                }
            }
        });
    }




    @Override
    protected void onResume(){
        super.onResume();
        if (!timeThread.isAlive()){
            timeThread.start();
        }
    }

    //on pause, stop the thread
    @Override
    protected void onPause(){
        super.onPause();
        //pause the time tracking thread
        timeThread.interrupt();
        finish();
    }

    int getHour(String time){
        String hourMinSec = time.split(",")[3];
        return Integer.parseInt(hourMinSec.split(":")[0]);
    }
    int getMinute(String time){
        String hourMinSec = time.split(",")[3];
        return Integer.parseInt(hourMinSec.split(":")[1]);
    }
    int getSecond(String time){
        String hourMinSec = time.split(",")[3];
        return Integer.parseInt(hourMinSec.split(":")[2]);
    }


}