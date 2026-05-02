package fr.ecosort.backend.controllers;

import fr.ecosort.backend.models.Admin;
import fr.ecosort.backend.repositories.AdminRepository;
import fr.ecosort.backend.repositories.ConseilRepository;
import fr.ecosort.backend.repositories.DechetRepository;
import fr.ecosort.backend.repositories.UsersRepository;   // ← corrigé
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminRepository   adminRepository;
    private final UsersRepository   usersRepository;    // ← corrigé
    private final DechetRepository  dechetRepository;
    private final ConseilRepository conseilRepository;

    public AdminController(AdminRepository adminRepository,
                           UsersRepository usersRepository,   // ← corrigé
                           DechetRepository dechetRepository,
                           ConseilRepository conseilRepository) {
        this.adminRepository   = adminRepository;
        this.usersRepository   = usersRepository;            // ← corrigé
        this.dechetRepository  = dechetRepository;
        this.conseilRepository = conseilRepository;
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Admin> getByEmail(@PathVariable String email) {
        return adminRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers",    usersRepository.countUsersOnly()); // ← exclut l'admin
        stats.put("totalDechets",  dechetRepository.count());
        stats.put("totalConseils", conseilRepository.count());
        return ResponseEntity.ok(stats);
    }
}