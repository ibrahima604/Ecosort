package fr.ecosort.backend.models;
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
    public Conseil(String titre, String description) {
        this.titre = titre;
        this.description = description;
    }
    public Conseil() {
        // Constructeur par défaut
    }

    // Relation avec Admin
    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    private Admin admin;

    // Relation avec Users
    @ManyToMany(mappedBy = "conseils")
    private java.util.List<Users> users;
    // Getters & Setters
    public int getIdConseil() { return idConseil; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

}
