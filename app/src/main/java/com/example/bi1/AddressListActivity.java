package com.example.bi1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddressListActivity extends AppCompatActivity implements AddressAdapter.OnAddressSelectedListener {

    private RecyclerView rvAddresses;
    private Button btnAddAddress;
    private ImageButton btnBack;
    private AddressAdapter adapter;
    private List<UserAddress> addressList;
    private FirebaseFirestore db;
    private String userPhone;
    private String selectedId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        db = FirebaseFirestore.getInstance();
        SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
        userPhone = sp.getString("phone", "");
        selectedId = getIntent().getStringExtra("selected_id");

        rvAddresses = findViewById(R.id.rvAddresses);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        btnBack = findViewById(R.id.btnBackAddress);

        btnBack.setOnClickListener(v -> finish());
        btnAddAddress.setOnClickListener(v -> showAddressDialog(null));

        addressList = new ArrayList<>();
        adapter = new AddressAdapter(addressList, selectedId, this);
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rvAddresses.setAdapter(adapter);

        loadAddresses();
    }

    private void loadAddresses() {
        db.collection("user_addresses")
                .whereEqualTo("userId", userPhone)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    addressList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        UserAddress addr = doc.toObject(UserAddress.class);
                        addr.setId(doc.getId());
                        addressList.add(addr);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showAddressDialog(UserAddress addressToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_address, null);
        
        EditText edtName = view.findViewById(R.id.edtReceiverName);
        EditText edtPhone = view.findViewById(R.id.edtReceiverPhone);
        EditText edtAddress = view.findViewById(R.id.edtNewAddress);

        if (addressToEdit != null) {
            edtName.setText(addressToEdit.getReceiverName());
            edtPhone.setText(addressToEdit.getReceiverPhone());
            edtAddress.setText(addressToEdit.getDetailAddress());
        }

        builder.setView(view)
                .setTitle(addressToEdit == null ? "Thêm địa chỉ mới" : "Chỉnh sửa địa chỉ")
                .setPositiveButton("LƯU", (dialog, which) -> {
                    String name = edtName.getText().toString().trim();
                    String phone = edtPhone.getText().toString().trim();
                    String detail = edtAddress.getText().toString().trim();

                    if (name.isEmpty() || phone.isEmpty() || detail.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (addressToEdit == null) {
                        String id = UUID.randomUUID().toString();
                        UserAddress newAddr = new UserAddress(id, userPhone, name, phone, detail, addressList.isEmpty());
                        db.collection("user_addresses").document(id).set(newAddr).addOnSuccessListener(aVoid -> loadAddresses());
                    } else {
                        addressToEdit.setReceiverName(name);
                        addressToEdit.setReceiverPhone(phone);
                        addressToEdit.setDetailAddress(detail);
                        db.collection("user_addresses").document(addressToEdit.getId()).set(addressToEdit).addOnSuccessListener(aVoid -> loadAddresses());
                    }
                })
                .setNegativeButton("HỦY", null)
                .show();
    }

    @Override
    public void onAddressSelected(UserAddress address) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_address", address);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onEditAddress(UserAddress address) {
        showAddressDialog(address);
    }
}
