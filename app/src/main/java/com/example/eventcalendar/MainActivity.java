package com.example.eventcalendar;

import static com.example.eventcalendar.NotificationReceiver.NOTIFICATION;
import static com.example.eventcalendar.NotificationReceiver.NOTIFICATION_ID;

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
import android.widget.SearchView;

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
    private List<Event> allEvents = new ArrayList<>();
    String selectedDate;
    private SharedPreferences sharedPreferences;
    static final List<Event> holidays = new ArrayList<>();
    private SearchView searchView;

    static {
        holidays.add(new Event("Tết dương lịch", getDateInMillis(1, 1)));
        holidays.add(new Event("Quốc tế phụ nữ", getDateInMillis(3, 8)));
        holidays.add(new Event("Giải phóng miền nam, thống nhất đất nước", getDateInMillis(4, 30)));
        holidays.add(new Event("Quốc tế lao động", getDateInMillis(5, 1)));
        holidays.add(new Event("Quốc khánh", getDateInMillis(9, 2)));
        holidays.add(new Event("Ngày phụ nữ việt nam", getDateInMillis(10, 20)));
        holidays.add(new Event("Ngày nhà giáo việt nam", getDateInMillis(11, 20)));
        // Add other holidays here
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendarView);
        btnAddEvent = findViewById(R.id.btnAddEvent);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);

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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchEvents(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchEvents(newText);
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                loadEvents(selectedDate);
                eventAdapter.updateEventList(eventList);
                return false;
            }
        });

        loadAllEvents();
    }

    private static long getDateInMillis(int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
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
                allEvents.add(event);
                eventAdapter.notifyDataSetChanged();
                saveEvents(selectedDate, eventList);
                scheduleNotification(event);
            }
        });
        dialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadEvents(String date) {
        eventList.clear();

        // Load holidays
        for (Event holiday : holidays) {
            if (isSameDay(holiday.getTimeInMillis(), date)) {
                eventList.add(holiday);
            }
        }

        // Load user events
        Set<String> events = sharedPreferences.getStringSet(date, new HashSet<>());
        for (String eventStr : events) {
            Event event = Event.fromString(eventStr);
            if (!isHoliday(event)) {
                eventList.add(event);
            }
        }
        eventAdapter.notifyDataSetChanged();
    }

    void saveEvents(String date, List<Event> events) {
        Set<String> eventSet = new HashSet<>();
        for (Event event : events) {
            if (!isHoliday(event)) {
                eventSet.add(event.toString());
            }
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(date, eventSet);
        editor.apply();
        loadAllEvents();
    }

    @SuppressLint("ScheduleExactAlarm")
    void scheduleNotification(Event event) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra(NOTIFICATION, event.getName());
        long notificationId = System.currentTimeMillis();
        intent.putExtra(NOTIFICATION_ID, notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) notificationId, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(event.getTimeInMillis());

        long minutes = calendar.getTimeInMillis() / 1000 / 60;
        long minutesInMilli = minutes * 60 * 1000;

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, minutesInMilli, pendingIntent);
    }

    private boolean isSameDay(long timeInMillis, String date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);

        String eventDate = (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
        String currentDate = date.substring(date.indexOf("-") + 1);
        return eventDate.equals(currentDate);
    }

    private boolean isHoliday(Event event) {
        for (Event holiday : holidays) {
            if (holiday.getName().equals(event.getName()) && holiday.getTimeInMillis() == event.getTimeInMillis()) {
                return true;
            }
        }
        return false;
    }

    private void searchEvents(String query) {
        List<Event> filteredEvents = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredEvents.add(event);
            }
        }
        eventAdapter.updateEventList(filteredEvents);
    }

    private void loadAllEvents() {
        allEvents.clear();

        // Load holidays
        allEvents.addAll(holidays);

        // Load user events for all dates
        for (String date : sharedPreferences.getAll().keySet()) {
            Set<String> events = sharedPreferences.getStringSet(date, new HashSet<>());
            for (String eventStr : events) {
                Event event = Event.fromString(eventStr);
                if (!isHoliday(event)) {
                    allEvents.add(event);
                }
            }
        }
    }
}
