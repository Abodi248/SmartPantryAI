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
import com.example.smartpantry.databinding.FragmentPantryBinding;
import com.example.smartpantry.ui.adapters.IngredientAdapter;
import com.example.smartpantry.ui.dialogs.AddIngredientDialog;
import com.example.smartpantry.viewmodel.PantryViewModel;

public class PantryFragment extends Fragment {

    private FragmentPantryBinding binding;
    private PantryViewModel viewModel;
    private IngredientAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPantryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        adapter = new IngredientAdapter(
                ingredient -> AddIngredientDialog
                        .newEditInstance(ingredient.getId(), ingredient.getName(),
                                ingredient.getQuantity(), ingredient.getUnit())
                        .show(getChildFragmentManager(), AddIngredientDialog.TAG),
                ingredient -> viewModel.deleteIngredient(ingredient)
        );

        binding.ingredientsRecycler.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.ingredientsRecycler.setAdapter(adapter);

        viewModel.getIngredients().observe(getViewLifecycleOwner(), ingredients -> {
            adapter.submitList(ingredients);
            boolean empty = ingredients == null || ingredients.isEmpty();
            binding.ingredientsRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.emptyIcon.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.emptyMessage.setVisibility(empty ? View.VISIBLE : View.GONE);
        });

        binding.fabAddIngredient.setOnClickListener(v ->
                AddIngredientDialog.newAddInstance()
                        .show(getChildFragmentManager(), AddIngredientDialog.TAG));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
