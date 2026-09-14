package com.example.Backend.repository;

import com.example.Backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findBySignalIdAndParentCommentIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(UUID signalId);

    List<Comment> findByParentCommentIdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID parentId);
}
