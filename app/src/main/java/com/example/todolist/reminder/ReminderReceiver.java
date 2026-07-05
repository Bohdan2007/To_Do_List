package com.example.todolist.reminder;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.todolist.view.MainActivity;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int noteId = intent.getIntExtra("extra_note_id", 0);
        String title = intent.getStringExtra("extra_title");
        String content = intent.getStringExtra("extra_content");
        long deadlineMillis = intent.getLongExtra("extra_deadline", 0);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "simple_todo_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Сповіщення To-Do", NotificationManager.IMPORTANCE_HIGH);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, noteId, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle((title == null || title.isEmpty()) ? "Нагадування" : title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        if (manager != null) {
            manager.notify(noteId, builder.build());
        }

        if (deadlineMillis == 0 || System.currentTimeMillis() < deadlineMillis) {
            long nextTriggerTomorrow = System.currentTimeMillis() + AlarmManager.INTERVAL_DAY;
            ReminderScheduler.scheduleReminder(context, noteId, title, content, nextTriggerTomorrow, deadlineMillis);
        }
    }
}