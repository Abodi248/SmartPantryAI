package com.example.smartpantry.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpantry.databinding.ItemIngredientBinding;
import com.example.smartpantry.model.Ingredient;
import java.util.function.Consumer;

public class IngredientAdapter extends ListAdapter<Ingredient, IngredientAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<Ingredient> DIFF_CB =
            new DiffUtil.ItemCallback<Ingredient>() {
                @Override
                public boolean areItemsTheSame(@NonNull Ingredient a, @NonNull Ingredient b) {
                    return a.getId() == b.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Ingredient a, @NonNull Ingredient b) {
                    return a.getName().equals(b.getName())
                            && safeEquals(a.getQuantity(), b.getQuantity())
                            && safeEquals(a.getUnit(), b.getUnit());
                }

                private boolean safeEquals(String a, String b) {
                    if (a == null && b == null) return true;
                    if (a == null || b == null) return false;
                    return a.equals(b);
                }
            };

    private final Consumer<Ingredient> onEdit;
    private final Consumer<Ingredient> onDelete;

    public IngredientAdapter(Consumer<Ingredient> onEdit, Consumer<Ingredient> onDelete) {
        super(DIFF_CB);
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIngredientBinding binding = ItemIngredientBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemIngredientBinding binding;

        ViewHolder(ItemIngredientBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Ingredient ingredient) {
            binding.tvName.setText(ingredient.getName());

            String qty = ingredient.getQuantity();
            String unit = ingredient.getUnit();
            boolean hasQty = qty != null && !qty.isEmpty();
            boolean hasUnit = unit != null && !unit.isEmpty();

            if (hasQty && hasUnit) {
                binding.tvQuantity.setText(qty + " " + unit);
            } else if (hasQty) {
                binding.tvQuantity.setText(qty);
            } else if (hasUnit) {
                binding.tvQuantity.setText(unit);
            } else {
                binding.tvQuantity.setText("");
            }

            binding.btnEdit.setOnClickListener(v -> onEdit.accept(ingredient));
            binding.btnDelete.setOnClickListener(v -> onDelete.accept(ingredient));
        }
    }
}
