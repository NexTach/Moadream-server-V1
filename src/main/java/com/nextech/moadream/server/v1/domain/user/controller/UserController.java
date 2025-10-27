package com.nextech.moadream.server.v1.domain.user.controller;

import com.nextech.moadream.server.v1.domain.user.dto.UserResponse;
import com.nextech.moadream.server.v1.domain.user.dto.UserSignUpRequest;
import com.nextech.moadream.server.v1.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자를 등록합니다. 이메일 중복 확인 및 비밀번호 암호화가 자동으로 처리됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이메일 중복, 유효성 검증 실패 등)"
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(
            @Parameter(description = "회원가입 요청 정보", required = true)
            @Valid @RequestBody UserSignUpRequest request
    ) {
        UserResponse response = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "사용자 조회",
            description = "사용자 ID로 사용자 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long userId
    ) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }
}