package com.example.mindu.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit;
    private static final ThreadLocal<Boolean> ROTAS_SEM_LOG = new ThreadLocal<>();

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            Context appContext = context.getApplicationContext();
            // Margem para inicialização lenta da API local e consultas mais demoradas.
            boolean debug = (appContext.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
                if (!Boolean.TRUE.equals(ROTAS_SEM_LOG.get())) android.util.Log.d("MindU-API", message);
            });
            logging.redactHeader("Authorization");
            logging.setLevel(debug ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        String token = com.example.mindu.SessionManager.getToken(appContext);
                        Request.Builder b = chain.request().newBuilder();
                        if (token != null) b.addHeader("Authorization", "Bearer " + token);
                        return chain.proceed(b.build());
                    })
                    .addInterceptor(chain -> {
                        Response response = chain.proceed(chain.request());
                        if (response.code() == 401
                                && com.example.mindu.SessionManager.getToken(appContext) != null
                                && !com.example.mindu.SessionManager.isValidatingLogin(appContext)) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    com.example.mindu.SessionManager.expireSession(appContext));
                        } else if (response.code() == 429
                                && !chain.request().url().encodedPath().endsWith("/auth/login")
                                && !chain.request().url().encodedPath().startsWith("/v1/chat")) {
                            String retry = response.header("Retry-After");
                            String message = "Muitas solicitações. Aguarde um pouco e tente novamente.";
                            if (retry != null && retry.matches("[0-9]+")) message = "Muitas solicitações. Tente novamente em " + retry + " segundos.";
                            final String notice = message;
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(appContext, notice, Toast.LENGTH_LONG).show());
                        }
                        return response;
                    })
                    .addInterceptor(chain -> {
                        String path = chain.request().url().encodedPath();
                        boolean semLog = path.endsWith("/v1/clientes/me/dados-sensiveis")
                                || path.equals("/v1/chat") || path.startsWith("/v1/chat/");
                        if (semLog) ROTAS_SEM_LOG.set(true);
                        try {
                            return chain.proceed(chain.request());
                        } finally {
                            if (semLog) ROTAS_SEM_LOG.remove();
                        }
                    })
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
    public static ApiService getApiService(Context context){
       return getInstance(context).create(ApiService.class);
    }
}
