package com.nextech.moadream.server.v1.domain.user.dto;

import java.time.LocalDateTime;

import com.nextech.moadream.server.v1.domain.user.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "사용자 응답")
@Getter
@Builder
public class UserResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "생년월일", example = "1990-01-01")
    private String dateOfBirth;

    @Schema(description = "사용자 인증 코드 (청구서 연동용)", example = "A1B2C3D4")
    private String userVerificationCode;

    @Schema(description = "계정 생성 일시", example = "2025-01-01T00:00:00")
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder().userId(user.getUserId()).email(user.getEmail()).name(user.getName())
                .phone(user.getPhone()).address(user.getAddress()).dateOfBirth(user.getDateOfBirth())
                .userVerificationCode(user.getUserVerificationCode()).createdAt(user.getCreatedAt()).build();
    }
}
