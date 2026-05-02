package fr.ecosort.backend.models;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "dechet")
public class Dechet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_dechet", nullable = false, unique = true)
    private int idDechet;
    @Column(name="date_tri", nullable = false)
    private LocalDateTime dateTri;

    public Dechet(LocalDateTime dateTri) {
        this.dateTri = dateTri;
    }
    public Dechet() {
        // Constructeur par défaut
    }

    // Relation avec Users
    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    @JsonIgnore
    private Users user;
    // Relation avec TypeDechet
    @ManyToOne
    @JoinColumn(name = "id_type_dechet", nullable = false)
    private TypeDechet typeDechet;
    // Getters & Setters
    public int getIdDechet() { return idDechet; }
    public LocalDateTime getDateTri() { return dateTri; }
    public void setDateTri(LocalDateTime dateTri) { this.dateTri = dateTri; }
    public Users getUser() { return user; }
public void setUser(Users user) { this.user = user; }

public TypeDechet getTypeDechet() { return typeDechet; }
public void setTypeDechet(TypeDechet typeDechet) { this.typeDechet = typeDechet; }

}
