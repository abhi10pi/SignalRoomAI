package com.example.Backend.service;

import com.example.Backend.dto.request.CreateCommentRequest;
import com.example.Backend.dto.response.CommentResponse;
import com.example.Backend.entity.*;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.exception.UnauthorizedException;
import com.example.Backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final SignalRepository signalRepository;
    private final OpinionRepository opinionRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          SignalRepository signalRepository,
                          OpinionRepository opinionRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.signalRepository = signalRepository;
        this.opinionRepository = opinionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommentResponse createComment(UUID signalId, UUID userId, CreateCommentRequest req) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));
        if (signal.getStatus() != SignalStatus.OPEN) {
            throw new IllegalArgumentException("Signal is not open for comments");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setSignal(signal);
        comment.setUser(user);
        comment.setContent(req.getContent());

        if (req.getOpinionId() != null) {
            Opinion opinion = opinionRepository.findById(req.getOpinionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Opinion not found"));
            if (!opinion.getSignal().getId().equals(signalId)) {
                throw new IllegalArgumentException("Opinion does not belong to this signal");
            }
            comment.setOpinion(opinion);
        }

        if (req.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(req.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            if (!parent.getSignal().getId().equals(signalId)) {
                throw new IllegalArgumentException("Parent comment does not belong to this signal");
            }
            comment.setParentComment(parent);
        }

        return toResponse(commentRepository.save(comment));
    }

    public List<CommentResponse> getComments(UUID signalId) {
        List<Comment> roots = commentRepository
                .findBySignalIdAndParentCommentIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(signalId);
        return roots.stream().map(this::toResponseWithReplies).collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID userId, boolean isAdmin) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (!isAdmin && !comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not your comment");
        }
        comment.setDeletedAt(LocalDateTime.now());
        comment.setContent("[deleted]");
        commentRepository.save(comment);
    }

    private CommentResponse toResponseWithReplies(Comment c) {
        CommentResponse r = toResponse(c);
        List<Comment> replies = commentRepository
                .findByParentCommentIdAndDeletedAtIsNullOrderByCreatedAtAsc(c.getId());
        r.setReplies(replies.stream().map(this::toResponse).collect(Collectors.toList()));
        return r;
    }

    private CommentResponse toResponse(Comment c) {
        CommentResponse r = new CommentResponse();
        r.setId(c.getId());
        r.setSignalId(c.getSignal().getId());
        r.setOpinionId(c.getOpinion() != null ? c.getOpinion().getId() : null);
        r.setUserId(c.getUser().getId());
        r.setUsername(c.getUser().getUsername());
        r.setParentCommentId(c.getParentComment() != null ? c.getParentComment().getId() : null);
        r.setContent(c.getContent());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }
}
