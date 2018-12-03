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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iteso.trace.beans.User;

import static com.iteso.trace.utils.Constants.CONVERSATION_ID;
import static com.iteso.trace.utils.Constants.DB_USERS;
import static com.iteso.trace.utils.Constants.USER_ID;

public class ActivityUserProfile extends AppCompatActivity {

    /**
     * Application database and user instances.
     */
    private FirebaseDatabase appDatabase;
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
        getUserData();
        // Get chat button
        sendDirectMessage = findViewById(R.id.activity_user_profile_send_message_button);
        // Initialize Fresco
        Fresco.initialize(this);
        sendDirectMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityUserProfile.this, ActivityMain.class);
                intent.putExtra(CONVERSATION_ID, 0);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
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
                        ((TextView)findViewById(R.id.activity_user_profile_name))
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
}
