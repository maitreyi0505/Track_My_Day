package com.example.trackmyday;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

public class SignUp extends Activity implements View.OnClickListener {
    private EditText editTextEmail, editTextPassword, editTextPhoneNo, editTextUsername, editTextConfirmPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();

        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);
        editTextUsername = (EditText) findViewById(R.id.username);
        editTextPhoneNo = (EditText) findViewById(R.id.phone_number);
        editTextConfirmPassword = (EditText) findViewById(R.id.confirm_password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        findViewById(R.id.login_button_signup_page).setOnClickListener(this);
        findViewById(R.id.register_button).setOnClickListener(this);
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String phoneNo = editTextPhoneNo.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        if (email.isEmpty()) {
            editTextEmail.setError("Please enter your Email ID");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid Email ID");
            editTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Please enter your password");
            editTextPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Password length should be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }
        if (!confirmPassword.equals(password)) {
            editTextConfirmPassword.setError("Confirmation password does not match");
            editTextConfirmPassword.requestFocus();
            return;
        }
        if (username.isEmpty()) {
            editTextUsername.setError("Please set a username");
            editTextUsername.requestFocus();
            return;
        }
        if (phoneNo.isEmpty()) {
            editTextPhoneNo.setError("Please enter your mobile number");
            editTextPhoneNo.requestFocus();
            return;
        }
        if (phoneNo.length() != 10) {
            editTextPhoneNo.setError("Phone No length should be 10 digits");
            editTextPhoneNo.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        //do authentication using firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //update username of user as well
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build();

                        mAuth.getCurrentUser().updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "Username updated.");
                                        }
                                    }
                                });

                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {

                            Toast.makeText(getApplicationContext(), "User registered successfully.",
                                    Toast.LENGTH_SHORT).show();


                            //add phone no and username to database
                            Map<String, Object> userObject = new HashMap<>();
                            userObject.put("username", username);
                            userObject.put("phone_no", phoneNo);
                            db.collection("users").document(mAuth.getCurrentUser().getUid())
                                    .set(userObject)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "user name and phone no. successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing user name and phone no.", e);
                                        }
                                    });

                            //open the main page
                            finish();
                            Intent intent = new Intent(SignUp.this, MainActivity.class );
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getApplicationContext(), "User already exists.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_button:
                registerUser();
                break;
            case R.id.login_button_signup_page:
                onBackPressed();
                break;
        }
    }
}
