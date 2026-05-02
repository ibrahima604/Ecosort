package fr.ecosort.backend.controllers;

import fr.ecosort.backend.models.Dechet;
import fr.ecosort.backend.models.TypeDechet;
import fr.ecosort.backend.repositories.DechetRepository;
import fr.ecosort.backend.repositories.TypeDechetRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dechets")
public class DechetController {

    private final DechetRepository     dechetRepository;
    private final TypeDechetRepository typeDechetRepository;

    public DechetController(DechetRepository dechetRepository,
                            TypeDechetRepository typeDechetRepository) {
        this.dechetRepository     = dechetRepository;
        this.typeDechetRepository = typeDechetRepository;
    }

    // GET /api/dechets/user/{idClient}
    @GetMapping("/user/{idClient}")
    public ResponseEntity<List<Map<String, Object>>> getByUser(
            @PathVariable String idClient) {
        UUID uuid = UUID.fromString(idClient);
        List<Map<String, Object>> result = dechetRepository
                .findByUserIdClient(uuid)
                .stream()
                .map(d -> Map.of(
                        "id_dechet",       (Object) d.getIdDechet(),
                        "id_type_dechet",  d.getTypeDechet().getIdTypeDechet(),
                        "date_tri",        d.getDateTri().toString()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // GET /api/dechets/types
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