package com.example.mindu;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

import android.net.Uri;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.example.mindu.api.ApiService;
import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.profissional.ProfissionalResponse;

public class CadastroProfissional extends AppCompatActivity {

    private ActivityResultLauncher<String> selecionarImagemLauncher;

    private Uri fotoCarteirinhaUri;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_profissional);
        View main = findViewById(R.id.main);
        if (main != null) {
            ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
                Insets systemBars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                );

                v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom
                );

                return insets;
            });
        }

        BlurTarget blurTargetCadastro = findViewById(
                R.id.blurTargetCadastro
        );

        BlurView blurCadastroProfissional = findViewById(
                R.id.blurCadastroProfissional
        );

        if (blurTargetCadastro != null && blurCadastroProfissional != null) {
            blurCadastroProfissional
                    .setupWith(blurTargetCadastro)
                    .setBlurRadius(20f);

            aplicarEfeitoVidroLiquido(blurCadastroProfissional);
        }

        // Referências das etapas
        View layoutEtapa1 = findViewById(R.id.layoutEtapa1);
        View layoutEtapa2 = findViewById(R.id.layoutEtapa2);

        // Indicadores de progresso
        View progressoEtapa1 = findViewById(R.id.progressoEtapa1);
        View progressoEtapa2 = findViewById(R.id.progressoEtapa2);

        Button buttonProximaEtapa = findViewById(R.id.buttonProximaEtapa);
        Button buttonVoltarEtapa = findViewById(R.id.buttonVoltarEtapa);

        // Avançar para a segunda etapa
        buttonProximaEtapa.setOnClickListener(v -> {
            layoutEtapa1.setVisibility(View.GONE);
            layoutEtapa2.setVisibility(View.VISIBLE);

            progressoEtapa1.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#75689A"))
            );

            progressoEtapa2.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#75689A"))
            );
        });

        // Voltar para a primeira etapa
        buttonVoltarEtapa.setOnClickListener(v -> {
            layoutEtapa2.setVisibility(View.GONE);
            layoutEtapa1.setVisibility(View.VISIBLE);
        });

        selecionarImagemLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        fotoCarteirinhaUri = uri;

                        TextView textCarteirinha = findViewById(
                                R.id.textCarteirinhaSelecionada
                        );

                        textCarteirinha.setText("Carteirinha selecionada!");
                    }
                }
        );

        Button btnSelecionarCarteirinha = findViewById(
                R.id.btnSelecionarCarteirinha
        );

        btnSelecionarCarteirinha.setOnClickListener(v -> {
            selecionarImagemLauncher.launch("image/*");
        });

        Button btnCriarConta = findViewById(R.id.btnCriarConta);
        btnCriarConta.setOnClickListener(v ->  cadastrar());
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

        view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        view.setClipToOutline(true);
        view.setElevation(4 * density);
    }

    private void cadastrar(){
        EditText edtNome = findViewById(R.id.edtNomeProfissional);
        EditText edtEmail = findViewById(R.id.edtEmailProfissional);
        EditText edtSenha = findViewById(R.id.edtSenha);
        EditText edtTipo = findViewById(R.id.edtTipoProfissional);
        EditText edtTags = findViewById(R.id.edtTags);

        String nome = edtNome.getText().toString();
        String email = edtEmail.getText().toString();
        String senha = edtSenha.getText().toString();
        String tipo = edtTipo.getText().toString();


        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            // Exibir mensagem de erro
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (senha.length() < 8) {
            // Exibir mensagem de erro
            Toast.makeText(this, "A senha deve ter no mínimo 8 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] tags = edtTags.getText().toString().split(",");
        List<RequestBody> tagsList = new ArrayList<>();
        for (String tag : tags) {
            String tagLimpa = tag.trim();

            if (!tagLimpa.isEmpty()) {
                RequestBody tagBody = RequestBody.create(
                        tagLimpa,
                        MediaType.parse("text/plain")
                );

                tagsList.add(tagBody);
            }
        }
        MultipartBody.Part fotoCarteirinha = null;
            if (fotoCarteirinhaUri != null) {
                try {
                    String mimeType = getContentResolver()
                            .getType(fotoCarteirinhaUri);

                    if (mimeType == null) {
                        mimeType = "image/jpeg";
                    }

                    InputStream inputStream = getContentResolver()
                            .openInputStream(fotoCarteirinhaUri);

                    if (inputStream != null) {
                        byte[] bytes = inputStream.readAllBytes();
                        inputStream.close();

                        RequestBody requestFile = RequestBody.create(
                                MediaType.parse(mimeType),
                                bytes
                        );

                        fotoCarteirinha = MultipartBody.Part.createFormData(
                                "fotoCarteirinha",
                                "carteirinha",
                                requestFile
                        );
                    }

                } catch (IOException e) {
                    e.printStackTrace();

                    Toast.makeText(
                            this,
                            "Não foi possível carregar a carteirinha.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }
            }

            apiService = RetrofitClient.getApiService(this);
            RequestBody nomeBody = RequestBody.create(
                    nome,
                    MediaType.parse("text/plain")
            );
            RequestBody emailBody = RequestBody.create(
                    email,
                    MediaType.parse("text/plain")
            );
            RequestBody senhaBody = RequestBody.create(
                    senha,
                    MediaType.parse("text/plain")
            );
            RequestBody tipoBody = null;
            if (!tipo.isEmpty()) {
                tipoBody = RequestBody.create(
                        tipo,
                        MediaType.parse("text/plain")
                );
            }
            List<RequestBody> tagsParaEnviar = tagsList.isEmpty() ? null : tagsList;
            if (tagsParaEnviar == null) {
                tagsParaEnviar = new ArrayList<>();
            }

            apiService.cadastrar(nomeBody, emailBody, senhaBody, tipoBody, tagsList, fotoCarteirinha)
                    .enqueue(
                        new retrofit2.Callback<>() {
                            @Override
                            public void onResponse(
                                    retrofit2.Call<ProfissionalResponse> call,
                                    retrofit2.Response<ProfissionalResponse> response
                            ) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(
                                            CadastroProfissional.this,
                                            "Cadastro realizado com sucesso!",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    finish();

                                } else {
                                    Log.e("CADASTRO_PRO", "Código: " + response.code());

                                    try {
                                        Log.e("CADASTRO_PRO", "Erro: "
                                                + response.errorBody().string());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(
                                    retrofit2.Call<ProfissionalResponse> call,
                                    Throwable t
                            ) {
                                Toast.makeText(
                                        CadastroProfissional.this,
                                        "Erro ao realizar o cadastro.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
        }
    }


