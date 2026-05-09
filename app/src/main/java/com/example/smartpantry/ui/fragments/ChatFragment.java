package com.example.smartpantry.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.smartpantry.databinding.FragmentChatBinding;
import com.example.smartpantry.viewmodel.ChatViewModel;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private ChatViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        binding.btnSend.setOnClickListener(v -> {
            String message = binding.etMessage.getText() != null
                    ? binding.etMessage.getText().toString().trim() : "";
            if (!message.isEmpty()) {
                // do later: viewModel.sendMessage(message)
                binding.etMessage.setText("");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}