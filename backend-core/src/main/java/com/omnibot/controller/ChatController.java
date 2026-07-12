package com.omnibot.controller;

import com.omnibot.model.ChatDto.*;
import com.omnibot.model.User;
import com.omnibot.repository.UserRepository;
import com.omnibot.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Chat REST endpoints.
 * All endpoints require a valid Bearer JWT token.
 *
 * POST /api/chat          — send a message, get AI reply + service cards
 * GET  /api/chat/history  — get session chat history
 */
@RestController
@RequestMapping("/v1/chat")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:8080", "null"})
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    public ChatController(ChatService chatService, UserRepository userRepository) {
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request) {

        Long userId = 1L; // Hardcoded for demo integration
        ChatResponse response = chatService.chat(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatHistoryItem>> history(
            @RequestParam String sessionId) {

        Long userId = 1L; // Hardcoded for demo integration
        return ResponseEntity.ok(chatService.getHistory(userId, sessionId));
    }

    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null) return 1L;
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElse(1L);
    }
}
