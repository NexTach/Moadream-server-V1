package com.nextech.moadream.server.v1.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private KakaoOAuthService kakaoOAuthService;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    private UserAuthenticationService userAuthenticationService;

    @InjectMocks
    private UserProfileService userProfileService;

    private UserService userService;

    private User testUser;
    private UserSignUpRequest signUpRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() throws Exception {
        testUser = User.builder().email("test@example.com").passwordHash("encodedPassword").name("테스트")
                .phone("010-1234-5678").address("서울특별시").dateOfBirth("1990-01-01").userVerificationCode("TESTCODE")
                .build();
        ReflectionTestUtils.setField(testUser, "userId", 1L);

        signUpRequest = new UserSignUpRequest();
        setField(signUpRequest, "email", "test@example.com");
        setField(signUpRequest, "password", "password123");
        setField(signUpRequest, "name", "테스트");
        setField(signUpRequest, "phone", "010-1234-5678");
        setField(signUpRequest, "address", "서울특별시");
        setField(signUpRequest, "dateOfBirth", "1990-01-01");

        loginRequest = new LoginRequest();
        setField(loginRequest, "email", "test@example.com");
        setField(loginRequest, "password", "password123");

        userAuthenticationService = new UserAuthenticationService(userRepository, passwordEncoder, jwtProvider,
                kakaoOAuthService, userRegistrationService);
        userService = new UserService(userRegistrationService, userAuthenticationService, userProfileService);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() {
        // given
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        UserResponse result = userService.signUp(signUpRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("테스트");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 실패")
    void signUp_UserAlreadyExists() {
        // given
        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signUp(signUpRequest)).isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_ALREADY_EXISTS);

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtProvider.generateAccessToken(anyString())).willReturn("accessToken");
        given(jwtProvider.generateRefreshToken(anyString())).willReturn("refreshToken");

        // when
        TokenResponse result = userService.login(loginRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("accessToken");
        assertThat(result.getRefreshToken()).isEqualTo("refreshToken");
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    @Test
    @DisplayName("잘못된 이메일로 로그인 실패")
    void login_InvalidEmail() throws Exception {
        // given
        LoginRequest wrongLoginRequest = new LoginRequest();
        setField(wrongLoginRequest, "email", "wrong@example.com");
        setField(wrongLoginRequest, "password", "password123");
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(wrongLoginRequest)).isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 실패")
    void login_InvalidPassword() throws Exception {
        // given
        LoginRequest wrongPasswordRequest = new LoginRequest();
        setField(wrongPasswordRequest, "email", "test@example.com");
        setField(wrongPasswordRequest, "password", "wrongPassword");
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(wrongPasswordRequest)).isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("사용자 ID로 조회 성공")
    void getUserById_Success() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));

        // when
        UserResponse result = userService.getUserById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("테스트");
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 조회 실패")
    void getUserById_NotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserById(999L)).isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("프로필 업데이트 성공")
    void updateProfile_Success() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));

        // when
        UserResponse result = userService.updateProfile(1L, "새이름", "010-9876-5432", "부산광역시");

        // then
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("비밀번호 업데이트 성공")
    void updatePassword_Success() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(passwordEncoder.encode(anyString())).willReturn("newEncodedPassword");

        // when
        userService.updatePassword(1L, "newPassword123");

        // then
        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("newPassword123");
    }

    @Test
    @DisplayName("액세스 토큰 갱신 성공")
    void refreshAccessToken_Success() {
        // given
        String refreshToken = "validRefreshToken";
        testUser.updateRefreshToken(refreshToken);
        given(jwtProvider.validateToken(anyString())).willReturn(true);
        given(jwtProvider.getEmailFromToken(anyString())).willReturn("test@example.com");
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(testUser));
        given(jwtProvider.generateAccessToken(anyString())).willReturn("newAccessToken");
        given(jwtProvider.generateRefreshToken(anyString())).willReturn("newRefreshToken");

        // when
        TokenResponse result = userService.refreshAccessToken(refreshToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(result.getRefreshToken()).isEqualTo("newRefreshToken");
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 갱신 실패")
    void refreshAccessToken_InvalidToken() {
        // given
        given(jwtProvider.validateToken(anyString())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.refreshAccessToken("invalidToken")).isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
    }
}
