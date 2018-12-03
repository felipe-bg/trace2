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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iteso.trace.beans.Channel;

import static com.iteso.trace.utils.Constants.CHANNELS_GROUP;
import static com.iteso.trace.utils.Constants.CHATS_GROUP;
import static com.iteso.trace.utils.Constants.CONVERSATION_ID;
import static com.iteso.trace.utils.Constants.DB_CHANNELS;
import static com.iteso.trace.utils.Constants.DB_USERS;

public class ActivityMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Application database and user instances
     */
    private FirebaseDatabase appDatabase;
    private FirebaseUser loggedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get database instance
        appDatabase = FirebaseDatabase.getInstance();
        loggedUser = FirebaseAuth.getInstance().getCurrentUser();
        // Load Toolbar and NavigationDrawer
        setupNavigation();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_activity_channel_title) {
            return true;
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
        // Load Channels
        loadUserChannels(navigationView.getMenu().getItem(CHANNELS_GROUP).getSubMenu());
        navigationView.getMenu().getItem(CHATS_GROUP).getSubMenu().add(CHATS_GROUP,Menu.NONE, Menu.NONE,"MyChatT2");
        navigationView.getMenu().getItem(CHATS_GROUP).getSubMenu().add(CHATS_GROUP,Menu.NONE, Menu.NONE,"MyChatT3");
    }

    /**
     * Gets user channels from database and adds them to the submenu.
     * @param channelsSubmenu Submenu where the channels will be placed.
     */
    private void loadUserChannels(final SubMenu channelsSubmenu) {
        appDatabase.getReference(DB_USERS).child(loggedUser.getUid()).child(DB_CHANNELS)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot channelSnapshot, @Nullable String s) {
                        // Read Channel information
                        appDatabase.getReference(DB_CHANNELS).child(channelSnapshot.getKey())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Channel c = dataSnapshot.getValue(Channel.class);
                                        MenuItem menuItem = channelsSubmenu.add(CHANNELS_GROUP,Menu.NONE, Menu.NONE,c.getName());
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
}
