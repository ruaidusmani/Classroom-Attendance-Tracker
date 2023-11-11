package com.example.classroomattendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LiveStatsActivity extends AppCompatActivity {

    TextView cardViewTimeRemainingNum;
    TextView textViewLastStudentJoinedName;
    TextView textViewLastStudentJoinedTime;
    TextView textViewLastStudentJoinedID;
    TextView textViewLiveCountNum;
    TextView textViewLiveCountCurrentTimeNum;

    Thread timeThread;

    String Room = "H907";
    PreferencesController preferencesController;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    int currentCountStudents = 0;


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