package com.example.ecosort;

import com.google.gson.annotations.SerializedName;

public class ConseilRequest {

    private String titre;
    private String description;

    @SerializedName("idAdmin")
    private String idAdmin; // UUID String !

    public ConseilRequest(String titre, String description, String idAdmin) {
        this.titre       = titre;
        this.description = description;
        this.idAdmin     = idAdmin;
    }

    public String getTitre()       { return titre; }
    public String getDescription() { return description; }
    public String getIdAdmin()     { return idAdmin; }
}