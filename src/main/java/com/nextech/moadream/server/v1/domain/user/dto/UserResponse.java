package com.nextech.moadream.server.v1.domain.user.dto;

import com.nextech.moadream.server.v1.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long userId;
    private String email;
    private String name;
    private String phone;
    private String address;
    private String dateOfBirth;
    private String userVerificationCode;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .userVerificationCode(user.getUserVerificationCode())
                .createdAt(user.getCreatedAt())
                .build();
    }
}