package fr.ecosort.backend.controllers;

import fr.ecosort.backend.dto.FeedbackRequest;
import fr.ecosort.backend.dto.FeedbackResponse;
import fr.ecosort.backend.services.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    private final FeedbackService feedbackService;

    // ── Injection par constructeur ───────────────────────────────────────────
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // ── POST /api/feedback ───────────────────────────────────────────────────
    // Créer un feedback (appelé depuis Android)
    @PostMapping
    public ResponseEntity<?> createFeedback(@RequestBody FeedbackRequest request) {
        try {
            FeedbackResponse response = feedbackService.createFeedback(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ── GET /api/feedback/user/{idClient} ────────────────────────────────────
    // Historique des feedbacks d'un utilisateur
    @GetMapping("/user/{idClient}")
    public ResponseEntity<?> getFeedbackByUser(@PathVariable String idClient) {
        try {
            List<FeedbackResponse> feedbacks =
                feedbackService.getFeedbackByUser(idClient);
            return ResponseEntity.ok(feedbacks);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ── GET /api/feedback ────────────────────────────────────────────────────
    // Tous les feedbacks (admin uniquement)
    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }

    // ── GET /api/feedback/categorie/{categorie} ──────────────────────────────
    // Feedbacks filtrés par catégorie (admin)
    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackByCategorie(
            @PathVariable String categorie) {
        return ResponseEntity.ok(
            feedbackService.getFeedbackByCategorie(categorie));
    }

    // ── GET /api/feedback/stats/moyenne ──────────────────────────────────────
    // Note moyenne globale (admin)
    @GetMapping("/stats/moyenne")
    public ResponseEntity<?> getAverageNote() {
        try {
            Double moyenne = feedbackService.getAverageNote();
            return ResponseEntity.ok(moyenne);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

    // ── DELETE /api/feedback/{id} ─────────────────────────────────────────────
    // Supprimer un feedback (admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable int id) {
        try {
            feedbackService.deleteFeedback(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}