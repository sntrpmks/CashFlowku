package com.example.cashflowkujava.services;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.cashflowkujava.database.DatabaseHelper;
import com.example.cashflowkujava.models.BackupLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocalBackupService implements BackupService {

    private static final String TAG = "LocalBackupService";
    private static final String BACKUP_DIR = "backups";
    private static final String BACKUP_FILE_NAME = "cashflowku_backup.db";

    @Override
    public boolean performBackup(Context context) {
        // Force WAL checkpoint to flush all pending writes to the main database file
        try {
            DatabaseHelper helper = new DatabaseHelper(context);
            android.database.sqlite.SQLiteDatabase db = helper.getWritableDatabase();
            db.rawQuery("PRAGMA wal_checkpoint(FULL);", null).close();
            helper.close();
        } catch (Exception e) {
            Log.e(TAG, "Failed to force WAL checkpoint before backup", e);
        }

        File dbFile = context.getDatabasePath("cashflowku.db");
        if (!dbFile.exists()) {
            Log.e(TAG, "Source database file does not exist!");
            logBackupEvent(context, BACKUP_FILE_NAME, "", 0, "FAILED");
            return false;
        }


        File backupDir = context.getExternalFilesDir(BACKUP_DIR);
        if (backupDir == null) {
            logBackupEvent(context, BACKUP_FILE_NAME, "", 0, "FAILED");
            return false;
        }

        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        File backupFile = new File(backupDir, BACKUP_FILE_NAME);
        FileChannel src = null;
        FileChannel dst = null;
        boolean success = false;

        try {
            src = new FileInputStream(dbFile).getChannel();
            dst = new FileOutputStream(backupFile).getChannel();
            dst.transferFrom(src, 0, src.size());
            success = true;
            Log.d(TAG, "Database backup successful locally: " + backupFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error performing local backup", e);
        } finally {
            try {
                if (src != null) src.close();
                if (dst != null) dst.close();
            } catch (IOException ignored) {}
        }

        long fileSize = backupFile.exists() ? backupFile.length() : 0;
        
        if (success) {
            // Copy to public downloads folder
            saveBackupToPublicDownloads(context, BACKUP_FILE_NAME, backupFile);
        }

        logBackupEvent(
                context,
                BACKUP_FILE_NAME,
                backupFile.getAbsolutePath(),
                fileSize,
                success ? "SUCCESS" : "FAILED"
        );

        return success;
    }

    @Override
    public boolean performRestore(Context context) {
        // Fallback default restore logic
        File backupDir = context.getExternalFilesDir(BACKUP_DIR);
        if (backupDir == null) {
            return false;
        }

        File backupFile = new File(backupDir, BACKUP_FILE_NAME);
        if (!backupFile.exists()) {
            Log.e(TAG, "Backup file does not exist for default restore!");
            return false;
        }

        File dbFile = context.getDatabasePath("cashflowku.db");
        File walFile = new File(dbFile.getPath() + "-wal");
        File shmFile = new File(dbFile.getPath() + "-shm");
        File journalFile = new File(dbFile.getPath() + "-journal");

        // Close DB before restoring
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        dbHelper.close();

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

        FileChannel src = null;
        FileChannel dst = null;
        boolean success = false;

        try {
            src = new FileInputStream(backupFile).getChannel();
            dst = new FileOutputStream(dbFile).getChannel();
            dst.transferFrom(src, 0, src.size());
            success = true;
            Log.d(TAG, "Database default restore successful.");
        } catch (IOException e) {
            Log.e(TAG, "Error performing restore", e);
        } finally {
            try {
                if (src != null) src.close();
                if (dst != null) dst.close();
            } catch (IOException ignored) {}
        }

        return success;
    }

    @Override
    public String getBackupStatusInfo() {
        return "Tersimpan Lokal & Publik Download";
    }

    private void logBackupEvent(Context context, String filename, String path, long size, String status) {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String date = sdf.format(new Date());

            BackupLog log = new BackupLog(filename, path, size, date, status);
            dbHelper.insertBackupLog(log);
        } catch (Exception e) {
            Log.e(TAG, "Failed to log backup event to database", e);
        }
    }

    private void saveBackupToPublicDownloads(Context context, String filename, File localFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CashFlowku");
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            ContentResolver resolver = context.getContentResolver();
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                try (OutputStream os = resolver.openOutputStream(uri);
                     FileInputStream fis = new FileInputStream(localFile)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                    os.flush();

                    values.clear();
                    values.put(MediaStore.Downloads.IS_PENDING, 0);
                    resolver.update(uri, values, null, null);
                    Log.d(TAG, "Copied backup to public Downloads: " + uri.toString());
                } catch (IOException e) {
                    Log.e(TAG, "Error writing backup to public Downloads", e);
                }
            }
        } else {
            // Legacy public storage
            File publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File subDir = new File(publicDir, "CashFlowku");
            if (!subDir.exists()) {
                subDir.mkdirs();
            }
            File publicFile = new File(subDir, filename);
            try (FileInputStream fis = new FileInputStream(localFile);
                 FileOutputStream fos = new FileOutputStream(publicFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                fos.flush();
                Log.d(TAG, "Copied backup to legacy public Downloads: " + publicFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Error writing backup to legacy public Downloads", e);
            }
        }
    }
}
