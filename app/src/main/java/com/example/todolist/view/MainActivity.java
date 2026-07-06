package com.example.todolist.view;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.R;
import com.example.todolist.adapter.NoteAdapter;
import com.example.todolist.model.Note;
import com.example.todolist.reminder.ReminderScheduler;
import com.example.todolist.thememanager.ThemeManager;
import com.example.todolist.viewmodel.NoteViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteActionListener {

    public static final String EXTRA_NOTE_ID = "note_id";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;

    private NoteViewModel noteViewModel;
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        android.widget.TextView btnThemeToggle = findViewById(R.id.btnThemeToggle);
        if (btnThemeToggle != null) {
            if (ThemeManager.isDarkTheme(this)) {
                btnThemeToggle.setText("☀️");
            } else {
                btnThemeToggle.setText("🌙");
            }
            btnThemeToggle.setOnClickListener(v -> ThemeManager.toggleTheme(this));
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAddNote);
        fabAdd.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EditNoteActivity.class)));

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, notes -> adapter.setNotes(notes));

        requestNotificationPermissionIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkExactAlarmPermission();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                        this,
                        "Без дозволу на сповіщення нагадування не будуть показані. Увімкніть його в Налаштуваннях.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!ReminderScheduler.canScheduleExactAlarms(this)) {
                showExactAlarmPermissionDialog();
            }
        }
    }

    private void showExactAlarmPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Потрібен дозвіл")
                .setMessage("Щоб нагадування приходили точно у вказаний час, дозвольте застосунку "
                        + "ставити точні будильники в системних налаштуваннях.")
                .setPositiveButton("Відкрити налаштування", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Пізніше", null)
                .setCancelable(true)
                .show();
    }

    @Override
    public void onEditClick(Note note) {
        Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra(EXTRA_NOTE_ID, note.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Note note) {
        noteViewModel.delete(note);
        ReminderScheduler.cancelReminder(this, note.getId());
        Toast.makeText(this, "Нотатку видалено", Toast.LENGTH_SHORT).show();
    }
}
