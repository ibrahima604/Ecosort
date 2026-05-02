package com.example.ecosort;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConseilFragment extends Fragment {

    private static final String PREFS_NAME = "ecosort_prefs";

    private View           cardForm;
    private MaterialButton btnAddConseil;
    private MaterialButton btnSauvegarder;
    private MaterialButton btnAnnuler;
    private TextInputLayout   layoutTitre;
    private TextInputLayout   layoutDescription;
    private TextInputEditText etTitre;
    private TextInputEditText etDescription;
    private RecyclerView      recyclerConseils;
    private LinearLayout      emptyState;

    private final List<ConseilResponse> conseilList = new ArrayList<>();
    private ConseilsAdapter adapter;

    // Pour savoir si on est en mode édition
    private boolean isEditing  = false;
    private int     editingId  = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conseil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardForm          = view.findViewById(R.id.card_form);
        btnAddConseil     = view.findViewById(R.id.btn_add_conseil);
        btnSauvegarder    = view.findViewById(R.id.btn_sauvegarder);
        btnAnnuler        = view.findViewById(R.id.btn_annuler);
        layoutTitre       = view.findViewById(R.id.layout_titre);
        layoutDescription = view.findViewById(R.id.layout_description);
        etTitre           = view.findViewById(R.id.et_titre);
        etDescription     = view.findViewById(R.id.et_description);
        recyclerConseils  = view.findViewById(R.id.recycler_conseils);
        emptyState        = view.findViewById(R.id.empty_state);

        // Adapter avec callback edit + delete
        adapter = new ConseilsAdapter(conseilList, this::onDeleteConseil, this::onEditConseil);
        recyclerConseils.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerConseils.setAdapter(adapter);

        btnAddConseil.setOnClickListener(v -> showForm(false, null));
        btnAnnuler.setOnClickListener(v -> hideForm());
        btnSauvegarder.setOnClickListener(v -> soumettreConseil());

        chargerConseils();
    }

    // ── Formulaire ──────────────────────────────────────────

    private void showForm(boolean editing, ConseilResponse conseil) {
        isEditing = editing;
        cardForm.setVisibility(View.VISIBLE);
        btnAddConseil.setVisibility(View.GONE);

        if (editing && conseil != null) {
            editingId = conseil.getIdConseil();
            etTitre.setText(conseil.getTitre());
            etDescription.setText(conseil.getDescription());
            btnSauvegarder.setText("✏️ Modifier");
        } else {
            editingId = -1;
            etTitre.setText("");
            etDescription.setText("");
            btnSauvegarder.setText("💾 Sauvegarder");
        }
    }

    private void hideForm() {
        cardForm.setVisibility(View.GONE);
        btnAddConseil.setVisibility(View.VISIBLE);
        etTitre.setText("");
        etDescription.setText("");
        layoutTitre.setError(null);
        layoutDescription.setError(null);
        isEditing = false;
        editingId = -1;
    }

    private void soumettreConseil() {
        String titre       = etTitre.getText()       != null ? etTitre.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        boolean valid = true;
        if (TextUtils.isEmpty(titre)) {
            layoutTitre.setError("Le titre est obligatoire"); valid = false;
        } else { layoutTitre.setError(null); }

        if (TextUtils.isEmpty(description)) {
            layoutDescription.setError("La description est obligatoire"); valid = false;
        } else { layoutDescription.setError(null); }

        if (!valid) return;

        btnSauvegarder.setEnabled(false);
        btnSauvegarder.setText("Envoi…");

        if (isEditing) {
            modifierConseil(titre, description);
        } else {
            creerConseil(titre, description);
        }
    }

    // ── Créer ───────────────────────────────────────────────

    private void creerConseil(String titre, String description) {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        String adminId = prefs.getString("admin_id", null); // String UUID !

        if (adminId == null) {
            Toast.makeText(requireContext(),
                    "Impossible d'identifier l'admin", Toast.LENGTH_SHORT).show();
            btnSauvegarder.setEnabled(true);
            btnSauvegarder.setText("💾 Sauvegarder");
            return;
        }

        ConseilRequest request = new ConseilRequest(titre, description, adminId);

        RetrofitClient.getApiService().createConseil(request)
                .enqueue(new Callback<ConseilResponse>() {
                    @Override
                    public void onResponse(Call<ConseilResponse> call,
                                           Response<ConseilResponse> response) {
                        requireActivity().runOnUiThread(() -> {
                            btnSauvegarder.setEnabled(true);
                            btnSauvegarder.setText("💾 Sauvegarder");
                            if (response.isSuccessful() && response.body() != null) {
                                conseilList.add(0, response.body());
                                adapter.notifyItemInserted(0);
                                recyclerConseils.scrollToPosition(0);
                                updateEmptyState();
                                hideForm();
                                Toast.makeText(requireContext(),
                                        "Conseil ajouté ✅", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(),
                                        "Erreur serveur : " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ConseilResponse> call, Throwable t) {
                        requireActivity().runOnUiThread(() -> {
                            btnSauvegarder.setEnabled(true);
                            btnSauvegarder.setText("💾 Sauvegarder");
                            Toast.makeText(requireContext(),
                                    "Erreur réseau : " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    // ── Modifier ────────────────────────────────────────────

    private void modifierConseil(String titre, String description) {
        ConseilRequest request = new ConseilRequest(titre, description, null);

        RetrofitClient.getApiService().updateConseil(editingId, request)
                .enqueue(new Callback<ConseilResponse>() {
                    @Override
                    public void onResponse(Call<ConseilResponse> call,
                                           Response<ConseilResponse> response) {
                        requireActivity().runOnUiThread(() -> {
                            btnSauvegarder.setEnabled(true);
                            btnSauvegarder.setText("✏️ Modifier");
                            if (response.isSuccessful() && response.body() != null) {
                                // Mettre à jour dans la liste
                                for (int i = 0; i < conseilList.size(); i++) {
                                    if (conseilList.get(i).getIdConseil() == editingId) {
                                        conseilList.set(i, response.body());
                                        adapter.notifyItemChanged(i);
                                        break;
                                    }
                                }
                                hideForm();
                                Toast.makeText(requireContext(),
                                        "Conseil modifié ✅", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(),
                                        "Erreur serveur : " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ConseilResponse> call, Throwable t) {
                        requireActivity().runOnUiThread(() -> {
                            btnSauvegarder.setEnabled(true);
                            btnSauvegarder.setText("✏️ Modifier");
                            Toast.makeText(requireContext(),
                                    "Erreur réseau : " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    // ── Charger ─────────────────────────────────────────────

    private void chargerConseils() {
        RetrofitClient.getApiService().getAllConseils()
                .enqueue(new Callback<List<ConseilResponse>>() {
                    @Override
                    public void onResponse(Call<List<ConseilResponse>> call,
                                           Response<List<ConseilResponse>> response) {
                        requireActivity().runOnUiThread(() -> {
                            if (response.isSuccessful() && response.body() != null) {
                                conseilList.clear();
                                conseilList.addAll(response.body());
                                adapter.notifyDataSetChanged();
                                updateEmptyState();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<ConseilResponse>> call, Throwable t) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(),
                                        "Impossible de charger les conseils",
                                        Toast.LENGTH_SHORT).show());
                    }
                });
    }

    // ── Supprimer ───────────────────────────────────────────

    private void onDeleteConseil(ConseilResponse conseil, int position) {
        RetrofitClient.getApiService().deleteConseil(conseil.getIdConseil())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        requireActivity().runOnUiThread(() -> {
                            if (response.isSuccessful()) {
                                conseilList.remove(position);
                                adapter.notifyItemRemoved(position);
                                updateEmptyState();
                                Toast.makeText(requireContext(),
                                        "Conseil supprimé 🗑️", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(),
                                        "Erreur suppression", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(),
                                        "Erreur réseau", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    // ── Éditer ──────────────────────────────────────────────

    private void onEditConseil(ConseilResponse conseil) {
        showForm(true, conseil);
    }

    // ── Helper ──────────────────────────────────────────────

    private void updateEmptyState() {
        if (conseilList.isEmpty()) {
            recyclerConseils.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerConseils.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
}