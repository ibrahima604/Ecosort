package fr.ecosort.backend.repositories;

import fr.ecosort.backend.models.Dechet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DechetRepository extends JpaRepository<Dechet, Integer> {
}
