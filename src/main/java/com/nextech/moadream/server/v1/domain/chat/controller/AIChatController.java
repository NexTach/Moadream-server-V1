package com.nextech.moadream.server.v1.domain.chat.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nextech.moadream.server.v1.domain.chat.dto.ChatRequest;
import com.nextech.moadream.server.v1.domain.chat.dto.ChatResponse;
import com.nextech.moadream.server.v1.domain.chat.dto.ChatSessionResponse;
import com.nextech.moadream.server.v1.domain.chat.service.AIChatService;
import com.nextech.moadream.server.v1.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "AI Chat", description = "AI 챗봇 상담 API")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AIChatController {

    private final AIChatService aiChatService;

    @Operation(summary = "메시지 전송", description = "AI 챗봇에게 메시지를 전송하고 응답을 받습니다. sessionId가 없으면 새 세션이 생성됩니다.")
    @PostMapping("/users/{userId}/message")
    public ResponseEntity<ApiResponse<ChatResponse>> sendMessage(
            @Parameter(description = "사용자 ID") @PathVariable Long userId, @Valid @RequestBody ChatRequest request) {
        ChatResponse response = aiChatService.sendMessage(userId, request.getSessionId(), request.getMessage());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "세션 메시지 조회", description = "특정 세션의 모든 메시지를 조회합니다.")
    @GetMapping("/users/{userId}/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<ChatResponse>>> getSessionMessages(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "세션 ID") @PathVariable Long sessionId) {
        List<ChatResponse> messages = aiChatService.getSessionMessages(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @Operation(summary = "사용자 세션 목록 조회", description = "사용자의 모든 활성 채팅 세션을 조회합니다.")
    @GetMapping("/users/{userId}/sessions")
    public ResponseEntity<ApiResponse<List<ChatSessionResponse>>> getUserSessions(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<ChatSessionResponse> sessions = aiChatService.getUserSessions(userId).stream()
                .map(ChatSessionResponse::fromWithoutMessages).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @Operation(summary = "세션 삭제", description = "특정 채팅 세션을 비활성화합니다.")
    @DeleteMapping("/users/{userId}/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(@Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "세션 ID") @PathVariable Long sessionId) {
        aiChatService.deleteSession(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}