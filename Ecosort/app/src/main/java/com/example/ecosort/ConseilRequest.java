package com.example.ecosort;

public class ConseilRequest {

    private String titre;
    private String description;

    public ConseilRequest(String titre, String description) {
        this.titre       = titre;
        this.description = description;
    }

    public String getTitre()       { return titre; }
    public String getDescription() { return description; }
}