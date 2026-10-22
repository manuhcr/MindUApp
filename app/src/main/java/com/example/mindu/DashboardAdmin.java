
package com.example.mindu;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.profissional.ProfissionalResponse;

import java.util.List;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardAdmin extends AppCompatActivity {

    private TextView txtTotalPendentes;
    private LinearLayout layoutListaPendentes;
    private boolean carregando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_admin);

        BlurTarget target =
                findViewById(R.id.blurTargetDashboardAdmin);

        aplicarEfeitoVidro(
                findViewById(R.id.blurCardBoasVindas), target);
        aplicarEfeitoVidro(
                findViewById(R.id.blurCardPendentes), target);
        aplicarEfeitoVidro(
                findViewById(R.id.blurCardAtivos), target);
        aplicarEfeitoVidro(
                findViewById(R.id.blurCardUsuarios), target);
        aplicarEfeitoVidro(
                findViewById(R.id.blurCardAprovacoes), target);

        txtTotalPendentes =
                findViewById(R.id.txtTotalPendentes);

        layoutListaPendentes =
                findViewById(R.id.layoutListaPendentes);

        findViewById(R.id.navInicio).setOnClickListener(v ->
                Toast.makeText(this,
                        "Você já está no início.",
                        Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnAbrirAprovacoes).setOnClickListener(v ->
                carregarPendentes());

        findViewById(R.id.navAprovacoes).setOnClickListener(v ->
                carregarPendentes());

        carregarPendentes();
    }

    private void carregarPendentes() {
        if (carregando) return;

        carregando = true;
        txtTotalPendentes.setText("...");
        layoutListaPendentes.removeAllViews();

        TextView carregandoTexto = new TextView(this);
        carregandoTexto.setText("Carregando profissionais...");
        carregandoTexto.setTextColor(Color.WHITE);
        layoutListaPendentes.addView(carregandoTexto);

        RetrofitClient.getApiService(this)
                .profissionaisPendentes()
                .enqueue(new Callback<List<ProfissionalResponse>>() {
                    @Override
                    public void onResponse(
                            Call<List<ProfissionalResponse>> call,
                            Response<List<ProfissionalResponse>> response) {

                        carregando = false;
                        layoutListaPendentes.removeAllViews();

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            txtTotalPendentes.setText("—");
                            mostrarMensagem(
                                    "Não foi possível carregar os pendentes.");
                            return;
                        }

                        List<ProfissionalResponse> profissionais =
                                response.body();

                        txtTotalPendentes.setText(
                                String.valueOf(profissionais.size()));

                        if (profissionais.isEmpty()) {
                            mostrarMensagem(
                                    "Não há profissionais pendentes.");
                            return;
                        }

                        for (ProfissionalResponse profissional
                                : profissionais) {
                            adicionarProfissional(profissional);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ProfissionalResponse>> call,
                            Throwable t) {

                        carregando = false;
                        txtTotalPendentes.setText("—");
                        layoutListaPendentes.removeAllViews();
                        mostrarMensagem(
                                "Erro de conexão ao carregar os pendentes.");
                    }
                });
    }

    private void adicionarProfissional(
            ProfissionalResponse profissional) {

        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(12), dp(12), dp(12), dp(12));

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.parseColor("#337FFFFFF"));
        fundo.setCornerRadius(dp(16));
        fundo.setStroke(dp(1),
                Color.parseColor("#66F5F3F7"));
        item.setBackground(fundo);

        LinearLayout.LayoutParams itemParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        itemParams.setMargins(0, dp(6), 0, dp(6));

        TextView nome = new TextView(this);
        nome.setText(profissional.getNome() != null
                ? profissional.getNome()
                : "Profissional sem nome");
        nome.setTextColor(Color.WHITE);
        nome.setTextSize(16);
        nome.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView status = new TextView(this);
        status.setText("Status: " + profissional.getStatus());
        status.setTextColor(Color.WHITE);
        status.setTextSize(13);

        LinearLayout botoes = new LinearLayout(this);
        botoes.setOrientation(LinearLayout.HORIZONTAL);
        botoes.setPadding(0, dp(8), 0, 0);

        Button aprovar = new Button(this);
        aprovar.setText("Aprovar");
        aprovar.setAllCaps(false);

        Button rejeitar = new Button(this);
        rejeitar.setText("Rejeitar");
        rejeitar.setAllCaps(false);

        LinearLayout.LayoutParams botaoParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f);
        botaoParams.setMargins(0, 0, dp(6), 0);

        botoes.addView(aprovar, botaoParams);

        LinearLayout.LayoutParams rejeitarParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f);
        rejeitarParams.setMargins(dp(6), 0, 0, 0);

        botoes.addView(rejeitar, rejeitarParams);

        item.addView(nome);
        item.addView(status);
        item.addView(botoes);

        layoutListaPendentes.addView(item, itemParams);

        aprovar.setOnClickListener(v ->
                atualizarAprovacao(profissional, true,
                        aprovar, rejeitar));

        rejeitar.setOnClickListener(v ->
                atualizarAprovacao(profissional, false,
                        aprovar, rejeitar));
    }

    private void atualizarAprovacao(
            ProfissionalResponse profissional,
            boolean aprovado,
            Button botaoAprovar,
            Button botaoRejeitar) {

        botaoAprovar.setEnabled(false);
        botaoRejeitar.setEnabled(false);

        RetrofitClient.getApiService(this)
                .aprovarProfissional(
                        profissional.getId(), aprovado)
                .enqueue(new Callback<ProfissionalResponse>() {

                    @Override
                    public void onResponse(
                            Call<ProfissionalResponse> call,
                            Response<ProfissionalResponse> response) {

                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    DashboardAdmin.this,
                                    aprovado
                                            ? "Profissional aprovado!"
                                            : "Solicitação rejeitada.",
                                    Toast.LENGTH_SHORT).show();

                            carregarPendentes();
                        } else {
                            botaoAprovar.setEnabled(true);
                            botaoRejeitar.setEnabled(true);

                            Toast.makeText(
                                    DashboardAdmin.this,
                                    "Não foi possível concluir a ação. "
                                            + "Código: " + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ProfissionalResponse> call,
                            Throwable t) {

                        botaoAprovar.setEnabled(true);
                        botaoRejeitar.setEnabled(true);

                        Toast.makeText(
                                DashboardAdmin.this,
                                "Erro de conexão ao atualizar cadastro.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void mostrarMensagem(String mensagem) {
        TextView texto = new TextView(this);
        texto.setText(mensagem);
        texto.setTextColor(Color.WHITE);
        texto.setTextSize(14);
        texto.setPadding(0, dp(8), 0, dp(8));
        layoutListaPendentes.addView(texto);
    }

    private int dp(float valor) {
        return (int) (valor * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private void aplicarEfeitoVidro(
            BlurView blurView,
            BlurTarget blurTarget) {

        if (blurView == null || blurTarget == null) return;

        float density = getResources()
                .getDisplayMetrics().density;

        GradientDrawable vidro = new GradientDrawable();
        vidro.setShape(GradientDrawable.RECTANGLE);
        vidro.setColor(Color.parseColor("#1F8294EF"));
        vidro.setCornerRadius(24 * density);
        vidro.setStroke(
                Math.max(1, (int) density),
                Color.parseColor("#99F5F3F7"));

        blurView.setBackground(vidro);
        blurView.setupWith(blurTarget)
                .setBlurRadius(20f);

        blurView.setOutlineProvider(
                ViewOutlineProvider.BACKGROUND);
        blurView.setClipToOutline(true);
        blurView.setElevation(4 * density);
    }
}
