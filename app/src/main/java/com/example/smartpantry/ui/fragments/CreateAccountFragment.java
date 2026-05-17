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
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.example.smartpantry.R;
import com.example.smartpantry.databinding.FragmentCreateAccountBinding;
import com.example.smartpantry.viewmodel.UserViewModel;
import com.google.android.material.snackbar.Snackbar;

public class CreateAccountFragment extends Fragment {

    private FragmentCreateAccountBinding binding;
    private UserViewModel userViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateAccountBinding.inflate(inflater, container, false);
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
                        NavHostFragment.findNavController(CreateAccountFragment.this).navigateUp();
                    }
                });

        userViewModel.resetAuthResult();
        binding.btnCreate.setOnClickListener(v -> attemptCreate());

        userViewModel.getAuthResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result) {
                case SUCCESS:
                    navigateToHome();
                    break;
                case USERNAME_TAKEN:
                    Snackbar.make(binding.getRoot(),
                            R.string.error_username_taken,
                            Snackbar.LENGTH_SHORT).show();
                    break;
                default:
                    Snackbar.make(binding.getRoot(),
                            R.string.error_generic,
                            Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptCreate() {
        String displayName = getText(binding.etDisplayName);
        String username = getText(binding.etUsername);
        String password = getText(binding.etPassword);
        String confirm = getText(binding.etConfirmPassword);

        if (displayName.isEmpty()) {
            binding.inputDisplayName.setError(getString(R.string.error_name_required));
            return;
        }
        binding.inputDisplayName.setError(null);

        if (username.isEmpty()) {
            binding.inputUsername.setError(getString(R.string.error_name_required));
            return;
        }
        binding.inputUsername.setError(null);

        if (password.isEmpty()) {
            binding.inputPassword.setError(getString(R.string.error_password_required));
            return;
        }
        binding.inputPassword.setError(null);

        if (!password.equals(confirm)) {
            binding.inputConfirmPassword.setError(getString(R.string.error_passwords_no_match));
            return;
        }
        binding.inputConfirmPassword.setError(null);

        userViewModel.createAccount(displayName, username, password);
    }

    private String getText(com.google.android.material.textfield.TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void navigateToHome() {
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_login, true)
                .build();
        NavHostFragment.findNavController(this)
                       .navigate(R.id.action_nav_create_account_to_nav_home, null, navOptions);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
