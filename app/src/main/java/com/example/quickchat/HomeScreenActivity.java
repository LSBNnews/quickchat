package com.example.quickchat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.bumptech.glide.Glide;
import com.example.quickchat.adapter.RecentChatAdapter;
import com.example.quickchat.model.Message;
import com.example.quickchat.model.RecentChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
    private NotificationManager notificationManager; // Thêm NotificationManager
    private boolean isForeground = false; // Theo dõi trạng thái foreground

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        initializeFields();
        displayUserInfo();
        setupRecentChats();
        setupMessageListener(); // Thêm listener cho tin nhắn mới

        hs_search.setOnClickListener(v -> startActivity(new Intent(HomeScreenActivity.this, SearchActivity.class)));
        hs_signout.setOnClickListener(v -> signout());
        hs_profile.setOnClickListener(v -> startActivity(new Intent(HomeScreenActivity.this, EditProfileActivity.class)));
        hs_setting.setOnClickListener(v -> startActivity(new Intent(HomeScreenActivity.this, SettingsActivity.class)));

        recyclerRecentChats.setOnItemClickListener((parent, view, position, id) -> {
            RecentChat recentChat = recentChats.get(position);
            String chatId = recentChat.getChatId();
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String otherUserId = recentChat.getParticipants().get(0).equals(currentUserId) ? recentChat.getParticipants().get(1) : recentChat.getParticipants().get(0);
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

        hs_block_user.setOnClickListener(v -> {
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
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải thông tin...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // Khởi tạo NotificationManager
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeground = true; // Cập nhật trạng thái foreground
    }

    @Override
    protected void onPause() {
        super.onPause();
        isForeground = false; // Cập nhật trạng thái khi không ở foreground
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
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeScreenActivity", "Lỗi khi đọc dữ liệu người dùng: " + error.getMessage());
                progressDialog.dismiss();
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
                    String lastSenderId = chatSnapshot.child("lastSenderId").getValue(String.class);
                    RecentChat recentChat = new RecentChat(chatId, lastMessage, timestamp, participants, lastSenderId);
                    recentChats.add(recentChat);
                }
                Collections.sort(recentChats, new Comparator<RecentChat>() {
                    @Override
                    public int compare(RecentChat c1, RecentChat c2) {
                        return Long.compare(c2.getTimestamp(), c1.getTimestamp());
                    }
                });
                recentChatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeScreenActivity", "Lỗi khi lấy danh sách cuộc trò chuyện gần đây: " + databaseError.getMessage());
            }
        });
    }

    private void setupMessageListener() {
        String currentUserId = auth.getCurrentUser().getUid();
        DatabaseReference recentChatsRef = FirebaseDatabase.getInstance().getReference()
                .child("recentChats")
                .child(currentUserId);

        recentChatsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                String chatId = dataSnapshot.getKey();
                String lastMessage = dataSnapshot.child("lastMessage").getValue(String.class);
                String lastSenderId = dataSnapshot.child("lastSenderId").getValue(String.class);
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                List<String> participants = dataSnapshot.child("participants").getValue(t);

                // Tìm targetUserId (người gửi tin nhắn)
                String targetUserId = participants.get(0).equals(currentUserId) ? participants.get(1) : participants.get(0);

                // Chỉ hiển thị thông báo nếu tin nhắn từ người khác và activity ở foreground
                if (!lastSenderId.equals(currentUserId) && isForeground) {
                    // Lấy tên người gửi để hiển thị trong thông báo
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                            .child("users")
                            .child(targetUserId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            String targetUsername = userSnapshot.child("username").getValue(String.class);
                            showNotification(targetUsername, lastMessage);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("HomeScreenActivity", "Lỗi khi lấy thông tin người dùng: " + databaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Xử lý khi cuộc trò chuyện được cập nhật
                String chatId = dataSnapshot.getKey();
                String lastMessage = dataSnapshot.child("lastMessage").getValue(String.class);
                String lastSenderId = dataSnapshot.child("lastSenderId").getValue(String.class);
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                List<String> participants = dataSnapshot.child("participants").getValue(t);

                String targetUserId = participants.get(0).equals(currentUserId) ? participants.get(1) : participants.get(0);

                if (!lastSenderId.equals(currentUserId) && isForeground) {
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                            .child("users")
                            .child(targetUserId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            String targetUsername = userSnapshot.child("username").getValue(String.class);
                            showNotification(targetUsername, lastMessage);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("HomeScreenActivity", "Lỗi khi lấy thông tin người dùng: " + databaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeScreenActivity", "Lỗi khi lắng nghe tin nhắn mới: " + databaseError.getMessage());
            }
        });
    }

    private void showNotification(String targetUsername, String messageContent) {
        String channelId = "quickchat_channel";
        String channelName = "Quick Chat Notifications";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Tin nhắn mới từ " + targetUsername)
                .setContentText(messageContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void signout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(HomeScreenActivity.this, "Bạn đã đăng xuất khỏi ứng dụng", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(HomeScreenActivity.this, LoginActivity.class));
        finish();
    }

    private void blockUser(String targetUserId) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference recentChatsRef = FirebaseDatabase.getInstance().getReference()
                .child("recentChats")
                .child(currentUserId);
        recentChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                    List<String> participants = chatSnapshot.child("participants").getValue(new GenericTypeIndicator<List<String>>() {});
                    if (participants != null && participants.contains(targetUserId)) {
                        chatSnapshot.getRef().removeValue();
                    }
                }
                setupRecentChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeScreenActivity", "Lỗi khi xóa cuộc trò chuyện: " + databaseError.getMessage());
            }
        });

        DatabaseReference targetRecentChatsRef = FirebaseDatabase.getInstance().getReference()
                .child("recentChats")
                .child(targetUserId);
        targetRecentChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                    List<String> participants = chatSnapshot.child("participants").getValue(new GenericTypeIndicator<List<String>>() {});
                    if (participants != null && participants.contains(currentUserId)) {
                        chatSnapshot.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeScreenActivity", "Lỗi khi xóa cuộc trò chuyện: " + databaseError.getMessage());
            }
        });

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