package com.example.classroomattendancetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

public class AttendedStudentItemAdapter extends RecyclerView.Adapter<AttendedStudentItemAdapter.ViewHolder>  {
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView student_name, student_id, presence; // holds day and date

        public ViewHolder(View itemView) {
            super(itemView);
            student_name = (TextView) itemView.findViewById(R.id.textView_student_name);
            student_id = (TextView) itemView.findViewById(R.id.textView_student_id);
            presence = (TextView) itemView.findViewById(R.id.textView_presence);

        }
    }

    List<AttendedStudentItem> attendedStudentItem_List = new ArrayList<AttendedStudentItem>();

    public AttendedStudentItemAdapter(List<AttendedStudentItem> attended_student_item_list) {
        attendedStudentItem_List.addAll(attended_student_item_list); //copy items to class_item-specific array
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View classView = inflater.inflate(R.layout.attended_student_items, parent, false);
        ViewHolder viewHolder = new ViewHolder(classView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendedStudentItem attendedstudentitem = attendedStudentItem_List.get(position);
        TextView student_name = holder.student_name;
        student_name.setText(attendedstudentitem.getStudentName());
        TextView student_id = holder.student_id;
        student_id.setText(attendedstudentitem.getStudentID());
        TextView presence = holder.presence;
        if (attendedstudentitem.isPresent())
            presence.setText("Present");
        else
            presence.setText("Absent");
    }

    @Override
    public int getItemCount() {
        return attendedStudentItem_List.size();
    }
}
