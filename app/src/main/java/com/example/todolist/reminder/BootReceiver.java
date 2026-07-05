package com.example.todolist.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.todolist.model.Note;
import com.example.todolist.model.NoteDao;
import com.example.todolist.model.NoteDatabase;

import java.util.List;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        Context appContext = context.getApplicationContext();

        Executors.newSingleThreadExecutor().execute(() -> {
            NoteDao dao = NoteDatabase.getInstance(appContext).noteDao();
            List<Note> notes = dao.getAllNotesSync();
            if (notes == null) return;

            for (Note note : notes) {
                if (note.isReminderEnabled()) {
                    ReminderScheduler.scheduleReminder(
                            appContext,
                            note.getId(),
                            note.getTitle(),
                            note.getContent(),
                            note.getReminderTimeMillis(),
                            note.getDeadlineMillis() // Передаємо дедлайн
                    );
                }
            }
        });
    }
}