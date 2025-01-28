package com.example.quickchat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;


public class EditProfileActivity extends AppCompatActivity {
    private EditText editProfile_username, editProfile_description;

    private Button editProfile_saveButton, editProfile_backButton;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private String usernameChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initializeFields();

        displayUserInfo();

        editProfile_backButton.setOnClickListener(v -> startActivity(new Intent(EditProfileActivity.this, HomeScreenActivity.class)));

        editProfile_saveButton.setOnClickListener(v -> updateProfile());
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            startActivity(new Intent(EditProfileActivity.this, LoginActivity.class));
        }
    }

    private void initializeFields() {
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("users");
        currentUser = auth.getCurrentUser();
        editProfile_username = findViewById(R.id.ep_username);
        editProfile_description = findViewById(R.id.ep_description);
        editProfile_backButton = findViewById(R.id.ep_backButton);
        editProfile_saveButton = findViewById(R.id.ep_saveButton);

    }

    private void displayUserInfo() {
        String currentUserID = currentUser.getUid();
        reference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                if (data.exists() && data.hasChild("username") && data.hasChild("description")) {
                    String retrieveUsername = data.child("username").getValue(String.class);
                    String retrieveDescription = data.child("description").getValue(String.class);

                    usernameChanged = retrieveUsername;
                    editProfile_username.setText(retrieveUsername);
                    editProfile_description.setText(retrieveDescription);
                }
                else if (data.exists() && data.hasChild("username")) {
                    String username = data.child("username").getValue(String.class);

                    editProfile_username.setText(username);
                    editProfile_description.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateProfile() {
        String usernameInput = editProfile_username.getText().toString().trim();
        String descriptionInput = editProfile_description.getText().toString().trim();
        String currentUserID = currentUser.getUid();

        if (!usernameInput.equals(usernameChanged)) {
            if (usernameInput.isEmpty()) {
                editProfile_username.setError("Vui lòng điền tên người dùng");
                editProfile_username.requestFocus();
                return;
            }

            if(usernameInput.length() > 10) {
                editProfile_username.setError("Tên người dùng không vượt quá 10 kí tự");
                editProfile_username.requestFocus();
                return;
            }

            Query checkUsernameInDB = reference.orderByChild("username").equalTo(usernameInput);
            checkUsernameInDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot data) {
                    if (data.exists()) {
                        editProfile_username.setError("Tên người dùng này đã tồn tại");
                        editProfile_username.requestFocus();
                    }
                    else {
                        updateData(currentUserID, usernameInput, descriptionInput);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(EditProfileActivity.this, "Lỗi khi kiểm tra tên người dùng", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            if(descriptionInput.length() > 30) {
                editProfile_description.setError("Mô tả bản thân không vượt quá 30 kí tự");
                editProfile_description.requestFocus();
            }
            else {
                updateData(currentUserID, usernameInput, descriptionInput);
            }
        }
    }

    private void updateData(String userID, String username, String description ) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("description", description);
        reference.child(userID).updateChildren(userData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditProfileActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EditProfileActivity.this, HomeScreenActivity.class));
            }
            else {
                Toast.makeText(EditProfileActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}