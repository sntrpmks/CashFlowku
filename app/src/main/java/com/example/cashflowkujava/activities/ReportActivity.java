package com.example.cashflowkujava.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashflowkujava.R;
import com.example.cashflowkujava.adapters.TransactionAdapter;
import com.example.cashflowkujava.database.DatabaseHelper;
import com.example.cashflowkujava.database.DatabaseHelper.RecentTransaction;
import com.example.cashflowkujava.models.Expense;
import com.example.cashflowkujava.models.Sale;
import com.example.cashflowkujava.utils.ExportUtil;
import com.example.cashflowkujava.utils.FormatUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    private EditText etFromDate, etToDate;
    private Button btnRefresh, btnPdf, btnExcel;
    private TextView tvNetProfit, tvIncome, tvTotalModal, tvGrossProfit, tvExpenses, tvMargin, tvEmptyState;
    private RecyclerView rvTransactions;

    private DatabaseHelper dbHelper;
    private List<Sale> salesPeriodList;
    private List<Expense> expensesPeriodList;
    private List<RecentTransaction> combinedPeriodList;

    private String queryFromDate = "";
    private String queryToDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        dbHelper = new DatabaseHelper(this);

        // Bind Views
        etFromDate = findViewById(R.id.et_report_from_date);
        etToDate = findViewById(R.id.et_report_to_date);
        btnRefresh = findViewById(R.id.btn_report_refresh);
        btnPdf = findViewById(R.id.btn_export_pdf);
        btnExcel = findViewById(R.id.btn_export_excel);
        tvNetProfit = findViewById(R.id.tv_report_net_profit);
        tvIncome = findViewById(R.id.tv_report_income);
        tvTotalModal = findViewById(R.id.tv_report_total_modal);
        tvGrossProfit = findViewById(R.id.tv_report_gross_profit);
        tvExpenses = findViewById(R.id.tv_report_expenses);
        tvMargin = findViewById(R.id.tv_report_margin);
        tvEmptyState = findViewById(R.id.tv_empty_report_list);
        rvTransactions = findViewById(R.id.rv_report_transactions);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));

        // Back action
        findViewById(R.id.btn_back_report).setOnClickListener(v -> finish());

        // Default Date: Start of current month to today
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startOfMonth = cal.getTime();

        SimpleDateFormat dbSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        queryFromDate = dbSdf.format(startOfMonth);
        queryToDate = dbSdf.format(today);

        etFromDate.setText(FormatUtil.formatDateToIndonesian(queryFromDate));
        etToDate.setText(FormatUtil.formatDateToIndonesian(queryToDate));

        // Listeners
        etFromDate.setOnClickListener(v -> showDatePicker(true));
        etToDate.setOnClickListener(v -> showDatePicker(false));

        btnRefresh.setOnClickListener(v -> loadReportData());

        // Document Export listeners
        btnPdf.setOnClickListener(v -> exportPdfFile());
        btnExcel.setOnClickListener(v -> exportExcelFile());

        loadReportData();
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
                queryFromDate = selectedDate;
            } else {
                etToDate.setText(FormatUtil.formatDateToIndonesian(selectedDate));
                queryToDate = selectedDate;
            }
        }, year, month, day);
        picker.show();
    }

    private void loadReportData() {
        // Fetch filtered items
        salesPeriodList = dbHelper.getSalesFiltered("", queryFromDate, queryToDate);
        expensesPeriodList = dbHelper.getExpensesFiltered("", "Semua", queryFromDate, queryToDate);

        // Map to combined list
        combinedPeriodList = new ArrayList<>();
        double totalSales = 0;
        double totalModal = 0;
        for (Sale s : salesPeriodList) {
            RecentTransaction t = new RecentTransaction();
            t.id = s.getId();
            t.type = "Penjualan";
            t.description = s.getProductName() + " (x" + s.getQty() + ")";
            t.amount = s.getSubtotal();
            t.date = s.getDate();
            t.createdAt = s.getCreatedAt();
            t.imagePath = s.getProductImagePath();
            combinedPeriodList.add(t);
            totalSales += s.getSubtotal();
            totalModal += (s.getModal() * s.getQty());
        }

        double totalExpenses = 0;
        for (Expense e : expensesPeriodList) {
            RecentTransaction t = new RecentTransaction();
            t.id = e.getId();
            t.type = "Pengeluaran";
            t.description = e.getCategory() + ": " + (e.getNotes() != null ? e.getNotes() : "");
            t.amount = e.getAmount();
            t.date = e.getDate();
            t.createdAt = e.getCreatedAt();
            t.imagePath = e.getReceiptPhoto();
            combinedPeriodList.add(t);
            totalExpenses += e.getAmount();
        }

        // Sort combined chronologically (newest first)
        combinedPeriodList.sort((t1, t2) -> t2.date.compareTo(t1.date));

        // Bind RecyclerView
        if (combinedPeriodList.isEmpty()) {
            rvTransactions.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvTransactions.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            TransactionAdapter adapter = new TransactionAdapter(combinedPeriodList);
            rvTransactions.setAdapter(adapter);
        }

        // Bind summaries cards
        tvIncome.setText(FormatUtil.formatRupiah(totalSales));
        tvTotalModal.setText(FormatUtil.formatRupiah(totalModal));

        double grossProfit = totalSales - totalModal;
        tvGrossProfit.setText(FormatUtil.formatRupiah(grossProfit));

        tvExpenses.setText(FormatUtil.formatRupiah(totalExpenses));

        double netProfit = grossProfit - totalExpenses;
        if (netProfit >= 0) {
            tvNetProfit.setText(FormatUtil.formatRupiah(netProfit));
            tvNetProfit.setTextColor(getResources().getColor(R.color.textPrimary));
        } else {
            tvNetProfit.setText("-" + FormatUtil.formatRupiah(Math.abs(netProfit)));
            tvNetProfit.setTextColor(getResources().getColor(R.color.colorExpense));
        }

        // Calculate margin percentage
        double margin = 0;
        if (totalSales > 0) {
            margin = (netProfit / totalSales) * 100;
        }
        tvMargin.setText(String.format(Locale.getDefault(), "%.1f%%", margin));
    }

    private void exportPdfFile() {
        if (salesPeriodList == null || expensesPeriodList == null) {
            Toast.makeText(this, "Silakan tampilkan laporan terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        File file = ExportUtil.exportToPdf(this, queryFromDate, queryToDate, salesPeriodList, expensesPeriodList);
        if (file != null) {
            Toast.makeText(this, "PDF Berhasil diekspor!", Toast.LENGTH_SHORT).show();
            ExportUtil.openFile(this, file, "application/pdf");
        } else {
            Toast.makeText(this, "Gagal mengekspor PDF.", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportExcelFile() {
        if (salesPeriodList == null || expensesPeriodList == null) {
            Toast.makeText(this, "Silakan tampilkan laporan terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = ExportUtil.exportToExcel(this, queryFromDate, queryToDate, salesPeriodList, expensesPeriodList);
        if (file != null) {
            Toast.makeText(this, "Excel Berhasil diekspor!", Toast.LENGTH_SHORT).show();
            ExportUtil.openFile(this, file, "text/csv");
        } else {
            Toast.makeText(this, "Gagal mengekspor Excel.", Toast.LENGTH_SHORT).show();
        }
    }
}
