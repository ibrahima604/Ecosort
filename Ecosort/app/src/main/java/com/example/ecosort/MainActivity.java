package com.example.ecosort;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // Clé SharedPreferences — si elle existe, l'utilisateur est déjà enregistré
    private static final String PREFS_NAME    = "ecosort_prefs";
    private static final String KEY_REGISTERED = "user_registered";

    PreviewView previewView;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    // Launcher permission caméra
    ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openCamera();
                else Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
            });

    // Launcher galerie
    ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Toast.makeText(this, "Image sélectionnée", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout   = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar        = findViewById(R.id.toolbar);
        previewView    = findViewById(R.id.previewView);

        MaterialButton btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if      (id == R.id.nav_home)     Toast.makeText(this, "Accueil",     Toast.LENGTH_SHORT).show();
            else if (id == R.id.nav_profile)  Toast.makeText(this, "Profil",      Toast.LENGTH_SHORT).show();
            else if (id == R.id.nav_history)  Toast.makeText(this, "Historique",  Toast.LENGTH_SHORT).show();
            else if (id == R.id.nav_stats)    Toast.makeText(this, "Statistiques",Toast.LENGTH_SHORT).show();
            else if (id == R.id.nav_tips)     Toast.makeText(this, "Conseils",    Toast.LENGTH_SHORT).show();
            else if (id == R.id.nav_settings) Toast.makeText(this, "Paramètres",  Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Caméra
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            openCamera();
        }

        // Vérifier si c'est la première ouverture
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isRegistered = prefs.getBoolean(KEY_REGISTERED, false);

        if (!isRegistered) {
            showUserInfoDialog();
        }
    }

    /*
     * Affiche le popup de saisie des informations utilisateur.
     * Non annulable : l'utilisateur doit remplir le formulaire.
     */
    private void showUserInfoDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_user_info, null);

        TextInputLayout  layoutNom    = dialogView.findViewById(R.id.layoutNom);
        TextInputLayout  layoutPrenom = dialogView.findViewById(R.id.layoutPrenom);
        TextInputLayout  layoutEmail  = dialogView.findViewById(R.id.layoutEmail);
        TextInputEditText etNom       = dialogView.findViewById(R.id.etNom);
        TextInputEditText etPrenom    = dialogView.findViewById(R.id.etPrenom);
        TextInputEditText etEmail     = dialogView.findViewById(R.id.etEmail);
        MaterialButton    btnValider  = dialogView.findViewById(R.id.btnValider);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)  // impossible de fermer sans valider
                .create();

        // Arrondir les coins du dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.drawable.dialog_holo_light_frame);
        }

        btnValider.setOnClickListener(v -> {
            String nom    = etNom.getText()    != null ? etNom.getText().toString().trim()    : "";
            String prenom = etPrenom.getText() != null ? etPrenom.getText().toString().trim() : "";
            String email  = etEmail.getText()  != null ? etEmail.getText().toString().trim()  : "";

            // Validation des champs
            boolean valid = true;

            if (TextUtils.isEmpty(nom)) {
                layoutNom.setError("Le nom est obligatoire");
                valid = false;
            } else {
                layoutNom.setError(null);
            }

            if (TextUtils.isEmpty(prenom)) {
                layoutPrenom.setError("Le prénom est obligatoire");
                valid = false;
            } else {
                layoutPrenom.setError(null);
            }

            if (TextUtils.isEmpty(email)) {
                layoutEmail.setError("L'email est obligatoire");
                valid = false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.setError("Email invalide");
                valid = false;
            } else {
                layoutEmail.setError(null);
            }

            if (!valid) return;

            // Désactiver le bouton pendant le traitement
            btnValider.setEnabled(false);
            btnValider.setText("Enregistrement…");

            // 1. Sauvegarder en SQLite local
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            long localId = dbHelper.insertUser(nom, prenom, email);

            if (localId == -1) {
                Toast.makeText(this, "Erreur de sauvegarde locale", Toast.LENGTH_SHORT).show();
                btnValider.setEnabled(true);
                btnValider.setText("Commencer");
                return;
            }

            // 2. Appeler l'API Spring Boot pour enregistrer en backend
            UserRequest userRequest = new UserRequest(nom, prenom, email);
            RetrofitClient.getApiService().createUser(userRequest)
                    .enqueue(new Callback<Void>() {

                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                // Succès : marquer comme enregistré
                                saveRegistrationFlag();
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this,
                                        "Bienvenue " + prenom + " !", Toast.LENGTH_LONG).show();
                            } else {
                                // L'API a répondu mais avec une erreur (ex: email déjà existant)
                                // On sauvegarde quand même localement et on continue
                                saveRegistrationFlag();
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this,
                                        "Bienvenue " + prenom + " !", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            // Pas de connexion réseau : on sauvegarde quand même localement
                            // et on marque comme enregistré pour ne plus afficher le popup
                            saveRegistrationFlag();
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this,
                                    "Sauvegardé localement (pas de réseau)",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dialog.show();
    }

    // Enregistre la clé dans SharedPreferences pour ne plus afficher le popup
    private void saveRegistrationFlag() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_REGISTERED, true)
                .apply();
    }

    private void openCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}