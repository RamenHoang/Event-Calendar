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

    // Bien luu tru thong tin cua lich
    private CalendarView calendarView;
    // Bien lien ket voi button Add Event
    private Button btnAddEvent;
    // Bien lien ket voi RecyclerView
    private RecyclerView recyclerView;
    // Bien luu tru danh sach cac su kien
    private EventAdapter eventAdapter;
    // Bien luu tru danh sach cac su kien ma nguoi dung them vao
    private List<Event> eventList = new ArrayList<>();
    // Bien luu tru tat ca su kien, bao gom su kien cua nguoi dung va cac ngay le viet nam
    private List<Event> allEvents = new ArrayList<>();
    // Bien luu ngay duoc tron tren lich
    static String selectedDate;
    // Bien kho luu tru thong tin ung dung
    private SharedPreferences sharedPreferences;
    // Bien luu tru ngay le viet nam
    static final List<Event> holidays = new ArrayList<>();
    // Bien lien ket voi SearchView
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
        // Khoi tao phan core ung dung
        super.onCreate(savedInstanceState);
        
        // Hien thi giao dien cua ung dung
        setContentView(R.layout.activity_main);

        // Lien ket cac bien voi cac view tuong ung
        calendarView = findViewById(R.id.calendarView);
        btnAddEvent = findViewById(R.id.btnAddEvent);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);

        // Khoi tao kho luu tru
        sharedPreferences = getSharedPreferences("EventPref", MODE_PRIVATE);

        // Xu ly hien thi holidays, events theo ngay duoc chon
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth; // 2024-06-27
                
                // Tai events cua ngay duoc chon 
                loadEvents(selectedDate);
            }
        });

        // Xy ly khi nhan nut Add Event
        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hien thi dialog add event
                showAddEventDialog();
            }
        });

        // Khoi tao RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(eventAdapter);

        // Tu dong chon ngay hien tai, va hien thi event cua ngay hien tai
        selectedDate = getCurrentDate();
        loadEvents(selectedDate);

        // Xu ly tim kiem su kien
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // Tim kien khi nhan nut tim tren ban phim
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchEvents(query);
                return true;
            }

            // Tim kiem khi thay doi noi dung tren thanh tim kiem
            @Override
            public boolean onQueryTextChange(String newText) {
                searchEvents(newText);
                return true;
            }
        });

        // Xu ly khi dong thanh tim kiem
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // Hien thi events cua ngay duoc chon truoc do
                loadEvents(selectedDate);
                eventAdapter.updateEventList(eventList);
                return false;
            }
        });

        // Tai tat ca events
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
        // Hien thi dialog them, sua event
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
        // Tim trong danh sach holidays, ngay nao trung voi ngay duoc chon, se them vao eventList
        for (Event holiday : holidays) {
            if (isSameDay(holiday.getTimeInMillis(), date)) {
                eventList.add(holiday);
            }
        }

        // Load user events
        // Lay tu kho du lieu, neu ngay nao trung voi ngay duoc chon, se them vao eventList
        Set<String> events = sharedPreferences.getStringSet(date, new HashSet<>());
        for (String eventStr : events) {
            Event event = Event.fromString(eventStr);
            if (!isHoliday(event)) {
                eventList.add(event);
            }
        }
        
        // Thong bao toi eventAdapter, cap nhat du lieu moi tu eventList ra view
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
        // Tao intent de gui thong bao
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra(NOTIFICATION, event.getName());
        long notificationId = System.currentTimeMillis();
        intent.putExtra(NOTIFICATION_ID, notificationId);

        // Tao pendingIntent de gui thong bao
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) notificationId, intent, PendingIntent.FLAG_IMMUTABLE);

        // Tao alarmManager de gui thong bao
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(event.getTimeInMillis());

        long minutes = calendar.getTimeInMillis() / 1000 / 60;
        long minutesInMilli = minutes * 60 * 1000;

        // Gui thong bao vao thoi diem event xay ra
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

    // Method tim kiem event
    private void searchEvents(String query) {
        // Khoi tao danh dach event duoc tim kiem
        List<Event> filteredEvents = new ArrayList<>();

        // Tim kiem trong allEvents
        for (Event event : allEvents) {
            // Neu ten event co chua noi dung tim kiem
            if (event.getName().toLowerCase().contains(query.toLowerCase())) {
                // Them vao danh sach event duoc tim kiem
                filteredEvents.add(event);
            }
        }

        // Ra lenh cho eventAdapter cap nhat thong tren view
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
