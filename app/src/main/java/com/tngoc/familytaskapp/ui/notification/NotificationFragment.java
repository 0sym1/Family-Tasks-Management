package com.tngoc.familytaskapp.ui.notification;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.tngoc.familytaskapp.MainActivity;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.adapter.NotificationAdapter;
import com.tngoc.familytaskapp.data.model.Notification;
import com.tngoc.familytaskapp.data.repository.NotificationRepository;

public class NotificationFragment extends Fragment {

    private NotificationViewModel notificationViewModel;
    private NotificationRepository notificationRepository;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        notificationRepository = new NotificationRepository();

        recyclerView = view.findViewById(R.id.recyclerViewNotifications);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnNotificationActionListener(new NotificationAdapter.OnNotificationActionListener() {
            @Override
            public void onAccept(Notification notification) {
                String uid = FirebaseAuth.getInstance().getUid();
                if (uid != null) {
                    notificationViewModel.acceptInvitation(uid, notification);
                    
                    // Sau khi chấp nhận, chuyển hướng vào workspace
                    Intent intent = new Intent(requireContext(), MainActivity.class);
                    intent.putExtra("OPEN_TASK_LIST", true);
                    intent.putExtra("workspaceId", notification.getWorkspaceId());
                    intent.putExtra("workspaceName", notification.getTitle()); // Hoặc lấy từ message nếu cần
                    startActivity(intent);
                }
            }

            @Override
            public void onDecline(Notification notification) {
                String uid = FirebaseAuth.getInstance().getUid();
                if (uid != null) {
                    notificationViewModel.declineInvitation(uid, notification.getNotificationId());
                    Toast.makeText(requireContext(), "Đã từ chối lời mời", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDelete(Notification notification) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xóa thông báo")
                        .setMessage("Bạn có chắc chắn muốn xóa thông báo này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            String uid = FirebaseAuth.getInstance().getUid();
                            if (uid != null) {
                                MutableLiveData<Boolean> success = new MutableLiveData<>();
                                success.observe(getViewLifecycleOwner(), s -> {
                                    if (Boolean.TRUE.equals(s)) {
                                        Toast.makeText(requireContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                notificationRepository.deleteNotification(uid, notification.getNotificationId(), success);
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        observeViewModel();
        loadData();
    }

    private void loadData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid != null) notificationViewModel.loadNotifications(uid);
    }

    private void observeViewModel() {
        notificationViewModel.notificationsLiveData.observe(getViewLifecycleOwner(), notifications -> {
            if (notifications != null && !notifications.isEmpty()) {
                adapter.setNotifications(notifications);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        notificationViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
        
        notificationViewModel.acceptSuccessLiveData.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Đã tham gia Workspace", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
