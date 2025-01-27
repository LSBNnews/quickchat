package com.example.quickchat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    EditText edit_username, edit_email, edit_password;

    Button chooseImage;

    String nameUser, usernameUser, emailUser, imageUser, passwordUser;
    DatabaseReference reference;

    boolean isChanged = false;
    boolean isError = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edit_username = findViewById(R.id.ep_username);
        edit_password = findViewById(R.id.ep_password);
        edit_email = findViewById(R.id.ep_email);
        chooseImage = findViewById(R.id.ep_chooseImage);

        Button edit_backButton = findViewById(R.id.ep_backButton);
        Button edit_saveButton = findViewById(R.id.ep_saveButton);

        reference = FirebaseDatabase.getInstance().getReference("users");

        Intent intent = getIntent();
        nameUser = intent.getStringExtra("name");
        usernameUser = intent.getStringExtra("username");
        emailUser = intent.getStringExtra("email");
        imageUser = intent.getStringExtra("imageURL");
        passwordUser = intent.getStringExtra("password");

        edit_username.setText(usernameUser);
        edit_email.setText(emailUser);
        edit_image.setText(imageUser);
        edit_password.setText(passwordUser);

        // Xử lý tính năng cho Xác nhận
        edit_saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isError = false;

                if (edit_username.getText().toString().trim().isEmpty()) {
                    edit_username.setError("Tên người dùng không được để trống!");
                    isError = true;
                }
                if (edit_email.getText().toString().trim().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(edit_email.getText().toString()).matches()) {
                    edit_email.setError("Email không hợp lệ!");
                    isError = true;
                }
                if (edit_password.getText().toString().trim().isEmpty() || edit_password.getText().toString().length() < 6) {
                    edit_password.setError("Mật khẩu không hợp lệ");
                    isError = true;
                }
                if (edit_image.getText().toString().trim().isEmpty()) {
                    edit_image.setError(" Nội dung Image không được để trống!");
                    isError = true;
                }

                if (isError) return;


                if (isNameChanged() || isPasswordChanged() || isEmailChanged() || isImageChanged()){
                    Toast.makeText(EditProfileActivity.this, "Đã lưu thông tin thay đổi", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Không có thông tin nào bị thay đổi", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Xử lý tính năng cho Quay lại
        edit_backButton.setOnClickListener(v -> {
            String user = intent.getStringExtra("name");
            reference = FirebaseDatabase.getInstance().getReference("users");
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

                        Intent intent = new Intent(EditProfileActivity.this, HomeScreenActivity.class);
                        intent.putExtra("name", namefromDB);
                        intent.putExtra("username", usernamefromDB);
                        intent.putExtra("email", emailFromDB);
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


    // Các hàm liên quan để hỗ trợ
    private boolean isNameChanged() {
        if (!usernameUser.equals(edit_username.getText().toString().trim())){
            reference.child(nameUser).child("username").setValue(edit_username.getText().toString().trim());
            usernameUser = edit_username.getText().toString().trim();
            return true;
        } else {
            return false;
        }
    }
    private boolean isEmailChanged() {
        if (!emailUser.equals(edit_email.getText().toString().trim())){
            reference.child(nameUser).child("email").setValue(edit_email.getText().toString().trim());
            emailUser = edit_email.getText().toString().trim();
            return true;
        } else {
            return false;
        }
    }
    private boolean isPasswordChanged() {
        if (!passwordUser.equals(edit_password.getText().toString().trim())){
            reference.child(nameUser).child("password").setValue(edit_password.getText().toString().trim());
            passwordUser = edit_password.getText().toString().trim();
            return true;
        } else {
            return false;
        }
    }

    private boolean isImageChanged() {
        if (!imageUser.equals(edit_image.getText().toString().trim())){
            reference.child(nameUser).child("imageURL").setValue(edit_image.getText().toString().trim());
            imageUser = edit_image.getText().toString().trim();
            return true;
        } else {
            return false;
        }
    }



}