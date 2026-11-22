package com.example.mindu;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;
import com.example.mindu.api.login.MinhaContaResponse;
import com.google.android.material.textfield.TextInputEditText;

public class LoginProfissional extends AppCompatActivity {
    private LoginFlow loginFlow;
    private TextInputEditText edtEmail;
    private TextInputEditText edtSenha;
    private Button btnEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_profissional);
        LoginBackButton.attach(this);
        loginFlow = new LoginFlow(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BlurTarget blurTargetCadastro = findViewById(R.id.blurTargetCadastro);
        BlurView blurCadastroBeneficiario = findViewById(R.id.blurCadastroBeneficiario);
        blurCadastroBeneficiario.setupWith(blurTargetCadastro).setBlurRadius(20f);

        aplicarEfeitoVidroLiquido(blurCadastroBeneficiario);

        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btnEntrar = findViewById(R.id.button);
        btnEntrar.setOnClickListener(v -> realizarLogin());

        TextView textSolicitaCredenciamento = findViewById(R.id.textCadastreOrg);
        textSolicitaCredenciamento.setOnClickListener(v -> {
            Intent rotacionar = new Intent(LoginProfissional.this, CadastroProfissional.class);
            startActivity(rotacionar);
        });
    }

    private void realizarLogin() {
        String email = edtEmail.getText() == null ? "" : edtEmail.getText().toString().trim();
        String senha = edtSenha.getText() == null ? "" : edtSenha.getText().toString();
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

        btnEntrar.setEnabled(false);
        btnEntrar.setText("Entrando...");
        loginFlow.login(email, senha, "PROFISSIONAL", new LoginFlow.Listener() {
            @Override public void onSuccess(MinhaContaResponse account) {
                Intent intent = new Intent(LoginProfissional.this, DashboardProfissional.class);
                intent.putExtra("nome_profissional", account.getNome());
                startActivity(intent);
                finish();
            }
            @Override public void onError(String message) {
                btnEntrar.setEnabled(true);
                btnEntrar.setText("Entrar");
                Toast.makeText(LoginProfissional.this, message, Toast.LENGTH_LONG).show();
            }
        });
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
