package com.example.eventcalendar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notificationId";
    public static String NOTIFICATION = "notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        String eventName = intent.getStringExtra(NOTIFICATION);
        int notificationId = (int) intent.getLongExtra(NOTIFICATION_ID, 0);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "eventChannel",
                    "Event Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "eventChannel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Event Reminder")
                .setContentText("Event: " + eventName)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(notificationId, builder.build());
    }
}
