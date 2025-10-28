package com.nextech.moadream.server.v1.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."), USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST,
            "이미 사용 중인 이메일입니다."), INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."), INVALID_TOKEN(
                    HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."), EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    USER_BILL_NOT_FOUND(HttpStatus.NOT_FOUND, "청구서 정보를 찾을 수 없습니다."), USER_BILL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST,
            "이미 등록된 청구서입니다."), USER_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 설정을 찾을 수 없습니다."),

    USAGE_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "사용량 데이터를 찾을 수 없습니다."), MONTHLY_BILL_NOT_FOUND(HttpStatus.NOT_FOUND,
            "월별 청구서를 찾을 수 없습니다."), BILL_NOT_FOUND(HttpStatus.NOT_FOUND, "청구서를 찾을 수 없습니다."), USAGE_ALERT_NOT_FOUND(
                    HttpStatus.NOT_FOUND,
                    "알림을 찾을 수 없습니다."), ALERT_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."), SETTING_NOT_FOUND(
                            HttpStatus.NOT_FOUND,
                            "사용자 설정을 찾을 수 없습니다."), SETTING_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "사용자 설정이 이미 존재합니다."),

    USAGE_PATTERN_NOT_FOUND(HttpStatus.NOT_FOUND, "사용 패턴을 찾을 수 없습니다."), RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND,
            "추천 정보를 찾을 수 없습니다."), SAVINGS_TRACKING_NOT_FOUND(HttpStatus.NOT_FOUND, "절감 추적 정보를 찾을 수 없습니다."),

    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."), EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "외부 API 호출 중 오류가 발생했습니다."), INVALID_INPUT(HttpStatus.BAD_REQUEST,
                    "잘못된 입력입니다."), INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
