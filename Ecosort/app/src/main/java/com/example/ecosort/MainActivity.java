package com.example.ecosort;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔹 Récupération des composants
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        // 🔹 Toolbar
        setSupportActionBar(toolbar);

        // 🔹 Bouton hamburger
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 🔹 Gestion des clics du menu
        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Toast.makeText(this, "Home cliqué", Toast.LENGTH_SHORT).show();
            }

            else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Profile cliqué", Toast.LENGTH_SHORT).show();
            }

            else if (id == R.id.nav_history) {
                Toast.makeText(this, "History cliqué", Toast.LENGTH_SHORT).show();
            }

            else if (id == R.id.nav_stats) {
                Toast.makeText(this, "Stats cliqué", Toast.LENGTH_SHORT).show();
            }

            else if (id == R.id.nav_tips) {
                Toast.makeText(this, "Tips cliqué", Toast.LENGTH_SHORT).show();
            }

            else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Settings cliqué", Toast.LENGTH_SHORT).show();
            }

            // 🔹 fermer le drawer après clic
            drawerLayout.closeDrawer(GravityCompat.START);

            return true;
        });
    }

    // 🔹 bouton back ferme le menu
    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        else {
            super.onBackPressed();
        }
    }
    
}