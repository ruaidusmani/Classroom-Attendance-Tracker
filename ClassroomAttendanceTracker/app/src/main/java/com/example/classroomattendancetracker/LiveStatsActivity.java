package com.example.classroomattendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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
import com.google.firebase.firestore.FieldValue;
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
    TextView textViewLiveCountClassStart;
    TextView textViewLiveCountClassEnd;
    CardView cardViewLastStudent;

    Thread timeThread;
    String MostRecentStudentID;

    String Room ;
    PreferencesController preferencesController;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String email;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser user;
    String courseName;
    int classStartHour;
    int classStartMinute = 0;
    int classStartSecond;
    int classEndHour;
    int classEndMinute;
    int classEndSecond = 0;
    String emailMostRecentlyJoinedStudent;

    Button buttonRemoveRecentlyJoinedStudent;


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
        textViewLiveCountClassStart = findViewById(R.id.textViewLiveCountClassStart);
        textViewLiveCountClassEnd = findViewById(R.id.textViewLiveCountClassEnd);
        cardViewLastStudent = findViewById(R.id.cardViewLastStudent);
        buttonRemoveRecentlyJoinedStudent = findViewById(R.id.buttonRemoveRecentlyJoinedStudent);


        Toolbar toolbar;
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Live View");

        toolbar.showOverflowMenu();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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
                                String countDownEndClass;

                                if (hour*60*60 + minute*60 + second < classEndHour*60*60 + classEndMinute*60 + classEndSecond*60){
                                    int hourDiff = classEndHour - hour;
                                    int minuteDiff = classEndMinute - minute;
                                    int secondDiff = classEndSecond - second;
                                    if (secondDiff < 0){
                                        secondDiff = 60 + secondDiff;
                                        minuteDiff--;
                                    }
                                    if (minuteDiff < 0){
                                        minuteDiff = 60 + minuteDiff;
                                        hourDiff--;
                                    }
                                    if (hourDiff < 0){
                                        hourDiff = 24 + hourDiff;
                                    }
                                    String hourDiffString = Integer.toString(hourDiff);
                                    String minuteDiffString = Integer.toString(minuteDiff);
                                    String secondDiffString = Integer.toString(secondDiff);

                                    if (hourDiff < 10){
                                        hourDiffString = "0" + hourDiffString;
                                    }
                                    if (minuteDiff < 10){
                                        minuteDiffString = "0" + minuteDiffString;
                                    }
                                    if (secondDiff < 10){
                                        secondDiffString = "0" + secondDiffString;
                                    }
                                    countDownEndClass = hourDiffString + ":" + minuteDiffString + ":" + secondDiffString;
                                }
                                else{
                                    countDownEndClass = "00:00:00";
                                }


                                cardViewTimeRemainingNum.setText(countDownEndClass);
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
        refreshNameMostRecentStudent();

        buttonRemoveRecentlyJoinedStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (MostRecentStudentID != null){
//                    removeRecentlyJoinedStudentFirestore();
                    removeAttendance();
                    Toast.makeText(getApplicationContext(), "Removed " + MostRecentStudentID, Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "No student to remove", Toast.LENGTH_SHORT).show();
                }
            }
        } );
    }

    void removeRecentlyJoinedStudentRealtime(){
        Log.d("Calling remove", "removeRecentlyJoinedStudentRealtime");
        DatabaseReference ref = database.getReference("/PRESENCE/"+ Room + "/" + MostRecentStudentID + "/present");
        ref.setValue(false);
    }

    void removeAttendance() {

        DocumentReference docRef = db.collection("COURSES").document(courseName);
        Log.d("courseName", courseName);
        Calendar currentTime = Calendar.getInstance();
        int day = (currentTime.get(Calendar.DAY_OF_MONTH));
        int month = (currentTime.get(Calendar.MONTH)) + 1;
        int year = (currentTime.get(Calendar.YEAR));



        String encodedEmail = EncoderHelper.encode(emailMostRecentlyJoinedStudent);
        Log.d("encodedEmail", encodedEmail);
        Log.d("Decoded Email", EncoderHelper.decode(encodedEmail));

        String stringref = "PRESENT." + day + "_" + month + "_" + year + "." + encodedEmail;
        Log.d("stringref", stringref);


        Map<String, Object> updates = new HashMap<>();

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {


                    // Update the document with the modified array
                    docRef.update(stringref, FieldValue.delete())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Update successful
                                    Log.d("Success", "DocumentSnapshot successfully updated!");
                                    removeRecentlyJoinedStudentRealtime();
                                    refreshMostRecentStudent();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle errors
                                    Log.d("Failure", "Error updating document", e);
                                }
                            });
                }
            }
        });
    }
    void removeRecentlyJoinedStudentFirestore(){
        String mostRecentStudentID = textViewLastStudentJoinedID.getText().toString();
        if (courseName == null || courseName == null) {
            Toast.makeText(getApplicationContext(), "There is no most recently Joined student", Toast.LENGTH_SHORT).show();
            return;
        }
        DocumentReference docRef = db.collection("COURSES").document(courseName);
        Log.d("courseName", courseName);
        Calendar currentTime = Calendar.getInstance();
        int day = (currentTime.get(Calendar.DAY_OF_MONTH));
        int month = (currentTime.get(Calendar.MONTH)) + 1;
        int year = (currentTime.get(Calendar.YEAR));

        String stringref = "PRESENT." + day + "_" + month + "_" + year;
        Log.d("stringref", stringref);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    List<Object> yourArray = (List<Object>) documentSnapshot.get(stringref);
                    Log.d("yourArray", yourArray.toString());
                    for (int i = 0; i < yourArray.size(); i++) {
                        if (yourArray.get(i).equals(emailMostRecentlyJoinedStudent)) {
                            Log.d("Found", String.valueOf(i));
                            yourArray.remove(i);

                        }
                    }
                    MostRecentStudentID = null;
                    refreshNameMostRecentStudent();



                    // Update the document with the modified array
                    docRef.update(stringref, yourArray)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Update successful
                                    removeRecentlyJoinedStudentRealtime();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle errors
                                }
                            });
                }
            }
        });


        Map<String,Object> updates = new HashMap<>();

        updates.put(stringref, FieldValue.arrayRemove(0));
        docRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error deleting document", e);
                    }
                });

    }
    void refreshNameMostRecentStudent(){
        CollectionReference docRef = db.collection("USERS");
        docRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                String mostRecentStudentID = textViewLastStudentJoinedID.getText().toString();

                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        // Access the document data
                        String documentId = document.getId();
                        String android_id = document.getString("android_id");
                        String first_name = document.getString("first name");
                        String last_name = document.getString("last name");

                        if (mostRecentStudentID.equals(android_id)){
                            //change color of card view to warn professor
                            //send notification
                            //create snackbar
                            View view = findViewById(android.R.id.content).getRootView();

                            Snackbar a = Snackbar.make(findViewById(android.R.id.content).getRootView(), "New student joined the class ", Snackbar.LENGTH_SHORT);
                            emailMostRecentlyJoinedStudent = documentId;
                            textViewLastStudentJoinedName.setText(first_name + " " + last_name);
                            MostRecentStudentID = android_id;
                        }
                    }
                } else {

                    Log.e("ERROR", "Error getting documents: ", task.getException());
                }
            }
        });
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
                int mostRecentHour = 0;
                int mostRecentMinute = 0;
                int mostRecentSecond = 0;
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
                                    if (hour > mostRecentHour) {
                                        mostRecentHour = hour;
                                        mostRecentMinute = minute;
                                        mostRecentSecond = second;
                                        mostRecentStudentName = idSnapshot.getKey();
                                        mostRecentStudentID = idSnapshot.getKey();
                                        foundOneStudent = true;
                                    } else if (hour == mostRecentHour && minute > mostRecentMinute) {
                                        mostRecentHour = hour;
                                        mostRecentMinute = minute;
                                        mostRecentSecond = second;
                                        mostRecentStudentName = idSnapshot.getKey();
                                        mostRecentStudentID = idSnapshot.getKey();
                                        foundOneStudent = true;
                                    } else if (hour == mostRecentHour && minute == mostRecentMinute && second > mostRecentSecond) {
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
                            textViewLastStudentJoinedID.setText(mostRecentStudentID);
                            textViewLastStudentJoinedName.setText(mostRecentStudentName);
                            String mostRecentHourString = String.valueOf(mostRecentHour);
                            String mostRecentMinuteString = String.valueOf(mostRecentMinute);
                            String mostRecentSecondString = String.valueOf(mostRecentSecond);
                            if(mostRecentHour < 10){
                                mostRecentHourString = "0" + mostRecentHour;
                            }
                            if(mostRecentMinute < 10){
                                mostRecentMinuteString = "0" + mostRecentMinute;
                            }
                            if(mostRecentSecond < 10){
                                mostRecentSecondString = "0" + mostRecentSecond;
                            }
                            textViewLastStudentJoinedTime.setText(mostRecentHourString + ":" + mostRecentMinuteString + ":" + mostRecentSecondString);
                        }
                        else{
                            textViewLastStudentJoinedName.setText("No Student has joined yet");
                            textViewLastStudentJoinedID.setText("");
                            textViewLastStudentJoinedTime.setText("");
                        }
                        textViewLiveCountNum.setText(String.valueOf(currentCountStudents));
                         refreshNameMostRecentStudent();

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
                Boolean classFound = false;


                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        // Access the document data
                        String documentId = document.getId();
                        Log.d("CLass name: " , documentId);
                        String owner = document.getString("OWNER");


                        if (owner.equals(email)){
                            Log.d("Found class", documentId);
                            List<String> daysOfWeek = (List<String>) document.get("DAYS");
                            String roomNumber = document.getString("ROOM_NUMBER");
                            int startHour = document.getLong("START_HOUR").intValue();
                            int startMinute = document.getLong("START_MIN").intValue();
                            int endHour = document.getLong("END_HOUR").intValue();
                            int endMinute = document.getLong("END_MIN").intValue();
                            int startSeconds = startHour*60*60 + startMinute*60;
                            int endSeconds = endHour*60*60 + endMinute*60;
                            int currentSeconds = current_hour*60*60 + current_minute*60;

                            String startHourString = String.valueOf(startHour);
                            String startMinuteString = String.valueOf(startMinute);
                            String endHourString = String.valueOf(endHour);
                            String endMinuteString = String.valueOf(endMinute);

                            if (startHour <10){
                                startHourString = "0" + startHourString;
                            }
                            if (startMinute <10){
                                startMinuteString = "0" + startMinuteString;
                            }
                            if (endHour <10){
                                endHourString = "0" + endHourString;
                            }
                            if (endMinute <10){
                                endMinuteString = "0" + endMinuteString;
                            }


                            if (daysOfWeek.contains(current_day_of_week_string)){

                                if (currentSeconds >= startSeconds && currentSeconds <= endSeconds) {
//                                    if (current_minute <= endMinute){
                                    Log.d("Found minute", String.valueOf(current_minute));
                                    classFound = true;
                                    Room = roomNumber;
                                    courseName = documentId;
                                    textViewLiveCountClassEnd.setText("Class End: " + endHourString + ":" + endMinuteString);
                                    textViewLiveCountClassStart.setText("Class Start: " + startHourString + ":" + startMinuteString);
                                    Log.d("Found class and room: ", Room + " " + courseName);
                                    textViewCourseName.setText("Course: " + courseName);
                                    textViewRoomNumber.setText("Room: " + Room);
                                    classEndHour = endHour;
                                    classEndMinute = endMinute;
                                    classStartHour = startHour;
                                    classStartMinute = startMinute;
                                    classStartSecond = 0;
                                    classEndSecond = 0;

                                }
//                            Log.d("Info", "Document ID: " + documentId + "Owner " + owner + "Days of week " + daysOfWeek);
                                refreshMostRecentStudent();
                            }

                        }


                    }
                    if (!classFound){
                        textViewCourseName.setText("No class Yet");
                        textViewRoomNumber.setText("");
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