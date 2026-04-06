package com.tngoc.familytaskapp.ui.task;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyTaskAdapter extends RecyclerView.Adapter<MyTaskAdapter.MyTaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(Task task, View view);
    }

    private List<Task> tasks = new ArrayList<>();
    private OnTaskClickListener listener;

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_task, parent, false);
        return new MyTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyTaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        Locale currentLocale = Locale.getDefault();
        SimpleDateFormat deadlineFormat = new SimpleDateFormat("dd/MM", currentLocale);
        String deadlinePrefix = holder.itemView.getContext().getString(R.string.task_deadline_prefix);
        
        holder.tvTaskTitle.setText(task.getTitle());
        
        String timePart = task.getEndTime() != null ? task.getEndTime() : "12:00";

        if (task.isRepeating()) {
            String repeatLabel = holder.itemView.getContext().getString(R.string.repeat_daily);
            if ("weekly".equalsIgnoreCase(task.getRepeatType())) {
                repeatLabel = holder.itemView.getContext().getString(R.string.repeat_weekly);
            }
            holder.tvTaskTime.setText(timePart + " " + repeatLabel);
            holder.ivRepeatStatus.setVisibility(View.VISIBLE);
        } else {
            if (task.getEndDate() != null) {
                holder.tvTaskTime.setText(deadlinePrefix + " " + timePart + " " + deadlineFormat.format(task.getEndDate().toDate()));
            } else {
                holder.tvTaskTime.setText(deadlinePrefix + " " + timePart);
            }
            holder.ivRepeatStatus.setVisibility(View.GONE);
        }

        if (Constants.TASK_STATUS_DONE.equals(task.getStatus())) {
            holder.tvStatusLabel.setText(R.string.status_done);
            holder.tvStatusLabel.setTextColor(0xFF59D595);
        } else if (Constants.TASK_STATUS_PENDING.equals(task.getStatus())) {
            holder.tvStatusLabel.setText(R.string.status_pending);
            holder.tvStatusLabel.setTextColor(0xFFEBB059);
        } else {
            holder.tvStatusLabel.setText(R.string.status_doing);
            holder.tvStatusLabel.setTextColor(0xFF5995D5);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task, v);
            }
        });

        holder.ivMore.setOnClickListener(v -> {
            // Show menu if needed
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class MyTaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTime, tvStatusLabel, tvTaskTitle;
        ImageView ivMore, ivRepeatStatus;

        public MyTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTime = itemView.findViewById(R.id.tvTaskTime);
            tvStatusLabel = itemView.findViewById(R.id.tvStatusLabel);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            ivMore = itemView.findViewById(R.id.ivMore);
            ivRepeatStatus = itemView.findViewById(R.id.ivRepeatStatus);
            
            itemView.setClickable(true);
            itemView.setFocusable(true);
        }
    }
}
