package com.example.mindu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindu.R;
import com.example.mindu.api.profissional.ProfissionalResponse;

import java.util.List;

public class ProfissionaisAdapter
        extends RecyclerView.Adapter<ProfissionaisAdapter.ProfissionalViewHolder> {
    private List<ProfissionalResponse> profissionais;

    public interface OnAprovacaoClickListener {
        void onAprovacaoClick(ProfissionalResponse profissional, boolean aprovado, int position);
    }

    private OnAprovacaoClickListener listener;

    @NonNull
    @Override
    public ProfissionalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profissional, parent, false);
        return new ProfissionalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfissionalViewHolder holder, int position) {
        int posicaoAtual =holder.getAdapterPosition();
        ProfissionalResponse profissional = profissionais.get(position);

        holder.txtNomeProfissional.setText(profissional.getNome());
        holder.txtStatusProfissional.setText("Status: " + profissional.getStatus());
        holder.btnAprovar.setOnClickListener(v -> {
            holder.btnAprovar.setEnabled(false);
            holder.btnRejeitar.setEnabled(false);
            if (posicaoAtual != RecyclerView.NO_POSITION){
                listener.onAprovacaoClick(profissional, true, posicaoAtual);
            }

        });
        holder.btnRejeitar.setOnClickListener(v -> {
            holder.btnAprovar.setEnabled(false);
            holder.btnRejeitar.setEnabled(false);

            if (posicaoAtual != RecyclerView.NO_POSITION){
                listener.onAprovacaoClick(profissional, false, posicaoAtual);
            }
        });
    }

    @Override
    public int getItemCount() {
        return profissionais != null ? profissionais.size() : 0;

    }

    public class ProfissionalViewHolder extends RecyclerView.ViewHolder {

        private TextView txtNomeProfissional;
        private TextView txtStatusProfissional;
        private Button btnAprovar;
        private Button btnRejeitar;
        public ProfissionalViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNomeProfissional = itemView.findViewById(R.id.txtNomeProfissional);
            txtStatusProfissional = itemView.findViewById(R.id.txtStatusProfissional);
            btnAprovar = itemView.findViewById(R.id.btnAprovar);
            btnRejeitar = itemView.findViewById(R.id.btnRejeitar);

        }
    }

    public ProfissionaisAdapter(List<ProfissionalResponse> profissionais, OnAprovacaoClickListener listener) {
        this.profissionais = profissionais;
        this.listener = listener;
    }

    public void removerProfissional(int position) {
        // 1. Remover o item da lista profissionais
        profissionais.remove(position);

        // 2. Notificar o RecyclerView que o item foi removido
        notifyItemRemoved(position);
    }

}
