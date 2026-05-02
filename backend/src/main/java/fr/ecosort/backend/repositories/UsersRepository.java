package fr.ecosort.backend.repositories;

import fr.ecosort.backend.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UsersRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByEmail(String email);

    @Query("SELECT u FROM Users u WHERE TYPE(u) = Users")
    List<Users> findAllUsersOnly();

    //compte uniquement les clients (exclut Admin)
    @Query("SELECT COUNT(u) FROM Users u WHERE TYPE(u) = Users")
    long countUsersOnly();
}