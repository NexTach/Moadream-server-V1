# Moadream Server API 문서

## 목차
1. [인증 (Authentication)](#1-인증-authentication)
2. [사용자 관리 (User Management)](#2-사용자-관리-user-management)
3. [사용자 설정 (User Settings)](#3-사용자-설정-user-settings)
4. [사용량 데이터 (Usage Data)](#4-사용량-데이터-usage-data)
5. [사용량 알림 (Usage Alerts)](#5-사용량-알림-usage-alerts)
6. [월간 청구서 (Monthly Bills)](#6-월간-청구서-monthly-bills)
7. [AI 챗봇 (AI Chat)](#7-ai-챗봇-ai-chat)
8. [AI 절약 추천 (Recommendations)](#8-ai-절약-추천-recommendations)
9. [절감 효과 추적 (Savings Tracking)](#9-절감-효과-추적-savings-tracking)
10. [사용 패턴 분석 (Usage Patterns)](#10-사용-패턴-분석-usage-patterns)
11. [뷰 페이지 (Views)](#11-뷰-페이지-views)

---

## 1. 인증 (Authentication)

### Base URL: `/api/v1/auth`

#### 1.1 회원가입
- **Method**: `POST`
- **Path**: `/api/v1/auth/signup`
- **Description**: 새로운 사용자를 등록합니다. 이메일 중복 확인 및 비밀번호 암호화가 자동으로 처리됩니다.
- **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678"
}
```
- **Response**: `201 Created`
```json
{
  "userId": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678",
  "provider": "LOCAL",
  "createdAt": "2024-01-01T00:00:00"
}
```

#### 1.2 로그인
- **Method**: `POST`
- **Path**: `/api/v1/auth/login`
- **Description**: 이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.
- **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
- **Response**: `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

#### 1.3 토큰 재발급
- **Method**: `POST`
- **Path**: `/api/v1/auth/refresh`
- **Description**: Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.
- **Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
- **Response**: `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

#### 1.4 카카오 로그인
- **Method**: `POST`
- **Path**: `/api/v1/auth/kakao/login`
- **Description**: 카카오 Access Token을 사용하여 로그인합니다. 신규 사용자의 경우 자동으로 회원가입이 진행됩니다.
- **Request Parameters**:
  - `accessToken` (query parameter): 카카오 Access Token
- **Response**: `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

## 2. 사용자 관리 (User Management)

### Base URL: `/api/v1/auth`

#### 2.1 사용자 조회
- **Method**: `GET`
- **Path**: `/api/v1/auth/users/{userId}`
- **Description**: 사용자 ID로 사용자 정보를 조회합니다.
- **Path Variables**:
  - `userId`: 사용자 ID
- **Response**: `200 OK`
```json
{
  "userId": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678",
  "provider": "LOCAL",
  "createdAt": "2024-01-01T00:00:00"
}
```

---

## 3. 사용자 설정 (User Settings)

### Base URL: `/api/v1/settings`
### 인증 필요: Bearer Token

#### 3.1 사용자 설정 조회
- **Method**: `GET`
- **Path**: `/api/v1/settings/users/{userId}`
- **Description**: 사용자의 설정을 조회합니다.
- **Response**: `200 OK`
```json
{
  "status": "SUCCESS",
  "data": {
    "settingId": 1,
    "userId": 1,
    "monthlyBudget": 500000,
    "alertThreshold": 0.8,
    "pushNotificationEnabled": true,
    "emailNotificationEnabled": true,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

#### 3.2 사용자 설정 생성
- **Method**: `POST`
- **Path**: `/api/v1/settings/users/{userId}`
- **Description**: 사용자의 설정을 생성합니다.
- **Request Body**:
```json
{
  "monthlyBudget": 500000,
  "alertThreshold": 0.8,
  "pushNotificationEnabled": true,
  "emailNotificationEnabled": true
}
```

#### 3.3 예산 설정 수정
- **Method**: `PATCH`
- **Path**: `/api/v1/settings/users/{userId}/budget`
- **Description**: 월간 예산 및 알림 임계값을 수정합니다.
- **Request Body**:
```json
{
  "monthlyBudget": 600000,
  "alertThreshold": 0.9
}
```

#### 3.4 알림 설정 수정
- **Method**: `PATCH`
- **Path**: `/api/v1/settings/users/{userId}/notifications`
- **Description**: 푸시 및 이메일 알림 설정을 수정합니다.
- **Request Body**:
```json
{
  "pushNotificationEnabled": false,
  "emailNotificationEnabled": true
}
```

#### 3.5 사용자 설정 전체 수정
- **Method**: `PUT`
- **Path**: `/api/v1/settings/users/{userId}`
- **Description**: 사용자의 모든 설정을 수정합니다.
- **Request Body**:
```json
{
  "monthlyBudget": 500000,
  "alertThreshold": 0.8,
  "pushNotificationEnabled": true,
  "emailNotificationEnabled": true
}
```

---

## 4. 사용량 데이터 (Usage Data)

### Base URL: `/api/v1/usage-data`
### 인증 필요: Bearer Token

#### 4.1 사용량 데이터 등록
- **Method**: `POST`
- **Path**: `/api/v1/usage-data/users/{userId}`
- **Description**: 새로운 사용량 데이터를 등록합니다.
- **Request Body**:
```json
{
  "utilityType": "ELECTRICITY",
  "usageAmount": 350.5,
  "usageDate": "2024-01-01T00:00:00",
  "cost": 45000
}
```
- **UtilityType**: `ELECTRICITY`, `WATER`, `GAS`, `INTERNET`, `MOBILE`

#### 4.2 사용자 사용량 데이터 조회
- **Method**: `GET`
- **Path**: `/api/v1/usage-data/users/{userId}`
- **Description**: 사용자의 모든 사용량 데이터를 조회합니다.

#### 4.3 유형별 사용량 데이터 조회
- **Method**: `GET`
- **Path**: `/api/v1/usage-data/users/{userId}/type/{utilityType}`
- **Description**: 특정 유형의 사용량 데이터를 조회합니다.

#### 4.4 기간별 사용량 데이터 조회
- **Method**: `GET`
- **Path**: `/api/v1/usage-data/users/{userId}/range`
- **Description**: 특정 기간의 사용량 데이터를 조회합니다.
- **Query Parameters**:
  - `startDate`: 시작 날짜 (ISO 8601 format)
  - `endDate`: 종료 날짜 (ISO 8601 format)

#### 4.5 최신 사용량 데이터 조회
- **Method**: `GET`
- **Path**: `/api/v1/usage-data/users/{userId}/latest/{utilityType}`
- **Description**: 특정 유형의 최신 사용량 데이터를 조회합니다.

#### 4.6 사용량 데이터 수정
- **Method**: `PUT`
- **Path**: `/api/v1/usage-data/users/{userId}/{usageId}`
- **Description**: 사용량 데이터를 수정합니다.

---

## 5. 사용량 알림 (Usage Alerts)

### Base URL: `/api/v1/alerts`
### 인증 필요: Bearer Token

#### 5.1 알림 생성
- **Method**: `POST`
- **Path**: `/api/v1/alerts/users/{userId}`
- **Description**: 새로운 알림을 생성합니다.
- **Request Body**:
```json
{
  "utilityType": "ELECTRICITY",
  "alertType": "BUDGET_EXCEEDED",
  "message": "전기 예산을 초과했습니다.",
  "threshold": 0.9
}
```
- **AlertType**: `BUDGET_WARNING`, `BUDGET_EXCEEDED`, `UNUSUAL_USAGE`, `SAVING_OPPORTUNITY`

#### 5.2 사용자 알림 조회
- **Method**: `GET`
- **Path**: `/api/v1/alerts/users/{userId}`
- **Description**: 사용자의 모든 알림을 조회합니다.

#### 5.3 미읽음 알림 조회
- **Method**: `GET`
- **Path**: `/api/v1/alerts/users/{userId}/unread`
- **Description**: 사용자의 미읽음 알림을 조회합니다.

#### 5.4 유형별 알림 조회
- **Method**: `GET`
- **Path**: `/api/v1/alerts/users/{userId}/type/{utilityType}`
- **Description**: 특정 유형의 알림을 조회합니다.

#### 5.5 알림 타입별 조회
- **Method**: `GET`
- **Path**: `/api/v1/alerts/users/{userId}/alert-type/{alertType}`
- **Description**: 특정 알림 타입의 알림을 조회합니다.

#### 5.6 알림 읽음 처리
- **Method**: `PATCH`
- **Path**: `/api/v1/alerts/{alertId}/read`
- **Description**: 특정 알림을 읽음으로 표시합니다.

#### 5.7 모든 알림 읽음 처리
- **Method**: `PATCH`
- **Path**: `/api/v1/alerts/users/{userId}/read-all`
- **Description**: 사용자의 모든 미읽음 알림을 읽음으로 표시합니다.

---

## 6. 월간 청구서 (Monthly Bills)

### Base URL: `/api/v1/bills`
### 인증 필요: Bearer Token

#### 6.1 청구서 생성
- **Method**: `POST`
- **Path**: `/api/v1/bills/users/{userId}`
- **Description**: 새로운 청구서를 생성합니다.
- **Request Body**:
```json
{
  "utilityType": "ELECTRICITY",
  "billingMonth": "2024-01-01",
  "usageAmount": 350.5,
  "totalCost": 45000,
  "dueDate": "2024-02-10"
}
```

#### 6.2 사용자 청구서 조회
- **Method**: `GET`
- **Path**: `/api/v1/bills/users/{userId}`
- **Description**: 사용자의 모든 청구서를 조회합니다.

#### 6.3 유형별 청구서 조회
- **Method**: `GET`
- **Path**: `/api/v1/bills/users/{userId}/type/{utilityType}`
- **Description**: 특정 유형의 청구서를 조회합니다.

#### 6.4 월별 청구서 조회
- **Method**: `GET`
- **Path**: `/api/v1/bills/users/{userId}/month`
- **Description**: 특정 월의 청구서를 조회합니다.
- **Query Parameters**:
  - `utilityType`: 사용량 유형
  - `billingMonth`: 청구 월 (ISO date format)

#### 6.5 미납 청구서 조회
- **Method**: `GET`
- **Path**: `/api/v1/bills/users/{userId}/unpaid`
- **Description**: 사용자의 미납 청구서를 조회합니다.

#### 6.6 청구서 납부 처리
- **Method**: `PATCH`
- **Path**: `/api/v1/bills/{billId}/pay`
- **Description**: 청구서를 납부 완료로 표시합니다.

#### 6.7 청구서 통계 조회
- **Method**: `GET`
- **Path**: `/api/v1/bills/users/{userId}/statistics`
- **Description**: 특정 기간의 청구서 통계를 조회합니다.
- **Query Parameters**:
  - `startMonth`: 시작 월 (ISO date format)
  - `endMonth`: 종료 월 (ISO date format)

#### 6.8 전월 대비 청구서 비교 (특정 유형) ⭐
- **Method**: `GET`
- **Path**: `/api/v1/bills/users/{userId}/compare/{utilityType}`
- **Description**: 특정 유형(전기/수도/가스 등)의 전월 대비 사용량 및 요금 증감률을 조회합니다.
- **Path Variables**:
  - `userId`: 사용자 ID
  - `utilityType`: 사용량 유형 (ELECTRICITY, WATER, GAS, INTERNET, MOBILE)
- **Query Parameters**:
  - `currentMonth`: 비교 기준 월 (ISO date format, 예: 2024-01-01)
- **Response**: `200 OK`
```json
{
  "status": "SUCCESS",
  "data": {
    "utilityType": "ELECTRICITY",
    "currentMonth": "2024-01-01",
    "previousMonth": "2023-12-01",
    "currentUsage": 350.5,
    "previousUsage": 300.0,
    "currentCharge": 45000.00,
    "previousCharge": 40000.00,
    "usageChangeRate": 16.83,
    "chargeChangeRate": 12.5,
    "usageChange": 50.5,
    "chargeChange": 5000.00,
    "isIncrease": true
  }
}
```

#### 6.9 전월 대비 전체 유형 청구서 비교 ⭐
- **Method**: `GET`
- **Path**: `/api/v1/bills/users/{userId}/compare-all`
- **Description**: 전기, 수도, 가스, 인터넷, 모바일 전체 유형의 전월 대비 증감률을 한 번에 조회합니다.
- **Path Variables**:
  - `userId`: 사용자 ID
- **Query Parameters**:
  - `currentMonth`: 비교 기준 월 (ISO date format, 예: 2024-01-01)
- **Response**: `200 OK`
```json
{
  "status": "SUCCESS",
  "data": {
    "comparisonMonth": "2024-01-01",
    "comparisons": [
      {
        "utilityType": "ELECTRICITY",
        "currentMonth": "2024-01-01",
        "previousMonth": "2023-12-01",
        "currentUsage": 350.5,
        "previousUsage": 300.0,
        "currentCharge": 45000.00,
        "previousCharge": 40000.00,
        "usageChangeRate": 16.83,
        "chargeChangeRate": 12.5,
        "usageChange": 50.5,
        "chargeChange": 5000.00,
        "isIncrease": true
      },
      {
        "utilityType": "WATER",
        "currentMonth": "2024-01-01",
        "previousMonth": "2023-12-01",
        "currentUsage": 15.5,
        "previousUsage": 18.0,
        "currentCharge": 25000.00,
        "previousCharge": 28000.00,
        "usageChangeRate": -13.89,
        "chargeChangeRate": -10.71,
        "usageChange": -2.5,
        "chargeChange": -3000.00,
        "isIncrease": false
      },
      {
        "utilityType": "GAS",
        "currentMonth": "2024-01-01",
        "previousMonth": "2023-12-01",
        "currentUsage": 80.0,
        "previousUsage": 75.0,
        "currentCharge": 60000.00,
        "previousCharge": 55000.00,
        "usageChangeRate": 6.67,
        "chargeChangeRate": 9.09,
        "usageChange": 5.0,
        "chargeChange": 5000.00,
        "isIncrease": true
      }
    ],
    "totalCurrentCharge": 130000.00,
    "totalPreviousCharge": 123000.00,
    "totalChargeChangeRate": 5.69,
    "totalChargeChange": 7000.00
  }
}
```

---

## 7. AI 챗봇 (AI Chat)

### Base URL: `/api/v1/chat`
### 인증 필요: Bearer Token

#### 7.1 메시지 전송
- **Method**: `POST`
- **Path**: `/api/v1/chat/users/{userId}/message`
- **Description**: AI 챗봇에게 메시지를 전송하고 응답을 받습니다. sessionId가 없으면 새 세션이 생성됩니다.
- **Request Body**:
```json
{
  "sessionId": 1,
  "message": "이번 달 전기 사용량이 많은 이유가 뭘까요?"
}
```
- **Response**:
```json
{
  "status": "SUCCESS",
  "data": {
    "messageId": 123,
    "sessionId": 1,
    "message": "이번 달 전기 사용량이 많은 이유가 뭘까요?",
    "response": "분석 결과, 에어컨 사용량이 평소보다 30% 증가했습니다...",
    "isUserMessage": true,
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

#### 7.2 세션 메시지 조회
- **Method**: `GET`
- **Path**: `/api/v1/chat/users/{userId}/sessions/{sessionId}/messages`
- **Description**: 특정 세션의 모든 메시지를 조회합니다.

#### 7.3 사용자 세션 목록 조회
- **Method**: `GET`
- **Path**: `/api/v1/chat/users/{userId}/sessions`
- **Description**: 사용자의 모든 활성 채팅 세션을 조회합니다.

#### 7.4 세션 삭제
- **Method**: `DELETE`
- **Path**: `/api/v1/chat/users/{userId}/sessions/{sessionId}`
- **Description**: 특정 채팅 세션을 비활성화합니다.

---

## 8. AI 절약 추천 (Recommendations)

### Base URL: `/api/v1/recommendations`
### 인증 필요: Bearer Token

#### 8.1 AI 추천 생성
- **Method**: `POST`
- **Path**: `/api/v1/recommendations/users/{userId}/generate`
- **Description**: 사용 패턴을 분석하여 맞춤형 절약 추천을 생성합니다.
- **Response**:
```json
{
  "status": "SUCCESS",
  "data": [
    {
      "recId": 1,
      "userId": 1,
      "utilityType": "ELECTRICITY",
      "recommendationType": "USAGE_REDUCTION",
      "title": "에어컨 사용 최적화",
      "description": "에어컨 설정 온도를 2도 올리면 월 15,000원 절약 가능합니다.",
      "potentialSavings": 15000,
      "priority": "HIGH",
      "isApplied": false,
      "createdAt": "2024-01-15T10:00:00"
    }
  ]
}
```

#### 8.2 사용자 추천 조회
- **Method**: `GET`
- **Path**: `/api/v1/recommendations/users/{userId}`
- **Description**: 사용자의 모든 추천을 조회합니다.

#### 8.3 미적용 추천 조회
- **Method**: `GET`
- **Path**: `/api/v1/recommendations/users/{userId}/unapplied`
- **Description**: 아직 적용하지 않은 추천을 조회합니다.

#### 8.4 추천 적용 처리
- **Method**: `PATCH`
- **Path**: `/api/v1/recommendations/{recId}/apply`
- **Description**: 추천을 적용 완료로 표시합니다.

---

## 9. 절감 효과 추적 (Savings Tracking)

### Base URL: `/api/v1/savings`
### 인증 필요: Bearer Token

#### 9.1 절감 추적 시작
- **Method**: `POST`
- **Path**: `/api/v1/savings/users/{userId}/recommendations/{recId}/start`
- **Description**: 특정 추천에 대한 절감 효과 추적을 시작합니다.

#### 9.2 절감 추적 업데이트
- **Method**: `PATCH`
- **Path**: `/api/v1/savings/{trackingId}/update`
- **Description**: 절감 효과를 현재 사용량 기준으로 업데이트합니다.

#### 9.3 사용자 절감 추적 조회
- **Method**: `GET`
- **Path**: `/api/v1/savings/users/{userId}`
- **Description**: 사용자의 모든 절감 추적 내역을 조회합니다.

#### 9.4 기간별 절감 추적 조회
- **Method**: `GET`
- **Path**: `/api/v1/savings/users/{userId}/period`
- **Description**: 특정 기간의 절감 추적 내역을 조회합니다.
- **Query Parameters**:
  - `startMonth`: 시작 월 (ISO date format)
  - `endMonth`: 종료 월 (ISO date format)

#### 9.5 총 절감액 조회
- **Method**: `GET`
- **Path**: `/api/v1/savings/users/{userId}/total`
- **Description**: 사용자의 총 절감액을 조회합니다.
- **Response**:
```json
{
  "status": "SUCCESS",
  "data": 125000
}
```

---

## 10. 사용 패턴 분석 (Usage Patterns)

### Base URL: `/api/v1/patterns`
### 인증 필요: Bearer Token

#### 10.1 사용 패턴 분석 및 생성
- **Method**: `POST`
- **Path**: `/api/v1/patterns/users/{userId}/analyze`
- **Description**: 사용자의 사용 데이터를 분석하여 패턴을 생성합니다.
- **Response**:
```json
{
  "status": "SUCCESS",
  "data": [
    {
      "patternId": 1,
      "userId": 1,
      "utilityType": "ELECTRICITY",
      "patternType": "PEAK_USAGE",
      "description": "평일 오후 6-9시 사이 전기 사용량이 평균 대비 40% 높습니다.",
      "averageUsage": 12.5,
      "peakUsage": 25.3,
      "analyzedMonth": "2024-01",
      "createdAt": "2024-01-15T10:00:00"
    }
  ]
}
```

#### 10.2 사용자 패턴 조회
- **Method**: `GET`
- **Path**: `/api/v1/patterns/users/{userId}`
- **Description**: 사용자의 모든 사용 패턴을 조회합니다.

#### 10.3 유틸리티 타입별 패턴 조회
- **Method**: `GET`
- **Path**: `/api/v1/patterns/users/{userId}/type/{utilityType}`
- **Description**: 특정 유틸리티 타입의 패턴을 조회합니다.

---

## 11. 뷰 페이지 (Views)

### Base URL: `/`

#### 11.1 메인 페이지
- **Method**: `GET`
- **Path**: `/`
- **Description**: 서버 정보가 포함된 메인 페이지를 반환합니다.

#### 11.2 테스트 페이지
- **Method**: `GET`
- **Path**: `/test`
- **Description**: 테스트 페이지를 반환합니다.

#### 11.3 서버 정보 조회
- **Method**: `GET`
- **Path**: `/api/server-info`
- **Description**: 서버 통계 정보 프래그먼트를 반환합니다.

---

## 공통 응답 형식

### 성공 응답
```json
{
  "status": "SUCCESS",
  "data": { /* 응답 데이터 */ },
  "message": null
}
```

### 에러 응답
```json
{
  "status": "ERROR",
  "data": null,
  "message": "에러 메시지"
}
```

---

## 인증 헤더

인증이 필요한 모든 API는 다음 헤더를 포함해야 합니다:

```
Authorization: Bearer {accessToken}
```

---

## 에러 코드

| HTTP Status | 설명 |
|-------------|------|
| 200 | 성공 |
| 201 | 생성 성공 |
| 400 | 잘못된 요청 |
| 401 | 인증 실패 |
| 403 | 권한 없음 |
| 404 | 리소스를 찾을 수 없음 |
| 500 | 서버 내부 오류 |

---

## 열거형 (Enums)

### UtilityType (사용량 유형)
- `ELECTRICITY`: 전기
- `WATER`: 수도
- `GAS`: 가스
- `INTERNET`: 인터넷
- `MOBILE`: 모바일

### AlertType (알림 유형)
- `BUDGET_WARNING`: 예산 경고 (임계값 도달)
- `BUDGET_EXCEEDED`: 예산 초과
- `UNUSUAL_USAGE`: 비정상적 사용 패턴
- `SAVING_OPPORTUNITY`: 절약 기회

### Provider (로그인 제공자)
- `LOCAL`: 로컬 계정
- `KAKAO`: 카카오 계정

---

## Swagger UI

API 문서는 Swagger UI를 통해서도 확인할 수 있습니다:
- URL: `http://localhost:8080/swagger-ui.html` (개발 환경)
- URL: `https://api.moadream.com/swagger-ui.html` (프로덕션 환경)

