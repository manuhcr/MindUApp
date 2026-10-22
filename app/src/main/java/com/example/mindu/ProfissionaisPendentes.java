
package com.example.mindu;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.profissional.ProfissionalResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfissionaisPendentes extends AppCompatActivity {

    private LinearLayout containerProfissionais;
    private TextView txtEstadoLista;
    private Button btnAtualizarProfissionais;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profissionais_pendentes);

        containerProfissionais = findViewById(R.id.containerProfissionais);
        txtEstadoLista = findViewById(R.id.txtEstadoLista);
        btnAtualizarProfissionais =
                findViewById(R.id.btnAtualizarProfissionais);

        btnAtualizarProfissionais.setOnClickListener(v -> carregarProfissionais());

        carregarProfissionais();
    }

    private void carregarProfissionais() {
        txtEstadoLista.setText("Carregando profissionais...");
        containerProfissionais.removeAllViews();

        RetrofitClient.getApiService(this)
                .profissionaisPendentes()
                .enqueue(new Callback<List<ProfissionalResponse>>() {
                    @Override
                    public void onResponse(
                            Call<List<ProfissionalResponse>> call,
                            Response<List<ProfissionalResponse>> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            txtEstadoLista.setText(
                                    "Não foi possível carregar a lista."
                            );
                            return;
                        }

                        List<ProfissionalResponse> profissionais = response.body();

                        if (profissionais.isEmpty()) {
                            txtEstadoLista.setText(
                                    "Não há profissionais aguardando aprovação."
                            );
                            return;
                        }

                        txtEstadoLista.setText(
                                "Profissionais encontrados: " + profissionais.size()
                        );

                        for (ProfissionalResponse profissional : profissionais) {
                            adicionarCardProfissional(profissional);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ProfissionalResponse>> call,
                            Throwable t) {

                        txtEstadoLista.setText(
                                "Erro de conexão ao carregar a lista."
                        );
                    }
                });
    }

    private void adicionarCardProfissional(ProfissionalResponse profissional) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(32, 28, 32, 28);

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.parseColor("#DDF5F3F7"));
        fundo.setCornerRadius(24);
        fundo.setStroke(2, Color.parseColor("#9975689A"));
        card.setBackground(fundo);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        cardParams.setMargins(0, 0, 0, 24);
        card.setLayoutParams(cardParams);

        TextView txtId = new TextView(this);
        txtId.setText("ID: " + profissional.getId());
        txtId.setTextColor(Color.parseColor("#3D3642"));
        txtId.setTextSize(13);
        card.addView(txtId);

        TextView txtStatus = new TextView(this);
        txtStatus.setText("Status: " + profissional.getStatus());
        txtStatus.setTextColor(Color.parseColor("#75689A"));
        txtStatus.setTextSize(15);
        txtStatus.setPadding(0, 12, 0, 20);
        card.addView(txtStatus);

        Button btnAprovar = new Button(this);
        btnAprovar.setText("Aprovar");
        btnAprovar.setAllCaps(false);
        btnAprovar.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.parseColor("#75689A")
                )
        );
        btnAprovar.setTextColor(Color.WHITE);
        card.addView(btnAprovar);

        Button btnRejeitar = new Button(this);
        btnRejeitar.setText("Rejeitar");
        btnRejeitar.setAllCaps(false);
        card.addView(btnRejeitar);

        btnAprovar.setOnClickListener(v ->
                alterarAprovacao(profissional.getId(), true));

        btnRejeitar.setOnClickListener(v ->
                alterarAprovacao(profissional.getId(), false));

        containerProfissionais.addView(card);
    }

    private void alterarAprovacao(String id, boolean aprovado) {
        RetrofitClient.getApiService(this)
                .aprovarProfissional(id, aprovado)
                .enqueue(new Callback<ProfissionalResponse>() {
                    @Override
                    public void onResponse(
                            Call<ProfissionalResponse> call,
                            Response<ProfissionalResponse> response) {

                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    ProfissionaisPendentes.this,
                                    aprovado
                                            ? "Profissional aprovado!"
                                            : "Solicitação rejeitada.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            carregarProfissionais();
                        } else {
                            Toast.makeText(
                                    ProfissionaisPendentes.this,
                                    "Não foi possível atualizar. Verifique as permissões de administrador.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ProfissionalResponse> call,
                            Throwable t) {
                        Toast.makeText(
                                ProfissionaisPendentes.this,
                                "Erro de conexão ao atualizar.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
