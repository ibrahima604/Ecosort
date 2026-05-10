package com.example.ecosort;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

public class FeedbackResponse {

    @SerializedName("id_feedback")
    private int idFeedback;

    @SerializedName("id_client")
    private String idClient;

    private int    note;
    private String commentaire;
    private String categorie;

    @SerializedName("date_feedback")
    private String dateFeedback;

    public int    getIdFeedback()   { return idFeedback; }
    public String getIdClient()     { return idClient; }
    public int    getNote()         { return note; }
    public String getCommentaire()  { return commentaire; }
    public String getCategorie()    { return categorie; }
    public String getDateFeedback() { return dateFeedback; }
}