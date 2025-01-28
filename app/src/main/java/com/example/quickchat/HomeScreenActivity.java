package com.example.quickchat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import de.hdodenhof.circleimageview.CircleImageView;

public class HomeScreenActivity extends AppCompatActivity {

    private CircleImageView homescreen_image;
    private TextView homescreen_username;
    private Button homescreen_signout, homescreen_editProfile, homescreen_settings;
    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        initializeFields();
        displayUsernameAndAvatar();

        homescreen_signout.setOnClickListener(v -> signout());

        homescreen_editProfile.setOnClickListener(v -> Toast.makeText(HomeScreenActivity.this, "Tính năng này đang phát triển", Toast.LENGTH_SHORT).show());

        homescreen_settings.setOnClickListener(v -> Toast.makeText(HomeScreenActivity.this, "Tính năng này đang phát triển", Toast.LENGTH_SHORT).show());
    }





    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser == null) {
            startActivity(new Intent(HomeScreenActivity.this, LoginActivity.class));
            finish();
        }
    }
    private void initializeFields() {
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("users");
        currentUser = auth.getCurrentUser();
        homescreen_image = findViewById(R.id.hs_image);
        homescreen_username = findViewById(R.id.hs_username);
        homescreen_signout = findViewById(R.id.hs_signout);
        homescreen_editProfile = findViewById(R.id.hs_profile);
        homescreen_settings = findViewById(R.id.hs_setting);
    }

    private void displayUsernameAndAvatar() {
        String currentUserID = currentUser.getUid();
        reference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                if (data.exists() && data.hasChild("username") && data.hasChild("imageURL")) {
                    String retrieveUsername = data.child("username").getValue(String.class);
                    String retrieveImage = data.child("imageURL").getValue(String.class);

                    homescreen_username.setText(retrieveUsername);
                    if (retrieveImage.equals("default")) {
                        homescreen_image.setImageResource(R.mipmap.ic_launcher);
                    }
                }
                else if (data.exists() && data.hasChild("username")) {
                    String retrieveUsername = data.child("username").getValue(String.class);
                    homescreen_username.setText(retrieveUsername);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void signout() {
        auth.signOut();
        Toast.makeText(HomeScreenActivity.this, "Bạn đã đăng xuất khỏi ứng dụng", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(HomeScreenActivity.this, LoginActivity.class));
        finish();
    }


}