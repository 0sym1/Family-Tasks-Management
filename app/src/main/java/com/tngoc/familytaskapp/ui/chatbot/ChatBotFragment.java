package com.tngoc.familytaskapp.ui.chatbot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.tngoc.familytaskapp.R;

public class ChatBotFragment extends Fragment {

    private ChatBotViewModel chatBotViewModel;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageButton btnSend;
    private ProgressBar progressBar;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chatbot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatBotViewModel = new ViewModelProvider(this).get(ChatBotViewModel.class);

        userId      = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        recyclerView = view.findViewById(R.id.recyclerViewMessages);
        etMessage    = view.findViewById(R.id.etMessage);
        btnSend      = view.findViewById(R.id.btnSend);
        progressBar  = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        btnSend.setOnClickListener(v -> {
            String content = etMessage.getText().toString().trim();
            if (content.isEmpty()) return;
            etMessage.setText("");
            if (userId != null) chatBotViewModel.sendMessage(userId, content);
        });

        observeViewModel();
        if (userId != null) chatBotViewModel.loadHistory(userId);
    }

    private void observeViewModel() {
        chatBotViewModel.messagesLiveData.observe(getViewLifecycleOwner(), messages -> {
            // TODO: set adapter data, scroll to bottom
        });

        chatBotViewModel.loadingLiveData.observe(getViewLifecycleOwner(), isLoading ->
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE)
        );

        chatBotViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }
}

