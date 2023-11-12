package com.example.classroomattendancetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classroomattendancetracker.ClassRoomItem;
import com.example.classroomattendancetracker.R;


import java.util.ArrayList;
import java.util.List;

public class ClassRoomItemAdapter extends RecyclerView.Adapter<ClassRoomItemAdapter.ViewHolder>  {
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView day_date; // holds day and date

        public ViewHolder(View itemView) {
            super(itemView);
            day_date = (TextView) itemView.findViewById(R.id.textView_day_date_placeholder);


            //initialize click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (clickListener != null && position != RecyclerView.NO_POSITION)
                        clickListener.onItemClick(getAdapterPosition());
                }
            });
        }
    }

    List<ClassRoomItem> ClassRoomItem_List = new ArrayList<ClassRoomItem>();
    private OnItemClickListener clickListener;

//    public ClassItemAdapter(List<ClassItem> class_item_list) {
//        ClassItem_List.addAll(class_item_list); //copy items to class_item-specific array
//    }

    public ClassRoomItemAdapter(List<ClassRoomItem> classroom_item_list, OnItemClickListener clickListener) {
        ClassRoomItem_List.addAll(classroom_item_list); //copy items to class_item-specific array
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View classView = inflater.inflate(R.layout.class_items, parent, false);
        ViewHolder viewHolder = new ViewHolder(classView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassRoomItem classroomitem = ClassRoomItem_List.get(position);
        TextView class_name = holder.day_date;

        class_name.setText(classroomitem.getDay_date());
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    @Override
    public int getItemCount() {
        return ClassRoomItem_List.size();
    }
}

