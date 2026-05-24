package com.example.cashflowkujava.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashflowkujava.R;
import com.example.cashflowkujava.models.BackupLog;

import java.util.List;

public class BackupAdapter extends RecyclerView.Adapter<BackupAdapter.ViewHolder> {

    private final List<BackupLog> logsList;

    public BackupAdapter(List<BackupLog> list) {
        this.logsList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_backup_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BackupLog item = logsList.get(position);

        holder.tvFilename.setText(item.getFilename());
        holder.tvDate.setText(item.getDate());
        
        // Format size
        long bytes = item.getSize();
        String sizeStr;
        if (bytes >= 1024 * 1024) {
            sizeStr = String.format(java.util.Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
        } else if (bytes >= 1024) {
            sizeStr = String.format(java.util.Locale.US, "%.1f KB", bytes / 1024.0);
        } else {
            sizeStr = bytes + " B";
        }
        holder.tvSize.setText(sizeStr);

    }

    @Override
    public int getItemCount() {
        return logsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFilename, tvDate, tvSize;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFilename = itemView.findViewById(R.id.tv_log_filename);
            tvDate = itemView.findViewById(R.id.tv_log_date);
            tvSize = itemView.findViewById(R.id.tv_log_size);
        }
    }
}
