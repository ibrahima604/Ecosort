package com.example.ecosort;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackFragment extends Fragment {

    private RatingBar    ratingBar;
    private Spinner      spinnerCategorie;
    private EditText     etCommentaire;
    private Button       btnEnvoyer;
    private LinearLayout layoutSuccess;
    private final String[] categories = {
            "Sélectionner une catégorie",
            "SUGGESTION",
            "BUG",
            "ERREUR_SCAN",
            "AUTRE"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        ratingBar        = view.findViewById(R.id.ratingBar);
        spinnerCategorie = view.findViewById(R.id.spinnerCategorie);
        etCommentaire    = view.findViewById(R.id.etCommentaire);
        btnEnvoyer       = view.findViewById(R.id.btnEnvoyer);
        layoutSuccess    = view.findViewById(R.id.layoutSuccess);

        setupSpinner();
        setupBoutonEnvoyer();

        return view;
    }

    //Spinner
    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategorie.setAdapter(adapter);
    }


    private void setupBoutonEnvoyer() {
        btnEnvoyer.setOnClickListener(v -> {

            int    note        = (int) ratingBar.getRating();
            String categorie   = spinnerCategorie.getSelectedItem().toString();
            String commentaire = etCommentaire.getText().toString().trim();

            if (note == 0) {
                Toast.makeText(getContext(),
                        "Veuillez donner une note", Toast.LENGTH_SHORT).show();
                return;
            }
            if (categorie.equals("Sélectionner une catégorie")) {
                Toast.makeText(getContext(),
                        "Veuillez choisir une catégorie", Toast.LENGTH_SHORT).show();
                return;
            }


            String idClient = requireActivity()
                    .getSharedPreferences("ecosort_prefs", 0)
                    .getString("user_id", null);

            if (idClient == null) {
                Toast.makeText(getContext(),
                        "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
                return;
            }

            FeedbackRequest request = new FeedbackRequest(
                    idClient, note, commentaire, categorie);

            envoyerFeedback(request);
        });
    }

    // Appel API
    private void envoyerFeedback(FeedbackRequest request) {

        btnEnvoyer.setEnabled(false);
        btnEnvoyer.setText("Envoi en cours...");


        RetrofitClient.getApiService().createFeedback(request)
                .enqueue(new Callback<FeedbackResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<FeedbackResponse> call,
                                           @NonNull Response<FeedbackResponse> response) {
                        if (response.isSuccessful()) {
                            afficherSucces();
                        } else {
                            Toast.makeText(getContext(),
                                    "Erreur serveur : " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                            resetBouton();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FeedbackResponse> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(getContext(),
                                "Erreur réseau : " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        resetBouton();
                    }
                });
    }

    // ── Succès ────────────────────────────────────────────────────────────────
    private void afficherSucces() {
        layoutSuccess.setVisibility(View.VISIBLE);
        btnEnvoyer.setVisibility(View.GONE);
        ratingBar.setIsIndicator(true);
        spinnerCategorie.setEnabled(false);
        etCommentaire.setEnabled(false);

        btnEnvoyer.postDelayed(this::resetFormulaire, 3000);
    }

    // ── Reset
    private void resetFormulaire() {
        ratingBar.setRating(0);
        ratingBar.setIsIndicator(false);
        spinnerCategorie.setSelection(0);
        spinnerCategorie.setEnabled(true);
        etCommentaire.setText("");
        etCommentaire.setEnabled(true);
        layoutSuccess.setVisibility(View.GONE);
        btnEnvoyer.setVisibility(View.VISIBLE);
        btnEnvoyer.setEnabled(true);
        btnEnvoyer.setText("Envoyer mon avis");
    }

    private void resetBouton() {
        btnEnvoyer.setEnabled(true);
        btnEnvoyer.setText("Envoyer mon avis");
    }
}