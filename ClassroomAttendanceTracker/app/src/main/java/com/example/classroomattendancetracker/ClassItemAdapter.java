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

public class ClassItemAdapter extends RecyclerView.Adapter<ClassItemAdapter.ViewHolder>  {
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView class_name; // holds class name
        public TextView room_number; // holds the room number
        public ViewHolder(View itemView) {
            super(itemView);
            class_name = (TextView) itemView.findViewById(R.id.class_name);
            room_number = (TextView) itemView.findViewById(R.id.room_number);

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

    List<ClassItem> ClassItem_List = new ArrayList<ClassItem>();
    private OnItemClickListener clickListener;

    public ClassItemAdapter(List<ClassItem> class_item_list, OnItemClickListener clickListener) {
        ClassItem_List.addAll(class_item_list); //copy items to class_item-specific array
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
        ClassItem classitem = ClassItem_List.get(position);
        TextView class_name = holder.class_name;
        TextView room_number = holder.room_number;

        class_name.setText(classitem.getClass_name());
        room_number.setText(classitem.getRoom_number());
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    @Override
    public int getItemCount() {
        return ClassItem_List.size();
    }
}

