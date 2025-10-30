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

## 라이선스

Copyright © 2025 Team 세모이. All rights reserved.