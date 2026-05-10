package fr.ecosort.backend.dto;

public class FeedbackRequest {

    private String idClient;
    private int    note;
    private String commentaire;
    private String categorie;

    public FeedbackRequest() {}

    public String getIdClient()    { return idClient; }
    public void   setIdClient(String idClient) { this.idClient = idClient; }

    public int    getNote()        { return note; }
    public void   setNote(int note) { this.note = note; }

    public String getCommentaire() { return commentaire; }
    public void   setCommentaire(String commentaire) {
                      this.commentaire = commentaire; }

    public String getCategorie()   { return categorie; }
    public void   setCategorie(String categorie) {
                      this.categorie = categorie; }
}