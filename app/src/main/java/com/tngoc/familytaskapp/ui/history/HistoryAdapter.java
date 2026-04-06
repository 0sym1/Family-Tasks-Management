package com.tngoc.familytaskapp.ui.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.Reward;
import com.tngoc.familytaskapp.data.model.TaskHistory;
import com.tngoc.familytaskapp.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<Object> itemList = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

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
        Object previousItem = position > 0 ? itemList.get(position - 1) : null;

        // Xử lý Header ngày tháng
        Date currentDate = getItemDate(item);
        if (currentDate != null) {
            String currentHeader = getHeaderDate(holder.itemView.getContext(), currentDate);
            Date previousDate = getItemDate(previousItem);
            String previousHeader = previousDate != null ? getHeaderDate(holder.itemView.getContext(), previousDate) : null;

            if (previousHeader == null || !currentHeader.equals(previousHeader)) {
                holder.tvHeaderDate.setVisibility(View.VISIBLE);
                holder.tvHeaderDate.setText(currentHeader);
            } else {
                holder.tvHeaderDate.setVisibility(View.GONE);
            }
        } else {
            holder.tvHeaderDate.setVisibility(View.GONE);
        }

        if (item instanceof TaskHistory) {
            bindHistory(holder, (TaskHistory) item);
        } else if (item instanceof Reward) {
            bindReward(holder, (Reward) item);
        }
    }

    private Date getItemDate(Object item) {
        if (item instanceof TaskHistory) {
            return ((TaskHistory) item).getCreatedAt() != null ? ((TaskHistory) item).getCreatedAt().toDate() : null;
        } else if (item instanceof Reward) {
            return ((Reward) item).getCreatedAt() != null ? ((Reward) item).getCreatedAt().toDate() : null;
        }
        return null;
    }

    private String getHeaderDate(Context context, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        Date today = cal.getTime();
        
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

        Calendar itemCal = Calendar.getInstance();
        itemCal.setTime(date);
        itemCal.set(Calendar.HOUR_OF_DAY, 0); itemCal.set(Calendar.MINUTE, 0); itemCal.set(Calendar.SECOND, 0); itemCal.set(Calendar.MILLISECOND, 0);
        Date itemDate = itemCal.getTime();

        if (itemDate.equals(today)) return context.getString(R.string.history_today);
        if (itemDate.equals(yesterday)) return context.getString(R.string.history_yesterday);
        return dateFormat.format(date);
    }

    private void bindHistory(HistoryViewHolder holder, TaskHistory history) {
        Context context = holder.itemView.getContext();
        String actionText = "";
        String action = history.getAction();
        
        if ("task_created".equals(action)) {
            actionText = context.getString(R.string.history_created_task);
        } else if ("task_overdue".equals(action)) {
            actionText = context.getString(R.string.history_overdue_task);
        } else if ("task_status_updated".equals(action)) {
            String newStatus = history.getNewValue();
            if (Constants.TASK_STATUS_DONE.equals(newStatus)) {
                actionText = context.getString(R.string.history_done_task);
            } else if (Constants.TASK_STATUS_PENDING.equals(newStatus)) {
                actionText = context.getString(R.string.history_pending_task);
            } else if (Constants.TASK_STATUS_IN_PROGRESS.equals(newStatus)) {
                actionText = context.getString(R.string.history_in_progress_task);
            } else {
                actionText = context.getString(R.string.history_updated_task);
            }
        } else {
            actionText = context.getString(R.string.history_updated_task);
        }

        holder.tvTitle.setText((history.getUserName() != null ? history.getUserName() : "") + " " + actionText);
        holder.tvReason.setText(context.getString(R.string.history_label_task, history.getTaskName()));
        
        if (history.getCreatedAt() != null) {
            holder.tvTime.setText(timeFormat.format(history.getCreatedAt().toDate()));
        }

        holder.dot.setBackgroundResource(R.drawable.bg_dot_blue);
    }

    private void bindReward(HistoryViewHolder holder, Reward reward) {
        Context context = holder.itemView.getContext();
        String type = reward.getType();
        String actionText = "";

        if ("task_completed".equals(type)) {
            actionText = context.getString(R.string.history_done_task_reward);
        } else if ("task_overdue".equals(type)) {
            actionText = context.getString(R.string.history_overdue_task_reward);
        } else if ("task_completed_late".equals(type)) {
            actionText = context.getString(R.string.history_done_task_late_reward);
        } else {
            actionText = reward.getNote() != null ? reward.getNote() : "";
        }

        holder.tvTitle.setText((reward.getUserName() != null ? reward.getUserName() : "") + " " + actionText);
        
        String taskInfo = context.getString(R.string.history_label_task, reward.getTaskName() != null ? reward.getTaskName() : "---");
        String pointsVal = (reward.getPoints() > 0 ? "+" : "") + reward.getPoints() + " " + context.getString(R.string.tab_point).toLowerCase();
        String variationInfo = context.getString(R.string.history_label_variation, pointsVal);
        
        holder.tvReason.setText(taskInfo + " | " + variationInfo);

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
        TextView tvHeaderDate;
        View dot;
        TextView tvTitle, tvReason, tvTime;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderDate = itemView.findViewById(R.id.tvHeaderDate);
            dot = itemView.findViewById(R.id.dot);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
