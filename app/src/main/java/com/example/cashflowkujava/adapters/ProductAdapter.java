package com.example.cashflowkujava.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashflowkujava.R;
import com.example.cashflowkujava.models.Product;
import com.example.cashflowkujava.utils.FormatUtil;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onProductLongClick(Product product);
        void onProductEdit(Product product);
        void onProductRestock(Product product);
    }

    private final List<Product> productsList;
    private final OnProductClickListener listener;

    public ProductAdapter(List<Product> list, OnProductClickListener listener) {
        this.productsList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product item = productsList.get(position);

        holder.tvName.setText(item.getName());
        holder.tvCategory.setText("Kategori: " + item.getCategory());
        holder.tvPrice.setText(FormatUtil.formatRupiah(item.getPrice()));

        // Color stock red when 0
        String stockText = "Stok: " + item.getStock();
        holder.tvStock.setText(stockText);
        if (item.getStock() <= 0) {
            holder.tvStock.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorExpense));
        } else {
            holder.tvStock.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.textSecondary));
        }

        // Simple initials for category bullet
        String initial = item.getCategory() != null && !item.getCategory().isEmpty() ?
                item.getCategory().substring(0, 1).toUpperCase() : "P";
        holder.tvBadge.setText(initial);

        // Bind image if photo path exists
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                if (item.getImagePath().startsWith("mock/")) {
                    holder.ivProduct.setImageResource(android.R.drawable.ic_menu_gallery);
                    holder.ivProduct.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF4F46E5));
                } else {
                    holder.ivProduct.setImageURI(Uri.fromFile(new java.io.File(item.getImagePath())));
                }
                holder.ivProduct.setVisibility(View.VISIBLE);
                holder.tvBadge.setVisibility(View.GONE);
            } catch (Exception e) {
                holder.ivProduct.setVisibility(View.GONE);
                holder.tvBadge.setVisibility(View.VISIBLE);
            }
        } else {
            holder.ivProduct.setVisibility(View.GONE);
            holder.tvBadge.setVisibility(View.VISIBLE);
        }

        // Row click → detail / edit dialog (legacy)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(item);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onProductLongClick(item);
            return true;
        });

        // Restock button
        holder.btnRestock.setOnClickListener(v -> {
            if (listener != null) listener.onProductRestock(item);
        });
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBadge, tvName, tvCategory, tvPrice, tvStock;
        ImageView ivProduct;
        Button btnRestock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBadge = itemView.findViewById(R.id.tv_product_badge);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvCategory = itemView.findViewById(R.id.tv_product_category);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            tvStock = itemView.findViewById(R.id.tv_product_stock);
            ivProduct = itemView.findViewById(R.id.iv_product_image);
            btnRestock = itemView.findViewById(R.id.btn_product_restock);
        }
    }
}
