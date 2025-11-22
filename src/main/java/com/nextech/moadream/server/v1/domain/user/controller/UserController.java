package com.nextech.moadream.server.v1.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nextech.moadream.server.v1.domain.user.dto.LoginRequest;
import com.nextech.moadream.server.v1.domain.user.dto.RefreshTokenRequest;
import com.nextech.moadream.server.v1.domain.user.dto.TokenResponse;
import com.nextech.moadream.server.v1.domain.user.dto.UserResponse;
import com.nextech.moadream.server.v1.domain.user.dto.UserSignUpRequest;
import com.nextech.moadream.server.v1.domain.user.service.UserAuthenticationService;
import com.nextech.moadream.server.v1.domain.user.service.UserProfileService;
import com.nextech.moadream.server.v1.domain.user.service.UserRegistrationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserRegistrationService userRegistrationService;
    private final UserAuthenticationService userAuthenticationService;
    private final UserProfileService userProfileService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다. 이메일 중복 확인 및 비밀번호 암호화가 자동으로 처리됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이메일 중복, 유효성 검증 실패 등)", content = @Content())})
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(
            @Parameter(description = "회원가입 요청 정보", required = true) @Valid @RequestBody UserSignUpRequest request) {
        UserResponse response = userRegistrationService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "이메일 또는 비밀번호가 올바르지 않음", content = @Content())})
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Parameter(description = "로그인 요청 정보", required = true) @Valid @RequestBody LoginRequest request) {
        TokenResponse response = userAuthenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 토큰", content = @Content())})
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Parameter(description = "토큰 재발급 요청 정보", required = true) @Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = userAuthenticationService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용자 조회", description = "사용자 ID로 사용자 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content())})
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "사용자 ID", required = true, example = "1") @PathVariable Long userId) {
        UserResponse response = userProfileService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카카오 로그인", description = "카카오 Access Token을 사용하여 로그인합니다. 신규 사용자의 경우 자동으로 회원가입이 진행됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "OAuth 인증 실패")})
    @PostMapping("/kakao/login")
    public ResponseEntity<TokenResponse> kakaoLogin(
            @Parameter(description = "카카오 Access Token", required = true) @RequestParam String accessToken) {
        TokenResponse response = userAuthenticationService.kakaoLogin(accessToken);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "테스트용 JWT 발급", description = "만료 시간이 매우 긴 테스트용 JWT를 발급합니다. 최초 호출 시 '이주언' 사용자를 자동으로 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT 발급 성공", content = @Content(schema = @Schema(implementation = TokenResponse.class)))})
    @GetMapping("/test-token")
    public ResponseEntity<TokenResponse> getTestToken() {
        TokenResponse response = userAuthenticationService.getTestToken();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "현재 인증된 사용자 정보 조회", description = "JWT 토큰으로부터 현재 인증된 사용자의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content())})
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse response = userProfileService.getCurrentUser();
        return ResponseEntity.ok(response);
    }
}
