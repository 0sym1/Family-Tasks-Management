package com.tngoc.familytaskapp.ui.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.ChatHistory;

import java.util.ArrayList;
import java.util.List;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.HistoryViewHolder> {

    private List<ChatHistory> histories = new ArrayList<>();
    private OnHistoryClickListener listener;

    public interface OnHistoryClickListener {
        void onHistoryClick(ChatHistory history);
    }

    public void setOnHistoryClickListener(OnHistoryClickListener listener) {
        this.listener = listener;
    }

    public void setHistories(List<ChatHistory> histories) {
        this.histories = histories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ChatHistory history = histories.get(position);
        holder.tvHistoryHeader.setText(history.getHeader());
        
        // Hide divider for the last item
        if (position == getItemCount() - 1) {
            holder.vDivider.setVisibility(View.GONE);
        } else {
            holder.vDivider.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onHistoryClick(history);
        });
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryHeader;
        View vDivider;
        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryHeader = itemView.findViewById(R.id.tvHistoryHeader);
            vDivider = itemView.findViewById(R.id.vDivider);
        }
    }
}
