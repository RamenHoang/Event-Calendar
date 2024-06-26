package com.example.eventcalendar;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Event {
    private String name;
    private long timeInMillis;

    public Event(String name, long timeInMillis) {
        this.name = name;
        this.timeInMillis = timeInMillis;
    }

    public String getName() {
        return name;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public String parseTimeInMillisToDateTimeFormat() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date(timeInMillis));
    }

    public String getFullName() {
        return parseTimeInMillisToDateTimeFormat() + "\n" + name;
    }

    @Override
    public String toString() {
        return name + "," + timeInMillis;
    }

    public static Event fromString(String eventStr) {
        String[] parts = eventStr.split(",");
        return new Event(parts[0], Long.parseLong(parts[1]));
    }
}
