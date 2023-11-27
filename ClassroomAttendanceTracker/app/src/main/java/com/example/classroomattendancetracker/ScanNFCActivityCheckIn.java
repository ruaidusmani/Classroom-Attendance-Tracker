package com.example.classroomattendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.index.qual.LengthOf;

import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanNFCActivityCheckIn extends AppCompatActivity {
    PreferencesController preferencesController;

    ImageView imageViewError;
    ImageView imageViewSuccess;

    TextView textViewError;
    TextView textViewSuccess;
    String roomNumber;
    String classNameSignIn;



    FirebaseUser user;
    FirebaseAuth mAuth;
    String email;
    TextView textViewPersonInfo;
    FirebaseFirestore db;
    boolean enrolledIntoClass = false;

    boolean tried_to_sign_in;

    HashMap<Integer, String> daysTranslate= new HashMap<Integer, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_nfc_check_in);
        Log.d("onCreate", "onCreate");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        preferencesController = new PreferencesController(getApplicationContext());
        imageViewError = findViewById(R.id.imageViewError);
        imageViewSuccess = findViewById(R.id.imageViewSuccess);
        textViewError = findViewById(R.id.textViewError);
        textViewSuccess = findViewById(R.id.textViewSuccess);
        textViewPersonInfo = findViewById(R.id.textViewPersonInfo);

        if (user != null) {
            email = user.getEmail();
        } else {
            email = "null";
        }
        textViewPersonInfo.setText("\tEmail :\t\t" + email + " \n\tID :\t\t\t\t\t\t\t" + preferencesController.getString("AndroidID"));
        Log.d("SCAN_NFC_EMAIL", email);

        daysTranslate.put(1, "Sunday");
        daysTranslate.put(2, "Monday");
        daysTranslate.put(3, "Tuesday");
        daysTranslate.put(4, "Wednesday");
        daysTranslate.put(5, "Thursday");
        daysTranslate.put(6, "Friday");
        daysTranslate.put(7, "Saturday");





        tried_to_sign_in = false;
        refresh();

        // Toolbar
        Toolbar check_in_toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(check_in_toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Classroom Check-in");

        //Toolbar items
        check_in_toolbar.showOverflowMenu();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //start the NFC Reading service
        Intent serviceIntent1 = new Intent(this, NFCHost.class);
        Log.d("Status Main: ", preferencesController.getString("NFCString"));
        preferencesController.setPreference("NFCString", "CI_" + preferencesController.getString("AndroidID"));
        if ("null".equals(preferencesController.getString("NFCString"))) { // TODO: remove this, not needed anymore, just send the payload from the menu directly.
            Log.d("NFCString", "Shared Pref is null");
            preferencesController.setPreference("NFCString", "CI_" + preferencesController.getString("AndroidID"));
        }
//        serviceIntent1.putExtra("NFCString", preferencesController.getString("NFCString"));
        startService(serviceIntent1);
    }

    //when the activity is resumed, start the NFC Reading service
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "onResume");
        refresh();
        Intent serviceIntent1 = new Intent(this, NFCHost.class);
        preferencesController.setPreference("NFCString", "CI_" + preferencesController.getString("AndroidID"));
        if ("null".equals(preferencesController.getString("NFCString"))) {
            Log.d("NFCString", "Shared Pref is null");
            preferencesController.setPreference("NFCString", "CI_" + preferencesController.getString("AndroidID"));
        }
        Log.d("Status Main: ", preferencesController.getString("NFCString"));
        serviceIntent1.putExtra("NFCString", "CI_" + preferencesController.getString("AndroidID")); // to change
        startService(serviceIntent1);
    }


    //stop the service when the activity is paused or destroyed
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause", "onPause");
        tried_to_sign_in = true;
        Intent serviceIntent1 = new Intent(this, NFCHost.class);
        stopService(serviceIntent1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", "onDestroy");
        Intent serviceIntent1 = new Intent(this, NFCHost.class);
        stopService(serviceIntent1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Back button
        if (id == android.R.id.home) {
            this.finish();
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkIfEnrolled() {
        String collectionName = "USERS";
        String documentName = email;
        DocumentReference docRef = db.collection(collectionName).document(documentName);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String[] enrolled = documentSnapshot.get("classes").toString().replace("[", "").replace("]", "").replace(", ", ",").split(",");
//                Log.d("GJHSDG", enrolled.toString());
                //check if in enrolled array the class is present
                enrolledIntoClass = false;
                for (int i = 0; i < enrolled.length; i++) {
                    Log.d("GJHSDG", enrolled[i]);
                    if (enrolled[i].equals(classNameSignIn)) {
                        enrolledIntoClass = true;
                    }
                }

                if (!enrolledIntoClass) {
//                    textViewSuccess.setText("The class that is ongoing right now is not in your enrolled classes.");
                    textViewSuccess.setVisibility(View.INVISIBLE);
                    imageViewSuccess.setVisibility(View.INVISIBLE);
                    textViewError.setVisibility(View.VISIBLE);
                    textViewError.setText("You are not enrolled in the current Class (" + classNameSignIn + "), or you are too early to class - You must sign-in 15 minutes before class starts or later");
                    imageViewError.setVisibility(View.VISIBLE);
                    Log.d("Calling setPresence", "setPresence");
                    setPresence(false);
                }
                else{
                    updateFirestoreDocument();

                }
            }
        });
    }
    public void updateFirestoreDocument(){

        if (classNameSignIn.equals("null") || classNameSignIn == null) {
            return;
        }
        String collectionName = "COURSES";
        String documentName = classNameSignIn;
        DocumentReference docRef = db.collection(collectionName).document(documentName);
        Calendar currentTime = Calendar.getInstance();



        int day = (currentTime.get(Calendar.DAY_OF_MONTH));
        int month = (currentTime.get(Calendar.MONTH)) + 1;
        int year = (currentTime.get(Calendar.YEAR));

        String monthString = String.valueOf(month);
        if (monthString.length() == 1) {
            monthString = "0" + monthString;
        }
        String dayString = String.valueOf(day);
        if (dayString.length() == 1){
            dayString = "0" + dayString;
        }

        String dayMonthYear = dayString + "_" + monthString + "_" + year;

//        String stringToPush = "PRESENT" + "." + dayMonthYear + "." + email + "." + "present";
        String stringToPush = "PRESENT" + "." + dayMonthYear;

//        stringToPush  = stringToPush + "." + email.replace(".", "!") + ".";
        stringToPush  = stringToPush + "." + EncoderHelper.encode(email) + ".";
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentTime.get(Calendar.MINUTE);

        Log.d("ENROLLED INTO CLASS IS: ", String.valueOf(enrolledIntoClass))   ;

        if (enrolledIntoClass) {

//        docRef.update(stringToPush, FieldValue.arrayUnion(email)).addOnSuccessListener(new OnSuccessListener<Void>() {
            docRef.update(stringToPush + "arrival_hour", currentHour, stringToPush + "arrival_minute", currentMinute).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("A", "DocumentSnapshot successfully updated!");
                            textViewSuccess.setText("You have successfully signed into " + classNameSignIn + ", Room " + roomNumber);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("A", "Error updating document", e);
                        }
                    });
        }
    }
    public void findFirestoreDocument() {
        //get android id
        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (roomNumber.equals("null") || roomNumber == null) {
            Log.d("Room Number null", "null");
           return;
        }

        db.collection("COURSES").whereEqualTo("ROOM_NUMBER", roomNumber).get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("Entered query", roomNumber);
                        for (QueryDocumentSnapshot query : task.getResult()) {
                            int startMin = query.getLong("START_MIN").intValue();
                            int startHour = query.getLong("START_HOUR").intValue();
                            int endMin = query.getLong("END_MIN").intValue();
                            int endHour = query.getLong("END_HOUR").intValue();
                            String [] days = query.get("DAYS").toString().replace("[", "").replace("]", "").replace(" ", "").split(",");
//                            Log.d("Days", days.toString());
                            int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                            Log.d("Current day", String.valueOf(currentDay));
                            Calendar currentTime = Calendar.getInstance();
                            int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                            int currentMinute = currentTime.get(Calendar.MINUTE);

                            int startTimeSecs = startHour * 3600 + startMin * 60;
                            int endTimeSecs = endHour * 3600 + endMin * 60;
                            int currentTimeSecs = currentHour * 3600 + currentMinute * 60 ;

//                            Log.d("Start time", String.valueOf(startTimeSecs));
//                            Log.d("End time", String.valueOf(endTimeSecs));
//                            Log.d("Current time", String.valueOf(currentTimeSecs));

                            boolean isDay = false;

                            for (int i = 0; i < days.length; i++) {
                                Log.d("Days", days[i]);
                                Log.d("days translate", daysTranslate.get(currentDay));
                                if (daysTranslate.get(currentDay).equals(days[i])) {
                                    isDay = true;
                                }
                            }

                            Log.d("isDay" + query.getId(), String.valueOf(isDay));
                            Log.d("Current time" + query.getId(), String.valueOf(currentTimeSecs));
                            Log.d("Start time"+ query.getId(), String.valueOf(startTimeSecs));
                            Log.d("End time"+ query.getId(), String.valueOf(endTimeSecs));


                            if (isDay && (currentTimeSecs > (startTimeSecs-(15*60)))  && currentTimeSecs < endTimeSecs) { // if current time is between start and end time
                                classNameSignIn = query.getId();
                                Log.d("FOUND CLASS", classNameSignIn);
                                Log.d("isDay" + query.getId(), String.valueOf(isDay));
                                Log.d("Current time" + query.getId(), String.valueOf(currentTimeSecs));
                                Log.d("Start time"+ query.getId(), String.valueOf(startTimeSecs));
                                Log.d("End time"+ query.getId(), String.valueOf(endTimeSecs));
                                Log.d("Class name", classNameSignIn);
                                checkIfEnrolled();

                            }

                        }
                        if (classNameSignIn == null || classNameSignIn.equals("null")){
                            textViewSuccess.setVisibility(View.INVISIBLE);
                            imageViewSuccess.setVisibility(View.INVISIBLE);
                            textViewError.setVisibility(View.VISIBLE);
                            textViewError.setText("There is no class happening right now or in the next 15 minutes");
                            imageViewError.setVisibility(View.VISIBLE);
                            setPresence(false);
                        }
                    }
                }
        );
    }

    void setPresence(Boolean userin){
        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("/PRESENCE/" + roomNumber + "/" + android_id);
        Log.d("Setting presence", "Setting presence");
        ref.child("present").setValue(userin).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Setting presence", "Success");
                refresh();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Setting presence", "Failure");
            }
        });

    }

    void refresh() {
        final FirebaseDatabase database = com.google.firebase.database.FirebaseDatabase.getInstance();
        //TODO: Change path of database to student's ID, should be dynamic as it check if a student is currently logged in
        //write code that checks if a specific ID is marked as present = true. the path is /Presence/room/ID/present

        String id = preferencesController.getString("AndroidID");
        DatabaseReference ref = database.getReference("/PRESENCE"); //to be replaced with student
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean present = false;
                try {
                    for (DataSnapshot roomNumberSnapshot : dataSnapshot.getChildren()) { // loop through each room
                        for (DataSnapshot idSnapshot : roomNumberSnapshot.getChildren()) { // loop through each ID
                            //print all the values
                            Log.d("Room Number", roomNumberSnapshot.getKey());
                            Log.d("ID", idSnapshot.getKey());
                            for (DataSnapshot presentSnapshot : idSnapshot.getChildren()) { // loop through each present status
                                if (idSnapshot.getKey().equals(id) && presentSnapshot.getKey().equals("present") && presentSnapshot.getValue(Boolean.class) != null && presentSnapshot.getValue(Boolean.class)) {
                                    Log.d("Updating Present", presentSnapshot.getValue().toString());
                                    roomNumber = roomNumberSnapshot.getKey();
                                    present = true;
                                }
                                Log.d("Present", presentSnapshot.getValue().toString());
                            }
                        }
                    }
                    Log.d("PresentFinal", String.valueOf(present));
                    //if present, make elements in xml visible

                    if (tried_to_sign_in) {
                        if (present) {
                            textViewSuccess.setVisibility(View.VISIBLE);
                            textViewSuccess.setText("You have successfully signed in!");
                            imageViewSuccess.setVisibility(View.VISIBLE);
                            textViewError.setVisibility(View.INVISIBLE);
                            imageViewError.setVisibility(View.INVISIBLE);
                            findFirestoreDocument();

                        } else {
                            textViewSuccess.setVisibility(View.INVISIBLE);
                            imageViewSuccess.setVisibility(View.INVISIBLE);
                            textViewError.setVisibility(View.VISIBLE);
                            if (textViewError.getVisibility() != View.VISIBLE){
                                textViewError.setText("Something Came Up! Login Failed!");
                                textViewError.setVisibility(View.VISIBLE);
                            }
//
                            imageViewError.setVisibility(View.VISIBLE);
                            roomNumber = "null";
                        }
                    } else {
                        if (present) {
                            textViewSuccess.setVisibility(View.VISIBLE);
                            textViewSuccess.setText("You are already Signed into this class!");
                            imageViewSuccess.setVisibility(View.VISIBLE);
                            textViewError.setVisibility(View.INVISIBLE);
                            imageViewError.setVisibility(View.INVISIBLE);
//                            findFirestoreDocument();
                        } else {
                            textViewSuccess.setVisibility(View.INVISIBLE);
                            imageViewSuccess.setVisibility(View.INVISIBLE);
                            textViewError.setVisibility(View.INVISIBLE);
                            textViewError.setText("Please approach the phone to the NFC Reader");
                            imageViewError.setVisibility(View.INVISIBLE);
                            roomNumber = "null";
                        }
                    }

                } catch (Exception e) {
                    Log.d("ERROR", e.toString());
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




}