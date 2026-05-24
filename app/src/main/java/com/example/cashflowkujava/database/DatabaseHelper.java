package com.example.cashflowkujava.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.cashflowkujava.models.BackupLog;
import com.example.cashflowkujava.models.Expense;
import com.example.cashflowkujava.models.Product;
import com.example.cashflowkujava.models.Sale;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "cashflowku.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_SALES = "sales";
    private static final String TABLE_EXPENSES = "expenses";
    private static final String TABLE_BACKUP_LOGS = "backup_logs";

    // Products table columns
    private static final String COL_PROD_ID = "id";
    private static final String COL_PROD_NAME = "nama_produk";
    private static final String COL_PROD_PRICE = "harga";
    private static final String COL_PROD_MODAL = "modal";
    private static final String COL_PROD_STOCK = "stok";
    private static final String COL_PROD_CAT = "kategori";
    private static final String COL_PROD_PHOTO = "foto_produk";
    private static final String COL_PROD_CREATED = "created_at";

    // Sales table columns
    private static final String COL_SALE_ID = "id";
    private static final String COL_SALE_DATE = "tanggal";
    private static final String COL_SALE_PROD_NAME = "nama_produk";
    private static final String COL_SALE_QTY = "qty";
    private static final String COL_SALE_PRICE = "harga";
    private static final String COL_SALE_MODAL = "modal";
    private static final String COL_SALE_SUBTOTAL = "subtotal";
    private static final String COL_SALE_PAYMENT = "metode_pembayaran";
    private static final String COL_SALE_NOTES = "catatan";
    private static final String COL_SALE_CREATED = "created_at";

    // Expenses table columns
    private static final String COL_EXP_ID = "id";
    private static final String COL_EXP_CAT = "kategori";
    private static final String COL_EXP_NOMINAL = "nominal";
    private static final String COL_EXP_DATE = "tanggal";
    private static final String COL_EXP_NOTES = "catatan";
    private static final String COL_EXP_PHOTO = "foto_bukti";
    private static final String COL_EXP_CREATED = "created_at";

    // Backup Logs table columns
    private static final String COL_LOG_ID = "id";
    private static final String COL_LOG_FILE = "filename";
    private static final String COL_LOG_PATH = "filepath";
    private static final String COL_LOG_SIZE = "size";
    private static final String COL_LOG_DATE = "date";
    private static final String COL_LOG_STATUS = "status";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Products Table
        String createProductsTable = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                COL_PROD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PROD_NAME + " TEXT, " +
                COL_PROD_PRICE + " REAL, " +
                COL_PROD_MODAL + " REAL, " +
                COL_PROD_STOCK + " INTEGER, " +
                COL_PROD_CAT + " TEXT, " +
                COL_PROD_PHOTO + " TEXT, " +
                COL_PROD_CREATED + " TEXT)";
        db.execSQL(createProductsTable);

        // Create Sales Table
        String createSalesTable = "CREATE TABLE " + TABLE_SALES + " (" +
                COL_SALE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SALE_DATE + " TEXT, " +
                COL_SALE_PROD_NAME + " TEXT, " +
                COL_SALE_QTY + " INTEGER, " +
                COL_SALE_PRICE + " REAL, " +
                COL_SALE_MODAL + " REAL, " +
                COL_SALE_SUBTOTAL + " REAL, " +
                COL_SALE_PAYMENT + " TEXT, " +
                COL_SALE_NOTES + " TEXT, " +
                COL_SALE_CREATED + " TEXT)";
        db.execSQL(createSalesTable);

        // Create Expenses Table
        String createExpensesTable = "CREATE TABLE " + TABLE_EXPENSES + " (" +
                COL_EXP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EXP_CAT + " TEXT, " +
                COL_EXP_NOMINAL + " REAL, " +
                COL_EXP_DATE + " TEXT, " +
                COL_EXP_NOTES + " TEXT, " +
                COL_EXP_PHOTO + " TEXT, " +
                COL_EXP_CREATED + " TEXT)";
        db.execSQL(createExpensesTable);

        // Create Backup Logs Table
        String createBackupLogsTable = "CREATE TABLE " + TABLE_BACKUP_LOGS + " (" +
                COL_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_LOG_FILE + " TEXT, " +
                COL_LOG_PATH + " TEXT, " +
                COL_LOG_SIZE + " INTEGER, " +
                COL_LOG_DATE + " TEXT, " +
                COL_LOG_STATUS + " TEXT)";
        db.execSQL(createBackupLogsTable);

        // No products are seeded on new install to keep user data clean
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BACKUP_LOGS);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("CREATE TABLE IF NOT EXISTS settings (key TEXT PRIMARY KEY, value TEXT)");
    }

    // ==========================================
    // PRODUCTS CRUD Operations
    // ==========================================

    public long insertProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PROD_NAME, product.getName());
        cv.put(COL_PROD_PRICE, product.getPrice());
        cv.put(COL_PROD_MODAL, product.getModal());
        cv.put(COL_PROD_STOCK, product.getStock());
        cv.put(COL_PROD_CAT, product.getCategory());
        cv.put(COL_PROD_PHOTO, product.getImagePath());
        cv.put(COL_PROD_CREATED, product.getCreatedAt() != null ? product.getCreatedAt() : getDateTimeString());
        long result = db.insert(TABLE_PRODUCTS, null, cv);
        db.close();
        return result;
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS + " ORDER BY " + COL_PROD_NAME + " ASC", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Product product = new Product(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_NAME)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PROD_PRICE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PROD_MODAL)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_STOCK)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_CAT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_PHOTO)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_CREATED))
                    );
                    products.add(product);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return products;
    }

    public int updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PROD_NAME, product.getName());
        cv.put(COL_PROD_PRICE, product.getPrice());
        cv.put(COL_PROD_MODAL, product.getModal());
        cv.put(COL_PROD_STOCK, product.getStock());
        cv.put(COL_PROD_CAT, product.getCategory());
        cv.put(COL_PROD_PHOTO, product.getImagePath());
        int rows = db.update(TABLE_PRODUCTS, cv, COL_PROD_ID + " = ?", new String[]{String.valueOf(product.getId())});
        db.close();
        return rows;
    }

    public int deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_PRODUCTS, COL_PROD_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public int updateProductStock(String productName, int quantitySold) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, new String[]{COL_PROD_ID, COL_PROD_STOCK}, COL_PROD_NAME + " = ?", new String[]{productName}, null, null, null);
        int rowsUpdated = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_ID));
                int currentStock = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_STOCK));
                int newStock = Math.max(0, currentStock - quantitySold);

                ContentValues cv = new ContentValues();
                cv.put(COL_PROD_STOCK, newStock);
                rowsUpdated = db.update(TABLE_PRODUCTS, cv, COL_PROD_ID + " = ?", new String[]{String.valueOf(id)});
            }
            cursor.close();
        }
        db.close();
        return rowsUpdated;
    }

    // ==========================================
    // SALES CRUD Operations
    // ==========================================

    public long insertSale(Sale sale) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SALE_DATE, sale.getDate());
        cv.put(COL_SALE_PROD_NAME, sale.getProductName());
        cv.put(COL_SALE_QTY, sale.getQty());
        cv.put(COL_SALE_PRICE, sale.getPrice());
        cv.put(COL_SALE_MODAL, sale.getModal());
        cv.put(COL_SALE_SUBTOTAL, sale.getSubtotal());
        cv.put(COL_SALE_PAYMENT, sale.getPaymentMethod());
        cv.put(COL_SALE_NOTES, sale.getNotes());
        cv.put(COL_SALE_CREATED, sale.getCreatedAt() != null ? sale.getCreatedAt() : getDateTimeString());
        long result = db.insert(TABLE_SALES, null, cv);
        db.close();

        if (result != -1) {
            // Automatically deduct stock if product exists
            updateProductStock(sale.getProductName(), sale.getQty());
        }
        return result;
    }

    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT s.*, p." + COL_PROD_PHOTO + " FROM " + TABLE_SALES + " s " +
                "LEFT JOIN " + TABLE_PRODUCTS + " p ON s." + COL_SALE_PROD_NAME + " = p." + COL_PROD_NAME + " " +
                "ORDER BY s." + COL_SALE_DATE + " DESC, s." + COL_SALE_ID + " DESC";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Sale sale = new Sale(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_SALE_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_PROD_NAME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_SALE_QTY)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SALE_PRICE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SALE_MODAL)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SALE_SUBTOTAL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_PAYMENT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_NOTES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_CREATED))
                    );
                    int photoIdx = cursor.getColumnIndex(COL_PROD_PHOTO);
                    if (photoIdx != -1) {
                        sale.setProductImagePath(cursor.getString(photoIdx));
                    }
                    sales.add(sale);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return sales;
    }

    public int updateSale(Sale sale) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SALE_DATE, sale.getDate());
        cv.put(COL_SALE_PROD_NAME, sale.getProductName());
        cv.put(COL_SALE_QTY, sale.getQty());
        cv.put(COL_SALE_PRICE, sale.getPrice());
        cv.put(COL_SALE_MODAL, sale.getModal());
        cv.put(COL_SALE_SUBTOTAL, sale.getSubtotal());
        cv.put(COL_SALE_PAYMENT, sale.getPaymentMethod());
        cv.put(COL_SALE_NOTES, sale.getNotes());
        int rows = db.update(TABLE_SALES, cv, COL_SALE_ID + " = ?", new String[]{String.valueOf(sale.getId())});
        db.close();
        return rows;
    }

    public int deleteSale(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_SALES, COL_SALE_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public List<Sale> getSalesFiltered(String query, String fromDate, String toDate) {
        List<Sale> sales = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder sql = new StringBuilder("SELECT s.*, p." + COL_PROD_PHOTO + " FROM " + TABLE_SALES + " s " +
                "LEFT JOIN " + TABLE_PRODUCTS + " p ON s." + COL_SALE_PROD_NAME + " = p." + COL_PROD_NAME + " WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND s." + COL_SALE_PROD_NAME + " LIKE ?");
            args.add("%" + query + "%");
        }

        if (fromDate != null && !fromDate.trim().isEmpty() && toDate != null && !toDate.trim().isEmpty()) {
            sql.append(" AND substr(s." + COL_SALE_DATE + ", 1, 10) BETWEEN ? AND ?");
            args.add(fromDate);
            args.add(toDate);
        }

        sql.append(" ORDER BY s." + COL_SALE_DATE + " DESC, s." + COL_SALE_ID + " DESC");

        Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Sale sale = new Sale(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_SALE_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_PROD_NAME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_SALE_QTY)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SALE_PRICE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SALE_MODAL)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SALE_SUBTOTAL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_PAYMENT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_NOTES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_CREATED))
                    );
                    int photoIdx = cursor.getColumnIndex(COL_PROD_PHOTO);
                    if (photoIdx != -1) {
                        sale.setProductImagePath(cursor.getString(photoIdx));
                    }
                    sales.add(sale);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return sales;
    }

    // ==========================================
    // EXPENSES CRUD Operations
    // ==========================================

    public long insertExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EXP_CAT, expense.getCategory());
        cv.put(COL_EXP_NOMINAL, expense.getAmount());
        cv.put(COL_EXP_DATE, expense.getDate());
        cv.put(COL_EXP_NOTES, expense.getNotes());
        cv.put(COL_EXP_PHOTO, expense.getReceiptPhoto());
        cv.put(COL_EXP_CREATED, expense.getCreatedAt() != null ? expense.getCreatedAt() : getDateTimeString());
        long result = db.insert(TABLE_EXPENSES, null, cv);
        db.close();
        return result;
    }

    public List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EXPENSES + " ORDER BY " + COL_EXP_DATE + " DESC, " + COL_EXP_ID + " DESC", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Expense expense = new Expense(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_EXP_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_CAT)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COL_EXP_NOMINAL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_NOTES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_PHOTO)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_CREATED))
                    );
                    expenses.add(expense);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return expenses;
    }

    public int updateExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EXP_CAT, expense.getCategory());
        cv.put(COL_EXP_NOMINAL, expense.getAmount());
        cv.put(COL_EXP_DATE, expense.getDate());
        cv.put(COL_EXP_NOTES, expense.getNotes());
        cv.put(COL_EXP_PHOTO, expense.getReceiptPhoto());
        int rows = db.update(TABLE_EXPENSES, cv, COL_EXP_ID + " = ?", new String[]{String.valueOf(expense.getId())});
        db.close();
        return rows;
    }

    public int deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_EXPENSES, COL_EXP_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public List<Expense> getExpensesFiltered(String query, String category, String fromDate, String toDate) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE_EXPENSES + " WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND " + COL_EXP_NOTES + " LIKE ?");
            args.add("%" + query + "%");
        }

        if (category != null && !category.equals("Semua") && !category.trim().isEmpty()) {
            sql.append(" AND " + COL_EXP_CAT + " = ?");
            args.add(category);
        }

        if (fromDate != null && !fromDate.trim().isEmpty() && toDate != null && !toDate.trim().isEmpty()) {
            sql.append(" AND substr(" + COL_EXP_DATE + ", 1, 10) BETWEEN ? AND ?");
            args.add(fromDate);
            args.add(toDate);
        }

        sql.append(" ORDER BY " + COL_EXP_DATE + " DESC, " + COL_EXP_ID + " DESC");

        Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Expense expense = new Expense(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_EXP_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_CAT)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COL_EXP_NOMINAL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_NOTES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_PHOTO)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_CREATED))
                    );
                    expenses.add(expense);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return expenses;
    }

    // ==========================================
    // BACKUP LOGS CRUD Operations
    // ==========================================

    public long insertBackupLog(BackupLog log) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_LOG_FILE, log.getFilename());
        cv.put(COL_LOG_PATH, log.getFilepath());
        cv.put(COL_LOG_SIZE, log.getSize());
        cv.put(COL_LOG_DATE, log.getDate() != null ? log.getDate() : getDateTimeString());
        cv.put(COL_LOG_STATUS, log.getStatus());
        long result = db.insert(TABLE_BACKUP_LOGS, null, cv);
        db.close();
        return result;
    }

    public List<BackupLog> getAllBackupLogs() {
        List<BackupLog> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BACKUP_LOGS + " ORDER BY " + COL_LOG_DATE + " DESC", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    BackupLog log = new BackupLog(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOG_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_FILE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_PATH)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOG_SIZE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_STATUS))
                    );
                    logs.add(log);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return logs;
    }

    // ==========================================
    // STATS & REPORTING METHODS
    // ==========================================

    /**
     * Get total sales income for a specific day.
     */
    public double getTotalSalesForDay(String dateStr) {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COL_SALE_SUBTOTAL + ") FROM " + TABLE_SALES + " WHERE substr(" + COL_SALE_DATE + ", 1, 10) = ?";
        Cursor cursor = db.rawQuery(query, new String[]{dateStr});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
            cursor.close();
        }
        db.close();
        return total;
    }

    /**
     * Get total COGS (modal cost) of items sold on a specific day.
     */
    public double getTotalModalForDay(String dateStr) {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COL_SALE_MODAL + " * " + COL_SALE_QTY + ") FROM " + TABLE_SALES + " WHERE substr(" + COL_SALE_DATE + ", 1, 10) = ?";
        Cursor cursor = db.rawQuery(query, new String[]{dateStr});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
            cursor.close();
        }
        db.close();
        return total;
    }

    /**
     * Get total sales income between two dates (inclusive).
     */
    public double getTotalSalesBetween(String fromDate, String toDate) {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COL_SALE_SUBTOTAL + ") FROM " + TABLE_SALES + " WHERE substr(" + COL_SALE_DATE + ", 1, 10) BETWEEN ? AND ?";
        Cursor cursor = db.rawQuery(query, new String[]{fromDate, toDate});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
            cursor.close();
        }
        db.close();
        return total;
    }

    /**
     * Get total COGS (modal cost) of items sold between two dates.
     */
    public double getTotalModalBetween(String fromDate, String toDate) {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COL_SALE_MODAL + " * " + COL_SALE_QTY + ") FROM " + TABLE_SALES + " WHERE substr(" + COL_SALE_DATE + ", 1, 10) BETWEEN ? AND ?";
        Cursor cursor = db.rawQuery(query, new String[]{fromDate, toDate});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
            cursor.close();
        }
        db.close();
        return total;
    }

    /**
     * Get total expense for a specific day.
     */
    public double getTotalExpensesForDay(String dateStr) {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COL_EXP_NOMINAL + ") FROM " + TABLE_EXPENSES + " WHERE substr(" + COL_EXP_DATE + ", 1, 10) = ?";
        Cursor cursor = db.rawQuery(query, new String[]{dateStr});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
            cursor.close();
        }
        db.close();
        return total;
    }

    /**
     * Get total expenses between two dates (inclusive).
     */
    public double getTotalExpensesBetween(String fromDate, String toDate) {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COL_EXP_NOMINAL + ") FROM " + TABLE_EXPENSES + " WHERE substr(" + COL_EXP_DATE + ", 1, 10) BETWEEN ? AND ?";
        Cursor cursor = db.rawQuery(query, new String[]{fromDate, toDate});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
            cursor.close();
        }
        db.close();
        return total;
    }

    /**
     * Get recent transactions as a list of generic items (e.g. name, amount, date, type).
     */
    public List<RecentTransaction> getRecentTransactions(String dateStr) {
        List<RecentTransaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Union sales and expenses to get a single unified chronological flow
        String query = "SELECT s.id, 'Penjualan' as tipe, s.nama_produk || ' (x' || s.qty || ')' as deskripsi, s.subtotal as nominal, s.tanggal, s.created_at, p." + COL_PROD_PHOTO + " as foto " +
                "FROM " + TABLE_SALES + " s " +
                "LEFT JOIN " + TABLE_PRODUCTS + " p ON s." + COL_SALE_PROD_NAME + " = p." + COL_PROD_NAME + " " +
                "WHERE substr(s." + COL_SALE_DATE + ", 1, 10) = ? " +
                "UNION ALL " +
                "SELECT e.id, 'Pengeluaran' as tipe, e." + COL_EXP_CAT + " || ': ' || e." + COL_EXP_NOTES + " as deskripsi, e." + COL_EXP_NOMINAL + ", e." + COL_EXP_DATE + ", e." + COL_EXP_CREATED + ", e." + COL_EXP_PHOTO + " as foto " +
                "FROM " + TABLE_EXPENSES + " e " +
                "WHERE substr(e." + COL_EXP_DATE + ", 1, 10) = ? " +
                "ORDER BY tanggal DESC, created_at DESC";

        Cursor cursor = db.rawQuery(query, new String[]{dateStr, dateStr});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    RecentTransaction trans = new RecentTransaction();
                    trans.id = cursor.getInt(0);
                    trans.type = cursor.getString(1);
                    trans.description = cursor.getString(2);
                    trans.amount = cursor.getDouble(3);
                    trans.date = cursor.getString(4);
                    trans.createdAt = cursor.getString(5);
                    trans.imagePath = cursor.getString(6);
                    list.add(trans);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return list;
    }

    // Helper class for combined dashboard transactions list
    public static class RecentTransaction {
        public int id;
        public String type; // Penjualan or Pengeluaran
        public String description;
        public double amount;
        public String date;
        public String createdAt;
        public String imagePath;
    }

    // Helper to get current datetime string
    private String getDateTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // ==========================================
    // SETTINGS / PROFILE SYNC CRUD
    // ==========================================

    public void saveSetting(String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("key", key);
        cv.put("value", value);
        db.replace("settings", null, cv);
        db.close();
    }

    public String getSetting(String key, String defaultValue) {
        SQLiteDatabase db = this.getReadableDatabase();
        String value = defaultValue;
        Cursor cursor = db.query("settings", new String[]{"value"}, "key = ?", new String[]{key}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                value = cursor.getString(0);
            }
            cursor.close();
        }
        db.close();
        return value;
    }

    public void syncSettingsFromDatabase(Context context) {
        SharedPreferences bizPrefs = context.getSharedPreferences("BusinessPrefs", Context.MODE_PRIVATE);
        SharedPreferences themePrefs = context.getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);

        String dbName = getSetting("biz_name", null);
        String dbEmail = getSetting("biz_email", null);
        String dbPhone = getSetting("biz_phone", null);
        String dbTheme = getSetting("dark_mode", null);

        // If database profile settings are empty but SharedPreferences has a value, save SharedPreferences to database
        if (dbName == null) {
            String spName = bizPrefs.getString("biz_name", "Toko Saya");
            String spEmail = bizPrefs.getString("biz_email", "kontak@tokosaya.com");
            String spPhone = bizPrefs.getString("biz_phone", "08123456789");
            saveSetting("biz_name", spName);
            saveSetting("biz_email", spEmail);
            saveSetting("biz_phone", spPhone);
        } else {
            // Otherwise, sync from database to SharedPreferences
            bizPrefs.edit()
                    .putString("biz_name", dbName)
                    .putString("biz_email", dbEmail != null ? dbEmail : "kontak@tokosaya.com")
                    .putString("biz_phone", dbPhone != null ? dbPhone : "08123456789")
                    .apply();
        }

        if (dbTheme == null) {
            boolean spTheme = themePrefs.getBoolean("dark_mode", false);
            saveSetting("dark_mode", String.valueOf(spTheme));
        } else {
            boolean isDark = Boolean.parseBoolean(dbTheme);
            themePrefs.edit().putBoolean("dark_mode", isDark).apply();
            // Apply night mode immediately
            if (isDark) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }
}
