package com.example.quickchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {
    private EditText edit_username, edit_password, edit_email, edit_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edit_username = findViewById(R.id.ep_username);
        edit_password = findViewById(R.id.ep_password);
        edit_email = findViewById(R.id.ep_email);
        edit_image = findViewById(R.id.ep_image);
        Button edit_backButton = findViewById(R.id.ep_backButton);
        Button edit_saveButton = findViewById(R.id.ep_saveButton);

        Intent intent = getIntent();
        String name = intent.getStringExtra("username");
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");
        String image = intent.getStringExtra("imageURL");

        edit_username.setText(name);
        edit_password.setText(password);
        edit_email.setText(email);
        edit_image.setText(image);


        edit_backButton.setOnClickListener(v -> {
            startActivity(new Intent(EditProfileActivity.this, HomeScreenActivity.class));
            finish();
        });

    }


}