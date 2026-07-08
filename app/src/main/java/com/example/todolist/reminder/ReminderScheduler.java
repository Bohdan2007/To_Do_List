package com.example.todolist.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

public class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";

    public static void scheduleReminder(Context context, int noteId, String title, String content, long reminderTimeMillis, long deadlineMillis) {
        if (reminderTimeMillis <= 0) {
            cancelReminder(context, noteId);
            return;
        }

        long firstTrigger = calculateNextTrigger(reminderTimeMillis, System.currentTimeMillis());

        if (isAfterDeadlineDay(firstTrigger, deadlineMillis)) {
            Log.d(TAG, "Дедлайн noteId=" + noteId + " уже минув - нагадування не заплановано");
            cancelReminder(context, noteId);
            return;
        }

        scheduleExactAlarm(context, noteId, title, content, firstTrigger, deadlineMillis);
    }

    static void scheduleNextDay(Context context, int noteId, String title, String content, long previousTriggerMillis, long deadlineMillis) {
        long nextTrigger = addOneDay(previousTriggerMillis);

        if (isAfterDeadlineDay(nextTrigger, deadlineMillis)) {
            Log.d(TAG, "Дедлайн noteId=" + noteId + " досягнуто - нагадування більше не повторюється");
            return;
        }

        scheduleExactAlarm(context, noteId, title, content, nextTrigger, deadlineMillis);
    }

    private static void scheduleExactAlarm(Context context, int noteId, String title, String content, long triggerAtMillis, long deadlineMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager == null");
            return;
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("extra_note_id", noteId);
        intent.putExtra("extra_title", title);
        intent.putExtra("extra_content", content);
        intent.putExtra("extra_deadline", deadlineMillis);
        intent.putExtra("extra_trigger", triggerAtMillis);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                Log.w(TAG, "Немає дозволу на точні будильники, noteId=" + noteId);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }

        Log.d(TAG, "Нагадування noteId=" + noteId + " заплановано на " + new Date(triggerAtMillis));
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        return alarmManager != null && alarmManager.canScheduleExactAlarms();
    }

    private static long calculateNextTrigger(long reminderTimeMillis, long now) {
        Calendar reminderCal = Calendar.getInstance();
        reminderCal.setTimeInMillis(reminderTimeMillis);
        int hour = reminderCal.get(Calendar.HOUR_OF_DAY);
        int minute = reminderCal.get(Calendar.MINUTE);

        Calendar trigger = Calendar.getInstance();
        trigger.setTimeInMillis(now);
        trigger.set(Calendar.HOUR_OF_DAY, hour);
        trigger.set(Calendar.MINUTE, minute);
        trigger.set(Calendar.SECOND, 0);
        trigger.set(Calendar.MILLISECOND, 0);

        if (trigger.getTimeInMillis() <= now) {
            trigger.add(Calendar.DAY_OF_YEAR, 1);
        }
        return trigger.getTimeInMillis();
    }

    private static long addOneDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        return cal.getTimeInMillis();
    }

    private static boolean isAfterDeadlineDay(long triggerMillis, long deadlineMillis) {
        if (deadlineMillis <= 0) {
            return false;
        }
        Calendar triggerDay = truncateToDay(triggerMillis);
        Calendar deadlineDay = truncateToDay(deadlineMillis);
        return triggerDay.after(deadlineDay);
    }

    private static Calendar truncateToDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }
}
