package com.example.ecosort;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ClientsAdapter extends RecyclerView.Adapter<ClientsAdapter.ViewHolder> {

    public interface OnEditListener   { void onEdit(UserResponse user, int position); }
    public interface OnDeleteListener { void onDelete(UserResponse user, int position); }

    private final List<UserResponse> items;
    private final OnEditListener     editListener;
    private final OnDeleteListener   deleteListener;

    public ClientsAdapter(List<UserResponse> items,
                          OnEditListener editListener,
                          OnDeleteListener deleteListener) {
        this.items          = items;
        this.editListener   = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserResponse user = items.get(position);
        holder.tvNom.setText(user.getNom() + " " + user.getPrenom());
        holder.tvEmail.setText(user.getEmail());
        holder.btnModifier.setOnClickListener(v ->
                editListener.onEdit(user, holder.getAdapterPosition()));
        holder.btnSupprimer.setOnClickListener(v ->
                deleteListener.onDelete(user, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView       tvNom, tvEmail;
        MaterialButton btnModifier, btnSupprimer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNom        = itemView.findViewById(R.id.tv_nom);
            tvEmail      = itemView.findViewById(R.id.tv_email);
            btnModifier  = itemView.findViewById(R.id.btn_modifier);
            btnSupprimer = itemView.findViewById(R.id.btn_supprimer);
        }
    }
}