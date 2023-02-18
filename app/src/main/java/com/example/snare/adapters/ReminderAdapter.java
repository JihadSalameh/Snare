package com.example.snare.adapters;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snare.Entities.Reminder;
import com.example.snare.R;
import com.example.snare.ViewHolders.ReminderViewHolder;
import com.example.snare.listeners.RemindersListeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderViewHolder> {

    private List<Reminder> reminders;
    private final RemindersListeners remindersListeners;
    private Timer timer;
    private final List<Reminder> reminderSource;

    public ReminderAdapter(List<Reminder> reminders, RemindersListeners remindersListeners) {
        this.reminders = reminders;
        this.remindersListeners = remindersListeners;
        reminderSource = reminders;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReminderViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_container_reminder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setReminder(reminders.get(position));
        holder.layoutReminder.setOnClickListener(view -> remindersListeners.onReminderClick(reminders.get(position), position));
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public void searchReminders(String searchKeyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()) {
                    reminders = reminderSource;
                } else {
                    ArrayList<Reminder> temp = new ArrayList<>();
                    for (Reminder reminder : reminderSource) {
                        if (reminder.getTitle().toLowerCase().contains(searchKeyword.toLowerCase()) ||
                                reminder.getReminderText().toLowerCase().contains(searchKeyword.toLowerCase())) {
                            temp.add(reminder);
                        }
                    }
                    reminders = temp;
                }

                new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());

            }
        }, 1);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

}
