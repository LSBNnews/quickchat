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
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.quickchat.adapter.MessageAdapter;
import com.example.quickchat.model.Message;
import com.example.quickchat.model.RecentChat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private NotificationManager notificationManager;
    private List<RecentChat> recentChats;
    private List<String> recentChatUserIds;
    private List<String> recentChatUserNames;
    private Set<String> recentlyForwardedMessages; // Track forwarded messages to prevent duplicates

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
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        setupRecentChats();
    }

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
        messageAdapter = new MessageAdapter(this, messages, this::forwardMessage);
        chatList.setAdapter(messageAdapter);
        chatId = getOrCreateChatId(auth.getCurrentUser().getUid(), targetUserId);
        recentChats = new ArrayList<>();
        recentChatUserIds = new ArrayList<>();
        recentChatUserNames = new ArrayList<>();
        recentlyForwardedMessages = new HashSet<>(); // Initialize the set for tracking forwarded messages
    }

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

    private void setupMessageList() {
        DatabaseReference chatRef = reference.child("chats").child(chatId).child("messages");

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    messages.add(message);
                }
                messageAdapter.notifyDataSetChanged();
                chatList.setSelection(messages.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatActivity", "Lỗi khi lấy danh sách tin nhắn: " + databaseError.getMessage());
            }
        });

        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    messages.add(message);
                    messageAdapter.notifyDataSetChanged();
                    chatList.setSelection(messages.size() - 1);
                    long timestamp = message.getTimestamp();
                    String lastSenderId = message.getSenderId();
                    updateRecentChats(chatId, message.getContent(), timestamp, lastSenderId, targetUserId);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatActivity", "Lỗi khi lắng nghe tin nhắn mới: " + databaseError.getMessage());
            }
        });
    }

    private void showNotification(String messageContent) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

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

    private void setupMapButton() {
        chatMapButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                requestCurrentLocation();
            }
        });
    }

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

    private void requestCurrentLocation() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(500);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, android.os.Looper.getMainLooper());
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
                    updateRecentChats(chatId, messageContent, currentTime, currentUserId, targetUserId);
                } else {
                    Toast.makeText(ChatActivity.this, "Lỗi khi gửi tin nhắn", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendLocationMessage(double latitude, double longitude) {
        String mapUrl = "https://maps.google.com/?q=" + latitude + "," + longitude;
        String messageContent = "Vị trí của tôi: " + mapUrl;
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
                updateRecentChats(chatId, messageContent, currentTime, currentUserId, targetUserId);
            } else {
                Toast.makeText(ChatActivity.this, "Lỗi khi gửi tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecentChats(String chatId, String lastMessage, long timestamp, String lastSenderId, String receiverUserId) {
        String currentUserId = auth.getCurrentUser().getUid();
        DatabaseReference recentChatsRef = reference.child("recentChats").child(currentUserId).child(chatId);
        Map<String, Object> recentChatData = new HashMap<>();
        recentChatData.put("lastMessage", lastMessage);
        recentChatData.put("timestamp", timestamp);
        recentChatData.put("lastSenderId", lastSenderId);

        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(receiverUserId);
        recentChatData.put("participants", participants);

        recentChatsRef.updateChildren(recentChatData);
        DatabaseReference recipientRecentChatsRef = reference.child("recentChats").child(receiverUserId).child(chatId);
        recipientRecentChatsRef.updateChildren(recentChatData);
    }

    private String getOrCreateChatId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

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

    private void setupBlockButton() {
        chatBlockButton.setOnClickListener(v -> blockUserFromChat());
    }

    private void blockUserFromChat() {
        String targetUserId = getIntent().getStringExtra("targetUserId");
        if (targetUserId != null) {
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

    private void forwardMessage(Message message) {
        if (recentChatUserNames.isEmpty()) {
            Toast.makeText(this, "Đang tải danh sách người dùng, vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn người chuyển tiếp tin nhắn");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, recentChatUserNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = new Spinner(this);
        spinner.setAdapter(adapter);

        builder.setView(spinner);
        builder.setPositiveButton("Gửi", (dialog, which) -> {
            int selectedPosition = spinner.getSelectedItemPosition();
            String selectedUserId = recentChatUserIds.get(selectedPosition);
            String selectedUsername = recentChatUserNames.get(selectedPosition);
            if (!selectedUserId.equals(auth.getCurrentUser().getUid())) {
                sendForwardedMessage(selectedUserId, selectedUsername, message.getContent());
            } else {
                Toast.makeText(this, "Bạn không thể gửi tin nhắn cho chính mình", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void sendForwardedMessage(String targetUserId, String targetUsername, String messageContent) {
        String currentUserId = auth.getCurrentUser().getUid();
        String chatId = getOrCreateChatId(currentUserId, targetUserId);
        String forwardKey = chatId + "_" + messageContent;

        if (recentlyForwardedMessages.contains(forwardKey)) {
            Toast.makeText(this, "Tin nhắn này đã được chuyển tiếp đến người dùng này", Toast.LENGTH_SHORT).show();
            return;
        }

        recentlyForwardedMessages.add(forwardKey);

        DatabaseReference chatRef = reference.child("chats").child(chatId).child("messages");
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    createNewChatAndSendMessage(targetUserId, targetUsername, messageContent, chatId);
                } else {
                    sendMessageToExistingChat(targetUserId, targetUsername, messageContent, chatId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatActivity", "Lỗi khi kiểm tra đoạn chat: " + databaseError.getMessage());
            }
        });

        new Handler().postDelayed(() -> recentlyForwardedMessages.remove(forwardKey), 5000);
    }

    private void createNewChatAndSendMessage(String targetUserId, String targetUsername, String messageContent, String chatId) {
        DatabaseReference chatRef = reference.child("chats").child(chatId).child("messages");
        String messageId = chatRef.push().getKey();
        long currentTime = System.currentTimeMillis();
        Message message = new Message(auth.getCurrentUser().getUid(), messageContent, currentTime);
        Map<String, Object> messageData = new HashMap<>();
        messageData.put(messageId, message);
        chatRef.updateChildren(messageData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateRecentChats(chatId, messageContent, currentTime, auth.getCurrentUser().getUid(), targetUserId);
                Toast.makeText(this, "Tin nhắn đã được chuyển tiếp", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi khi chuyển tiếp tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessageToExistingChat(String targetUserId, String targetUsername, String messageContent, String chatId) {
        DatabaseReference chatRef = reference.child("chats").child(chatId).child("messages");
        String messageId = chatRef.push().getKey();
        long currentTime = System.currentTimeMillis();
        Message message = new Message(auth.getCurrentUser().getUid(), messageContent, currentTime);
        Map<String, Object> messageData = new HashMap<>();
        messageData.put(messageId, message);
        chatRef.updateChildren(messageData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateRecentChats(chatId, messageContent, currentTime, auth.getCurrentUser().getUid(), targetUserId);
                Toast.makeText(this, "Tin nhắn đã được chuyển tiếp", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi khi chuyển tiếp tin nhắn", Toast.LENGTH_SHORT).show();
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
                recentChatUserIds.clear();
                recentChatUserNames.clear();

                Set<String> userIdSet = new HashSet<>();
                List<String> tempUserIds = new ArrayList<>();
                List<String> tempUserNames = new ArrayList<>();

                for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                    String chatId = chatSnapshot.getKey();
                    String lastMessage = chatSnapshot.child("lastMessage").getValue(String.class);
                    long timestamp = chatSnapshot.child("timestamp").getValue(Long.class);
                    GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                    List<String> participants = chatSnapshot.child("participants").getValue(t);
                    String lastSenderId = chatSnapshot.child("lastSenderId").getValue(String.class);
                    RecentChat recentChat = new RecentChat(chatId, lastMessage, timestamp, participants, lastSenderId);
                    recentChats.add(recentChat);

                    for (String participant : participants) {
                        if (!participant.equals(currentUserId) && !userIdSet.contains(participant)) {
                            userIdSet.add(participant);
                            tempUserIds.add(participant);

                            DatabaseReference userRef = reference.child("users").child(participant);
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String username = dataSnapshot.child("username").getValue(String.class);
                                    if (username != null && !tempUserNames.contains(username)) {
                                        tempUserNames.add(username);
                                    }
                                    if (tempUserNames.size() == userIdSet.size()) {
                                        recentChatUserIds.clear();
                                        recentChatUserNames.clear();
                                        recentChatUserIds.addAll(tempUserIds);
                                        recentChatUserNames.addAll(tempUserNames);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e("ChatActivity", "Lỗi khi lấy thông tin người dùng: " + databaseError.getMessage());
                                }
                            });
                        }
                    }
                }

                Collections.sort(recentChats, (c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatActivity", "Lỗi khi lấy danh sách cuộc trò chuyện gần đây: " + databaseError.getMessage());
            }
        });
    }
}