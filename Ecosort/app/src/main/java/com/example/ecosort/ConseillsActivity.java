package com.example.ecosort;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConseillsActivity extends AppCompatActivity {

    private View         layoutLoading;
    private View         emptyView;
    private RecyclerView recyclerConseils;
    private ConseilAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conseils_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Conseils de tri");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        layoutLoading    = findViewById(R.id.layoutLoading);
        emptyView        = findViewById(R.id.emptyView);
        recyclerConseils = findViewById(R.id.recyclerConseils);

        recyclerConseils.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConseilAdapter(new ArrayList<>(), this::afficherPopup);
        recyclerConseils.setAdapter(adapter);

        chargerConseils();
    }

    private void afficherPopup(ConseilResponse conseil) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_conseil_detail);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvNumero        = dialog.findViewById(R.id.tvPopupNumero);
        TextView tvTitre         = dialog.findViewById(R.id.tvPopupTitre);
        TextView tvDescription   = dialog.findViewById(R.id.tvPopupDescription);
        MaterialButton btnFermer = dialog.findViewById(R.id.btnFermerPopup);

        int position = trouverPosition(conseil);
        tvNumero.setText(String.valueOf(position + 1));
        tvTitre.setText(conseil.getTitre());
        tvDescription.setText(conseil.getDescription());

        btnFermer.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private int trouverPosition(ConseilResponse conseil) {
        List<ConseilResponse> items = adapter.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getIdConseil() == conseil.getIdConseil()) return i;
        }
        return 0;
    }

    private void chargerConseils() {
        showLoading();
        RetrofitClient.getApiService().getAllConseils()
                .enqueue(new Callback<List<ConseilResponse>>() {
                    @Override
                    public void onResponse(Call<List<ConseilResponse>> call,
                                           Response<List<ConseilResponse>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {
                            showConseils(response.body());
                        } else {
                            showEmpty();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<ConseilResponse>> call, Throwable t) {
                        showEmpty();
                    }
                });
    }

    private void showLoading() {
        runOnUiThread(() -> {
            layoutLoading.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            recyclerConseils.setVisibility(View.GONE);
        });
    }

    private void showConseils(List<ConseilResponse> conseils) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            layoutLoading.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            recyclerConseils.setVisibility(View.VISIBLE);
            adapter.update(conseils);
        });
    }

    private void showEmpty() {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            layoutLoading.setVisibility(View.GONE);
            recyclerConseils.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        });
    }

    interface OnConseilClickListener {
        void onClick(ConseilResponse conseil);
    }

    static class ConseilAdapter extends RecyclerView.Adapter<ConseilAdapter.VH> {

        private List<ConseilResponse>        items;
        private final OnConseilClickListener clickListener;

        ConseilAdapter(List<ConseilResponse> items, OnConseilClickListener clickListener) {
            this.items         = items;
            this.clickListener = clickListener;
        }

        void update(List<ConseilResponse> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        List<ConseilResponse> getItems() { return items; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_conseils_user, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ConseilResponse conseil = items.get(position);
            holder.tvNumero.setText(String.valueOf(position + 1));
            holder.tvTitre.setText(conseil.getTitre());

            // Description tronquee dans la liste, complete dans le popup
            String desc = conseil.getDescription();
            holder.tvDescription.setText(
                    desc != null && desc.length() > 80
                            ? desc.substring(0, 80) + "…"
                            : desc);

            holder.itemView.setOnClickListener(v -> clickListener.onClick(conseil));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvNumero, tvTitre, tvDescription;

            VH(@NonNull View itemView) {
                super(itemView);
                tvNumero      = itemView.findViewById(R.id.tvNumeroConseil);
                tvTitre       = itemView.findViewById(R.id.tvTitreConseil);
                tvDescription = itemView.findViewById(R.id.tvDescriptionConseil);
            }
        }
    }
}