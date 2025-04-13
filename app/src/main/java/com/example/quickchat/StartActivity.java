package com.example.quickchat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;


public class StartActivity extends AppCompatActivity {

    private Button signupButton, loginButton;
    private FirebaseUser currentUser;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        initializeFields();

        signupButton.setOnClickListener(v -> startActivity(new Intent(StartActivity.this, SignUpActivity.class)));

        loginButton.setOnClickListener(v -> startActivity(new Intent(StartActivity.this, LoginActivity.class)));
        getFCMToken();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null) {
            startActivity(new Intent(StartActivity.this, HomeScreenActivity.class));
            finish();
        }
    }

    private void initializeFields() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        signupButton = findViewById(R.id.signUpButton);
        loginButton = findViewById(R.id.SignInButton);
    }

    void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
           if(task.isSuccessful()) {
               String token = task.getResult();
               Log.i("FCM token: ", token);
           }
        });
    }

}