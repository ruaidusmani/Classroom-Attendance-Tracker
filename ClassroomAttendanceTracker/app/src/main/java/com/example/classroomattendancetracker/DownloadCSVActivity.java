package com.example.classroomattendancetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DownloadCSVActivity extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseUser user;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_csvactivity);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();



        //create test file
        String[] headers = {"Name", "ID", "Date", "Entrance_time", "Exit_time", "Attendance"};
        //generate some random data
        String [][] data = new String[10][6];
        for (int i = 0; i < 10; i++){
            data[i][0] = "Name" + i;
            data[i][1] = "ID" + i;
            data[i][2] = "Date" + i;
            data[i][3] = "Entrance_time" + i;
            data[i][4] = "Exit_time" + i;
            data[i][5] = "Attendance" + i;
        }



        //create csv file
        String csv = "";
        for (int i = 0; i < headers.length; i++){
            csv += headers[i] + ",";
        }
        csv += "\n";
        for (int i = 0; i < data.length; i++){
            for (int j = 0; j < data[i].length; j++){
                csv += data[i][j] + ",";
            }
            csv += "\n";
        }


        File path  = getApplicationContext().getFilesDir();

        try {


            FileOutputStream writer = new FileOutputStream(path + "/test.csv");
            writer.write(csv.getBytes());
            writer.close();

        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        Log.d("Path", getApplicationInfo().dataDir);


        File fileWithinMyDir = new File(path + "/test.csv");
            if (fileWithinMyDir.exists()) {
                // Do something

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                Log.d("Path", path + "/test.csv");
                Log.d("uri" , Uri.parse(path + "/test.csv").toString());
//                Uri fileUri = Uri.fromFile(fileWithinMyDir);
                Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", fileWithinMyDir);

                sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                sendIntent.setType("application/csv");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
            }

    }
}