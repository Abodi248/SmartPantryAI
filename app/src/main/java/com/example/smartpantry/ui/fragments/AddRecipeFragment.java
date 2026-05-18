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
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.viewmodel.RecipesViewModel;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class AddRecipeFragment extends Fragment {

    public static final String ARG_RECIPE_ID = "recipe_id";

    private FragmentAddRecipeBinding binding;
    private RecipesViewModel viewModel;
    private long editingRecipeId = -1L;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddRecipeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(RecipesViewModel.class);

        if (getArguments() != null) {
            editingRecipeId = getArguments().getLong(ARG_RECIPE_ID, -1L);
        }

        boolean editMode = editingRecipeId != -1L;
        binding.toolbar.setTitle(editMode ? R.string.title_edit_recipe : R.string.title_add_recipe);
        binding.toolbar.setNavigationIcon(R.drawable.ic_nav_back);
        binding.toolbar.setNavigationContentDescription(R.string.cd_navigate_back);
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        if (editMode) {
            viewModel.getRecipeById(editingRecipeId).observe(getViewLifecycleOwner(), recipe -> {
                if (recipe != null && binding.etRecipeTitle.getText() != null
                        && binding.etRecipeTitle.getText().toString().isEmpty()) {
                    prefillForm(recipe);
                }
            });
        } else {
            addIngredientRow(null);
            addStepRow(null);
        }

        binding.btnAddIngredientRow.setOnClickListener(v -> addIngredientRow(null));
        binding.btnAddStepRow.setOnClickListener(v -> addStepRow(null));
        binding.btnSaveRecipe.setOnClickListener(v -> saveRecipe());
    }

    private void prefillForm(Recipe recipe) {
        binding.etRecipeTitle.setText(recipe.getTitle());
        binding.containerIngredients.removeAllViews();
        binding.containerSteps.removeAllViews();

        if (recipe.getIngredients().isEmpty()) {
            addIngredientRow(null);
        } else {
            for (String ing : recipe.getIngredients()) addIngredientRow(ing);
        }

        if (recipe.getSteps().isEmpty()) {
            addStepRow(null);
        } else {
            for (String step : recipe.getSteps()) addStepRow(step);
        }

        if (!recipe.getTips().isEmpty()) {
            binding.etTips.setText(recipe.getTips());
        }
    }

    private void addIngredientRow(@Nullable String initialText) {
        ItemDynamicRowBinding row = ItemDynamicRowBinding.inflate(
                getLayoutInflater(), binding.containerIngredients, false);
        row.inputRow.setHint(getString(R.string.hint_ingredient_name));
        if (initialText != null) row.etRow.setText(initialText);
        row.btnRemoveRow.setOnClickListener(v -> {
            if (binding.containerIngredients.getChildCount() > 1) {
                binding.containerIngredients.removeView(row.getRoot());
            }
        });
        binding.containerIngredients.addView(row.getRoot());
    }

    private void addStepRow(@Nullable String initialText) {
        ItemDynamicRowBinding row = ItemDynamicRowBinding.inflate(
                getLayoutInflater(), binding.containerSteps, false);
        row.inputRow.setHint(getString(R.string.hint_step_description));
        if (initialText != null) row.etRow.setText(initialText);
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
        String tips = binding.etTips.getText() != null
                ? binding.etTips.getText().toString().trim() : "";

        if (ingredients.isEmpty()) {
            Snackbar.make(binding.getRoot(), R.string.error_add_ingredient, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (editingRecipeId != -1L) {
            Recipe updated = new Recipe(title, ingredients, steps, new ArrayList<>());
            updated.setSavedId(editingRecipeId);
            updated.setSaved(true);
            updated.setUserCreated(true);
            updated.setTips(tips);
            viewModel.updateRecipe(updated);
        } else {
            viewModel.saveRecipe(title, ingredients, steps, tips);
        }

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
