package com.example.snare.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snare.R;

public class NotificationsViewHolder extends RecyclerView.ViewHolder{

    public TextView title1;
    public TextView message1;

    public NotificationsViewHolder(@NonNull View itemView) {
        super(itemView);

        title1 = itemView.findViewById(R.id.title);
        message1 = itemView.findViewById(R.id.message);
    }

}
