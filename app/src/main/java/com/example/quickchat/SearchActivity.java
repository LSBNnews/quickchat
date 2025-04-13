package com.example.quickchat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

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
    private ProgressBar loadingBar;
    private UserSearchAdapter userSearchAdapter;
    private ArrayList<DataSnapshot> searchResults;

    private DatabaseReference reference;
    private FirebaseAuth auth;

    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initializeFields();
        setupSearchView();
    }

    private void initializeFields() {
        reference = FirebaseDatabase.getInstance().getReference().child("users");
        searchView = findViewById(R.id.search_view);
        userList = findViewById(R.id.user_list);
        loadingBar = findViewById(R.id.loading_bar); // <-- Add a ProgressBar with this ID in layout
        searchResults = new ArrayList<>();
        userSearchAdapter = new UserSearchAdapter(this, searchResults);
        userList.setAdapter(userSearchAdapter);
        auth = FirebaseAuth.getInstance();

        loadingBar.setVisibility(View.GONE);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                if (newText.isEmpty()) {
                    searchResults.clear();
                    userSearchAdapter.notifyDataSetChanged();
                    loadingBar.setVisibility(View.GONE);
                } else {
                    searchRunnable = () -> {
                        loadingBar.setVisibility(View.VISIBLE);
                        searchUsers(newText);
                    };
                    searchHandler.postDelayed(searchRunnable, 500); // Debounce 500ms
                }

                return true;
            }
        });

        userList.setOnItemClickListener((parent, view, position, id) -> {
            DataSnapshot selectedUser = searchResults.get(position);
            String targetUserId = selectedUser.getKey();
            String targetUsername = selectedUser.child("username").getValue(String.class);

            DatabaseReference blockedUsersRef = FirebaseDatabase.getInstance().getReference()
                    .child("blockedUsers")
                    .child(auth.getCurrentUser().getUid())
                    .child(targetUserId);

            blockedUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean isBlocked = dataSnapshot.exists() && dataSnapshot.getValue(Boolean.class);
                    showUserActionDialog(targetUserId, targetUsername, isBlocked);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("SearchActivity", "Lỗi khi kiểm tra trạng thái chặn: " + databaseError.getMessage());
                }
            });
        });
    }

    private void showUserActionDialog(final String targetUserId, final String targetUsername, boolean isBlocked) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(targetUsername);

        if (isBlocked) {
            builder.setMessage("Bạn đã chặn người dùng này")
                    .setPositiveButton("Bỏ chặn", (dialog, which) -> unblockUser(targetUserId))
                    .setNegativeButton("Đóng", (dialog, which) -> dialog.dismiss());
        } else {
            builder.setPositiveButton("Nhắn tin", (dialog, which) -> openChatActivity(targetUserId, targetUsername))
                    .setNegativeButton("Chặn", (dialog, which) -> blockUser(targetUserId))
                    .setNeutralButton("Đóng", (dialog, which) -> dialog.dismiss());
        }

        builder.show();
    }

    private void blockUser(String targetUserId) {
        FirebaseDatabase.getInstance().getReference()
                .child("blockedUsers")
                .child(auth.getCurrentUser().getUid())
                .child(targetUserId)
                .setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SearchActivity.this, "Đã chặn người dùng thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SearchActivity.this, "Lỗi khi chặn người dùng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unblockUser(String targetUserId) {
        FirebaseDatabase.getInstance().getReference()
                .child("blockedUsers")
                .child(auth.getCurrentUser().getUid())
                .child(targetUserId)
                .removeValue()
                .addOnCompleteListener(task -> {
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

        String currentUserId = auth.getCurrentUser().getUid();
        Query searchQuery = reference.orderByChild("username").startAt(query).endAt(query + "\uf8ff");

        searchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null && !userId.equals(currentUserId)) {
                        searchResults.add(userSnapshot);
                    }
                }
                userSearchAdapter.notifyDataSetChanged();
                loadingBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SearchActivity", "Lỗi khi tìm kiếm người dùng: " + databaseError.getMessage());
                Toast.makeText(SearchActivity.this, "Lỗi khi tìm kiếm người dùng", Toast.LENGTH_SHORT).show();
                loadingBar.setVisibility(View.GONE);
            }
        });
    }
}
