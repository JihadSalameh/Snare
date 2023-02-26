package com.example.snare.adapters;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snare.Entities.PinnedLocations;
import com.example.snare.R;
import com.example.snare.listeners.PinnedLocationListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class ViewPinnedAdapter extends RecyclerView.Adapter<ViewPinnedAdapter.PinnedViewHolder> {

    private List<PinnedLocations> pinnedLocations;
    private final PinnedLocationListener pinnedLocationListener;

    public ViewPinnedAdapter(List<PinnedLocations> pinnedLocations, PinnedLocationListener pinnedLocationListener) {
       this.pinnedLocationListener = pinnedLocationListener;
       this.pinnedLocations = pinnedLocations;
    }

    @NonNull
    @Override
    public PinnedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PinnedViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_container_pinned, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PinnedViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setPinned(pinnedLocations.get(position));
        holder.layoutPinned.setOnClickListener(view ->
                pinnedLocationListener.onPinnedClick(pinnedLocations.get(position), position));
    }

    @Override
    public int getItemCount() {
        return pinnedLocations.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    static class PinnedViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout layoutPinned;
        private final TextView namePinned;
        private final RoundedImageView pinnedItem;

        public PinnedViewHolder(@NonNull View itemView) {
            super(itemView);
            namePinned = itemView.findViewById(R.id.namePinned);
            layoutPinned = itemView.findViewById(R.id.layoutPinned);
            pinnedItem = itemView.findViewById(R.id.pinnedItem);
        }

        void setPinned(PinnedLocations pinnedLocations) {
            namePinned.setText(pinnedLocations.getName());
            GradientDrawable gradientDrawable = (GradientDrawable) layoutPinned.getBackground();
        }
    }

}
