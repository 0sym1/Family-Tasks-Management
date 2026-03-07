package com.tngoc.familytaskapp.ui.workspace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.tngoc.familytaskapp.R;

public class InviteMemberFragment extends Fragment {

    private EditText etInviteEmail;
    private Button btnSendInvite;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invite_member, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etInviteEmail = view.findViewById(R.id.etInviteEmail);
        btnSendInvite = view.findViewById(R.id.btnSendInvite);

        btnSendInvite.setOnClickListener(v -> {
            String email = etInviteEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: gửi invitation
            Toast.makeText(requireContext(), getString(R.string.invitation_sent), Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigateUp();
        });
    }
}

