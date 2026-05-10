package fr.ecosort.backend.services;

import fr.ecosort.backend.dto.FeedbackRequest;
import fr.ecosort.backend.dto.FeedbackResponse;
import fr.ecosort.backend.models.Feedback;
import fr.ecosort.backend.models.Users;
import fr.ecosort.backend.repositories.FeedbackRepository;
import fr.ecosort.backend.repositories.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UsersRepository    usersRepository;

    // ── Injection par constructeur ───────────────────────────────────────────
    public FeedbackService(FeedbackRepository feedbackRepository,
                           UsersRepository usersRepository) {
        this.feedbackRepository = feedbackRepository;
        this.usersRepository    = usersRepository;
    }

    // ── Créer un feedback ────────────────────────────────────────────────────
    public FeedbackResponse createFeedback(FeedbackRequest request) {

        // 1. Récupérer l'utilisateur
        UUID idClient = UUID.fromString(request.getIdClient());
        Users user = usersRepository.findById(idClient)
            .orElseThrow(() -> new RuntimeException(
                "Utilisateur introuvable : " + request.getIdClient()));

        // 2. Validation note
        if (request.getNote() < 1 || request.getNote() > 5) {
            throw new IllegalArgumentException("La note doit être entre 1 et 5");
        }

        // 3. Créer et sauvegarder
        Feedback feedback = new Feedback(
            request.getNote(),
            request.getCommentaire(),
            request.getCategorie(),
            user
        );

        Feedback saved = feedbackRepository.save(feedback);
        return toResponse(saved);
    }

    // ── Feedbacks d'un utilisateur ───────────────────────────────────────────
    public List<FeedbackResponse> getFeedbackByUser(String idClientStr) {
        UUID idClient = UUID.fromString(idClientStr);
        return feedbackRepository.findByUser_IdClient(idClient)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ── Tous les feedbacks (admin) ───────────────────────────────────────────
    public List<FeedbackResponse> getAllFeedbacks() {
        return feedbackRepository.findAll()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ── Feedbacks par catégorie (admin) ──────────────────────────────────────
    public List<FeedbackResponse> getFeedbackByCategorie(String categorie) {
        return feedbackRepository.findByCategorie(categorie)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ── Moyenne globale (admin) ───────────────────────────────────────────────
    public Double getAverageNote() {
        Double avg = feedbackRepository.findAverageNote();
        return avg != null ? avg : 0.0;
    }

    // ── Supprimer un feedback ─────────────────────────────────────────────────
    public void deleteFeedback(int idFeedback) {
        if (!feedbackRepository.existsById(idFeedback)) {
            throw new RuntimeException("Feedback introuvable : " + idFeedback);
        }
        feedbackRepository.deleteById(idFeedback);
    }

    // ── Mapper Feedback → FeedbackResponse ───────────────────────────────────
    private FeedbackResponse toResponse(Feedback feedback) {
        return new FeedbackResponse(
            feedback.getIdFeedback(),
            feedback.getUser().getIdClient().toString(),
            feedback.getNote(),
            feedback.getCommentaire(),
            feedback.getCategorie(),
            feedback.getDateFeedback().toString()
        );
    }
}