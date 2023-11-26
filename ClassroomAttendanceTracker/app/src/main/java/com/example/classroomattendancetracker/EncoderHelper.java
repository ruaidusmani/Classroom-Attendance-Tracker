package com.example.classroomattendancetracker;

import android.util.Log;

public class EncoderHelper {
    //convert for every 2 chars in the string of hex numbers, convert to ascii
    static String decode(String string_in){
        String string_out = "";
        //convert ascii string to decoded string
        String[] string_arr = string_in.split("_");

        for (int i = 0; i < string_arr.length; i++){
            int val = Integer.parseInt(string_arr[i]);
            string_out += (char)val;
//            Log.d("EncoderHelper", "val: " + val);
//            Log.d("Value is: " , (char)val + "");
        }
        Log.d("EncoderHelper", "string_out: " + string_out);
        return string_out.replace(" ", "");
    }
    static String encode(String string_in){
        String string_out = "";
        //convert ascii string to decoded string
        String[] string_arr = string_in.split("");

        for (int i = 0; i < string_arr.length; i++){
            int val = (int)string_arr[i].charAt(0);
            string_out += val + "_";
//            Log.d("EncoderHelper", "val: " + val);
//            Log.d("Value is: " , (char)val + "");

        }
        Log.d("EncoderHelper", "string_out: " + string_out);
        return string_out.replace(" ", "");
    }



}
