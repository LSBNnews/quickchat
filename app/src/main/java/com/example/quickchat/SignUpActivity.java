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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private EditText signup_username, signup_password, signup_confirmPassword, signup_email;
    private Button signup_button;
    private TextView signup_loginRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        inializeFields();

        signup_loginRedirect.setOnClickListener(v -> startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));

        signup_button.setOnClickListener(v -> createNewAccount());
    }





    private void inializeFields() {
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        signup_username = findViewById(R.id.su_username);
        signup_password = findViewById(R.id.su_password);
        signup_confirmPassword = findViewById(R.id.su_confirm);
        signup_email = findViewById(R.id.su_email);
        signup_button = findViewById(R.id.su_button);
        signup_loginRedirect = findViewById(R.id.su_loginRedirect);
    }

    private void createNewAccount() {
        String username = signup_username.getText().toString().trim();
        String password = signup_password.getText().toString().trim();
        String confirmPassword = signup_confirmPassword.getText().toString().trim();
        String email = signup_email.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            signup_username.setError("Vui lòng điền tên người dùng");
            signup_username.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signup_email.setError("Vui lòng điền email hợp lệ");
            signup_email.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            signup_password.setError("Vui lòng điền mật khẩu");
            signup_password.requestFocus();
            return;
        }

        if (password.length() < 6) {
            signup_password.setError("Mật khẩu chứa tối thiểu 6 kí tự");
            signup_password.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            signup_confirmPassword.setError("Vui lòng điền xác nhận mật khẩu");
            signup_confirmPassword.requestFocus();
            return;
        }

        if (!confirmPassword.equals(password)) {
            signup_confirmPassword.setError("Mật khẩu và xác nhận mật khẩu không trùng nhau");
            signup_confirmPassword.requestFocus();
        }

        else {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();

                    HashMap<Object, Object> userData = new HashMap<>();
                    userData.put("id", userID);
                    userData.put("username", username);
                    userData.put("imageURL", "default");

                    reference.child("users").child(userID).setValue(userData).addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                            finish();
                        }
                    });
                }
                else {
                    Toast.makeText(SignUpActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
