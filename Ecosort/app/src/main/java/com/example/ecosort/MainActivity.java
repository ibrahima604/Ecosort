package com.example.ecosort;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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

    private static final String TAG            = "MainActivity";
    private static final String PREFS_NAME     = "ecosort_prefs";
    private static final String KEY_REGISTERED = "user_registered";
    private static final String KEY_EMAIL      = "user_email";
    private static final String ADMIN_EMAIL    = "Admin604@gmail.com";

    PreviewView    previewView;
    DrawerLayout   drawerLayout;
    NavigationView navigationView;
    Toolbar        toolbar;

    private PlasticClassifier classifier;
    private MaterialButton    btnScan;

    // =========================================================
    //  Launchers
    // =========================================================

    ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openCamera();
                else Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
            });

    ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        android.net.Uri uri = result.getData().getData();
                        Bitmap bmp = android.provider.MediaStore.Images.Media
                                .getBitmap(getContentResolver(), uri);
                        analyzeAndShow(bmp);
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur chargement image galerie", e);
                        Toast.makeText(this, "Erreur chargement image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // =========================================================
    //  onCreate
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RetrofitClient.init(this);

        classifier = new PlasticClassifier(this);

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

        btnScan = findViewById(R.id.btnScan);
        if (btnScan != null) {
            btnScan.setOnClickListener(v -> {
                Bitmap frame = previewView.getBitmap();
                if (frame != null) {
                    analyzeAndShow(frame);
                } else {
                    Toast.makeText(this, "Caméra pas encore prête", Toast.LENGTH_SHORT).show();
                }
            });
        }

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // ─── Navigation : chaque lien du navbar branché ───────────────────────
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Deja sur l'accueil, on ferme le drawer
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            if (id == R.id.nav_profile) {
                // BottomSheet profil deja configure
                new ProfileBottomSheet()
                        .show(getSupportFragmentManager(), "profile");

            } else if (id == R.id.nav_stats) {
                // Statistiques de l'utilisateur
                startActivity(new Intent(this, StatsActivity.class));

            } else if (id == R.id.nav_tips) {
                // Conseils de tri
                startActivity(new Intent(this, ConseillsActivity.class));

            } else if (id == R.id.nav_history) {
                // Historique des scans
                startActivity(new Intent(this, HistoriqueActivity.class));

            } else if (id == R.id.nav_settings) {
                // Parametres - a implementer plus tard
                Toast.makeText(this, "Paramètres (bientôt)", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        // ──────────────────────────────────────────────────────────────────────

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            openCamera();
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isRegistered    = prefs.getBoolean(KEY_REGISTERED, false);
        String  savedEmail      = prefs.getString(KEY_EMAIL, "");

        if (!isRegistered) {
            showUserInfoDialog();
        } else {
            routeByEmail(savedEmail);
        }
    }

    // =========================================================
    //  Analyse TFLite + Dialog resultat
    // =========================================================

    private void analyzeAndShow(Bitmap bitmap) {
        runOnUiThread(() -> {
            if (btnScan != null) btnScan.setEnabled(false);
            Toast.makeText(this, "Analyse en cours…", Toast.LENGTH_SHORT).show();
        });

        new Thread(() -> {
            float score = classifier.predict(bitmap);
            PlasticClassifier.Result result = PlasticClassifier.interpret(score);

            runOnUiThread(() -> {
                if (btnScan != null) btnScan.setEnabled(true);
                showResultDialog(result);
            });
        }).start();
    }

    private void showResultDialog(PlasticClassifier.Result result) {
        int iconRes;
        String titre;
        switch (result.type) {
            case PLASTIC:
                iconRes = android.R.drawable.ic_dialog_alert;
                titre   = "Plastique détecté";
                break;
            case UNCERTAIN:
                iconRes = android.R.drawable.ic_dialog_info;
                titre   = "Résultat incertain";
                break;
            case NOT_PLASTIC:
                iconRes = android.R.drawable.ic_dialog_info;
                titre   = "Non plastique";
                break;
            default:
                iconRes = android.R.drawable.ic_dialog_alert;
                titre   = "Erreur";
                break;
        }

        String messageComplet = result.message + "\n\nScore : " + result.percent + "%";

        if (result.type == PlasticClassifier.Type.UNCERTAIN) {
            new AlertDialog.Builder(this)
                    .setTitle(titre)
                    .setMessage(messageComplet + "\n\nL'objet est mal cadré ou trop éloigné. Reprends la photo.")
                    .setIcon(iconRes)
                    .setPositiveButton("Réessayer", (d, w) -> d.dismiss())
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(titre)
                    .setMessage(messageComplet)
                    .setIcon(iconRes)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    // =========================================================
    //  Routage admin / user
    // =========================================================

    private void routeByEmail(String email) {
        if (ADMIN_EMAIL.equalsIgnoreCase(email)) {
            RetrofitClient.getApiService().getAdminByEmail(email)
                    .enqueue(new Callback<AdminResponse>() {
                        @Override
                        public void onResponse(Call<AdminResponse> call,
                                               Response<AdminResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                                        .putString("admin_id", response.body().getIdClient())
                                        .apply();
                            }
                            startActivity(new Intent(MainActivity.this, AdminActivity.class));
                            finish();
                        }

                        @Override
                        public void onFailure(Call<AdminResponse> call, Throwable t) {
                            startActivity(new Intent(MainActivity.this, AdminActivity.class));
                            finish();
                        }
                    });
        }
    }

    // =========================================================
    //  Dialog inscription
    // =========================================================

    private void showUserInfoDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_user_info, null);

        TextInputLayout   layoutNom    = dialogView.findViewById(R.id.layoutNom);
        TextInputLayout   layoutPrenom = dialogView.findViewById(R.id.layoutPrenom);
        TextInputLayout   layoutEmail  = dialogView.findViewById(R.id.layoutEmail);
        TextInputEditText etNom        = dialogView.findViewById(R.id.etNom);
        TextInputEditText etPrenom     = dialogView.findViewById(R.id.etPrenom);
        TextInputEditText etEmail      = dialogView.findViewById(R.id.etEmail);
        MaterialButton    btnValider   = dialogView.findViewById(R.id.btnValider);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnValider.setOnClickListener(v -> {
            String nom    = etNom.getText()    != null ? etNom.getText().toString().trim() : "";
            String prenom = etPrenom.getText() != null ? etPrenom.getText().toString().trim() : "";
            String email  = etEmail.getText()  != null ? etEmail.getText().toString().trim() : "";

            boolean valid = true;
            if (TextUtils.isEmpty(nom)) {
                layoutNom.setError("Le nom est obligatoire"); valid = false;
            } else { layoutNom.setError(null); }

            if (TextUtils.isEmpty(prenom)) {
                layoutPrenom.setError("Le prénom est obligatoire"); valid = false;
            } else { layoutPrenom.setError(null); }

            if (TextUtils.isEmpty(email)) {
                layoutEmail.setError("L'email est obligatoire"); valid = false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.setError("Email invalide"); valid = false;
            } else { layoutEmail.setError(null); }

            if (!valid) return;

            btnValider.setEnabled(false);
            btnValider.setText("Vérification…");

            RetrofitClient.getApiService().getUserByEmail(email)
                    .enqueue(new Callback<UserResponse>() {
                        @Override
                        public void onResponse(Call<UserResponse> call,
                                               Response<UserResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                UserResponse existing = response.body();
                                saveUserLocally(existing.getNom(), existing.getPrenom(),
                                        existing.getEmail(), dialog);
                            } else {
                                createUser(nom, prenom, email, dialog, btnValider, layoutEmail);
                            }
                        }

                        @Override
                        public void onFailure(Call<UserResponse> call, Throwable t) {
                            Log.e(TAG, "Erreur réseau getUserByEmail", t);
                            saveOffline(nom, prenom, email, dialog);
                        }
                    });
        });

        dialog.show();
    }

    private void createUser(String nom, String prenom, String email,
                            AlertDialog dialog, MaterialButton btn,
                            TextInputLayout layoutEmail) {
        UserRequest userRequest = new UserRequest(nom, prenom, email);
        RetrofitClient.getApiService().createUser(userRequest)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            saveUserLocally(nom, prenom, email, dialog);
                        } else if (response.code() == 409) {
                            runOnUiThread(() -> {
                                layoutEmail.setError("Cet email est déjà utilisé");
                                btn.setEnabled(true);
                                btn.setText("Commencer");
                            });
                        } else {
                            Log.w(TAG, "createUser code inattendu : " + response.code());
                            saveUserLocally(nom, prenom, email, dialog);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Erreur réseau createUser", t);
                        saveOffline(nom, prenom, email, dialog);
                    }
                });
    }

    private void saveUserLocally(String nom, String prenom,
                                 String email, AlertDialog dialog) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.insertUser(nom, prenom, email);

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putBoolean(KEY_REGISTERED, true)
                .putString(KEY_EMAIL, email)
                .apply();

        runOnUiThread(() -> {
            dialog.dismiss();
            Toast.makeText(this, "Bienvenue " + prenom + " !", Toast.LENGTH_LONG).show();
            routeByEmail(email);
        });
    }

    private void saveOffline(String nom, String prenom,
                             String email, AlertDialog dialog) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.insertUser(nom, prenom, email);

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putBoolean(KEY_REGISTERED, true)
                .putString(KEY_EMAIL, email)
                .apply();

        runOnUiThread(() -> {
            dialog.dismiss();
            Toast.makeText(this, "Sauvegardé localement (pas de réseau)", Toast.LENGTH_SHORT).show();
            routeByEmail(email);
        });
    }

    // =========================================================
    //  Camera
    // =========================================================

    private void openCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, CameraSelector.DEFAULT_BACK_CAMERA, preview);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Erreur ouverture caméra", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Erreur caméra : " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // =========================================================
    //  Cycle de vie
    // =========================================================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (classifier != null) classifier.close();
    }
}