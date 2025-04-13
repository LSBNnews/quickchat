package com.example.quickchat.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.quickchat.R;
import com.example.quickchat.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class MessageAdapter extends BaseAdapter {
    private Context context;
    private List<Message> messages;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = messages.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            // Tin nhắn gửi
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
            TextView sentMessageText = convertView.findViewById(R.id.sent_message_text);
            TextView sentMessageTime = convertView.findViewById(R.id.sent_message_time);
            sentMessageText.setText(message.getContent());
            sentMessageTime.setText(DateUtils.formatDateTime(context, message.getTimestamp(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE));
            // Hiển thị tin nhắn gửi và ẩn tin nhắn nhận
            convertView.findViewById(R.id.sent_message_container).setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.received_message_container).setVisibility(View.GONE);
        } else {
            // Tin nhắn nhận
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
            TextView receivedMessageText = convertView.findViewById(R.id.received_message_text);
            TextView receivedMessageTime = convertView.findViewById(R.id.received_message_time);
            receivedMessageText.setText(message.getContent());
            receivedMessageTime.setText(DateUtils.formatDateTime(context, message.getTimestamp(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE));
            // Hiển thị tin nhắn nhận và ẩn tin nhắn gửi
            convertView.findViewById(R.id.sent_message_container).setVisibility(View.GONE);
            convertView.findViewById(R.id.received_message_container).setVisibility(View.VISIBLE);
        }
        return convertView;
    }
}