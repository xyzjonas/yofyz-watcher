package com.brauer.android.yofyz;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Adapter for the list view, handling individual yoga items rendering
 */
public class YogaClassAdapter extends RecyclerView.Adapter<YogaClassAdapter.ViewHolder> {

    public static final int COLOR_GREEN = Color.rgb(195,233,145);
    public static final int COLOR_RED = Color.rgb(216,154,158);

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // List view data
    private static List<YogaClass> DATA = Collections.synchronizedList(new ArrayList<>());

    public synchronized void refreshData(List<YogaClass> yogaClasses) {
        DATA.clear();
        DATA.addAll(yogaClasses);
        this.notifyDataSetChanged();
    }

    public synchronized void addData(List<YogaClass> yogaClasses) {
        DATA.removeAll(yogaClasses);
        yogaClasses.forEach(c -> c.setN3w(true));
        DATA.addAll(yogaClasses);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.list_item, viewGroup, false);

        ViewHolder vh = new ViewHolder(layout);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.updateItem(DATA.get(i));
    }

    @Override
    public int getItemCount() {
        return DATA.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView date;
        private TextView name;
        private TextView instructor;
        private TextView capacity;


        public ViewHolder(@NonNull LinearLayout layout) {
            super(layout);
            this.date = (TextView) layout.findViewById(R.id.item_date);
            this.name = (TextView) layout.findViewById(R.id.item_name);
            this.instructor = (TextView) layout.findViewById(R.id.item_instructor);
            this.capacity = (TextView) layout.findViewById(R.id.capacity);
        }

        public synchronized void updateItem(YogaClass item) {
            String d = item.isNew() ? "(NEW) " + DATE_FORMAT.format(item.getDate()) : DATE_FORMAT.format(item.getDate());
            date.setText(d);

            String n = item.getName();
            name.setText(n);

            if (item.hasFreeSlots()) {
                name.setTextColor(COLOR_GREEN);
                if (item.getFree() >= 2) {
                    String cap = item.getFree() + " free slots (or more)";
                    capacity.setText(cap);
                } else {
                    capacity.setText(item.getFree() + " free slots");
                }
            } else {
                name.setTextColor(COLOR_RED);
                if (item.getSubs() != 0) {
                    capacity.setText(item.getSubs() + " substitutes slots left");
                }
            }
            instructor.setText(item.getInstructor());
        }
    }
}
