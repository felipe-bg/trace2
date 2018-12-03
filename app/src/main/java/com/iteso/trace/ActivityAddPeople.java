package com.iteso.trace;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static com.iteso.trace.utils.Constants.CONVERSATION_ID;
import static com.iteso.trace.utils.Constants.DB_CHATS;
import static com.iteso.trace.utils.Constants.DB_EMAILS;
import static com.iteso.trace.utils.Constants.DB_MEMBERS;
import static com.iteso.trace.utils.Constants.DB_USERS;
import static com.iteso.trace.utils.Constants.EMAIL_ERROR;

public class ActivityAddPeople extends AppCompatActivity {
    /**
     * Application database instance.
     */
    private FirebaseDatabase appDatabase;
    /**
     * Current conversation id. Defines the members to load.
     */
    private String conversationId;
    /**
     * Input text area
     */
    private EditText emailInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_people);
        appDatabase = FirebaseDatabase.getInstance();
        conversationId = getIntent().getExtras().getString(CONVERSATION_ID);

        // Get email EditText
        emailInput = findViewById(R.id.add_people_email);
    }

    public void addEmail(View v) {
        appDatabase.getReference(DB_EMAILS).child(emailInput.getText().toString().replace(".",""))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            GenericTypeIndicator<HashMap<String, Boolean>> t = new GenericTypeIndicator<HashMap<String, Boolean>>() {
                            };
                            HashMap<String, Boolean> userHashMap = dataSnapshot.getValue(t);
                            for (String userUid : userHashMap.keySet()) {
                                // Get this user and add it to the channel member list
                                appDatabase.getReference(DB_MEMBERS).child(conversationId)
                                        .child(userUid).setValue(true);
                                // Add it to the user's channels list
                                appDatabase.getReference(DB_USERS).child(userUid).child(DB_CHATS)
                                        .child(conversationId).setValue(true);
                                finish();
                            }
                        } else {
                            Toast.makeText(ActivityAddPeople.this, EMAIL_ERROR, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
