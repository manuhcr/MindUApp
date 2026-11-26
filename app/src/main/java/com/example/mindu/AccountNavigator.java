package com.example.mindu;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.mindu.api.login.MinhaContaResponse;

final class AccountNavigator {
    private AccountNavigator() { }

    static void open(Activity activity, MinhaContaResponse account) {
        String type = account.getTipo() == null ? "" : account.getTipo().trim().toUpperCase(java.util.Locale.ROOT);
        String status = account.getStatus() == null ? "" : account.getStatus().trim().toUpperCase(java.util.Locale.ROOT);
        SessionManager.save(activity, SessionManager.getToken(activity), account.getTipo(),
                account.getStatus(), account.getId(), account.getNome());
        if ("REJEITADO".equals(status)) {
            SessionManager.clear(activity);
            String message = "CLIENTE".equals(type)
                    ? "O acesso desta conta foi recusado pela empresa."
                    : "Seu cadastro profissional foi recusado. Entre em contato com o suporte do MindU.";
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent;
        boolean pending = ("CLIENTE".equals(type) && "AGUARDANDO_APROVACAO".equals(status))
                || ("PROFISSIONAL".equals(type) && "PENDENTE".equals(status));
        if (pending) {
            intent = new Intent(activity, ContaPendenteActivity.class);
        } else if ("CLIENTE".equals(type) || "EMPRESA".equals(type)) {
            intent = new Intent(activity, DashboardBeneficiario.class);
            if ("EMPRESA".equals(type)) intent = new Intent(activity, DashboardEmpresa.class);
        } else if ("PROFISSIONAL".equals(type)) {
            intent = new Intent(activity, DashboardProfissional.class);
        } else if ("ADMIN".equals(type)) {
            intent = new Intent(activity, DashboardAdmin.class);
        } else {
            SessionManager.clear(activity);
            Toast.makeText(activity, "Não foi possível identificar o perfil desta conta.", Toast.LENGTH_LONG).show();
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
