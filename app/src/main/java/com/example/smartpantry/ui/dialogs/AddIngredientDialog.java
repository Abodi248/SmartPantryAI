package com.example.smartpantry.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.smartpantry.R;
import com.example.smartpantry.databinding.DialogAddIngredientBinding;
import com.example.smartpantry.viewmodel.PantryViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AddIngredientDialog extends DialogFragment {

    public static final String TAG = "AddIngredientDialog";

    private static final String ARG_ID = "id";
    private static final String ARG_NAME = "name";
    private static final String ARG_QUANTITY = "quantity";
    private static final String ARG_UNIT = "unit";

    public static AddIngredientDialog newAddInstance() {
        return new AddIngredientDialog();
    }

    public static AddIngredientDialog newEditInstance(long id, String name,
                                                      String quantity, String unit) {
        AddIngredientDialog d = new AddIngredientDialog();
        Bundle b = new Bundle();
        b.putLong(ARG_ID, id);
        b.putString(ARG_NAME, name);
        b.putString(ARG_QUANTITY, quantity != null ? quantity : "");
        b.putString(ARG_UNIT, unit != null ? unit : "");
        d.setArguments(b);
        return d;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        boolean isEdit = args != null && args.containsKey(ARG_NAME);

        DialogAddIngredientBinding binding = DialogAddIngredientBinding.inflate(
                LayoutInflater.from(requireContext()));

        if (isEdit) {
            binding.etName.setText(args.getString(ARG_NAME));
            binding.etQuantity.setText(args.getString(ARG_QUANTITY));
            binding.etUnit.setText(args.getString(ARG_UNIT));
        }

        PantryViewModel viewModel = new ViewModelProvider(requireParentFragment())
                .get(PantryViewModel.class);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(isEdit ? R.string.dialog_edit_ingredient_title
                                 : R.string.dialog_add_ingredient_title)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String name = binding.etName.getText() != null
                            ? binding.etName.getText().toString().trim() : "";
                    if (name.isEmpty()) {
                        binding.tilName.setError(getString(R.string.error_name_required));
                        return;
                    }
                    binding.tilName.setError(null);
                    String quantity = binding.etQuantity.getText() != null
                            ? binding.etQuantity.getText().toString().trim() : "";
                    String unit = binding.etUnit.getText() != null
                            ? binding.etUnit.getText().toString().trim() : "";
                    if (isEdit) {
                        viewModel.updateIngredient(args.getLong(ARG_ID), name, quantity, unit);
                    } else {
                        viewModel.addIngredient(name, quantity, unit);
                    }
                    dismiss();
                }));

        return dialog;
    }
}
