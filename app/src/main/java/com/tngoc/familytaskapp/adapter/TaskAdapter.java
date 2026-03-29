package com.tngoc.familytaskapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
    private OnTaskMoreActionsListener moreActionsListener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public interface OnTaskMoreActionsListener {
        void onDeleteTask(Task task);
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void setOnTaskMoreActionsListener(OnTaskMoreActionsListener moreActionsListener) {
        this.moreActionsListener = moreActionsListener;
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
        holder.bind(task, listener, moreActionsListener);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTime, tvTaskDeadline, tvTaskStatus, tvTaskTitle;
        ImageView ivRepeat, ivMore;
        SimpleDateFormat sdf = new SimpleDateFormat("HH'h' EEE dd/MM", new Locale("vi", "VN"));

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTime = itemView.findViewById(R.id.tvTaskTime);
            tvTaskDeadline = itemView.findViewById(R.id.tvTaskDeadline);
            tvTaskStatus = itemView.findViewById(R.id.tvTaskStatus);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            ivRepeat = itemView.findViewById(R.id.ivRepeat);
            ivMore = itemView.findViewById(R.id.ivMore);
        }

        public void bind(Task task, OnTaskClickListener listener, OnTaskMoreActionsListener moreListener) {
            tvTaskTitle.setText(task.getTitle());
            
            if (task.getCreatedAt() != null) {
                tvTaskTime.setText(sdf.format(task.getCreatedAt().toDate()));
            }
            
            if (task.getEndDate() != null) {
                tvTaskDeadline.setText("Hạn: " + sdf.format(task.getEndDate().toDate()));
            }

            ivRepeat.setVisibility(task.isRepeat() ? View.VISIBLE : View.GONE);

            String status = task.getStatus();
            if ("done".equalsIgnoreCase(status)) {
                tvTaskStatus.setText("XONG");
                tvTaskStatus.setTextColor(Color.parseColor("#59D595"));
                tvTaskStatus.getBackground().setTint(Color.parseColor("#1B3022"));
            } else if ("doing".equalsIgnoreCase(status) || "in_progress".equalsIgnoreCase(status)) {
                tvTaskStatus.setText("ĐANG LÀM");
                tvTaskStatus.setTextColor(Color.parseColor("#5EB5F7"));
                tvTaskStatus.getBackground().setTint(Color.parseColor("#1A2B3D"));
            } else if ("todo".equalsIgnoreCase(status)) {
                tvTaskStatus.setText("CHƯA LÀM");
                tvTaskStatus.setTextColor(Color.parseColor("#888888"));
                tvTaskStatus.getBackground().setTint(Color.parseColor("#333333"));
            } else if ("review".equalsIgnoreCase(status)) {
                tvTaskStatus.setText("KIỂM TRA");
                tvTaskStatus.setTextColor(Color.parseColor("#FFD700"));
                tvTaskStatus.getBackground().setTint(Color.parseColor("#332D00"));
            } else {
                tvTaskStatus.setText("TODO");
                tvTaskStatus.setTextColor(Color.WHITE);
                tvTaskStatus.getBackground().setTint(Color.parseColor("#1E1E1E"));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onTaskClick(task);
            });

            ivMore.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenu().add("Xóa");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().equals("Xóa")) {
                        if (moreListener != null) moreListener.onDeleteTask(task);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }
}
