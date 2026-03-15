package com.example.bi1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtPhone;
    private Button btnReset;
    private ImageButton btnBack;
    private TextView txtInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        edtPhone = findViewById(R.id.edtPhoneForgot);
        btnReset = findViewById(R.id.btnResetPassword);
        btnBack = findViewById(R.id.btnBackForgot);
        txtInfo = findViewById(R.id.txtNewPasswordInfo);

        btnBack.setOnClickListener(v -> finish());

        btnReset.setOnClickListener(v -> {
            String phoneInput = edtPhone.getText().toString().trim();
            
            if (phoneInput.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
            String savedPhone = sp.getString("phone", "");

            // Kiểm tra xem số điện thoại có khớp với tài khoản đã đăng ký không
            if (phoneInput.equals(savedPhone)) {
                // Giả lập tạo mật khẩu mới ngẫu nhiên (6 chữ số)
                String newPass = String.valueOf(new Random().nextInt(899999) + 100000);

                // Cập nhật mật khẩu mới vào bộ nhớ
                sp.edit().putString("password", newPass).apply();

                // Hiển thị thông báo giả lập gửi tin nhắn thành công
                txtInfo.setVisibility(View.VISIBLE);
                txtInfo.setText("Hệ thống đã gửi mật khẩu mới về số điện thoại " + phoneInput + ".\n\nMẬT KHẨU MỚI CỦA BẠN LÀ: " + newPass + "\n\n(Vui lòng dùng mật khẩu này để đăng nhập lại)");
                
                Toast.makeText(this, "Đã khôi phục mật khẩu thành công!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Số điện thoại này chưa được đăng ký tài khoản!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
