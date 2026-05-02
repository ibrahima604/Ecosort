package com.example.ecosort;

import com.google.gson.annotations.SerializedName;

public class AdminResponse {

    @SerializedName("id_client")
    private String idClient; // UUID → String !

    private String nom;
    private String prenom;
    private String email;

    public String getIdClient() { return idClient; }
    public String getNom()      { return nom; }
    public String getPrenom()   { return prenom; }
    public String getEmail()    { return email; }
}