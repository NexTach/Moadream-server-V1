# MoaDream Server v1 프로젝트

## 프로젝트 개요
MoaDream은 전기/가스/수도 사용량을 관리하고 AI 기반 절약 추천을 제공하는 에너지 관리 플랫폼입니다.

## 기술 스택
- **Backend**: Spring Boot 3.5.1, Java 17
- **Database**: H2 (dev), PostgreSQL (prod)
- **ORM**: Spring Data JPA, Hibernate
- **Security**: Spring Security, JWT
- **Template Engine**: Thymeleaf
- **API Documentation**: Swagger/OpenAPI
- **Build Tool**: Gradle

## 프로젝트 구조
```
src/main/java/com/nextech/moadream/server/v1/
├── domain/                 # 도메인별 패키지
│   ├── user/              # 사용자 관리
│   │   ├── entity/        # User, UserSetting, UserBill
│   │   ├── repository/
│   │   ├── service/
│   │   └── controller/
│   ├── usage/             # 사용량 관리
│   │   ├── entity/        # UsageData, ElectricityBill, GasBill, WaterBill, MonthlyBill, UsageAlert
│   │   ├── repository/
│   │   ├── service/
│   │   └── controller/
│   ├── chat/              # AI 채팅
│   │   ├── entity/        # ChatSession, ChatMessage
│   │   ├── repository/
│   │   ├── service/
│   │   └── controller/
│   ├── analysis/          # 사용 패턴 분석
│   │   ├── entity/        # UsagePattern, Recommendation, SavingsTracking
│   │   ├── repository/
│   │   ├── service/
│   │   └── controller/
│   └── privacy/           # 개인정보 관리
│       ├── entity/        # PrivacyLog
│       ├── repository/
│       ├── service/
│       └── controller/
├── global/                # 전역 설정 및 공통 기능
│   ├── config/           # 설정 클래스
│   ├── security/         # 보안 설정
│   ├── exception/        # 예외 처리
│   ├── dto/              # 공통 DTO
│   └── util/             # 유틸리티
└── V1ServerApplication.java
```

## 데이터베이스 스키마

### 주요 엔티티
1. **User** - 사용자 정보 (이메일, 비밀번호, 이름, 전화번호, 주소, 생년월일)
2. **UserSetting** - 사용자 설정 (월 예산, 알림 임계값, 효율성 점수)
3. **UserBill** - 등록된 고지서 (전기/가스/수도)
4. **UsageData** - 실시간 사용량 데이터
5. **ElectricityBill/GasBill/WaterBill** - 월별 요금 고지서
6. **MonthlyBill** - 월별 통합 요금 정보
7. **UsageAlert** - 사용량 알림
8. **ChatSession/ChatMessage** - AI 채팅 세션 및 메시지
9. **UsagePattern** - 사용 패턴 분석 결과
10. **Recommendation** - AI 추천사항
11. **SavingsTracking** - 절약 추적
12. **PrivacyLog** - 개인정보 처리 로그

### 주요 Enum
- **UtilityType**: ELECTRICITY, GAS, WATER
- **AlertType**: HIGH_USAGE, BUDGET_EXCEEDED, UNUSUAL_PATTERN, POSITIVE_FEEDBACK
- **MessageRole**: USER, ASSISTANT, SYSTEM
- **FrequencyType**: DAILY, WEEKLY, MONTHLY, SEASONAL
- **RecommendationType**: USAGE_REDUCTION, TIME_SHIFT, APPLIANCE_UPGRADE, BEHAVIOR_CHANGE, TARIFF_OPTIMIZATION
- **ActionType**: DATA_COLLECTED, DATA_PROCESSED, DATA_DELETED

## 코딩 컨벤션

### 네이밍
- 클래스명: PascalCase (예: `UserService`, `ElectricityBillRepository`)
- 메서드명: camelCase (예: `getUserById`, `calculateTotalCharge`)
- 변수명: camelCase (예: `totalUsage`, `billingMonth`)
- 상수명: UPPER_SNAKE_CASE (예: `MAX_RETRY_COUNT`)
- 패키지명: lowercase (예: `com.nextech.moadream.server.v1`)

### 엔티티 설계
- `@Builder` 패턴 사용
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 적용
- `@AllArgsConstructor(access = AccessLevel.PRIVATE)` 적용
- 생성/수정 시간은 `@CreatedDate`, `@LastModifiedDate` 사용
- ID는 `@GeneratedValue(strategy = GenerationType.IDENTITY)` 사용

### 서비스 레이어
- `@Service` + `@RequiredArgsConstructor` 조합
- 트랜잭션은 `@Transactional` 사용
- 비즈니스 로직은 서비스 레이어에 집중

### API 응답
- 성공: `ApiResponse<T>` 사용
- 에러: 커스텀 예외 + `@RestControllerAdvice`로 처리

### 로깅
- Lombok `@Slf4j` 사용
- 중요 작업은 INFO 레벨로 로깅
- 에러는 ERROR 레벨로 스택 트레이스 포함

## Mock Data
- `/src/main/resources/mock-data/users.json`에 테스트 데이터 정의
- `JsonMockDataLoader`가 애플리케이션 시작 시 자동으로 로드
- 2명의 테스트 사용자 데이터 포함 (이주언, 김민수)

## 개발 페이지
- `/` - 서버 대시보드 (실시간 서버 상태)
- `/test` - API 테스트 콘솔
- `/data` - 데이터베이스 뷰어
- `/swagger-ui/index.html` - Swagger API 문서
- `/h2-console` - H2 데이터베이스 콘솔

## 주의사항
- 민감한 정보는 `.env` 파일로 관리 (git 제외)
- API 호출 시 JWT 토큰 인증 필요
- 프로덕션 환경에서는 반드시 PostgreSQL 사용
- 개인정보 처리 시 PrivacyLog 생성 필수