package com.example.classroomattendancetracker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;

public class PreferencesController extends Activity {
    static String namePreferences;
    static SharedPreferences sharedPreferences;
    static SharedPreferences.Editor sharedPreferencesEdit;
    static Context context;

    PreferencesController(Context context){
        namePreferences = "sharedPreferences";
        this.context = context;
        sharedPreferences = context.getSharedPreferences(this.namePreferences, context.MODE_PRIVATE);
        sharedPreferencesEdit = sharedPreferences.edit();
    }

    static void refreshPreferences(){
        sharedPreferences = context.getSharedPreferences(namePreferences, MODE_PRIVATE);
        sharedPreferencesEdit = sharedPreferences.edit();
    }
    void setPreference(String key, String value){
        refreshPreferences();
        sharedPreferencesEdit.putString(key, value);
        sharedPreferencesEdit.commit();
    }
    void setPreference(String key, int value){
        refreshPreferences();
        sharedPreferencesEdit.putInt(key, value);
        sharedPreferencesEdit.commit();
    }
    void setPreference(String key, boolean value){
        refreshPreferences();
        sharedPreferencesEdit.putBoolean(key, value);
        sharedPreferencesEdit.commit();
    }

    void addEventsArray(int num){
//        SharedPreferences.Editor sharedPreferencesEdit = data.edit();
        String[] array = this.getString("arrayEvents").toString().replace(" ", "").replace("[","").replace("]","").split(",");
        ArrayList numArray = new ArrayList();
        for (int i = 0; i < array.length; i++){
            Log.d("element", array[i]);
            try {
                numArray.add(Integer.parseInt(array[i]));
            }
            catch (Exception exception){
                System.out.println(exception);
            }
        }
        numArray.add(num);
//        numArray.add(1);
        Log.d("prefsCreated", numArray.toString().replace("[","").replace("]",""));
        setPreference("arrayEvents", numArray.toString().replace("[","").replace("]",""));
//        return sharedPreferencesEdit;
    }

    String getString(String key){
        refreshPreferences();
        return sharedPreferences.getString(key, "null");
    }
    int getInt(String key){
        refreshPreferences();
        return sharedPreferences.getInt(key, -1);
    }
    boolean getBoolean(String key){
        refreshPreferences();
        return sharedPreferences.getBoolean(key, false);
    }


}
