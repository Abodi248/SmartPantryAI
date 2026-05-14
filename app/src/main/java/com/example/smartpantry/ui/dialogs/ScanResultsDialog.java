package com.example.smartpantry.ui.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.smartpantry.R;
import com.example.smartpantry.databinding.DialogScanResultsBinding;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.ui.adapters.ScanIngredientAdapter;
import com.example.smartpantry.viewmodel.PantryViewModel;
import java.util.List;

public class ScanResultsDialog extends DialogFragment {

    public static final String TAG = "ScanResultsDialog";

    private DialogScanResultsBinding binding;
    private ScanIngredientAdapter adapter;
    private PantryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogScanResultsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(PantryViewModel.class);

        List<Ingredient> scanned = viewModel.getScannedIngredients().getValue();
        if (scanned == null || scanned.isEmpty()) {
            dismiss();
            return;
        }

        adapter = new ScanIngredientAdapter(scanned);
        binding.rvScanIngredients.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvScanIngredients.setAdapter(adapter);
        binding.tvSubtitle.setText(getString(R.string.scan_items_found, scanned.size()));

        binding.btnAdd.setOnClickListener(v -> {
            List<Ingredient> selected = adapter.getSelectedIngredients();
            if (!selected.isEmpty()) viewModel.addAllIngredients(selected);
            dismiss();
        });

        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9f);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // Clear LiveData so the dialog doesn't re-appear when navigating back to this fragment
        if (viewModel != null) viewModel.clearScannedIngredients();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
