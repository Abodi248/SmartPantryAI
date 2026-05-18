package com.example.smartpantry.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpantry.databinding.ItemChatAssistantBinding;
import com.example.smartpantry.databinding.ItemChatUserBinding;
import com.example.smartpantry.model.ChatMessage;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.utils.RecipeExtractor;

public class ChatAdapter extends ListAdapter<ChatMessage, RecyclerView.ViewHolder> {

    public interface OnSaveRecipeListener {
        void onSaveRecipe(Recipe recipe);
    }

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

    @Nullable private final OnSaveRecipeListener saveRecipeListener;

    public ChatAdapter(@Nullable OnSaveRecipeListener saveRecipeListener) {
        super(DIFF_CB);
        this.saveRecipeListener = saveRecipeListener;
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
            ((AssistantViewHolder) holder).bind(msg, saveRecipeListener);
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

        void bind(ChatMessage msg, @Nullable OnSaveRecipeListener saveRecipeListener) {
            binding.tvMessage.setText(msg.getText());

            if (saveRecipeListener != null && RecipeExtractor.isLikelyRecipe(msg.getText())) {
                binding.chipSaveRecipe.setVisibility(View.VISIBLE);
                binding.chipSaveRecipe.setOnClickListener(v -> {
                    Recipe recipe = RecipeExtractor.extract(msg.getText());
                    if (recipe != null) {
                        saveRecipeListener.onSaveRecipe(recipe);
                        binding.chipSaveRecipe.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(v.getContext(),
                                "Could not parse recipe — try again", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                binding.chipSaveRecipe.setVisibility(View.GONE);
                binding.chipSaveRecipe.setOnClickListener(null);
            }
        }
    }
}
