package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

public class LiveStatsActivity extends AppCompatActivity {

    TextView cardViewTimeRemainingNum;
    TextView textViewLastStudentJoinedName;
    TextView textViewLastStudentJoinedTime;
    TextView textViewLastStudentJoinedID;
    TextView textViewLiveCountNum;
    TextView textViewLiveCountCurrentTimeNum;

    Thread timeThread;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_stats);

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

                                Log.d("time", hour + ":" + minute + ":" + second);

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

}