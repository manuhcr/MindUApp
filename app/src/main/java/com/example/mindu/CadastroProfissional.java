package com.example.mindu;


import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

import android.net.Uri;
import java.io.File;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.text.Normalizer;

import com.example.mindu.api.ApiService;
import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.profissional.ProfissionalResponse;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class CadastroProfissional extends AppCompatActivity {

    private ActivityResultLauncher<String> selecionarImagemLauncher;
    private ActivityResultLauncher<Uri> tirarFotoLauncher;

    private Uri fotoCarteirinhaUri;
    private Uri uriCapturaAtual;
    private File arquivoCaptura;

    private ApiService apiService;
    private Button btnCriarConta;
    private retrofit2.Call<ProfissionalResponse> cadastroCall;
    private LoginFlow cadastroLoginFlow;
    private ChipGroup groupTags;
    private EditText campoTag;
    private final Set<String> tagsSelecionadas = new LinkedHashSet<>();
    private boolean processandoVirgula;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_profissional);
        LoginBackButton.attach(this);
        if (savedInstanceState != null) {
            String caminhoCaptura = savedInstanceState.getString("captura_carteirinha");
            if (caminhoCaptura != null) {
                arquivoCaptura = new File(caminhoCaptura);
                uriCapturaAtual = uriDoArquivo(arquivoCaptura);
            }
        }
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
                        apagarArquivoCapturado();
                        fotoCarteirinhaUri = uri;

                        TextView textCarteirinha = findViewById(
                                R.id.textCarteirinhaSelecionada
                        );

                        textCarteirinha.setText("Carteirinha escolhida da galeria.");
                    }
                }
        );

        tirarFotoLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                this::concluirCaptura
        );

        Button btnSelecionarCarteirinha = findViewById(
                R.id.btnSelecionarCarteirinha
        );

        btnSelecionarCarteirinha.setText("Fotografar ou escolher imagem");
        btnSelecionarCarteirinha.setOnClickListener(v -> escolherOrigemCarteirinha());

        btnCriarConta = findViewById(R.id.btnCriarConta);
        btnCriarConta.setOnClickListener(v ->  cadastrar());
        configurarTipoETags();
    }

    private void configurarTipoETags() {
        MaterialAutoCompleteTextView tipo = findViewById(R.id.edtTipoProfissional);
        String[] rotulos = {"Psicólogo", "Psiquiatra", "Terapeuta"};
        tipo.setAdapter(new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, rotulos));
        tipo.setOnClickListener(v -> tipo.showDropDown());
        tipo.setOnFocusChangeListener((v, focused) -> { if (focused) tipo.showDropDown(); });

        groupTags = findViewById(R.id.groupTagsProfissional);
        campoTag = findViewById(R.id.edtTagInput);
        campoTag.setOnEditorActionListener((view, actionId, event) -> {
            boolean enter = actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN);
            if (enter) { adicionarTagDoCampo(); return true; }
            return false;
        });
        campoTag.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!processandoVirgula && s.toString().contains(",")) {
                    processandoVirgula = true;
                    String[] partes = s.toString().split(",", -1);
                    for (int i = 0; i < partes.length - 1; i++) adicionarTag(partes[i]);
                    campoTag.setText(partes[partes.length - 1]);
                    campoTag.setSelection(campoTag.length());
                    processandoVirgula = false;
                }
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void adicionarTagDoCampo() {
        adicionarTag(campoTag.getText().toString());
        campoTag.setText("");
        ((android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(campoTag.getWindowToken(), 0);
    }

    private void adicionarTag(String texto) {
        String tag = texto == null ? "" : texto.trim();
        if (tag.isEmpty()) return;
        for (String existente : tagsSelecionadas) {
            if (existente.equalsIgnoreCase(tag)) return;
        }
        tagsSelecionadas.add(tag);
        Chip chip = new Chip(this);
        chip.setText(tag);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            tagsSelecionadas.remove(tag);
            groupTags.removeView(chip);
        });
        groupTags.addView(chip);
    }

    @Override protected void onDestroy() {
        if (cadastroCall != null) cadastroCall.cancel();
        if (cadastroLoginFlow != null) cadastroLoginFlow.cancel();
        if (!isChangingConfigurations()) apagarArquivoCapturado();
        super.onDestroy();
    }

    private void escolherOrigemCarteirinha() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Carteirinha profissional")
                .setItems(new String[]{"Tirar foto", "Escolher da galeria"}, (dialog, escolha) -> {
                    if (escolha == 0) iniciarCapturaCarteirinha();
                    else selecionarImagemLauncher.launch("image/*");
                })
                .show();
    }

    private void iniciarCapturaCarteirinha() {
        try {
            apagarArquivoCapturado();
            fotoCarteirinhaUri = null;
            arquivoCaptura = File.createTempFile("carteirinha_", ".jpg", getCacheDir());
            uriCapturaAtual = uriDoArquivo(arquivoCaptura);
            tirarFotoLauncher.launch(uriCapturaAtual);
        } catch (Exception erro) {
            apagarArquivoCapturado();
            Toast.makeText(this, "Não foi possível abrir a câmera. Escolha uma foto da galeria.", Toast.LENGTH_LONG).show();
        }
    }

    private Uri uriDoArquivo(File arquivo) {
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", arquivo);
    }

    private void concluirCaptura(boolean sucesso) {
        TextView textoCarteirinha = findViewById(R.id.textCarteirinhaSelecionada);
        if (!sucesso || arquivoCaptura == null || !arquivoCaptura.exists() || arquivoCaptura.length() == 0) {
            apagarArquivoCapturado();
            fotoCarteirinhaUri = null;
            textoCarteirinha.setText("Nenhuma imagem selecionada");
            Toast.makeText(this, "A captura foi cancelada ou não gerou uma imagem.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (arquivoCaptura.length() > 5L * 1024 * 1024) {
            apagarArquivoCapturado();
            fotoCarteirinhaUri = null;
            textoCarteirinha.setText("Nenhuma imagem selecionada");
            Toast.makeText(this, "A foto passou de 5 MB. Tire outra foto mais leve ou escolha uma imagem.", Toast.LENGTH_LONG).show();
            return;
        }
        fotoCarteirinhaUri = uriCapturaAtual;
        textoCarteirinha.setText("Foto da carteirinha capturada.");
    }

    private void apagarArquivoCapturado() {
        if (arquivoCaptura != null && arquivoCaptura.exists()) arquivoCaptura.delete();
        arquivoCaptura = null;
        uriCapturaAtual = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (arquivoCaptura != null && arquivoCaptura.exists()) {
            outState.putString("captura_carteirinha", arquivoCaptura.getAbsolutePath());
        }
        super.onSaveInstanceState(outState);
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
        MaterialAutoCompleteTextView edtTipo = findViewById(R.id.edtTipoProfissional);

        String nome = edtNome.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString();
        String tipo = normalizarTipo(edtTipo.getText().toString());

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            // Exibir mensagem de erro
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (senha.length() < 8) {
            Toast.makeText(this, "A senha deve ter no mínimo 8 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Informe um e-mail válido");
            edtEmail.requestFocus();
            return;
        }
        if (fotoCarteirinhaUri == null) {
            TextView textoCarteirinha = findViewById(R.id.textCarteirinhaSelecionada);
            textoCarteirinha.setText("A carteirinha é obrigatória para enviar o cadastro.");
            Toast.makeText(this, "Selecione uma carteirinha PNG ou JPEG (até 5 MB).", Toast.LENGTH_LONG).show();
            return;
        }

        List<RequestBody> tagsList = new ArrayList<>();
        if (!campoTag.getText().toString().trim().isEmpty()) adicionarTagDoCampo();
        for (String tag : tagsSelecionadas) {
            tagsList.add(RequestBody.create(tag, MediaType.parse("text/plain")));
        }
        MultipartBody.Part fotoCarteirinha;
        String mimeType = getContentResolver().getType(fotoCarteirinhaUri);
        if (mimeType == null) mimeType = "image/jpeg";
        if (!("image/png".equalsIgnoreCase(mimeType) || "image/jpeg".equalsIgnoreCase(mimeType)
                || "image/jpg".equalsIgnoreCase(mimeType))) {
            Toast.makeText(this, "A carteirinha precisa ser uma imagem PNG ou JPEG.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            byte[] bytes = lerArquivoLimitado(fotoCarteirinhaUri, 5 * 1024 * 1024);
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("image/jpg".equalsIgnoreCase(mimeType) ? "image/jpeg" : mimeType), bytes);
            String extensao = "image/png".equalsIgnoreCase(mimeType) ? ".png" : ".jpg";
            fotoCarteirinha = MultipartBody.Part.createFormData("fotoCarteirinha", "carteirinha" + extensao, requestFile);
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage() == null ? "Não foi possível carregar a carteirinha." : e.getMessage(), Toast.LENGTH_LONG).show();
            return;
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
            btnCriarConta.setEnabled(false);
            btnCriarConta.setText("Enviando...");
            cadastroCall = apiService.cadastrar(nomeBody, emailBody, senhaBody, tipoBody, tagsList, fotoCarteirinha);
            cadastroCall.enqueue(
                        new retrofit2.Callback<>() {
                            @Override
                            public void onResponse(
                                    retrofit2.Call<ProfissionalResponse> call,
                                    retrofit2.Response<ProfissionalResponse> response
                            ) {
                                if (isFinishing() || isDestroyed()) return;
                                restaurarBotaoCadastro();
                                if (response.isSuccessful()) {
                                    cadastroLoginFlow = new LoginFlow(CadastroProfissional.this);
                                    cadastroLoginFlow.login(email, senha, "PROFISSIONAL", new LoginFlow.Listener() {
                                        @Override public void onSuccess(com.example.mindu.api.login.MinhaContaResponse account) {
                                            AccountNavigator.open(CadastroProfissional.this, account);
                                        }
                                        @Override public void onError(String message) {
                                            Toast.makeText(CadastroProfissional.this,
                                                    "Cadastro enviado. O login automático falhou; entre pela tela de login.",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });

                                } else {
                                    Log.e("CADASTRO_PRO", "Código: " + response.code());
                                    Toast.makeText(CadastroProfissional.this,
                                            mensagemErroCadastro(response.code(), response.errorBody()),
                                            Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(
                                    retrofit2.Call<ProfissionalResponse> call,
                                    Throwable t
                            ) {
                                if (isFinishing() || isDestroyed()) return;
                                restaurarBotaoCadastro();
                                Toast.makeText(
                                        CadastroProfissional.this,
                                        "Não foi possível conectar à API local (10.0.2.2:8080). Confira se a API e o banco estão iniciados.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private String normalizarTipo(String valor) {
        String tipo = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT);
        if ("PSICOLOGO".equals(tipo)) return "PSICOLOGO";
        if ("PSIQUIATRA".equals(tipo)) return "PSIQUIATRA";
        if ("TERAPEUTA".equals(tipo)) return "TERAPEUTA";
        return "";
    }

    private byte[] lerArquivoLimitado(Uri uri, int limiteBytes) throws IOException {
        InputStream input = getContentResolver().openInputStream(uri);
        if (input == null) throw new IOException("Não foi possível abrir a imagem selecionada.");
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int lidos;
            while ((lidos = stream.read(buffer)) != -1) {
                if (output.size() + lidos > limiteBytes) {
                    throw new IOException("A carteirinha deve ter no máximo 5 MB.");
                }
                output.write(buffer, 0, lidos);
            }
            return output.toByteArray();
        }
    }

    private String mensagemErroCadastro(int codigo, okhttp3.ResponseBody errorBody) {
        String detalhe = null;
        if (errorBody != null) {
            try {
                org.json.JSONObject json = new org.json.JSONObject(errorBody.string());
                detalhe = json.optString("erro", null);
                org.json.JSONObject campos = json.optJSONObject("campos");
                if (campos != null && campos.keys().hasNext()) {
                    String campo = campos.keys().next();
                    detalhe = campos.optString(campo, detalhe);
                }
            } catch (Exception ignored) { }
        }
        if (detalhe != null && !detalhe.trim().isEmpty()) return detalhe;
        if (codigo == 409) return "Este e-mail já está cadastrado.";
        if (codigo == 413) return "A imagem enviada excede o limite permitido.";
        if (codigo >= 500) return "A API local não conseguiu concluir o cadastro. Tente novamente.";
        return "A API recusou o cadastro (HTTP " + codigo + "). Confira os campos e a carteirinha.";
    }

    private void restaurarBotaoCadastro() {
        btnCriarConta.setEnabled(true);
        btnCriarConta.setText("Criar conta");
    }
}
