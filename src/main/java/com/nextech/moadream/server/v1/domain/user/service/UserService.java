package com.nextech.moadream.server.v1.domain.user.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextech.moadream.server.v1.domain.oauth.dto.KakaoTokenResponse;
import com.nextech.moadream.server.v1.domain.oauth.dto.KakaoUserInfoResponse;
import com.nextech.moadream.server.v1.domain.oauth.service.KakaoOAuthService;
import com.nextech.moadream.server.v1.domain.user.dto.LoginRequest;
import com.nextech.moadream.server.v1.domain.user.dto.TokenResponse;
import com.nextech.moadream.server.v1.domain.user.dto.UserResponse;
import com.nextech.moadream.server.v1.domain.user.dto.UserSignUpRequest;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;
import com.nextech.moadream.server.v1.global.security.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final KakaoOAuthService kakaoOAuthService;

    @Transactional
    public UserResponse signUp(UserSignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }
        String verificationCode = generateVerificationCode();
        User user = User.builder().email(request.getEmail()).passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName()).phone(request.getPhone()).address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth()).userVerificationCode(verificationCode).build();
        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());
        user.updateRefreshToken(refreshToken);
        return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, String name, String phone, String address) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateProfile(name, phone, address);
        return UserResponse.from(user);
    }

    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        String email = jwtProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        String newAccessToken = jwtProvider.generateAccessToken(user.getEmail());
        String newRefreshToken = jwtProvider.generateRefreshToken(user.getEmail());
        user.updateRefreshToken(newRefreshToken);
        return TokenResponse.builder().accessToken(newAccessToken).refreshToken(newRefreshToken).build();
    }

    @Transactional
    public TokenResponse kakaoLogin(String code) {
        KakaoTokenResponse kakaoTokenResponse = kakaoOAuthService.getAccessToken(code);
        KakaoUserInfoResponse userInfo = kakaoOAuthService.getUserInfo(kakaoTokenResponse.getAccessToken());

        String providerId = String.valueOf(userInfo.getId());
        String provider = "KAKAO";

        User user = userRepository.findByProviderAndProviderId(provider, providerId).orElseGet(() -> {
            String verificationCode = generateVerificationCode();
            String email = userInfo.getKakaoAccount().getEmail();
            String name = userInfo.getKakaoAccount().getProfile().getNickname();

            if (email == null) {
                email = providerId + "@kakao.temp";
            }

            User newUser = User.builder().email(email).name(name).provider(provider).providerId(providerId)
                    .userVerificationCode(verificationCode).build();
            return userRepository.save(newUser);
        });

        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());
        user.updateRefreshToken(refreshToken);

        return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    private String generateVerificationCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (userRepository.existsByUserVerificationCode(code));
        return code;
    }
}
