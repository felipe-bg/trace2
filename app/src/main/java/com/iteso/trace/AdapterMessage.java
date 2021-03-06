package com.iteso.trace;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.iteso.trace.beans.Message;
import com.iteso.trace.beans.MessageName;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.iteso.trace.utils.Constants.USER_ID;

public class AdapterMessage extends RecyclerView.Adapter<AdapterMessage.MessageViewHolder> {
    // RecyclerView info: https://developer.android.com/guide/topics/ui/layout/recyclerview
    private ArrayList<MessageName> messages;
    private Context context; // Needed for intents to other activities

    public AdapterMessage(ArrayList<MessageName> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate a View for the item
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message, parent, false);
        return new MessageViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        final MessageName currentMessage = messages.get(position);
        holder.messageText.setText(currentMessage.getMessage());
        // Parse date
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", new Locale("us"));
        Date messageDate;
        try {
            messageDate = sdf.parse(currentMessage.getTimestamp());
        } catch (ParseException e) {
            messageDate = new Date();
        }
        holder.timestampText.setText(DateFormat.getDateInstance().format(messageDate));
        holder.usernameText.setText(currentMessage.getUsername());
        Uri uri = Uri.parse("https://s3.amazonaws.com/cc-698969-mobile/avatars/cat.png");
        holder.avatar.setImageURI(uri);

        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ActivityUserProfile.class);
                intent.putExtra(USER_ID, currentMessage.getUserUid());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // Each view holder is in charge of displaying a single item with a view.
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public TextView timestampText;
        public TextView usernameText;
        public SimpleDraweeView avatar;
        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timestampText = itemView.findViewById(R.id.message_timestamp);
            usernameText = itemView.findViewById(R.id.message_username);
            avatar = itemView.findViewById(R.id.message_avatar);
        }
    }
}
