package com.example.ecosort;

import android.content.Context;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;
    private static String BASE_URL;

    // CORRECTION : réinitialiser retrofit si l'URL change
    public static void init(Context context) {
        String newUrl = context.getString(R.string.base_url);
        if (!newUrl.equals(BASE_URL)) {
            BASE_URL = newUrl;
            retrofit = null; // force la reconstruction
        }
    }

    public static Retrofit getInstance() {
        if (retrofit == null) {
            if (BASE_URL == null || BASE_URL.isEmpty()) {
                throw new IllegalStateException(
                        "RetrofitClient non initialisé. Appelez RetrofitClient.init(context) d'abord.");
            }
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getInstance().create(ApiService.class);
    }
}