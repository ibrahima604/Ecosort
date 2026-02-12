package fr.ecosort.backend.repositories;

import fr.ecosort.backend.models.Conseil;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConseilRepository extends JpaRepository<Conseil, Integer> {
}
