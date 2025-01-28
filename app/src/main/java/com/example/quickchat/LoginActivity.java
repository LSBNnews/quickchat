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

    private EditText login_name, login_password;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login_name = findViewById(R.id.log_name);
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
        login_google.setOnClickListener(v -> Toast.makeText(LoginActivity.this, "Tính năng này đang phát triển", Toast.LENGTH_SHORT).show());
    }

    private void loginUser() {
        String name = login_name.getText().toString().trim();
        String password = login_password.getText().toString().trim();

        // Kiểm tra đầu vào
        if (TextUtils.isEmpty(name)) {
            login_name.setError("Không để trống tên người dùng");
            login_name.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            login_password.setError("Không để trống mật khẩu");
            login_password.requestFocus();
            return;
        }

        reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("name").equalTo(name);
        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String passwordFromDB = snapshot.child(name).child("password").getValue(String.class);

                    if (passwordFromDB.equals(password)) {
                        String idfromDB = snapshot.child(name).child("id").getValue(String.class);
                        String namefromDB = snapshot.child(name).child("name").getValue(String.class);
                        String usernameFromDB = snapshot.child(name).child("username").getValue(String.class);
                        String emailFromDB = snapshot.child(name).child("email").getValue(String.class);
                        String imageFromDB = snapshot.child(name).child("imageURL").getValue(String.class);
                        Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class);

                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                        intent.putExtra("id", idfromDB);
                        intent.putExtra("name", namefromDB);
                        intent.putExtra("username", usernameFromDB);
                        intent.putExtra("email", emailFromDB);
                        intent.putExtra("password", passwordFromDB);
                        intent.putExtra("imageURL", imageFromDB);

                        startActivity(intent);
                        finish();
                    } else {
                        login_password.setError("Bạn nhập sai mật khẩu");
                        login_password.requestFocus();
                    }
                } else {
                    login_name.setError("Tài khoản này không tồn tại");
                    login_name.requestFocus();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

}
