package fr.ecosort.backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "admin")
@PrimaryKeyJoinColumn(name = "id_client")
public class Admin extends Users {

    @Column(name="password", nullable = false, length = 255)
    private String password;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Conseil> conseils;

   public Admin(String nom, String prenom, String email, String password) {
    super();
    setNom(nom);
    setPrenom(prenom);
    setEmail(email);
    this.password = password;
}


    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

