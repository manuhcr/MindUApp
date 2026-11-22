
package com.example.mindu;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
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

import com.google.android.material.textfield.TextInputEditText;
import com.example.mindu.api.cliente.dados.DadosSensiveisRequest;
import com.example.mindu.api.cliente.dados.DadosSensiveisResponse;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;


public class LoginBeneficiario extends AppCompatActivity {

    private TextInputEditText edtEmail, edtSenha;
    private Button button;
    private LoginFlow loginFlow;
    private retrofit2.Call<DadosSensiveisResponse> dadosCall;
    private retrofit2.Call<com.example.mindu.api.cliente.ClienteResponse> envioDadosCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_beneficiario);
        LoginBackButton.attach(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top,
                    systemBars.right, systemBars.bottom);
            return insets;
        });

        loginFlow = new LoginFlow(this);

        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        button = findViewById(R.id.button);

        BlurTarget blurTargetCadastro = findViewById(R.id.blurTargetCadastro);
        BlurView blurCadastroBeneficiario = findViewById(R.id.blurCadastroBeneficiario);

        blurCadastroBeneficiario.setupWith(blurTargetCadastro)
                .setBlurRadius(35f);

        aplicarEfeitoVidroLiquido(blurCadastroBeneficiario);

        TextView textAtivaBeneficio = findViewById(R.id.textCadastreOrg);
        textAtivaBeneficio.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginBeneficiario.this,
                    CadastroBeneficiario.class
            );
            startActivity(intent);
        });

        button.setOnClickListener(v -> realizarLogin());
    }

    private void realizarLogin() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        if (email.isEmpty()) {
            edtEmail.setError("Informe seu e-mail");
            edtEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Informe um e-mail válido");
            edtEmail.requestFocus();
            return;
        }

        if (senha.isEmpty()) {
            edtSenha.setError("Informe sua senha");
            edtSenha.requestFocus();
            return;
        }

        setLoading(true);
        loginFlow.login(email, senha, "CLIENTE", new LoginFlow.Listener() {
            @Override public void onSuccess(com.example.mindu.api.login.MinhaContaResponse account) {
                verificarDadosSensiveis();
            }
            @Override public void onError(String message) {
                setLoading(false);
                Toast.makeText(LoginBeneficiario.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        button.setEnabled(!loading);
        button.setText(loading ? "Entrando..." : "Entrar");
    }

    private void verificarDadosSensiveis() {
        button.setEnabled(false);
        button.setText("Verificando perfil...");
        dadosCall = com.example.mindu.api.RetrofitClient.getApiService(this).meusDadosSensiveis();
        dadosCall.enqueue(new retrofit2.Callback<DadosSensiveisResponse>() {
            @Override public void onResponse(retrofit2.Call<DadosSensiveisResponse> call,
                                             retrofit2.Response<DadosSensiveisResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                button.setEnabled(true);
                button.setText("Entrar");
                if (response.isSuccessful() && response.body() != null && response.body().estaCompleto()) {
                    abrirDashboard();
                } else if (response.isSuccessful()) {
                    solicitarDadosSensiveis();
                } else {
                    SessionManager.clear(LoginBeneficiario.this);
                    Toast.makeText(LoginBeneficiario.this, "Não foi possível verificar os dados do perfil. Tente entrar novamente.", Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(retrofit2.Call<DadosSensiveisResponse> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                SessionManager.clear(LoginBeneficiario.this);
                button.setEnabled(true);
                button.setText("Entrar");
                Toast.makeText(LoginBeneficiario.this, "Não foi possível conectar à API local para verificar o perfil.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void solicitarDadosSensiveis() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        form.setPadding(padding, padding, padding, 0);
        EditText cns = new EditText(this);
        cns.setHint("CNS");
        cns.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        form.addView(cns);
        EditText nomeMae = new EditText(this);
        nomeMae.setHint("Nome da mãe");
        nomeMae.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        form.addView(nomeMae);
        final String[] nascimento = {""};
        android.widget.Button data = new android.widget.Button(this);
        data.setText("Selecionar data de nascimento");
        data.setOnClickListener(v -> {
            java.util.Calendar hoje = java.util.Calendar.getInstance();
            DatePickerDialog picker = new DatePickerDialog(this, (DatePicker view, int year, int month, int day) -> {
                nascimento[0] = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month + 1, day);
                data.setText(String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year));
            }, hoje.get(java.util.Calendar.YEAR), hoje.get(java.util.Calendar.MONTH), hoje.get(java.util.Calendar.DAY_OF_MONTH));
            picker.getDatePicker().setMaxDate(System.currentTimeMillis());
            picker.show();
        });
        form.addView(data);
        Spinner genero = new Spinner(this);
        String[] valores = {"", "MASCULINO", "FEMININO", "NAO_BINARIO", "AGENERO", "OUTRO", "PREFIRO_NAO_DIZER"};
        String[] rotulos = {"Selecione o gênero", "Masculino", "Feminino", "Não binário", "Agênero", "Outro", "Prefiro não dizer"};
        genero.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, rotulos));
        form.addView(genero);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Complete seus dados de saúde")
                .setMessage("A empresa aprovou sua conta. Preencha os dados obrigatórios para continuar.")
                .setView(form)
                .setCancelable(false)
                .setPositiveButton("Salvar", null)
                .setNegativeButton("Sair", (d, which) -> { SessionManager.clear(this); finish(); })
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String valorCns = cns.getText().toString().trim();
            String valorMae = nomeMae.getText().toString().trim();
            if (valorCns.isEmpty() || valorMae.isEmpty() || nascimento[0].isEmpty() || genero.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Preencha CNS, nome da mãe, data de nascimento e gênero.", Toast.LENGTH_LONG).show();
                return;
            }
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            DadosSensiveisRequest body = new DadosSensiveisRequest(valorCns, nascimento[0], valorMae, valores[genero.getSelectedItemPosition()]);
            envioDadosCall = com.example.mindu.api.RetrofitClient.getApiService(this).completarMeuPerfil(body);
            envioDadosCall.enqueue(new retrofit2.Callback<com.example.mindu.api.cliente.ClienteResponse>() {
                @Override public void onResponse(retrofit2.Call<com.example.mindu.api.cliente.ClienteResponse> call, retrofit2.Response<com.example.mindu.api.cliente.ClienteResponse> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (response.isSuccessful()) { dialog.dismiss(); abrirDashboard(); }
                    else {
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        Toast.makeText(LoginBeneficiario.this, "A API recusou os dados (HTTP " + response.code() + "). Confira e tente novamente.", Toast.LENGTH_LONG).show();
                    }
                }
                @Override public void onFailure(retrofit2.Call<com.example.mindu.api.cliente.ClienteResponse> call, Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                    Toast.makeText(LoginBeneficiario.this, "Não foi possível conectar à API local. Tente novamente.", Toast.LENGTH_LONG).show();
                }
            });
        }));
        dialog.show();
    }

    private void abrirDashboard() {
        startActivity(new Intent(LoginBeneficiario.this, DashboardBeneficiario.class));
        finish();
    }

    @Override protected void onDestroy() {
        if (loginFlow != null) loginFlow.cancel();
        if (dadosCall != null) dadosCall.cancel();
        if (envioDadosCall != null) envioDadosCall.cancel();
        super.onDestroy();
    }

    private void aplicarEfeitoVidroLiquido(View view) {
        if (view == null) return;

        GradientDrawable liquidGlass = new GradientDrawable();
        liquidGlass.setShape(GradientDrawable.RECTANGLE);
        liquidGlass.setOrientation(GradientDrawable.Orientation.TL_BR);
        liquidGlass.setColor(Color.parseColor("#1F8294EF"));

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
        view.setElevation(5 * density);
    }
}
