package com.example.Backend.controller;

import com.example.Backend.dto.request.CreateCommentRequest;
import com.example.Backend.dto.response.CommentResponse;
import com.example.Backend.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/signals/{signalId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(@PathVariable UUID signalId,
                                         @Valid @RequestBody CreateCommentRequest req,
                                         Authentication auth) {
        return commentService.createComment(signalId, userId(auth), req);
    }

    @GetMapping("/signals/{signalId}/comments")
    public List<CommentResponse> getComments(@PathVariable UUID signalId) {
        return commentService.getComments(signalId);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable UUID commentId, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        commentService.deleteComment(commentId, userId(auth), isAdmin);
    }

    private UUID userId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }
}
