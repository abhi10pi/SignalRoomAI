package com.example.Backend.repository;

import com.example.Backend.entity.Signal;
import com.example.Backend.enums.SignalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SignalRepository extends JpaRepository<Signal, UUID> {

    List<Signal> findByAuthorId(UUID authorId);

    List<Signal> findByStatus(SignalStatus status);

    List<Signal> findByStatusAndDiscussionEndBefore(SignalStatus status, LocalDateTime cutoff);

    @Query("SELECT s FROM Signal s WHERE s.status IN ('OPEN','PROCESSING','CLOSED','FAILED','ARCHIVED')")
    Page<Signal> findPublic(Pageable pageable);

    @Query("SELECT s FROM Signal s WHERE s.status IN ('OPEN','PROCESSING','CLOSED','FAILED','ARCHIVED') AND s.category = :category")
    Page<Signal> findPublicByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT s FROM Signal s WHERE s.status IN ('OPEN','PROCESSING','CLOSED','FAILED','ARCHIVED') AND " +
           "(LOWER(s.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Signal> search(@Param("q") String q, Pageable pageable);
}
