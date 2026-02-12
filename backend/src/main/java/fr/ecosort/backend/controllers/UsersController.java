package fr.ecosort.backend.controllers;

import fr.ecosort.backend.models.Users;
import fr.ecosort.backend.repositories.UsersRepository;
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
}
