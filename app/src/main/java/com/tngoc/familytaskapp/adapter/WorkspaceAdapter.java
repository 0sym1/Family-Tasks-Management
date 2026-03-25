package com.tngoc.familytaskapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public interface OnWorkspaceClickListener {
        void onWorkspaceClick(Workspace workspace);
    }

    public void setOnWorkspaceClickListener(OnWorkspaceClickListener listener) {
        this.listener = listener;
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
        holder.bind(workspace, listener);
    }

    @Override
    public int getItemCount() {
        return workspaces.size();
    }

    static class WorkspaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkspaceName, tvOwnerName, tvTaskCount;
        ProgressBar pbWorkspace;

        public WorkspaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkspaceName = itemView.findViewById(R.id.tvWorkspaceName);
            tvOwnerName = itemView.findViewById(R.id.tvOwnerName);
            tvTaskCount = itemView.findViewById(R.id.tvTaskCount);
            pbWorkspace = itemView.findViewById(R.id.pbWorkspace);
        }

        public void bind(Workspace workspace, OnWorkspaceClickListener listener) {
            tvWorkspaceName.setText(workspace.getName());
            // You might want to fetch owner name and task counts here or have them in the Workspace object
            // For now, using placeholder or data from object if available
            
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onWorkspaceClick(workspace);
            });
        }
    }
}
