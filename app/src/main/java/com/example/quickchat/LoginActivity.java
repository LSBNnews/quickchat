package com.example.quickchat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText login_username, login_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login_username = findViewById(R.id.log_username);
        login_password = findViewById(R.id.log_password);
        TextView login_forgotPassword = findViewById(R.id.log_forgotPassword);
        TextView login_signupRedirect = findViewById(R.id.log_signupRedirect);
        Button login_button = findViewById(R.id.log_button);
        Button login_google = findViewById(R.id.log_google);

        // Xử lý tính năng đăng nhập
        login_button.setOnClickListener(v -> loginUser());

        // Chuyển từ Đăng nhập sang Quên mật khẩu
        login_forgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        // CHuyển màn hình từ Đăng nhập sang Đăng ký
        login_signupRedirect.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));

        // Xử lý đăng nhập bằng Google
        login_google.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, HomeScreenActivity.class)));
    }

    private void loginUser() {
        String userUsername = login_username.getText().toString().trim();
        String userPassword = login_password.getText().toString().trim();

        // Kiểm tra đầu vào
        if (TextUtils.isEmpty(userUsername)) {
            login_username.setError("Không để trống tên người dùng");
            login_username.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userPassword)) {
            login_password.setError("Không để trống mật khẩu");
            login_password.requestFocus();
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);
        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    login_username.setError(null);
                    String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);
                    if (passwordFromDB.equals(userPassword)) {
                        login_username.setError(null);
                        String nameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);
                        String emailFromDB = snapshot.child(userUsername).child("email").getValue(String.class);
                        String imageFromDB = snapshot.child(userUsername).child("imageURL").getValue(String.class);
                        Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class);
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        intent.putExtra("username", nameFromDB);
                        intent.putExtra("email", emailFromDB);
                        intent.putExtra("password", passwordFromDB);
                        intent.putExtra("imageURL", imageFromDB);
                        startActivity(intent);
                        finish();
                    } else {
                        login_password.setError("Sai mật khẩu");
                        login_password.requestFocus();
                    }
                } else {
                    login_username.setError("Tài khoản không tồn tại");
                    login_username.requestFocus();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

}
