package com.example.eventcalendar;

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

    @Override
    public String toString() {
        return name + "," + timeInMillis;
    }

    public static Event fromString(String eventStr) {
        String[] parts = eventStr.split(",");
        return new Event(parts[0], Long.parseLong(parts[1]));
    }
}
