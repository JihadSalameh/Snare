package com.example.snare.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snare.R;

public class PinnedLocationsViewHolder extends RecyclerView.ViewHolder{

    public TextView name;
    public TextView coordinates;

    public PinnedLocationsViewHolder(@NonNull View itemView) {
        super(itemView);

        name = itemView.findViewById(R.id.name);
        coordinates = itemView.findViewById(R.id.lat_lng);
    }

}
