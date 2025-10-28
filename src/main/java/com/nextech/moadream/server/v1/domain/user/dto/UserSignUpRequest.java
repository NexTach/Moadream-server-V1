package com.nextech.moadream.server.v1.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회원가입 요청")
@Getter
@NoArgsConstructor
public class UserSignUpRequest {

    @Schema(description = "이메일 주소", example = "user@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "비밀번호 (최소 8자)", example = "password123!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @Schema(description = "사용자 이름", example = "홍길동")
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "생년월일", example = "1990-01-01")
    private String dateOfBirth;
}
