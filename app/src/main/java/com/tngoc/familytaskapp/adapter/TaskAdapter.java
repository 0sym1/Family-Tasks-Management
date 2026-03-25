package com.tngoc.familytaskapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList = new ArrayList<>();
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void setTaskList(List<Task> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task, listener);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTime, tvTaskDeadline, tvTaskStatus, tvTaskTitle;
        SimpleDateFormat sdf = new SimpleDateFormat("HH'h' EEE dd/MM", new Locale("vi", "VN"));

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTime = itemView.findViewById(R.id.tvTaskTime);
            tvTaskDeadline = itemView.findViewById(R.id.tvTaskDeadline);
            tvTaskStatus = itemView.findViewById(R.id.tvTaskStatus);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
        }

        public void bind(Task task, OnTaskClickListener listener) {
            tvTaskTitle.setText(task.getTitle());
            
            if (task.getCreatedAt() != null) {
                tvTaskTime.setText(sdf.format(task.getCreatedAt().toDate()));
            }
            
            if (task.getDueDate() != null) {
                tvTaskDeadline.setText("Hạn: " + sdf.format(task.getDueDate().toDate()));
            }

            String status = task.getStatus();
            if ("done".equalsIgnoreCase(status)) {
                tvTaskStatus.setText("XONG");
                tvTaskStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_light));
            } else if ("in_progress".equalsIgnoreCase(status)) {
                tvTaskStatus.setText("ĐANG LÀM");
                tvTaskStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_blue_light));
            } else {
                tvTaskStatus.setText("TODO");
                tvTaskStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onTaskClick(task);
            });
        }
    }
}
