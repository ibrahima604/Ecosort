package com.example.ecosort;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.card.MaterialCardView;

public class AdminActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ecosort_prefs";
    private static final String KEY_EMAIL  = "user_email";

    private View       headerLayout;
    private ScrollView gridScrollView;
    private View       fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        headerLayout      = findViewById(R.id.header_admin);
        gridScrollView    = findViewById(R.id.grid_scroll_view);
        fragmentContainer = findViewById(R.id.fragment_container);

        // Email dans le header
        TextView adminEmail = findViewById(R.id.admin_email);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        adminEmail.setText(prefs.getString(KEY_EMAIL, "admin@email.com"));

        // Cartes
        MaterialCardView cardDashboard = findViewById(R.id.card_dashboard);
        MaterialCardView cardClients   = findViewById(R.id.card_clients);
        MaterialCardView cardConseils  = findViewById(R.id.card_conseils);
        MaterialCardView cardStats     = findViewById(R.id.card_stats);

        cardDashboard.setOnClickListener(v -> showFragment(new DashboardFragment()));
        cardClients.setOnClickListener(v   -> showFragment(new ClientFragment()));
        cardConseils.setOnClickListener(v  -> showFragment(new ConseilFragment()));
        cardStats.setOnClickListener(v     -> showFragment(new StatsFragment()));
    }

    /**
     * Affiche un fragment et cache le menu admin
     */
    private void showFragment(Fragment fragment) {
        headerLayout.setVisibility(View.GONE);
        gridScrollView.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    /**
     * Appelé par les fragments pour revenir au menu admin (bouton retour toolbar)
     */
    public void showAdminMenu() {
        headerLayout.setVisibility(View.VISIBLE);
        gridScrollView.setVisibility(View.VISIBLE);
        fragmentContainer.setVisibility(View.GONE);
    }

    /**
     * Bouton retour physique → dépiler le fragment et revenir au menu
     */
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            showAdminMenu();
        } else {
            super.onBackPressed();
        }
    }
}