
package com.example.trackmyday;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static android.content.ContentValues.TAG;
import static java.lang.Integer.parseInt;

public class MoodHistory extends Activity {
    private CollectionReference collectionRef;
    private final DateFormat dateFormat = new SimpleDateFormat("dd MMM yy");
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //set reference to  collection
        collectionRef = FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("daily_mood");

        //create array of history from database
        collectionRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            ArrayList<ArrayList<String>> historyArray = new ArrayList<ArrayList<String>>();

                            for (QueryDocumentSnapshot document : task.getResult()) {   // LOOP
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                historyArray.add(new ArrayList<String>(Arrays.asList(
                                        document.getId(),
                                        document.get("mood").toString(),
                                        document.get("mood_note").toString()
                                )));
                            }

                            ArrayAdapter adapter = new ArrayAdapter(MoodHistory.this, 1, historyArray) {
                                LayoutInflater mInflater = (LayoutInflater) MoodHistory.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    View view = mInflater.inflate(R.layout.activity_mood_listview, parent, false);
                                    ImageView moodEmoji= (ImageView) view.findViewById(R.id.icon);
                                    TextView text1 = (TextView) view.findViewById(R.id.firstLine);
                                    TextView text2 = (TextView) view.findViewById(R.id.secondLine);
                                    String mood = "";
                                    moodEmoji.setBackgroundResource(R.drawable.smiley_normal);
                                    switch (historyArray.get(position).get(1)) {
                                        case "1":
                                            mood = "Very unhappy";
                                            moodEmoji.setBackgroundResource(R.drawable.smiley_disappointed);
                                            break;
                                        case "2":
                                            mood = "Unhappy";
                                            moodEmoji.setBackgroundResource(R.drawable.smiley_sad);
                                            break;
                                        case "3":
                                            mood = "Moderate";
                                            moodEmoji.setBackgroundResource(R.drawable.smiley_normal);
                                            break;
                                        case "4":
                                            mood = "Happy";
                                            moodEmoji.setBackgroundResource(R.drawable.smiley_happy);
                                            break;
                                        case "5":
                                            mood = "Very happy!";
                                            moodEmoji.setImageResource(R.drawable.smiley_super_happy);
                                            break;
                                    }
                                    try {
                                        Date date=new SimpleDateFormat("yyyy_MM_dd").parse(historyArray.get(position).get(0));
                                        text1.setText(dateFormat.format(date));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }


                                    text2.setText(historyArray.get(position).get(2));
                                    return view;
                                }
                            };
                            ListView listView = (ListView) findViewById(R.id.history_list);
                            listView.setAdapter(adapter);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
