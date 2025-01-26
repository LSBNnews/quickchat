package com.example.quickchat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser != null) {
            startActivity(new Intent(MainActivity.this, HomeScreenActivity.class));
            finish();
        }
        // Tìm button Signup trong file XML
        Button signUpButton = findViewById(R.id.signUpButton);
        Button logInButton = findViewById(R.id.SignInButton);

        signUpButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignUpActivity.class)));

        logInButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));

    }
}