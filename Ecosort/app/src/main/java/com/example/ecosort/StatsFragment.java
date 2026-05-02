package com.example.ecosort;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatsFragment extends Fragment {

    private BarChart barChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        setupToolbar(view);
        barChart = view.findViewById(R.id.chart_bar);
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

    private void loadStats() {
        RetrofitClient.getApiService().getAdminStats()
                .enqueue(new Callback<Map<String, Long>>() {

                    @Override
                    public void onResponse(@NonNull Call<Map<String, Long>> call,
                                           @NonNull Response<Map<String, Long>> response) {
                        if (!isAdded() || response.body() == null) return;

                        Map<String, Long> stats = response.body();
                        long users    = getLong(stats, "totalUsers");
                        long dechets  = getLong(stats, "totalDechets");
                        long conseils = getLong(stats, "totalConseils");

                        requireActivity().runOnUiThread(() ->
                                setupBarChart(users, dechets, conseils));
                    }

                    @Override
                    public void onFailure(@NonNull Call<Map<String, Long>> call,
                                          @NonNull Throwable t) {
                        // Chart reste vide si pas de réseau
                    }
                });
    }

    private void setupBarChart(long users, long dechets, long conseils) {
        // ---- Entrées ----
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, users));
        entries.add(new BarEntry(1f, dechets));
        entries.add(new BarEntry(2f, conseils));

        // ---- Dataset ----
        BarDataSet dataSet = new BarDataSet(entries, "EcoSort Stats");
        dataSet.setColors(
                Color.parseColor("#2E7D32"),  // vert  → Clients
                Color.parseColor("#1565C0"),  // bleu  → Tris
                Color.parseColor("#E65100")   // orange → Conseils
        );
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.BLACK);
        // Affiche les valeurs en entiers (pas de décimales)
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        // ---- Data ----
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        // ---- Axe X ----
        String[] labels = {"👥 Clients", "♻️ Tris", "💡 Conseils"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.DKGRAY);

        // ---- Axe Y ----
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        barChart.getAxisRight().setEnabled(false);

        // ---- Style chart ----
        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.animateY(800);
        barChart.invalidate();
    }

    private long getLong(Map<String, Long> map, String key) {
        Long val = map.get(key);
        return val != null ? val : 0L;
    }
}