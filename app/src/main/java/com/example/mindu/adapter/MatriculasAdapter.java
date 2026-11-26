package com.example.mindu.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindu.R;
import com.example.mindu.api.matriculas.MatriculaResponse;

import java.util.List;

public class MatriculasAdapter extends RecyclerView.Adapter<MatriculasAdapter.MatriculaViewHolder> {

    private final List<MatriculaResponse> matriculas;
    private final Context context;

    public MatriculasAdapter(Context context, List<MatriculaResponse> matriculas) {
        this.context = context;
        this.matriculas = matriculas;
    }

    @NonNull
    @Override
    public MatriculaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_matricula_empresa, parent, false);

        return new MatriculaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MatriculaViewHolder holder,
            int position) {

        MatriculaResponse matricula = matriculas.get(position);

        holder.txtCodigo.setText(matricula.codigo == null || matricula.codigo.trim().isEmpty()
                ? "Código não informado" : matricula.codigo);

        if (matricula.utilizada) {

            holder.txtStatus.setText("Utilizada");
            holder.txtStatus.setTextColor(Color.parseColor("#C74343"));
            holder.txtStatus.setBackgroundColor(Color.parseColor("#FBEAEA"));

            holder.btnCopiar.setVisibility(View.GONE);

            if (matricula.beneficiarioNome != null
                    && !matricula.beneficiarioNome.trim().isEmpty()) {

                holder.txtBeneficiario.setText(
                        "Colaborador: " + matricula.beneficiarioNome
                );

                holder.txtBeneficiario.setVisibility(View.VISIBLE);

            } else {
                holder.txtBeneficiario.setVisibility(View.GONE);
            }

        } else {

            holder.txtStatus.setText("Disponível");
            holder.txtStatus.setTextColor(Color.parseColor("#21966A"));
            holder.txtStatus.setBackgroundColor(Color.parseColor("#E5F3ED"));

            holder.txtBeneficiario.setVisibility(View.GONE);
            boolean codigoDisponivel = matricula.codigo != null && !matricula.codigo.trim().isEmpty();
            holder.btnCopiar.setVisibility(codigoDisponivel ? View.VISIBLE : View.GONE);
            holder.btnCopiar.setOnClickListener(v -> {
                if (!codigoDisponivel) return;

                ClipboardManager clipboard =
                        (ClipboardManager) context.getSystemService(
                                Context.CLIPBOARD_SERVICE
                        );

                ClipData clip = ClipData.newPlainText(
                        "Matrícula MindU",
                        matricula.codigo
                );

                clipboard.setPrimaryClip(clip);

                Toast.makeText(
                        context,
                        "Código copiado!",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return matriculas.size();
    }

    static class MatriculaViewHolder extends RecyclerView.ViewHolder {

        TextView txtCodigo;
        TextView txtStatus;
        TextView txtBeneficiario;
        Button btnCopiar;

        public MatriculaViewHolder(@NonNull View itemView) {
            super(itemView);

            txtCodigo = itemView.findViewById(R.id.txtCodigoMatricula);
            txtStatus = itemView.findViewById(R.id.txtStatusMatricula);
            txtBeneficiario = itemView.findViewById(R.id.txtBeneficiarioMatricula);
            btnCopiar = itemView.findViewById(R.id.btnCopiarMatricula);
        }
    }
}
