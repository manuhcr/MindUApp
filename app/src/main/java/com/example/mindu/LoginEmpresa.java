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

import com.example.mindu.api.login.MinhaContaResponse;
import com.google.android.material.textfield.TextInputEditText;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

public class LoginEmpresa extends AppCompatActivity {

    private LoginFlow loginFlow;
    private Button btnEntrar;
    private com.google.android.material.textfield.TextInputEditText edtEmail;
    private com.google.android.material.textfield.TextInputEditText edtSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_empresa);
        LoginBackButton.attach(this);
        loginFlow = new LoginFlow(this);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btnEntrar = findViewById(R.id.button);

        btnEntrar.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String senha = edtSenha.getText().toString();

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


            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha o e-mail e a senha!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            setLoading(true);
            loginFlow.login(email, senha, "EMPRESA", new LoginFlow.Listener() {
                @Override public void onSuccess(MinhaContaResponse account) {
                    startActivity(new Intent(LoginEmpresa.this, DashboardEmpresa.class));
                    finish();
                }
                @Override public void onError(String message) {
                    setLoading(false);
                    Toast.makeText(LoginEmpresa.this, message, Toast.LENGTH_LONG).show();
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

    private void setLoading(boolean loading) {
        btnEntrar.setEnabled(!loading);
        btnEntrar.setText(loading ? "Entrando..." : "Entrar");
    }

    @Override protected void onDestroy() {
        if (loginFlow != null) loginFlow.cancel();
        super.onDestroy();
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
