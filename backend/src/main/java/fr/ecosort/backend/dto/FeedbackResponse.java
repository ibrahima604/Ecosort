package fr.ecosort.backend.dto;

public class FeedbackResponse {

    private int    idFeedback;
    private String idClient;
    private int    note;
    private String commentaire;
    private String categorie;
    private String dateFeedback;

    public FeedbackResponse(int idFeedback, String idClient, int note,
                            String commentaire, String categorie,
                            String dateFeedback) {
        this.idFeedback   = idFeedback;
        this.idClient     = idClient;
        this.note         = note;
        this.commentaire  = commentaire;
        this.categorie    = categorie;
        this.dateFeedback = dateFeedback;
    }

    public int    getIdFeedback()   { return idFeedback; }
    public String getIdClient()     { return idClient; }
    public int    getNote()         { return note; }
    public String getCommentaire()  { return commentaire; }
    public String getCategorie()    { return categorie; }
    public String getDateFeedback() { return dateFeedback; }
}