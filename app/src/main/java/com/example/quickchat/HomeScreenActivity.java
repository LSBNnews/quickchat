package com.example.quickchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.quickchat.adapter.RecentChatAdapter;
import com.example.quickchat.model.RecentChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeScreenActivity extends AppCompatActivity {

    private ListView recyclerRecentChats;
    private RecentChatAdapter recentChatAdapter;
    private List<RecentChat> recentChats;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private Button hs_setting, hs_profile, hs_signout, hs_search, hs_block_user;
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
        // Xử lý sự kiện click vào item trong danh sách cuộc trò chuyện gần đây
        recyclerRecentChats.setOnItemClickListener((parent, view, position, id) -> {
            RecentChat recentChat = recentChats.get(position);
            String chatId = recentChat.getChatId();
            // Lấy thông tin người dùng khác từ danh sách participants
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String otherUserId = recentChat.getParticipants().get(0).equals(currentUserId) ? recentChat.getParticipants().get(1) : recentChat.getParticipants().get(0);
            // Kiểm tra xem người dùng khác có bị chặn hay không
            DatabaseReference blockedUsersRef = FirebaseDatabase.getInstance().getReference()
                    .child("blockedUsers")
                    .child(currentUserId)
                    .child(otherUserId);
            blockedUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean isBlocked = dataSnapshot.exists() && dataSnapshot.getValue(Boolean.class);
                    if (isBlocked) {
                        Toast.makeText(HomeScreenActivity.this, "Bạn đã chặn người dùng này", Toast.LENGTH_SHORT).show();
                    } else {
                        // Lấy tên người dùng khác từ Firebase
                        DatabaseReference userRef = reference.child(otherUserId);
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String targetUsername = dataSnapshot.child("username").getValue(String.class);
                                Intent intent = new Intent(HomeScreenActivity.this, ChatActivity.class);
                                intent.putExtra("targetUserId", otherUserId);
                                intent.putExtra("targetUsername", targetUsername);
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("HomeScreenActivity", "Lỗi khi lấy thông tin người dùng: " + databaseError.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("HomeScreenActivity", "Lỗi khi kiểm tra trạng thái chặn: " + databaseError.getMessage());
                }
            });
        });
        // Xử lý nút chặn người dùng
        hs_block_user.setOnClickListener(v -> {
            // Lấy ID của người dùng cần chặn (ví dụ: từ intent hoặc context)
            String targetUserId = getIntent().getStringExtra("targetUserId");
            if (targetUserId != null) {
                blockUser(targetUserId);
            } else {
                Toast.makeText(HomeScreenActivity.this, "Không thể xác định người dùng cần chặn.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeFields() {
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("users");
        currentUser = auth.getCurrentUser();
        recyclerRecentChats = findViewById(R.id.recycler_recent_chats);
        recentChats = new ArrayList<>();
        recentChatAdapter = new RecentChatAdapter(this, recentChats);
        recyclerRecentChats.setAdapter(recentChatAdapter);
        hs_setting = findViewById(R.id.hs_setting);
        hs_profile = findViewById(R.id.hs_profile);
        hs_signout = findViewById(R.id.hs_signout);
        hs_search = findViewById(R.id.hs_search);
        hs_block_user = findViewById(R.id.hs_block_user);
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
                    GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                    List<String> participants = chatSnapshot.child("participants").getValue(t);
                    String lastSenderId = chatSnapshot.child("lastSenderId").getValue(String.class); // Đọc lastSenderId
                    RecentChat recentChat = new RecentChat(chatId, lastMessage, timestamp, participants, lastSenderId);
                    recentChats.add(recentChat);
                }
                // Sắp xếp danh sách theo thời gian mới nhất
                Collections.sort(recentChats, new Comparator<RecentChat>() {
                    @Override
                    public int compare(RecentChat c1, RecentChat c2) {
                        return Long.compare(c2.getTimestamp(), c1.getTimestamp());
                    }
                });
                // Cập nhật ListView
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

    private void blockUser(String targetUserId) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Xóa cuộc trò chuyện gần đây của người dùng hiện tại
        DatabaseReference recentChatsRef = FirebaseDatabase.getInstance().getReference()
                .child("recentChats")
                .child(currentUserId);
        recentChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                    List<String> participants = chatSnapshot.child("participants").getValue(new GenericTypeIndicator<List<String>>() {});
                    if (participants != null && participants.contains(targetUserId)) {
                        chatSnapshot.getRef().removeValue(); // Xóa cuộc trò chuyện
                    }
                }
                // Cập nhật danh sách recentChats trên giao diện
                setupRecentChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeScreenActivity", "Lỗi khi xóa cuộc trò chuyện: " + databaseError.getMessage());
            }
        });
        // Xóa cuộc trò chuyện gần đây của người bị chặn (tùy chọn)
        DatabaseReference targetRecentChatsRef = FirebaseDatabase.getInstance().getReference()
                .child("recentChats")
                .child(targetUserId);
        targetRecentChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                    List<String> participants = chatSnapshot.child("participants").getValue(new GenericTypeIndicator<List<String>>() {});
                    if (participants != null && participants.contains(currentUserId)) {
                        chatSnapshot.getRef().removeValue(); // Xóa cuộc trò chuyện
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeScreenActivity", "Lỗi khi xóa cuộc trò chuyện: " + databaseError.getMessage());
            }
        });
        // Cập nhật trạng thái chặn trong Firebase
        DatabaseReference blockedUsersRef = FirebaseDatabase.getInstance().getReference()
                .child("blockedUsers")
                .child(currentUserId)
                .child(targetUserId);
        blockedUsersRef.setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(HomeScreenActivity.this, "Người dùng đã bị chặn và cuộc trò chuyện đã bị xóa.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HomeScreenActivity.this, "Lỗi khi chặn người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}