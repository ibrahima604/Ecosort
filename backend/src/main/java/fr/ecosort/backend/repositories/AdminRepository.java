package fr.ecosort.backend.repositories;

import fr.ecosort.backend.models.Admin;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, UUID> {
}
