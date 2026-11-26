package com.example.mindu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindu.R;
import com.example.mindu.api.profissional.agendamento.AgendamentoResponse;

import java.util.ArrayList;
import java.util.List;

public final class AgendaProfissionalAdapter extends RecyclerView.Adapter<AgendaProfissionalAdapter.Holder> {
    public interface Listener {
        void onLink(AgendamentoResponse item);
        void onRealizada(AgendamentoResponse item);
        void onCancelar(AgendamentoResponse item);
        void onStart(AgendamentoResponse item);
        void onDemo(AgendamentoResponse item);
    }

    private final List<AgendamentoResponse> items = new ArrayList<>();
    private final Listener listener;

    public AgendaProfissionalAdapter(Listener listener) { this.listener = listener; }

    public void atualizar(List<AgendamentoResponse> novaLista) {
        items.clear();
        if (novaLista != null) for (AgendamentoResponse item : novaLista) if (item != null) items.add(item);
        notifyDataSetChanged();
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_agendamento_profissional, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        AgendamentoResponse item = items.get(position);
        holder.info.setText("Data: " + value(item.getDataHora()) + "\nCliente: " + value(item.getClienteNome())
                + "\nModalidade: " + value(item.getModalidade()) + " • Status: " + value(item.getStatus()));
        boolean validId = item.getId() != null && !item.getId().trim().isEmpty();
        boolean agendado = com.example.mindu.AgendamentoUiRules.isActive(item.getStatus());
        boolean linkOnline = com.example.mindu.AgendamentoUiRules.supportsOnline(item.getModalidade());
        holder.link.setVisibility(linkOnline ? View.VISIBLE : View.GONE);
        holder.link.setEnabled(validId && agendado);
        holder.done.setEnabled(validId && agendado);
        holder.cancel.setEnabled(validId && agendado);
        holder.link.setOnClickListener(v -> { if (listener != null) listener.onLink(item); });
        holder.done.setOnClickListener(v -> { if (listener != null) listener.onRealizada(item); });
        holder.cancel.setOnClickListener(v -> { if (listener != null) listener.onCancelar(item); });
        holder.start.setEnabled(validId && agendado && linkOnline);
        holder.demo.setEnabled(validId);
        holder.start.setOnClickListener(v -> { if (listener != null) listener.onStart(item); });
        holder.demo.setOnClickListener(v -> { if (listener != null) listener.onDemo(item); });
    }

    private static String value(String text) { return text == null || text.trim().isEmpty() ? "Não informado" : text.trim(); }
    @Override public int getItemCount() { return items.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView info;
        final Button link, done, cancel, start, demo;
        Holder(View view) {
            super(view);
            info = view.findViewById(R.id.txtAgendamentoInfoProfissional);
            link = view.findViewById(R.id.btnLinkAgendamentoProfissional);
            done = view.findViewById(R.id.btnRealizadaAgendamentoProfissional);
            cancel = view.findViewById(R.id.btnCancelarAgendamentoProfissional);
            start = view.findViewById(R.id.btnIniciarConsultaProfissional);
            demo = view.findViewById(R.id.btnDemoSessaoProfissional);
        }
    }
}
