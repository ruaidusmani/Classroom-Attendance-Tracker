package com.example.classroomattendancetracker;

public class AttendedStudentItem {

    private String studentName;
    private String studentID;
    boolean isPresent;

    AttendedStudentItem() {
        studentName = "";
        studentID = "";
        isPresent = false;
    }

    public AttendedStudentItem(String studentName, String studentID, boolean isPresent) {
        this.studentName = studentName;
        this.studentID = studentID;
        this.isPresent = isPresent;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void setPresent(boolean present) {
        isPresent = present;
    }


}
