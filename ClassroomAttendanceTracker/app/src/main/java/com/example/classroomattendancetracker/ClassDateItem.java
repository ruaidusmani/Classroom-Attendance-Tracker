package com.example.classroomattendancetracker;

public class ClassDateItem {
    private String date;

    ClassDateItem() {
        date = "";
    }

    public ClassDateItem(String day_date) {
        this.date = day_date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String day_date) {
        this.date = day_date;
    }
}