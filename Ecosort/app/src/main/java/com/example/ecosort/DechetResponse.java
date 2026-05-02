package com.example.ecosort;

import com.google.gson.annotations.SerializedName;

public class DechetResponse {

    @SerializedName("id_dechet")
    private int idDechet;

    @SerializedName("id_type_dechet")
    private int idTypeDechet;

    @SerializedName("date_tri")
    private String dateTri;

    public int    getIdDechet()      { return idDechet; }
    public int    getIdTypeDechet()  { return idTypeDechet; }
    public String getDateTri()       { return dateTri; }
}