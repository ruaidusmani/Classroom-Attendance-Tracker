package com.example.classroomattendancetracker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditNFCPayloadActivity extends AppCompatActivity {
    TextView textViewEnterString;
    Button buttonSendString;
    PreferencesController preferencesController;
//    Vibrator vibrator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_nfc_bytes);

        preferencesController = new PreferencesController(getApplicationContext());

        buttonSendString = findViewById(R.id.buttonSendString);
        textViewEnterString = findViewById(R.id.textViewEnterString);

        buttonSendString.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                vibrator.vibrate(100);

                String stringToSend = textViewEnterString.getText().toString();

                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("message");
                String a = textViewEnterString.getText().toString();
                myRef.setValue(a);

            }
        });
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("message");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
//
                    preferencesController.setPreference("NFCString",dataSnapshot.getValue(String.class).toString());
                    Log.d("message is : ", dataSnapshot.getValue(String.class).toString());
                    Log.d("Shared preference: ", dataSnapshot.getValue(String.class).toString());

                }
                catch (Exception e){
                    Log.d("ERROR", e.toString());
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Database Error", "Something came up when attempting to update the database");
            }


        });
    }

}