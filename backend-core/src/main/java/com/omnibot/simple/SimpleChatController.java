package com.omnibot.simple;

import com.omnibot.simple.SimpleChatDto.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Simple Chat Controller — public, no JWT, no security.
 *
 * Endpoints:
 *   POST /api/simple/chat   — send a message
 *
 * Example request body:
 *   { "message": "I want to order food", "sessionId": "abc123" }
 *
 * Example response:
 *   {
 *     "sessionId": "abc123",
 *     "reply": "Sure! Where should food be delivered to?",
 *     "intent": "FOOD",
 *     "step": "waiting_location",
 *     "options": []
 *   }
 */
@RestController
@RequestMapping("/simple")
@CrossOrigin(origins = "*")   // open CORS — safe for local dev
public class SimpleChatController {

    private final SimpleChatService chatService;

    public SimpleChatController(SimpleChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public Response chat(@RequestBody Request request) {
        // Auto-generate session ID if the client didn't send one
        String sessionId = (request.getSessionId() != null && !request.getSessionId().isBlank())
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        return chatService.chat(sessionId, request.getMessage());
    }
}
