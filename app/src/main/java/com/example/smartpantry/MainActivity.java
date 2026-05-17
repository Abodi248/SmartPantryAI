package com.example.smartpantry;

import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.smartpantry.databinding.ActivityMainBinding;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // Destinations that should show the bottom navigation bar
    private static final List<Integer> BOTTOM_NAV_DESTINATIONS = Arrays.asList(
            R.id.nav_home,
            R.id.nav_pantry,
            R.id.nav_recipes,
            R.id.nav_chat
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.navHostFragment.setPadding(0, systemBars.top, 0, 0);
            binding.bottomNavView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNavView, navController);

        // Show bottom nav only for main app tabs; hide it on auth/detail screens
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean show = BOTTOM_NAV_DESTINATIONS.contains(destination.getId());
            binding.bottomNavView.setVisibility(show ? View.VISIBLE : View.GONE);
        });
    }
}
