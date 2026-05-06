package com.example.ecosort;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String TAG = "RetrofitClient";
    private static Retrofit retrofit;
    private static String BASE_URL;

    public static void init(Context context) {
        String newUrl = context.getString(R.string.base_url);
        if (!newUrl.equals(BASE_URL)) {
            BASE_URL = newUrl;
            retrofit = null; // force reconstruction si l'URL a changé
        }
    }

    public static Retrofit getInstance() {
        if (retrofit == null) {
            if (BASE_URL == null || BASE_URL.isEmpty()) {
                throw new IllegalStateException(
                        "RetrofitClient non initialisé. Appelez RetrofitClient.init(context) d'abord.");
            }

            // Intercepteur de logs simple — visible dans Logcat tag "RetrofitClient"
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(15,    TimeUnit.SECONDS)
                    .writeTimeout(15,   TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request req = chain.request();
                        Log.d(TAG, "→ " + req.method() + " " + req.url());
                        Response resp = chain.proceed(req);
                        Log.d(TAG, "← " + resp.code() + " " + req.url());
                        return resp;
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getInstance().create(ApiService.class);
    }
}