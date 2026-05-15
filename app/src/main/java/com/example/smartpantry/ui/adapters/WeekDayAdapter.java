package com.example.smartpantry.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpantry.databinding.ItemWeekDayBinding;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class WeekDayAdapter extends RecyclerView.Adapter<WeekDayAdapter.ViewHolder> {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public interface OnDayClickListener {
        void onDayClick(String isoDate);
    }

    private final LocalDate[] days = new LocalDate[7];
    private final Set<String> datesWithMeals = new HashSet<>();
    private String selectedDate;
    private final OnDayClickListener listener;

    public WeekDayAdapter(OnDayClickListener listener) {
        this.listener = listener;
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) days[i] = today.plusDays(i);
        selectedDate = today.format(ISO);
    }

    public void setDatesWithMeals(List<String> dates) {
        datesWithMeals.clear();
        if (dates != null) datesWithMeals.addAll(dates);
        notifyDataSetChanged();
    }

    public void setSelectedDate(String isoDate) {
        selectedDate = isoDate;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWeekDayBinding b = ItemWeekDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalDate date = days[position];
        String iso = date.format(ISO);
        boolean isSelected = iso.equals(selectedDate);
        boolean hasMeals = datesWithMeals.contains(iso);
        holder.bind(date, isSelected, hasMeals, () -> {
            setSelectedDate(iso);
            listener.onDayClick(iso);
        });
    }

    @Override
    public int getItemCount() { return 7; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemWeekDayBinding b;

        ViewHolder(ItemWeekDayBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(LocalDate date, boolean selected, boolean hasMeals, Runnable onClick) {
            b.tvDayName.setText(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            b.tvDayNumber.setText(String.valueOf(date.getDayOfMonth()));
            b.mealDot.setVisibility(hasMeals ? android.view.View.VISIBLE : android.view.View.INVISIBLE);
            b.getRoot().setSelected(selected);
            b.getRoot().setOnClickListener(v -> onClick.run());
        }
    }
}
