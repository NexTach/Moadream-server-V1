package com.nextech.moadream.server.v1.domain.chat.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.nextech.moadream.server.v1.domain.chat.entity.ChatSession;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSessionResponse {

    private Long sessionId;
    private String sessionTitle;
    private Boolean isActive;
    private List<ChatResponse> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChatSessionResponse from(ChatSession session) {
        return ChatSessionResponse.builder().sessionId(session.getSessionId()).sessionTitle(session.getSessionTitle())
                .isActive(session.getIsActive())
                .messages(session.getMessages().stream().map(ChatResponse::from).collect(Collectors.toList()))
                .createdAt(session.getCreatedAt()).updatedAt(session.getUpdatedAt()).build();
    }

    public static ChatSessionResponse fromWithoutMessages(ChatSession session) {
        return ChatSessionResponse.builder().sessionId(session.getSessionId()).sessionTitle(session.getSessionTitle())
                .isActive(session.getIsActive()).messages(List.of()).createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt()).build();
    }
}
