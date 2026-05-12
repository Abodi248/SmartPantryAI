package com.example.smartpantry.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpantry.databinding.ItemChatAssistantBinding;
import com.example.smartpantry.databinding.ItemChatUserBinding;
import com.example.smartpantry.model.ChatMessage;

public class ChatAdapter extends ListAdapter<ChatMessage, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_ASSISTANT = 1;

    private static final DiffUtil.ItemCallback<ChatMessage> DIFF_CB =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatMessage a, @NonNull ChatMessage b) {
                    return a.getTimestampMs() == b.getTimestampMs() && a.getRole() == b.getRole();
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatMessage a, @NonNull ChatMessage b) {
                    return a.getText().equals(b.getText());
                }
            };

    public ChatAdapter() {
        super(DIFF_CB);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_ASSISTANT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            return new UserViewHolder(ItemChatUserBinding.inflate(inflater, parent, false));
        }
        return new AssistantViewHolder(ItemChatAssistantBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = getItem(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(msg);
        } else {
            ((AssistantViewHolder) holder).bind(msg);
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatUserBinding binding;

        UserViewHolder(ItemChatUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatMessage msg) {
            binding.tvMessage.setText(msg.getText());
        }
    }

    static class AssistantViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatAssistantBinding binding;

        AssistantViewHolder(ItemChatAssistantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatMessage msg) {
            binding.tvMessage.setText(msg.getText());
        }
    }
}
