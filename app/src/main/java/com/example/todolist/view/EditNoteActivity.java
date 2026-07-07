package com.example.todolist.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.todolist.R;
import com.example.todolist.model.Note;
import com.example.todolist.model.NoteDao;
import com.example.todolist.model.NoteDatabase;
import com.example.todolist.reminder.ReminderScheduler;
import com.example.todolist.thememanager.ThemeManager;
import com.example.todolist.viewmodel.NoteViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditNoteActivity extends AppCompatActivity {

    private EditText etTitle;
    private EditText etContent;
    private EditText etDeadlineDate;
    private EditText etReminderTime;
    private Button btnSave;

    private NoteViewModel noteViewModel;
    private ExecutorService executorService;

    private Note existingNote;

    private long selectedDeadlineMillis = 0;
    private long selectedReminderMillis = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        android.widget.TextView btnThemeToggleEdit = findViewById(R.id.btnThemeToggleEdit);
        if (btnThemeToggleEdit != null) {
            if (ThemeManager.isDarkTheme(this)) {
                btnThemeToggleEdit.setText("☀️");
            } else {
                btnThemeToggleEdit.setText("🌙");
            }

            btnThemeToggleEdit.setOnClickListener(v -> ThemeManager.toggleTheme(this));
        }

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        etDeadlineDate = findViewById(R.id.etDeadlineDate);
        etReminderTime = findViewById(R.id.etReminderTime);
        btnSave = findViewById(R.id.btnSave);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        executorService = Executors.newSingleThreadExecutor();

        etDeadlineDate.setOnClickListener(v -> showDatePicker());
        etReminderTime.setOnClickListener(v -> showTimePicker());

        int noteId = getIntent().getIntExtra(MainActivity.EXTRA_NOTE_ID, -1);
        if (noteId != -1) {
            loadNote(noteId);
        } else {
            updateDateTimeFields();
        }

        btnSave.setOnClickListener(v -> saveNote());
    }

    private void loadNote(int noteId) {
        executorService.execute(() -> {
            NoteDao dao = NoteDatabase.getInstance(getApplicationContext()).noteDao();
            Note note = dao.getNoteById(noteId);
            runOnUiThread(() -> {
                if (note != null) {
                    existingNote = note;
                    etTitle.setText(note.getTitle());
                    etContent.setText(note.getContent());

                    selectedDeadlineMillis = note.getDeadlineMillis();
                    selectedReminderMillis = note.getReminderTimeMillis();

                    updateDateTimeFields();
                }
            });
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDeadlineMillis > 0) {
            calendar.setTimeInMillis(selectedDeadlineMillis);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    selectedDeadlineMillis = calendar.getTimeInMillis();
                    updateDateTimeFields();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Очистити", (d, which) -> {
            selectedDeadlineMillis = 0;
            updateDateTimeFields();
        });

        dialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedReminderMillis > 0) {
            calendar.setTimeInMillis(selectedReminderMillis);
        }

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    if (selectedDeadlineMillis > 0) {
                        calendar.setTimeInMillis(selectedDeadlineMillis);
                    } else {
                        calendar.setTimeInMillis(System.currentTimeMillis());
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    selectedReminderMillis = calendar.getTimeInMillis();
                    updateDateTimeFields();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Очистити", (d, which) -> {
            selectedReminderMillis = 0;
            updateDateTimeFields();
        });

        dialog.show();
    }

    private void updateDateTimeFields() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        if (selectedDeadlineMillis > 0) {
            etDeadlineDate.setText(dateFormat.format(selectedDeadlineMillis));
        } else {
            etDeadlineDate.setText("");
            etDeadlineDate.setHint("Дедлайн не встановлено");
        }

        if (selectedReminderMillis > 0) {
            etReminderTime.setText(timeFormat.format(selectedReminderMillis));
        } else {
            etReminderTime.setText("");
            etReminderTime.setHint("Без нагадування");
        }
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Введіть заголовок");
            return;
        }

        boolean reminderEnabled = (selectedReminderMillis > 0);

        if (existingNote != null) {
            existingNote.setTitle(title);
            existingNote.setContent(content);
            existingNote.setReminderTimeMillis(selectedReminderMillis);
            existingNote.setReminderEnabled(reminderEnabled);
            existingNote.setDeadlineMillis(selectedDeadlineMillis);

            noteViewModel.update(existingNote);
            scheduleOrCancelReminder(existingNote.getId(), title, content, selectedReminderMillis, reminderEnabled, selectedDeadlineMillis);
            Toast.makeText(this, "Нотатку оновлено", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Note newNote = new Note(title, content, selectedReminderMillis, reminderEnabled, selectedDeadlineMillis);
            noteViewModel.insert(newNote, id ->
                    scheduleOrCancelReminder((int) id, title, content, selectedReminderMillis, reminderEnabled, selectedDeadlineMillis)
            );
            Toast.makeText(this, "Нотатку створено", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void scheduleOrCancelReminder(int noteId, String title, String content, long reminderMillis, boolean reminderEnabled, long deadlineMillis) {
        if (reminderEnabled) {
            ReminderScheduler.scheduleReminder(getApplicationContext(), noteId, title, content, reminderMillis, deadlineMillis);
        } else {
            ReminderScheduler.cancelReminder(getApplicationContext(), noteId);
        }
    }
}