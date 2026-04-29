package fr.ecosort.backend.controllers;

import fr.ecosort.backend.models.Users;
import fr.ecosort.backend.repositories.UsersRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UsersRepository usersRepository;

    public UsersController(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @PostMapping
    public Users create(@RequestBody Users user) {
        return usersRepository.save(user);
    }

    @GetMapping
    public List<Users> getAll() {
        return usersRepository.findAll();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Users> getByEmail(@PathVariable String email) {
        return usersRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}