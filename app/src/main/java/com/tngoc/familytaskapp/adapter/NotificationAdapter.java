package com.tngoc.familytaskapp.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private OnNotificationActionListener listener;

    public interface OnNotificationActionListener {
        void onAccept(Notification notification);
        void onDecline(Notification notification);
        void onDelete(Notification notification);
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    public void setOnNotificationActionListener(OnNotificationActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        Notification previous = position > 0 ? notifications.get(position - 1) : null;
        holder.bind(notification, previous, listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeaderDate, tvNotificationMessage, tvNotificationTime, tvOptions;
        View vStatusIndicator;
        LinearLayout llActionButtons;
        Button btnAccept, btnDecline, btnDeleteNotification;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderDate = itemView.findViewById(R.id.tvHeaderDate);
            tvNotificationMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvNotificationTime = itemView.findViewById(R.id.tvNotificationTime);
            vStatusIndicator = itemView.findViewById(R.id.vStatusIndicator);
            llActionButtons = itemView.findViewById(R.id.llActionButtons);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
            tvOptions = itemView.findViewById(R.id.tvOptions);
            btnDeleteNotification = itemView.findViewById(R.id.btnDeleteNotification);
        }

        public void bind(Notification n, Notification previous, OnNotificationActionListener listener) {
            tvNotificationMessage.setText(n.getMessage());
            
            if (n.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                tvNotificationTime.setText(sdf.format(n.getCreatedAt().toDate()));
                
                String currentDateStr = getHeaderDate(n.getCreatedAt().toDate());
                if (previous == null || !currentDateStr.equals(getHeaderDate(previous.getCreatedAt().toDate()))) {
                    tvHeaderDate.setVisibility(View.VISIBLE);
                    tvHeaderDate.setText(currentDateStr);
                } else {
                    tvHeaderDate.setVisibility(View.GONE);
                }
            }

            int color = Color.parseColor("#FFCC00");
            String type = n.getType() != null ? n.getType() : "";
            
            switch (type) {
                case "invitation":
                case "invitation_accepted":
                case "task_done":
                    color = Color.parseColor("#2DD36F");
                    break;
                case "invitation_declined":
                    color = Color.parseColor("#FF3B30");
                    break;
            }
            
            GradientDrawable background = (GradientDrawable) vStatusIndicator.getBackground();
            background.setColor(color);

            tvOptions.setOnClickListener(v -> {
                btnDeleteNotification.setVisibility(btnDeleteNotification.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            });

            btnDeleteNotification.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(n);
                btnDeleteNotification.setVisibility(View.GONE);
            });

            if ("invitation".equals(type)) {
                llActionButtons.setVisibility(View.VISIBLE);
                btnAccept.setOnClickListener(v -> { if (listener != null) listener.onAccept(n); });
                btnDecline.setOnClickListener(v -> { if (listener != null) listener.onDecline(n); });
            } else {
                llActionButtons.setVisibility(View.GONE);
            }
        }

        private String getHeaderDate(Date date) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
            Date today = cal.getTime();
            cal.add(Calendar.DATE, -1);
            Date yesterday = cal.getTime();

            if (date.after(today)) return "Hôm nay";
            if (date.after(yesterday)) return "Hôm qua";
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
        }
    }
}
