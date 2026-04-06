package com.tngoc.familytaskapp.ui.task;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH'h' EEE dd/MM", new Locale("vi", "VN"));
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
        
        holder.tvTaskTitle.setText(task.getTitle());
        
        if (task.getStartDate() != null) {
            holder.tvTaskTime.setText(timeFormat.format(task.getStartDate().toDate()));
            holder.tvTaskTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvTaskTime.setVisibility(View.GONE);
        }

        if (task.getEndDate() != null) {
            holder.tvDeadline.setVisibility(View.VISIBLE);
            holder.tvDeadline.setText("Hạn: " + timeFormat.format(task.getEndDate().toDate()));
        } else {
            holder.tvDeadline.setVisibility(View.GONE);
        }

        String ownerName = task.getOwnerName();
        if (ownerName != null && !ownerName.isEmpty() && !ownerName.equals("Người thân")) {
            holder.tvAssignerName.setText("Giao bởi " + ownerName);
        } else {
            holder.tvAssignerName.setText("Giao bởi người thân");
        }

        if (Constants.TASK_STATUS_DONE.equals(task.getStatus())) {
            holder.tvStatusLabel.setText("XONG");
            holder.tvStatusLabel.setTextColor(0xFF59D595);
        } else if (Constants.TASK_STATUS_PENDING.equals(task.getStatus())) {
            holder.tvStatusLabel.setText("CHỜ DUYỆT");
            holder.tvStatusLabel.setTextColor(0xFFEBB059);
        } else {
            holder.tvStatusLabel.setText("ĐANG LÀM");
            holder.tvStatusLabel.setTextColor(0xFF5995D5);
        }

        // Đảm bảo click vào toàn bộ item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class MyTaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTime, tvDeadline, tvAssignerName, tvStatusLabel, tvTaskTitle;

        public MyTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTime = itemView.findViewById(R.id.tvTaskTime);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            tvAssignerName = itemView.findViewById(R.id.tvAssignerName);
            tvStatusLabel = itemView.findViewById(R.id.tvStatusLabel);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            
            // Thêm feedback khi nhấn
            itemView.setClickable(true);
            itemView.setFocusable(true);
        }
    }
}
