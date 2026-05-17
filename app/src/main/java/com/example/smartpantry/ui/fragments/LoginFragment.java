package com.example.smartpantry.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.example.smartpantry.R;
import com.example.smartpantry.databinding.FragmentLoginBinding;
import com.example.smartpantry.viewmodel.UserViewModel;
import com.google.android.material.snackbar.Snackbar;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private UserViewModel userViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Skip login if already authenticated
        if (userViewModel.isLoggedIn()) {
            navigateToHome();
            return;
        }

        userViewModel.resetAuthResult();
        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.btnCreateAccount.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                               .navigate(R.id.action_nav_login_to_nav_create_account));

        userViewModel.getAuthResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result) {
                case SUCCESS:
                    navigateToHome();
                    break;
                case INVALID_CREDENTIALS:
                    Snackbar.make(binding.getRoot(),
                            R.string.error_invalid_credentials,
                            Snackbar.LENGTH_SHORT).show();
                    break;
                default:
                    Snackbar.make(binding.getRoot(),
                            R.string.error_generic,
                            Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptLogin() {
        String username = binding.etUsername.getText() != null
                ? binding.etUsername.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null
                ? binding.etPassword.getText().toString() : "";
        userViewModel.login(username, password);
    }

    private void navigateToHome() {
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_login, true)
                .build();
        NavHostFragment.findNavController(this)
                       .navigate(R.id.action_nav_login_to_nav_home, null, navOptions);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
