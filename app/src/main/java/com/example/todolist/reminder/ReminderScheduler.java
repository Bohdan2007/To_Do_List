package com.example.todolist.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ReminderScheduler {
    public static void scheduleReminder(Context context, int noteId, String title, String content, long triggerAtMillis, long deadlineMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("extra_note_id", noteId);
        intent.putExtra("extra_title", title);
        intent.putExtra("extra_content", content);
        intent.putExtra("extra_deadline", deadlineMillis);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long now = System.currentTimeMillis();
        long nextTrigger = triggerAtMillis;

        while (nextTrigger < now - 60000) {
            nextTrigger += AlarmManager.INTERVAL_DAY;
        }

        try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent);
        } catch (SecurityException e) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent);
        }
    }

    public static void cancelReminder(Context context, int noteId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
}