package com.tngoc.familytaskapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.Workspace;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceAdapter extends RecyclerView.Adapter<WorkspaceAdapter.WorkspaceViewHolder> {

    private List<Workspace> workspaces = new ArrayList<>();
    private OnWorkspaceClickListener listener;
    private OnDeleteClickListener deleteListener;

    public interface OnWorkspaceClickListener {
        void onWorkspaceClick(Workspace workspace);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Workspace workspace);
    }

    public void setOnWorkspaceClickListener(OnWorkspaceClickListener listener) {
        this.listener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    public void setWorkspaces(List<Workspace> workspaces) {
        this.workspaces = workspaces;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WorkspaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workspace, parent, false);
        return new WorkspaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkspaceViewHolder holder, int position) {
        Workspace workspace = workspaces.get(position);
        holder.bind(workspace, listener, deleteListener);
    }

    @Override
    public int getItemCount() {
        return workspaces.size();
    }

    static class WorkspaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkspaceName, tvOwnerName, tvTaskCount, tvOptions;
        ProgressBar pbWorkspace;
        Button btnDelete;

        public WorkspaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkspaceName = itemView.findViewById(R.id.tvWorkspaceName);
            tvOwnerName = itemView.findViewById(R.id.tvOwnerName);
            tvTaskCount = itemView.findViewById(R.id.tvTaskCount);
            pbWorkspace = itemView.findViewById(R.id.pbWorkspace);
            tvOptions = itemView.findViewById(R.id.tvOptions);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Workspace workspace, OnWorkspaceClickListener listener, OnDeleteClickListener deleteListener) {
            tvWorkspaceName.setText(workspace.getName());
            tvOwnerName.setText("Tạo bởi: " + (workspace.getOwnerName() != null ? workspace.getOwnerName() : "Unknown"));
            
            int total = workspace.getTotalTasks();
            int completed = workspace.getCompletedTasks();
            tvTaskCount.setText(completed + "/" + total + " task");
            
            if (total > 0) {
                pbWorkspace.setProgress((completed * 100) / total);
            } else {
                pbWorkspace.setProgress(0);
            }

            tvOptions.setOnClickListener(v -> {
                if (btnDelete.getVisibility() == View.GONE) {
                    btnDelete.setVisibility(View.VISIBLE);
                } else {
                    btnDelete.setVisibility(View.GONE);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(workspace);
                }
                btnDelete.setVisibility(View.GONE);
            });
            
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onWorkspaceClick(workspace);
            });
        }
    }
}
