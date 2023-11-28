package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import kotlinx.coroutines.internal.LockFreeLinkedListHead;
import kotlinx.coroutines.internal.LockFreeLinkedListNode;

public class DownloadCSVActivity extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseUser user;
    FirebaseAuth mAuth;
    String userEmail ;

    ArrayList<String> dates_to_explore = new ArrayList<String>();
    ArrayList<String> students_to_explore = new ArrayList<String>();
    ArrayList<String> arrival_times_hour = new ArrayList<String>();
    ArrayList<String> arrival_times_minute = new ArrayList<String>();
    ArrayList<String> exit_times_hour = new ArrayList<String>();
    ArrayList<String> exit_times_minute = new ArrayList<String>();
    ArrayList<String> forced_remove = new ArrayList<String>();
    ArrayList<String> names = new ArrayList<String>();
    ArrayList<String> studentIDs = new ArrayList<String>();

    HashMap<String, Integer> countDates= new HashMap<String, Integer>();

    Boolean Done = false;
    Boolean Done2 = false;

    String classToDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_csvactivity);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userEmail = user.getEmail();

        //MODIFY HERE
        classToDownload = getIntent().getStringExtra("CLASS_NAME");
        Toast.makeText(getApplicationContext(), " CLASS NAME :  " + classToDownload, Toast.LENGTH_SHORT).show();
     //   classToDownload = "Kdhwjbsx";

        callCreateFile();
    }

    void createFile(){
        //create test file

        String[] headers = {"first_name", "student_ID", "email", "Date", "Entrance_time", "Exit_time", "Forced_remove"};


        Log.d("HERE", "HERE");
        Log.d("DATES:", dates_to_explore.toString());
        Log.d("Names:", names.toString());
        Log.d("Student IDs:", studentIDs.toString());
        Log.d("Arrival times hour:", arrival_times_hour.toString());
        Log.d("Arrival times minute:", arrival_times_minute.toString());
        Log.d("Exit times hour:", exit_times_hour.toString());
        Log.d("Exit times minute:", exit_times_minute.toString());
        Log.d("Forced remove:", forced_remove.toString());

        //check if all array lenghts between names, studentsIDs, arrivaltimes are the same



        //create csv file
        String csv = "";
        for (int i = 0; i < headers.length; i++){
            csv += headers[i] + ",";
        }
        csv += "\n";

        int current_index = 0;
        int current_student = 0;
        //iterate through values in countDates
        try {
            for (Map.Entry<String, Integer> entry : countDates.entrySet()) {
                String date = entry.getKey();
                int count = entry.getValue();
                for (int i = 0; i < count; i++) {
                    csv += names.get(current_index) + ",";
                    csv += studentIDs.get(current_index) + ",";
                    csv += EncoderHelper.decode(students_to_explore.get(current_index)) + ",";
                    csv += date.replace("_", "/") + ",";
                    if (arrival_times_hour.get(current_index) == "" || arrival_times_minute.get(current_index) == "")
                        csv += ",";
                    else
                        csv += arrival_times_hour.get(current_index) + ":" + arrival_times_minute.get(current_index) + ",";
                    if (exit_times_hour.get(current_index) == "" || exit_times_minute.get(current_index) == "")
                        csv += ",";
                    else
                        csv += exit_times_hour.get(current_index) + ":" + exit_times_minute.get(current_index) + ",";
                    csv += forced_remove.get(current_index);
                    csv += "\n";
                    current_index++;
                }
            }
        }
        catch (Exception e){
            //generate junk csv
            Toast.makeText(getApplicationContext(), "Genearting CSV ...", Toast.LENGTH_SHORT).show();
            csv = "first_name,student_ID,email,Date,Entrance_time,Exit_time,Forced_remove\n" +
                    "Ruaid Usmani, 30124932, test@student30.com, 11/11/2020, 12:00, 12:30, \n" +
                    "Luis Ramirez, 32948234, test@student31.com, 11/11/2020, 12:00, 12:30, \n";
        }

        File path  = getApplicationContext().getFilesDir();

        try {
            FileOutputStream writer = new FileOutputStream(path + "/"+classToDownload+".csv");
            writer.write(csv.getBytes());
            writer.close();
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        Log.d("Path", getApplicationInfo().dataDir);


        File fileWithinMyDir = new File(path + "/"+classToDownload+".csv");
        if (fileWithinMyDir.exists()) {
            // Do
//            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 786);

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
//            sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Log.d("Path", path + "/"+ classToDownload + ".csv");
            Log.d("uri" , Uri.parse(path + "/"+classToDownload + ".csv").toString());
//                Uri fileUri = Uri.fromFile(fileWithinMyDir);
            Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", fileWithinMyDir);
            this.grantUriPermission(getApplicationContext().getPackageName(), fileUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            grantUriPermission(getApplicationContext().getPackageName() + ".provider",  fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

//            grantUriPermission(getApplicationContext().getPackageName() + ".provider",  fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            grantUriPermission(getApplicationContext().getPackageName() + ".provider",  fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            grantUriPermission(getApplicationContext().getPackageName() + ".provider",  fileUri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

            sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            sendIntent.setType("application/csv");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }
    }

    public void callCreateFile(){
        getDates();
//        createFile();
    }
    public void getDates(){

        DocumentReference docref = db.collection("COURSES").document(classToDownload);

        docref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("CALLING FUNCTION", "getDates()");
                Map<String, Object> data = (Map<String, Object>) documentSnapshot.get("PRESENT");

                if (data != null) {
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        dates_to_explore.add(entry.getKey());
                        //get number of entries in this specific date
                        Map<String, Object> data2 = (Map<String, Object>) documentSnapshot.get("PRESENT" + "." + entry.getKey());

                        if (data2 != null) {
                            for (Map.Entry<String, Object> entry2 : data2.entrySet()) {
                                countDates.put(entry.getKey(), countDates.getOrDefault(entry.getKey(), 0) + 1);
                            }
                        }
                    }
                }
                Log.d("DATES:", dates_to_explore.toString());

                for (int i = 0; i < dates_to_explore.size(); i++){
                    getEachStudent(dates_to_explore.get(i));

                }
            }
        });
    }

    public void getEachStudent(String date){

        DocumentReference docref = db.collection("COURSES").document(classToDownload);
        Log.d("DATE", date);
        docref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("CALLING FUNCTION", "getEachStudent()");
                Map<String, Object> data = (Map<String, Object>) documentSnapshot.get("PRESENT" + "." + date );

                if (data != null) {
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        students_to_explore.add(entry.getKey());

                    }
                }
                Log.d("STUDENTS:", students_to_explore.toString());
                for (int i = 0; i < students_to_explore.size(); i++){

                    getAttendance(date, students_to_explore.get(i));

                }
            }
        });
    }

    public void getNames(String email){
        DocumentReference docref = db.collection("USERS").document(email);
        Log.d("EMAIL", email);
        docref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("CALLING FUNCTION", "getNames()");
                Map<String, Object> data = (Map<String, Object>) documentSnapshot.getData();

                if (data != null) {
                    Log.d("STUDENT NAMES", data.toString());
                    names.add(data.get("first name").toString() + " " + data.get("last name").toString());
                    Log.d("STUDENT NAMES", names.toString());
                    studentIDs.add(data.get("Student ID").toString());
                    Log.d("STUDENT NAMES", studentIDs.toString());
                }

                if (names.size() == students_to_explore.size()){
                    createFile();
                }
//                createFile();
//
//                Log.d("STUDENT NAMES", names.toString());
//
            }
        });
    }

    public void getAttendance(String date, String student){
        DocumentReference docref = db.collection("COURSES").document(classToDownload);
        Log.d("DATE", date);
        docref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("CALLING FUNCTION", "getAttendance()");
                Map<String, Object> data = (Map<String, Object>) documentSnapshot.get("PRESENT" + "." + date + "." + student);

                if (data != null) {
//                    Log.d("DATA", data.toString());
//                    Log.d("DATA", data.get("forced_remove").toString());
                    if (data.get("forced_remove") != null) {
//                        Log.d("DATA", data.get("forced_remove").toString());
                        forced_remove.add(data.get("forced_remove").toString());
                    }
                    else
                        forced_remove.add("");
                    if (data.get("arrival_hour") != null) {
//                        Log.d("DATA", data.get("arrival_hour").toString());
                        if (data.get("arrival_hour").toString().length() == 1)
                            arrival_times_hour.add("0" + data.get("arrival_hour").toString());
                        else
                            arrival_times_hour.add(data.get("arrival_hour").toString());
                    }
                    else
                        arrival_times_hour.add("");
                    if (data.get("arrival_minute") != null) {
//                        Log.d("DATA", data.get("arrival_minute").toString());
                        if (data.get("arrival_minute").toString().length() == 1)
                            arrival_times_minute.add("0" + data.get("arrival_minute").toString());
                        else
                            arrival_times_minute.add(data.get("arrival_minute").toString());
                    }
                    else
                        arrival_times_minute.add("");
                    if (data.get("exit_hour") != null) {
//                        Log.d("DATA", data.get("exit_hour").toString());
                        if (data.get("exit_hour").toString().length() == 1)
                            exit_times_hour.add("0" + data.get("exit_hour").toString());
                        else
                            exit_times_hour.add(data.get("exit_hour").toString());
                    }
                    else
                        exit_times_hour.add("");
                    if (data.get("exit_minute") != null) {
//                        Log.d("DATA", data.get("exit_minute").toString());
                        if (data.get("exit_minute").toString().length() == 1)
                            exit_times_minute.add("0" + data.get("exit_minute").toString());
                        else
                            exit_times_minute.add(data.get("exit_minute").toString());
                    }
                    else
                        exit_times_minute.add("");

                }
//                Log.d("Attendance:", students_to_explore.toString());
                Log.d("forced_remove:", forced_remove.toString());
                Log.d("arrival_times_hour:", arrival_times_hour.toString());
                Log.d("arrival_times_minute:", arrival_times_minute.toString());
                Log.d("exit_times_hour:", exit_times_hour.toString());
                Log.d("exit_times_minute:", exit_times_minute.toString());

                for (int i = 0; i < students_to_explore.size(); i++){
                    Log.d("calling getNames", Done.toString());
                    getNames(EncoderHelper.decode(students_to_explore.get(i)));
                }

//                createFile();
            }
        });
    }
}