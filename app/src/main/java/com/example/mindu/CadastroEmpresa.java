package com.example.mindu;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;
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
import com.google.android.material.textfield.TextInputEditText;

import java.lang.annotation.Target;

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

        Button btnCriarConta = findViewById(R.id.btnCriarConta);
        btnCriarConta.setOnClickListener(v -> {
            // Lógica para criar a conta
            TextInputEditText edtRazaoSocial = findViewById(R.id.edtRazaoSocial);
            TextInputEditText edtNomeFantasia = findViewById(R.id.edtNomeFantasia);
            TextInputEditText edtCnpj = findViewById(R.id.edtCnpj);
            TextInputEditText edtInscricaoEstadual = findViewById(R.id.edtInscricaoEstadual);
            TextInputEditText edtResponsavel = findViewById(R.id.edtResponsavel);
            TextInputEditText edtCep = findViewById(R.id.edtCep);
            TextInputEditText edtLogradouro = findViewById(R.id.edtLogradouro);
            TextInputEditText edtNumero = findViewById(R.id.edtNumero);
            TextInputEditText edtComplemento = findViewById(R.id.edtComplemento);
            TextInputEditText edtBairro = findViewById(R.id.edtBairro);
            TextInputEditText edtCidade = findViewById(R.id.edtCidade);
            TextInputEditText edtEstado = findViewById(R.id.edtEstado);
            TextInputEditText edtEmailEmpresa = findViewById(R.id.edtEmailEmpresa);
            TextInputEditText edtTelefone = findViewById(R.id.edtTelefone);
            TextInputEditText edtSenha = findViewById(R.id.edtSenha);
            TextInputEditText edtPlanoId = findViewById(R.id.edtPlanoId);

            String razaoSocial = edtRazaoSocial.getText().toString();
            String nomeFantasia = edtNomeFantasia.getText().toString();
            String cnpj = edtCnpj.getText().toString();
            String inscricaoEstadual = edtInscricaoEstadual.getText().toString();
            String responsavel = edtResponsavel.getText().toString();
            String cep = edtCep.getText().toString();
            String logradouro = edtLogradouro.getText().toString();
            String numero = edtNumero.getText().toString();
            String complemento = edtComplemento.getText().toString();
            String bairro = edtBairro.getText().toString();
            String cidade = edtCidade.getText().toString();
            String estado = edtEstado.getText().toString();
            String emailEmpresa = edtEmailEmpresa.getText().toString();
            String telefone = edtTelefone.getText().toString();
            String senha = edtSenha.getText().toString();
            String planoId = edtPlanoId.getText().toString();

            String[] campos = {
                    razaoSocial, nomeFantasia, cnpj, inscricaoEstadual, responsavel,
                    cep, logradouro, numero, complemento, bairro, cidade, estado,
                    emailEmpresa, telefone, senha, planoId
            };


            EmpresaRequest request = new EmpresaRequest();
            request.setRazaoSocial(razaoSocial);
            request.setNomeFantasia(nomeFantasia);
            request.setCnpj(cnpj);
            request.setInscricaoEstadual(inscricaoEstadual);
            request.setResponsavel(responsavel);
            request.setCep(cep);
            request.setLogradouro(logradouro);
            request.setNumero(numero);
            request.setComplemento(complemento);
            request.setBairro(bairro);
            request.setCidade(cidade);
            request.setEstado(estado);
            request.setEmailEmpresa(emailEmpresa);
            request.setTelefone(telefone);
            request.setSenha(senha);
            request.setPlanoId(planoId);
            // Enviar a solicitação para API
            api.cadastrarEmpresa(request).enqueue(new Callback<EmpresaResponse>() {

                @Override
                public void onResponse(Call<EmpresaResponse> call, Response<EmpresaResponse> response) {
                     if (response.isSuccessful()) {
                         EmpresaResponse empresaResponse = response.body();
                         // Lógica para lidar com a resposta da API
                         Toast.makeText(CadastroEmpresa.this,
                                 "Empresa cadastrada com sucesso!",
                                 Toast.LENGTH_SHORT).show();
                         Intent rotacionar = new Intent(CadastroEmpresa.this, LoginEmpresa.class);
                         startActivity(rotacionar);
                     }
                     if (camposVazios(campos)) {
                         Toast.makeText(CadastroEmpresa.this,
                                 "Preencha todos os campos!",
                                 Toast.LENGTH_SHORT).show();

                         return;

                     }
                }

                @Override
                public void onFailure(Call<EmpresaResponse> call, Throwable throwable) {
                    if (throwable != null) {
                        Toast.makeText(CadastroEmpresa.this,
                                "Erro ao cadastrar empresa: " + throwable.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });



        });

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

}