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

public final class MinhasConsultasAdapter extends RecyclerView.Adapter<MinhasConsultasAdapter.Holder> {
    public interface Listener {
        void onAbrirSala(AgendamentoResponse item);
        void onCancelar(AgendamentoResponse item);
        void onRemarcar(AgendamentoResponse item);
    }

    private final List<AgendamentoResponse> items = new ArrayList<>();
    private final Listener listener;

    public MinhasConsultasAdapter(List<AgendamentoResponse> items, Listener listener) {
        this.listener = listener;
        atualizar(items);
    }

    public void atualizar(List<AgendamentoResponse> novaLista) {
        items.clear();
        if (novaLista != null) for (AgendamentoResponse item : novaLista) if (item != null) items.add(item);
        notifyDataSetChanged();
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_agendamento_cliente, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        AgendamentoResponse item = items.get(position);
        String dateTime = value(item.getDataHora());
        String day = dateTime.length() >= 10 ? dateTime.substring(0, 10) : dateTime;
        String hour = dateTime.length() >= 16 ? dateTime.substring(11, 16) : "Não informado";
        holder.info.setText("Profissional: " + value(item.getProfissionalNome())
                + "\nModalidade: " + value(item.getModalidade())
                + "\nDia: " + day + "  •  Hora: " + hour
                + "\nStatus: " + value(item.getStatus()));
        boolean active = com.example.mindu.AgendamentoUiRules.isActive(item.getStatus());
        holder.room.setVisibility(active ? View.VISIBLE : View.GONE);
        holder.room.setEnabled(item.getId() != null && !item.getId().trim().isEmpty());
        holder.room.setText("Entrar na Consulta (Visão Cliente)");
        holder.room.setOnClickListener(v -> { if (listener != null) listener.onAbrirSala(item); });
        boolean validId = item.getId() != null && !item.getId().trim().isEmpty();
        boolean agendado = com.example.mindu.AgendamentoUiRules.isActive(item.getStatus());
        holder.cancel.setEnabled(validId && agendado);
        holder.reschedule.setEnabled(validId && agendado && item.getProfissionalId() != null);
        holder.cancel.setOnClickListener(v -> { if (listener != null) listener.onCancelar(item); });
        holder.reschedule.setOnClickListener(v -> { if (listener != null) listener.onRemarcar(item); });
    }

    private static String value(String text) { return text == null || text.trim().isEmpty() ? "Não informado" : text.trim(); }
    @Override public int getItemCount() { return items.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView info;
        final Button room, cancel, reschedule;
        Holder(View view) {
            super(view);
            info = view.findViewById(R.id.txtAgendamentoInfoCliente);
            room = view.findViewById(R.id.btnSalaAgendamentoCliente);
            cancel = view.findViewById(R.id.btnCancelarAgendamentoCliente);
            reschedule = view.findViewById(R.id.btnRemarcarAgendamentoCliente);
        }
    }
}
