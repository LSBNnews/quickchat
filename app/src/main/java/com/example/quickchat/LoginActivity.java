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

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText login_email, login_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        login_email = findViewById(R.id.log_email);
        login_password = findViewById(R.id.log_password);
        TextView login_forgotPassword = findViewById(R.id.log_forgotPassword);
        TextView login_signupRedirect = findViewById(R.id.log_signupRedirect);
        Button login_button = findViewById(R.id.log_button);
        Button login_google = findViewById(R.id.log_google);

        login_button.setOnClickListener(v -> {
            String email = login_email.getText().toString().trim();
            String password = login_password.getText().toString().trim();
            if(TextUtils.isEmpty(email)) {
                login_email.setError("Không để trống email");
                login_email.requestFocus();
                return;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                login_email.setError("Vui lòng điền email hợp lệ");
                login_email.requestFocus();
                return;
            }

            if(TextUtils.isEmpty(password)) {
                login_password.setError("Không để trống mật khẩu");
                login_password.requestFocus();
                return;
            }

            if(!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (!password.isEmpty()) {
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, HomeScreenActivity.class));
                                finish();
                            }).addOnFailureListener(e -> {
                                if (e instanceof FirebaseAuthInvalidUserException) {
                                    Toast.makeText(LoginActivity.this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                                }

                            });

                } else {
                    login_password.setError("Không để trống mật khẩu");
                }
            } else if(email.isEmpty()) {
                    login_email.setError("Không để trống email");
                } else {
                    login_email.setError("Vui lòng điền email hợp lệ");
                }
            });

        login_forgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
        login_signupRedirect.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
        login_google.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, HomeScreenActivity.class)));
    }
}