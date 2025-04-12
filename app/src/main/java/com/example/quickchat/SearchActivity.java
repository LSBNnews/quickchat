package com.example.quickchat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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
                String userId = selectedUser.getKey();
                String userName = selectedUser.child("name").getValue(String.class);

                // TODO: Chuyển đến trang chi tiết người dùng hoặc thực hiện hành động khác
                Toast.makeText(SearchActivity.this, "Clicked on user: " + userName, Toast.LENGTH_SHORT).show();
            }
        });
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