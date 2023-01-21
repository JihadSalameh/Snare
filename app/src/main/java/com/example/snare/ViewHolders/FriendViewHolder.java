package com.example.snare.ViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snare.R;

public class FriendViewHolder extends RecyclerView.ViewHolder {

    public ImageView profile;
    public TextView name;

    public FriendViewHolder(@NonNull View itemView) {
        super(itemView);

        profile = itemView.findViewById(R.id.profileImgFriend);
        name = itemView.findViewById(R.id.username);
    }
}
