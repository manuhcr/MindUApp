package com.example.mindu;

import android.content.Intent;
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

import com.example.mindu.api.ApiService;
import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.login.LoginRequest;
import com.example.mindu.api.login.LoginResponse;
import com.google.android.material.textfield.TextInputEditText;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginEmpresa extends AppCompatActivity {

    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_empresa);
        api = RetrofitClient.getApiService(this);
        TextInputEditText edtEmail = findViewById(R.id.edtEmail);
        TextInputEditText edtSenha = findViewById(R.id.edtSenha);
        Button btnEntrar = findViewById(R.id.button);

        btnEntrar.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String senha = edtSenha.getText().toString();

            if (email.isEmpty() || senha.isEmpty()){
                Toast.makeText(this, "Preencha o e-mail e a senha!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            LoginRequest dados = new LoginRequest(email, senha);

            api.login(dados).enqueue(new Callback<>() {
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> res) {
                    if (res.isSuccessful() && res.body() != null) {
                        String token = res.body().getToken();

                        getSharedPreferences("mindu", MODE_PRIVATE)
                                .edit()
                                .putString("token", token)
                                .apply();

                        Toast.makeText(LoginEmpresa.this,
                                "Login realizado!", Toast.LENGTH_SHORT).show();

                        Intent rotacionar = new Intent(LoginEmpresa.this, DashboardEmpresa.class);
                        startActivity(rotacionar);
                    } else {
                        Toast.makeText(LoginEmpresa.this,
                                "Não foi possível entrar. Confira os dados.",
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(LoginEmpresa.this,
                            "Erro de conexão: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BlurTarget blurTargetCadastro = findViewById(R.id.blurTargetCadastro);
        BlurView blurCadastroBeneficiario = findViewById(R.id.blurCadastroBeneficiario);
        blurCadastroBeneficiario.setupWith(blurTargetCadastro).setBlurRadius(20f);

        aplicarEfeitoVidroLiquido(blurCadastroBeneficiario);

        TextView textCadastreOrg = findViewById(R.id.textCadastreOrg);
        textCadastreOrg.setOnClickListener(v -> {
            Intent rotacionar = new Intent(LoginEmpresa.this, CadastroEmpresa.class);
            startActivity(rotacionar);
        });
    }

    private void aplicarEfeitoVidroLiquido(View view) {
        if (view == null) return;
        GradientDrawable liquidGlass = new GradientDrawable();
        liquidGlass.setShape(GradientDrawable.RECTANGLE);
        liquidGlass.setOrientation(GradientDrawable.Orientation.TL_BR);




        liquidGlass.setColor(
                Color.parseColor("#1F8294EF")
        );
        /*
         * Valores como "24dp" precisam ser convertidos
         * para pixels quando trabalhamos diretamente com Java.
         *
         * density informa a densidade da tela.
         */
        float density = view.getContext()
                .getResources()
                .getDisplayMetrics()
                .density;
        liquidGlass.setCornerRadius(24 * density);
        int strokeWidthPx = (int) (1 * density);
        liquidGlass.setStroke(strokeWidthPx, Color.parseColor("#99F5F3F7"));
        view.setBackground(liquidGlass);
        view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);

        view.setClipToOutline(true);

        view.setElevation(5 * density);

    }



}