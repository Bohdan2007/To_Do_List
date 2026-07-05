package com.example.todolist.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "reminder_time_millis")
    private long reminderTimeMillis;

    @ColumnInfo(name = "reminder_enabled")
    private boolean reminderEnabled;

    @ColumnInfo(name = "deadline_millis")
    private long deadlineMillis;

    public Note(@NonNull String title, String content, long reminderTimeMillis, boolean reminderEnabled, long deadlineMillis) {
        this.title = title;
        this.content = content;
        this.reminderTimeMillis = reminderTimeMillis;
        this.reminderEnabled = reminderEnabled;
        this.deadlineMillis = deadlineMillis;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getTitle() { return title; }
    public void setTitle(@NonNull String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getReminderTimeMillis() { return reminderTimeMillis; }
    public void setReminderTimeMillis(long reminderTimeMillis) { this.reminderTimeMillis = reminderTimeMillis; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }

    public long getDeadlineMillis() { return deadlineMillis; }
    public void setDeadlineMillis(long deadlineMillis) { this.deadlineMillis = deadlineMillis; }
}