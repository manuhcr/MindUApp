package com.example.mindu.api;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://mindu-api.onrender.com"; // emulador -> localhost da máquina
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
    public static ApiService getApiService(Context context){
       return getInstance(context).create(ApiService.class);
    }
}
