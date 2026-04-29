package com.example.ecosort;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("api/users")
    Call<Void> createUser(@Body UserRequest user);

    @GET("api/users/email/{email}")
    Call<UserResponse> getUserByEmail(@Path("email") String email);

    @GET("api/conseils")
    Call<List<ConseilResponse>> getAllConseils();

    @POST("api/conseils")
    Call<ConseilResponse> createConseil(@Body ConseilRequest request);

    @DELETE("api/conseils/{id}")
    Call<Void> deleteConseil(@Path("id") int id);
}