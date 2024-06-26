package com.example.eventcalendar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private Button btnAddEvent;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList = new ArrayList<>();
    String selectedDate;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendarView);
        btnAddEvent = findViewById(R.id.btnAddEvent);
        recyclerView = findViewById(R.id.recyclerView);

        sharedPreferences = getSharedPreferences("EventPref", MODE_PRIVATE);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                loadEvents(selectedDate);
            }
        });

        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddEventDialog();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(eventAdapter);

        selectedDate = getCurrentDate();
        loadEvents(selectedDate);
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return year + "-" + (month + 1) + "-" + day;
    }

    private void showAddEventDialog() {
        AddEventDialog dialog = new AddEventDialog(this, new AddEventDialog.AddEventDialogListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onEventAdded(Event event) {
                eventList.add(event);
                eventAdapter.notifyDataSetChanged();
                saveEvents(selectedDate, eventList);
//                scheduleNotification(event);
            }
        });
        dialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadEvents(String date) {
        eventList.clear();
        Set<String> events = sharedPreferences.getStringSet(date, new HashSet<>());
        for (String eventStr : events) {
            eventList.add(Event.fromString(eventStr));
        }
        eventAdapter.notifyDataSetChanged();
    }

    void saveEvents(String date, List<Event> events) {
        Set<String> eventSet = new HashSet<>();
        for (Event event : events) {
            eventSet.add(event.toString());
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(date, eventSet);
        editor.apply();
    }

    @SuppressLint("ScheduleExactAlarm")
    void scheduleNotification(Event event) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("eventName", event.getName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(event.getTimeInMillis());

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}
