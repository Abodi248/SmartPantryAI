package com.example.smartpantry.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpantry.databinding.ItemMealPlanBinding;
import com.example.smartpantry.model.MealPlan;

public class MealPlanListAdapter extends ListAdapter<MealPlan, MealPlanListAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(MealPlan mealPlan);
    }

    private static final DiffUtil.ItemCallback<MealPlan> DIFF_CB =
            new DiffUtil.ItemCallback<MealPlan>() {
                @Override
                public boolean areItemsTheSame(@NonNull MealPlan a, @NonNull MealPlan b) {
                    return a.getId() == b.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull MealPlan a, @NonNull MealPlan b) {
                    return a.getId() == b.getId()
                            && a.getRecipeTitle().equals(b.getRecipeTitle())
                            && a.getNotes().equals(b.getNotes());
                }
            };

    private final OnDeleteListener deleteListener;

    public MealPlanListAdapter(OnDeleteListener deleteListener) {
        super(DIFF_CB);
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMealPlanBinding b = ItemMealPlanBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), deleteListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMealPlanBinding b;

        ViewHolder(ItemMealPlanBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(MealPlan meal, OnDeleteListener deleteListener) {
            b.tvMealTitle.setText(meal.getRecipeTitle());
            if (meal.getNotes() != null && !meal.getNotes().isEmpty()) {
                b.tvMealNotes.setText(meal.getNotes());
                b.tvMealNotes.setVisibility(android.view.View.VISIBLE);
            } else {
                b.tvMealNotes.setVisibility(android.view.View.GONE);
            }
            b.btnDeleteMeal.setOnClickListener(v -> deleteListener.onDelete(meal));
        }
    }
}
