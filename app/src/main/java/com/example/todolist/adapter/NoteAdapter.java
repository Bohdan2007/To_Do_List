package com.example.todolist.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.R;
import com.example.todolist.model.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    public interface OnNoteActionListener {
        void onEditClick(Note note);
        void onDeleteClick(Note note);
    }

    private final List<Note> notes = new ArrayList<>();
    private final OnNoteActionListener listener;

    public NoteAdapter(OnNoteActionListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<Note> newNotes) {
        notes.clear();
        if (newNotes != null) {
            notes.addAll(newNotes);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.bind(notes.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleView;
        private final TextView contentView;
        private final TextView reminderView;
        private final TextView deadlineView; // Додано змінну для дедлайну
        private final View editButton;
        private final View deleteButton;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.tvTitle);
            contentView = itemView.findViewById(R.id.tvContent);
            reminderView = itemView.findViewById(R.id.tvReminder);
            deadlineView = itemView.findViewById(R.id.tvDeadline); // Ініціалізація
            editButton = itemView.findViewById(R.id.btnEdit);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }

        void bind(Note note, OnNoteActionListener listener) {
            titleView.setText(note.getTitle());
            contentView.setText(note.getContent());

            // 1. Перевірка нагадування
            if (note.isReminderEnabled() && note.getReminderTimeMillis() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", new Locale("uk"));
                reminderView.setVisibility(View.VISIBLE);
                reminderView.setText("Нагадування о " + sdf.format(note.getReminderTimeMillis()));
            } else {
                reminderView.setVisibility(View.GONE);
            }

            // 2. Перевірка дедлайну
            if (note.getDeadlineMillis() > 0) {
                // Для дедлайну зазвичай потрібна і дата, і час
                SimpleDateFormat deadlineSdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", new Locale("uk"));
                deadlineView.setVisibility(View.VISIBLE);
                deadlineView.setText("Дедлайн: " + deadlineSdf.format(note.getDeadlineMillis()));
            } else {
                deadlineView.setVisibility(View.GONE);
            }

            editButton.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(note);
            });
            deleteButton.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(note);
            });
        }
    }
}