package com.example.smartpantry.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.smartpantry.R;
import com.example.smartpantry.databinding.FragmentRecipesBinding;
import com.example.smartpantry.ui.adapters.RecipeAdapter;
import com.example.smartpantry.ui.fragments.AddRecipeFragment;
import com.example.smartpantry.viewmodel.RecipesViewModel;

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
        viewModel = new ViewModelProvider(requireActivity()).get(RecipesViewModel.class);

        adapter = new RecipeAdapter(
                recipe -> {
                    viewModel.selectRecipe(recipe);
                    Navigation.findNavController(binding.getRoot())
                            .navigate(R.id.action_nav_recipes_to_nav_recipe_detail);
                },
                recipe -> {
                    Bundle args = new Bundle();
                    args.putLong(AddRecipeFragment.ARG_RECIPE_ID, recipe.getSavedId());
                    Navigation.findNavController(binding.getRoot())
                            .navigate(R.id.action_nav_recipes_to_nav_add_recipe, args);
                }
        );
        binding.recipesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recipesRecycler.setAdapter(adapter);

        viewModel.getRecipes().observe(getViewLifecycleOwner(), recipes -> {
            adapter.submitList(recipes);
            boolean hasResults = recipes != null && !recipes.isEmpty();
            binding.recipesRecycler.setVisibility(hasResults ? View.VISIBLE : View.GONE);
            binding.emptyIcon.setVisibility(hasResults ? View.GONE : View.VISIBLE);
            binding.emptyMessage.setVisibility(hasResults ? View.GONE : View.VISIBLE);
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.fabAddRecipe.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_nav_recipes_to_nav_add_recipe));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
