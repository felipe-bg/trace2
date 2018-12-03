package com.iteso.trace;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.iteso.trace.beans.Channel;
import com.iteso.trace.beans.Message;

import java.util.Date;

import static com.iteso.trace.utils.Constants.CONVERSATION_ID;
import static com.iteso.trace.utils.Constants.DB_CHANNELS;
import static com.iteso.trace.utils.Constants.DB_CHATS;
import static com.iteso.trace.utils.Constants.DB_MEMBERS;
import static com.iteso.trace.utils.Constants.DB_MESSAGES;
import static com.iteso.trace.utils.Constants.DB_TAGS;
import static com.iteso.trace.utils.Constants.DB_USERS;
import static com.iteso.trace.utils.Constants.DEFAULT_TAG;
import static com.iteso.trace.utils.Constants.DEFAULT_USER_UID;

public class ActivityCreateChannel extends AppCompatActivity {
    /**
     * Application database and user instances.
     */
    private FirebaseDatabase appDatabase;
    private FirebaseUser loggedUser;
    /**
     * Current conversation id. Defines the members to load.
     */
    private String conversationId;
    /**
     * Input text area
     */
    private EditText nameInput;
    private EditText descriptionInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_channel);
        appDatabase = FirebaseDatabase.getInstance();
        conversationId = getIntent().getExtras().getString(CONVERSATION_ID);
        loggedUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get EditText fields
        nameInput = findViewById(R.id.add_channel_name);
        descriptionInput = findViewById(R.id.add_channel_description);
    }


    public void createChannel(View v) {
        // Create channel and to to the newly created channel
        Channel newChannel = new Channel();
        newChannel.setName(nameInput.getText().toString());
        newChannel.setDescription(descriptionInput.getText().toString());
        newChannel.getTags().put(DEFAULT_TAG, true);
        // Get new channel id
        DatabaseReference newChannelRef = appDatabase.getReference(DB_CHANNELS).push();
        // Store the channel in the database
        newChannelRef.setValue(newChannel);
        // Add channel to tags list
        appDatabase.getReference(DB_TAGS).child(DEFAULT_TAG)
                .child(newChannelRef.getKey()).setValue(true);
        // Add welcome message to the newly created channel
        Message welcomeMessage = new Message();
        welcomeMessage.setMessage(getResources().getString(R.string.welcome_message));
        welcomeMessage.setTimestamp(new Date().toString());
        welcomeMessage.setUserUid(DEFAULT_USER_UID);
        // Get new message reference
        DatabaseReference welcomeMsgRef = appDatabase.getReference(DB_MESSAGES)
                .child(newChannelRef.getKey()).push();
        // Add message to database
        welcomeMsgRef.setValue(welcomeMessage);
        // Add user to channel's member list
        appDatabase.getReference(DB_MEMBERS).child(newChannelRef.getKey())
                .child(loggedUser.getUid())
                .setValue(true);
        // Add channel to user list
        appDatabase.getReference(DB_USERS).child(loggedUser.getUid()).child(DB_CHANNELS)
                .child(newChannelRef.getKey()).setValue(true);
        // Go to the new channel
        Intent intent = new Intent(ActivityCreateChannel.this, ActivityMain.class);
        intent.putExtra(CONVERSATION_ID, newChannelRef.getKey());
        startActivity(intent);
        finish(); // Deletes this from stack so it is not shown id user goes back
    }
}
