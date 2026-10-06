package com.example.mindu.api;

import android.content.Context;
import android.content.SharedPreferences;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/"; // emulador -> localhost da máquina
    private static Retrofit retrofit;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        SharedPreferences prefs = context.getSharedPreferences("mindu", Context.MODE_PRIVATE);
                        String token = prefs.getString("token", null);
                        Request.Builder b = chain.request().newBuilder();
                        if (token != null) b.addHeader("Authorization", "Bearer " + token);
                        return chain.proceed(b.build());
                    })
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
