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
import com.example.cashflowkujava.database.DatabaseHelper.RecentTransaction;
import com.example.cashflowkujava.utils.FormatUtil;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final List<RecentTransaction> transactionsList;

    public TransactionAdapter(List<RecentTransaction> list) {
        this.transactionsList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentTransaction item = transactionsList.get(position);

        holder.tvDesc.setText(item.description);
        holder.tvDate.setText(FormatUtil.formatDateToIndonesian(item.date));

        if ("Penjualan".equalsIgnoreCase(item.type)) {
            holder.tvIcon.setText("💰");
            holder.tvIcon.setBackgroundResource(android.R.drawable.toast_frame);
            holder.tvIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0x1010B981)); // 10% emerald
            
            holder.tvAmount.setText("+" + FormatUtil.formatRupiah(item.amount));
            holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorIncome));
            holder.tvType.setText("Penjualan");
        } else {
            holder.tvIcon.setText("📉");
            holder.tvIcon.setBackgroundResource(android.R.drawable.toast_frame);
            holder.tvIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0x10EF4444)); // 10% red
            
            holder.tvAmount.setText("-" + FormatUtil.formatRupiah(item.amount));
            holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorExpense));
            holder.tvType.setText("Pengeluaran");
        }

        // Bind image if photo path exists and item is a sale (no images for expenses)
        if (item.imagePath != null && !item.imagePath.isEmpty() && !"Pengeluaran".equalsIgnoreCase(item.type)) {
            try {
                if (item.imagePath.startsWith("mock/")) {
                    holder.ivProduct.setImageResource(android.R.drawable.ic_menu_gallery);
                    holder.ivProduct.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF4F46E5));
                } else {
                    holder.ivProduct.setImageURI(Uri.fromFile(new java.io.File(item.imagePath)));
                }
                holder.ivProduct.setVisibility(View.VISIBLE);
                holder.tvIcon.setVisibility(View.GONE);
            } catch (Exception e) {
                holder.ivProduct.setVisibility(View.GONE);
                holder.tvIcon.setVisibility(View.VISIBLE);
            }
        } else {
            holder.ivProduct.setVisibility(View.GONE);
            holder.tvIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return transactionsList.size();
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
