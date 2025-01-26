package com.example.quickchat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import de.hdodenhof.circleimageview.CircleImageView;

public class HomeScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        CircleImageView homescreen_image = findViewById(R.id.hs_image);
        TextView homescreen_username = findViewById(R.id.hs_username);
        Button homescreen_signout = findViewById(R.id.hs_signout);
        Button homescreen_editProfile = findViewById(R.id.hs_profile);
        Button homescreen_settings = findViewById(R.id.hs_setting);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        homescreen_username.setText(username);

        // Xử lý tính năng khi nhấn Đăng xuất
        homescreen_signout.setOnClickListener(v -> {
            Toast.makeText(HomeScreenActivity.this, "Bạn đã đăng xuất khỏi ứng dụng", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeScreenActivity.this, LoginActivity.class));
            finish();
        });

        // Xử lý tính năng khi nhấn Edit Profile
        homescreen_editProfile.setOnClickListener(v -> {

            // Nạp data sang cho Edit Profile
            String user = intent.getStringExtra("name");
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
            Query checkUserDatabase = reference.orderByChild("name").equalTo(user);
            checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        String namefromDB = snapshot.child(user).child("name").getValue(String.class);
                        String usernamefromDB = snapshot.child(user).child("username").getValue(String.class);
                        String emailFromDB = snapshot.child(user).child("email").getValue(String.class);
                        String passwordFromDB = snapshot.child(user).child("password").getValue(String.class);
                        String imageFromDB = snapshot.child(user).child("imageURL").getValue(String.class);

                        Intent intent = new Intent(HomeScreenActivity.this, EditProfileActivity.class);
                        intent.putExtra("name", namefromDB);
                        intent.putExtra("username", usernamefromDB);
                        intent.putExtra("email", emailFromDB);
                        intent.putExtra("password", passwordFromDB);
                        intent.putExtra("imageURL", imageFromDB);

                        startActivity(intent);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });

        homescreen_settings.setOnClickListener(v -> {
            Toast.makeText(HomeScreenActivity.this, "Tính năng này đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

}