package com.iteso.trace;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iteso.trace.beans.User;

import java.util.ArrayList;

public class AdapterMember extends RecyclerView.Adapter<AdapterMember.MemberViewHolder> {
    private ArrayList<User> members;
    private Context context;

    public AdapterMember(ArrayList<User> members, Context context) {
        this.members = members;
        this.context = context;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.member, parent, false);
        return new MemberViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        final User currentUser = members.get(position);
        holder.username.setText(currentUser.getDisplayName());
        holder.avatar.setImageResource(R.drawable.trace_icon_fore);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: SEND TO PROFILE
            }
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public ImageView avatar;
        public LinearLayout layout; // To make it clickable
        public MemberViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.channel_member_username);
            avatar = itemView.findViewById(R.id.channel_member_avatar);
            layout = itemView.findViewById(R.id.channel_member_layout);
        }
    }
}
