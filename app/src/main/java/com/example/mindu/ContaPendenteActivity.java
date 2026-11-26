package com.example.mindu;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.login.MinhaContaResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContaPendenteActivity extends AppCompatActivity {
    private TextView statusText;
    private Button retryButton;
    private Call<MinhaContaResponse> refreshCall;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String type = SessionManager.getType(this);
        if (SessionManager.getToken(this) == null || type == null
                || !("CLIENTE".equalsIgnoreCase(type) || "PROFISSIONAL".equalsIgnoreCase(type))) {
            SessionManager.returnToAccess(this, "Entre novamente para consultar sua conta.");
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(28), dp(28), dp(28));

        TextView title = new TextView(this);
        title.setTextSize(23);
        title.setGravity(Gravity.CENTER);
        title.setText("CLIENTE".equalsIgnoreCase(type) ? "Aguardando aprovação da empresa" : "Cadastro em análise");
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        statusText = new TextView(this);
        statusText.setGravity(Gravity.CENTER);
        statusText.setTextSize(16);
        statusText.setText("CLIENTE".equalsIgnoreCase(type)
                ? "A empresa precisa aprovar seu cadastro antes de você usar as consultas."
                : "Sua carteirinha está em verificação. Avisaremos quando a análise terminar.");
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.topMargin = dp(18);
        root.addView(statusText, statusParams);

        retryButton = new Button(this);
        retryButton.setText("Verificar novamente");
        retryButton.setOnClickListener(v -> verificarNovamente());
        LinearLayout.LayoutParams retryParams = new LinearLayout.LayoutParams(-1, -2);
        retryParams.topMargin = dp(22);
        root.addView(retryButton, retryParams);

        Button logout = new Button(this);
        logout.setText("Sair da conta");
        logout.setOnClickListener(v -> SessionManager.logout(this));
        root.addView(logout, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
        MilaFab.attach(this);
    }

    private void verificarNovamente() {
        if (refreshCall != null) refreshCall.cancel();
        retryButton.setEnabled(false);
        retryButton.setText("Verificando...");
        refreshCall = RetrofitClient.getApiService(this).minhaConta();
        refreshCall.enqueue(new Callback<>() {
            @Override public void onResponse(Call<MinhaContaResponse> call, Response<MinhaContaResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                retryButton.setEnabled(true);
                retryButton.setText("Verificar novamente");
                MinhaContaResponse account = response.body();
                if (!response.isSuccessful() || account == null) {
                    Toast.makeText(ContaPendenteActivity.this,
                            response.code() == 403 ? "Sua conta ainda não está aprovada." : "Não foi possível atualizar o status. Tente novamente.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                String type = SessionManager.getType(ContaPendenteActivity.this);
                SessionManager.save(ContaPendenteActivity.this, SessionManager.getToken(ContaPendenteActivity.this),
                        account.getTipo(), account.getStatus(), account.getId(), account.getNome());
                String status = account.getStatus() == null ? "" : account.getStatus().trim();
                if ("APROVADO".equalsIgnoreCase(status)) {
                    AccountNavigator.open(ContaPendenteActivity.this, account);
                } else if ("REJEITADO".equalsIgnoreCase(status)) {
                    statusText.setText("CLIENTE".equalsIgnoreCase(type)
                            ? "O acesso desta conta foi recusado pela empresa. Saia da conta ou procure a empresa."
                            : "Seu cadastro foi recusado. Entre em contato com o suporte do MindU.");
                    retryButton.setEnabled(false);
                    retryButton.setText("Análise concluída");
                } else {
                    Toast.makeText(ContaPendenteActivity.this, "Seu cadastro continua em análise.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<MinhaContaResponse> call, Throwable error) {
                if (isFinishing() || isDestroyed()) return;
                retryButton.setEnabled(true);
                retryButton.setText("Verificar novamente");
                if (!call.isCanceled()) Toast.makeText(ContaPendenteActivity.this,
                        "Não foi possível consultar a API. Tente novamente.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override protected void onDestroy() {
        if (refreshCall != null) refreshCall.cancel();
        super.onDestroy();
    }

    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
}
