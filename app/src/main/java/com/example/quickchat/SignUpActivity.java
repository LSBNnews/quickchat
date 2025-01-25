package com.example.quickchat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText signup_username, signup_password, signup_confirmPassword, signup_email;
    private Button signup_button;
    private TextView signup_loginRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Liên kết các view
        signup_username = findViewById(R.id.signup_username);
        signup_password = findViewById(R.id.signup_password);
        signup_confirmPassword = findViewById(R.id.signup_confirm);
        signup_email = findViewById(R.id.signup_email);
        signup_button = findViewById(R.id.signup_button);
        signup_loginRedirect = findViewById(R.id.login_redirect_text);

        // Sự kiện khi người dùng nhấn nút đăng ký
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();

                // Kiểm tra thông tin nhập vào và xử lý đăng ký ở đây
            }
        });
    }
}
