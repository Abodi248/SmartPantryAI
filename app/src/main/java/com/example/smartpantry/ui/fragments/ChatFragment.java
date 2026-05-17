package com.example.smartpantry.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.smartpantry.R;
import com.example.smartpantry.databinding.FragmentChatBinding;
import com.example.smartpantry.ui.adapters.ChatAdapter;
import com.example.smartpantry.viewmodel.ChatViewModel;
import com.google.android.material.snackbar.Snackbar;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private ChatViewModel viewModel;
    private ChatAdapter adapter;

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

        adapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        binding.chatRecycler.setLayoutManager(layoutManager);
        binding.chatRecycler.setAdapter(adapter);

        viewModel.getBackendLabel().observe(getViewLifecycleOwner(),
                label -> binding.tvBackendBadge.setText(label));

        if (!viewModel.isAiAvailable()) {
            // Persistent unavailability state — lock the whole chat UI
            binding.emptyMessage.setText(R.string.ai_unavailable_chat);
            binding.emptyMessage.setVisibility(View.VISIBLE);
            binding.chatRecycler.setVisibility(View.GONE);
            binding.inputLayout.setEnabled(false);
            binding.etMessage.setEnabled(false);
            binding.btnSend.setEnabled(false);
            return;
        }

        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            boolean empty = messages == null || messages.isEmpty();
            binding.emptyMessage.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.chatRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
            if (!empty) {
                adapter.submitList(messages);
                binding.chatRecycler.smoothScrollToPosition(messages.size() - 1);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnSend.setEnabled(!loading);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()
                    && !error.equals("on_device_initializing")
                    && !error.equals("on_device_unavailable")) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });

        binding.btnSend.setOnClickListener(v -> dispatchMessage());

        binding.etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                dispatchMessage();
                return true;
            }
            return false;
        });
    }

    private void dispatchMessage() {
        String text = binding.etMessage.getText() != null
                ? binding.etMessage.getText().toString().trim() : "";
        if (!text.isEmpty()) {
            viewModel.sendMessage(text);
            binding.etMessage.setText("");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
