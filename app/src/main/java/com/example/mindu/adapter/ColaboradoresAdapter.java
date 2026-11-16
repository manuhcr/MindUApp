
package com.example.mindu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindu.R;
import com.example.mindu.api.cliente.ClienteResponse;

import java.util.ArrayList;
import java.util.List;

public class ColaboradoresAdapter extends RecyclerView.Adapter<ColaboradoresAdapter.ColaboradorViewHolder> {

    public enum Modo {
        CONSULTA,
        GERENCIAMENTO
    }

    private final List<ClienteResponse> colaboradores;
    private final Modo modo;

    private final OnColaboradorClickListener clickListener;
    private final OnColaboradorAcaoListener acaoListener;

    public interface OnColaboradorClickListener {
        void onColaboradorClick(ClienteResponse colaborador);
    }

    public interface OnColaboradorAcaoListener {
        void onAcao(ClienteResponse colaborador, boolean aprovado);
    }

    public ColaboradoresAdapter(
            List<ClienteResponse> colaboradores,
            Modo modo,
            OnColaboradorClickListener clickListener,
            OnColaboradorAcaoListener acaoListener) {

        this.colaboradores = new ArrayList<>(colaboradores);
        this.modo = modo;
        this.clickListener = clickListener;
        this.acaoListener = acaoListener;
    }

    @NonNull
    @Override
    public ColaboradorViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_colaborador, parent, false);

        return new ColaboradorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ColaboradorViewHolder holder, int position) {

        ClienteResponse colaborador = colaboradores.get(position);

        holder.txtNome.setText(colaborador.getNome());
        holder.txtEmail.setText(colaborador.getEmail());

        String status = colaborador.getStatus();

        holder.txtStatus.setText(
                status == null ? "Não informado" : status
        );

        boolean pendente = "AGUARDANDO_APROVACAO".equals(status);

        boolean mostrarAcoes =
                modo == Modo.GERENCIAMENTO && pendente;

        holder.containerAcoes.setVisibility(
                mostrarAcoes ? View.VISIBLE : View.GONE
        );

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onColaboradorClick(colaborador);
            }
        });

        holder.btnAprovar.setOnClickListener(v -> {
            if (acaoListener != null) {
                acaoListener.onAcao(colaborador, true);
            }
        });

        holder.btnReprovar.setOnClickListener(v -> {
            if (acaoListener != null) {
                acaoListener.onAcao(colaborador, false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return colaboradores.size();
    }

    public void atualizarLista(List<ClienteResponse> novaLista) {
        colaboradores.clear();
        colaboradores.addAll(novaLista);
        notifyDataSetChanged();
    }

    static class ColaboradorViewHolder extends RecyclerView.ViewHolder {

        TextView txtNome;
        TextView txtEmail;
        TextView txtStatus;

        LinearLayout containerAcoes;

        Button btnAprovar;
        Button btnReprovar;

        public ColaboradorViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtNomeColaborador);
            txtEmail = itemView.findViewById(R.id.txtEmailColaborador);
            txtStatus = itemView.findViewById(R.id.txtStatusColaborador);

            containerAcoes = itemView.findViewById(R.id.containerAcoesColaborador);

            btnAprovar = itemView.findViewById(R.id.btnAprovarColaborador);
            btnReprovar = itemView.findViewById(R.id.btnReprovarColaborador);
        }
    }
}
