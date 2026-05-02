package com.example.ecosort;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.*;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private TextView tvUsers, tvDechets, tvConseils, tvActivite;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        setupToolbar(view);
        bindViews(view);
        loadStats();

        return view;
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
            ((AdminActivity) requireActivity()).showAdminMenu();
        });
    }

    private void bindViews(View view) {
        tvUsers = view.findViewById(R.id.tv_total_users);
        tvDechets = view.findViewById(R.id.tv_total_dechets);
        tvConseils = view.findViewById(R.id.tv_total_conseils);
        tvActivite = view.findViewById(R.id.tv_activite);
    }

    private void loadStats() {
        RetrofitClient.getApiService().getAdminStats()
                .enqueue(new Callback<Map<String, Long>>() {

                    @Override
                    public void onResponse(@NonNull Call<Map<String, Long>> call,
                                           @NonNull Response<Map<String, Long>> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Long> stats = response.body();

                            long users = getOrDefault(stats, "totalUsers", 0L);
                            long dechets = getOrDefault(stats, "totalDechets", 0L);
                            long conseils = getOrDefault(stats, "totalConseils", 0L);

                            requireActivity().runOnUiThread(() -> {
                                tvUsers.setText(String.valueOf(users));
                                tvDechets.setText(String.valueOf(dechets));
                                tvConseils.setText(String.valueOf(conseils));
                                tvActivite.setText(
                                        "✅ " + users + " client(s) enregistré(s)\n" +
                                                "♻️ " + dechets + " tri(s) effectué(s)\n" +
                                                "💡 " + conseils + " conseil(s) publié(s)"
                                );
                            });

                        } else {
                            showError("Erreur serveur : " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Map<String, Long>> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        showError("Serveur inaccessible.\nVérifie que ngrok tourne.");
                    }
                });
    }

    // Map.getOrDefault() nécessite API 24+ → méthode manuelle pour compatibilité
    private long getOrDefault(Map<String, Long> map, String key, long defaultVal) {
        Long val = map.get(key);
        return val != null ? val : defaultVal;
    }

    private void showError(String msg) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            if (tvActivite != null) tvActivite.setText("⚠️ " + msg);
            if (tvUsers != null) tvUsers.setText("—");
            if (tvDechets != null) tvDechets.setText("—");
            if (tvConseils != null) tvConseils.setText("—");
        });
    }
}