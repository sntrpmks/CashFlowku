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
import com.example.cashflowkujava.adapters.ExpenseAdapter;
import com.example.cashflowkujava.database.DatabaseHelper;
import com.example.cashflowkujava.models.Expense;
import com.example.cashflowkujava.utils.FormatUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseActivity extends AppCompatActivity implements ExpenseAdapter.OnExpenseClickListener {

    private EditText etSearch, etFromDate, etToDate;
    private Spinner spCategoryFilter;
    private TextView tvTotalSummary, tvEmptyState;
    private RecyclerView rvExpenses;
    private Button btnReset;

    private DatabaseHelper dbHelper;
    private List<Expense> expensesList;
    private ExpenseAdapter adapter;

    private String filterQuery = "";
    private String filterCategory = "Semua";
    private String filterFromDate = "";
    private String filterToDate = "";

    private final String[] categories = {"Semua", "Bahan Baku", "Operasional", "Gaji Karyawan", "Sewa Tempat", "Lain-lain"};
    private final String[] addCategories = {"Bahan Baku", "Operasional", "Gaji Karyawan", "Sewa Tempat", "Lain-lain"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        dbHelper = new DatabaseHelper(this);

        // Bind Views
        etSearch = findViewById(R.id.et_search_expenses);
        spCategoryFilter = findViewById(R.id.sp_filter_category);
        etFromDate = findViewById(R.id.et_expenses_from_date);
        etToDate = findViewById(R.id.et_expenses_to_date);
        tvTotalSummary = findViewById(R.id.tv_total_expenses_summary);
        tvEmptyState = findViewById(R.id.tv_empty_expenses);
        rvExpenses = findViewById(R.id.rv_expenses_list);
        btnReset = findViewById(R.id.btn_filter_expenses_reset);

        rvExpenses.setLayoutManager(new LinearLayoutManager(this));

        // Back button
        findViewById(R.id.btn_back_expenses).setOnClickListener(v -> finish());

        // Default: today's transactions
        String today = FormatUtil.getTodayJustDateString();
        filterFromDate = today;
        filterToDate = today;
        etFromDate.setText(FormatUtil.formatDateToIndonesian(today));
        etToDate.setText(FormatUtil.formatDateToIndonesian(today));

        // Spinner Setup
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoryFilter.setAdapter(catAdapter);

        // Search text watcher
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterQuery = s.toString();
                loadExpensesData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Category spinner listener
        spCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterCategory = categories[position];
                loadExpensesData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Date Picker listeners
        etFromDate.setOnClickListener(v -> showDatePicker(true));
        etToDate.setOnClickListener(v -> showDatePicker(false));

        // Reset filter
        btnReset.setOnClickListener(v -> {
            etSearch.setText("");
            spCategoryFilter.setSelection(0);
            etFromDate.setText("");
            etToDate.setText("");
            filterQuery = "";
            filterCategory = "Semua";
            filterFromDate = "";
            filterToDate = "";
            loadExpensesData();
        });

        // FAB to add expense
        findViewById(R.id.fab_add_expense).setOnClickListener(v -> showAddExpenseDialog());

        loadExpensesData();
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
            loadExpensesData();
        }, year, month, day);
        picker.show();
    }

    private void loadExpensesData() {
        expensesList = dbHelper.getExpensesFiltered(filterQuery, filterCategory, filterFromDate, filterToDate);
        if (expensesList.isEmpty()) {
            rvExpenses.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvExpenses.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            adapter = new ExpenseAdapter(expensesList, this);
            rvExpenses.setAdapter(adapter);
        }

        // Calculate and format total expenses sum
        double total = 0;
        for (Expense e : expensesList) {
            total += e.getAmount();
        }
        tvTotalSummary.setText(FormatUtil.formatRupiah(total));
    }

    private void showAddExpenseDialog() {
        int pad = (int) (10 * getResources().getDisplayMetrics().density);

        // Custom Layout creation
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        // Category Spinner
        TextView tvLabelCat = new TextView(this);
        tvLabelCat.setText("Kategori Pengeluaran:");
        tvLabelCat.setPadding(0, 16, 0, 8);
        tvLabelCat.setTextColor(getResources().getColor(R.color.textSecondary));
        layout.addView(tvLabelCat);

        Spinner spCat = new Spinner(this);
        ArrayAdapter<String> addCatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, addCategories);
        addCatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCat.setAdapter(addCatAdapter);
        spCat.setBackgroundResource(R.drawable.bg_spinner);
        spCat.setPadding(pad, pad, pad + (int)(24 * getResources().getDisplayMetrics().density), pad);
        layout.addView(spCat);

        // Nominal Input
        TextView tvLabelNominal = new TextView(this);
        tvLabelNominal.setText("Nominal / Jumlah Pengeluaran (Rp):");
        tvLabelNominal.setPadding(0, 16, 0, 8);
        tvLabelNominal.setTextColor(getResources().getColor(R.color.textSecondary));
        layout.addView(tvLabelNominal);

        EditText etAmount = new EditText(this);
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAmount.setHint("Masukkan nominal...");
        etAmount.setBackgroundResource(R.drawable.bg_input);
        etAmount.setPadding(pad, pad, pad, pad);
        etAmount.setTextColor(getResources().getColor(R.color.textPrimary));
        etAmount.setHintTextColor(getResources().getColor(R.color.textMuted));
        etAmount.setTextSize(14);
        layout.addView(etAmount);

        // Notes Input
        TextView tvLabelNotes = new TextView(this);
        tvLabelNotes.setText("Catatan / Keterangan:");
        tvLabelNotes.setPadding(0, 16, 0, 8);
        tvLabelNotes.setTextColor(getResources().getColor(R.color.textSecondary));
        layout.addView(tvLabelNotes);

        EditText etNotes = new EditText(this);
        etNotes.setHint("Keterangan pengeluaran...");
        etNotes.setBackgroundResource(R.drawable.bg_input);
        etNotes.setPadding(pad, pad, pad, pad);
        etNotes.setTextColor(getResources().getColor(R.color.textPrimary));
        etNotes.setHintTextColor(getResources().getColor(R.color.textMuted));
        etNotes.setTextSize(14);
        layout.addView(etNotes);

        // Wrap in ScrollView for small screens
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        scrollView.addView(layout);

        // Build with builder.setPositiveButton (ensures buttons are always visible)
        new AlertDialog.Builder(this)
                .setTitle("Tambah Pengeluaran Baru")
                .setView(scrollView)
                .setPositiveButton("Simpan", (dialogInterface, i) -> {
                    String amountStr = etAmount.getText().toString();
                    String notes = etNotes.getText().toString();
                    String cat = spCat.getSelectedItem().toString();

                    if (amountStr.isEmpty()) {
                        Toast.makeText(ExpenseActivity.this, "Nominal tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = Double.parseDouble(amountStr);

                    Expense newExpense = new Expense(
                            cat,
                            amount,
                            FormatUtil.getTodayDateString(),
                            notes,
                            "" // photo path is now empty
                    );

                    long result = dbHelper.insertExpense(newExpense);
                    if (result != -1) {
                        Toast.makeText(ExpenseActivity.this, "Pengeluaran berhasil dicatat!", Toast.LENGTH_SHORT).show();
                        loadExpensesData();
                    } else {
                        Toast.makeText(ExpenseActivity.this, "Gagal mencatat pengeluaran.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    @Override
    public void onExpenseClick(Expense expense) {
        String msg = "Pengeluaran: " + expense.getCategory() + "\nNominal: " + FormatUtil.formatRupiah(expense.getAmount());
        if (expense.getNotes() != null && !expense.getNotes().isEmpty()) {
            msg += "\nCatatan: " + expense.getNotes();
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onExpenseLongClick(Expense expense) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Pengeluaran")
                .setMessage("Apakah Anda yakin ingin menghapus catatan pengeluaran " + expense.getCategory() + "?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    int rows = dbHelper.deleteExpense(expense.getId());
                    if (rows > 0) {
                        Toast.makeText(ExpenseActivity.this, "Catatan pengeluaran berhasil dihapus.", Toast.LENGTH_SHORT).show();
                        loadExpensesData();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}
