package com.example.bi1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity";

    EditText edtPhone, edtPassword;
    Button btnLogin, btnFacebook, btnGoogle;
    TextView txtForgotPassword, btnGoRegisterText;
    LinearLayout btnTabRegister;
    FirebaseFirestore db;
    
    private CallbackManager callbackManager;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        edtPhone = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnFacebook = findViewById(R.id.btnFacebook);
        btnGoogle = findViewById(R.id.btnGoogle);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        btnGoRegisterText = findViewById(R.id.btnGoRegisterText);
        btnTabRegister = findViewById(R.id.btnTabRegister);

        TextWatcher loginTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { updateLoginButtonState(); }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        edtPhone.addTextChangedListener(loginTextWatcher);
        edtPassword.addTextChangedListener(loginTextWatcher);
        updateLoginButtonState();

        btnLogin.setOnClickListener(v -> login());
        
        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        btnFacebook.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
        });

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Đã hủy đăng nhập Facebook", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Lỗi Facebook: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        View.OnClickListener goRegister = v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        btnGoRegisterText.setOnClickListener(goRegister);
        btnTabRegister.setOnClickListener(goRegister);

        txtForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveLoginSession(user.getEmail(), user.getDisplayName(), 0);
                            Toast.makeText(LoginActivity.this, "Chào mừng " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Lỗi Firebase Auth với Google.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveLoginSession(user.getUid(), user.getDisplayName(), 0);
                            Toast.makeText(LoginActivity.this, "Chào mừng " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Lỗi Firebase Auth với Facebook.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                int statusCode = e.getStatusCode();
                String message = "Lỗi Google (" + statusCode + "): ";
                if (statusCode == 10) {
                    message += "Lỗi Developer. Bạn CẦN thêm mã SHA-1 vào Firebase Console.";
                } else if (statusCode == 12500) {
                    message += "Lỗi cấu hình Google Play Services.";
                } else {
                    message += "Vui lòng kiểm tra kết nối mạng.";
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Google Sign-In failed, code: " + statusCode, e);
            }
        }
    }

    private void updateLoginButtonState() {
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        if (!phone.isEmpty() && !password.isEmpty()) {
            btnLogin.setBackgroundResource(R.drawable.bg_button_orange_gradient);
            btnLogin.setTextColor(Color.WHITE);
            btnLogin.setEnabled(true);
        } else {
            btnLogin.setBackgroundResource(R.drawable.bg_button_gray);
            btnLogin.setTextColor(Color.parseColor("#888888"));
            btnLogin.setEnabled(false);
        }
    }

    private void login() {
        String inputPhone = edtPhone.getText().toString().trim();
        String inputPass = edtPassword.getText().toString().trim();
        if (inputPhone.isEmpty() || inputPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (inputPhone.equals("admin") && inputPass.equals("123456")) {
            saveLoginSession("admin", "Admin", 1);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        db.collection("users").document(inputPhone).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null && user.getPassword().equals(inputPass)) {
                            saveLoginSession(user.getPhone(), user.getHoTen(), user.getRoleid());
                            startActivity(new Intent(this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveLoginSession(String phone, String name, int roleId) {
        SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
        sp.edit()
                .putBoolean("logged_in", true)
                .putString("phone", phone)
                .putString("username", name)
                .putInt("roleid", roleId)
                .apply();
    }
}
