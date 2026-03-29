package com.tngoc.familytaskapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<User> members = new ArrayList<>();

    public void setMembers(List<User> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User user = members.get(position);
        
        String name = user.getDisplayName();
        if (name == null || name.isEmpty()) {
            name = user.getEmail();
        }
        holder.tvMemberName.setText(name);

        // Load avatar using Glide
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.ic_user_default)
                    .circleCrop()
                    .into(holder.ivMemberAvatar);
        } else {
            holder.ivMemberAvatar.setImageResource(R.drawable.ic_user_default);
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName;
        ImageView ivMemberAvatar;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            ivMemberAvatar = itemView.findViewById(R.id.ivMemberAvatar);
        }
    }
}
