package com.example.mindu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindu.R;
import com.example.mindu.api.chat.ChatTurn;

import java.util.List;

public class MensagemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TIPO_MILA = 0;
    private static final int TIPO_USUARIO = 1;

    private final List<ChatTurn> mensagens;

    public MensagemAdapter(List<ChatTurn> mensagens) {
        this.mensagens = mensagens;
    }

    @Override
    public int getItemViewType(int position) {

        ChatTurn mensagem = mensagens.get(position);

        if ("user".equals(mensagem.getRole())) {
            return TIPO_USUARIO;
        }

        return TIPO_MILA;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        int layout;

        if (viewType == TIPO_USUARIO) {
            layout = R.layout.item_mensagem_usuario;
        } else {
            layout = R.layout.item_mensagem_mila;
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new MensagemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position) {

        ChatTurn mensagem = mensagens.get(position);

        MensagemViewHolder mensagemHolder =
                (MensagemViewHolder) holder;

        mensagemHolder.txtMensagem.setText(mensagem.getContent());
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    static class MensagemViewHolder extends RecyclerView.ViewHolder {

        TextView txtMensagem;

        public MensagemViewHolder(@NonNull View itemView) {
            super(itemView);

            txtMensagem = itemView.findViewById(R.id.txtMensagem);
        }
    }
}
