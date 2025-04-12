package com.example.quickchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

public class UserSearchAdapter extends ArrayAdapter<DataSnapshot> {

    private Context context;
    private ArrayList<DataSnapshot> users;

    public UserSearchAdapter(@NonNull Context context, @NonNull ArrayList<DataSnapshot> users) {
        super(context, R.layout.item_user_search, users);
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.user_avatar);
        TextView usernameTextView = convertView.findViewById(R.id.user_name);
        TextView descriptionTextView = convertView.findViewById(R.id.user_description);

        DataSnapshot userSnapshot = users.get(position);
        String username = userSnapshot.child("username").getValue(String.class);
        String description = userSnapshot.child("description").getValue(String.class);
        String imageURL = userSnapshot.child("imageURL").getValue(String.class);

        // Kiểm tra nếu imageURL là null
        if (imageURL == null || imageURL.equals("default")) {
            imageView.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(context)
                    .load(imageURL)
                    .into(imageView);
        }

        // Kiểm tra nếu username là null
        if (username != null) {
            usernameTextView.setText(username);
        } else {
            usernameTextView.setText("Unknown");
        }

        // Kiểm tra nếu description là null
        if (description != null) {
            descriptionTextView.setText(description);
        } else {
            descriptionTextView.setText("No description");
        }

        return convertView;
    }
}