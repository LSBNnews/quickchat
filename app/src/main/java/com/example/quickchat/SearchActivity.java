package com.example.quickchat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.bumptech.glide.Glide;
import com.example.quickchat.adapter.UserSearchAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private ListView userList;
    private UserSearchAdapter userSearchAdapter;
    private ArrayList<DataSnapshot> searchResults;

    private DatabaseReference reference;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initializeFields();

        // Thiết lập thanh tìm kiếm
        setupSearchView();
    }

    private void initializeFields() {
        reference = FirebaseDatabase.getInstance().getReference().child("users");
        searchView = findViewById(R.id.search_view);
        userList = findViewById(R.id.user_list);
        searchResults = new ArrayList<>();
        userSearchAdapter = new UserSearchAdapter(this, searchResults);
        userList.setAdapter(userSearchAdapter);
        auth = FirebaseAuth.getInstance();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUsers(newText);
                return true;
            }
        });

        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataSnapshot selectedUser = searchResults.get(position);
                String targetUserId = selectedUser.getKey();
                String targetUsername = selectedUser.child("username").getValue(String.class);

                // Kiểm tra xem người dùng có bị chặn hay không
                DatabaseReference blockedUsersRef = FirebaseDatabase.getInstance().getReference()
                        .child("blockedUsers")
                        .child(auth.getCurrentUser().getUid())
                        .child(targetUserId);

                blockedUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isBlocked = dataSnapshot.exists() && dataSnapshot.getValue(Boolean.class);

                        // Hiển thị dialog tùy theo trạng thái chặn
                        showUserActionDialog(targetUserId, targetUsername, isBlocked);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("SearchActivity", "Lỗi khi kiểm tra trạng thái chặn: " + databaseError.getMessage());
                    }
                });
            }
        });
    }

    private void showUserActionDialog(final String targetUserId, final String targetUsername, boolean isBlocked) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(targetUsername);

        if (isBlocked) {
            // Người dùng đã bị chặn
            builder.setMessage("Bạn đã chặn người dùng này")
                    .setPositiveButton("Bỏ chặn", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Xử lý bỏ chặn
                            unblockUser(targetUserId);
                        }
                    })
                    .setNegativeButton("Đóng", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        } else {
            // Người dùng chưa bị chặn
            builder.setPositiveButton("Nhắn tin", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Mở ChatActivity
                            openChatActivity(targetUserId, targetUsername);
                        }
                    })
                    .setNegativeButton("Chặn", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Chặn người dùng
                            blockUser(targetUserId);
                        }
                    })
                    .setNeutralButton("Đóng", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }

        builder.show();
    }

    private void blockUser(String targetUserId) {
        DatabaseReference blockedUsersRef = FirebaseDatabase.getInstance().getReference()
                .child("blockedUsers")
                .child(auth.getCurrentUser().getUid())
                .child(targetUserId);

        blockedUsersRef.setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SearchActivity.this, "Đã chặn người dùng thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SearchActivity.this, "Lỗi khi chặn người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unblockUser(String targetUserId) {
        DatabaseReference blockedUsersRef = FirebaseDatabase.getInstance().getReference()
                .child("blockedUsers")
                .child(auth.getCurrentUser().getUid())
                .child(targetUserId);

        blockedUsersRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SearchActivity.this, "Đã bỏ chặn người dùng thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SearchActivity.this, "Lỗi khi bỏ chặn người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChatActivity(String targetUserId, String targetUsername) {
        Intent intent = new Intent(SearchActivity.this, ChatActivity.class);
        intent.putExtra("targetUserId", targetUserId);
        intent.putExtra("targetUsername", targetUsername);
        startActivity(intent);
    }

    private void searchUsers(String query) {
        searchResults.clear();

        // Sửa trường truy vấn từ "name" thành "username"
        Query searchQuery = reference.orderByChild("username").startAt(query).endAt(query + "\uf8ff");
        searchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    searchResults.add(userSnapshot);
                }
                userSearchAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SearchActivity", "Lỗi khi tìm kiếm người dùng: " + databaseError.getMessage());
                Toast.makeText(SearchActivity.this, "Lỗi khi tìm kiếm người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}