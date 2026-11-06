package com.example.mindu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindu.R;
import com.example.mindu.api.cadastro.EmpresaResponse;

import java.util.List;

public class EmpresasAdapter
        extends RecyclerView.Adapter<EmpresasAdapter.EmpresasViewHolder> {

    private List<EmpresaResponse> empresas;

    public interface OnExcluirEmpresaListener {
        void onExcluirEmpresa(String empresaId);
    }

    private OnExcluirEmpresaListener listener;

    public class EmpresasViewHolder extends RecyclerView.ViewHolder {

        TextView txtRazaoSocial;
        TextView txtIdEmpresa;
        TextView txtPlanoEmpresa;

        Button btnExcluirEmpresa;

        public EmpresasViewHolder(@NonNull View itemView) {
            super(itemView);

            txtIdEmpresa = itemView.findViewById(R.id.txtIdEmpresa);
            txtRazaoSocial = itemView.findViewById(R.id.txtRazaoSocial);
            txtPlanoEmpresa = itemView.findViewById(R.id.txtPlanoEmpresa);
            btnExcluirEmpresa = itemView.findViewById(R.id.btnExcluirEmpresa);
        }
    }



    public EmpresasAdapter(
            List<EmpresaResponse> empresas,
            OnExcluirEmpresaListener listener) {

        this.empresas = empresas;
        this.listener = listener;
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

        EmpresaResponse empresa = empresas.get(position);

        holder.txtIdEmpresa.setText("ID: " + empresa.id);
        holder.txtRazaoSocial.setText(
                "Razão Social: " + empresa.razaoSocial);
        holder.txtPlanoEmpresa.setText(
                "Plano: " + empresa.plano);

        holder.btnExcluirEmpresa.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExcluirEmpresa(empresa.id);
            }
        });


    }

    @Override
    public int getItemCount() {
        return empresas != null ? empresas.size() : 0;
    }
}
