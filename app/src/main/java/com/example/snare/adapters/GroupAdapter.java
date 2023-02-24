package com.example.snare.adapters;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.snare.Entities.Friends;
import com.example.snare.Entities.WrappingFriends;
import com.example.snare.R;
import com.example.snare.listeners.GroupListeners;
import com.makeramen.roundedimageview.RoundedImageView;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<WrappingFriends> friends;
    private final GroupListeners groupListeners;

    public GroupAdapter(List<WrappingFriends> friends, GroupListeners groupListeners) {
        this.friends = friends;
        this.groupListeners = groupListeners;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_container_friend, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setFriend(friends.get(position).getFriends());
        holder.layoutGroup.setOnClickListener(view -> groupListeners.onFriendClick(friends.get(position), position));
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    static class GroupViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout layoutGroup;
        private final TextView name;
        private ImageView profilePicture;
        private final RoundedImageView friendItem;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            layoutGroup = itemView.findViewById(R.id.layoutGroup);
            friendItem = itemView.findViewById(R.id.friendItem);
            profilePicture = itemView.findViewById(R.id.profilePicture);
        }

        void setFriend(Friends friends) {
            name.setText(friends.getName());
            Glide.with(itemView)
                    .load(friends.getProfilePic())
                    .apply(RequestOptions.bitmapTransform(new CenterCrop()))
                    .into(profilePicture);

            GradientDrawable gradientDrawable = (GradientDrawable) layoutGroup.getBackground();
        }
    }

}
