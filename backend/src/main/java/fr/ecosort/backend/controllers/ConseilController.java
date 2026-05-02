package fr.ecosort.backend.controllers;

import fr.ecosort.backend.models.Conseil;
import fr.ecosort.backend.services.ConseilService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conseils")
public class ConseilController {

    private final ConseilService conseilService;

    public ConseilController(ConseilService conseilService) {
        this.conseilService = conseilService;
    }

    @GetMapping
    public List<Conseil> getAll() {
        return conseilService.getAll();
    }

    @PostMapping
    public ResponseEntity<Conseil> create(@RequestBody Map<String, Object> body) {
        String titre       = (String) body.get("titre");
        String description = (String) body.get("description");
        String idAdmin     = (String) body.get("idAdmin");
        return ResponseEntity.ok(conseilService.create(titre, description, idAdmin));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Conseil> update(@PathVariable int id,
                                          @RequestBody Map<String, Object> body) {
        String titre       = (String) body.get("titre");
        String description = (String) body.get("description");
        return ResponseEntity.ok(conseilService.update(id, titre, description));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        conseilService.delete(id);
        return ResponseEntity.noContent().build();
    }
}