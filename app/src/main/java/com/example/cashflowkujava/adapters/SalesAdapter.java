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
import com.example.cashflowkujava.models.Sale;
import com.example.cashflowkujava.utils.FormatUtil;

import java.util.List;

public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.ViewHolder> {

    public interface OnSaleClickListener {
        void onSaleClick(Sale sale);
        void onSaleLongClick(Sale sale);
    }

    private final List<Sale> salesList;
    private final OnSaleClickListener listener;

    public SalesAdapter(List<Sale> list, OnSaleClickListener listener) {
        this.salesList = list;
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
        Sale item = salesList.get(position);

        holder.tvDesc.setText(item.getProductName() + " (x" + item.getQty() + ")");
        holder.tvDate.setText(FormatUtil.formatDateToIndonesian(item.getDate()));
        holder.tvAmount.setText("+" + FormatUtil.formatRupiah(item.getSubtotal()));
        holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorIncome));
        
        String method = item.getPaymentMethod();
        if (item.getNotes() != null && !item.getNotes().trim().isEmpty()) {
            method += " - " + item.getNotes();
        }
        holder.tvType.setText(method);
        
        holder.tvIcon.setText("💰");
        holder.tvIcon.setBackgroundResource(android.R.drawable.toast_frame);
        holder.tvIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0x1010B981)); // 10% emerald

        // Bind image if photo path exists
        if (item.getProductImagePath() != null && !item.getProductImagePath().isEmpty()) {
            try {
                if (item.getProductImagePath().startsWith("mock/")) {
                    holder.ivProduct.setImageResource(android.R.drawable.ic_menu_gallery);
                    holder.ivProduct.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF4F46E5));
                } else {
                    holder.ivProduct.setImageURI(Uri.fromFile(new java.io.File(item.getProductImagePath())));
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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSaleClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onSaleLongClick(item);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return salesList.size();
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
