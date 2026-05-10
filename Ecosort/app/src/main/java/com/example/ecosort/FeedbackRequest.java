package com.example.ecosort;

public class FeedbackRequest {

    private String idClient;
    private int    note;
    private String commentaire;
    private String categorie;

    public FeedbackRequest(String idClient, int note,
                           String commentaire, String categorie) {
        this.idClient    = idClient;
        this.note        = note;
        this.commentaire = commentaire;
        this.categorie   = categorie;
    }

    public String getIdClient()    { return idClient; }
    public int    getNote()        { return note; }
    public String getCommentaire() { return commentaire; }
    public String getCategorie()   { return categorie; }
}