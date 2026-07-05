package com.example.todolist.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

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

        if (ThemeManager.isDarkTheme(this)) {
            btnThemeToggle.setText("☀️");
        } else {
            btnThemeToggle.setText("🌙");
        }

        btnThemeToggle.setOnClickListener(v -> ThemeManager.toggleTheme(this));

        RecyclerView recyclerView = findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAddNote);
        fabAdd.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EditNoteActivity.class)));

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, notes -> adapter.setNotes(notes));

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