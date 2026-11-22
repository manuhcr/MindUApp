
package com.example.mindu.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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

    // Controle dos modos
    private boolean mostrarAcoes = true;
    private boolean modoBeneficiario = false;

    // Listener administrativo
    public interface OnAprovacaoClickListener {
        void onAprovacaoClick(
                ProfissionalResponse profissional,
                boolean aprovado,
                int position
        );
    }

    private final OnAprovacaoClickListener listener;

    // Listener do beneficiário
    public interface OnProfissionalClickListener {
        void onProfissionalClick(ProfissionalResponse profissional);
    }

    private OnProfissionalClickListener profissionalClickListener;

    public ProfissionaisAdapter(
            List<ProfissionalResponse> profissionais,
            OnAprovacaoClickListener listener
    ) {
        this.listener = listener;
        atualizarLista(profissionais);
    }

    // Define o modo beneficiário
    public void setModoBeneficiario(boolean modo) {
        modoBeneficiario = modo;
        notifyDataSetChanged();
    }

    // Define o clique no profissional
    public void setOnProfissionalClickListener(
            OnProfissionalClickListener listener
    ) {
        profissionalClickListener = listener;
    }

    // Define qual tipo de card será utilizado
    @Override
    public int getItemViewType(int position) {
        return modoBeneficiario ? 1 : 0;
    }

    @NonNull
    @Override
    public ProfissionalViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        int layout = viewType == 1
                ? R.layout.item_profissional_beneficiario
                : R.layout.item_profissional_admin;

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new ProfissionalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProfissionalViewHolder holder,
            int position
    ) {

        ProfissionalResponse profissional =
                profissionaisExibidos.get(position);

        // MODO BENEFICIÁRIO
        if (modoBeneficiario) {

            holder.txtNomeProfissional.setText(
                    profissional.getNome()
            );

            holder.txtTipoProfissional.setText(
                    profissional.getTipo() == null
                            ? "Especialista"
                            : profissional.getTipo()
            );

            holder.txtTagsProfissional.setText(
                    profissional.getTags() == null
                            || profissional.getTags().isEmpty()
                            ? "Especialidades não informadas"
                            : android.text.TextUtils.join(
                            " • ",
                            profissional.getTags()
                    )
            );

            holder.txtModalidadesProfissional.setText(
                    profissional.getModalidades() == null
                            || profissional.getModalidades().isEmpty()
                            ? "Modalidades não informadas"
                            : android.text.TextUtils.join(
                            " • ",
                            profissional.getModalidades()
                    )
            );

            holder.itemView.setOnClickListener(v -> {
                if (profissionalClickListener != null) {
                    profissionalClickListener.onProfissionalClick(
                            profissional
                    );
                }
            });

            return;
        }

        // MODO ADMINISTRADOR

        holder.txtNomeProfissional.setText(
                profissional.getNome()
        );

        holder.txtIdProfissional.setText(
                "ID: " + profissional.getId()
        );

        String status = profissional.getStatus() == null
                ? ""
                : profissional.getStatus().toUpperCase(Locale.ROOT);

        holder.txtStatusProfissional.setText(status);

        // Personaliza a etiqueta de acordo com o status
        if (status.equals("APROVADO")) {

            holder.txtStatusProfissional.setBackgroundResource(
                    R.drawable.bg_status_aprovado
            );

            holder.txtStatusProfissional.setTextColor(
                    Color.parseColor("#246B3B")
            );

        } else {

            holder.txtStatusProfissional.setBackgroundResource(
                    R.drawable.bg_status_pendente
            );

            holder.txtStatusProfissional.setTextColor(
                    Color.parseColor("#8A6500")
            );
        }

        // Ações administrativas
        if (mostrarAcoes) {

            holder.layoutAcoesProfissional.setVisibility(View.VISIBLE);

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

            holder.layoutAcoesProfissional.setVisibility(View.GONE);
        }

        // Remove clique residual do modo beneficiário
        holder.itemView.setOnClickListener(null);
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

    // Define se os botões administrativos devem aparecer
    public void setMostrarAcoes(boolean mostrar) {

        mostrarAcoes = mostrar;

        notifyDataSetChanged();
    }

    public static class ProfissionalViewHolder
            extends RecyclerView.ViewHolder {

        // Componentes administrativos
        TextView txtNomeProfissional;
        TextView txtStatusProfissional;
        TextView txtIdProfissional;

        Button btnAprovar;
        Button btnRejeitar;

        LinearLayout layoutAcoesProfissional;

        // Componentes do beneficiário
        TextView txtTipoProfissional;
        TextView txtTagsProfissional;
        TextView txtModalidadesProfissional;

        public ProfissionalViewHolder(@NonNull View itemView) {
            super(itemView);

            // Componentes administrativos
            txtNomeProfissional =
                    itemView.findViewById(R.id.txtNomeProfissional);

            txtStatusProfissional =
                    itemView.findViewById(R.id.txtStatusProfissional);

            btnAprovar =
                    itemView.findViewById(R.id.btnAprovar);

            btnRejeitar =
                    itemView.findViewById(R.id.btnRejeitar);

            txtIdProfissional =
                    itemView.findViewById(R.id.txtIdProfissional);

            layoutAcoesProfissional =
                    itemView.findViewById(R.id.layoutAcoesProfissional);

            // Componentes do beneficiário
            txtTipoProfissional =
                    itemView.findViewById(R.id.txtTipoProfissional);

            txtTagsProfissional =
                    itemView.findViewById(R.id.txtTagsProfissional);

            txtModalidadesProfissional =
                    itemView.findViewById(R.id.txtModalidadesProfissional);
        }
    }
}
