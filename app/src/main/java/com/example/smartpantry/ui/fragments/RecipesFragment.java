package com.example.smartpantry.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.smartpantry.databinding.FragmentRecipesBinding;
import com.example.smartpantry.ui.adapters.RecipeAdapter;
import com.example.smartpantry.viewmodel.RecipesViewModel;
import com.google.android.material.snackbar.Snackbar;

public class RecipesFragment extends Fragment {

    private FragmentRecipesBinding binding;
    private RecipesViewModel viewModel;
    private RecipeAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRecipesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RecipesViewModel.class);

        adapter = new RecipeAdapter();
        binding.recipesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recipesRecycler.setAdapter(adapter);

        viewModel.getRecipes().observe(getViewLifecycleOwner(), recipes -> {
            adapter.submitList(recipes);
            boolean hasResults = recipes != null && !recipes.isEmpty();
            binding.recipesRecycler.setVisibility(hasResults ? View.VISIBLE : View.GONE);
            binding.emptyIcon.setVisibility(hasResults ? View.GONE : View.VISIBLE);
            binding.emptyMessage.setVisibility(hasResults ? View.GONE : View.VISIBLE);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnGenerateRecipes.setEnabled(!loading);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG)
                        .setAction("Retry", v -> viewModel.generateRecipes())
                        .show();
            }
        });

        binding.btnGenerateRecipes.setOnClickListener(v -> viewModel.generateRecipes());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
