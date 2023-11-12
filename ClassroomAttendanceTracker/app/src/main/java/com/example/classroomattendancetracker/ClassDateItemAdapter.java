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

public class ClassDateItemAdapter extends RecyclerView.Adapter<ClassDateItemAdapter.ViewHolder>  {
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView date; // holds day and date

        public ViewHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.textView_date_placeholder);

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

    List<ClassDateItem> classDateItem_List = new ArrayList<ClassDateItem>();
    private OnItemClickListener clickListener;

    public ClassDateItemAdapter(List<ClassDateItem> classroom_item_list, OnItemClickListener clickListener) {
        classDateItem_List.addAll(classroom_item_list); //copy items to class_item-specific array
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View classView = inflater.inflate(R.layout.classroom_items, parent, false);
        ViewHolder viewHolder = new ViewHolder(classView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassDateItem classdateitem = classDateItem_List.get(position);
        TextView date = holder.date;
        date.setText(classdateitem.getDate());
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    @Override
    public int getItemCount() {
        return classDateItem_List.size();
    }
}