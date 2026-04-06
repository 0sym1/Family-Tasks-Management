package com.tngoc.familytaskapp.ui.chatbot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.utils.Constants;

public class ChatBotFragment extends Fragment {

    private ChatBotViewModel chatBotViewModel;
    private RecyclerView recyclerViewMessages;
    private RecyclerView rvChatHistory;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnToggleHistory;
    private TextView tvChatbotTitle;
    private TextView tvEmptyHistory;
    private ProgressBar progressBar;
    private String userId;
    
    private ChatMessageAdapter messageAdapter;
    private ChatHistoryAdapter historyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chatbot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatBotViewModel = new ViewModelProvider(requireActivity()).get(ChatBotViewModel.class);
        chatBotViewModel.initModel(Constants.GEMINI_API_KEY);

        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        rvChatHistory        = view.findViewById(R.id.rvChatHistory);
        etMessage            = view.findViewById(R.id.etMessage);
        btnSend              = view.findViewById(R.id.btnSend);
        btnToggleHistory     = view.findViewById(R.id.btnToggleHistory);
        tvChatbotTitle       = view.findViewById(R.id.tvChatbotTitle);
        tvEmptyHistory       = view.findViewById(R.id.tvEmptyHistory);
        progressBar          = view.findViewById(R.id.progressBar);

        messageAdapter = new ChatMessageAdapter();
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewMessages.setAdapter(messageAdapter);

        historyAdapter = new ChatHistoryAdapter();
        rvChatHistory.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvChatHistory.setAdapter(historyAdapter);
        
        historyAdapter.setOnHistoryClickListener(history -> {
            if (userId != null) {
                chatBotViewModel.selectHistory(userId, history.getHistoryId());
            }
        });

        tvChatbotTitle.setOnClickListener(v -> {
            chatBotViewModel.resetChat();
            Toast.makeText(requireContext(), "Bắt đầu đoạn chat mới", Toast.LENGTH_SHORT).show();
        });

        btnSend.setOnClickListener(v -> {
            String content = etMessage.getText().toString().trim();
            if (content.isEmpty()) return;
            etMessage.setText("");
            if (userId != null) chatBotViewModel.sendMessage(userId, content);
        });

        btnToggleHistory.setOnClickListener(v -> {
            if (rvChatHistory.getVisibility() == View.VISIBLE || tvEmptyHistory.getVisibility() == View.VISIBLE) {
                rvChatHistory.setVisibility(View.GONE);
                tvEmptyHistory.setVisibility(View.GONE);
            } else {
                updateHistoryVisibility(chatBotViewModel.historiesLiveData.getValue());
            }
        });

        observeViewModel();
        
        if (userId != null) {
            chatBotViewModel.loadContextData(userId);
            chatBotViewModel.loadHistories(userId);
        }
    }

    private void observeViewModel() {
        chatBotViewModel.messagesLiveData.observe(getViewLifecycleOwner(), messages -> {
            if (messages != null) {
                messageAdapter.setMessages(messages);
                if (!messages.isEmpty()) {
                    recyclerViewMessages.smoothScrollToPosition(messages.size() - 1);
                }
            }
        });

        chatBotViewModel.historiesLiveData.observe(getViewLifecycleOwner(), histories -> {
            if (histories != null) {
                historyAdapter.setHistories(histories);
                updateHistoryVisibility(histories);
            }
        });

        chatBotViewModel.loadingLiveData.observe(getViewLifecycleOwner(), isLoading ->
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE)
        );

        chatBotViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateHistoryVisibility(java.util.List<?> histories) {
        // Chỉ cập nhật hiển thị nếu vùng chứa lịch sử đang được mở (không bị ẩn bởi nút toggle)
        if (histories == null || histories.isEmpty()) {
            rvChatHistory.setVisibility(View.GONE);
            tvEmptyHistory.setVisibility(View.VISIBLE);
        } else {
            rvChatHistory.setVisibility(View.VISIBLE);
            tvEmptyHistory.setVisibility(View.GONE);
        }
    }
}
