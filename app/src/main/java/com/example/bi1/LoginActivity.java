package com.example.bi1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText edtPhone, edtPassword;
    Button btnLogin, btnGoRegister;
    TextView txtForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        edtPhone = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        btnLogin.setOnClickListener(v -> login());

        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

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

        SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
        
        // 1. KIỂM TRA ADMIN CỐ ĐỊNH
        if (inputPhone.equals("admin") && inputPass.equals("123456")) {
            sp.edit()
                    .putBoolean("logged_in", true)
                    .putInt("roleid", 1) // Admin dùng roleid = 1
                    .putString("username", "Admin")
                    .apply();
            
            Toast.makeText(this, "Chào Quản trị viên!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

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
    }
}
