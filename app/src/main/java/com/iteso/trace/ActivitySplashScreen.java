package com.iteso.trace;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iteso.trace.beans.Channel;
import com.iteso.trace.beans.Message;
import com.iteso.trace.beans.User;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.iteso.trace.utils.Constants.CONVERSATION_ID;
import static com.iteso.trace.utils.Constants.DB_CHANNELS;
import static com.iteso.trace.utils.Constants.DB_EMAILS;
import static com.iteso.trace.utils.Constants.DB_MEMBERS;
import static com.iteso.trace.utils.Constants.DB_MESSAGES;
import static com.iteso.trace.utils.Constants.DB_TAGS;
import static com.iteso.trace.utils.Constants.DB_USERS;
import static com.iteso.trace.utils.Constants.DEFAULT_TAG;
import static com.iteso.trace.utils.Constants.DEFAULT_USER_AVATAR;
import static com.iteso.trace.utils.Constants.DEFAULT_USER_UID;
import static com.iteso.trace.utils.Constants.RC_SIGN_IN;
import static com.iteso.trace.utils.Constants.RTDB_LOG;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ActivitySplashScreen extends AppCompatActivity {

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    /**
     * Application startup runnable
     */
    private static final int UI_STARTUP_DELAY = 1000;
    private final Handler mStartupHandler = new Handler();
    private final Runnable mStartupRunnable = new Runnable() {
        @Override
        public void run() {
            if (mAuth.getCurrentUser() != null) {
                // Authenticated
                Log.e("loggedUser", mAuth.getCurrentUser().getDisplayName());
                startMainActivity(mAuth.getCurrentUser());
            } else {
                // Not authenticated
                launchSignInFlow();
            }
        }
    };
    /**
     * Application authentication and database instances.
     */
    private FirebaseAuth mAuth;
    private FirebaseDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Hide controls by default.
        hide();

        // Initialize authentication and database instances.
        mAuth = FirebaseAuth.getInstance();
        appDatabase = FirebaseDatabase.getInstance();

        // Schedule application startup, cancelling any
        // previously scheduled calls.
        mStartupHandler.removeCallbacks(mStartupRunnable);
        mStartupHandler.postDelayed(mStartupRunnable, UI_STARTUP_DELAY);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Sets up the authentication providers and
     * kicks off the FirebaseUI sign in flow.
     */
    private void launchSignInFlow() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    /**
     * Handles the result of the FirebaseUI sign in flow.
     * @param requestCode Checks for RC_SIGN_IN
     * @param resultCode Checks sign int result
     * @param data Gets response from intent data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                startMainActivity(mAuth.getCurrentUser());
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                if (response == null) {
                    // The user canceled the sign-in flow
                    finish();
                } else {
                    // Send back to login for retry
                    launchSignInFlow();
                }
            }
        }
    }

    /**
     * Sets up user account if necessary and sends it to the
     * main activity.
     * @param firebaseUser Successfully logged user
     */
    private void startMainActivity(final FirebaseUser firebaseUser) {
        // Get user information from RealTime database
        appDatabase.getReference(DB_USERS).child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Use the user information to load the main activity
                        User currentUser = dataSnapshot.getValue(User.class);
                        if (currentUser == null) {
                            // New user, create default channel
                            Channel defaultChannel = new Channel();
                            defaultChannel.setName(getResources().getString(R.string.default_channel_name));
                            defaultChannel.setDescription(getResources().getString(R.string.default_channel_description));
                            defaultChannel.getTags().put(DEFAULT_TAG, true);
                            // Get new channel id
                            DatabaseReference defaultChannelRef = appDatabase.getReference(DB_CHANNELS).push();
                            // Store the channel in the database
                            defaultChannelRef.setValue(defaultChannel);
                            // Add channel to tags list
                            appDatabase.getReference(DB_TAGS).child(DEFAULT_TAG)
                                    .child(defaultChannelRef.getKey()).setValue(true);
                            // Add welcome message to the newly created channel
                            Message welcomeMessage = new Message();
                            welcomeMessage.setMessage(getResources().getString(R.string.welcome_message));
                            welcomeMessage.setTimestamp(new Date().toString());
                            welcomeMessage.setUserUid(DEFAULT_USER_UID);
                            // Get new message reference
                            DatabaseReference welcomeMsgRef = appDatabase.getReference(DB_MESSAGES)
                                    .child(defaultChannelRef.getKey()).push();
                            // Add message to database
                            welcomeMsgRef.setValue(welcomeMessage);
                            // Create new user and add it to the database
                            User newUser = new User();
                            newUser.setDisplayName(firebaseUser.getDisplayName());
                            newUser.setEmail(firebaseUser.getEmail());
                            newUser.setCurrentConversation(defaultChannelRef.getKey());
                            newUser.setAvatar(DEFAULT_USER_AVATAR);
                            newUser.getChannels().put(defaultChannelRef.getKey(), true);
                            // Add user to database
                            appDatabase.getReference(DB_USERS).child(firebaseUser.getUid())
                                    .setValue(newUser);
                            // Add user to channel's member list
                            appDatabase.getReference(DB_MEMBERS).child(defaultChannelRef.getKey())
                                    .child(firebaseUser.getUid())
                                    .setValue(true);
                            // Add user to emails list
                            appDatabase.getReference(DB_EMAILS).child(newUser.getEmail())
                                    .setValue(firebaseUser.getUid());
                        }
                        // Start main activity with user's active chat
                        Intent intent = new Intent(ActivitySplashScreen.this, ActivityMain.class);
                        intent.putExtra(CONVERSATION_ID, currentUser.getCurrentConversation());
                        startActivity(intent);
                        finish(); // Deletes this from stack so it is not shown id user goes back
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(RTDB_LOG, "loadUserInfo:onCancelled", databaseError.toException());
                    }
                });
    }
}
