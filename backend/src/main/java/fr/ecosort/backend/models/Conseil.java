package fr.ecosort.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "conseil")
public class Conseil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_conseil", nullable = false, unique = true)
    private int idConseil;

    @Column(name="titre", nullable = false, length = 255)
    private String titre;

    @Column(name="description", nullable = false, length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    @JsonIgnore
    private Admin admin;

    @ManyToMany(mappedBy = "conseils")
    @JsonIgnore
    private java.util.List<Users> users;

    public Conseil(String titre, String description) {
        this.titre = titre;
        this.description = description;
    }

    public Conseil() {}

    public int    getIdConseil()   { return idConseil; }
    public String getTitre()       { return titre; }
    public void   setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void   setDescription(String description) { this.description = description; }
    public Admin  getAdmin()       { return admin; }
    public void   setAdmin(Admin admin) { this.admin = admin; }
}