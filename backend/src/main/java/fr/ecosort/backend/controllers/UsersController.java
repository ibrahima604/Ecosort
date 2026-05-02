package fr.ecosort.backend.controllers;

import fr.ecosort.backend.models.Admin;
import fr.ecosort.backend.models.Users;
import fr.ecosort.backend.repositories.UsersRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UsersRepository usersRepository;

    public UsersController(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @PostMapping
    public ResponseEntity<Users> create(@RequestBody Users user) {
        return ResponseEntity.ok(usersRepository.save(user));
    }

   @GetMapping
public List<Map<String, String>> getAll() {
    return usersRepository.findAllUsersOnly()
            .stream()
            .map(u -> Map.of(
                    "id_client", u.getIdClient().toString(),
                    "nom",       u.getNom(),
                    "prenom",    u.getPrenom(),
                    "email",     u.getEmail()
            ))
            .collect(Collectors.toList());
}

    @GetMapping("/email/{email}")
    public ResponseEntity<Users> getByEmail(@PathVariable String email) {
        return usersRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> update(@PathVariable String id,
                                                      @RequestBody Map<String, String> body) {
        UUID uuid = UUID.fromString(id);
        return usersRepository.findById(uuid).map(user -> {
            if (body.get("nom")    != null) user.setNom(body.get("nom"));
            if (body.get("prenom") != null) user.setPrenom(body.get("prenom"));
            if (body.get("email")  != null) user.setEmail(body.get("email"));
            Users saved = usersRepository.save(user);
            return ResponseEntity.ok(Map.of(
                    "id_client", saved.getIdClient().toString(),
                    "nom",       saved.getNom(),
                    "prenom",    saved.getPrenom(),
                    "email",     saved.getEmail()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        if (!usersRepository.existsById(uuid)) {
            return ResponseEntity.notFound().build();
        }
        usersRepository.deleteById(uuid);
        return ResponseEntity.noContent().build();
    }
}