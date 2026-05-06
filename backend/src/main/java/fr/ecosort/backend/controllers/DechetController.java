package fr.ecosort.backend.controllers;

import fr.ecosort.backend.models.Dechet;
import fr.ecosort.backend.models.TypeDechet;
import fr.ecosort.backend.models.Users;
import fr.ecosort.backend.repositories.DechetRepository;
import fr.ecosort.backend.repositories.TypeDechetRepository;
import fr.ecosort.backend.repositories.UsersRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dechets")
public class DechetController {

    private final DechetRepository     dechetRepository;
    private final TypeDechetRepository typeDechetRepository;
    private final UsersRepository      usersRepository;

    public DechetController(DechetRepository     dechetRepository,
                            TypeDechetRepository typeDechetRepository,
                            UsersRepository      usersRepository) {
        this.dechetRepository     = dechetRepository;
        this.typeDechetRepository = typeDechetRepository;
        this.usersRepository      = usersRepository;
    }

    // ─── POST /api/dechets ────────────────────────────────────────────────────
    // Reçoit { "id_type_dechet": 1, "id_client": "uuid", "date_tri": "..." }
    @PostMapping
    public ResponseEntity<?> createDechet(@RequestBody Map<String, Object> body) {
        try {
            String idClientStr  = (String) body.get("id_client");
            int    idTypeDechet = ((Number) body.get("id_type_dechet")).intValue();

            UUID uuid = UUID.fromString(idClientStr);

            Users user = usersRepository.findById(uuid).orElse(null);
            if (user == null)
                return ResponseEntity.badRequest().body("Utilisateur introuvable : " + idClientStr);

            TypeDechet type = typeDechetRepository.findById(idTypeDechet).orElse(null);
            if (type == null)
                return ResponseEntity.badRequest().body("Type déchet introuvable : " + idTypeDechet);

            Dechet d = new Dechet();
            d.setUser(user);
            d.setTypeDechet(type);
            d.setDateTri(LocalDateTime.now());

            Dechet saved = dechetRepository.save(d);
            return ResponseEntity.ok(Map.of(
                    "id_dechet",      saved.getIdDechet(),
                    "id_type_dechet", saved.getTypeDechet().getIdTypeDechet(),
                    "date_tri",       saved.getDateTri().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur création déchet : " + e.getMessage());
        }
    }

    // ─── GET /api/dechets/user/{idClient} ─────────────────────────────────────
    @GetMapping("/user/{idClient}")
    public ResponseEntity<List<Map<String, Object>>> getByUser(
            @PathVariable String idClient) {
        UUID uuid = UUID.fromString(idClient);
        List<Map<String, Object>> result = dechetRepository
                .findByUserIdClient(uuid)
                .stream()
                .map(d -> Map.of(
                        "id_dechet",      (Object) d.getIdDechet(),
                        "id_type_dechet", d.getTypeDechet().getIdTypeDechet(),
                        "label",          d.getTypeDechet().getLabel(),
                        "date_tri",       d.getDateTri().toString()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ─── GET /api/dechets/types ───────────────────────────────────────────────
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, Object>>> getAllTypes() {
        List<Map<String, Object>> result = typeDechetRepository.findAll()
                .stream()
                .map(t -> Map.of(
                        "id_type_dechet", (Object) t.getIdTypeDechet(),
                        "etiquette",      t.getEtiquette()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}