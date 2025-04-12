package com.example.quickchat;

import android.content.Intent;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.example.quickchat.adapter.MessageAdapter;
import com.example.quickchat.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
    private ListView chatList;

    private DatabaseReference reference;
    private FirebaseAuth auth;
    private String targetUserId, targetUsername, chatId;

    private List<Message> messages;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeFields();

        // Thiết lập toolbar
        setupToolbar();

        // Thiết lập danh sách tin nhắn
        setupMessageList();

        // Thiết lập nút gửi tin nhắn
        setupSendMessageButton();
    }

    private void initializeFields() {
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        chatToolbar = findViewById(R.id.chat_toolbar);
        chatUserImage = findViewById(R.id.chat_user_image);
        chatUserName = findViewById(R.id.chat_user_name);
        chatInput = findViewById(R.id.chat_input);
        chatSendButton = findViewById(R.id.chat_send_button);
        chatList = findViewById(R.id.chat_list);

        targetUserId = getIntent().getStringExtra("targetUserId");
        targetUsername = getIntent().getStringExtra("targetUsername");

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages);
        chatList.setAdapter(messageAdapter);

        chatId = getOrCreateChatId(auth.getCurrentUser().getUid(), targetUserId);
    }

    private void setupToolbar() {
        setSupportActionBar(chatToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        chatUserName.setText(targetUsername);

        // Load ảnh đại diện của người dùng đích
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
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    messages.add(message);
                }
                messageAdapter.notifyDataSetChanged();
                chatList.setSelection(messages.size() - 1); // Cuộn xuống cuối danh sách tin nhắn
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatActivity", "Lỗi khi lấy danh sách tin nhắn: " + databaseError.getMessage());
            }
        });
    }

    private void setupSendMessageButton() {
        chatSendButton.setOnClickListener(v -> sendMessage());

        chatInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    chatSendButton.setEnabled(false);
                } else {
                    chatSendButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void sendMessage() {
        String messageContent = chatInput.getText().toString().trim();
        if (!messageContent.isEmpty()) {
            String currentUserId = auth.getCurrentUser().getUid();

            DatabaseReference chatRef = reference.child("chats").child(chatId).child("messages");
            String messageId = chatRef.push().getKey();

            Message message = new Message(currentUserId, messageContent, System.currentTimeMillis());

            Map<String, Object> messageData = new HashMap<>();
            messageData.put(messageId, message);

            chatRef.updateChildren(messageData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    chatInput.setText("");
                    updateRecentChats(chatId, messageContent, System.currentTimeMillis());
                } else {
                    Toast.makeText(ChatActivity.this, "Lỗi khi gửi tin nhắn", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateRecentChats(String chatId, String lastMessage, long timestamp) {
        String currentUserId = auth.getCurrentUser().getUid();

        DatabaseReference recentChatsRef = reference.child("recentChats").child(currentUserId).child(chatId);
        Map<String, Object> recentChatData = new HashMap<>();
        recentChatData.put("lastMessage", lastMessage);
        recentChatData.put("timestamp", timestamp);

        // Sử dụng List thay vì Array
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(targetUserId);
        recentChatData.put("participants", participants);

        recentChatsRef.updateChildren(recentChatData);

        // Cập nhật recentChats cho người nhận
        DatabaseReference recipientRecentChatsRef = reference.child("recentChats").child(targetUserId).child(chatId);
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
}