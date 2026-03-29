package com.tngoc.familytaskapp.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.Reward;
import com.tngoc.familytaskapp.data.model.TaskHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<Object> itemList = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public void setData(List<Object> items) {
        this.itemList = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Object item = itemList.get(position);

        if (item instanceof TaskHistory) {
            bindHistory(holder, (TaskHistory) item);
        } else if (item instanceof Reward) {
            bindReward(holder, (Reward) item);
        }
    }

    private void bindHistory(HistoryViewHolder holder, TaskHistory history) {
        String title = history.getUserName() + " đã cập nhật task " + history.getTaskName();
        holder.tvTitle.setText(title);
        
        String detail = history.getOldValue() + " → " + history.getNewValue();
        if (history.getPoint() != 0) {
            detail += " (" + (history.getPoint() > 0 ? "+" : "") + history.getPoint() + " điểm)";
        }
        holder.tvReason.setText(detail);

        if (history.getCreatedAt() != null) {
            holder.tvTime.setText(timeFormat.format(history.getCreatedAt().toDate()));
        }

        holder.dot.setBackgroundResource(R.drawable.bg_dot_blue);
    }

    private void bindReward(HistoryViewHolder holder, Reward reward) {
        holder.tvTitle.setText(reward.getNote());
        holder.tvReason.setText("Loại: " + reward.getType() + " (" + (reward.getPoints() > 0 ? "+" : "") + reward.getPoints() + " điểm)");

        if (reward.getCreatedAt() != null) {
            holder.tvTime.setText(timeFormat.format(reward.getCreatedAt().toDate()));
        }

        if (reward.getPoints() > 0) {
            holder.dot.setBackgroundResource(R.drawable.bg_dot_green);
        } else if (reward.getPoints() < 0) {
            holder.dot.setBackgroundResource(R.drawable.bg_dot_red);
        } else {
            holder.dot.setBackgroundResource(R.drawable.bg_dot_blue);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        View dot;
        TextView tvTitle, tvReason, tvTime;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            dot = itemView.findViewById(R.id.dot);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
