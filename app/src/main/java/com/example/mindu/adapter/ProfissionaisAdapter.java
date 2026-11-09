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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfissionaisAdapter
        extends RecyclerView.Adapter<ProfissionaisAdapter.ProfissionalViewHolder> {

    private final List<ProfissionalResponse> profissionaisOriginais = new ArrayList<>();
    private final List<ProfissionalResponse> profissionaisExibidos = new ArrayList<>();

    private String pesquisaAtual = "";
    private boolean mostrarAcoes = true;

    public interface OnAprovacaoClickListener {
        void onAprovacaoClick(
                ProfissionalResponse profissional,
                boolean aprovado,
                int position
        );
    }

    private final OnAprovacaoClickListener listener;

    public ProfissionaisAdapter(
            List<ProfissionalResponse> profissionais,
            OnAprovacaoClickListener listener
    ) {
        this.listener = listener;
        atualizarLista(profissionais);
    }

    @NonNull
    @Override
    public ProfissionalViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profissional_admin, parent, false);

        return new ProfissionalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProfissionalViewHolder holder,
            int position
    ) {
        ProfissionalResponse profissional = profissionaisExibidos.get(position);

        holder.txtNomeProfissional.setText(profissional.getNome());
        holder.txtStatusProfissional.setText(
                "Status: " + profissional.getStatus()
        );

        // Os botões só aparecem na aba de pendentes
        if (mostrarAcoes) {
            holder.btnAprovar.setVisibility(View.VISIBLE);
            holder.btnRejeitar.setVisibility(View.VISIBLE);

            holder.btnAprovar.setEnabled(true);
            holder.btnRejeitar.setEnabled(true);

            holder.btnAprovar.setOnClickListener(v -> {
                int posicaoAtual = holder.getAdapterPosition();

                if (posicaoAtual != RecyclerView.NO_POSITION
                        && listener != null) {

                    holder.btnAprovar.setEnabled(false);
                    holder.btnRejeitar.setEnabled(false);

                    listener.onAprovacaoClick(
                            profissional,
                            true,
                            posicaoAtual
                    );
                }
            });

            holder.btnRejeitar.setOnClickListener(v -> {
                int posicaoAtual = holder.getAdapterPosition();

                if (posicaoAtual != RecyclerView.NO_POSITION
                        && listener != null) {

                    holder.btnAprovar.setEnabled(false);
                    holder.btnRejeitar.setEnabled(false);

                    listener.onAprovacaoClick(
                            profissional,
                            false,
                            posicaoAtual
                    );
                }
            });

        } else {
            holder.btnAprovar.setVisibility(View.GONE);
            holder.btnRejeitar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return profissionaisExibidos.size();
    }

    // Recebe uma nova lista da API
    public void atualizarLista(List<ProfissionalResponse> lista) {
        profissionaisOriginais.clear();

        if (lista != null) {
            profissionaisOriginais.addAll(lista);
        }

        aplicarPesquisa();
    }

    // Pesquisa pelo nome
    public void filtrar(String texto) {
        pesquisaAtual = texto == null ? "" : texto;
        aplicarPesquisa();
    }

    private void aplicarPesquisa() {
        profissionaisExibidos.clear();

        String termo = pesquisaAtual.trim().toLowerCase(Locale.ROOT);

        for (ProfissionalResponse profissional : profissionaisOriginais) {
            String nome = profissional.getNome();

            if (nome != null
                    && nome.toLowerCase(Locale.ROOT).contains(termo)) {

                profissionaisExibidos.add(profissional);
            }
        }

        notifyDataSetChanged();
    }

    // Define se os botões de aprovação devem aparecer
    public void setMostrarAcoes(boolean mostrar) {
        mostrarAcoes = mostrar;
        notifyDataSetChanged();
    }

    public static class ProfissionalViewHolder extends RecyclerView.ViewHolder {

        TextView txtNomeProfissional;
        TextView txtStatusProfissional;
        Button btnAprovar;
        Button btnRejeitar;

        public ProfissionalViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNomeProfissional = itemView.findViewById(R.id.txtNomeProfissional);
            txtStatusProfissional = itemView.findViewById(R.id.txtStatusProfissional);
            btnAprovar = itemView.findViewById(R.id.btnAprovar);
            btnRejeitar = itemView.findViewById(R.id.btnRejeitar);
        }
    }

}
