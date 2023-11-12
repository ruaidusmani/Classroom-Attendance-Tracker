package com.example.classroomattendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnrollClassActivity extends AppCompatActivity implements ClassItemEnrollAdapter.ItemClickListener {

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser user;

    List<String> classList = new ArrayList<String>();
    List<Map<String,String>> classMap = new ArrayList<Map<String,String>>();

    RecyclerView recyclerViewClassList;
    ClassItemEnrollAdapter adapter;
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_class);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        //add all elements in classMap to recyclerViewClassList
        recyclerViewClassList = findViewById(R.id.recyclerViewClassList);
        recyclerViewClassList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassItemEnrollAdapter(this, classMap);
        adapter.setClickListener(this);
        recyclerViewClassList.setAdapter(adapter);

        fetchAllClasses();




    }


    void fetchAllClasses(){
        CollectionReference ref = db.collection("COURSES");
        //get all keys in COURSES
        ref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            HashMap<String,String> map = new HashMap<String,String>();
                            map.put("CLASS_NAME",document.getId());
                            map.put("DAYS", document.get("DAYS").toString());
                            map.put("START_MIN", document.get("START_MIN").toString());
                            map.put("START_HOUR", document.get("START_HOUR").toString());
                            map.put("END_MIN", document.get("END_MIN").toString());
                            map.put("END_HOUR", document.get("END_HOUR").toString());
                            map.put("OWNER", document.get("OWNER").toString());
                            map.put("ROOM_NUMBER", document.get("ROOM_NUMBER").toString());
                            classMap.add(map);
                            Log.d("TAG", document.getId() + " => " + document.getData());
                            // Access the document data using documentSnapshot.toObject(YourClass.class)
                            // Replace YourClass.class with the actual class representing your document
                            // Do something with the data...
                        }
                        for (Map<String,String> map : classMap){
                            Log.d("CLASS_NAME", map.get("CLASS_NAME"));
                            Log.d("DAYS", map.get("DAYS"));
                            Log.d("START_MIN", map.get("START_MIN"));
                            Log.d("START_HOUR", map.get("START_HOUR"));
                            Log.d("END_MIN", map.get("END_MIN"));
                            Log.d("END_HOUR", map.get("END_HOUR"));
                            Log.d("OWNER", map.get("OWNER"));
                            Log.d("ROOM_NUMBER", map.get("ROOM_NUMBER"));
                        }
                        defineAdapter();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle errors
                    }
                });
    }

    void enrollUpdateStudent(String className){
        String email = user.getEmail();
        DocumentReference docRef = db.collection("USERS").document(email);
       docRef.update("classes", FieldValue.arrayUnion(className)).addOnSuccessListener(new OnSuccessListener<Void>() {
           @Override
           public void onSuccess(Void aVoid) {
               Toast.makeText(getApplicationContext(), "Enrolled in " + className, Toast.LENGTH_SHORT).show();
           }
       }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure(@NonNull Exception e) {
               Toast.makeText(getApplicationContext(), "Failed to enroll in " + className, Toast.LENGTH_SHORT).show();
           }
       }
         );
    }
    void enrollUpdateClass(String className){
        String email = user.getEmail();
        DocumentReference docRef = db.collection("COURSES").document(className);
        docRef.update("students", FieldValue.arrayUnion(email)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                Toast.makeText(getApplicationContext(), "Enrolled in " + className, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed to enroll in " + className, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }


    @Override
    public void onItemClick(View view, int position) {

//        Toast.makeText(this, "You clicked " + adapter.getItem(position).get("CLASS_NAME") + " on row number " + position, Toast.LENGTH_SHORT).show();
        enrollUpdateStudent(adapter.getItem(position).get("CLASS_NAME"));
        enrollUpdateClass(adapter.getItem(position).get("CLASS_NAME"));

    }

    void defineAdapter(){
        recyclerViewClassList = findViewById(R.id.recyclerViewClassList);
        recyclerViewClassList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassItemEnrollAdapter(this, classMap);
        adapter.setClickListener(this);
        recyclerViewClassList.setAdapter(adapter);
    }
}