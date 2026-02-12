package fr.ecosort.backend.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email"})
    },
    indexes = {
        @Index(name = "idx_users_email", columnList = "email")
    }
)
@Inheritance(strategy = InheritanceType.JOINED)
public class Users {

    @Id
@GeneratedValue(strategy = GenerationType.UUID)
@Column(name="id_client", nullable = false, unique = true)
private UUID idClient;

    @Column(name="nom", nullable = false, length = 100)
    private String nom;

    @Column(name="prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name="email", nullable = false, unique = true, length = 150)
    private String email;

    public Users() {}

    // Relation avec Dechet
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Dechet> dechets;

    // Relation avec Conseil
    @ManyToMany
    @JoinTable(
        name = "users_conseil",
        joinColumns = @JoinColumn(name = "id_client"),
        inverseJoinColumns = @JoinColumn(name = "id_conseil")
    )
    private List<Conseil> conseils;

    // ===== Getters & Setters =====

    public String getIdClient() {
        return idClient;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Dechet> getDechets() {
        return dechets;
    }

    public void setDechets(List<Dechet> dechets) {
        this.dechets = dechets;
    }

    public List<Conseil> getConseils() {
        return conseils;
    }

    public void setConseils(List<Conseil> conseils) {
        this.conseils = conseils;
    }
}
