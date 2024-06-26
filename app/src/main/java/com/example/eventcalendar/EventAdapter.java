package com.example.eventcalendar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private Context context;

    public EventAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Event event = eventList.get(position);
        holder.tvEventName.setText(event.getFullName());

        if (isHoliday(event)) {
            holder.tvEventName.setTextColor(context.getResources().getColor(R.color.red, null));
        } else {
            holder.tvEventName.setTextColor(context.getResources().getColor(R.color.black, null));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDeleteDialog(event, position);
            }
        });
    }

    private void showEditDeleteDialog(Event event, int position) {
        if (isHoliday(event)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit or Delete Event")
                .setMessage("Do you want to edit or delete event? \n" + event.getFullName())
                .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showEditEventDialog(event, position);
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eventList.remove(position);
                        notifyDataSetChanged();
                        ((MainActivity) context).saveEvents(((MainActivity) context).selectedDate, eventList);
                    }
                })
                .create()
                .show();
    }

    private void showEditEventDialog(Event event, int position) {
        AddEventDialog dialog = new AddEventDialog(context, new AddEventDialog.AddEventDialogListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onEventAdded(Event newEvent) {
                eventList.set(position, newEvent);
                notifyDataSetChanged();
                ((MainActivity) context).saveEvents(((MainActivity) context).selectedDate, eventList);
                ((MainActivity) context).scheduleNotification(newEvent);
            }
        }, event);
        dialog.show();
    }

    private boolean isHoliday(Event event) {
        for (Event holiday : MainActivity.holidays) {
            if (holiday.getName().equals(event.getName()) && holiday.getTimeInMillis() == event.getTimeInMillis()) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateEventList(List<Event> filteredEvents) {
        this.eventList = filteredEvents;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
        }
    }
}
