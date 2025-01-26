package com.example.quickchat;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;


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

        Intent intent = getIntent();
        String name = intent.getStringExtra("username");
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");
        String image = intent.getStringExtra("imageURL");

        homescreen_username.setText("Xin chào, " + name);

        homescreen_signout.setOnClickListener(v -> {
            Toast.makeText(HomeScreenActivity.this, "Bạn đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeScreenActivity.this, LoginActivity.class));
            finish();
        });




    }
}