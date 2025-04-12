package com.example.quickchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quickchat.ChatActivity;
import com.example.quickchat.R;
import com.example.quickchat.model.RecentChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RecentChatAdapter extends RecyclerView.Adapter<RecentChatAdapter.RecentChatViewHolder> {

    private Context context;
    private List<RecentChat> recentChats;

    public RecentChatAdapter(List<RecentChat> recentChats) {
        this.recentChats = recentChats;
    }

    @NonNull
    @Override
    public RecentChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_chat, parent, false);
        return new RecentChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentChatViewHolder holder, int position) {
        RecentChat recentChat = recentChats.get(position);
        holder.bind(recentChat);
    }

    @Override
    public int getItemCount() {
        return recentChats.size();
    }

    static class RecentChatViewHolder extends RecyclerView.ViewHolder {
        private ImageView userAvatar;
        private TextView userName, lastMessage, timestamp;

        public RecentChatViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.rc_avatar);
            userName = itemView.findViewById(R.id.rc_username);
            lastMessage = itemView.findViewById(R.id.rc_last_message);
            timestamp = itemView.findViewById(R.id.rc_timestamp);
        }

        public void bind(RecentChat recentChat) {
            String targetUserId = getTargetUserId(recentChat.participants);
            String lastMessageText = recentChat.lastMessage;

            // Load avatar và username của người dùng đích
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(targetUserId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String imageURL = dataSnapshot.child("imageURL").getValue(String.class);
                    if (imageURL != null && !imageURL.equals("default")) {
                        Glide.with(itemView.getContext())
                                .load(imageURL)
                                .into(userAvatar);
                    } else {
                        userAvatar.setImageResource(R.mipmap.ic_launcher);
                    }

                    String username = dataSnapshot.child("username").getValue(String.class);
                    if (username != null) {
                        userName.setText(username);
                    } else {
                        userName.setText("Unknown");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("RecentChatAdapter", "Lỗi khi lấy thông tin người dùng: " + databaseError.getMessage());
                }
            });

            lastMessage.setText(lastMessageText);
            timestamp.setText(formatTimestamp(recentChat.timestamp));

            itemView.setOnClickListener(v -> {
                // Mở ChatActivity với thông tin người dùng đích
                Intent intent = new Intent(itemView.getContext(), ChatActivity.class);
                intent.putExtra("targetUserId", targetUserId);
                intent.putExtra("targetUsername", userName.getText().toString());
                itemView.getContext().startActivity(intent);
            });
        }

        private String getTargetUserId(List<String> participants) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            for (String participant : participants) {
                if (!participant.equals(currentUserId)) {
                    return participant;
                }
            }
            return "";
        }

        private String formatTimestamp(long timestamp) {
            // Định dạng thời gian tùy ý
            return "Hôm nay, 10:00 AM";
        }
    }
}