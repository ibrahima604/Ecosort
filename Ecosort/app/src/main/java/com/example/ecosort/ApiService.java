package com.example.ecosort;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    //Utilisateurs
    @POST("api/users")
    Call<Void> createUser(@Body UserRequest user);

    @GET("api/users/email/{email}")
    Call<UserResponse> getUserByEmail(@Path("email") String email);

    @GET("api/users")
    Call<List<UserResponse>> getAllUsers();

    @PUT("api/users/{id}")
    Call<UserResponse> updateUser(@Path("id") String id, @Body UserRequest body);

    @DELETE("api/users/{id}")
    Call<Void> deleteUser(@Path("id") String id);

    //Admin
    @GET("api/admin/email/{email}")
    Call<AdminResponse> getAdminByEmail(@Path("email") String email);

    @GET("api/admin/stats")
    Call<Map<String, Long>> getAdminStats();

    //Conseils
    @GET("api/conseils")
    Call<List<ConseilResponse>> getAllConseils();

    @POST("api/conseils")
    Call<ConseilResponse> createConseil(@Body ConseilRequest request);

    @PUT("api/conseils/{id}")
    Call<ConseilResponse> updateConseil(@Path("id") int id, @Body ConseilRequest request);

    @DELETE("api/conseils/{id}")
    Call<Void> deleteConseil(@Path("id") int id);

    //Déchets

    /** Crée un déchet dans Supabase après un scan */
    @POST("api/dechets")
    Call<DechetResponse> createDechet(@Body DechetRequest request);

    /** Historique des déchets d'un utilisateur */
    @GET("api/dechets/user/{idClient}")
    Call<List<DechetResponse>> getDechetsByUser(@Path("idClient") String idClient);

    /** Liste des types de déchets (synchronisation au démarrage) */
    @GET("api/dechets/types")
    Call<List<TypeDechetResponse>> getAllTypesDechets();
}