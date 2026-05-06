package com.example.ecosort;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileBottomSheet extends BottomSheetDialogFragment {

    private static final String PREFS_NAME = "ecosort_prefs";
    private static final String KEY_EMAIL  = "user_email";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // On inflate les deux layouts dans un conteneur :
        // - dialog_edit_profile (formulaire)
        // - dialog_delete_account (suppression)
        // Pour garder tout dans un seul BottomSheet, on utilise
        // un layout dédié fragment_profile_sheet.xml (à créer ci-dessous)
        return inflater.inflate(R.layout.fragment_profile_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        String email = prefs.getString(KEY_EMAIL, "");

        // ── Champs du formulaire ──────────────────────────────
        TextInputEditText etNom    = view.findViewById(R.id.etNom);
        TextInputEditText etPrenom = view.findViewById(R.id.etPrenom);
        TextInputEditText etEmail  = view.findViewById(R.id.etEmail);

        etEmail.setText(email);

        // Pré-remplir nom/prénom depuis l'API
        if (!email.isEmpty()) {
            RetrofitClient.getApiService().getUserByEmail(email)
                    .enqueue(new Callback<UserResponse>() {
                        @Override
                        public void onResponse(Call<UserResponse> c,
                                               Response<UserResponse> r) {
                            if (r.isSuccessful() && r.body() != null && isAdded()) {
                                requireActivity().runOnUiThread(() -> {
                                    etNom.setText(r.body().getNom());
                                    etPrenom.setText(r.body().getPrenom());
                                    // On stocke l'id pour PUT/DELETE
                                    view.setTag(r.body().getIdClient());
                                });
                            }
                        }
                        @Override
                        public void onFailure(Call<UserResponse> c, Throwable t) {}
                    });
        }

        // ── Bouton Enregistrer ────────────────────────────────
        MaterialButton btnSave = view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            String nom    = etNom.getText()    != null ? etNom.getText().toString().trim() : "";
            String prenom = etPrenom.getText() != null ? etPrenom.getText().toString().trim() : "";
            String id     = view.getTag() != null ? view.getTag().toString() : "";

            if (nom.isEmpty() || prenom.isEmpty() || id.isEmpty()) {
                Toast.makeText(requireContext(), "Champs invalides", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Mise à jour API centrale
            RetrofitClient.getApiService()
                    .updateUser(id, new UserRequest(nom, prenom, email))
                    .enqueue(new Callback<UserResponse>() {
                        @Override
                        public void onResponse(Call<UserResponse> c,
                                               Response<UserResponse> r) {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> {
                                // 2. Mise à jour base locale SQLite
                                updateLocalDb(nom, prenom, email);
                                Toast.makeText(requireContext(),
                                        "Profil mis à jour ✓", Toast.LENGTH_SHORT).show();
                                dismiss();
                            });
                        }
                        @Override
                        public void onFailure(Call<UserResponse> c, Throwable t) {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> {
                                // Réseau KO → on met à jour uniquement en local
                                updateLocalDb(nom, prenom, email);
                                Toast.makeText(requireContext(),
                                        "Mis à jour localement (pas de réseau)",
                                        Toast.LENGTH_SHORT).show();
                                dismiss();
                            });
                        }
                    });
        });

        // ── Bouton Annuler ────────────────────────────────────
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> dismiss());

        // ── Bouton Supprimer le compte ────────────────────────
        MaterialButton btnDelete = view.findViewById(R.id.btnConfirmerSuppression);
        btnDelete.setOnClickListener(v -> {
            String id = view.getTag() != null ? view.getTag().toString() : "";
            if (id.isEmpty()) {
                Toast.makeText(requireContext(), "ID introuvable", Toast.LENGTH_SHORT).show();
                return;
            }
            RetrofitClient.getApiService().deleteUser(id)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> c, Response<Void> r) {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> {
                                clearLocalData();
                                Toast.makeText(requireContext(),
                                        "Compte supprimé", Toast.LENGTH_SHORT).show();
                                dismiss();
                                // Retour à l'écran d'inscription
                                requireActivity().recreate();
                            });
                        }
                        @Override
                        public void onFailure(Call<Void> c, Throwable t) {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(),
                                            "Erreur réseau", Toast.LENGTH_SHORT).show());
                        }
                    });
        });

        // ── Bouton Annuler suppression ────────────────────────
        MaterialButton btnAnnulerDelete = view.findViewById(R.id.btnAnnulerSuppression);
        btnAnnulerDelete.setOnClickListener(v -> dismiss());
    }

    // ── Helpers ───────────────────────────────────────────────

    private void updateLocalDb(String nom, String prenom, String email) {
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put(DatabaseHelper.COL_USER_NOM,    nom);
        cv.put(DatabaseHelper.COL_USER_PRENOM, prenom);
        db.update(DatabaseHelper.TABLE_USERS, cv,
                DatabaseHelper.COL_USER_EMAIL + "=?",
                new String[]{email});
        db.close();
    }

    private void clearLocalData() {
        // Vide SharedPreferences
        requireActivity().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .edit().clear().apply();
        // Vide la table locale
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_USERS, null, null);
        db.close();
    }
}