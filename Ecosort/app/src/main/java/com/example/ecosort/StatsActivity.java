package com.example.ecosort;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ecosort_prefs";
    private static final String KEY_EMAIL  = "user_email";

    private View         layoutLoading;
    private View         emptyView;
    private View         statsContainer;
    private TextView     tvEmailStats;
    private TextView     tvTotalScans;
    private TextView     tvTotalPlastique;
    private TextView     tvTotalNonPlastique;
    private RecyclerView recyclerTypesDechets;
    private TypeDechetStatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mes statistiques");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        layoutLoading        = findViewById(R.id.layoutLoading);
        emptyView            = findViewById(R.id.emptyView);
        statsContainer       = findViewById(R.id.statsContainer);
        tvEmailStats         = findViewById(R.id.tvEmailStats);
        tvTotalScans         = findViewById(R.id.tvTotalScans);
        tvTotalPlastique     = findViewById(R.id.tvTotalPlastique);
        tvTotalNonPlastique  = findViewById(R.id.tvTotalNonPlastique);
        recyclerTypesDechets = findViewById(R.id.recyclerTypesDechets);

        recyclerTypesDechets.setLayoutManager(new LinearLayoutManager(this));
        recyclerTypesDechets.setNestedScrollingEnabled(false);
        adapter = new TypeDechetStatAdapter(new ArrayList<>(), 0);
        recyclerTypesDechets.setAdapter(adapter);

        String email = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_EMAIL, "");
        tvEmailStats.setText(email);

        if (email.isEmpty()) { showEmpty(); return; }

        showLoading();
        RetrofitClient.getApiService().getUserByEmail(email)
                .enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            loadStats(response.body().getIdClient());
                        } else { showEmpty(); }
                    }
                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable t) { showEmpty(); }
                });
    }

    private void loadStats(String idClient) {
        final List<DechetResponse>[]    dechetsHolder = new List[1];
        final List<TypeDechetResponse>[]typesHolder   = new List[1];
        final int[]                     done          = {0};

        RetrofitClient.getApiService().getDechetsByUser(idClient)
                .enqueue(new Callback<List<DechetResponse>>() {
                    @Override public void onResponse(Call<List<DechetResponse>> c, Response<List<DechetResponse>> r) {
                        dechetsHolder[0] = (r.isSuccessful() && r.body() != null) ? r.body() : new ArrayList<>();
                        if (++done[0] == 2) buildStats(dechetsHolder[0], typesHolder[0]);
                    }
                    @Override public void onFailure(Call<List<DechetResponse>> c, Throwable t) {
                        dechetsHolder[0] = new ArrayList<>();
                        if (++done[0] == 2) buildStats(dechetsHolder[0], typesHolder[0]);
                    }
                });

        RetrofitClient.getApiService().getAllTypesDechets()
                .enqueue(new Callback<List<TypeDechetResponse>>() {
                    @Override public void onResponse(Call<List<TypeDechetResponse>> c, Response<List<TypeDechetResponse>> r) {
                        typesHolder[0] = (r.isSuccessful() && r.body() != null) ? r.body() : new ArrayList<>();
                        if (++done[0] == 2) buildStats(dechetsHolder[0], typesHolder[0]);
                    }
                    @Override public void onFailure(Call<List<TypeDechetResponse>> c, Throwable t) {
                        typesHolder[0] = new ArrayList<>();
                        if (++done[0] == 2) buildStats(dechetsHolder[0], typesHolder[0]);
                    }
                });
    }

    private void buildStats(List<DechetResponse> dechets, List<TypeDechetResponse> types) {
        runOnUiThread(() -> {
            if (dechets == null || dechets.isEmpty()) { showEmpty(); return; }

            int total = dechets.size();

            Map<Integer, String>  labelMap = new HashMap<>();
            if (types != null) for (TypeDechetResponse t : types)
                labelMap.put(t.getIdTypeDechet(), t.getEtiquette());

            Map<Integer, Integer> countMap = new HashMap<>();
            for (DechetResponse d : dechets)
                countMap.put(d.getIdTypeDechet(), countMap.getOrDefault(d.getIdTypeDechet(), 0) + 1);

            int totalPlastique = 0;
            for (Map.Entry<Integer, Integer> e : countMap.entrySet()) {
                String label = labelMap.getOrDefault(e.getKey(), "");
                if (label.toLowerCase().contains("plastique")) totalPlastique += e.getValue();
            }

            List<TypeStatItem> items = new ArrayList<>();
            for (Map.Entry<Integer, Integer> e : countMap.entrySet()) {
                String label = labelMap.getOrDefault(e.getKey(), "Type " + e.getKey());
                int    count = e.getValue();
                int    pct   = (int) Math.round((count * 100.0) / total);
                items.add(new TypeStatItem(label, count, pct));
            }
            items.sort((a, b) -> Integer.compare(b.count, a.count));

            tvTotalScans.setText(String.valueOf(total));
            tvTotalPlastique.setText(String.valueOf(totalPlastique));
            tvTotalNonPlastique.setText(String.valueOf(total - totalPlastique));
            adapter.update(items, total);
            showStats();
        });
    }

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        statsContainer.setVisibility(View.GONE);
    }
    private void showStats() {
        layoutLoading.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        statsContainer.setVisibility(View.VISIBLE);
    }
    private void showEmpty() {
        layoutLoading.setVisibility(View.GONE);
        statsContainer.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    // ── Modèle ────────────────────────────────────────────────

    static class TypeStatItem {
        String etiquette; int count, pourcentage;
        TypeStatItem(String e, int c, int p) { etiquette=e; count=c; pourcentage=p; }
    }

    // ── Adapter ───────────────────────────────────────────────

    static class TypeDechetStatAdapter extends RecyclerView.Adapter<TypeDechetStatAdapter.VH> {
        private List<TypeStatItem> items;
        private int total;

        TypeDechetStatAdapter(List<TypeStatItem> items, int total) { this.items=items; this.total=total; }

        void update(List<TypeStatItem> items, int total) {
            this.items=items; this.total=total; notifyDataSetChanged();
        }

        private static String emojiFor(String label) {
            String l = label.toLowerCase();
            if (l.contains("plastique"))                          return "🧴";
            if (l.contains("verre"))                              return "🍶";
            if (l.contains("papier")||l.contains("carton"))       return "📦";
            if (l.contains("métal")||l.contains("aluminium"))     return "🥫";
            if (l.contains("organique")||l.contains("alimentaire"))return "🍃";
            if (l.contains("électronique"))                        return "💻";
            if (l.contains("textile")||l.contains("vêtement"))    return "👕";
            return "🗑️";
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_dechet_stat,p,false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            TypeStatItem item = items.get(pos);
            h.icon.setText(emojiFor(item.etiquette));
            h.label.setText(item.etiquette);
            h.count.setText(String.valueOf(item.count));
            h.pct.setText(item.pourcentage + "% du total");
            h.bar.setProgressCompat(item.pourcentage, true);
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvIconType, label, count, pct;
            TextView icon;
            LinearProgressIndicator bar;
            VH(@NonNull View v) {
                super(v);
                icon  = v.findViewById(R.id.tvIconType);
                label = v.findViewById(R.id.tvEtiquetteType);
                count = v.findViewById(R.id.tvCompteur);
                pct   = v.findViewById(R.id.tvPourcentage);
                bar   = v.findViewById(R.id.progressType);
            }
        }
    }
}