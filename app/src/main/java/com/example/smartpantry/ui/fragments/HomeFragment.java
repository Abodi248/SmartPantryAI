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
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.smartpantry.R;
import com.example.smartpantry.databinding.FragmentHomeBinding;
import com.example.smartpantry.ui.adapters.MealPlanListAdapter;
import com.example.smartpantry.ui.adapters.WeekDayAdapter;
import com.example.smartpantry.ui.dialogs.DayMealPlanBottomSheet;
import com.example.smartpantry.viewmodel.HomeViewModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("EEEE, MMM d");

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private WeekDayAdapter weekDayAdapter;
    private MealPlanListAdapter mealAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Toolbar — person icon navigates to Settings
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                NavHostFragment.findNavController(this)
                               .navigate(R.id.action_nav_home_to_nav_settings);
                return true;
            }
            return false;
        });

        // Back press on Home finishes the activity
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        requireActivity().finish();
                    }
                });

        // Welcome greeting
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Good morning!" : hour < 17 ? "Good afternoon!" : "Good evening!";
        binding.tvWelcome.setText(greeting);

        // Summary cards
        viewModel.getPantryCount().observe(getViewLifecycleOwner(), count ->
                binding.tvPantryCount.setText(String.valueOf(count != null ? count : 0)));

        viewModel.getReadyRecipeCount().observe(getViewLifecycleOwner(), count ->
                binding.tvReadyCount.setText(String.valueOf(count != null ? count : 0)));

        // Week strip
        weekDayAdapter = new WeekDayAdapter(isoDate -> {
            viewModel.selectDate(isoDate);
            updateDayLabel(isoDate);
        });
        binding.rvWeek.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvWeek.setAdapter(weekDayAdapter);

        viewModel.getDatesWithMeals().observe(getViewLifecycleOwner(),
                dates -> weekDayAdapter.setDatesWithMeals(dates));

        // Meals for selected day
        mealAdapter = new MealPlanListAdapter(meal -> viewModel.deleteMeal(meal));
        binding.rvDayMeals.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDayMeals.setAdapter(mealAdapter);
        binding.rvDayMeals.setNestedScrollingEnabled(false);

        viewModel.getMealsForSelectedDate().observe(getViewLifecycleOwner(), meals -> {
            mealAdapter.submitList(meals);
            boolean empty = meals == null || meals.isEmpty();
            binding.rvDayMeals.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.tvNoMeals.setVisibility(empty ? View.VISIBLE : View.GONE);
        });

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            weekDayAdapter.setSelectedDate(date);
            updateDayLabel(date);
        });

        // Add meal button opens the bottom sheet
        binding.btnAddMeal.setOnClickListener(v -> {
            String date = viewModel.getSelectedDate().getValue();
            if (date == null) date = LocalDate.now().format(ISO);
            DayMealPlanBottomSheet sheet = DayMealPlanBottomSheet.newInstance(date);
            sheet.show(getChildFragmentManager(), DayMealPlanBottomSheet.TAG);
        });

        updateDayLabel(LocalDate.now().format(ISO));
    }

    private void updateDayLabel(String isoDate) {
        if (binding == null) return;
        try {
            LocalDate date = LocalDate.parse(isoDate, ISO);
            binding.tvSelectedDayLabel.setText(date.format(DISPLAY));
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
