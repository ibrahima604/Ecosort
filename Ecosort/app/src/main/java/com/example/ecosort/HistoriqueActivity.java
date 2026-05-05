package com.example.ecosort;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoriqueActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ecosort_prefs";
    private static final String KEY_EMAIL  = "user_email";

    private View         layoutLoading;
    private View         emptyView;
    private RecyclerView recyclerHistorique;
    private HistoriqueAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mon historique");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        layoutLoading      = findViewById(R.id.layoutLoading);
        emptyView          = findViewById(R.id.emptyView);
        recyclerHistorique = findViewById(R.id.recyclerHistorique);

        recyclerHistorique.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoriqueAdapter(new ArrayList<>());
        recyclerHistorique.setAdapter(adapter);

        String email = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_EMAIL, "");

        if (email.isEmpty()) {
            showEmpty();
            return;
        }

        showLoading();

        RetrofitClient.getApiService().getUserByEmail(email)
                .enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call,
                                           Response<UserResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            chargerHistorique(response.body().getIdClient());
                        } else {
                            showEmpty();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable t) {
                        showEmpty();
                    }
                });
    }

    private void chargerHistorique(String idClient) {
        final List<DechetResponse>[]   dechetsHolder = new List[1];
        final List<TypeDechetResponse>[] typesHolder = new List[1];
        final int[] done = {0};

        RetrofitClient.getApiService().getDechetsByUser(idClient)
                .enqueue(new Callback<List<DechetResponse>>() {
                    @Override
                    public void onResponse(Call<List<DechetResponse>> call,
                                           Response<List<DechetResponse>> response) {
                        dechetsHolder[0] = (response.isSuccessful() && response.body() != null)
                                ? response.body() : new ArrayList<>();
                        if (++done[0] == 2) assembler(dechetsHolder[0], typesHolder[0]);
                    }

                    @Override
                    public void onFailure(Call<List<DechetResponse>> call, Throwable t) {
                        dechetsHolder[0] = new ArrayList<>();
                        if (++done[0] == 2) assembler(dechetsHolder[0], typesHolder[0]);
                    }
                });

        RetrofitClient.getApiService().getAllTypesDechets()
                .enqueue(new Callback<List<TypeDechetResponse>>() {
                    @Override
                    public void onResponse(Call<List<TypeDechetResponse>> call,
                                           Response<List<TypeDechetResponse>> response) {
                        typesHolder[0] = (response.isSuccessful() && response.body() != null)
                                ? response.body() : new ArrayList<>();
                        if (++done[0] == 2) assembler(dechetsHolder[0], typesHolder[0]);
                    }

                    @Override
                    public void onFailure(Call<List<TypeDechetResponse>> call, Throwable t) {
                        typesHolder[0] = new ArrayList<>();
                        if (++done[0] == 2) assembler(dechetsHolder[0], typesHolder[0]);
                    }
                });
    }

    private void assembler(List<DechetResponse> dechets, List<TypeDechetResponse> types) {
        runOnUiThread(() -> {
            if (dechets == null || dechets.isEmpty()) {
                showEmpty();
                return;
            }

            Map<Integer, String> labelMap = new HashMap<>();
            if (types != null) {
                for (TypeDechetResponse t : types) {
                    labelMap.put(t.getIdTypeDechet(), t.getEtiquette());
                }
            }

            List<HistoriqueItem> items = new ArrayList<>();
            for (DechetResponse d : dechets) {
                String label = labelMap.getOrDefault(d.getIdTypeDechet(), "Inconnu");
                items.add(new HistoriqueItem(label, d.getDateTri()));
            }

            // du plus recent au plus ancien
            Collections.reverse(items);

            adapter.update(items);
            showHistorique();
        });
    }

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        recyclerHistorique.setVisibility(View.GONE);
    }

    private void showHistorique() {
        layoutLoading.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        recyclerHistorique.setVisibility(View.VISIBLE);
    }

    private void showEmpty() {
        runOnUiThread(() -> {
            layoutLoading.setVisibility(View.GONE);
            recyclerHistorique.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        });
    }

    // Modele d'un item d'historique
    static class HistoriqueItem {
        String etiquette;
        String dateTri;

        HistoriqueItem(String etiquette, String dateTri) {
            this.etiquette = etiquette;
            this.dateTri   = dateTri != null ? dateTri : "";
        }

        // Formate "2024-12-25T14:30:00" en "25/12/2024 a 14:30"
        String dateFormatee() {
            try {
                if (dateTri.contains("T")) {
                    String[] parts  = dateTri.split("T");
                    String   date   = parts[0];
                    String   time   = parts[1].substring(0, 5);
                    String[] d      = date.split("-");
                    return d[2] + "/" + d[1] + "/" + d[0] + " a " + time;
                }
            } catch (Exception ignored) {}
            return dateTri;
        }

        boolean estPlastique() {
            return etiquette.toLowerCase().contains("plastique");
        }
    }

    // Adapter
    static class HistoriqueAdapter extends RecyclerView.Adapter<HistoriqueAdapter.VH> {

        private List<HistoriqueItem> items;

        HistoriqueAdapter(List<HistoriqueItem> items) {
            this.items = items;
        }

        void update(List<HistoriqueItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_historique, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            HistoriqueItem item = items.get(position);

            holder.tvEtiquette.setText(item.etiquette);
            holder.tvDate.setText(item.dateFormatee());

            if (item.estPlastique()) {
                holder.tvIndicateur.setText("Plastique");
                holder.tvIndicateur.setTextColor(
                        holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
                holder.tvIcone.setText("♻");
            } else {
                holder.tvIndicateur.setText("Non plastique");
                holder.tvIndicateur.setTextColor(
                        holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
                holder.tvIcone.setText("✓");
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvIcone;
            TextView tvEtiquette;
            TextView tvDate;
            TextView tvIndicateur;

            VH(@NonNull View itemView) {
                super(itemView);
                tvIcone      = itemView.findViewById(R.id.tvIconeHistorique);
                tvEtiquette  = itemView.findViewById(R.id.tvEtiquetteHistorique);
                tvDate       = itemView.findViewById(R.id.tvDateHistorique);
                tvIndicateur = itemView.findViewById(R.id.tvIndicateurHistorique);
            }
        }
    }
}