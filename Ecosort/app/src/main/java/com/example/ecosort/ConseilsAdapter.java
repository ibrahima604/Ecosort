package com.example.ecosort;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ConseilsAdapter extends RecyclerView.Adapter<ConseilsAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(ConseilResponse conseil, int position);
    }

    private final List<ConseilResponse> items;
    private final OnDeleteListener      deleteListener;

    public ConseilsAdapter(List<ConseilResponse> items, OnDeleteListener deleteListener) {
        this.items          = items;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conseil, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConseilResponse conseil = items.get(position);
        holder.tvTitre.setText(conseil.getTitre());
        holder.tvDescription.setText(conseil.getDescription());
        holder.btnSupprimer.setOnClickListener(v ->
                deleteListener.onDelete(conseil, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView       tvTitre, tvDescription;
        MaterialButton btnSupprimer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitre       = itemView.findViewById(R.id.tv_titre);
            tvDescription = itemView.findViewById(R.id.tv_description);
            btnSupprimer  = itemView.findViewById(R.id.btn_supprimer);
        }
    }
}