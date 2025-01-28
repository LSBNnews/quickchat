package com.example.quickchat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgotPasswordActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText forgotPassword_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Liên kết các view
        forgotPassword_email = findViewById(R.id.fpEmail);
        auth = FirebaseAuth.getInstance();
        Button forgotPassword_back = findViewById(R.id.fpBack);
        Button forgotPassword_confirm = findViewById(R.id.fpConfirm);

        forgotPassword_back.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        });

        // Xử lý khi nhấn nút "Xác nhận"
        forgotPassword_confirm.setOnClickListener(v -> {
            String userEmail = forgotPassword_email.getText().toString();

            // Kiểm tra email có rỗng không
            if (TextUtils.isEmpty(userEmail)) {
                forgotPassword_email.setError("Không được để trống email");
                forgotPassword_email.requestFocus();
                return;
            }

            // Kiểm tra email có đúng định dạng không
            if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                forgotPassword_email.setError("Vui lòng nhập email hợp lệ");
                forgotPassword_email.requestFocus();
                return;
            }

            // Nếu email hợp lệ, gửi yêu cầu reset mật khẩu
            auth.sendPasswordResetEmail(userEmail).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Vui lòng kiểm tra email của bạn!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                    finish();
                } else {
                    String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });


    }
}
