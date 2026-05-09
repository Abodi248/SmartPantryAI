package com.example.smartpantry.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.smartpantry.databinding.FragmentRecipesBinding;
import com.example.smartpantry.viewmodel.RecipesViewModel;

public class RecipesFragment extends Fragment {

    private FragmentRecipesBinding binding;
    private RecipesViewModel viewModel;

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
        // do later: observe viewModel.recipes LiveData and update RecyclerView
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}