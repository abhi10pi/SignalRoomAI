package com.example.Backend.repository;

import com.example.Backend.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DomainRepository extends JpaRepository<Domain, UUID> {
    Optional<Domain> findBySlug(String slug);
    boolean existsByName(String name);
}
