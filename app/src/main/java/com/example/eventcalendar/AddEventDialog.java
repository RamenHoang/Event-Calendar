package com.example.eventcalendar;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.Calendar;

public class AddEventDialog extends Dialog {
    // Edittext Event Name
    private EditText etEventName;
    // Time picker
    private TimePicker timePicker;
    // Nut luu
    private Button btnSave;
    // Listener cho su kien show Dialog
    private AddEventDialogListener listener;
    // Event
    private Event eventToEdit;

    // Constructor
    public AddEventDialog(@NonNull Context context, AddEventDialogListener listener) {
        super(context);
        this.listener = listener;
    }

    // Constructor
    public AddEventDialog(@NonNull Context context, AddEventDialogListener listener, Event eventToEdit) {
        super(context);
        this.listener = listener;
        this.eventToEdit = eventToEdit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Khoi tao core
        super.onCreate(savedInstanceState);

        // Lien ket voi layout
        setContentView(R.layout.dialog_add_event);

        // Lien ket cac view tren man hinh
        etEventName = findViewById(R.id.etEventName);
        timePicker = findViewById(R.id.timePicker);
        btnSave = findViewById(R.id.btnSave);

        String selectedDate = MainActivity.selectedDate;
        // Split by - to get year, month, day
        String[] date = selectedDate.split("-");
        int year = Integer.parseInt(date[0]);
        int month = Integer.parseInt(date[1]);
        int day = Integer.parseInt(date[2]);

        // Kiem tra xem co event nao duoc truyen vao khong
        // Neu co thi hien thi len dialog
        if (eventToEdit != null) {
            etEventName.setText(eventToEdit.getName());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(eventToEdit.getTimeInMillis());
            timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(calendar.get(Calendar.MINUTE));

            // Set date
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1;
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        // Xu ly su kien click nut luu
        int finalYear = year;
        int finalMonth = month;
        int finalDay = day;
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lay ten event
                String eventName = etEventName.getText().toString();

                // Lay gio va phut
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, finalYear);
                calendar.set(Calendar.MONTH, finalMonth - 1);
                calendar.set(Calendar.DAY_OF_MONTH, finalDay);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);

                // Tao event moi
                Event event = new Event(eventName, calendar.getTimeInMillis());
                listener.onEventAdded(event);

                // Tat dialog
                dismiss();
            }
        });

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public interface AddEventDialogListener {
        void onEventAdded(Event event);
    }
}
