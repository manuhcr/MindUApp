
package com.example.mindu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindu.R;
import com.example.mindu.api.empresas.EmpresaResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmpresasAdapter
        extends RecyclerView.Adapter<EmpresasAdapter.EmpresasViewHolder> {

    private final List<EmpresaResponse> empresasOriginais;
    private final List<EmpresaResponse> empresasExibidas;

    public interface OnExcluirEmpresaListener {
        void onExcluirEmpresa(String empresaId);
    }

    public interface OnEmpresaClickListener {
        void onEmpresaClick(EmpresaResponse empresa);
    }

    private final OnExcluirEmpresaListener excluirListener;
    private final OnEmpresaClickListener clickListener;

    public class EmpresasViewHolder extends RecyclerView.ViewHolder {

        TextView txtRazaoSocial;
        TextView txtPlanoEmpresa;
        Button btnExcluirEmpresa;

        public EmpresasViewHolder(@NonNull View itemView) {
            super(itemView);

            txtRazaoSocial = itemView.findViewById(R.id.txtRazaoSocial);
            txtPlanoEmpresa = itemView.findViewById(R.id.txtPlanoEmpresa);
            btnExcluirEmpresa = itemView.findViewById(R.id.btnExcluirEmpresa);
        }
    }

    public EmpresasAdapter(
            List<EmpresaResponse> empresas,
            OnExcluirEmpresaListener excluirListener,
            OnEmpresaClickListener clickListener) {

        this.empresasOriginais = new ArrayList<>();
        this.empresasExibidas = new ArrayList<>();

        this.excluirListener = excluirListener;
        this.clickListener = clickListener;

        atualizarLista(empresas);
    }

    @NonNull
    @Override
    public EmpresasViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_empresa_admin, parent, false);

        return new EmpresasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull EmpresasViewHolder holder, int position) {

        EmpresaResponse empresa = empresasExibidas.get(position);

        if (empresa == null) {
            holder.txtRazaoSocial.setText("Empresa não informada");
            holder.txtPlanoEmpresa.setText("");
            holder.btnExcluirEmpresa.setEnabled(false);
            holder.itemView.setOnClickListener(null);
            holder.btnExcluirEmpresa.setOnClickListener(null);
            return;
        }
        holder.btnExcluirEmpresa.setEnabled(empresa.id != null && !empresa.id.trim().isEmpty());
        holder.txtRazaoSocial.setText("Razão Social: " + (empresa.razaoSocial == null ? "Não informada" : empresa.razaoSocial));
        holder.txtPlanoEmpresa.setText("Plano: " + (empresa.plano == null ? "Não informado" : empresa.plano));

        // Clique no cartão: visualizar colaboradores
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onEmpresaClick(empresa);
            }
        });

        // Clique no botão: excluir empresa
        holder.btnExcluirEmpresa.setOnClickListener(v -> {
            if (excluirListener != null) {
                excluirListener.onExcluirEmpresa(empresa.id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return empresasExibidas.size();
    }

    public void atualizarLista(List<EmpresaResponse> novaLista) {

        empresasOriginais.clear();

        if (novaLista != null) {
            for (EmpresaResponse empresa : novaLista) if (empresa != null) empresasOriginais.add(empresa);
        }

        empresasExibidas.clear();
        empresasExibidas.addAll(empresasOriginais);

        notifyDataSetChanged();
    }

    public void filtrar(String texto) {

        empresasExibidas.clear();

        if (texto == null || texto.trim().isEmpty()) {

            empresasExibidas.addAll(empresasOriginais);

        } else {

            String pesquisa = texto.toLowerCase(Locale.ROOT).trim();

            for (EmpresaResponse empresa : empresasOriginais) {

                if (empresa.razaoSocial != null &&
                        empresa.razaoSocial.toLowerCase(Locale.ROOT)
                                .contains(pesquisa)) {

                    empresasExibidas.add(empresa);
                }
            }
        }

        notifyDataSetChanged();
    }
}
