package com.example.bi1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
<<<<<<< HEAD
import android.widget.Button;
import android.widget.EditText;
=======
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
>>>>>>> 0d5c59f (22/3)
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

<<<<<<< HEAD
public class LoginActivity extends AppCompatActivity {

    EditText edtPhone, edtPassword;
    Button btnLogin, btnGoRegister;
    TextView txtForgotPassword;
=======
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText edtPhone, edtPassword;
    Button btnLogin;
    TextView txtForgotPassword, btnGoRegisterText;
    LinearLayout btnTabRegister;
    FirebaseFirestore db;
>>>>>>> 0d5c59f (22/3)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

<<<<<<< HEAD
=======
        db = FirebaseFirestore.getInstance();

>>>>>>> 0d5c59f (22/3)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

<<<<<<< HEAD
        edtPhone = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        btnLogin.setOnClickListener(v -> login());

        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
=======
        // Ánh xạ theo ID mới trong activity_login.xml
        edtPhone = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        btnGoRegisterText = findViewById(R.id.btnGoRegisterText);
        btnTabRegister = findViewById(R.id.btnTabRegister);

        btnLogin.setOnClickListener(v -> login());

        // Chuyển sang trang đăng ký (từ dòng chữ hoặc từ tab)
        View.OnClickListener goRegister = v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        };
        btnGoRegisterText.setOnClickListener(goRegister);
        btnTabRegister.setOnClickListener(goRegister);
>>>>>>> 0d5c59f (22/3)

        txtForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );
    }

    private void login() {
        String inputPhone = edtPhone.getText().toString().trim();
        String inputPass = edtPassword.getText().toString().trim();

        if (inputPhone.isEmpty() || inputPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

<<<<<<< HEAD
        SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
        
        // 1. KIỂM TRA ADMIN CỐ ĐỊNH
        if (inputPhone.equals("admin") && inputPass.equals("123456")) {
            sp.edit()
                    .putBoolean("logged_in", true)
                    .putInt("roleid", 1) // Admin dùng roleid = 1
                    .putString("username", "Admin")
                    .apply();
            
=======
        // 1. KIỂM TRA ADMIN CỐ ĐỊNH
        if (inputPhone.equals("admin") && inputPass.equals("123456")) {
            saveLoginSession("admin", "Admin", 1);
>>>>>>> 0d5c59f (22/3)
            Toast.makeText(this, "Chào Quản trị viên!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

<<<<<<< HEAD
        // 2. KIỂM TRA USER ĐÃ ĐĂNG KÝ
        String savedPhone = sp.getString("phone", "");
        String savedPass = sp.getString("password", "");

        if (inputPhone.equals(savedPhone) && inputPass.equals(savedPass)) {
            sp.edit()
                    .putBoolean("logged_in", true)
                    .putInt("roleid", 2) // User dùng roleid = 2
                    .putString("username", savedPhone)
                    .apply();

            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Số điện thoại hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
        }
=======
        // 2. KIỂM TRA USER TỪ FIREBASE
        db.collection("users").document(inputPhone).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getPassword().equals(inputPass)) {
                            saveLoginSession(user.getPhone(), user.getHoTen(), user.getRoleid());
                            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveLoginSession(String phone, String name, int roleId) {
        SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
        sp.edit()
                .putBoolean("logged_in", true)
                .putString("phone", phone)
                .putString("username", name)
                .putInt("roleid", roleId)
                .apply();
>>>>>>> 0d5c59f (22/3)
    }
}
