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

public class ForgotPasswordActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private  EditText forgotPassword_email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        forgotPassword_email = findViewById(R.id.fpEmail);
        auth = FirebaseAuth.getInstance();
        Button forgotPassword_back = findViewById(R.id.fpBack);
        Button forgotPassword_confirm = findViewById(R.id.fpConfirm);

        forgotPassword_confirm.setOnClickListener(v -> {
            String userEmail = forgotPassword_email.getText().toString().trim();
            if(userEmail.isEmpty()) forgotPassword_email.setError("Không được để trống email");

            if(TextUtils.isEmpty(userEmail) && !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                Toast.makeText(ForgotPasswordActivity.this, "Nhập email bạn đã đăng kí", Toast.LENGTH_SHORT).show();
                return;
            }
            auth.sendPasswordResetEmail(userEmail).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Vui lòng kiểm tra email của bạn !", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Thao tác thất bại !", Toast.LENGTH_SHORT).show();

                }
            });
        });
        forgotPassword_back.setOnClickListener(v -> startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class)));


    }
}