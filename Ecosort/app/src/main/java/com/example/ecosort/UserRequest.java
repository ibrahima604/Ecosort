package com.example.ecosort;

// Modèle envoyé au backend Spring Boot via POST /api/users
public class UserRequest {
    public String nom;
    public String prenom;
    public String email;

    public UserRequest(String nom, String prenom, String email) {
        this.nom    = nom;
        this.prenom = prenom;
        this.email  = email;
    }
}