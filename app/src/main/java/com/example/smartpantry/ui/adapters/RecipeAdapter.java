package com.example.smartpantry.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpantry.databinding.ItemRecipeBinding;
import com.example.smartpantry.model.Recipe;
import java.util.List;

public class RecipeAdapter extends ListAdapter<Recipe, RecipeAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<Recipe> DIFF_CB =
            new DiffUtil.ItemCallback<Recipe>() {
                @Override
                public boolean areItemsTheSame(@NonNull Recipe a, @NonNull Recipe b) {
                    return a.getTitle().equals(b.getTitle());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Recipe a, @NonNull Recipe b) {
                    return a.getTitle().equals(b.getTitle())
                            && a.getIngredients().equals(b.getIngredients());
                }
            };

    public RecipeAdapter() {
        super(DIFF_CB);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecipeBinding binding = ItemRecipeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecipeBinding binding;

        ViewHolder(ItemRecipeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Recipe recipe) {
            binding.tvRecipeTitle.setText(recipe.getTitle());

            binding.tvRecipeIngredients.setText(formatList(recipe.getIngredients(), false));
            binding.tvRecipeSteps.setText(formatList(recipe.getSteps(), true));

            List<String> missing = recipe.getMissingIngredients();
            if (missing != null && !missing.isEmpty()) {
                binding.tvMissing.setText("Missing from pantry: " + String.join(", ", missing));
                binding.tvMissing.setVisibility(View.VISIBLE);
            } else {
                binding.tvMissing.setVisibility(View.GONE);
            }
        }

        private String formatList(List<String> items, boolean numbered) {
            if (items == null || items.isEmpty()) return "—";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) sb.append('\n');
                sb.append(numbered ? (i + 1) + ". " : "• ");
                sb.append(items.get(i));
            }
            return sb.toString();
        }
    }
}
