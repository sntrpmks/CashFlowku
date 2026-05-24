package com.example.cashflowkujava;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashflowkujava.activities.BackupActivity;
import com.example.cashflowkujava.activities.ExpenseActivity;
import com.example.cashflowkujava.activities.ProfileActivity;
import com.example.cashflowkujava.activities.ReportActivity;
import com.example.cashflowkujava.activities.SalesActivity;
import com.example.cashflowkujava.activities.SettingsActivity;
import com.example.cashflowkujava.adapters.TransactionAdapter;
import com.example.cashflowkujava.database.DatabaseHelper;
import com.example.cashflowkujava.database.DatabaseHelper.RecentTransaction;
import com.example.cashflowkujava.ui.SimpleBarChartView;
import com.example.cashflowkujava.utils.FormatUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvUserHeader;
    private TextView tvTotalProfit, tvTotalIncome, tvTotalExpenses, tvTotalModal, tvGrossProfit;
    private SimpleBarChartView barChart;
    private RecyclerView rvRecent;
    private TextView tvEmptyTransactions;

    private DatabaseHelper dbHelper;
    private SharedPreferences bizPrefs;

    private static final String PREF_NAME = "BusinessPrefs";
    private static final String KEY_BIZ_NAME = "biz_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        bizPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Bind Views
        tvUserHeader = findViewById(R.id.tv_user_name_header);
        tvTotalProfit = findViewById(R.id.tv_total_profit);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalModal = findViewById(R.id.tv_total_modal);
        tvGrossProfit = findViewById(R.id.tv_gross_profit);
        tvTotalExpenses = findViewById(R.id.tv_total_expenses);
        barChart = findViewById(R.id.bar_chart);
        rvRecent = findViewById(R.id.rv_recent_transactions);
        tvEmptyTransactions = findViewById(R.id.tv_empty_transactions);

        rvRecent.setLayoutManager(new LinearLayoutManager(this));

        // Set Menu actions
        findViewById(R.id.menu_sales).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SalesActivity.class)));
        findViewById(R.id.menu_expenses).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ExpenseActivity.class)));
        findViewById(R.id.menu_reports).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ReportActivity.class)));
        findViewById(R.id.menu_backup).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BackupActivity.class)));
        findViewById(R.id.menu_inventory).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
        findViewById(R.id.menu_profile).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        
        findViewById(R.id.tv_btn_all_transactions).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ReportActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Sync settings from database to SharedPreferences first
        try {
            dbHelper.syncSettingsFromDatabase(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Reload data on resume so changes reflect immediately
        loadUserData();
        loadFinancialSummary();
        loadRecentTransactions();
        loadChartData();
    }

    private void loadUserData() {
        String name = bizPrefs.getString(KEY_BIZ_NAME, "Toko Saya");
        tvUserHeader.setText(name);
    }
    private void loadFinancialSummary() {
        String today = FormatUtil.getTodayJustDateString();
        double income = dbHelper.getTotalSalesForDay(today);
        double modal = dbHelper.getTotalModalForDay(today);
        double expenses = dbHelper.getTotalExpensesForDay(today);
        double profit = (income - modal) - expenses;
        double grossProfit = income - modal;

        tvTotalIncome.setText(FormatUtil.formatRupiah(income));
        tvTotalModal.setText(FormatUtil.formatRupiah(modal));
        tvGrossProfit.setText(FormatUtil.formatRupiah(grossProfit));
        tvTotalExpenses.setText(FormatUtil.formatRupiah(expenses));
        
        if (profit >= 0) {
            tvTotalProfit.setText(FormatUtil.formatRupiah(profit));
            tvTotalProfit.setTextColor(getResources().getColor(R.color.textPrimary));
        } else {
            tvTotalProfit.setText("-" + FormatUtil.formatRupiah(Math.abs(profit)));
            tvTotalProfit.setTextColor(getResources().getColor(R.color.colorExpense));
        }
    }

    private void loadRecentTransactions() {
        String today = FormatUtil.getTodayJustDateString();
        List<RecentTransaction> list = dbHelper.getRecentTransactions(today);
        if (list.isEmpty()) {
            rvRecent.setVisibility(View.GONE);
            tvEmptyTransactions.setVisibility(View.VISIBLE);
        } else {
            rvRecent.setVisibility(View.VISIBLE);
            tvEmptyTransactions.setVisibility(View.GONE);
            TransactionAdapter adapter = new TransactionAdapter(list);
            rvRecent.setAdapter(adapter);
        }
    }

    private void loadChartData() {
        // Prepare arrays for last 7 days
        double[] sales = new double[7];
        double[] expenses = new double[7];
        String[] labels = new String[7];

        SimpleDateFormat labelSdf = new SimpleDateFormat("EEE", new Locale("id", "ID"));
        SimpleDateFormat dbSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Calendar cal = Calendar.getInstance();
        // Set cal to 6 days ago
        cal.add(Calendar.DAY_OF_YEAR, -6);

        for (int i = 0; i < 7; i++) {
            Date date = cal.getTime();
            String dbDateStr = dbSdf.format(date);

            sales[i] = dbHelper.getTotalSalesForDay(dbDateStr);
            expenses[i] = dbHelper.getTotalExpensesForDay(dbDateStr);
            labels[i] = labelSdf.format(date);

            // Move to next day
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        barChart.setData(sales, expenses, labels);
    }
}