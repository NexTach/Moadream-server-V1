package com.nextech.moadream.server.v1.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextech.moadream.server.v1.domain.oauth.dto.KakaoUserInfoResponse;
import com.nextech.moadream.server.v1.domain.oauth.service.KakaoOAuthService;
import com.nextech.moadream.server.v1.domain.user.dto.LoginRequest;
import com.nextech.moadream.server.v1.domain.user.dto.TokenResponse;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;
import com.nextech.moadream.server.v1.global.security.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final KakaoOAuthService kakaoOAuthService;
    private final UserRegistrationService userRegistrationService;

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
        return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).userId(user.getUserId())
                .build();
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
    public TokenResponse kakaoLogin(String kakaoAccessToken) {
        // 카카오 Access Token으로 사용자 정보 조회
        KakaoUserInfoResponse userInfo = kakaoOAuthService.getUserInfo(kakaoAccessToken);

        String providerId = String.valueOf(userInfo.getId());
        String provider = "KAKAO";

        User user = userRepository.findByProviderAndProviderId(provider, providerId).orElseGet(() -> {
            String verificationCode = userRegistrationService.generateVerificationCode();
            String email = userInfo.getKakaoAccount() != null && userInfo.getKakaoAccount().getEmail() != null
                    ? userInfo.getKakaoAccount().getEmail()
                    : providerId + "@kakao.temp";

            String name = "카카오사용자";
            if (userInfo.getKakaoAccount() != null && userInfo.getKakaoAccount().getProfile() != null
                    && userInfo.getKakaoAccount().getProfile().getNickname() != null) {
                name = userInfo.getKakaoAccount().getProfile().getNickname();
            }

            User newUser = User.builder().email(email).name(name).provider(provider).providerId(providerId)
                    .userVerificationCode(verificationCode).build();
            return userRepository.save(newUser);
        });

        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());
        user.updateRefreshToken(refreshToken);

        return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).userId(user.getUserId())
                .build();
    }

    @Transactional
    public TokenResponse getTestToken() {
        String testEmail = "jueonlee@test.com";
        String testName = "이주언";

        User user = userRepository.findByEmail(testEmail).orElseGet(() -> {
            String verificationCode = generateTestVerificationCode();
            User newUser = User.builder().email(testEmail).name(testName).userVerificationCode(verificationCode)
                    .build();
            return userRepository.save(newUser);
        });

        String accessToken = jwtProvider.generateLongLivedToken(user.getEmail());
        String refreshToken = jwtProvider.generateLongLivedToken(user.getEmail());
        user.updateRefreshToken(refreshToken);

        return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).userId(user.getUserId())
                .build();
    }

    private String generateTestVerificationCode() {
        String code;
        do {
            code = "TEST" + System.currentTimeMillis();
        } while (userRepository.existsByUserVerificationCode(code));
        return code;
    }
}
