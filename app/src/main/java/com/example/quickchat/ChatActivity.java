package com.example.quickchat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.quickchat.adapter.MessageAdapter;
import com.example.quickchat.model.Message;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private Toolbar chatToolbar;
    private CircleImageView chatUserImage;
    private TextView chatUserName;
    private EditText chatInput;
    private Button chatSendButton;
    private Button chatMapButton;
    private Button chatBlockButton;
    private ListView chatList;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private String targetUserId, targetUsername, chatId;
    private List<Message> messages;
    private MessageAdapter messageAdapter;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private NotificationManager notificationManager; // Thêm biến quản lý thông báo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initializeFields();
        setupToolbar();
        setupMessageList();
        setupSendMessageButton();
        setupMapButton();
        setupBlockButton();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupLocationCallback();

        // Khởi tạo NotificationManager
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    // Khởi tạo các biến
    private void initializeFields() {
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        chatToolbar = findViewById(R.id.chat_toolbar);
        chatUserImage = findViewById(R.id.chat_user_image);
        chatUserName = findViewById(R.id.chat_user_name);
        chatInput = findViewById(R.id.chat_input);
        chatSendButton = findViewById(R.id.chat_send_button);
        chatMapButton = findViewById(R.id.chat_map_button);
        chatBlockButton = findViewById(R.id.chat_block_button);
        chatList = findViewById(R.id.chat_list);
        targetUserId = getIntent().getStringExtra("targetUserId");
        targetUsername = getIntent().getStringExtra("targetUsername");
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages);
        chatList.setAdapter(messageAdapter);
        chatId = getOrCreateChatId(auth.getCurrentUser().getUid(), targetUserId);
    }

    // Thiết lập toolbar
    private void setupToolbar() {
        setSupportActionBar(chatToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        chatUserName.setText(targetUsername);
        DatabaseReference userRef = reference.child("users").child(targetUserId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String imageURL = dataSnapshot.child("imageURL").getValue(String.class);
                if (imageURL != null && !imageURL.equals("default")) {
                    Glide.with(ChatActivity.this)
                            .load(imageURL)
                            .into(chatUserImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatActivity", "Lỗi khi lấy thông tin người dùng: " + databaseError.getMessage());
            }
        });
    }

    // Cập nhật danh sách tin nhắn
    private void setupMessageList() {
        DatabaseReference chatRef = reference.child("chats").child(chatId).child("messages");
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    messages.add(message);
                }
                messageAdapter.notifyDataSetChanged();
                chatList.setSelection(messages.size() - 1);
                if (!messages.isEmpty()) {
                    Message lastMessage = messages.get(messages.size() - 1);
                    // Use current time as a fallback or get timestamp from message
                    long timestamp = lastMessage.getTimestamp(); // Assuming Message has a getTimestamp() method
                    String lastSenderId = lastMessage.getSenderId();
                    updateRecentChats(chatId, lastMessage.getContent(), timestamp, lastSenderId);

                    // Hiển thị thông báo nếu tin nhắn mới từ người khác
                    if (!lastMessage.getSenderId().equals(auth.getCurrentUser().getUid())) {
                        showNotification(lastMessage.getContent());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatActivity", "Lỗi khi lấy danh sách tin nhắn: " + databaseError.getMessage());
            }
        });
    }

    // Hiển thị thông báo
    private void showNotification(String messageContent) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "quickchat_channel";
        String channelName = "Quick Chat Notifications";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification) // Đảm bảo có icon này
                .setContentTitle("Tin nhắn mới từ " + targetUsername) // Hiển thị tên người dùng
                .setContentText(messageContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // Thiết lập nút gửi tin nhắn
    private void setupSendMessageButton() {
        chatSendButton.setOnClickListener(v -> sendMessage());
        chatInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chatSendButton.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Thiết lập nút gửi vị trí
    private void setupMapButton() {
        chatMapButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                requestCurrentLocation();
            }
        });
    }

    // Thiết lập callback vị trí
    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    sendLocationMessage(location.getLatitude(), location.getLongitude());
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                    break;
                }
            }
        };
    }

    // Yêu cầu vị trí hiện tại
    private void requestCurrentLocation() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(500);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void sendMessage() {
        String messageContent = chatInput.getText().toString().trim();
        if (!messageContent.isEmpty()) {
            String currentUserId = auth.getCurrentUser().getUid();
            DatabaseReference chatRef = reference.child("chats").child(chatId).child("messages");
            String messageId = chatRef.push().getKey();
            long currentTime = System.currentTimeMillis();
            Message message = new Message(currentUserId, messageContent, currentTime);
            Map<String, Object> messageData = new HashMap<>();
            messageData.put(messageId, message);
            chatRef.updateChildren(messageData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    chatInput.setText("");
                    updateRecentChats(chatId, messageContent, currentTime, currentUserId); // Truyền currentTime thay vì ServerValue.TIMESTAMP
                } else {
                    Toast.makeText(ChatActivity.this, "Lỗi khi gửi tin nhắn", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendLocationMessage(double latitude, double longitude) {
        String mapUrl = "https://maps.google.com/?q=" + latitude + "," + longitude;
        String messageContent = "Vị trí của tôi: " + mapUrl + "";
        String currentUserId = auth.getCurrentUser().getUid();
        DatabaseReference chatRef = reference.child("chats").child(chatId).child("messages");
        String messageId = chatRef.push().getKey();
        long currentTime = System.currentTimeMillis();
        Message message = new Message(currentUserId, messageContent, currentTime);
        Map<String, Object> messageData = new HashMap<>();
        messageData.put(messageId, message);
        chatRef.updateChildren(messageData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatInput.setText("");
                updateRecentChats(chatId, messageContent, currentTime, currentUserId); // Truyền currentTime thay vì ServerValue.TIMESTAMP
            } else {
                Toast.makeText(ChatActivity.this, "Lỗi khi gửi tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecentChats(String chatId, String lastMessage, long timestamp, String lastSenderId) {
        String currentUserId = auth.getCurrentUser().getUid();
        DatabaseReference recentChatsRef = reference.child("recentChats").child(currentUserId).child(chatId);
        Map<String, Object> recentChatData = new HashMap<>();
        recentChatData.put("lastMessage", lastMessage);
        recentChatData.put("timestamp", timestamp);
        recentChatData.put("lastSenderId", lastSenderId); // Thêm lastSenderId
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(targetUserId);
        recentChatData.put("participants", participants);
        recentChatsRef.updateChildren(recentChatData);
        DatabaseReference recipientRecentChatsRef = reference.child("recentChats").child(targetUserId).child(chatId);
        recipientRecentChatsRef.updateChildren(recentChatData);
    }

    // Tạo/đọc ID chat
    private String getOrCreateChatId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

    // Quay lại HomeScreen
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChatActivity.this, HomeScreenActivity.class);
        startActivity(intent);
        finish();
    }

    // Xử lý quyền truy cập vị trí
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestCurrentLocation();
            } else {
                Toast.makeText(this, "Quyền truy cập vị trí bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Thiết lập nút chặn
    private void setupBlockButton() {
        chatBlockButton.setOnClickListener(v -> blockUserFromChat());
    }

    // Chặn người dùng
    private void blockUserFromChat() {
        String targetUserId = getIntent().getStringExtra("targetUserId");
        if (targetUserId != null) {
            // Xóa cuộc trò chuyện gần đây của người dùng hiện tại
            DatabaseReference recentChatsRef = FirebaseDatabase.getInstance().getReference()
                    .child("recentChats")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            recentChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                        List<String> participants = chatSnapshot.child("participants").getValue(new GenericTypeIndicator<List<String>>() {});
                        if (participants != null && participants.contains(targetUserId)) {
                            chatSnapshot.getRef().removeValue();
                        }
                    }
                    Toast.makeText(ChatActivity.this, "Cuộc trò chuyện đã bị xóa.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("ChatActivity", "Lỗi khi xóa cuộc trò chuyện: " + databaseError.getMessage());
                }
            });
            // Cập nhật trạng thái chặn
            DatabaseReference blockedUsersRef = FirebaseDatabase.getInstance().getReference()
                    .child("blockedUsers")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(targetUserId);
            blockedUsersRef.setValue(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ChatActivity.this, "Người dùng đã bị chặn.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChatActivity.this, "Lỗi khi chặn người dùng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}