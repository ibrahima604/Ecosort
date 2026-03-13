package com.example.ecosort;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import java.util.concurrent.ExecutionException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import com.google.common.util.concurrent.ListenableFuture;

public class MainActivity extends AppCompatActivity {
    ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

                if (isGranted) {

                    openCamera();

                } else {

                    Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();

                }

            });
    ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                            Toast.makeText(this, "Image sélectionnée", Toast.LENGTH_SHORT).show();

                        }

                    });
    PreviewView previewView;
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
        previewView = findViewById(R.id.previewView);
        Button btnUpload=findViewById(R.id.btnUpload);
        //Traitement de l'evenement pour uploader une image à partir du telephone par le bouton upload
        btnUpload.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");

            galleryLauncher.launch(intent);

        });

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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissionLauncher.launch(Manifest.permission.CAMERA);

        } else {

            openCamera();

        }
    }

    private void openCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            try {

                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview
                );

            }

            catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(this));

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