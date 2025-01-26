package com.example.quickchat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quickchat.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText signup_username, signup_password, signup_confirmPassword, signup_email;
    DatabaseReference reference;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Liên kết các view
        signup_username = findViewById(R.id.su_username);
        signup_password = findViewById(R.id.su_password);
        signup_confirmPassword = findViewById(R.id.su_confirm);
        signup_email = findViewById(R.id.su_email);
        Button signup_button = findViewById(R.id.su_button);
        TextView signup_loginRedirect = findViewById(R.id.su_loginRedirect);

        // Chuyển hướng từ Đăng ký sang Đăng nhập
        signup_loginRedirect.setOnClickListener(v -> startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));

        // Xử lý tính năng khi nhấn Đăng ký
        signup_button.setOnClickListener(v -> {

            database = FirebaseDatabase.getInstance();
            reference = database.getReference("users");

            String username = signup_username.getText().toString().trim();
            String password = signup_password.getText().toString().trim();
            String confirmPassword = signup_confirmPassword.getText().toString().trim();
            String email = signup_email.getText().toString().trim();

            if(TextUtils.isEmpty(username)) {
                signup_username.setError("Không để trống tên người dùng");
                signup_username.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                signup_password.setError("Không để trống mật khẩu");
                signup_password.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(confirmPassword)) {
                signup_confirmPassword.setError("Không để trống xác nhận mật khẩu");
                signup_confirmPassword.requestFocus();
                return;
            }
            if (!confirmPassword.equals(password)) {
                signup_confirmPassword.setError("Mật khẩu không khớp");
                signup_confirmPassword.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(email)) {
                signup_email.setError("Không để trống email");
                signup_email.requestFocus();
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                signup_email.setError("Vui lòng điền email hợp lệ");
                signup_email.requestFocus();
            }

            else {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = new User(username, email, password, "default");
                        reference.child(username).setValue(user);

                        Toast.makeText(SignUpActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                        finish();
                    }
                    else {
                     Toast.makeText(SignUpActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
    }
}
