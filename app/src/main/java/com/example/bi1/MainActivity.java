package com.example.bi1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseFirestore firestore;
    private ArrayList<Book> bookList;

    TextView txtWelcome;
    Button btnDetail, btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        txtWelcome = findViewById(R.id.txtWelcome);
        btnDetail = findViewById(R.id.btnDetail);
        btnProfile = findViewById(R.id.btnProfile);

        SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);

        String user = getIntent().getStringExtra("username");

        if (user == null) {
            user = sp.getString("username", "User");
        }

        txtWelcome.setText("Xin Chào " + user + " 👋");

        btnDetail.setOnClickListener(v -> {

            Intent i = new Intent(MainActivity.this, DetailActivity.class);

            i.putExtra("name", "Sản Phẩm Demo");
            i.putExtra("price", "500000");
            i.putExtra("desc", "Đây là sản phẩm demo");
            i.putExtra("image", "https://images-na.ssl-images-amazon.com/images/I/51NKhnjhpGL.jpg");

            startActivity(i);
        });

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        firestore = FirebaseFirestore.getInstance();
        bookList = new ArrayList<>();

        loadBooksFromFirestore();

        Log.d(TAG, "MainActivity ready");
    }

    private void loadBooksFromFirestore() {

        firestore.collection("books")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    bookList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        Book book = document.toObject(Book.class);
                        book.setId(document.getId());

                        bookList.add(book);

                        Log.d(TAG, "Tên sách: " + book.getTenSach());
                    }

                    Toast.makeText(this,
                            "Đã tải " + bookList.size() + " sách",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {

                    Log.e(TAG, "Lỗi Firestore: " + e.getMessage());

                    Toast.makeText(this,
                            "Lỗi tải dữ liệu",
                            Toast.LENGTH_LONG).show();
                });
    }
}
