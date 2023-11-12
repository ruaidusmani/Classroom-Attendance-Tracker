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
import java.util.Map;

public class ClassItemEnrollAdapter extends RecyclerView.Adapter<ClassItemEnrollAdapter.ViewHolder>  {
    private List<Map<String, String>> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    String className;
    String classDays;
    String classStartHour;
    String classStartMinute;
    String classEndHour;
    String classEndMinute;
    String classLocation;


    // data is passed into the constructor
    ClassItemEnrollAdapter(Context context, List<Map<String, String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.class_items_enroll, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map<String, String> classMap = mData.get(position);
        holder.textViewDays.setText(classMap.get("DAYS"));
        int startHour = Integer.parseInt(classMap.get("START_HOUR"));
        int startMinute = Integer.parseInt(classMap.get("START_MIN"));
        int endHour = Integer.parseInt(classMap.get("END_HOUR"));
        int endMinute = Integer.parseInt(classMap.get("END_MIN"));
        if (startHour < 10) {
            classMap.put("START_HOUR", "0" + startHour);
        }
        if (startMinute < 10) {
            classMap.put("START_MIN", "0" + startMinute);
        }
        if (endHour < 10) {
            classMap.put("END_HOUR", "0" + endHour);
        }
        if (endMinute < 10) {
            classMap.put("END_MIN", "0" + endMinute);
        }

        holder.textViewTime.setText(classMap.get("START_HOUR") + ":" + classMap.get("START_MIN") + " - " + classMap.get("END_HOUR") + ":" + classMap.get("END_MIN"));
        holder.textViewSectionName.setText(classMap.get("CLASS_NAME"));
        holder.textViewRoomNumber.setText(classMap.get("ROOM_NUMBER"));

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewDays;
        TextView textViewTime;
        TextView textViewSectionName;
        TextView textViewRoomNumber;

        ViewHolder(View itemView) {
            super(itemView);
            textViewDays = itemView.findViewById(R.id.textViewDays);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewSectionName = itemView.findViewById(R.id.textViewSectionName);
            textViewRoomNumber = itemView.findViewById(R.id.textViewRoomNumber);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Map<String, String> getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

