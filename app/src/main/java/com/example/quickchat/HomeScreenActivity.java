package com.example.quickchat;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.quickchat.model.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeScreenActivity extends AppCompatActivity {

    private CircleImageView homescreen_image;
    private TextView homescreen_username;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    private Button homescreen_signout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        homescreen_signout = findViewById(R.id.hs_signout);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        homescreen_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeScreenActivity.this, LoginActivity.class));
                finish();
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                homescreen_username.setText(user.getUsername());

                if(user.getImageURL().equals("default")) {
                    homescreen_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(HomeScreenActivity.this).load(user.getImageURL()).into(homescreen_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        homescreen_image = findViewById(R.id.hs_image);
        homescreen_username = findViewById(R.id.hs_username);

    }
}