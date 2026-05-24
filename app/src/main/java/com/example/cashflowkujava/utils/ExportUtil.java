package com.example.cashflowkujava.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.cashflowkujava.models.Expense;
import com.example.cashflowkujava.models.Sale;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExportUtil {

    private static final String TAG = "ExportUtil";

    /**
     * Exports sales and expenses between dates to a CSV file.
     * CSV files open natively in Microsoft Excel.
     */
    /**
     * Exports sales and expenses between dates to a CSV file.
     * CSV files open natively in Microsoft Excel.
     */
    public static File exportToExcel(Context context, String fromDate, String toDate, List<Sale> sales, List<Expense> expenses) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) return null;

        String filename = "Laporan_Keuangan_" + fromDate + "_ke_" + toDate + ".csv";
        File localFile = new File(dir, filename);

        try (FileOutputStream fos = new FileOutputStream(localFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {

            // Write BOM for Excel UTF-8 recognition
            writer.write('\ufeff');

            // Header Section
            writer.write("LAPORAN KEUANGAN CASHFLOWKU\n");
            writer.write("Periode Laporan: " + fromDate + " s/d " + toDate + "\n\n");

            // Sales Header
            writer.write("DAFTAR PENJUALAN\n");
            writer.write("No.;Tanggal;Nama Produk;QTY;Harga Jual;Harga Modal (HPP);Subtotal;Total Modal (HPP);Laba Kotor;Metode Pembayaran;Catatan\n");

            int rowNum = 6; // Starts at row 6 (Title=1, Period=2, Blank=3, TableTitle=4, Header=5)
            if (sales.isEmpty()) {
                // Write placeholder
                writer.write("1;;Tidak ada transaksi penjualan;0;0;0;0;0;0;;\n");
                rowNum++;
            } else {
                for (int i = 0; i < sales.size(); i++) {
                    Sale sale = sales.get(i);
                    // Formulas:
                    // Subtotal (G) = Qty (D) * Price (E) -> =D[row]*E[row]
                    // Total Modal (H) = Qty (D) * Modal (F) -> =D[row]*F[row]
                    // Laba Kotor (I) = Subtotal (G) - Total Modal (H) -> =G[row]-H[row]
                    writer.write(String.format(java.util.Locale.US, "%d;%s;%s;%d;%.0f;%.0f;=D%d*E%d;=D%d*F%d;=G%d-H%d;%s;%s\n",
                            i + 1,
                            sale.getDate(),
                            sale.getProductName(),
                            sale.getQty(),
                            sale.getPrice(),
                            sale.getModal(),
                            rowNum, rowNum,
                            rowNum, rowNum,
                            rowNum, rowNum,
                            sale.getPaymentMethod(),
                            sale.getNotes() != null ? sale.getNotes().replace(";", " ") : ""));
                    rowNum++;
                }
            }

            int salesTotalRow = rowNum;
            // Write sales total formulas row (aligned with G, H, I)
            // No (A), Tanggal (B), Produk (C), Qty (D), Harga (E), Modal (F), Subtotal(G), TotalModal(H), LabaKotor(I)
            int salesStartRow = 6;
            int salesEndRow = sales.isEmpty() ? 6 : 5 + sales.size();
            writer.write(String.format(java.util.Locale.US, ";;;;;TOTAL;=SUM(G%d:G%d);=SUM(H%d:H%d);=SUM(I%d:I%d);;\n\n",
                    salesStartRow, salesEndRow, salesStartRow, salesEndRow, salesStartRow, salesEndRow));
            rowNum += 2;

            // Expenses Header
            writer.write("DAFTAR PENGELUARAN\n");
            writer.write("No.;Tanggal;Kategori;Nominal;Catatan\n");
            rowNum += 2;

            int expStartRow = rowNum;
            if (expenses.isEmpty()) {
                writer.write("1;;Tidak ada pengeluaran;0;\n");
                rowNum++;
            } else {
                for (int i = 0; i < expenses.size(); i++) {
                    Expense exp = expenses.get(i);
                    writer.write(String.format(java.util.Locale.US, "%d;%s;%s;%.0f;%s\n",
                            i + 1,
                            exp.getDate(),
                            exp.getCategory(),
                            exp.getAmount(),
                            exp.getNotes() != null ? exp.getNotes().replace(";", " ") : ""));
                    rowNum++;
                }
            }

            int expensesTotalRow = rowNum;
            int expEndRow = expenses.isEmpty() ? expStartRow : expStartRow + expenses.size() - 1;
            writer.write(String.format(java.util.Locale.US, ";;;TOTAL;=SUM(D%d:D%d);\n\n", expStartRow, expEndRow));
            rowNum += 2;

            // Summary Section
            writer.write("RINGKASAN LAPORAN KEUANGAN\n");
            writer.write(String.format(java.util.Locale.US, "Total Pemasukan (Penjualan);=G%d\n", salesTotalRow));
            writer.write(String.format(java.util.Locale.US, "Total Harga Pokok Penjualan (Modal);=H%d\n", salesTotalRow));
            writer.write(String.format(java.util.Locale.US, "Total Laba Kotor;=I%d\n", salesTotalRow));
            writer.write(String.format(java.util.Locale.US, "Total Pengeluaran;=D%d\n", expensesTotalRow));
            writer.write(String.format(java.util.Locale.US, "Laba Bersih;=I%d-D%d\n", salesTotalRow, expensesTotalRow));

            writer.flush();
            Log.d(TAG, "CSV exported locally with formulas: " + localFile.getAbsolutePath());

            // Save to public downloads folder
            saveToPublicDownloads(context, filename, "text/csv", localFile);

            return localFile;

        } catch (IOException e) {
            Log.e(TAG, "Error exporting CSV", e);
            return null;
        }
    }

    /**
     * Exports financial summary to PDF using Android's native PdfDocument.
     */
    public static File exportToPdf(Context context, String fromDate, String toDate, List<Sale> sales, List<Expense> expenses) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) return null;

        String filename = "Laporan_Keuangan_" + fromDate + "_ke_" + toDate + ".pdf";
        File localFile = new File(dir, filename);

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // 1. Draw Header Band (Dark Accent Background)
        paint.setColor(0xFF18181B); // Dark Zinc
        canvas.drawRect(0, 0, 595, 85, paint);

        // Header Title
        paint.setColor(Color.WHITE);
        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        canvas.drawText("Laporan Keuangan CashFlowku", 40, 45, paint);

        // Header Subtitle
        paint.setTextSize(9f);
        paint.setFakeBoldText(false);
        paint.setColor(0xFFA1A1AA); // Muted grey
        canvas.drawText("Periode Laporan: " + fromDate + " s/d " + toDate, 40, 64, paint);

        // Calculate Totals
        double totalSales = 0;
        double totalModal = 0;
        for (Sale s : sales) {
            totalSales += s.getSubtotal();
            totalModal += (s.getModal() * s.getQty());
        }
        double grossProfit = totalSales - totalModal;
        double totalExpenses = 0;
        for (Expense e : expenses) {
            totalExpenses += e.getAmount();
        }
        double netProfit = grossProfit - totalExpenses;

        // 2. Draw Bento Summary Cards Grid
        // Card 1: Pemasukan (X: 40 to 200)
        paint.setColor(0xFFF4F4F5); // light grey background
        canvas.drawRoundRect(40, 105, 200, 160, 8, 8, paint);
        
        paint.setColor(0xFF71717A); // text secondary
        paint.setTextSize(7f);
        paint.setFakeBoldText(true);
        canvas.drawText("TOTAL PEMASUKAN", 50, 122, paint);
        
        paint.setColor(0xFF10B981); // emerald green
        paint.setTextSize(11f);
        canvas.drawText(FormatUtil.formatRupiah(totalSales), 50, 144, paint);

        // Card 2: Pengeluaran (X: 215 to 375)
        paint.setColor(0xFFF4F4F5);
        canvas.drawRoundRect(215, 105, 375, 160, 8, 8, paint);
        
        paint.setColor(0xFF71717A);
        paint.setTextSize(7f);
        paint.setFakeBoldText(true);
        canvas.drawText("TOTAL PENGELUARAN", 225, 122, paint);
        
        paint.setColor(0xFFEF4444); // red
        paint.setTextSize(11f);
        canvas.drawText(FormatUtil.formatRupiah(totalExpenses), 225, 144, paint);

        // Card 3: Laba Bersih (X: 390 to 550)
        paint.setColor(0xFFF4F4F5);
        canvas.drawRoundRect(390, 105, 550, 160, 8, 8, paint);
        
        paint.setColor(0xFF71717A);
        paint.setTextSize(7f);
        paint.setFakeBoldText(true);
        canvas.drawText("LABA BERSIH", 400, 122, paint);
        
        paint.setColor(netProfit >= 0 ? 0xFF10B981 : 0xFFEF4444);
        paint.setTextSize(11f);
        canvas.drawText(FormatUtil.formatRupiah(netProfit), 400, 144, paint);

        // 3. Draw Sales List Table
        paint.setColor(0xFF18181B);
        paint.setFakeBoldText(true);
        paint.setTextSize(10f);
        canvas.drawText("Rincian Transaksi Penjualan", 40, 190, paint);

        // Sales Table header band
        int y = 205;
        paint.setColor(0xFFE4E4E7); // Header grey background
        canvas.drawRect(40, y, 550, y + 16, paint);

        paint.setColor(0xFF18181B);
        paint.setTextSize(7.5f);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Tanggal", 45, y + 11, paint);
        canvas.drawText("Produk", 110, y + 11, paint);
        
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Qty", 280, y + 11, paint);
        canvas.drawText("Harga", 360, y + 11, paint);
        canvas.drawText("Modal", 440, y + 11, paint);
        canvas.drawText("Total", 545, y + 11, paint);

        y += 24;
        paint.setFakeBoldText(false);
        paint.setTextSize(7.5f);

        if (sales.isEmpty()) {
            paint.setColor(0xFF71717A);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Tidak ada transaksi penjualan dalam periode ini", 295, y, paint);
            y += 16;
        } else {
            int maxSalesToShow = Math.min(sales.size(), 12);
            for (int i = 0; i < maxSalesToShow; i++) {
                Sale s = sales.get(i);
                
                // Zebra row stripe background
                if (i % 2 == 1) {
                    paint.setColor(0xFFF9FAFB);
                    canvas.drawRect(40, y - 10, 550, y + 6, paint);
                }

                // Row divider
                paint.setColor(0xFFE4E4E7);
                paint.setStrokeWidth(0.5f);
                canvas.drawLine(40, y + 6, 550, y + 6, paint);

                paint.setColor(0xFF27272A);
                paint.setTextAlign(Paint.Align.LEFT);
                String displayDate = s.getDate().length() > 10 ? s.getDate().substring(0, 10) : s.getDate();
                canvas.drawText(displayDate, 45, y, paint);
                
                String name = s.getProductName();
                if (name.length() > 28) name = name.substring(0, 25) + "...";
                canvas.drawText(name, 110, y, paint);

                // Numeric columns aligned to the right
                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(String.valueOf(s.getQty()), 280, y, paint);
                canvas.drawText(FormatUtil.formatRupiah(s.getPrice()), 360, y, paint);
                canvas.drawText(FormatUtil.formatRupiah(s.getModal()), 440, y, paint);
                canvas.drawText(FormatUtil.formatRupiah(s.getSubtotal()), 545, y, paint);
                
                y += 16;
            }

            if (sales.size() > 12) {
                paint.setColor(0xFF71717A);
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText("... dan " + (sales.size() - 12) + " transaksi lainnya", 45, y, paint);
                y += 16;
            }
        }

        // 4. Draw Expenses List Table
        y += 10;
        paint.setColor(0xFF18181B);
        paint.setFakeBoldText(true);
        paint.setTextSize(10f);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Rincian Transaksi Pengeluaran", 40, y, paint);

        // Expenses Table header band
        y += 15;
        paint.setColor(0xFFE4E4E7);
        canvas.drawRect(40, y, 550, y + 16, paint);

        paint.setColor(0xFF18181B);
        paint.setTextSize(7.5f);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Tanggal", 45, y + 11, paint);
        canvas.drawText("Kategori", 110, y + 11, paint);
        canvas.drawText("Keterangan/Catatan", 240, y + 11, paint);
        
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Nominal", 545, y + 11, paint);

        y += 24;
        paint.setFakeBoldText(false);
        paint.setTextSize(7.5f);

        if (expenses.isEmpty()) {
            paint.setColor(0xFF71717A);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Tidak ada transaksi pengeluaran dalam periode ini", 295, y, paint);
        } else {
            int maxExpToShow = Math.min(expenses.size(), 12);
            for (int i = 0; i < maxExpToShow; i++) {
                Expense e = expenses.get(i);

                // Zebra row stripe background
                if (i % 2 == 1) {
                    paint.setColor(0xFFF9FAFB);
                    canvas.drawRect(40, y - 10, 550, y + 6, paint);
                }

                // Row divider
                paint.setColor(0xFFE4E4E7);
                paint.setStrokeWidth(0.5f);
                canvas.drawLine(40, y + 6, 550, y + 6, paint);

                paint.setColor(0xFF27272A);
                paint.setTextAlign(Paint.Align.LEFT);
                String displayDate = e.getDate().length() > 10 ? e.getDate().substring(0, 10) : e.getDate();
                canvas.drawText(displayDate, 45, y, paint);
                canvas.drawText(e.getCategory(), 110, y, paint);
                
                String desc = e.getNotes() != null ? e.getNotes() : "-";
                if (desc.length() > 42) desc = desc.substring(0, 39) + "...";
                canvas.drawText(desc, 240, y, paint);

                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(FormatUtil.formatRupiah(e.getAmount()), 545, y, paint);

                y += 16;
            }

            if (expenses.size() > 12) {
                paint.setColor(0xFF71717A);
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText("... dan " + (expenses.size() - 12) + " pengeluaran lainnya", 45, y, paint);
            }
        }

        pdfDocument.finishPage(page);

        try {
            pdfDocument.writeTo(new FileOutputStream(localFile));
            Log.d(TAG, "Premium PDF exported locally: " + localFile.getAbsolutePath());

            // Save to public downloads folder
            saveToPublicDownloads(context, filename, "application/pdf", localFile);

            return localFile;
        } catch (IOException e) {
            Log.e(TAG, "Error writing PDF", e);
            return null;
        } finally {
            pdfDocument.close();
        }
    }

    /**
     * Launches the system intent chooser to open/view/share the exported file.
     */
    public static void openFile(Context context, File file, String mimeType) {
        try {
            Uri fileUri = FileProvider.getUriForFile(context, 
                    "com.example.cashflowkujava.fileprovider", file);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            context.startActivity(Intent.createChooser(intent, "Buka Laporan Keuangan"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Tidak ada aplikasi penampil berkas ini. Berkas tersimpan di folder Download/CashFlowku.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Copy database backups to the public downloads location.
     */
    private static void saveToPublicDownloads(Context context, String filename, String mimeType, File localFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE, mimeType);
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
                    Log.d(TAG, "Copied file to public Downloads via MediaStore: " + uri.toString());
                } catch (IOException e) {
                    Log.e(TAG, "Error writing to public Downloads via MediaStore", e);
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
                Log.d(TAG, "Copied file to legacy public Downloads: " + publicFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Error writing to legacy public Downloads", e);
            }
        }
    }
}
