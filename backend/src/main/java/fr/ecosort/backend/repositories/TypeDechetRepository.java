package fr.ecosort.backend.repositories;

import fr.ecosort.backend.models.TypeDechet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeDechetRepository extends JpaRepository<TypeDechet, Integer> {
}
