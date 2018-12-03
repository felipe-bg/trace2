package com.iteso.trace;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.iteso.trace.beans.Channel;
import com.iteso.trace.beans.Message;
import com.iteso.trace.beans.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.iteso.trace.utils.Constants.CHANNELS_GROUP;
import static com.iteso.trace.utils.Constants.CHATS_GROUP;
import static com.iteso.trace.utils.Constants.CONVERSATION_ID;
import static com.iteso.trace.utils.Constants.DB_CHANNELS;
import static com.iteso.trace.utils.Constants.DB_CHATS;
import static com.iteso.trace.utils.Constants.DB_MEMBERS;
import static com.iteso.trace.utils.Constants.DB_MESSAGES;
import static com.iteso.trace.utils.Constants.DB_USERS;

public class ActivityMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Application database and user instances.
     */
    private FirebaseDatabase appDatabase;
    private FirebaseUser loggedUser;
    /**
     * Current conversation id. Defines the messages to load.
     */
    private String conversationId;
    /**
     * Conversation messages list.
     */
    private ArrayList<Message> messages;
    /**
     * Messages RecyclerView.
     */
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    /**
     * Input text area
     */
    private EditText messageInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get database instance
        appDatabase = FirebaseDatabase.getInstance();
        loggedUser = FirebaseAuth.getInstance().getCurrentUser();
        // Get conversationId
        conversationId = getIntent().getExtras().getString(CONVERSATION_ID);
        // Load Toolbar and NavigationDrawer
        setupNavigation();
        // Initialize messages list
        messages = new ArrayList<>();
        // Load messages
        loadMessages();
        // Setup message input
        messageInput = findViewById(R.id.message_input_text);
        // Setup Fresco
        Fresco.initialize(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent;

        switch (id) {
            case R.id.menu_activity_channel_title:
                //intent = new Intent(ActivityMain.this, ActivityChannelDetails.class);
                //intent.putExtra(Constants.CURRENT_CHANNEL, currentChannelO);
                //startActivityForResult(intent, Constants.CHANNEL_EDIT_CODE);
                break;
            case R.id.menu_activity_channel_members:
                intent = new Intent(ActivityMain.this, ActivityMembers.class);
                intent.putExtra(CONVERSATION_ID, conversationId);
                startActivity(intent);
                break;
            case R.id.menu_activity_channel_add_people:
                intent = new Intent(ActivityMain.this, ActivityAddPeople.class);
                intent.putExtra(CONVERSATION_ID, conversationId);
                startActivity(intent);
                break;
            case R.id.menu_activity_channel_edit_profile:
                //intent = new Intent(ActivityMain.this, ActivityUserProfileEdit.class);
                //intent.putExtra(Constants.CURRENT_USER, user);
                //startActivity(intent);
                break;
            case R.id.menu_activity_channel_logout:
                // Logout from Firebase
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // Return to splash screen
                                Intent intent = new Intent(ActivityMain.this, ActivitySplashScreen.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        });
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // New conversation selected in Navigation Drawer
        startActivity(item.getIntent());
        return true;
    }

    /**
     * Loads Toolbar and Navigation Drawer.
     * Gets the user's channels and chats and adds them to the
     * navigation drawer.
     */
    private void setupNavigation() {
        // Add Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Add Drawer and toggle
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        // Set Container for contents of drawer within a NavigationView
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Load Channels and Chats to Navigation Drawer
        loadUserChannels(navigationView.getMenu().getItem(CHANNELS_GROUP).getSubMenu());
        loadUserChats(navigationView.getMenu().getItem(CHATS_GROUP).getSubMenu());
    }

    /**
     * Gets user channels from database and adds them to the submenu.
     *
     * @param channelsSubmenu Submenu where the channels will be placed.
     */
    private void loadUserChannels(final SubMenu channelsSubmenu) {
        appDatabase.getReference(DB_USERS).child(loggedUser.getUid()).child(DB_CHANNELS)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot userChannelSnapshot, @Nullable String s) {
                        // Read Channel information
                        appDatabase.getReference(DB_CHANNELS).child(userChannelSnapshot.getKey())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Channel c = dataSnapshot.getValue(Channel.class);
                                        MenuItem menuItem = channelsSubmenu.add(CHANNELS_GROUP, Menu.NONE, Menu.NONE, c.getName());
                                        menuItem.setIcon(R.drawable.ic_radio_black_24dp);
                                        // Set menuItem intent to go to a fresh new conversation
                                        Intent intent = new Intent(ActivityMain.this, ActivityMain.class);
                                        intent.putExtra(CONVERSATION_ID, dataSnapshot.getKey());
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        menuItem.setIntent(intent);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    /**
     * Gets user chats from database and adds them to the submenu.
     *
     * @param chatsSubmenu Submenu where the chats will be placed.
     */
    private void loadUserChats(final SubMenu chatsSubmenu) {
        appDatabase.getReference(DB_USERS).child(loggedUser.getUid()).child(DB_CHATS)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot userChatSnapshot, @Nullable String s) {
                        // Read Chat information from Members to get the name of the interlocutor
                        appDatabase.getReference(DB_MEMBERS).child(userChatSnapshot.getKey())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        // https://firebase.google.com/docs/reference/android/com/google/firebase/database/GenericTypeIndicator
                                        GenericTypeIndicator<HashMap<String, Boolean>> t = new GenericTypeIndicator<HashMap<String, Boolean>>() {
                                        };
                                        HashMap<String, Boolean> participants = dataSnapshot.getValue(t);
                                        // Set menuItem intent to go to a fresh new conversation
                                        final Intent intent = new Intent(ActivityMain.this, ActivityMain.class);
                                        intent.putExtra(CONVERSATION_ID, dataSnapshot.getKey());
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        // The name to be shown is the first conversation participant that isn't yourself
                                        for (String userUid : participants.keySet()) {
                                            if (!userUid.equals(loggedUser.getUid())) {
                                                // Get display name for this user from database
                                                appDatabase.getReference(DB_USERS).child(userUid)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                User u = dataSnapshot.getValue(User.class);
                                                                MenuItem menuItem = chatsSubmenu.add(CHANNELS_GROUP, Menu.NONE, Menu.NONE, u.getDisplayName());
                                                                menuItem.setIcon(R.drawable.ic_person_black_24dp);
                                                                menuItem.setIntent(intent);
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });
                                                break; // Exit loop
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    /**
     * Sets up the RecyclerView and loads the messages to it
     */
    private void loadMessages() {
        // Setup RecyclerView
        mRecyclerView = findViewById(R.id.messages_recycler_view);
        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // Use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // Specify an adapter
        mAdapter = new AdapterMessage(messages, this);
        mRecyclerView.setAdapter(mAdapter);
        // Add layout change listener to scroll when layout changes occur
        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    if (mRecyclerView != null) {
                        mRecyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                int newPosition = mRecyclerView.getAdapter().getItemCount() - 1;
                                if (newPosition < 0) newPosition = 0;
                                mRecyclerView.smoothScrollToPosition(newPosition);
                            }
                        }, 100);
                    }
                }
            }
        });

        // Load messages
        appDatabase.getReference(DB_MESSAGES).child(conversationId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        messages.add(dataSnapshot.getValue(Message.class));
                        mAdapter.notifyItemInserted(messages.size() - 1);
                        // Scroll RecyclerView to show new message
                        if (mRecyclerView != null) {
                            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void hitSend(View v) {
        if (!messageInput.getText().toString().isEmpty()) {
            // Get typed message and user
            Message newMessage = new Message();
            newMessage.setMessage(messageInput.getText().toString());
            newMessage.setTimestamp(new Date().toString());
            newMessage.setUserUid(loggedUser.getUid());

            // Clear EditText
            messageInput.getText().clear();

            // Get new child with push() (generates unique ID)
            DatabaseReference newMessageReference = appDatabase.getReference(DB_MESSAGES)
                    .child(conversationId).push();
            newMessageReference.setValue(newMessage);
        }
    }
}
