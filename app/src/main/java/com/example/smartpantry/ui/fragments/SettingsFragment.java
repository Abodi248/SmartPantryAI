package com.example.smartpantry.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.example.smartpantry.R;
import com.example.smartpantry.databinding.FragmentSettingsBinding;
import com.example.smartpantry.viewmodel.UserViewModel;
import com.google.android.material.snackbar.Snackbar;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private UserViewModel userViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        NavHostFragment.findNavController(SettingsFragment.this).navigateUp();
                    }
                });

        // Pre-fill display name from the logged-in user
        if (userViewModel.getLoggedInUser() != null) {
            userViewModel.getLoggedInUser().observe(getViewLifecycleOwner(), user -> {
                if (user != null && binding.etDisplayName.getText() != null
                        && binding.etDisplayName.getText().toString().isEmpty()) {
                    binding.etDisplayName.setText(user.displayName);
                }
            });
        }

        binding.btnSave.setOnClickListener(v -> {
            String newName = binding.etDisplayName.getText() != null
                    ? binding.etDisplayName.getText().toString().trim() : "";
            if (newName.isEmpty()) {
                binding.inputDisplayName.setError(getString(R.string.error_name_required));
                return;
            }
            binding.inputDisplayName.setError(null);
            userViewModel.updateDisplayName(newName);
            Snackbar.make(binding.getRoot(), R.string.msg_saved, Snackbar.LENGTH_SHORT).show();
        });

        binding.btnLogout.setOnClickListener(v -> {
            userViewModel.logout();
            NavHostFragment.findNavController(this).navigate(R.id.action_global_nav_login);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
