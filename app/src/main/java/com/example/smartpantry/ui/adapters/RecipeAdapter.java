package com.example.smartpantry.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    public interface OnRecipeEditListener {
        void onRecipeEdit(Recipe recipe);
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
                            && a.isUserCreated() == b.isUserCreated()
                            && a.getTips().equals(b.getTips());
                }
            };

    private final OnRecipeClickListener clickListener;
    @Nullable private final OnRecipeEditListener editListener;

    public RecipeAdapter(OnRecipeClickListener clickListener) {
        this(clickListener, null);
    }

    public RecipeAdapter(OnRecipeClickListener clickListener,
                         @Nullable OnRecipeEditListener editListener) {
        super(DIFF_CB);
        this.clickListener = clickListener;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemRecipeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), clickListener, editListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecipeBinding b;

        ViewHolder(ItemRecipeBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(Recipe recipe, OnRecipeClickListener clickListener,
                  @Nullable OnRecipeEditListener editListener) {
            b.tvRecipeTitle.setText(recipe.getTitle());

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

            // Show edit button only for user-created saved recipes
            if (recipe.isSaved() && recipe.isUserCreated() && editListener != null) {
                b.btnEditRecipe.setVisibility(View.VISIBLE);
                b.btnEditRecipe.setOnClickListener(v -> editListener.onRecipeEdit(recipe));
            } else {
                b.btnEditRecipe.setVisibility(View.GONE);
                b.btnEditRecipe.setOnClickListener(null);
            }

            b.cardRecipe.setOnClickListener(v -> clickListener.onRecipeClick(recipe));
        }
    }
}
