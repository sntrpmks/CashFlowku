package com.example.cashflowkujava.activities;

import androidx.appcompat.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashflowkujava.R;
import com.example.cashflowkujava.adapters.SalesAdapter;
import com.example.cashflowkujava.database.DatabaseHelper;
import com.example.cashflowkujava.models.Product;
import com.example.cashflowkujava.models.Sale;
import com.example.cashflowkujava.utils.FormatUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SalesActivity extends AppCompatActivity implements SalesAdapter.OnSaleClickListener {

    private EditText etSearch, etFromDate, etToDate;
    private TextView tvTotalSummary, tvEmptyState;
    private RecyclerView rvSales;
    private Button btnReset;

    private DatabaseHelper dbHelper;
    private List<Sale> salesList;
    private SalesAdapter adapter;

    private String filterQuery = "";
    private String filterFromDate = "";
    private String filterToDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);

        dbHelper = new DatabaseHelper(this);

        // Bind Views
        etSearch = findViewById(R.id.et_search_sales);
        etFromDate = findViewById(R.id.et_sales_from_date);
        etToDate = findViewById(R.id.et_sales_to_date);
        tvTotalSummary = findViewById(R.id.tv_total_sales_summary);
        tvEmptyState = findViewById(R.id.tv_empty_sales);
        rvSales = findViewById(R.id.rv_sales_list);
        btnReset = findViewById(R.id.btn_filter_sales_reset);

        rvSales.setLayoutManager(new LinearLayoutManager(this));

        // Back button
        findViewById(R.id.btn_back_sales).setOnClickListener(v -> finish());

        // Default: today's transactions
        String today = FormatUtil.getTodayJustDateString();
        filterFromDate = today;
        filterToDate = today;
        etFromDate.setText(FormatUtil.formatDateToIndonesian(today));
        etToDate.setText(FormatUtil.formatDateToIndonesian(today));

        // Search text watcher
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterQuery = s.toString();
                loadSalesData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Date Picker listeners
        etFromDate.setOnClickListener(v -> showDatePicker(true));
        etToDate.setOnClickListener(v -> showDatePicker(false));

        // Reset filter
        btnReset.setOnClickListener(v -> {
            etSearch.setText("");
            etFromDate.setText("");
            etToDate.setText("");
            filterQuery = "";
            filterFromDate = "";
            filterToDate = "";
            loadSalesData();
        });

        // FAB to add sale
        findViewById(R.id.fab_add_sale).setOnClickListener(v -> showAddSaleDialog());

        loadSalesData();
    }

    private void showDatePicker(boolean isFromDate) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog picker = new DatePickerDialog(this, (view, y, m, d) -> {
            String selectedDate = String.format(Locale.US, "%d-%02d-%02d", y, m + 1, d);
            if (isFromDate) {
                etFromDate.setText(FormatUtil.formatDateToIndonesian(selectedDate));
                filterFromDate = selectedDate;
            } else {
                etToDate.setText(FormatUtil.formatDateToIndonesian(selectedDate));
                filterToDate = selectedDate;
            }
            loadSalesData();
        }, year, month, day);
        picker.show();
    }

    private void loadSalesData() {
        salesList = dbHelper.getSalesFiltered(filterQuery, filterFromDate, filterToDate);
        if (salesList.isEmpty()) {
            rvSales.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvSales.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            adapter = new SalesAdapter(salesList, this);
            rvSales.setAdapter(adapter);
        }

        // Calculate and format total sales income
        double total = 0;
        for (Sale s : salesList) {
            total += s.getSubtotal();
        }
        tvTotalSummary.setText(FormatUtil.formatRupiah(total));
    }

    private void showAddSaleDialog() {
        List<Product> products = dbHelper.getAllProducts();
        if (products.isEmpty()) {
            Toast.makeText(this, "Silakan tambah produk di menu kelola produk terlebih dahulu!", Toast.LENGTH_LONG).show();
            return;
        }

        int pad = (int) (10 * getResources().getDisplayMetrics().density);

        // Custom Layout creation for Add Sale Dialog
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        // Product Selector Spinner
        TextView tvLabelProduct = new TextView(this);
        tvLabelProduct.setText("Pilih Produk:");
        tvLabelProduct.setPadding(0, 16, 0, 8);
        tvLabelProduct.setTextColor(getResources().getColor(R.color.textSecondary));
        layout.addView(tvLabelProduct);

        Spinner spProduct = new Spinner(this);
        ArrayAdapter<Product> prodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, products);
        prodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spProduct.setAdapter(prodAdapter);
        spProduct.setBackgroundResource(R.drawable.bg_spinner);
        spProduct.setPadding(pad, pad, pad + (int)(24 * getResources().getDisplayMetrics().density), pad);
        layout.addView(spProduct);

        // Stock info label
        TextView tvStockInfo = new TextView(this);
        tvStockInfo.setPadding(0, 4, 0, 4);
        tvStockInfo.setTextSize(12);
        tvStockInfo.setTextColor(getResources().getColor(R.color.textMuted));
        layout.addView(tvStockInfo);

        // Price Input
        TextView tvLabelPrice = new TextView(this);
        tvLabelPrice.setText("Harga Satuan:");
        tvLabelPrice.setPadding(0, 16, 0, 8);
        tvLabelPrice.setTextColor(getResources().getColor(R.color.textSecondary));
        layout.addView(tvLabelPrice);

        EditText etPrice = new EditText(this);
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etPrice.setBackgroundResource(R.drawable.bg_input);
        etPrice.setPadding(pad, pad, pad, pad);
        etPrice.setTextColor(getResources().getColor(R.color.textPrimary));
        etPrice.setHintTextColor(getResources().getColor(R.color.textMuted));
        etPrice.setTextSize(14);
        layout.addView(etPrice);

        // Qty Input
        TextView tvLabelQty = new TextView(this);
        tvLabelQty.setText("Jumlah (Qty):");
        tvLabelQty.setPadding(0, 16, 0, 8);
        tvLabelQty.setTextColor(getResources().getColor(R.color.textSecondary));
        layout.addView(tvLabelQty);

        EditText etQty = new EditText(this);
        etQty.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etQty.setText("1");
        etQty.setBackgroundResource(R.drawable.bg_input);
        etQty.setPadding(pad, pad, pad, pad);
        etQty.setTextColor(getResources().getColor(R.color.textPrimary));
        etQty.setHintTextColor(getResources().getColor(R.color.textMuted));
        etQty.setTextSize(14);
        layout.addView(etQty);

        // Payment Method Selector Spinner
        TextView tvLabelPay = new TextView(this);
        tvLabelPay.setText("Metode Pembayaran:");
        tvLabelPay.setPadding(0, 16, 0, 8);
        tvLabelPay.setTextColor(getResources().getColor(R.color.textSecondary));
        layout.addView(tvLabelPay);

        Spinner spPayment = new Spinner(this);
        String[] payMethods = {"Tunai", "Transfer Bank", "E-Wallet (QRIS)"};
        ArrayAdapter<String> payAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, payMethods);
        payAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPayment.setAdapter(payAdapter);
        spPayment.setBackgroundResource(R.drawable.bg_spinner);
        spPayment.setPadding(pad, pad, pad + (int)(24 * getResources().getDisplayMetrics().density), pad);
        layout.addView(spPayment);

        // Notes Input
        TextView tvLabelNotes = new TextView(this);
        tvLabelNotes.setText("Catatan:");
        tvLabelNotes.setPadding(0, 16, 0, 8);
        tvLabelNotes.setTextColor(getResources().getColor(R.color.textSecondary));
        layout.addView(tvLabelNotes);

        EditText etNotes = new EditText(this);
        etNotes.setHint("Tambahkan catatan (opsional)...");
        etNotes.setBackgroundResource(R.drawable.bg_input);
        etNotes.setPadding(pad, pad, pad, pad);
        etNotes.setTextColor(getResources().getColor(R.color.textPrimary));
        etNotes.setHintTextColor(getResources().getColor(R.color.textMuted));
        etNotes.setTextSize(14);
        layout.addView(etNotes);

        // Subtotal Preview Text
        TextView tvSubtotalPreview = new TextView(this);
        tvSubtotalPreview.setText("Total Subtotal: Rp 0");
        tvSubtotalPreview.setPadding(0, 32, 0, 16);
        tvSubtotalPreview.setTextSize(16);
        tvSubtotalPreview.setTypeface(null, android.graphics.Typeface.BOLD);
        tvSubtotalPreview.setTextColor(getResources().getColor(R.color.colorIncome));
        layout.addView(tvSubtotalPreview);

        // Wrap in ScrollView so all fields are accessible on small screens
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        scrollView.addView(layout);

        // Auto update values when product changes
        spProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Product selected = (Product) parent.getSelectedItem();
                etPrice.setText(String.format(Locale.US, "%.0f", selected.getPrice()));
                int stock = selected.getStock();
                if (stock <= 0) {
                    tvStockInfo.setText("⚠ Stok habis! Tidak dapat dijual.");
                    tvStockInfo.setTextColor(getResources().getColor(R.color.colorExpense));
                } else {
                    tvStockInfo.setText("Stok tersedia: " + stock + " unit");
                    tvStockInfo.setTextColor(getResources().getColor(R.color.textMuted));
                }
                updateSubtotalPreview(etPrice, etQty, tvSubtotalPreview);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Watch inputs to update subtotal live
        TextWatcher subWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSubtotalPreview(etPrice, etQty, tvSubtotalPreview);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        etPrice.addTextChangedListener(subWatcher);
        etQty.addTextChangedListener(subWatcher);

        // Build dialog using builder's setPositiveButton (correct pattern — ensures buttons are visible)
        new AlertDialog.Builder(this)
                .setTitle("Tambah Transaksi Penjualan")
                .setView(scrollView)
                .setPositiveButton("Simpan Transaksi", (dialogInterface, i) -> {
                    Product selected = (Product) spProduct.getSelectedItem();
                    String priceStr = etPrice.getText().toString();
                    String qtyStr = etQty.getText().toString();

                    if (priceStr.isEmpty() || qtyStr.isEmpty()) {
                        Toast.makeText(SalesActivity.this, "Harga dan Qty tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Block if stock is zero
                    if (selected.getStock() <= 0) {
                        Toast.makeText(SalesActivity.this, "Stok " + selected.getName() + " sudah habis! Tidak dapat diproses.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    double price = Double.parseDouble(priceStr);
                    int qty = Integer.parseInt(qtyStr);

                    // Block if qty exceeds available stock
                    if (qty > selected.getStock()) {
                        Toast.makeText(SalesActivity.this, "Qty melebihi stok! Stok tersedia: " + selected.getStock(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    double subtotal = price * qty;
                    String payMethod = spPayment.getSelectedItem().toString();
                    String notes = etNotes.getText().toString();
                    double modal = selected.getModal();

                    Sale newSale = new Sale(
                            FormatUtil.getTodayDateString(),
                            selected.getName(),
                            qty,
                            price,
                            modal,
                            subtotal,
                            payMethod,
                            notes
                    );

                    long result = dbHelper.insertSale(newSale);
                    if (result != -1) {
                        Toast.makeText(SalesActivity.this, "Penjualan berhasil dicatat!", Toast.LENGTH_SHORT).show();
                        loadSalesData();
                    } else {
                        Toast.makeText(SalesActivity.this, "Gagal mencatat penjualan.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private void updateSubtotalPreview(EditText etPrice, EditText etQty, TextView tvSubtotal) {
        try {
            String pStr = etPrice.getText().toString();
            String qStr = etQty.getText().toString();
            double p = pStr.isEmpty() ? 0 : Double.parseDouble(pStr);
            int q = qStr.isEmpty() ? 0 : Integer.parseInt(qStr);
            tvSubtotal.setText("Total Subtotal: " + FormatUtil.formatRupiah(p * q));
        } catch (NumberFormatException ignored) {}
    }

    @Override
    public void onSaleClick(Sale sale) {
        // Option to view or edit notes
        Toast.makeText(this, "Produk: " + sale.getProductName() + "\nMetode: " + sale.getPaymentMethod(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSaleLongClick(Sale sale) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Transaksi")
                .setMessage("Apakah Anda yakin ingin menghapus penjualan " + sale.getProductName() + "?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    int rows = dbHelper.deleteSale(sale.getId());
                    if (rows > 0) {
                        Toast.makeText(SalesActivity.this, "Transaksi berhasil dihapus.", Toast.LENGTH_SHORT).show();
                        loadSalesData();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}
