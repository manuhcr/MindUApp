package com.example.mindu;

import android.app.Activity;

import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.login.LoginRequest;
import com.example.mindu.api.login.LoginResponse;
import com.example.mindu.api.login.MinhaContaResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

final class LoginFlow {
    interface Listener {
        void onSuccess(MinhaContaResponse account);
        void onError(String message);
    }

    private final Activity activity;
    private Call<LoginResponse> loginCall;
    private Call<MinhaContaResponse> accountCall;

    LoginFlow(Activity activity) { this.activity = activity; }

    void login(String email, String password, String expectedType, Listener listener) {
        SessionManager.clear(activity);
        loginCall = RetrofitClient.getApiService(activity).login(new LoginRequest(email, password));
        loginCall.enqueue(new Callback<LoginResponse>() {
            @Override public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (!isAlive()) return;
                if (!response.isSuccessful() || response.body() == null || response.body().getToken() == null) {
                    listener.onError(errorMessage(response.code(), response.headers()));
                    return;
                }
                String token = response.body().getToken();
                SessionManager.beginValidation(activity, token);
                accountCall = RetrofitClient.getApiService(activity).minhaConta();
                accountCall.enqueue(new Callback<MinhaContaResponse>() {
                    @Override public void onResponse(Call<MinhaContaResponse> meCall, Response<MinhaContaResponse> meResponse) {
                        if (!isAlive()) return;
                        MinhaContaResponse account = meResponse.body();
                        if (!meResponse.isSuccessful() || account == null) {
                            SessionManager.clear(activity);
                            listener.onError(errorMessage(meResponse.code(), meResponse.headers()));
                            return;
                        }
                        if (!expectedType.equalsIgnoreCase(value(account.getTipo()))) {
                            SessionManager.clear(activity);
                            listener.onError("Esta conta não possui acesso para este perfil.");
                            return;
                        }
                        String status = account.getStatus();
                        if (("PROFISSIONAL".equals(expectedType) && "REJEITADO".equalsIgnoreCase(value(status)))
                                || ("CLIENTE".equals(expectedType) && "REJEITADO".equalsIgnoreCase(value(status)))) {
                            SessionManager.clear(activity);
                            listener.onError("REJEITADO".equalsIgnoreCase(value(status))
                                    ? ("CLIENTE".equals(expectedType)
                                        ? "O acesso desta conta foi recusado pela empresa."
                                        : "Seu cadastro profissional foi recusado. Entre em contato com o suporte do MindU.")
                                    : "Sua conta ainda não está aprovada.");
                            return;
                        }
                        SessionManager.save(activity, token, account.getTipo(), status,
                                account.getId(), account.getNome());
                        listener.onSuccess(account);
                    }

                    @Override public void onFailure(Call<MinhaContaResponse> meCall, Throwable t) {
                        if (!isAlive()) return;
                        SessionManager.clear(activity);
                        listener.onError(networkMessage());
                    }
                });
            }

            @Override public void onFailure(Call<LoginResponse> call, Throwable t) {
                if (isAlive()) listener.onError(networkMessage());
            }
        });
    }

    void cancel() {
        if (loginCall != null) loginCall.cancel();
        if (accountCall != null) accountCall.cancel();
    }

    private boolean isAlive() { return !activity.isFinishing() && !activity.isDestroyed(); }
    private static String value(String text) { return text == null ? "" : text.trim(); }
    private static String networkMessage() {
        return "Não foi possível conectar à API local (10.0.2.2:8080). Verifique se a API e o banco estão iniciados.";
    }

    static String errorMessage(int code, Headers headers) {
        if (code == 401) return "E-mail ou senha inválidos.";
        if (code == 403) return "Conta desativada ou sem autorização. Procure o suporte do MindU.";
        if (code == 429) {
            String retry = headers.get("Retry-After");
            if (retry != null) {
                try { return "Muitas tentativas. Tente novamente em " + Math.max(1, Long.parseLong(retry)) + " segundos."; }
                catch (NumberFormatException ignored) {
                    try {
                        Date retryAt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).parse(retry);
                        if (retryAt == null) return "Muitas tentativas. Aguarde um pouco antes de tentar novamente.";
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(retryAt.getTime() - System.currentTimeMillis());
                        return "Muitas tentativas. Tente novamente em " + Math.max(1, seconds) + " segundos.";
                    } catch (java.text.ParseException ignoredAgain) { }
                }
            }
            return "Muitas tentativas. Aguarde um pouco antes de tentar novamente.";
        }
        if (code >= 500) return "A API local está indisponível no momento. Tente novamente.";
        return "Não foi possível entrar. Verifique seus dados e tente novamente.";
    }
}
