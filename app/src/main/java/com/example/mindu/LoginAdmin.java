package com.example.mindu;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mindu.api.login.MinhaContaResponse;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

public class LoginAdmin extends AppCompatActivity {
    private LoginFlow loginFlow;
    private Button btnEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_admin);
        LoginBackButton.attach(this);
        loginFlow = new LoginFlow(this);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // CORREÇÃO: Pegando o ID correto do BlurTarget (fundo) e do BlurView (vidro)
        // No XML activity_login_admin.xml, o fundo é 'blurTargetAdmin' e o vidro é 'blurCadastroAdmin'
        BlurTarget blurTarget = findViewById(R.id.blurTargetAdmin);
        BlurView blurView = findViewById(R.id.blurCadastroAdmin);

        if (blurView != null && blurTarget != null) {
            blurView.setupWith(blurTarget)
                    .setBlurRadius(20f);

            aplicarEfeitoVidroLiquido(blurView);
        }

        EditText edtEmail = findViewById(R.id.edtEmailAdmin);
        EditText edtSenha = findViewById(R.id.edtSenhaAdmin);
        btnEntrar = findViewById(R.id.btnEntrarAdmin);

        btnEntrar.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String senha = edtSenha.getText().toString();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(
                        LoginAdmin.this,
                        "Preencha o e-mail e a senha",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Informe um e-mail válido");
                edtEmail.requestFocus();
                return;
            }

            login(email, senha);
        });
    }

    private void aplicarEfeitoVidroLiquido(View view) {
        if (view == null) return;
        
        GradientDrawable liquidGlass = new GradientDrawable();
        liquidGlass.setShape(GradientDrawable.RECTANGLE);
        liquidGlass.setOrientation(GradientDrawable.Orientation.TL_BR);
        liquidGlass.setColor(Color.parseColor("#1F8294EF"));

        float density = view.getContext().getResources().getDisplayMetrics().density;
        liquidGlass.setCornerRadius(24 * density);
        
        int strokeWidthPx = (int) (1 * density);
        liquidGlass.setStroke(strokeWidthPx, Color.parseColor("#99F5F3F7"));

        view.setBackground(liquidGlass);
        view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        view.setClipToOutline(true);
        view.setElevation(5 * density);
    }


    private void login(String email, String senha) {
        btnEntrar.setEnabled(false);
        btnEntrar.setText("Entrando...");
        loginFlow.login(email, senha, "ADMIN", new LoginFlow.Listener() {
            @Override public void onSuccess(MinhaContaResponse account) {
                AccountNavigator.open(LoginAdmin.this, account);
            }
            @Override public void onError(String message) {
                btnEntrar.setEnabled(true);
                btnEntrar.setText("Entrar");
                Toast.makeText(LoginAdmin.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override protected void onDestroy() {
        if (loginFlow != null) loginFlow.cancel();
        super.onDestroy();
    }
    }
