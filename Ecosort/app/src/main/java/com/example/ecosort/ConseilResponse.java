package com.example.ecosort;

import com.google.gson.annotations.SerializedName;

public class ConseilResponse {

    @SerializedName("id_conseil")
    private int idConseil;

    private String titre;
    private String description;

    public int    getIdConseil()   { return idConseil; }
    public String getTitre()       { return titre; }
    public String getDescription() { return description; }
}