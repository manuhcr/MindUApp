package com.example.mindu;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mindu.api.ApiService;
import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.cadastro.EmpresaRequest;
import com.example.mindu.api.cadastro.EmpresaResponse;
import com.example.mindu.api.planos.PlanoResponse;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroEmpresa extends AppCompatActivity {


    private final int ROXO = Color.parseColor("#75689A");
    private final int ROXO_CLARO = Color.parseColor("#D9D1E8");
    private LinearLayout layoutEtapa1;
    private LinearLayout layoutEtapa2;
    private TextView textEtapaAtual;
    private View progressoEtapa1;
    private View progressoEtapa2;

    private ApiService api;

    private MaterialAutoCompleteTextView campoPlano;

    private List<PlanoResponse> planosDisponiveis = new ArrayList<>();

    private String planoIdSelecionado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_empresa);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        api = RetrofitClient.getApiService(this);

        BlurTarget blurTargetCadastro = findViewById(R.id.blurTargetCadastro);
        BlurView blurCadastroEmpresa = findViewById(R.id.blurCadastroEmpresa);

        blurCadastroEmpresa
                .setupWith(blurTargetCadastro)
                .setBlurRadius(20f);

        aplicarEfeitoVidroLiquido(blurCadastroEmpresa);

        // Componentes das duas etapas
        layoutEtapa1 = findViewById(R.id.layoutEtapa1);
        layoutEtapa2 = findViewById(R.id.layoutEtapa2);

        textEtapaAtual = findViewById(R.id.textEtapaAtual);
        progressoEtapa1 = findViewById(R.id.progressoEtapa1);
        progressoEtapa2 = findViewById(R.id.progressoEtapa2);

        Button buttonProximaEtapa =
                findViewById(R.id.buttonProximaEtapa);

        Button buttonVoltarEtapa =
                findViewById(R.id.buttonVoltarEtapa);

        // Avançar para a segunda etapa
        buttonProximaEtapa.setOnClickListener(v -> {
            mostrarEtapa(2);
        });

        // Voltar para a primeira etapa
        buttonVoltarEtapa.setOnClickListener(v -> {
            mostrarEtapa(1);
        });

        api = RetrofitClient.getApiService(this);

        campoPlano = findViewById(R.id.edtPlanoId);

        carregarPlanos();

        Button btnCriarConta = findViewById(R.id.btnCriarConta);
        btnCriarConta.setOnClickListener(v ->  cadastrarEmpresa());

    }
    private void mostrarEtapa(int etapa) {

        boolean primeiraEtapa = etapa == 1;

        layoutEtapa1.setVisibility(
                primeiraEtapa ? View.VISIBLE : View.GONE);

        layoutEtapa2.setVisibility(
                primeiraEtapa ? View.GONE : View.VISIBLE);

        textEtapaAtual.setText(
                primeiraEtapa ? "ETAPA 1 DE 2" : "ETAPA 2 DE 2");

        // A etapa ativa fica roxa; a outra fica mais clara.
        progressoEtapa1.setBackgroundTintList(
                ColorStateList.valueOf(
                        primeiraEtapa ? ROXO : ROXO_CLARO));

        progressoEtapa2.setBackgroundTintList(
                ColorStateList.valueOf(
                        primeiraEtapa ? ROXO_CLARO : ROXO));

        // Mantém a barra ativa totalmente visível.
        progressoEtapa1.setAlpha(1f);
        progressoEtapa2.setAlpha(1f);

        progressoEtapa1.setAlpha(1f);
        progressoEtapa2.setAlpha(primeiraEtapa ? 0.35f : 1f);

        // Volta o formulário para o início da etapa
        View scroll = findViewById(R.id.scrollCadastroEmpresa);
        scroll.scrollTo(0, 0);
    }

    public boolean camposVazios(String[] campos){
        for (String campo : campos) {
            if (campo.isEmpty()) {
                return true;
            }
        }
        return false;
    }


    private void aplicarEfeitoVidroLiquido(View view) {
        if (view == null) return;

        GradientDrawable liquidGlass = new GradientDrawable();

        liquidGlass.setShape(GradientDrawable.RECTANGLE);
        liquidGlass.setOrientation(
                GradientDrawable.Orientation.TL_BR
        );

        liquidGlass.setColor(
                Color.parseColor("#1F8294EF")
        );

        float density = view.getContext()
                .getResources()
                .getDisplayMetrics()
                .density;

        liquidGlass.setCornerRadius(24 * density);

        int strokeWidthPx = (int) (1 * density);

        liquidGlass.setStroke(
                strokeWidthPx,
                Color.parseColor("#99F5F3F7")
        );

        view.setBackground(liquidGlass);

        view.setOutlineProvider(
                ViewOutlineProvider.BACKGROUND
        );

        view.setClipToOutline(true);

        view.setElevation(4 * density);
    }

    private void carregarPlanos() {
        api.listarPlanos().enqueue(new Callback<List<PlanoResponse>>() {

            @Override
            public void onResponse(
                    Call<List<PlanoResponse>> call,
                    Response<List<PlanoResponse>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    planosDisponiveis.clear();
                    planosDisponiveis.addAll(response.body());

                    List<String> nomesPlanos = new ArrayList<>();

                    for (PlanoResponse plano : planosDisponiveis) {
                        nomesPlanos.add(plano.nome);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            CadastroEmpresa.this,
                            android.R.layout.simple_dropdown_item_1line,
                            nomesPlanos
                    );

                    campoPlano.setAdapter(adapter);

                    campoPlano.setOnItemClickListener((parent, view, position, id) -> {
                        PlanoResponse plano = planosDisponiveis.get(position);
                        planoIdSelecionado = plano.id;
                    });

                } else {
                    Toast.makeText(
                            CadastroEmpresa.this,
                            "Não foi possível carregar os planos.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<List<PlanoResponse>> call,
                    Throwable t) {

                Toast.makeText(
                        CadastroEmpresa.this,
                        "Erro de conexão ao buscar os planos.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private String valorCampo(int id) {
        TextInputEditText campo = findViewById(id);
        return campo.getText().toString().trim();
    }

    private void cadastrarEmpresa() {

        if (planoIdSelecionado == null) {
            Toast.makeText(
                    this,
                    "Selecione um plano antes de continuar.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        EmpresaRequest request = new EmpresaRequest();

        request.setRazaoSocial(valorCampo(R.id.edtRazaoSocial));
        request.setNomeFantasia(valorCampo(R.id.edtNomeFantasia));
        request.setCnpj(valorCampo(R.id.edtCnpj));
        request.setInscricaoEstadual(valorCampo(R.id.edtInscricaoEstadual));
        request.setResponsavel(valorCampo(R.id.edtResponsavel));

        request.setCep(valorCampo(R.id.edtCep));
        request.setLogradouro(valorCampo(R.id.edtLogradouro));
        request.setNumero(valorCampo(R.id.edtNumero));
        request.setComplemento(valorCampo(R.id.edtComplemento));
        request.setBairro(valorCampo(R.id.edtBairro));
        request.setCidade(valorCampo(R.id.edtCidade));
        request.setEstado(valorCampo(R.id.edtEstado));

        request.setEmailEmpresa(valorCampo(R.id.edtEmailEmpresa));
        request.setTelefone(valorCampo(R.id.edtTelefone));
        request.setSenha(valorCampo(R.id.edtSenha));

        request.setPlanoId(planoIdSelecionado);

        if (camposVazios(new String[]{
                request.getRazaoSocial(),
                request.getNomeFantasia(),
                request.getCnpj(),
                request.getInscricaoEstadual(),
                request.getResponsavel(),
                request.getCep(),
                request.getLogradouro(),
                request.getNumero(),
                request.getBairro(),
                request.getCidade(),
                request.getEstado(),
                request.getEmailEmpresa(),
                request.getTelefone(),
                request.getSenha(),
                request.getPlanoId()
        })) {
            Toast.makeText(
                    this,
                    "Preencha todos os campos antes de continuar.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
                }

        if (request.getSenha().length() < 8) {
            Toast.makeText(
                    this,
                    "A senha deve ter no mínimo 8 caracteres.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (request.getSenha().matches(".*\\d.*")) {
            Toast.makeText(
                    this,
                    "A senha deve conter pelo menos um número"
                    , Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!request.getSenha().matches(".*[A-Z].*")) {
            Toast.makeText(
                    this,
                    "A senha deve conter pelo menos uma letra maiúscula.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!request.getSenha().matches(".*[a-z].*")) {
            Toast.makeText(
                    this,
                    "A senha deve conter pelo menos uma letra minúscula.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!request.getSenha().matches(".*[!@#$%^&*()_+].*")) {
            Toast.makeText(
                    this,
                    "A senha deve conter pelo menos um caractere especial.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }


        api.cadastrarEmpresa(request).enqueue(new Callback<EmpresaResponse>() {

            @Override
            public void onResponse(
                    Call<EmpresaResponse> call,
                    Response<EmpresaResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    EmpresaResponse empresa = response.body();

                    if (empresa.id == null || empresa.id.isEmpty()) {
                        Toast.makeText(
                                CadastroEmpresa.this,
                                "A API não retornou o ID da empresa.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    getSharedPreferences("mindu", MODE_PRIVATE)
                            .edit()
                            .putString("empresaId", empresa.id)
                            .apply();

                    Toast.makeText(
                            CadastroEmpresa.this,
                            "Empresa cadastrada com sucesso!",
                            Toast.LENGTH_LONG
                    ).show();

                } else {
                    Toast.makeText(
                            CadastroEmpresa.this,
                            "Falha no cadastro. Código HTTP: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<EmpresaResponse> call,
                    Throwable t) {

                Toast.makeText(
                        CadastroEmpresa.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }


}