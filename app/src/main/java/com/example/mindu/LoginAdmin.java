package com.example.mindu;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.login.LoginRequest;
import com.example.mindu.api.login.LoginResponse;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;

public class LoginAdmin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_admin);

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
        Button btnEntrar = findViewById(R.id.btnEntrarAdmin);

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
        RetrofitClient.getApiService(this)
                .login(new LoginRequest(email, senha))
                .enqueue(new Callback<LoginResponse>() {

                    @Override
                    public void onResponse(
                            Call<LoginResponse> call,
                            Response<LoginResponse> response) {

                        if (response.isSuccessful()) {
                            LoginResponse loginResponse = response.body();

                            if (loginResponse != null
                                    && loginResponse.getToken() != null) {

                                String token = loginResponse.getToken();

                                SharedPreferences prefs =
                                        getSharedPreferences("mindu", MODE_PRIVATE);

                                prefs.edit()
                                        .putString("token", token)
                                        .apply();

                                // TODO: redirecionar para a tela correta
                                Toast.makeText(
                                        LoginAdmin.this,
                                        "Login bem-sucedido",
                                        Toast.LENGTH_SHORT
                                ).show();
                                Intent intent = new Intent(
                                        LoginAdmin.this,
                                        DashboardAdmin.class
                                );
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(
                                        LoginAdmin.this,
                                        "Erro ao fazer login",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        } else {
                            Toast.makeText(
                                    LoginAdmin.this,
                                    "Credenciais inválidas",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<LoginResponse> call,
                            Throwable t) {

                        android.util.Log.e("MINDU_LOGIN", "Falha na requisição de login", t);

                        Toast.makeText(
                                LoginAdmin.this,
                                "Erro: " + t.getClass().getSimpleName()
                                        + " - " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

    }
    }
