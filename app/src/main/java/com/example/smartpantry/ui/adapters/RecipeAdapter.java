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

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private static final DiffUtil.ItemCallback<Recipe> DIFF_CB =
            new DiffUtil.ItemCallback<Recipe>() {
                @Override
                public boolean areItemsTheSame(@NonNull Recipe a, @NonNull Recipe b) {
                    if (a.isSaved() && b.isSaved()) return a.getSavedId() == b.getSavedId();
                    if (!a.isSaved() && !b.isSaved()) return a.getTitle().equals(b.getTitle());
                    return false;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Recipe a, @NonNull Recipe b) {
                    return a.getTitle().equals(b.getTitle())
                            && a.getIngredients().equals(b.getIngredients())
                            && a.isSaved() == b.isSaved()
                            && a.isUserCreated() == b.isUserCreated();
                }
            };

    private final OnRecipeClickListener clickListener;

    public RecipeAdapter(OnRecipeClickListener clickListener) {
        super(DIFF_CB);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemRecipeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), clickListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecipeBinding b;

        ViewHolder(ItemRecipeBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(Recipe recipe, OnRecipeClickListener clickListener) {
            b.tvRecipeTitle.setText(recipe.getTitle());

            // Ingredient preview
            List<String> ings = recipe.getIngredients();
            if (ings != null && !ings.isEmpty()) {
                String preview = ings.size() <= 2
                        ? String.join(", ", ings)
                        : ings.get(0) + ", " + ings.get(1) + " +" + (ings.size() - 2) + " more";
                b.tvIngredientPreview.setText(preview);
                b.tvIngredientPreview.setVisibility(View.VISIBLE);
            } else {
                b.tvIngredientPreview.setVisibility(View.GONE);
            }
            if (recipe.isSaved() && recipe.isUserCreated()) {
                b.chipMyRecipe.setVisibility(View.VISIBLE);
                b.chipAiRecipe.setVisibility(View.GONE);
            } else if (recipe.isSaved() && !recipe.isUserCreated()) {
                b.chipMyRecipe.setVisibility(View.GONE);
                b.chipAiRecipe.setVisibility(View.VISIBLE);
            } else {
                b.chipMyRecipe.setVisibility(View.GONE);
                b.chipAiRecipe.setVisibility(View.GONE);
            }

            b.cardRecipe.setOnClickListener(v -> clickListener.onRecipeClick(recipe));
        }
    }
}
