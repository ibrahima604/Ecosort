package fr.ecosort.backend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_feedback", nullable = false, unique = true)
    private int idFeedback;

    @Column(name = "note", nullable = false)
    private int note;

    @Column(name = "commentaire", length = 1000)
    private String commentaire;

    @Column(name = "categorie", length = 50)
    private String categorie;

    @Column(name = "date_feedback", nullable = false)
    private LocalDateTime dateFeedback;

    // ── Relation avec Users ──────────────────────────────────────────────────
    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    @JsonIgnore
    private Users user;

    // ── Constructeurs ────────────────────────────────────────────────────────
    public Feedback() {}

    public Feedback(int note, String commentaire,
                    String categorie, Users user) {
        this.note         = note;
        this.commentaire  = commentaire;
        this.categorie    = categorie;
        this.dateFeedback = LocalDateTime.now();
        this.user         = user;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public int           getIdFeedback()   { return idFeedback; }

    public int           getNote()         { return note; }
    public void          setNote(int note) { this.note = note; }

    public String        getCommentaire()  { return commentaire; }
    public void          setCommentaire(String commentaire) {
                             this.commentaire = commentaire; }

    public String        getCategorie()    { return categorie; }
    public void          setCategorie(String categorie) {
                             this.categorie = categorie; }

    public LocalDateTime getDateFeedback() { return dateFeedback; }
    public void          setDateFeedback(LocalDateTime dateFeedback) {
                             this.dateFeedback = dateFeedback; }

    public Users         getUser()         { return user; }
    public void          setUser(Users user) { this.user = user; }
}