package com.example.snare.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snare.Entities.PinnedLocations;
import com.example.snare.R;
import com.example.snare.ViewHolders.PinnedLocationsViewHolder;

import java.util.List;

public class PinnedLocationsAdapter extends RecyclerView.Adapter<PinnedLocationsViewHolder> {

    private final List<PinnedLocations> pinnedLocationsList;

    public PinnedLocationsAdapter(List<PinnedLocations> pinnedLocationsList) {
        this.pinnedLocationsList = pinnedLocationsList;
    }

    @NonNull
    @Override
    public PinnedLocationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PinnedLocationsViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.activity_pinned_locations_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PinnedLocationsViewHolder holder, int position) {
        holder.name.setText(pinnedLocationsList.get(position).getName());
        holder.coordinates.setText(pinnedLocationsList.get(position).getLat() + ", " + pinnedLocationsList.get(position).getLng());
    }

    @Override
    public int getItemCount() {
        return pinnedLocationsList.size();
    }

}
