package fr.ecosort.backend.models;
import jakarta.persistence.*;
@Entity
@Table(name = "type_dechet")
public class TypeDechet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_type_dechet", nullable = false, unique = true)
    private int idTypeDechet;
    @Column(name="label", nullable = false, length = 100)
    private String label;
    public TypeDechet(String label) {
        this.label = label;
    }
    public TypeDechet() {
        // Constructeur par défaut
    }

    // Relation avec Dechet
    @OneToMany(mappedBy = "typeDechet", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Dechet> dechets;
    // Getters & Setters
    public int getIdTypeDechet() { return idTypeDechet; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

}
