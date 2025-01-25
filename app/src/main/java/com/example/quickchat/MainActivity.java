package com.example.quickchat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Tìm button Signup trong file XML
        Button signUpButton = findViewById(R.id.signUpButton);
        Button logInButton = findViewById(R.id.SignInButton);

        signUpButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignUpActivity.class)));

        logInButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));

    }
}