package com.example.ecosort;

import com.google.gson.annotations.SerializedName;

public class DechetRequest {

    @SerializedName("id_type_dechet")
    private int idTypeDechet;

    @SerializedName("id_client")
    private String idClient;

    @SerializedName("date_tri")
    private String dateTri;

    public DechetRequest(int idTypeDechet, String idClient, String dateTri) {
        this.idTypeDechet = idTypeDechet;
        this.idClient     = idClient;
        this.dateTri      = dateTri;
    }

    public int    getIdTypeDechet() { return idTypeDechet; }
    public String getIdClient()     { return idClient; }
    public String getDateTri()      { return dateTri; }
}