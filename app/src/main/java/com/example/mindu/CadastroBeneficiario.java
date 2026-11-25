
package com.example.mindu;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mindu.api.ApiService;
import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.cliente.ClienteRequest;
import com.example.mindu.api.cliente.ClienteResponse;
import com.example.mindu.api.empresas.EmpresaDaMatriculaResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroBeneficiario extends AppCompatActivity {

    private TextInputEditText edtNome;
    private TextInputEditText edtEmailCorp;
    private TextInputEditText edtMatricula;
    private TextInputEditText edtSenha;

    private TextInputLayout inputMatricula;
    private TextView txtEmpresaValidada;
    private Button button;

    private ApiService apiService;

    private String empresaIdValidada = null;
    private String matriculaValidada = null;

    private boolean validandoMatricula = false;
    private boolean cadastrando = false;

    private Call<EmpresaDaMatriculaResponse> chamadaValidacao;
    private Call<ClienteResponse> chamadaCadastro;
    private LoginFlow cadastroLoginFlow;

    @Override
    protected void onDestroy() {
        if (chamadaValidacao != null) chamadaValidacao.cancel();
        if (chamadaCadastro != null) chamadaCadastro.cancel();
        if (cadastroLoginFlow != null) cadastroLoginFlow.cancel();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_beneficiario);
        LoginBackButton.attach(this);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {

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
                }
        );

        // BlurView
        BlurTarget blurTargetCadastro =
                findViewById(R.id.blurTargetCadastro);

        BlurView blurCadastroBeneficiario =
                findViewById(R.id.blurCadastroBeneficiario);

        blurCadastroBeneficiario
                .setupWith(blurTargetCadastro)
                .setBlurRadius(20f);

        aplicarEfeitoVidroLiquido(blurCadastroBeneficiario);

        // API
        apiService = RetrofitClient.getApiService(this);

        // Campos
        edtNome = findViewById(R.id.edtNome);
        edtEmailCorp = findViewById(R.id.edtEmailCorp);
        edtMatricula = findViewById(R.id.edtMatricula);
        edtSenha = findViewById(R.id.edtSenha);

        inputMatricula = findViewById(R.id.inputMatricula);
        txtEmpresaValidada = findViewById(R.id.txtEmpresaValidada);
        button = findViewById(R.id.button);

        configurarValidacaoMatricula();

        button.setOnClickListener(v -> cadastrarBeneficiario());
    }

    // =========================================
    // VALIDAÇÃO DA MATRÍCULA
    // =========================================

    private void configurarValidacaoMatricula() {

        // Detecta quando a pessoa sai do campo
        edtMatricula.setOnFocusChangeListener((view, temFoco) -> {

            if (!temFoco) {
                validarMatricula();
            }
        });

        // Se a matrícula mudar, invalida a empresa anterior
        edtMatricula.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                if (chamadaValidacao != null) {
                    chamadaValidacao.cancel();
                    chamadaValidacao = null;
                }

                validandoMatricula = false;
                empresaIdValidada = null;
                matriculaValidada = null;

                inputMatricula.setError(null);

                txtEmpresaValidada.setText(
                        "A empresa será identificada pela matrícula."
                );

                button.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void validarMatricula() {

        String codigo = edtMatricula.getText()
                .toString()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (codigo.isEmpty()) {
            empresaIdValidada = null;
            matriculaValidada = null;

            txtEmpresaValidada.setText(
                    "Digite sua matrícula para identificar a empresa."
            );

            return;
        }

        // Evita consultar novamente uma matrícula já validada
        if (codigo.equals(matriculaValidada)
                && empresaIdValidada != null) {
            return;
        }

        if (validandoMatricula) {
            return;
        }

        validandoMatricula = true;

        inputMatricula.setError(null);

        txtEmpresaValidada.setText("Validando matrícula...");

        chamadaValidacao = apiService.validarMatricula(codigo);

        chamadaValidacao.enqueue(
                new Callback<EmpresaDaMatriculaResponse>() {

                    @Override
                    public void onResponse(
                            Call<EmpresaDaMatriculaResponse> call,
                            Response<EmpresaDaMatriculaResponse> response) {

                        if (!codigo.equals(
                                edtMatricula.getText()
                                        .toString()
                                        .trim()
                                        .toUpperCase(Locale.ROOT))) {
                            return;
                        }

                        validandoMatricula = false;

                        if (response.isSuccessful()
                                && response.body() != null) {

                            EmpresaDaMatriculaResponse empresa =
                                    response.body();

                            empresaIdValidada = empresa.getEmpresaId();
                            matriculaValidada = codigo;

                            if (empresaIdValidada == null
                                    || empresaIdValidada.isEmpty()) {

                                empresaIdValidada = null;
                                matriculaValidada = null;

                                inputMatricula.setError(
                                        "Não foi possível identificar a empresa."
                                );

                                txtEmpresaValidada.setText(
                                        "Não foi possível identificar a empresa."
                                );

                                return;
                            }

                            txtEmpresaValidada.setText(
                                    "Empresa identificada: "
                                            + empresa.getNomeFantasia()
                            );

                            inputMatricula.setError(null);

                        } else {

                            empresaIdValidada = null;
                            matriculaValidada = null;

                            inputMatricula.setError(
                                    "Matrícula inválida."
                            );

                            txtEmpresaValidada.setText(
                                    "Não encontramos uma empresa para essa matrícula."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<EmpresaDaMatriculaResponse> call,
                            Throwable t) {

                        if (call.isCanceled()) {
                            return;
                        }

                        validandoMatricula = false;

                        if (!codigo.equals(
                                edtMatricula.getText()
                                        .toString()
                                        .trim()
                                        .toUpperCase(Locale.ROOT))) {
                            return;
                        }

                        empresaIdValidada = null;
                        matriculaValidada = null;

                        txtEmpresaValidada.setText(
                                "Erro ao validar. Verifique sua conexão."
                        );

                        Toast.makeText(
                                CadastroBeneficiario.this,
                                "Não foi possível validar a matrícula.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // =========================================
    // CADASTRO DO BENEFICIÁRIO
    // =========================================

    private void cadastrarBeneficiario() {

        String nome = edtNome.getText().toString().trim();
        String email = edtEmailCorp.getText().toString().trim();
        String matricula = edtMatricula.getText()
                .toString()
                .trim()
                .toUpperCase(Locale.ROOT);
        String senha = edtSenha.getText().toString();

        // Verifica os campos
        if (nome.isEmpty()) {
            edtNome.setError("Informe seu nome.");
            edtNome.requestFocus();
            return;
        }

        if (email.isEmpty()
                || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            edtEmailCorp.setError("Informe um e-mail válido.");
            edtEmailCorp.requestFocus();
            return;
        }

        if (matricula.isEmpty()) {
            edtMatricula.setError("Informe sua matrícula.");
            edtMatricula.requestFocus();
            return;
        }

        if (senha.isEmpty()) {
            edtSenha.setError("Informe sua senha.");
            edtSenha.requestFocus();
            return;
        }
        if (senha.length() < 8) {
            edtSenha.setError("A senha deve ter no mínimo 8 caracteres.");
            edtSenha.requestFocus();
            return;
        }

        // Exige matrícula validada
        if (empresaIdValidada == null
                || matriculaValidada == null
                || !matricula.equals(matriculaValidada)) {

            Toast.makeText(
                    this,
                    "Valide sua matrícula antes de continuar.",
                    Toast.LENGTH_SHORT
            ).show();

            edtMatricula.requestFocus();

            return;
        }

        if (validandoMatricula || cadastrando) {
            return;
        }

        cadastrando = true;
        button.setEnabled(false);

        // Monta o objeto com os cinco campos
        ClienteRequest request = new ClienteRequest(
                nome,
                email,
                senha,
                empresaIdValidada,
                matricula
        );

        // Envia para a API
        chamadaCadastro = apiService.cadastrarCliente(request);
        chamadaCadastro.enqueue(
                new Callback<ClienteResponse>() {

                    @Override
                    public void onResponse(
                            Call<ClienteResponse> call,
                            Response<ClienteResponse> response) {

                        if (isFinishing() || isDestroyed()) return;
                        cadastrando = false;
                        button.setEnabled(true);

                        if (response.isSuccessful()
                                && response.body() != null) {

                            cadastroLoginFlow = new LoginFlow(CadastroBeneficiario.this);
                            cadastroLoginFlow.login(request.getEmail(), request.getSenha(), "CLIENTE",
                                    new LoginFlow.Listener() {
                                        @Override public void onSuccess(com.example.mindu.api.login.MinhaContaResponse account) {
                                            AccountNavigator.open(CadastroBeneficiario.this, account);
                                        }
                                        @Override public void onError(String message) {
                                            Toast.makeText(CadastroBeneficiario.this,
                                                    "Cadastro realizado. O login automático falhou; entre pela tela de login.",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });

                        } else {

                            Toast.makeText(
                                    CadastroBeneficiario.this,
                                    "Não foi possível realizar o cadastro. Verifique seus dados.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ClienteResponse> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) return;
                        cadastrando = false;
                        button.setEnabled(true);

                        Toast.makeText(
                                CadastroBeneficiario.this,
                                "Erro de conexão com o servidor.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // =========================================
    // EFEITO VIDRO LÍQUIDO
    // =========================================

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
