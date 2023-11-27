package com.example.classroomattendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherAddClass extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Button monday_chip;
    Button tuesday_chip;
    Button wednesday_chip;
    Button thursday_chip;
    Button friday_chip;

    TimePicker time_picker_start;
    TimePicker time_picker_end;

    EditText subject_title;
    EditText room_number;
    EditText course_number;

    Button submit_class;

    ChipGroup chipGroup;

    Spinner building_spinner;
    Spinner class_spinner;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    boolean classValid = false;

    //List of possible subjects
    ArrayList<String> subjects = new ArrayList<> (Arrays.asList("COEN", "ELEC", "AERO", "MECH"));

    //List of possible buildings
    HashMap<String, List<String>> Map_Building_Rooms = new HashMap<String, List<String>>();
    String selected_building;
    String selected_class;

    String room; //room number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_add_class);

        //Firebase init variables
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        //Chips init
        monday_chip     = findViewById(R.id.monday_chip);
        tuesday_chip    = findViewById(R.id.tuesday_chip);
        wednesday_chip  = findViewById(R.id.wednesday_chip);
        thursday_chip= findViewById(R.id.thursday_chip);
        friday_chip     = findViewById(R.id.friday_chip);

        //Time pickers init
        time_picker_start = findViewById(R.id.time_picker_start);
        time_picker_end= findViewById(R.id.time_picker_end);

        //Buttons init
        submit_class = findViewById(R.id.submit_class_teacher);

        //EditTexts init
        subject_title = findViewById(R.id.subject_title);
        course_number = findViewById(R.id.course_number);
        room_number = findViewById(R.id.room_number);

        chipGroup = findViewById(R.id.chip_days_group);



        //Spinner init for selecting selected_building
        building_spinner = findViewById(R.id.building_spinner);
        //Spinner init for selecting rooms
        class_spinner = findViewById(R.id.class_spinner);

        Populate_Map_Building_Rooms();


        ArrayAdapter<CharSequence> building_adapter = ArrayAdapter.createFromResource(this,
                R.array.building_array, R.layout.building_spinner_text);
        building_adapter.setDropDownViewResource(R.layout.building_spinner_dropdown);
        building_spinner.setAdapter(building_adapter);
        building_spinner.setSelection(0); // "Please Select" will be the default selection

        building_spinner.setOnItemSelectedListener(this);
        class_spinner.setOnItemSelectedListener(this);



        //Adding Listeners
        submit_class.setOnClickListener(ActivityClickListener);
    }

    public void Populate_Map_Building_Rooms(){
        String[] H_rooms = {
                "501", "507", "509", "513", "520", "521", "529",
                "531", "535","537", "539", "540", "544", "553",
                "557", "561", "562", "564", "565",
                "601", "605", "607", "609", "613", "620", "621",
                "625", "670",
                "820",
                "920", "937",
                "1070", "1011",
                "1252", "1254-02", "1269", "1267", "1271"
        };

        String[] MB_rooms = {
                "S2.105", "S2.115", "S2.210", "S2.285", "S2.330", "S2.401", "S2.445",
                "S2.455", "S2.465",
                "S1.105", "S1.115", "S1.1235", "S1.1255", "S1.1401", "S1.430", "S1.435",
                "1.210", "1.301", "1.437",
                "2.210", "2.255", "2.265", "2.270", "2.285",
                "3.210", "3.255", "3.265", "3.270", "3.285",
                "3.430", "3.435", "3.445",
                "4.206",
                "5.255", "5.265", "5.275",
                "6.240", "6.260", "6.425"
        };

        String[] LS_rooms = {
                "105", "107", "108", "110",
                "205", "207", "208", "210"
        };

        String[] FB_rooms = {
                "S-109", "S-113", "S-129",
                "S-133", "S-143", "S-150"
        };

        String[] FG_rooms = {
                "B-030", "B-040", "B-050", "B-060", "B-070", "B-080",
                "C-070", "C-080"
        };

        String[] please_select = {"Please Select"};

        Map_Building_Rooms.put("Please Select", Arrays.asList(please_select));
        Map_Building_Rooms.put("H", Arrays.asList(H_rooms));
        Map_Building_Rooms.put("MB", Arrays.asList(MB_rooms));
        Map_Building_Rooms.put("FB", Arrays.asList(FB_rooms));
        Map_Building_Rooms.put("FG", Arrays.asList(FG_rooms));
        Map_Building_Rooms.put("LS", Arrays.asList(LS_rooms));

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(adapterView.getId() == R.id.building_spinner)
        {
            if (!adapterView.getItemAtPosition(i).toString().equals("Please Select")) {
                selected_building = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(adapterView.getContext(), adapterView.getItemAtPosition(i).toString(), Toast.LENGTH_SHORT).show();

                // Get classes for the selected selected_building from the HashMap
                List<String> classes = Map_Building_Rooms.get(selected_building);


                if (classes != null) {
                    List<String> modifiableClasses = new ArrayList<>(classes);
                    modifiableClasses.add(0, "Please Select");
                    ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this,
                            R.layout.building_spinner_text, modifiableClasses);
                    classAdapter.setDropDownViewResource(R.layout.building_spinner_dropdown);
                    class_spinner.setAdapter(classAdapter);
                    class_spinner.setSelection(0);
                    doSomethingWithSelectedBuilding(selected_building);
                } else {
                    // Handle the case when no rooms are available for the selected building
                    // For instance, you might clear the class_spinner or display a message
                    class_spinner.setAdapter(null); // Clear the adapter or set to default
                    Toast.makeText(adapterView.getContext(), "No rooms available", Toast.LENGTH_SHORT).show();
                    doSomethingWithSelectedBuilding(selected_building);
                }

                if (selected_class != null && !selected_class.equals("Please Select")) {
                    room = selected_building + " " + selected_class;
                    Log.d("Room value 1", room); // Log the room value for verification
                    update_room(selected_building, selected_class);
                }


            } else {}
        }
        else if(adapterView.getId() == R.id.class_spinner)
        {
            if (!adapterView.getItemAtPosition(i).toString().equals("Please Select")){
                selected_class = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(adapterView.getContext(), selected_class, Toast.LENGTH_SHORT).show();
                //room = selected_building + " " + selected_class;

                if (selected_building != null && !selected_building.equals("Please Select")) {
                    room = selected_building + " " + selected_class;
                    Log.d("Room value 2", room); // Log the room value for verification
                    update_room(selected_building, selected_class);
                }
            }
            else{}
        }

    }

    public void update_room(String selected_building, String selected_class){
        room = selected_building + " " + selected_class;
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

    public void doSomethingWithSelectedBuilding(String mew_selected_building){
        this.selected_building = mew_selected_building;
        Log.d("DO Selected Building = ", selected_building);
//        Log.d("DO Room = ", room);
    }


    View.OnClickListener ActivityClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.submit_class_teacher){
                if(validateInputs()){
                    validateClass();
                }
            }
        }
    };

    public boolean validateInputs(){
        if (subject_title.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Please enter a class name" , Toast.LENGTH_SHORT).show();
            return false;
        }
        // check if subject title is in list of possible subject titles array
        if (!subjects.contains(subject_title.getText().toString())){
            Toast.makeText(getApplicationContext(), "Please enter a valid subject title" , Toast.LENGTH_SHORT).show();
            return false;
        }
//        if (room_number.getText().toString().equals("")){
//            Toast.makeText(getApplicationContext(), "Please enter a room number" , Toast.LENGTH_SHORT).show();
//            return false;
//        }
        if (chipGroup.getCheckedChipIds().size() == 0){
            Toast.makeText(getApplicationContext(), "Please select at least one day" , Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void validateClass(){
        //make sure that the class does not conflict with the time of another one
        db = FirebaseFirestore.getInstance();

        // Reference to your collection
        CollectionReference collectionReference = db.collection("COURSES");


        ArrayList<String> days_of_the_week_selected = new ArrayList<>();
        for (Integer id : chipGroup.getCheckedChipIds()){
            Chip chip = chipGroup.findViewById(id);
            days_of_the_week_selected.add(chip.getText().toString());
        }
//        String selectedRoom = room_number.getText().toString();
        String selectedRoom = room;
        // Retrieve all documents in the collection
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                classValid = true;
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        // Access each document here
                        Log.d("Class", document.getId() + " => " + document.getData());
                        Map<String, Object> documentDict = document.getData();
                        if (selectedRoom != null && selectedRoom.equals(documentDict.get("ROOM_NUMBER"))){
                            Log.d("Class", "Room Number is the same");
                            //check if the days are the same
                            ArrayList<String> days_of_the_week = (ArrayList<String>) documentDict.get("DAYS");
                            for (String day : days_of_the_week_selected){
                                if (days_of_the_week.contains(day)){
//                                    Log.d("Class", "Looking at day: " + day);
//                                    Log.d("Class", d)
                                    Log.d("Class", "Days are the same");
                                    //check if the time is the same
                                    long start_hour_long = (long) documentDict.get("START_HOUR");
                                    int start_hour = Long.valueOf(start_hour_long).intValue();

                                    long start_min_long = (long) documentDict.get("START_MIN");
                                    int start_min = Long.valueOf(start_min_long).intValue();


                                    long end_hour_long = (long) documentDict.get("END_HOUR");
                                    int end_hour = Long.valueOf(end_hour_long).intValue();

                                    long end_min_long = (long) documentDict.get("END_MIN");
                                    int end_min = Long.valueOf(end_min_long).intValue();


                                    int start_hour_selected = time_picker_start.getHour();
                                    int start_min_selected = time_picker_start.getMinute();

                                    int end_hour_selected = time_picker_end.getHour();
                                    int end_min_selected = time_picker_end.getMinute();

                                    int start_sec = start_hour * 60 * 60 + start_min * 60;
                                    int end_sec = end_hour * 60 * 60 + end_min * 60;
                                    int start_sec_selected = start_hour_selected * 60 * 60 + start_min_selected * 60;
                                    int end_sec_selected = end_hour_selected * 60 * 60 + end_min_selected * 60;

                                    if (start_sec_selected >= start_sec && start_sec_selected <= end_sec){
                                        Log.d("Class", "Start hour is the same");
                                        Toast.makeText(getApplicationContext(), "Cannot add this class, as it conflicts with another class at the same time" , Toast.LENGTH_SHORT).show();
                                        classValid = false;
                                        return;
                                    }
                                    else if (end_sec_selected >= start_sec && end_sec_selected <= end_sec){
                                        Log.d("Class", "End hour is the same");
                                        Toast.makeText(getApplicationContext(), "Cannot add this class, as it conflicts with another class at the same time" , Toast.LENGTH_SHORT).show();
                                        classValid = false;
                                        return;
                                    }

                                }
                            }
                        }

                    }
                    if (classValid){
                        addClassService();
                    }
                } else {
                    Log.w("Class", "Error getting documents.", task.getException());
                }
            }
        });
    }

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

//        class_information.put("ROOM_NUMBER", room_number.getText().toString());

        class_information.put("ROOM_NUMBER", room);
        String full_class_name = subject_title.getText().toString() + " " + course_number.getText().toString();

        db.collection("COURSES")
                .document(full_class_name)
                .set(class_information)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Class Sucessfully Added !" , Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), TeacherHomepage.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to add Class " , Toast.LENGTH_SHORT).show();
                    }
                });
    }


}