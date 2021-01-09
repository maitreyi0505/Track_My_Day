package com.example.trackmyday;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class Mood extends Activity {
    ImageView mood1, mood2, mood3, mood4, mood5;    //  1 for extreme bad, 5 for extreme good
    int mood;
    private String userId;
    private EditText mood_notes;
    private FirebaseAuth mAuth;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        //get reference for today's database
        docRef = db.collection("users")
                .document(userId)
                .collection("daily_mood")
                .document(dateFormat.format(new Date()));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Mood.this, MoodHistory.class);
                startActivity(i);
            }
        });

        Button submit = findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mood_notes = findViewById(R.id.moodNotes);

                //store this note to database
                Map<String, Object> moodObject = new HashMap<>();
                moodObject.put("mood_note", mood_notes.getText().toString());

                docRef.update(moodObject)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Today's mood note updated successfully!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing today's mood note", e);
                            }
                        });
                mood_notes.setText("");
            }
        });
        mood1 = (ImageView) findViewById(R.id.mood1);
        mood2 = (ImageView) findViewById(R.id.mood2);
        mood3 = (ImageView) findViewById(R.id.mood3);
        mood4 = (ImageView) findViewById(R.id.mood4);
        mood5 = (ImageView) findViewById(R.id.mood5);

        mood1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Mood.this, "Sorry to hear that, dark clouds don't last forever!", Toast.LENGTH_LONG).show();
                moodChosen(1, getResources().getColor(R.color.mood1));
            }
        });
        mood2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Mood.this, "I think a walk can help you", Toast.LENGTH_LONG).show();
                moodChosen(2, getResources().getColor(R.color.mood2));
            }
        });
        mood3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Mood.this, "Some days are just..  meh!", Toast.LENGTH_LONG).show();
                moodChosen(3, getResources().getColor(R.color.mood3));
            }
        });
        mood4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Mood.this, "A good day it is for sure!", Toast.LENGTH_LONG).show();
                moodChosen(4, getResources().getColor(R.color.mood4));
            }
        });
        mood5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Mood.this, "Yay! Today's a great day!", Toast.LENGTH_LONG).show();
                moodChosen(5, getResources().getColor(R.color.mood5));
            }
        });
    }

    void moodChosen(int currentMood, int backgroundColour) {

        mood = currentMood;
        LinearLayout currentLayout = findViewById(R.id.activity_mood);
        currentLayout.setBackgroundColor(backgroundColour);

        //add mood to database
        Map<String, Object> moodObject = new HashMap<>();
        moodObject.put("mood", mood);

        //update doc of it exists, else set new values
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        docRef.update(moodObject).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Today's mood updated successfully!");
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing today's mood", e);
                                    }
                                });
                    } else {
                        moodObject.put("mood_note", "-");
                        docRef.set(moodObject).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Today's mood updated successfully!");
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing today's mood", e);
                                    }
                                });
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }
}
