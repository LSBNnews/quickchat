package com.example.quickchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.quickchat.R;
import com.example.quickchat.model.RecentChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecentChatAdapter extends BaseAdapter {

    private Context context;
    private List<RecentChat> recentChats;
    private DatabaseReference reference;

    public RecentChatAdapter(Context context, List<RecentChat> recentChats) {
        this.context = context;
        this.recentChats = recentChats;
        this.reference = FirebaseDatabase.getInstance().getReference().child("users");
    }

    @Override
    public int getCount() {
        return recentChats.size();
    }

    @Override
    public Object getItem(int position) {
        return recentChats.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_recent_chat, parent, false);
        }

        RecentChat recentChat = recentChats.get(position);

        TextView lastMessageText = convertView.findViewById(R.id.rc_last_message);
        TextView chatNameText = convertView.findViewById(R.id.rc_chat_name);
        CircleImageView chatUserImage = convertView.findViewById(R.id.rc_user_image);

        lastMessageText.setText(recentChat.getLastMessage());

        // Lấy thông tin người dùng khác từ danh sách participants
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherUserId = recentChat.getParticipants().get(0).equals(currentUserId) ? recentChat.getParticipants().get(1) : recentChat.getParticipants().get(0);

        reference.child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue(String.class);
                String imageURL = dataSnapshot.child("imageURL").getValue(String.class);

                chatNameText.setText(username);
                if (imageURL != null && !imageURL.equals("default")) {
                    Glide.with(context)
                            .load(imageURL)
                            .into(chatUserImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu cần
            }
        });

        return convertView;
    }
}