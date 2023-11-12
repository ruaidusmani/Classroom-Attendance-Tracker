package com.example.classroomattendancetracker;

public class ClassRoomItem {

    private String day_date;


    ClassRoomItem() {
        day_date = "";
    }

    public ClassRoomItem(String day_date) {
        this.day_date = day_date;
    }

    public String getDay_date() {
        return day_date;
    }

    public void setDay_date(String day_date) {
        this.day_date = day_date;
    }
}
