package com.example.snare;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FriendMyViewHolder extends RecyclerView.ViewHolder {

    ImageView profile;
    TextView name;

    public FriendMyViewHolder(@NonNull View itemView) {
        super(itemView);

        profile = itemView.findViewById(R.id.profileImgFriend);
        name = itemView.findViewById(R.id.username);
    }
}
