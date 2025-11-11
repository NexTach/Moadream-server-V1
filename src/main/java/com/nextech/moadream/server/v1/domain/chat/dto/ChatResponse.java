package com.nextech.moadream.server.v1.domain.chat.dto;

import java.time.LocalDateTime;

import com.nextech.moadream.server.v1.domain.chat.entity.ChatMessage;
import com.nextech.moadream.server.v1.domain.chat.enums.MessageRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {

    private Long sessionId;
    private Long messageId;
    private MessageRole role;
    private String content;
    private Integer tokensUsed;
    private LocalDateTime createdAt;

    public static ChatResponse from(ChatMessage message) {
        return ChatResponse.builder().sessionId(message.getChatSession().getSessionId())
                .messageId(message.getMessageId()).role(message.getRole()).content(message.getContent())
                .tokensUsed(message.getTokensUsed()).createdAt(message.getCreatedAt()).build();
    }
}