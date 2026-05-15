package com.example.smartpantry.ui.dialogs;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.smartpantry.R;
import com.example.smartpantry.databinding.DialogDayMealPlanBinding;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.ui.adapters.MealPlanListAdapter;
import com.example.smartpantry.viewmodel.HomeViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DayMealPlanBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "DayMealPlanBottomSheet";
    private static final String ARG_DATE = "date";
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("EEEE, MMMM d");

    private DialogDayMealPlanBinding binding;
    private HomeViewModel viewModel;
    private List<Recipe> savedRecipesCache = new ArrayList<>();

    public static DayMealPlanBottomSheet newInstance(String isoDate) {
        DayMealPlanBottomSheet sheet = new DayMealPlanBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, isoDate);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogDayMealPlanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireParentFragment()).get(HomeViewModel.class);

        String date = getArguments() != null ? getArguments().getString(ARG_DATE, "") : "";
        viewModel.selectDate(date);

        try {
            binding.tvDayTitle.setText(LocalDate.parse(date, ISO).format(DISPLAY));
        } catch (Exception e) {
            binding.tvDayTitle.setText(date);
        }

        // Meals list
        MealPlanListAdapter adapter = new MealPlanListAdapter(meal -> viewModel.deleteMeal(meal));
        binding.rvMeals.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMeals.setAdapter(adapter);
        binding.rvMeals.setNestedScrollingEnabled(false);

        viewModel.getMealsForSelectedDate().observe(getViewLifecycleOwner(), meals -> {
            adapter.submitList(meals);
            boolean empty = meals == null || meals.isEmpty();
            binding.rvMeals.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.tvEmptyMeals.setVisibility(empty ? View.VISIBLE : View.GONE);
        });

        // Keep saved recipes in a cache for the picker
        viewModel.getSavedRecipes().observe(getViewLifecycleOwner(), recipes ->
                savedRecipesCache = recipes != null ? recipes : new ArrayList<>());

        // "Pick from saved recipes" button
        binding.btnPickRecipe.setOnClickListener(v -> showRecipePicker());

        // Save button
        binding.btnSaveMeal.setOnClickListener(v -> {
            String title = binding.etMealTitle.getText() != null
                    ? binding.etMealTitle.getText().toString().trim() : "";
            if (TextUtils.isEmpty(title)) {
                binding.inputMealTitle.setError(getString(R.string.error_name_required));
                return;
            }
            binding.inputMealTitle.setError(null);
            String notes = binding.etMealNotes.getText() != null
                    ? binding.etMealNotes.getText().toString().trim() : "";
            viewModel.addMeal(title, notes);
            binding.etMealTitle.setText("");
            binding.etMealNotes.setText("");
        });
    }

    private void showRecipePicker() {
        if (savedRecipesCache.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.btn_pick_saved_recipe)
                    .setMessage(R.string.msg_no_saved_recipes)
                    .setPositiveButton(R.string.action_cancel, null)
                    .show();
            return;
        }

        String[] titles = new String[savedRecipesCache.size()];
        for (int i = 0; i < savedRecipesCache.size(); i++) {
            titles[i] = savedRecipesCache.get(i).getTitle();
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.btn_pick_saved_recipe)
                .setItems(titles, (dialog, which) -> {
                    if (binding != null) {
                        binding.etMealTitle.setText(savedRecipesCache.get(which).getTitle());
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
