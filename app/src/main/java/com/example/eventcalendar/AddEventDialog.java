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

import java.util.Calendar;

public class AddEventDialog extends Dialog {

    private EditText etEventName;
    private TimePicker timePicker;
    private Button btnSave;
    private AddEventDialogListener listener;
    private Event eventToEdit;

    public AddEventDialog(@NonNull Context context, AddEventDialogListener listener) {
        super(context);
        this.listener = listener;
    }

    public AddEventDialog(@NonNull Context context, AddEventDialogListener listener, Event eventToEdit) {
        super(context);
        this.listener = listener;
        this.eventToEdit = eventToEdit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_event);

        etEventName = findViewById(R.id.etEventName);
        timePicker = findViewById(R.id.timePicker);
        btnSave = findViewById(R.id.btnSave);

        if (eventToEdit != null) {
            etEventName.setText(eventToEdit.getName());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(eventToEdit.getTimeInMillis());
            timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(calendar.get(Calendar.MINUTE));
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventName = etEventName.getText().toString();
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);

                Event event = new Event(eventName, calendar.getTimeInMillis());
                listener.onEventAdded(event);
                dismiss();
            }
        });

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public interface AddEventDialogListener {
        void onEventAdded(Event event);
    }
}
