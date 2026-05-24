package com.example.cashflowkujava.services;

import android.content.Context;

public interface BackupService {
    boolean performBackup(Context context);
    boolean performRestore(Context context);
    String getBackupStatusInfo();
}
