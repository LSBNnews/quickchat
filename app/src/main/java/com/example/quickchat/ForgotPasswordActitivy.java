package com.example.quickchat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActitivy extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText forgotPassword_email = findViewById(R.id.fpEmail);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();
        Button forgotPassword_back = findViewById(R.id.fpBack);
        Button forgotPassword_confirm = findViewById(R.id.fpConfirm);

        forgotPassword_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = forgotPassword_email.getText().toString().trim();
                if(userEmail.isEmpty()) forgotPassword_email.setError("Không được để trống email");

                if(TextUtils.isEmpty(userEmail) && !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                    Toast.makeText(ForgotPasswordActitivy.this, "Nhập email bạn đã đăng kí", Toast.LENGTH_SHORT).show();
                    return;
                }
                auth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActitivy.this, "Vui lòng kiểm tra email của bạn !", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ForgotPasswordActitivy.this, "Thao tác thất bại !", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });
        forgotPassword_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgotPasswordActitivy.this, LoginActivity.class));
            }
        });


    }
}