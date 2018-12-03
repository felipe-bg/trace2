package com.iteso.trace;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iteso.trace.beans.User;

import java.util.ArrayList;

import static com.iteso.trace.utils.Constants.CONVERSATION_ID;
import static com.iteso.trace.utils.Constants.DB_MEMBERS;
import static com.iteso.trace.utils.Constants.DB_USERS;

public class ActivityMembers extends AppCompatActivity {

    /**
     * Application database instance.
     */
    private FirebaseDatabase appDatabase;
    /**
     * Current conversation id. Defines the members to load.
     */
    private String conversationId;
    /**
     * Conversation messages list.
     */
    private ArrayList<User> members;
    /**
     * Members RecyclerView.
     */
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);
        appDatabase = FirebaseDatabase.getInstance();
        conversationId = getIntent().getExtras().getString(CONVERSATION_ID);
        Toolbar toolbar = findViewById(R.id.activity_members_toolbar);
        setSupportActionBar(toolbar);
        members = new ArrayList<>();
        // Load member data
        loadMembers();
    }

    /**
     * Sets up the RecyclerView and loads the messages to it
     */
    private void loadMembers() {
        // Setup RecyclerView
        mRecyclerView = findViewById(R.id.member_list_recycler_view);
        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // Use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // Specify an adapter
        mAdapter = new AdapterMember(members, this);
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
                                mRecyclerView.smoothScrollToPosition(
                                        mRecyclerView.getAdapter().getItemCount() - 1);
                            }
                        }, 100);
                    }
                }
            }
        });

        // Load members
        appDatabase.getReference(DB_MEMBERS).child(conversationId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        appDatabase.getReference(DB_USERS)
                                .child(dataSnapshot.getKey())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        members.add(dataSnapshot.getValue(User.class));
                                        mAdapter.notifyItemInserted(members.size() - 1);
                                        // Scroll RecyclerView to show new member
                                        if (mRecyclerView != null) {
                                            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
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
}
