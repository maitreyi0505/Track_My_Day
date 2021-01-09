package com.example.trackmyday;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private String username;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        username= mAuth.getCurrentUser().getDisplayName();
        TextView title =findViewById(R.id.welcome_title);
        title.setText("Welcome "+username);

        Button moodButton = (Button) findViewById(R.id.mood_button);

        moodButton.setOnClickListener(
                (v) -> {
                    Intent i = new Intent(MainActivity.this, Mood.class);
                    startActivity(i);
                }
        );

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, Login.class));
        }

        Log.w(TAG, "User is logged in as "+ username);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //function to display 3 dot menu on app
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //function to take action when something from menu  is chosen
        switch (item.getItemId()) {
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, Login.class));
                break;
        }
        return true;
    }
}