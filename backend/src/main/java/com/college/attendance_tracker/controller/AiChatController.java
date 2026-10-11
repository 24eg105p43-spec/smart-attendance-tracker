package com.college.attendance_tracker.controller;

import com.college.attendance_tracker.dto.ChatRequest;
import com.college.attendance_tracker.dto.ChatResponse;
import com.college.attendance_tracker.service.AiChatService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request, Authentication auth) {
        return aiChatService.processQuery(request.message(), auth.getName());
    }
}
