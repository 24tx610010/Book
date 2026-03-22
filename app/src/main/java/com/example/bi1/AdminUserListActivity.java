package com.example.bi1;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminUserListActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private AdminUserAdapter adapter;
    private ArrayList<User> userList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);

        db = FirebaseFirestore.getInstance();
        rvUsers = findViewById(R.id.rvAdminUserList);
        ImageButton btnBack = findViewById(R.id.btnBackAdminUser);

        btnBack.setOnClickListener(v -> finish());

        userList = new ArrayList<>();
        adapter = new AdminUserAdapter(this, userList);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        listenToUsers();
    }

    private void listenToUsers() {
        db.collection("users")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        userList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            User user = doc.toObject(User.class);
                            userList.add(user);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
