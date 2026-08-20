package com.example.Backend.repository;

import com.example.Backend.entity.Signal;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.enums.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

public interface SignalRepository extends JpaRepository<Signal, UUID> {

    List<Signal> findBySubmitterId(UUID submitterId);

    List<Signal> findBySubmitterIdAndStatus(UUID submitterId, SignalStatus status);

    List<Signal> findByStatus(SignalStatus status);

    List<Signal> findByStatusInAndResolutionDateBefore(
           List<SignalStatus> statuses, LocalDateTime cutoff);

    List<Signal> findByDomainId(UUID domainId);

    List<Signal> findByDomainIdAndStatus(UUID domainId, SignalStatus status);

    List<Signal> findByVisibility(Visibility visibility);

    Optional<Signal> findByIdAndSubmitterId(UUID id, UUID submitterId);

    @Query("SELECT s FROM Signal s WHERE s.visibility = 'PUBLIC' AND s.status != 'DRAFT'")
    Page<Signal> findPublic(Pageable pageable);

    @Query("SELECT s FROM Signal s WHERE s.visibility = 'PUBLIC' AND s.status != 'DRAFT' AND s.domain.id = :domainId")
    Page<Signal> findPublicByDomain(@Param("domainId") UUID domainId, Pageable pageable);

    @Query("SELECT s FROM Signal s WHERE s.visibility = 'PUBLIC' AND s.status != 'DRAFT' " +
           "AND (LOWER(s.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Signal> search(@Param("q") String q, Pageable pageable);

    @Query("SELECT s FROM Signal s WHERE s.visibility = 'PUBLIC' AND s.status != 'DRAFT' " +
           "AND s.domain.slug = :slug")
    Page<Signal> findPublicByDomainSlug(@Param("slug") String slug, Pageable pageable);
}
