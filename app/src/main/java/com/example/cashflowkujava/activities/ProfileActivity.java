package com.example.cashflowkujava.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cashflowkujava.R;
import com.example.cashflowkujava.database.DatabaseHelper;

import java.io.File;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvDbSize, tvTotalTrans;
    private EditText etName, etEmail, etPhone;
    private Button btnSave;

    private DatabaseHelper dbHelper;
    private SharedPreferences bizPrefs;

    private static final String PREF_NAME = "BusinessPrefs";
    private static final String KEY_BIZ_NAME = "biz_name";
    private static final String KEY_BIZ_EMAIL = "biz_email";
    private static final String KEY_BIZ_PHONE = "biz_phone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        bizPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Bind Views
        etName = findViewById(R.id.et_profile_name);
        etEmail = findViewById(R.id.et_profile_email);
        etPhone = findViewById(R.id.et_profile_phone);
        tvDbSize = findViewById(R.id.tv_profile_db_size);
        tvTotalTrans = findViewById(R.id.tv_profile_total_trans);
        btnSave = findViewById(R.id.btn_profile_save);

        // Back button
        findViewById(R.id.btn_back_profile).setOnClickListener(v -> finish());

        // Save listener
        btnSave.setOnClickListener(v -> saveProfileData());

        loadProfileData();
    }

    private void loadProfileData() {
        String name = bizPrefs.getString(KEY_BIZ_NAME, "Toko Saya");
        String email = bizPrefs.getString(KEY_BIZ_EMAIL, "kontak@tokosaya.com");
        String phone = bizPrefs.getString(KEY_BIZ_PHONE, "08123456789");

        etName.setText(name);
        etEmail.setText(email);
        etPhone.setText(phone);
        


        // Calculate database size
        File dbFile = getDatabasePath("cashflowku.db");
        long bytes = dbFile.exists() ? dbFile.length() : 0;
        String sizeStr;
        if (bytes >= 1024 * 1024) {
            sizeStr = String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
        } else if (bytes >= 1024) {
            sizeStr = String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        } else {
            sizeStr = bytes + " B";
        }
        tvDbSize.setText(sizeStr);

        // Calculate total transactions
        int salesCount = dbHelper.getAllSales().size();
        int expensesCount = dbHelper.getAllExpenses().size();
        tvTotalTrans.setText(String.valueOf(salesCount + expensesCount));
    }

    private void saveProfileData() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Nama usaha tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        bizPrefs.edit()
                .putString(KEY_BIZ_NAME, name)
                .putString(KEY_BIZ_EMAIL, email)
                .putString(KEY_BIZ_PHONE, phone)
                .apply();

        dbHelper.saveSetting(KEY_BIZ_NAME, name);
        dbHelper.saveSetting(KEY_BIZ_EMAIL, email);
        dbHelper.saveSetting(KEY_BIZ_PHONE, phone);

        Toast.makeText(this, "Profil usaha berhasil disimpan!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
