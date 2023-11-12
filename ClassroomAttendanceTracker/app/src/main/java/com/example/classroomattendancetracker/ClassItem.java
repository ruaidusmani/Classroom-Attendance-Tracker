package com.example.classroomattendancetracker;

public class ClassItem {
    private String class_name, room_number;

    ClassItem() {
        class_name = "";
        room_number = "";
    }

    public ClassItem(String class_name, String room_number) {
        this.class_name = class_name;
        this.room_number = room_number;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public String getRoom_number() {
        return room_number;
    }

    public void setRoom_number(String room_number) {
        this.room_number = room_number;
    }
}