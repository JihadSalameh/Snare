package com.example.snare.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snare.Entities.Notifications;
import com.example.snare.R;
import com.example.snare.ViewHolders.NotificationsViewHolder;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsViewHolder> {

    private final List<Notifications> notificationsList;

    public NotificationsAdapter(List<Notifications> notificationsList) {
        this.notificationsList = notificationsList;
    }

    @NonNull
    @Override
    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotificationsViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.activity_notifications_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsViewHolder holder, int position) {
        holder.title1.setText(notificationsList.get(position).getTitle());
        holder.message1.setText(notificationsList.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

}
