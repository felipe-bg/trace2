package com.iteso.trace;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.iteso.trace.beans.User;

import java.util.HashMap;

import static com.iteso.trace.utils.Constants.CONVERSATION_ID;
import static com.iteso.trace.utils.Constants.DB_CHANNELS;
import static com.iteso.trace.utils.Constants.DB_CHATS;
import static com.iteso.trace.utils.Constants.DB_MEMBERS;
import static com.iteso.trace.utils.Constants.DB_USERS;
import static com.iteso.trace.utils.Constants.USER_ID;

public class ActivityUserProfile extends AppCompatActivity {

    /**
     * Application database and user instances.
     */
    private FirebaseDatabase appDatabase;
    private FirebaseUser loggedUser;
    private String userUid;
    private User currentUser;

    Button sendDirectMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        // Get user id from intent
        userUid = getIntent().getExtras().getString(USER_ID);
        appDatabase = FirebaseDatabase.getInstance();
        loggedUser = FirebaseAuth.getInstance().getCurrentUser();
        getUserData();
        // Get chat button
        sendDirectMessage = findViewById(R.id.activity_user_profile_send_message_button);
        // Initialize Fresco
        Fresco.initialize(this);
        sendDirectMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToChat(loggedUser.getUid(), userUid);
            }
        });
    }

    /**
     * Get user data from database.
     */
    private void getUserData() {
        appDatabase.getReference(DB_USERS).child(userUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(User.class);
                        ((TextView) findViewById(R.id.activity_user_profile_name))
                                .setText(currentUser.getDisplayName());
                        Uri uri = Uri.parse(currentUser.getAvatar());
                        SimpleDraweeView profileAvatar = findViewById(R.id.profile_avatar);
                        profileAvatar.setImageURI(uri);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    /**
     * Gets the key for the Chat Index from two user ids.
     * It concatenates user ids alphabetically.
     */
    private String getChatId(String u1, String u2) {
        if (u1.compareTo(u2) > 0) {
            return  u2.concat(u1);
        } else {
            return u1.concat(u2);
        }
    }

    /**
     * Goes to the requested chat. It creates it if does not exist.
     */
    private void goToChat(final String fromUser, final String toUser) {
        appDatabase.getReference(DB_CHATS).child(getChatId(fromUser, toUser))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Go to chat
                            GenericTypeIndicator<HashMap<String, Boolean>> t = new GenericTypeIndicator<HashMap<String, Boolean>>() {
                            };
                            HashMap<String, Boolean> conversation = dataSnapshot.getValue(t);
                            for (String conversationId : conversation.keySet()) {
                                Intent intent = new Intent(ActivityUserProfile.this, ActivityMain.class);
                                intent.putExtra(CONVERSATION_ID, conversationId);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        } else {
                            // Create chat and go there
                            // Get new channel id
                            DatabaseReference newChatRef = appDatabase.getReference(DB_USERS)
                                    .child(fromUser).child(DB_CHATS).push();
                            // Store the chat in the user's chat list
                            newChatRef.setValue(true);
                            // Store it in the other user's too
                            appDatabase.getReference(DB_USERS).child(toUser).child(DB_CHATS)
                                    .child(newChatRef.getKey()).setValue(true);
                            // Add it to the chats node
                            appDatabase.getReference(DB_CHATS).child(getChatId(fromUser, toUser))
                                    .child(newChatRef.getKey()).setValue(true);
                            // Add both users to the members node
                            appDatabase.getReference(DB_MEMBERS).child(newChatRef.getKey())
                                    .child(fromUser).setValue(true);
                            appDatabase.getReference(DB_MEMBERS).child(newChatRef.getKey())
                                    .child(toUser).setValue(true);
                            // Go to that chat
                            Intent intent = new Intent(ActivityUserProfile.this, ActivityMain.class);
                            intent.putExtra(CONVERSATION_ID, newChatRef.getKey());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
