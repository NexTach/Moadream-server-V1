# Moadream Server API 요약

## 📋 전체 API 개요

총 **10개 도메인**, **66개 엔드포인트**로 구성된 공과금 관리 및 AI 절약 추천 서비스

---

## 🔐 1. 인증 (Authentication) - 5개 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 |
| POST | `/api/v1/auth/refresh` | 토큰 재발급 |
| POST | `/api/v1/auth/kakao/login` | 카카오 로그인 |
| GET | `/api/v1/auth/users/{userId}` | 사용자 조회 |

---

## 👤 2. 사용자 설정 (User Settings) - 5개 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/settings/users/{userId}` | 설정 조회 |
| POST | `/api/v1/settings/users/{userId}` | 설정 생성 |
| PATCH | `/api/v1/settings/users/{userId}/budget` | 예산 설정 수정 |
| PATCH | `/api/v1/settings/users/{userId}/notifications` | 알림 설정 수정 |
| PUT | `/api/v1/settings/users/{userId}` | 설정 전체 수정 |

---

## 📊 3. 사용량 데이터 (Usage Data) - 6개 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/usage-data/users/{userId}` | 사용량 데이터 등록 |
| GET | `/api/v1/usage-data/users/{userId}` | 사용자 사용량 조회 |
| GET | `/api/v1/usage-data/users/{userId}/type/{utilityType}` | 유형별 사용량 조회 |
| GET | `/api/v1/usage-data/users/{userId}/range` | 기간별 사용량 조회 |
| GET | `/api/v1/usage-data/users/{userId}/latest/{utilityType}` | 최신 사용량 조회 |
| PUT | `/api/v1/usage-data/users/{userId}/{usageId}` | 사용량 데이터 수정 |

**지원 유형**: 전기(ELECTRICITY), 수도(WATER), 가스(GAS), 인터넷(INTERNET), 모바일(MOBILE)

---

## 🔔 4. 사용량 알림 (Usage Alerts) - 7개 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/alerts/users/{userId}` | 알림 생성 |
| GET | `/api/v1/alerts/users/{userId}` | 사용자 알림 조회 |
| GET | `/api/v1/alerts/users/{userId}/unread` | 미읽음 알림 조회 |
| GET | `/api/v1/alerts/users/{userId}/type/{utilityType}` | 유형별 알림 조회 |
| GET | `/api/v1/alerts/users/{userId}/alert-type/{alertType}` | 알림 타입별 조회 |
| PATCH | `/api/v1/alerts/{alertId}/read` | 알림 읽음 처리 |
| PATCH | `/api/v1/alerts/users/{userId}/read-all` | 모든 알림 읽음 처리 |

**알림 타입**: 예산 경고(BUDGET_WARNING), 예산 초과(BUDGET_EXCEEDED), 비정상 사용(UNUSUAL_USAGE), 절약 기회(SAVING_OPPORTUNITY)

---

## 💳 5. 월간 청구서 (Monthly Bills) - 7개 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/bills/users/{userId}` | 청구서 생성 |
| GET | `/api/v1/bills/users/{userId}` | 사용자 청구서 조회 |
| GET | `/api/v1/bills/users/{userId}/type/{utilityType}` | 유형별 청구서 조회 |
| GET | `/api/v1/bills/users/{userId}/month` | 월별 청구서 조회 |
| GET | `/api/v1/bills/users/{userId}/unpaid` | 미납 청구서 조회 |
| PATCH | `/api/v1/bills/{billId}/pay` | 청구서 납부 처리 |
| GET | `/api/v1/bills/users/{userId}/statistics` | 청구서 통계 조회 |

---

## 💬 6. AI 챗봇 (AI Chat) - 4개 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/chat/users/{userId}/message` | 메시지 전송 |
| GET | `/api/v1/chat/users/{userId}/sessions/{sessionId}/messages` | 세션 메시지 조회 |
| GET | `/api/v1/chat/users/{userId}/sessions` | 세션 목록 조회 |
| DELETE | `/api/v1/chat/users/{userId}/sessions/{sessionId}` | 세션 삭제 |

**기능**: 사용량 분석, 절약 팁 제공, 질의응답

---

## 💡 7. AI 절약 추천 (Recommendations) - 4개 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/recommendations/users/{userId}/generate` | AI 추천 생성 |
| GET | `/api/v1/recommendations/users/{userId}` | 사용자 추천 조회 |
| GET | `/api/v1/recommendations/users/{userId}/unapplied` | 미적용 추천 조회 |
| PATCH | `/api/v1/recommendations/{recId}/apply` | 추천 적용 처리 |

**추천 유형**: 사용량 감소, 요금제 변경, 사용 시간대 조정, 설비 개선

---

## 💰 8. 절감 효과 추적 (Savings Tracking) - 5개 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/savings/users/{userId}/recommendations/{recId}/start` | 절감 추적 시작 |
| PATCH | `/api/v1/savings/{trackingId}/update` | 절감 추적 업데이트 |
| GET | `/api/v1/savings/users/{userId}` | 절감 추적 조회 |
| GET | `/api/v1/savings/users/{userId}/period` | 기간별 절감 추적 |
| GET | `/api/v1/savings/users/{userId}/total` | 총 절감액 조회 |

---

## 📈 9. 사용 패턴 분석 (Usage Patterns) - 3개 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/patterns/users/{userId}/analyze` | 패턴 분석 및 생성 |
| GET | `/api/v1/patterns/users/{userId}` | 사용자 패턴 조회 |
| GET | `/api/v1/patterns/users/{userId}/type/{utilityType}` | 유형별 패턴 조회 |

**패턴 분석**: 피크 시간대, 평균 사용량, 이상 패턴 감지

---

## 🌐 10. 뷰 페이지 (Views) - 3개 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/` | 메인 페이지 |
| GET | `/test` | 테스트 페이지 |
| GET | `/api/server-info` | 서버 정보 조회 |

---

## 🔒 인증 방식

- **JWT Bearer Token** 사용
- Access Token 유효기간: 1시간
- Refresh Token으로 재발급 가능
- 카카오 OAuth 2.0 지원

---

## 📦 응답 형식

### 성공
```json
{
  "status": "SUCCESS",
  "data": { /* 데이터 */ }
}
```

### 실패
```json
{
  "status": "ERROR",
  "message": "에러 메시지"
}
```

---

## 🔄 주요 워크플로우

### 1️⃣ 회원가입 → 로그인
```
POST /api/v1/auth/signup → POST /api/v1/auth/login
```

### 2️⃣ 사용량 데이터 등록 → 패턴 분석 → AI 추천
```
POST /api/v1/usage-data/users/{userId}
  ↓
POST /api/v1/patterns/users/{userId}/analyze
  ↓
POST /api/v1/recommendations/users/{userId}/generate
```

### 3️⃣ 추천 적용 → 절감 추적
```
PATCH /api/v1/recommendations/{recId}/apply
  ↓
POST /api/v1/savings/users/{userId}/recommendations/{recId}/start
  ↓
PATCH /api/v1/savings/{trackingId}/update
```

### 4️⃣ AI 챗봇 상담
```
POST /api/v1/chat/users/{userId}/message
  ↓
GET /api/v1/chat/users/{userId}/sessions/{sessionId}/messages
```

---

## 📊 통계 요약

| 항목 | 수량 |
|------|------|
| 총 도메인 | 10개 |
| 총 엔드포인트 | 66개 |
| 인증 필요 API | 58개 |
| 공개 API | 8개 |
| 지원 공과금 유형 | 5개 |
| 알림 타입 | 4개 |

---

## 🛠 기술 스택

- **Framework**: Spring Boot 3.x
- **Database**: JPA/Hibernate
- **Authentication**: JWT, OAuth 2.0 (Kakao)
- **Documentation**: Swagger/OpenAPI 3.0
- **AI Integration**: External AI Service (GPT-based)

---

## 📝 참고 문서

- [상세 API 문서](./API_DOCUMENTATION.md)
- [프로젝트 목표](../PROJECT_TARGET.pdf)
- [AI 분석 플로우](./AI_ANALYSIS_FLOW.md)

---

## 🔗 Swagger UI

- 개발: http://localhost:8080/swagger-ui.html
- 프로덕션: https://api.moadream.com/swagger-ui.html

