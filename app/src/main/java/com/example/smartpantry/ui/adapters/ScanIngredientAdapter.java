package com.example.smartpantry.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpantry.databinding.ItemScanIngredientBinding;
import com.example.smartpantry.model.Ingredient;
import java.util.ArrayList;
import java.util.List;

public class ScanIngredientAdapter extends RecyclerView.Adapter<ScanIngredientAdapter.ViewHolder> {

    private final List<Ingredient> items;
    private final boolean[] checked;

    public ScanIngredientAdapter(List<Ingredient> items) {
        this.items = items;
        this.checked = new boolean[items.size()];
        for (int i = 0; i < checked.length; i++) checked[i] = true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemScanIngredientBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient item = items.get(position);
        holder.binding.tvName.setText(item.getName());
        String qty = item.getQuantity();
        String unit = item.getUnit();
        holder.binding.tvQtyUnit.setText(
                (unit != null && !unit.isEmpty()) ? qty + " " + unit : qty);
        holder.binding.cbSelected.setOnCheckedChangeListener(null);
        holder.binding.cbSelected.setChecked(checked[position]);
        int pos = position;
        holder.binding.cbSelected.setOnCheckedChangeListener(
                (btn, isChecked) -> checked[pos] = isChecked);
    }

    @Override
    public int getItemCount() { return items.size(); }

    public List<Ingredient> getSelectedIngredients() {
        List<Ingredient> selected = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (checked[i]) selected.add(items.get(i));
        }
        return selected;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemScanIngredientBinding binding;

        ViewHolder(ItemScanIngredientBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
