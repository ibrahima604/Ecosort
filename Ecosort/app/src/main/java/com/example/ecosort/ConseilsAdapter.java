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

    public interface OnEditListener {
        void onEdit(ConseilResponse conseil);
    }

    private final List<ConseilResponse> items;
    private final OnDeleteListener      deleteListener;
    private final OnEditListener        editListener;

    public ConseilsAdapter(List<ConseilResponse> items,
                           OnDeleteListener deleteListener,
                           OnEditListener editListener) {
        this.items          = items;
        this.deleteListener = deleteListener;
        this.editListener   = editListener;
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
        holder.btnModifier.setOnClickListener(v ->
                editListener.onEdit(conseil));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView       tvTitre, tvDescription;
        MaterialButton btnSupprimer, btnModifier;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitre       = itemView.findViewById(R.id.tv_titre);
            tvDescription = itemView.findViewById(R.id.tv_description);
            btnSupprimer  = itemView.findViewById(R.id.btn_supprimer);
            btnModifier   = itemView.findViewById(R.id.btn_modifier);
        }
    }
}