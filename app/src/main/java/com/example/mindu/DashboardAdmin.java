
package com.example.mindu;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.dashboard.DashboardResponse;
import com.example.mindu.api.profissional.ProfissionalResponse;
import java.util.List;
import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindu.adapter.ProfissionaisAdapter;

public class DashboardAdmin extends AppCompatActivity {

    private View contentInicio;
    private View contentEmpresas;
    private View contentProfissionais;
    private View contentPerfil;
    private FrameLayout containerAdmin;
    private TextView navInicio;
    private TextView navEmpresas;
    private TextView navProfissionais;
    private TextView navPerfil;

    private RecyclerView recyclerProfissionais;
    private ProfissionaisAdapter profissionaisAdapter;

    private TextView txtTotalPendentes;
    private TextView txtTotalProfissionais;
    private TextView txtTotalEmpresas;

    private TextView txtListaVazia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_admin);

        containerAdmin = findViewById(R.id.containerAdmin);

        navInicio = findViewById(R.id.navInicio);
        navEmpresas = findViewById(R.id.navEmpresas);
        navProfissionais = findViewById(R.id.navProfissionais);
        navPerfil = findViewById(R.id.navPerfil);

        navInicio.setOnClickListener(v ->
                mostrarTela(R.layout.content_inicio_admin));

        navEmpresas.setOnClickListener(v ->
                mostrarTela(R.layout.content_empresas_admin));

        navProfissionais.setOnClickListener(v -> mostrarProfissionais());

        navPerfil.setOnClickListener(v ->
                mostrarTela(R.layout.content_perfil_admin));

        mostrarTela(R.layout.content_inicio_admin);
    }

    private void mostrarTela(int layout) {

        View tela = getLayoutInflater().inflate(layout, containerAdmin, false);

        containerAdmin.removeAllViews();
        containerAdmin.addView(tela);


        if (layout == R.layout.content_inicio_admin) {

            txtTotalPendentes = tela.findViewById(R.id.txtTotalPendentes);
            txtTotalProfissionais = tela.findViewById(R.id.txtTotalProfissionais);
            txtTotalEmpresas = tela.findViewById(R.id.txtTotalEmpresas);

            carregarIndicadores();
        }
    }

    private void mostrarProfissionais(){
        mostrarTela(R.layout.content_profissionais_admin);

        txtListaVazia = containerAdmin.findViewById(R.id.txtListaVazia);

        recyclerProfissionais = containerAdmin.findViewById(R.id.recyclerProfissionais);

        recyclerProfissionais.setLayoutManager(
                new LinearLayoutManager(this));


        carregarProfissionais();

    }
    private void carregarIndicadores() {
    }

    private void carregarProfissionais() {

        RetrofitClient.getApiService(this)
                .profissionaisPendentes()
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(
                            Call<List<ProfissionalResponse>> call,
                            Response<List<ProfissionalResponse>> response) {
                        Log.d("MINDU_API", "Código: " + response.code());

                        Log.d("MINDU_API", "Body: " + response.body());
                        if (!response.isSuccessful()
                                || response.body() == null) {
                            Toast.makeText(
                                    DashboardAdmin.this,
                                    "Não foi possível carregar os profissionais.",
                                    Toast.LENGTH_LONG).show();
                        }else{
                            List<ProfissionalResponse> profissionais =
                                    response.body();
                            if (profissionais.isEmpty()) {
                                txtListaVazia.setVisibility(View.VISIBLE);
                                recyclerProfissionais.setVisibility(View.GONE);

                            } else {
                                txtListaVazia.setVisibility(View.GONE);
                                recyclerProfissionais.setVisibility(View.VISIBLE);
                            }
                            profissionaisAdapter = new ProfissionaisAdapter(

                                    profissionais,
                                    (profissional, aprovado, position) -> {
                                        atualizarAprovacao(profissional, aprovado, position);
                                    }
                            );
                            recyclerProfissionais.setAdapter(profissionaisAdapter);
                            Log.d("MINDU_API", "Total de profissionais: "
                                    + profissionais.size());


                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ProfissionalResponse>> call,
                            Throwable t) {
                        Toast.makeText(
                                DashboardAdmin.this,
                                "Erro de conexão ao carregar os profissionais.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                });
    }

    private void atualizarAprovacao(
            ProfissionalResponse profissional,
            boolean aprovado, int position) {

        RetrofitClient.getApiService(this)
                .aprovarProfissional(
                        profissional.getId(), aprovado)
                .enqueue(new Callback<>() {

                    @Override
                    public void onResponse(
                            Call<ProfissionalResponse> call,
                            Response<ProfissionalResponse> response) {

                        if (response.isSuccessful()) {
                            profissionaisAdapter.removerProfissional(position);

                            if (profissionaisAdapter.getItemCount() == 0){
                                txtListaVazia.setVisibility(View.VISIBLE);
                                recyclerProfissionais.setVisibility(View.GONE);

                            }
                        } else {
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

                        Toast.makeText(
                                DashboardAdmin.this,
                                "Erro de conexão ao atualizar cadastro.",
                                Toast.LENGTH_LONG).show();
                    }
                });
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