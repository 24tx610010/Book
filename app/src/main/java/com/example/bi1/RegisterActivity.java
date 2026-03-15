package com.example.bi1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    EditText edtPhone, edtPassword, edtConfirmPassword, edtFullName;
    Button btnRegister;
    ImageButton btnBack;
    TextView txtGoLogin;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtGoLogin = findViewById(R.id.txtGoLogin);

        btnBack.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> register());

        txtGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void register() {
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirm = edtConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.matches("0\\d{9,10}")) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải từ 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng User mới để lưu lên Firebase
        String userId = UUID.randomUUID().toString().substring(0, 8);
        User newUser = new User(userId, phone, fullName, password, 2); // 2 là mặc định User

        // LƯU LÊN FIREBASE CLOUD
        db.collection("users").document(phone).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    // Lưu vào máy để dùng cho Login/Profile
                    SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
                    sp.edit()
                            .putString("phone", phone)
                            .putString("username", fullName) // Lưu họ tên vào làm username hiển thị
                            .putString("password", password)
                            .putInt("roleid", 2)
                            .apply();

                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi đăng ký: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
