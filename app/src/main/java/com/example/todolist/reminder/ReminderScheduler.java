package com.example.todolist.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";

    public static void scheduleReminder(Context context, int noteId, String title, String content, long triggerAtMillis, long deadlineMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager == null, неможливо запланувати нагадування");
            return;
        }

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent);
                Log.d(TAG, "Заплановано ТОЧНЕ нагадування (є дозвіл) на " + nextTrigger + " для noteId=" + noteId);
            } else {
                // Дозволу нема - все одно ставимо неточний будильник, щоб хоч якось спрацювало,
                // але сповіщення про це потрібно показати користувачу в UI (див. MainActivity).
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent);
                Log.w(TAG, "НЕМАЄ дозволу на точні будильники! Заплановано неточне нагадування для noteId=" + noteId);
            }
        } else {
            // На Android 11 і нижче setExact працює без додаткового дозволу
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent);
            Log.d(TAG, "Заплановано точне нагадування на " + nextTrigger + " для noteId=" + noteId);
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

    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true; // на старіших версіях дозвіл не потрібен
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        return alarmManager != null && alarmManager.canScheduleExactAlarms();
    }
}
