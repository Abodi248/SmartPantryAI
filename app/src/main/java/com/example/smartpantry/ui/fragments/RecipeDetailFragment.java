package com.example.smartpantry.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.smartpantry.R;
import com.example.smartpantry.databinding.FragmentRecipeDetailBinding;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.viewmodel.RecipesViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;

public class RecipeDetailFragment extends Fragment {

    private FragmentRecipeDetailBinding binding;
    private RecipesViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRecipeDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(RecipesViewModel.class);

        binding.toolbar.setNavigationIcon(R.drawable.ic_nav_back);
        binding.toolbar.setNavigationContentDescription(R.string.cd_navigate_back);
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        viewModel.getSelectedRecipe().observe(getViewLifecycleOwner(), recipe -> {
            if (recipe == null) return;
            populateView(recipe);
        });
    }

    private void populateView(Recipe recipe) {
        binding.toolbar.setTitle(recipe.getTitle());

        // Source chip
        if (recipe.isSaved()) {
            binding.chipSource.setVisibility(View.VISIBLE);
            if (recipe.isUserCreated()) {
                binding.chipSource.setText(getString(R.string.chip_my_recipe));
            } else {
                binding.chipSource.setText(getString(R.string.chip_ai_recipe));
            }
        } else {
            binding.chipSource.setVisibility(View.GONE);
        }

        // Ingredients
        binding.tvIngredients.setText(formatList(recipe.getIngredients(), false));

        // Steps
        binding.tvSteps.setText(formatList(recipe.getSteps(), true));

        // Missing
        List<String> missing = recipe.getMissingIngredients();
        if (missing != null && !missing.isEmpty()) {
            binding.layoutMissing.setVisibility(View.VISIBLE);
            binding.tvMissing.setText(String.join("\n", missing));
        } else {
            binding.layoutMissing.setVisibility(View.GONE);
        }

        // Tips
        String tips = recipe.getTips();
        if (tips != null && !tips.isEmpty()) {
            binding.layoutTips.setVisibility(View.VISIBLE);
            binding.tvTips.setText(tips);
        } else {
            binding.layoutTips.setVisibility(View.GONE);
        }

        // Delete FAB — only for persisted recipes
        if (recipe.isSaved()) {
            binding.fabDelete.setVisibility(View.VISIBLE);
            binding.fabDelete.setOnClickListener(v -> confirmDelete(recipe));
        } else {
            binding.fabDelete.setVisibility(View.GONE);
        }
    }

    private void confirmDelete(Recipe recipe) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_delete_recipe_title)
                .setMessage(R.string.dialog_delete_recipe_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    viewModel.deleteSavedRecipe(recipe);
                    Navigation.findNavController(binding.getRoot()).navigateUp();
                })
                .show();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
