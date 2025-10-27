# 모아드림 (Moadream) Server V1

> 실시간 전기·가스·수도 사용량 요금 확인 앱 백엔드 서버

2025 빛가람 에너지밸리 소프트웨어 작품 경진대회 출품작

## 프로젝트 개요

모아드림은 사회초년생과 1인 가구를 위한 공과금 관리 서비스입니다. 실시간으로 전기, 수도, 가스 사용량과 요금을 확인하고, AI 기반 패턴 분석을 통해 맞춤형 절약 가이드를 제공합니다.

### 주요 기능

- **사용자 관리**: 회원가입, 로그인, 프로필 관리
- **청구서 연동**: 공과금 청구서 번호 기반 사용자 인증
- **실시간 모니터링**: 전기, 수도, 가스 사용량 실시간 추적
- **사용 패턴 분석**: 일별/주별/월별/계절별 패턴 분석
- **AI 기반 추천**: 개인 맞춤형 절약 방안 제공
- **알림 시스템**: 비정상 사용량 및 예산 초과 경고
- **절감 효과 추적**: 실제 절약 성과 측정 및 피드백

## 기술 스택

### Backend
- **Java 17**
- **Spring Boot 3.4.11**
- **Spring Data JPA**
- **Spring Security**
- **H2 Database** (개발용)
- **MySQL** (프로덕션용)

### Build Tool
- **Gradle (Kotlin DSL)**

### Additional Libraries
- **Lombok**: 보일러플레이트 코드 제거
- **Spring Cloud OpenFeign**: 외부 API 연동
- **Validation API**: 입력 데이터 검증

## 시스템 아키텍처

```
┌─────────────────────────────────────────────────────┐
│          External Data Sources                      │
│  전력공단API │ 수도공단API │ 가스공단API │ 기타API  │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│           Spring Boot API Gateway                   │
│    (Feign Client / Scheduled Data Collection)       │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│              Backend Services Layer                 │
│  Database Service │ AI Analysis Service             │
│  (Python + Pandas │ (PyTorch)                       │
│  + Chart.js)                                        │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│            Notification Service                     │
│    (Email, Push Notifications via FCM)             │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│           Frontend Applications                     │
│  Mobile App (React Native / Flutter)                │
│  Web Dashboard (JavaScript + Chart.js)              │
└─────────────────────────────────────────────────────┘
```

## 데이터베이스 구조

### 주요 엔티티

#### 사용자 관리
- **User**: 사용자 기본 정보
- **UserBill**: 사용자 청구서 정보 (전기/수도/가스)
- **UserSetting**: 사용자 설정 (예산, 알림 등)

#### 사용량 데이터
- **UsageData**: 실시간 사용량 데이터
- **MonthlyBill**: 월별 청구서
- **UsageAlert**: 사용량 경고

#### 분석 및 추천
- **UsagePattern**: 사용 패턴 분석 결과
- **Recommendation**: AI 기반 절약 추천
- **SavingsTracking**: 절감 효과 추적

#### 개인정보 보호
- **PrivacyLog**: 개인정보 처리 로그

## 빌드 및 실행

### 사전 요구사항
- Java 17 이상
- Gradle 8.x

### 빌드

```bash
# Java 17로 환경 설정
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# 빌드 (테스트 제외)
./gradlew clean build -x test

# 빌드 (테스트 포함)
./gradlew clean build
```

### 실행

```bash
# Spring Boot 애플리케이션 실행
./gradlew bootRun

# 또는 JAR 파일 실행
java -jar build/libs/server.v1-0.0.1-SNAPSHOT.jar
```

### 개발 모드
```bash
# DevTools 활성화된 개발 모드
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## API 엔드포인트

### Swagger UI (API 문서)
```
URL: http://localhost:8080/swagger-ui.html
OpenAPI Spec: http://localhost:8080/v3/api-docs
```

### 인증 API
```
POST /api/v1/auth/signup         # 회원가입
POST /api/v1/auth/login          # 로그인
GET  /api/v1/auth/users/{userId} # 사용자 조회
```

### H2 콘솔 (개발용)
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (비어있음)
```

## 환경 설정

### application.yaml 주요 설정

```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:mem:testdb  # H2 인메모리 DB
  jpa:
    hibernate:
      ddl-auto: update       # 스키마 자동 업데이트
    show-sql: true           # SQL 로깅
server:
  port: 8080
```

## 개발 계획

### Phase 1: 기본 인프라 (완료)
- [x] 데이터베이스 스키마 설계
- [x] 엔티티 및 Repository 구현
- [x] Spring Security 설정
- [x] 기본 CRUD API 개발

### Phase 2: 외부 API 연동 (진행 예정)
- [ ] 전력/수도/가스 공단 API 연동
- [ ] 데이터 수집 스케줄러 구현
- [ ] 실시간 데이터 처리 파이프라인

### Phase 3: AI 분석 시스템 (진행 예정)
- [ ] Python 기반 데이터 분석 모듈
- [ ] PyTorch 기반 패턴 학습 모델
- [ ] 맞춤형 추천 알고리즘

### Phase 4: 시각화 및 알림 (진행 예정)
- [ ] Chart.js 기반 데이터 시각화
- [ ] 이메일/푸시 알림 시스템
- [ ] 대시보드 개발

## 팀 정보

- **팀명**: 세모이
- **팀장**: 김민솔 (디자이너, PM)
- **팀원**: 김태은 (서버, AI, 프론트)
- **소속**: 광주소프트웨어마이스터고등학교
- **대회**: 2025 빛가람 에너지밸리 소프트웨어 작품 경진대회

## 라이선스

Copyright © 2025 Team 세모이. All rights reserved.