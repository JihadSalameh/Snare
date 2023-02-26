package com.example.snare.adapters;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snare.Entities.Group;
import com.example.snare.R;
import com.example.snare.listeners.GroupListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groups;
    private List<Group> groupsSource;
    private final GroupListener groupListener;

    public GroupAdapter(List<Group> groups, GroupListener groupListener) {
        this.groups = groups;
        this.groupListener = groupListener;
        this.groupsSource = groups;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_container_group, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setGroup(groups.get(position));
        holder.layoutViewGroup.setOnClickListener(view -> groupListener.onGroupClick(groups.get(position), position));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void searchGroups(String searchKeyword) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()) {
                    groups = groupsSource;
                } else {
                    ArrayList<Group> temp = new ArrayList<>();
                    for (Group group : groupsSource) {
                        if (group.getName().toLowerCase().contains(searchKeyword.toLowerCase())){
                            temp.add(group);
                        }
                    }
                    groups = temp;
                }

                new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());

            }
        }, 500);
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout layoutViewGroup;
        private final TextView groupName;
        private final RoundedImageView groupItem;
        private final TextView count;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.groupName);
            layoutViewGroup = itemView.findViewById(R.id.layoutViewGroup);
            groupItem = itemView.findViewById(R.id.groupItem);
            count = itemView.findViewById(R.id.count);
        }

        void setGroup(Group group) {
            groupName.setText(group.getName());
            if (group.getImagePath() != null) {
                groupItem.setImageBitmap(BitmapFactory.decodeFile(group.getImagePath()));
                groupItem.setVisibility(View.VISIBLE);
            } else {
                groupItem.setVisibility(View.GONE);
            }
            if(group.getGroupMembers() != null){
                count.setText(group.getGroupMembers().size()+"");
            }
            GradientDrawable gradientDrawable = (GradientDrawable) layoutViewGroup.getBackground();
        }
    }

}
