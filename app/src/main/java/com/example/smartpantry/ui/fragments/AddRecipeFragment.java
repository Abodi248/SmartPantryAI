package com.example.smartpantry.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.smartpantry.R;
import com.example.smartpantry.databinding.FragmentAddRecipeBinding;
import com.example.smartpantry.databinding.ItemDynamicRowBinding;
import com.example.smartpantry.viewmodel.RecipesViewModel;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class AddRecipeFragment extends Fragment {

    private FragmentAddRecipeBinding binding;
    private RecipesViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddRecipeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity())
                .get(RecipesViewModel.class);

        binding.toolbar.setNavigationIcon(R.drawable.ic_nav_back);
        binding.toolbar.setNavigationContentDescription(R.string.cd_navigate_back);
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        addIngredientRow();
        addStepRow();

        binding.btnAddIngredientRow.setOnClickListener(v -> addIngredientRow());
        binding.btnAddStepRow.setOnClickListener(v -> addStepRow());

        binding.btnSaveRecipe.setOnClickListener(v -> saveRecipe());
    }

    private void addIngredientRow() {
        ItemDynamicRowBinding row = ItemDynamicRowBinding.inflate(
                getLayoutInflater(), binding.containerIngredients, false);
        row.inputRow.setHint(getString(R.string.hint_ingredient_name));
        row.btnRemoveRow.setOnClickListener(v -> {
            if (binding.containerIngredients.getChildCount() > 1) {
                binding.containerIngredients.removeView(row.getRoot());
            }
        });
        binding.containerIngredients.addView(row.getRoot());
    }

    private void addStepRow() {
        ItemDynamicRowBinding row = ItemDynamicRowBinding.inflate(
                getLayoutInflater(), binding.containerSteps, false);
        row.inputRow.setHint(getString(R.string.hint_step_description));
        row.btnRemoveRow.setOnClickListener(v -> {
            if (binding.containerSteps.getChildCount() > 1) {
                binding.containerSteps.removeView(row.getRoot());
            }
        });
        binding.containerSteps.addView(row.getRoot());
    }

    private void saveRecipe() {
        String title = binding.etRecipeTitle.getText() != null
                ? binding.etRecipeTitle.getText().toString().trim() : "";
        if (TextUtils.isEmpty(title)) {
            binding.inputRecipeTitle.setError(getString(R.string.error_name_required));
            return;
        }
        binding.inputRecipeTitle.setError(null);

        List<String> ingredients = collectRows(binding.containerIngredients);
        List<String> steps = collectRows(binding.containerSteps);

        if (ingredients.isEmpty()) {
            Snackbar.make(binding.getRoot(), R.string.error_add_ingredient, Snackbar.LENGTH_SHORT).show();
            return;
        }

        viewModel.saveRecipe(title, ingredients, steps);
        Navigation.findNavController(binding.getRoot()).navigateUp();
    }

    private List<String> collectRows(ViewGroup container) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            ItemDynamicRowBinding row = ItemDynamicRowBinding.bind(child);
            String text = row.etRow.getText() != null ? row.etRow.getText().toString().trim() : "";
            if (!text.isEmpty()) result.add(text);
        }
        return result;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
