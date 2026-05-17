package com.example.smartpantry.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.smartpantry.R;
import com.example.smartpantry.databinding.FragmentPantryBinding;
import com.example.smartpantry.ui.adapters.IngredientAdapter;
import com.example.smartpantry.ui.dialogs.AddIngredientDialog;
import com.example.smartpantry.ui.dialogs.ScanResultsDialog;
import com.example.smartpantry.viewmodel.PantryViewModel;
import com.google.android.material.snackbar.Snackbar;
import java.io.File;

public class PantryFragment extends Fragment {

    private static final String KEY_PENDING_URI = "pending_photo_uri";

    private FragmentPantryBinding binding;
    private PantryViewModel viewModel;
    private IngredientAdapter adapter;
    private Uri pendingPhotoUri;

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && pendingPhotoUri != null) {
                    viewModel.scanReceipt(pendingPhotoUri);
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchCamera();
                } else if (binding != null) {
                    Snackbar.make(binding.getRoot(),
                            R.string.scan_error_no_camera, Snackbar.LENGTH_LONG).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPantryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            pendingPhotoUri = savedInstanceState.getParcelable(KEY_PENDING_URI);
        }

        viewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        adapter = new IngredientAdapter(
                ingredient -> AddIngredientDialog
                        .newEditInstance(ingredient.getId(), ingredient.getName(),
                                ingredient.getQuantity(), ingredient.getUnit())
                        .show(getChildFragmentManager(), AddIngredientDialog.TAG),
                ingredient -> viewModel.deleteIngredient(ingredient)
        );

        binding.ingredientsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.ingredientsRecycler.setAdapter(adapter);

        viewModel.getIngredients().observe(getViewLifecycleOwner(), ingredients -> {
            adapter.submitList(ingredients);
            boolean empty = ingredients == null || ingredients.isEmpty();
            binding.ingredientsRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.emptyIcon.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.emptyMessage.setVisibility(empty ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsScanLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.scanProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.fabScanReceipt.setEnabled(!loading);
            binding.fabAddIngredient.setEnabled(!loading);
        });

        viewModel.getScanError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getScannedIngredients().observe(getViewLifecycleOwner(), scanned -> {
            if (scanned != null && !scanned.isEmpty()
                    && getChildFragmentManager()
                            .findFragmentByTag(ScanResultsDialog.TAG) == null) {
                new ScanResultsDialog().show(getChildFragmentManager(), ScanResultsDialog.TAG);
            }
        });

        binding.fabAddIngredient.setOnClickListener(v ->
                AddIngredientDialog.newAddInstance()
                        .show(getChildFragmentManager(), AddIngredientDialog.TAG));

        if (!viewModel.isAiAvailable()) {
            binding.fabScanReceipt.setEnabled(false);
            binding.tvScanUnavailable.setVisibility(View.VISIBLE);
        } else {
            binding.fabScanReceipt.setOnClickListener(v -> checkPermissionAndScan());
        }

        // Search
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (pendingPhotoUri != null) {
            outState.putParcelable(KEY_PENDING_URI, pendingPhotoUri);
        }
    }

    private void checkPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        File dir = new File(requireContext().getCacheDir(), "receipt_images");
        dir.mkdirs();
        File photoFile = new File(dir, "receipt_" + System.currentTimeMillis() + ".jpg");
        pendingPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                photoFile);
        takePictureLauncher.launch(pendingPhotoUri);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
