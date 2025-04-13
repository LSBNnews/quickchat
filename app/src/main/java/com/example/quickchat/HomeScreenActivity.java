package com.example.quickchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quickchat.adapter.RecentChatAdapter;
import com.example.quickchat.model.RecentChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeScreenActivity extends AppCompatActivity {

    private RecyclerView recyclerRecentChats;
    private RecentChatAdapter recentChatAdapter;
    private List<RecentChat> recentChats;

    private DatabaseReference reference;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private Button hs_setting, hs_profile, hs_signout, hs_search;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        initializeFields();

        // Thiết lập thông tin người dùng
        displayUserInfo();

        // Thiết lập danh sách cuộc trò chuyện gần đây
        setupRecentChats();

        // Xử lý nút "Tìm kiếm"
        hs_search.setOnClickListener(v -> startActivity(new Intent(HomeScreenActivity.this, SearchActivity.class)));

        // Xử lý nút "Đăng xuất"
        hs_signout.setOnClickListener(v -> signout());

        // Chuyển đến trang chỉnh sửa profile
        hs_profile.setOnClickListener(v -> startActivity(new Intent(HomeScreenActivity.this, EditProfileActivity.class)));

        // Chuyển đến trang cài đặt
        hs_setting.setOnClickListener(v -> startActivity(new Intent(HomeScreenActivity.this, SettingsActivity.class)));
    }

    private void initializeFields() {
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("users");
        currentUser = auth.getCurrentUser();
        recyclerRecentChats = findViewById(R.id.recycler_recent_chats);

        recentChats = new ArrayList<>();
        recentChatAdapter = new RecentChatAdapter(recentChats);
        recyclerRecentChats.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecentChats.setAdapter(recentChatAdapter);

        hs_setting = findViewById(R.id.hs_setting);
        hs_profile = findViewById(R.id.hs_profile);
        hs_signout = findViewById(R.id.hs_signout);
        hs_search = findViewById(R.id.hs_search);

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải thông tin...");
        progressDialog.setCancelable(false);
        progressDialog.show(); // Hiển thị ProgressDialog ngay khi bắt đầu
    }

    private void displayUserInfo() {
        String currentUserID = currentUser.getUid();
        reference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                if (data.exists()) {
                    String retrieveUsername = data.child("username").getValue(String.class);
                    String retrieveImage = data.child("imageURL").getValue(String.class);

                    if (data.hasChild("description")) {
                        String retrieveDescription = data.child("description").getValue(String.class);
                        TextView descriptionTextView = findViewById(R.id.hs_description);
                        descriptionTextView.setText(retrieveDescription);
                    }

                    TextView usernameTextView = findViewById(R.id.hs_username);
                    usernameTextView.setText(retrieveUsername);

                    ImageView imageView = findViewById(R.id.hs_image);
                    Glide.with(HomeScreenActivity.this)
                            .load(retrieveImage.equals("default") ? R.mipmap.ic_launcher : retrieveImage)
                            .into(imageView);
                } else {
                    Toast.makeText(HomeScreenActivity.this, "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(HomeScreenActivity.this, LoginActivity.class));
                    finish();
                }
                progressDialog.dismiss(); // Ẩn ProgressDialog sau khi tải xong thông tin người dùng
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeScreenActivity", "Lỗi khi đọc dữ liệu người dùng: " + error.getMessage());
                progressDialog.dismiss(); // Ẩn ProgressDialog nếu có lỗi
            }
        });
    }

    private void setupRecentChats() {
        String currentUserId = auth.getCurrentUser().getUid();
        DatabaseReference recentChatsRef = FirebaseDatabase.getInstance().getReference()
                .child("recentChats")
                .child(currentUserId);

        recentChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recentChats.clear();
                for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                    String chatId = chatSnapshot.getKey();
                    String lastMessage = chatSnapshot.child("lastMessage").getValue(String.class);
                    long timestamp = chatSnapshot.child("timestamp").getValue(Long.class);
                    List<String> participants = chatSnapshot.child("participants").getValue(List.class);

                    RecentChat recentChat = new RecentChat(chatId, lastMessage, timestamp, participants);
                    recentChats.add(recentChat);
                }

                // Sắp xếp danh sách theo thời gian mới nhất
                Collections.sort(recentChats, new Comparator<RecentChat>() {
                    @Override
                    public int compare(RecentChat c1, RecentChat c2) {
                        return Long.compare(c2.timestamp, c1.timestamp);
                    }
                });

                // Cập nhật RecyclerView
                recentChatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeScreenActivity", "Lỗi khi lấy danh sách cuộc trò chuyện gần đây: " + databaseError.getMessage());
            }
        });
    }

    private void signout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(HomeScreenActivity.this, "Bạn đã đăng xuất khỏi ứng dụng", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(HomeScreenActivity.this, LoginActivity.class));
        finish();
    }
}