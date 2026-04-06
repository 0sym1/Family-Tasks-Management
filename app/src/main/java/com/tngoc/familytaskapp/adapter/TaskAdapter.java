package com.tngoc.familytaskapp.adapter;

import android.content.Context;
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
import com.tngoc.familytaskapp.utils.Constants;

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
            Context context = itemView.getContext();
            SimpleDateFormat dateOnlySdf = new SimpleDateFormat("EEE dd/MM", Locale.getDefault());
            tvTaskTitle.setText(task.getTitle());
            
            tvTaskTime.setVisibility(View.GONE);
            
            String timePart = task.getEndTime() != null ? task.getEndTime().replace(":", "h") : "12h00";

            if (task.isRepeat() || task.isRepeating()) {
                tvTaskDeadline.setVisibility(View.VISIBLE);
                String repeatInfo = context.getString(R.string.repeat_daily);
                if ("Weekly".equalsIgnoreCase(task.getRepeatType())) {
                    repeatInfo = context.getString(R.string.repeat_weekly);
                }
                tvTaskDeadline.setText(timePart + " " + repeatInfo);
            } else if (task.getEndDate() != null) {
                tvTaskDeadline.setVisibility(View.VISIBLE);
                String deadlineStr = timePart + " " + dateOnlySdf.format(task.getEndDate().toDate());
                tvTaskDeadline.setText(context.getString(R.string.task_deadline_label, deadlineStr));
            } else {
                tvTaskDeadline.setVisibility(View.VISIBLE);
                tvTaskDeadline.setText(context.getString(R.string.task_deadline_label, timePart));
            }

            ivRepeat.setVisibility((task.isRepeat() || task.isRepeating()) ? View.VISIBLE : View.GONE);

            String status = task.getStatus();
            if (Constants.TASK_STATUS_DONE.equalsIgnoreCase(status)) {
                tvTaskStatus.setText(context.getString(R.string.status_done));
                tvTaskStatus.setTextColor(Color.parseColor("#59D595"));
                tvTaskStatus.getBackground().setTint(Color.parseColor("#1B3022"));
            } else if (Constants.TASK_STATUS_IN_PROGRESS.equalsIgnoreCase(status) || "doing".equalsIgnoreCase(status) || Constants.TASK_STATUS_TODO.equalsIgnoreCase(status)) {
                // "CHƯA LÀM" (TODO) cũng chuyển thành "ĐANG LÀM" (DOING) theo yêu cầu
                tvTaskStatus.setText(context.getString(R.string.status_doing));
                tvTaskStatus.setTextColor(Color.parseColor("#5EB5F7"));
                tvTaskStatus.getBackground().setTint(Color.parseColor("#1A2B3D"));
            } else if ("review".equalsIgnoreCase(status) || Constants.TASK_STATUS_PENDING.equalsIgnoreCase(status)) {
                tvTaskStatus.setText(context.getString(R.string.status_review));
                tvTaskStatus.setTextColor(Color.parseColor("#FFD700"));
                tvTaskStatus.getBackground().setTint(Color.parseColor("#332D00"));
            } else if (Constants.TASK_STATUS_OVERDUE.equalsIgnoreCase(status)) {
                tvTaskStatus.setText(context.getString(R.string.status_overdue));
                tvTaskStatus.setTextColor(Color.parseColor("#FF5252"));
                tvTaskStatus.getBackground().setTint(Color.parseColor("#3D1A1A"));
            } else {
                tvTaskStatus.setText(context.getString(R.string.status_doing));
                tvTaskStatus.setTextColor(Color.parseColor("#5EB5F7"));
                tvTaskStatus.getBackground().setTint(Color.parseColor("#1A2B3D"));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onTaskClick(task);
            });

            ivMore.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenu().add(context.getString(R.string.btn_delete));
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().equals(context.getString(R.string.btn_delete))) {
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
