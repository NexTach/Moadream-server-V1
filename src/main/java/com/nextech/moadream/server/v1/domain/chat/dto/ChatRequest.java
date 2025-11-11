package com.nextech.moadream.server.v1.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    private Long sessionId;

    @NotBlank(message = "메시지 내용은 필수입니다.")
    private String message;
}
