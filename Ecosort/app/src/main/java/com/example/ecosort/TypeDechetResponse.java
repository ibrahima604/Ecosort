package com.example.ecosort;

import com.google.gson.annotations.SerializedName;

public class TypeDechetResponse {

    @SerializedName("id_type_dechet")
    private int idTypeDechet;

    @SerializedName("etiquette")
    private String etiquette;

    public int    getIdTypeDechet() { return idTypeDechet; }
    public String getEtiquette()    { return etiquette; }
}