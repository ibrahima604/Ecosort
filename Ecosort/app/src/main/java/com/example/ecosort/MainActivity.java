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
import android.widget.FrameLayout;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG            = "MainActivity";
    private static final String PREFS_NAME     = "ecosort_prefs";
    private static final String KEY_REGISTERED = "user_registered";
    private static final String KEY_EMAIL      = "user_email";
    private static final String KEY_USER_ID    = "user_id";
    private static final String ADMIN_EMAIL    = "Admin604@gmail.com";

    PreviewView    previewView;
    DrawerLayout   drawerLayout;
    NavigationView navigationView;
    Toolbar        toolbar;

    private PlasticClassifier classifier;
    private MaterialButton     btnScan;

    // ── Conteneurs ────────────────────────────────────────────────────────────
    private FrameLayout fragmentContainer;
    private View        layoutHome;

    // =========================================================
    //  Launchers caméra / galerie
    // =========================================================

    ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(), isGranted -> {
                        if (isGranted) openCamera();
                        else Toast.makeText(this,
                                "Permission caméra refusée", Toast.LENGTH_SHORT).show();
                    });

    ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            try {
                                android.net.Uri uri = result.getData().getData();
                                Bitmap bmp = android.provider.MediaStore.Images.Media
                                        .getBitmap(getContentResolver(), uri);
                                analyzeAndShow(bmp);
                            } catch (Exception e) {
                                Log.e(TAG, "Erreur chargement galerie", e);
                                Toast.makeText(this,
                                        "Erreur chargement image", Toast.LENGTH_SHORT).show();
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

        drawerLayout      = findViewById(R.id.drawer_layout);
        navigationView    = findViewById(R.id.navigation_view);
        toolbar           = findViewById(R.id.toolbar);
        previewView       = findViewById(R.id.previewView);
        fragmentContainer = findViewById(R.id.fragment_container);
        layoutHome        = findViewById(R.id.layoutHome);

        // Bouton galerie
        MaterialButton btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        // Bouton scanner la frame caméra
        btnScan = findViewById(R.id.btnScan);
        if (btnScan != null) {
            btnScan.setOnClickListener(v -> {
                Bitmap frame = previewView.getBitmap();
                if (frame != null) analyzeAndShow(frame);
                else Toast.makeText(this,
                        "Caméra pas encore prête", Toast.LENGTH_SHORT).show();
            });
        }

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showHome();                                                      // ← retour accueil
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            if      (id == R.id.nav_profile)  new ProfileBottomSheet()
                    .show(getSupportFragmentManager(), "profile");
            else if (id == R.id.nav_stats)    startActivity(new Intent(this, StatsActivity.class));
            else if (id == R.id.nav_tips)     startActivity(new Intent(this, ConseillsActivity.class));
            else if (id == R.id.nav_history)  startActivity(new Intent(this, HistoriqueActivity.class));
            else if (id == R.id.nav_feedback) openFeedbackFragment();           // ← feedback
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Bouton retour : si fragment ouvert → revenir à l'accueil
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (fragmentContainer.getVisibility() == View.VISIBLE) {
                    showHome();                                                  // ← back depuis feedback
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Caméra
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        else openCamera();

        // Authentification
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isRegistered    = prefs.getBoolean(KEY_REGISTERED, false);
        String  savedEmail      = prefs.getString(KEY_EMAIL, "");

        if (!isRegistered) showUserInfoDialog();
        else               routeByEmail(savedEmail);

        syncUnsyncedDechets();
    }

    // =========================================================
    //  Navigation Feedback
    // =========================================================

    private void openFeedbackFragment() {
        fragmentContainer.setVisibility(View.VISIBLE);
        layoutHome.setVisibility(View.GONE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new FeedbackFragment())
                .addToBackStack("feedback")
                .commit();
    }

    private void showHome() {
        // Vide le back stack des fragments
        getSupportFragmentManager()
                .popBackStack(null,
                        androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentContainer.setVisibility(View.GONE);
        layoutHome.setVisibility(View.VISIBLE);
    }

    // =========================================================
    //  Analyse TFLite
    // =========================================================

    private void analyzeAndShow(Bitmap bitmap) {
        runOnUiThread(() -> {
            if (btnScan != null) btnScan.setEnabled(false);
            Toast.makeText(this, "Analyse en cours…", Toast.LENGTH_SHORT).show();
        });

        new Thread(() -> {
            float score  = classifier.predict(bitmap);
            PlasticClassifier.Result result = PlasticClassifier.interpret(score);
            runOnUiThread(() -> {
                if (btnScan != null) btnScan.setEnabled(true);
                if (result.type == PlasticClassifier.Type.PLASTIC ||
                        result.type == PlasticClassifier.Type.NOT_PLASTIC) {
                    saveDechet(result);
                }
                showResultDialog(result);
            });
        }).start();
    }

    // =========================================================
    //  Sauvegarde déchet — local PUIS Supabase
    // =========================================================

    private void saveDechet(PlasticClassifier.Result result) {
        String typeLabel = (result.type == PlasticClassifier.Type.PLASTIC)
                ? "Plastique" : "Non plastique";
        int typeId = (result.type == PlasticClassifier.Type.PLASTIC) ? 1 : 2;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userEmail = prefs.getString(KEY_EMAIL,   "");
        String userId    = prefs.getString(KEY_USER_ID, "");

        DatabaseHelper db      = new DatabaseHelper(this);
        long           localId = db.insertDechet(typeLabel, userEmail);

        if (!userId.isEmpty()) {
            postDechetToApi(typeId, userId, localId);
        } else {
            RetrofitClient.getApiService().getUserByEmail(userEmail)
                    .enqueue(new Callback<UserResponse>() {
                        @Override public void onResponse(Call<UserResponse> call,
                                                         Response<UserResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String id = response.body().getIdClient();
                                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                        .edit().putString(KEY_USER_ID, id).apply();
                                postDechetToApi(typeId, id, localId);
                            }
                        }
                        @Override public void onFailure(Call<UserResponse> c, Throwable t) {
                            Log.w(TAG, "getUserByEmail offline — déchet sauvé localement");
                        }
                    });
        }
    }

    private void postDechetToApi(int typeId, String userId, long localId) {
        String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(new Date());
        DechetRequest req = new DechetRequest(typeId, userId, date);

        RetrofitClient.getApiService().createDechet(req)
                .enqueue(new Callback<DechetResponse>() {
                    @Override public void onResponse(Call<DechetResponse> call,
                                                     Response<DechetResponse> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Déchet enregistré sur Supabase ✅");
                            new DatabaseHelper(MainActivity.this).markDechetSynced(localId);
                        } else {
                            Log.w(TAG, "Erreur API déchet : " + response.code());
                        }
                    }
                    @Override public void onFailure(Call<DechetResponse> c, Throwable t) {
                        Log.w(TAG, "postDechetToApi offline — sera synchronisé plus tard");
                    }
                });
    }

    private void syncUnsyncedDechets() {
        String userId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_USER_ID, "");
        if (userId.isEmpty()) return;

        new Thread(() -> {
            DatabaseHelper db = new DatabaseHelper(this);
            List<Map<String, String>> pending = db.getUnsyncedDechets();
            for (Map<String, String> row : pending) {
                long   localId = Long.parseLong(row.get("id"));
                int    typeId  = Integer.parseInt(row.get("type_id"));
                postDechetToApi(typeId, userId, localId);
                Log.d(TAG, "Sync offline déchet id=" + localId);
            }
        }).start();
    }

    // =========================================================
    //  Dialog résultat + bouton Google Maps
    // =========================================================

    private void showResultDialog(PlasticClassifier.Result result) {
        if (result.type == PlasticClassifier.Type.ERROR) return;

        if (result.type == PlasticClassifier.Type.UNCERTAIN) {
            new AlertDialog.Builder(this)
                    .setTitle("Résultat incertain")
                    .setMessage(result.message +
                            "\n\nL'objet est mal cadré ou trop éloigné. Reprends la photo.")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("Réessayer", (d, w) -> d.dismiss())
                    .show();
            return;
        }

        boolean isPlastic = (result.type == PlasticClassifier.Type.PLASTIC);
        String titre   = isPlastic ? "♻️ Plastique détecté" : "🗑️ Non plastique";
        String message = result.message + "\n\nScore : " + result.percent + "%" +
                "\n\n✅ Déchet enregistré dans votre historique.";

        String mapsQuery = isPlastic
                ? "poubelle+de+recyclage+près+de+moi"
                : "poubelle+ordures+ménagères+près+de+moi";

        new AlertDialog.Builder(this)
                .setTitle(titre)
                .setMessage(message)
                .setIcon(isPlastic
                        ? android.R.drawable.ic_dialog_alert
                        : android.R.drawable.ic_dialog_info)
                .setPositiveButton("📍 Trouver une poubelle", (d, w) -> openMaps(mapsQuery))
                .setNegativeButton("Fermer", null)
                .show();
    }

    private void openMaps(String query) {
        android.net.Uri uri = android.net.Uri.parse("geo:0,0?q=" + query);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://www.google.com/maps/search/" + query)));
        }
    }

    // =========================================================
    //  Routage admin / user
    // =========================================================

    private void routeByEmail(String email) {
        if (ADMIN_EMAIL.equalsIgnoreCase(email)) {
            RetrofitClient.getApiService().getAdminByEmail(email)
                    .enqueue(new Callback<AdminResponse>() {
                        @Override public void onResponse(Call<AdminResponse> call,
                                                         Response<AdminResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                                        .putString("admin_id", response.body().getIdClient())
                                        .apply();
                            }
                            startActivity(new Intent(MainActivity.this, AdminActivity.class));
                            finish();
                        }
                        @Override public void onFailure(Call<AdminResponse> c, Throwable t) {
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
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(
                            android.graphics.Color.TRANSPARENT));

        btnValider.setOnClickListener(v -> {
            String nom    = etNom.getText()    != null ? etNom.getText().toString().trim()    : "";
            String prenom = etPrenom.getText() != null ? etPrenom.getText().toString().trim() : "";
            String email  = etEmail.getText()  != null ? etEmail.getText().toString().trim()  : "";

            boolean valid = true;
            if (TextUtils.isEmpty(nom))    { layoutNom.setError("Obligatoire");     valid = false; }
            else                             layoutNom.setError(null);
            if (TextUtils.isEmpty(prenom)) { layoutPrenom.setError("Obligatoire");  valid = false; }
            else                             layoutPrenom.setError(null);
            if (TextUtils.isEmpty(email))  { layoutEmail.setError("Obligatoire");   valid = false; }
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            { layoutEmail.setError("Email invalide"); valid = false; }
            else                             layoutEmail.setError(null);
            if (!valid) return;

            btnValider.setEnabled(false);
            btnValider.setText("Vérification…");

            RetrofitClient.getApiService().getUserByEmail(email)
                    .enqueue(new Callback<UserResponse>() {
                        @Override public void onResponse(Call<UserResponse> call,
                                                         Response<UserResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                UserResponse existing = response.body();
                                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                                        .putString(KEY_USER_ID, existing.getIdClient()).apply();
                                saveUserLocally(existing.getNom(), existing.getPrenom(),
                                        existing.getEmail(), dialog);
                            } else {
                                createUser(nom, prenom, email, dialog, btnValider, layoutEmail);
                            }
                        }
                        @Override public void onFailure(Call<UserResponse> c, Throwable t) {
                            saveOffline(nom, prenom, email, dialog);
                        }
                    });
        });
        dialog.show();
    }

    private void createUser(String nom, String prenom, String email,
                            AlertDialog dialog, MaterialButton btn,
                            TextInputLayout layoutEmail) {
        RetrofitClient.getApiService().createUser(new UserRequest(nom, prenom, email))
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            RetrofitClient.getApiService().getUserByEmail(email)
                                    .enqueue(new Callback<UserResponse>() {
                                        @Override public void onResponse(Call<UserResponse> c2,
                                                                         Response<UserResponse> r2) {
                                            if (r2.isSuccessful() && r2.body() != null)
                                                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                                        .edit().putString(KEY_USER_ID,
                                                                r2.body().getIdClient()).apply();
                                        }
                                        @Override public void onFailure(Call<UserResponse> c2,
                                                                        Throwable t) {}
                                    });
                            saveUserLocally(nom, prenom, email, dialog);
                        } else if (response.code() == 409) {
                            runOnUiThread(() -> {
                                layoutEmail.setError("Cet email est déjà utilisé");
                                btn.setEnabled(true);
                                btn.setText("Commencer");
                            });
                        } else {
                            saveUserLocally(nom, prenom, email, dialog);
                        }
                    }
                    @Override public void onFailure(Call<Void> c, Throwable t) {
                        saveOffline(nom, prenom, email, dialog);
                    }
                });
    }

    private void saveUserLocally(String nom, String prenom, String email, AlertDialog dialog) {
        new DatabaseHelper(this).insertUser(nom, prenom, email);
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

    private void saveOffline(String nom, String prenom, String email, AlertDialog dialog) {
        new DatabaseHelper(this).insertUser(nom, prenom, email);
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putBoolean(KEY_REGISTERED, true)
                .putString(KEY_EMAIL, email)
                .apply();
        runOnUiThread(() -> {
            dialog.dismiss();
            Toast.makeText(this, "Sauvegardé localement (pas de réseau)",
                    Toast.LENGTH_SHORT).show();
            routeByEmail(email);
        });
    }

    // =========================================================
    //  Caméra
    // =========================================================

    private void openCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                provider.unbindAll();
                provider.bindToLifecycle(
                        this, CameraSelector.DEFAULT_BACK_CAMERA, preview);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Erreur ouverture caméra", e);
                runOnUiThread(() -> Toast.makeText(this,
                        "Erreur caméra : " + e.getMessage(), Toast.LENGTH_SHORT).show());
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