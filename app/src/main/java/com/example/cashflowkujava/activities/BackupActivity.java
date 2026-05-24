package com.example.cashflowkujava.activities;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashflowkujava.R;
import com.example.cashflowkujava.adapters.BackupAdapter;
import com.example.cashflowkujava.database.DatabaseHelper;
import com.example.cashflowkujava.models.BackupLog;
import com.example.cashflowkujava.services.LocalBackupService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class BackupActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_DB = 2001;

    private TextView tvStatus, tvLastTime, tvEmptyState;
    private Button btnBackup, btnRestore;
    private RecyclerView rvLogs;

    private DatabaseHelper dbHelper;
    private LocalBackupService backupService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        dbHelper = new DatabaseHelper(this);
        backupService = new LocalBackupService();

        // Bind Views
        tvStatus = findViewById(R.id.tv_backup_connection_status);
        tvLastTime = findViewById(R.id.tv_last_backup_time);
        tvEmptyState = findViewById(R.id.tv_empty_backup_logs);
        btnBackup = findViewById(R.id.btn_action_backup);
        btnRestore = findViewById(R.id.btn_action_restore);
        rvLogs = findViewById(R.id.rv_backup_logs);

        rvLogs.setLayoutManager(new LinearLayoutManager(this));

        // Back action
        findViewById(R.id.btn_back_backup).setOnClickListener(v -> finish());

        // Setup visual status
        setupSyncUI();

        // Listeners
        btnBackup.setOnClickListener(v -> triggerBackup());
        btnRestore.setOnClickListener(v -> confirmRestore());

        loadBackupLogs();
    }

    private void setupSyncUI() {
        tvStatus.setText("Status Cadangan: Aktif (Ekspor Ke Folder Download)");
        tvStatus.setTextColor(getResources().getColor(R.color.success));
    }

    private void loadBackupLogs() {
        List<BackupLog> logs = dbHelper.getAllBackupLogs();
        if (logs.isEmpty()) {
            rvLogs.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvLastTime.setText("Terakhir sinkronisasi: Belum pernah");
        } else {
            rvLogs.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            BackupAdapter adapter = new BackupAdapter(logs);
            rvLogs.setAdapter(adapter);

            // Set last success backup time label
            for (BackupLog log : logs) {
                if ("SUCCESS".equalsIgnoreCase(log.getStatus())) {
                    tvLastTime.setText("Terakhir sinkronisasi: " + log.getDate());
                    break;
                }
            }
        }
    }

    private void triggerBackup() {
        // Show simulated sync loader dialog
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("Cadangkan Database")
                .setMessage("Menulis salinan database ke folder Download...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        // Perform backup with delay to simulate writing
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean success = backupService.performBackup(BackupActivity.this);
            progressDialog.dismiss();
            
            if (success) {
                Toast.makeText(BackupActivity.this, "Database berhasil dicadangkan ke folder Download/CashFlowku!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(BackupActivity.this, "Gagal mencadangkan database.", Toast.LENGTH_SHORT).show();
            }
            loadBackupLogs();
        }, 1200);
    }

    private void confirmRestore() {
        new AlertDialog.Builder(this)
                .setTitle("Restore Database")
                .setMessage("Peringatan: Seluruh data lokal saat ini akan ditimpa oleh data dari file backup pilihan Anda. Apakah Anda ingin melanjutkan?")
                .setPositiveButton("Pilih File Backup", (dialog, which) -> launchFilePicker())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void launchFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // Accept all file extensions
        startActivityForResult(intent, REQUEST_CODE_PICK_DB);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_DB && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri backupUri = data.getData();
            performDbRestore(backupUri);
        }
    }

    private void performDbRestore(Uri uri) {
        // 1. Read first 16 bytes and validate SQLite signature
        byte[] header = new byte[16];
        try (InputStream testIs = getContentResolver().openInputStream(uri)) {
            if (testIs == null || testIs.read(header) < 16) {
                Toast.makeText(this, "File backup tidak valid (terlalu kecil).", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membaca file backup.", Toast.LENGTH_SHORT).show();
            return;
        }

        // SQLite header string is "SQLite format 3\0"
        String signature = new String(header, 0, 15, java.nio.charset.StandardCharsets.US_ASCII);
        if (!"SQLite format 3".equals(signature)) {
            Toast.makeText(this, "File terpilih bukan database SQLite yang valid!", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Close DB connections before replacing file
        dbHelper.close();

        File dbFile = getDatabasePath("cashflowku.db");
        File walFile = new File(dbFile.getPath() + "-wal");
        File shmFile = new File(dbFile.getPath() + "-shm");
        File journalFile = new File(dbFile.getPath() + "-journal");

        // Clear existing files and locks
        if (dbFile.exists()) dbFile.delete();
        if (walFile.exists()) walFile.delete();
        if (shmFile.exists()) shmFile.delete();
        if (journalFile.exists()) journalFile.delete();

        // Ensure parent folder exists
        File dbDir = dbFile.getParentFile();
        if (dbDir != null && !dbDir.exists()) {
            dbDir.mkdirs();
        }

        // 3. Write restore file
        try (InputStream is = getContentResolver().openInputStream(uri);
             FileOutputStream fos = new FileOutputStream(dbFile)) {
            
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.flush();
            Toast.makeText(this, "Restore database berhasil! Data diperbarui.", Toast.LENGTH_SHORT).show();

            // Re-instantiate database helper & refresh list
            dbHelper = new DatabaseHelper(this);
            dbHelper.syncSettingsFromDatabase(this);
            loadBackupLogs();
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal memulihkan database. Pastikan berkas terpilih valid.", Toast.LENGTH_LONG).show();
            // Re-open DB anyway to recover state
            dbHelper = new DatabaseHelper(this);
        }
    }
}
