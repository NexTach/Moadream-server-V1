package com.nextech.moadream.server.v1.domain.user.service;

import org.springframework.stereotype.Service;

import com.nextech.moadream.server.v1.domain.user.dto.LoginRequest;
import com.nextech.moadream.server.v1.domain.user.dto.TokenResponse;
import com.nextech.moadream.server.v1.domain.user.dto.UserResponse;
import com.nextech.moadream.server.v1.domain.user.dto.UserSignUpRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRegistrationService userRegistrationService;
    private final UserAuthenticationService userAuthenticationService;
    private final UserProfileService userProfileService;

    public UserResponse signUp(UserSignUpRequest request) {
        return userRegistrationService.signUp(request);
    }

    public TokenResponse login(LoginRequest request) {
        return userAuthenticationService.login(request);
    }

    public UserResponse getUserById(Long userId) {
        return userProfileService.getUserById(userId);
    }

    public UserResponse updateProfile(Long userId, String name, String phone, String address) {
        return userProfileService.updateProfile(userId, name, phone, address);
    }

    public void updatePassword(Long userId, String newPassword) {
        userProfileService.updatePassword(userId, newPassword);
    }

    public TokenResponse refreshAccessToken(String refreshToken) {
        return userAuthenticationService.refreshAccessToken(refreshToken);
    }

    public TokenResponse kakaoLogin(String kakaoAccessToken) {
        return userAuthenticationService.kakaoLogin(kakaoAccessToken);
    }
}
