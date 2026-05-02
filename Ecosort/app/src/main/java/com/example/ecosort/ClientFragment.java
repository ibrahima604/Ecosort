package com.example.ecosort;

import android.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClientFragment extends Fragment {

    private RecyclerView  recyclerClients;
    private LinearLayout  emptyState;

    private final List<UserResponse> clientList = new ArrayList<>();
    private ClientsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerClients = view.findViewById(R.id.recycler_clients);
        emptyState      = view.findViewById(R.id.empty_state);

        adapter = new ClientsAdapter(clientList, this::onEditClient, this::onDeleteClient);
        recyclerClients.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerClients.setAdapter(adapter);

        chargerClients();
    }

    private void chargerClients() {
        RetrofitClient.getApiService().getAllUsers()
                .enqueue(new Callback<List<UserResponse>>() {
                    @Override
                    public void onResponse(Call<List<UserResponse>> call,
                                           Response<List<UserResponse>> response) {
                        requireActivity().runOnUiThread(() -> {
                            if (response.isSuccessful() && response.body() != null) {
                                clientList.clear();
                                clientList.addAll(response.body());
                                adapter.notifyDataSetChanged();
                                updateEmptyState();
                            }
                        });
                    }
                    @Override
                    public void onFailure(Call<List<UserResponse>> call, Throwable t) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(),
                                        "Erreur chargement", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void onDeleteClient(UserResponse user, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer")
                .setMessage("Supprimer " + user.getNom() + " " + user.getPrenom() + " ?")
                .setPositiveButton("Supprimer", (d, w) -> {
                    RetrofitClient.getApiService().deleteUser(user.getIdClient())
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    requireActivity().runOnUiThread(() -> {
                                        if (response.isSuccessful()) {
                                            clientList.remove(position);
                                            adapter.notifyItemRemoved(position);
                                            updateEmptyState();
                                            Toast.makeText(requireContext(),
                                                    "Utilisateur supprimé 🗑️",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(requireContext(),
                                                    "Erreur suppression",
                                                    Toast.LENGTH_SHORT).show();
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
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void onEditClient(UserResponse user, int position) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_client, null);

        com.google.android.material.textfield.TextInputEditText etNom    =
                dialogView.findViewById(R.id.et_nom);
        com.google.android.material.textfield.TextInputEditText etPrenom =
                dialogView.findViewById(R.id.et_prenom);
        com.google.android.material.textfield.TextInputEditText etEmail  =
                dialogView.findViewById(R.id.et_email);

        etNom.setText(user.getNom());
        etPrenom.setText(user.getPrenom());
        etEmail.setText(user.getEmail());

        new AlertDialog.Builder(requireContext())
                .setTitle("Modifier l'utilisateur")
                .setView(dialogView)
                .setPositiveButton("Sauvegarder", (d, w) -> {
                    String nom    = etNom.getText()    != null ? etNom.getText().toString().trim()    : "";
                    String prenom = etPrenom.getText() != null ? etPrenom.getText().toString().trim() : "";
                    String email  = etEmail.getText()  != null ? etEmail.getText().toString().trim()  : "";

                    if (TextUtils.isEmpty(nom) || TextUtils.isEmpty(prenom) || TextUtils.isEmpty(email)) {
                        Toast.makeText(requireContext(), "Tous les champs sont obligatoires",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserRequest request = new UserRequest(nom, prenom, email);
                    RetrofitClient.getApiService().updateUser(user.getIdClient(), request)
                            .enqueue(new Callback<UserResponse>() {
                                @Override
                                public void onResponse(Call<UserResponse> call,
                                                       Response<UserResponse> response) {
                                    requireActivity().runOnUiThread(() -> {
                                        if (response.isSuccessful() && response.body() != null) {
                                            clientList.set(position, response.body());
                                            adapter.notifyItemChanged(position);
                                            Toast.makeText(requireContext(),
                                                    "Modifié ✅", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(requireContext(),
                                                    "Erreur serveur : " + response.code(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                @Override
                                public void onFailure(Call<UserResponse> call, Throwable t) {
                                    requireActivity().runOnUiThread(() ->
                                            Toast.makeText(requireContext(),
                                                    "Erreur réseau", Toast.LENGTH_SHORT).show());
                                }
                            });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void updateEmptyState() {
        if (clientList.isEmpty()) {
            recyclerClients.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerClients.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
}