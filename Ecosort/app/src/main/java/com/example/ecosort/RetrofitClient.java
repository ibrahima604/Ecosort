package com.example.ecosort;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
 
    // BASE_URL = racine du serveur UNIQUEMENT, avec / final
    // Le chemin /api/users est déjà défini dans ApiService avec @POST("api/users")
    private static final String BASE_URL = "https://2007-193-55-29-169.ngrok-free.app";

    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
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