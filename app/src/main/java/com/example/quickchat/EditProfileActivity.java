package com.example.quickchat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class EditProfileActivity extends AppCompatActivity {
    private EditText edit_username, edit_password, edit_email, edit_image;
    private String name, email, password, image;
    DatabaseReference reference;
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
        name = intent.getStringExtra("username");
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");
        image = intent.getStringExtra("imageURL");

        edit_username.setText(name);
        edit_password.setText(password);
        edit_email.setText(email);
        edit_image.setText(image);

        // Xử lý tính năng cho Xác nhận
        edit_saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference = FirebaseDatabase.getInstance().getReference("users");
                boolean isChanged = false;

                if(!name.equals(edit_username.getText().toString().trim())) {
                    if(name.isEmpty()) {
                        edit_username.setError("Không được để trống tên");
                        edit_username.requestFocus();
                    } else {
                        reference.child(name).child("username").setValue(edit_username.getText().toString().trim());
                        name = edit_username.getText().toString().trim();
                        isChanged = true;
                    }

                }

                // Xử lý trường hợp Email trống hoặc không đúng định dạng
                if(!email.equals(edit_email.getText().toString().trim())) {
                    if(email.isEmpty()) {
                        edit_email.setError("Không được để trống email");
                        edit_email.requestFocus();
                    } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        edit_email.setError("Vui lòng điền đúng định dạng email");
                        edit_email.requestFocus();
                    } else {
                        reference.child(name).child("email").setValue(edit_email.getText().toString().trim());
                        email = edit_email.getText().toString().trim();
                        isChanged = true;
                    }

                }


                if(!password.equals(edit_password.getText().toString().trim())) {
                    if(password.isEmpty()) {
                        edit_password.setError("Không được để trống mật khẩu");
                        edit_password.requestFocus();
                    } else if(password.length() < 6) {
                        edit_password.setError("Mật khẩu phải chứa tổi thiểu 6 kí tự");
                        edit_password.requestFocus();
                    } else {
                        reference.child(name).child("password").setValue(edit_password.getText().toString().trim());
                        password = edit_password.getText().toString().trim();
                        isChanged = true;
                    }

                }

                if(!image.equals(edit_image.getText().toString().trim())) {
                    if(image.isEmpty()) {
                        edit_image.setError("Không được để trống file ảnh");
                        edit_image.requestFocus();
                    } else {
                        reference.child(name).child("image").setValue(edit_image.getText().toString().trim());
                        image = edit_image.getText().toString().trim();
                        isChanged = true;
                    }

                }

                if(isChanged) {
                    Toast.makeText(EditProfileActivity.this, "Đã lưu thông tin thay đổi", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Không có thông tin gì thay đổi", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Xử lý tính năng cho Quay lại
        edit_backButton.setOnClickListener(v -> {

            // Nạp data sang cho Home Screen
            String userUsername = edit_username.getText().toString().trim();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
            Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);
            checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        String emailFromDB = snapshot.child(userUsername).child("email").getValue(String.class);
                        String nameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);
                        String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);
                        String imageFromDB = snapshot.child(userUsername).child("imageURL").getValue(String.class);

                        Intent intent = new Intent(EditProfileActivity.this, HomeScreenActivity.class);

                        intent.putExtra("email", emailFromDB);
                        intent.putExtra("username", nameFromDB);
                        intent.putExtra("password", passwordFromDB);
                        intent.putExtra("imageURL", imageFromDB);

                        startActivity(intent);
                        finish();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        });



    }




}