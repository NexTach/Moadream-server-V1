package com.nextech.moadream.server.v1.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Schema(description = "오류 응답")
@Getter
@Builder
public class ErrorResponse {

    @Schema(description = "오류 메시지", example = "사용자를 찾을 수 없습니다.")
    private final String message;

    @Schema(description = "HTTP 상태 코드", example = "404")
    private final int status;

    @Schema(description = "HTTP 상태", example = "NOT_FOUND")
    private final String error;

    @Schema(description = "오류 발생 시간", example = "2025-01-27T12:34:56")
    private final LocalDateTime timestamp;

    public static ErrorResponse of(HttpStatus status, String message) {
        return ErrorResponse.builder()
                .message(message)
                .status(status.value())
                .error(status.name())
                .timestamp(LocalDateTime.now())
                .build();
    }
}