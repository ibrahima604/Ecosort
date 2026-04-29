package com.example.ecosort;

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

    // --- Vues ---
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

    // --- Données ---
    private final List<ConseilResponse> conseilList = new ArrayList<>();
    private ConseilsAdapter adapter;

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

        // Liaison vues
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

        // RecyclerView
        adapter = new ConseilsAdapter(conseilList, this::onDeleteConseil);
        recyclerConseils.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerConseils.setAdapter(adapter);

        // Afficher le formulaire
        btnAddConseil.setOnClickListener(v -> showForm());

        // Annuler → cacher formulaire
        btnAnnuler.setOnClickListener(v -> hideForm());

        // Sauvegarder → valider + envoyer API
        btnSauvegarder.setOnClickListener(v -> soumettreConseil());

        // Charger la liste
        chargerConseils();
    }

    // ─────────────────────────────────────────────
    //  Formulaire
    // ─────────────────────────────────────────────

    private void showForm() {
        cardForm.setVisibility(View.VISIBLE);
        btnAddConseil.setVisibility(View.GONE);
    }

    private void hideForm() {
        cardForm.setVisibility(View.GONE);
        btnAddConseil.setVisibility(View.VISIBLE);
        etTitre.setText("");
        etDescription.setText("");
        layoutTitre.setError(null);
        layoutDescription.setError(null);
    }

    private void soumettreConseil() {
        String titre       = etTitre.getText()       != null ? etTitre.getText().toString().trim()       : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        // Validation
        boolean valid = true;
        if (TextUtils.isEmpty(titre)) {
            layoutTitre.setError("Le titre est obligatoire");
            valid = false;
        } else {
            layoutTitre.setError(null);
        }

        if (TextUtils.isEmpty(description)) {
            layoutDescription.setError("La description est obligatoire");
            valid = false;
        } else {
            layoutDescription.setError(null);
        }

        if (!valid) return;

        btnSauvegarder.setEnabled(false);
        btnSauvegarder.setText("Envoi…");

        ConseilRequest request = new ConseilRequest(titre, description);

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

    // ─────────────────────────────────────────────
    //  Chargement liste
    // ─────────────────────────────────────────────

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

    // ─────────────────────────────────────────────
    //  Suppression
    // ─────────────────────────────────────────────

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
                                        "Conseil supprimé", Toast.LENGTH_SHORT).show();
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

    // ─────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────

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