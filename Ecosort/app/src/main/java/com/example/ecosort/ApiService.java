package com.example.ecosort;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

// IMPORTANT : c'est une INTERFACE, pas une classe
public interface ApiService {

    @POST("api/users")
    Call<Void> createUser(@Body UserRequest user);
}