package fr.ecosort.backend.repositories;

import fr.ecosort.backend.models.Feedback;
import fr.ecosort.backend.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    // ── Tous les feedbacks d'un utilisateur ──────────────────────────────────
    List<Feedback> findByUser(Users user);

    // ── Feedbacks par id_client directement ─────────────────────────────────
    List<Feedback> findByUser_IdClient(UUID idClient);

    // ── Feedbacks par catégorie ──────────────────────────────────────────────
    List<Feedback> findByCategorie(String categorie);

    // ── Moyenne des notes globale ────────────────────────────────────────────
    @Query("SELECT AVG(f.note) FROM Feedback f")
    Double findAverageNote();

    // ── Moyenne des notes d'un utilisateur ──────────────────────────────────
    @Query("SELECT AVG(f.note) FROM Feedback f WHERE f.user.idClient = :idClient")
    Double findAverageNoteByUser(@Param("idClient") UUID idClient);

    // ── Nombre de feedbacks par catégorie ────────────────────────────────────
    @Query("SELECT f.categorie, COUNT(f) FROM Feedback f GROUP BY f.categorie")
    List<Object[]> countByCategorie();
}