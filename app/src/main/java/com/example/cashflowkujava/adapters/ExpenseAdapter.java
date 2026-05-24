package com.example.cashflowkujava.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashflowkujava.R;
import com.example.cashflowkujava.models.Expense;
import com.example.cashflowkujava.utils.FormatUtil;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
        void onExpenseLongClick(Expense expense);
    }

    private final List<Expense> expensesList;
    private final OnExpenseClickListener listener;

    public ExpenseAdapter(List<Expense> list, OnExpenseClickListener listener) {
        this.expensesList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense item = expensesList.get(position);

        holder.tvDesc.setText(item.getCategory() + ": " + (item.getNotes() != null ? item.getNotes() : ""));
        holder.tvDate.setText(FormatUtil.formatDateToIndonesian(item.getDate()));
        holder.tvAmount.setText("-" + FormatUtil.formatRupiah(item.getAmount()));
        holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorExpense));
        
        holder.tvType.setText("Pengeluaran");

        holder.tvIcon.setText("📉");
        holder.tvIcon.setBackgroundResource(android.R.drawable.toast_frame);
        holder.tvIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0x10EF4444)); // 10% red

        // Expenses no longer support receipt photos
        holder.ivProduct.setVisibility(View.GONE);
        holder.tvIcon.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExpenseClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onExpenseLongClick(item);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return expensesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvDesc, tvDate, tvAmount, tvType;
        ImageView ivProduct;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tv_trans_icon);
            tvDesc = itemView.findViewById(R.id.tv_trans_desc);
            tvDate = itemView.findViewById(R.id.tv_trans_date);
            tvAmount = itemView.findViewById(R.id.tv_trans_amount);
            tvType = itemView.findViewById(R.id.tv_trans_type);
            ivProduct = itemView.findViewById(R.id.iv_trans_product_image);
        }
    }
}
