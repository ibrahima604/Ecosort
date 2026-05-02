package com.example.ecosort;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.PUT;

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



    @GET("api/admin/email/{email}")
    Call<AdminResponse> getAdminByEmail(@Path("email") String email);

    @PUT("api/conseils/{id}")
    Call<ConseilResponse> updateConseil(@Path("id") int id, @Body ConseilRequest request);

    @GET("api/users")
    Call<List<UserResponse>> getAllUsers();

    @PUT("api/users/{id}")
    Call<UserResponse> updateUser(@Path("id") String id, @Body UserRequest body);

    @DELETE("api/users/{id}")
    Call<Void> deleteUser(@Path("id") String id);

    @GET("api/admin/stats")
    Call<Map<String, Long>> getAdminStats();
    // Dans ApiService, ajoute ces deux lignes avec les autres @GET :

    @GET("api/dechets/user/{idClient}")
    Call<List<DechetResponse>> getDechetsByUser(@Path("idClient") String idClient);

    @GET("api/dechets/types")
    Call<List<TypeDechetResponse>> getAllTypesDechets();

}