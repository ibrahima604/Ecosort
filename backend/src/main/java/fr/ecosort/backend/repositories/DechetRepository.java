package fr.ecosort.backend.repositories;

import fr.ecosort.backend.models.Dechet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DechetRepository extends JpaRepository<Dechet, Integer> {
    List<Dechet> findByUserIdClient(UUID idClient);
}