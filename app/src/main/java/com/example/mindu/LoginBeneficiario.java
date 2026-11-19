
package com.example.mindu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

import com.example.mindu.api.ApiService;
import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.login.LoginRequest;
import com.example.mindu.api.login.LoginResponse;
import com.example.mindu.api.login.MinhaContaResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginBeneficiario extends AppCompatActivity {

    private TextInputEditText edtEmail, edtSenha;
    private Button button;
    private SharedPreferences preferences;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_beneficiario);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top,
                    systemBars.right, systemBars.bottom);
            return insets;
        });

        preferences = getSharedPreferences("mindu", MODE_PRIVATE);
        api = RetrofitClient.getApiService(this);

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

        if (senha.isEmpty()) {
            edtSenha.setError("Informe sua senha");
            edtSenha.requestFocus();
            return;
        }

        button.setEnabled(false);
        button.setText("Entrando...");

        // Remove um token anterior para evitar reutilizar outra sessão.
        preferences.edit().remove("token").apply();

        LoginRequest request = new LoginRequest(email, senha);

        api.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call,
                                   Response<LoginResponse> response) {

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getToken() != null) {

                    String token = response.body().getToken();

                    preferences.edit()
                            .putString("token", token)
                            .apply();

                    verificarBeneficiario();

                } else {
                    restaurarBotao();
                    Toast.makeText(
                            LoginBeneficiario.this,
                            "E-mail ou senha inválidos.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                restaurarBotao();

                Toast.makeText(
                        LoginBeneficiario.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void verificarBeneficiario() {

        api.minhaConta().enqueue(new Callback<MinhaContaResponse>() {
            @Override
            public void onResponse(Call<MinhaContaResponse> call,
                                   Response<MinhaContaResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    MinhaContaResponse conta = response.body();

                    boolean ehCliente = "CLIENTE".equalsIgnoreCase(conta.getTipo());
                    boolean aprovado = "APROVADO".equalsIgnoreCase(conta.getStatus());

                    if (ehCliente && aprovado) {

                        Toast.makeText(
                                LoginBeneficiario.this,
                                "Login realizado com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent = new Intent(
                                LoginBeneficiario.this,
                                DashboardBeneficiario.class
                        );

                        startActivity(intent);
                        finish();

                    } else {
                        preferences.edit().remove("token").apply();
                        restaurarBotao();

                        Toast.makeText(
                                LoginBeneficiario.this,
                                "Sua conta ainda não está aprovada ou não possui acesso de beneficiário.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                } else {
                    preferences.edit().remove("token").apply();
                    restaurarBotao();

                    Toast.makeText(
                            LoginBeneficiario.this,
                            "Não foi possível validar sua conta.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<MinhaContaResponse> call, Throwable t) {
                preferences.edit().remove("token").apply();
                restaurarBotao();

                Toast.makeText(
                        LoginBeneficiario.this,
                        "Erro ao consultar sua conta: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void restaurarBotao() {
        button.setEnabled(true);
        button.setText("Entrar");
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
