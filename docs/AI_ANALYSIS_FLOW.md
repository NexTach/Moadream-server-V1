# AI 분석 시스템 플로우

## 개요

모아드림의 AI 분석 시스템은 사용자의 에너지 사용 데이터를 분석하여 패턴을 파악하고, 맞춤형 절약 추천을 제공하며, 절감 효과를 추적하는 3단계 시스템입니다.

## 전체 플로우

```
1. 사용량 데이터 수집
   ↓
2. 패턴 분석 (UsagePattern)
   ↓
3. AI 추천 생성 (Recommendation)
   ↓
4. 추천 적용
   ↓
5. 절감 효과 추적 (SavingsTracking)
```

---

## 1단계: 사용량 데이터 수집

### API
```http
POST /api/v1/usage-data/users/{userId}
```

### 데이터
- 유틸리티 타입 (전기/수도/가스)
- 사용량
- 요금
- 측정 시간

### 자동 처리
사용량 데이터가 추가될 때마다 **예산 임계값 자동 체크**가 실행됩니다.

```java
// UsageDataService.java:53
checkThresholdAndCreateAlert(user, request.getUtilityType(), request.getMeasuredAt());
```

- 월 예산 대비 사용량 계산
- 임계값 초과 시 자동 알림 생성
- AlertType: BUDGET_EXCEEDED

---

## 2단계: 패턴 분석

### API
```http
POST /api/v1/patterns/users/{userId}/analyze
```

### 분석 주기
- **DAILY**: 최근 7일
- **WEEKLY**: 최근 4주
- **MONTHLY**: 최근 3개월
- **SEASONAL**: 최근 1년

### 분석 지표
1. **평균 사용량** (averageUsage)
   - 기간 내 전체 사용량의 평균

2. **피크 사용량** (peakUsage)
   - 상위 20% 사용량의 평균

3. **오프피크 사용량** (offPeakUsage)
   - 하위 20% 사용량의 평균

4. **추세** (trend)
   - 전반부와 후반부 평균 비교
   - 10% 이상 증가 → "증가"
   - 10% 이상 감소 → "감소"
   - 그 외 → "안정"

### 조회 API
```http
GET /api/v1/patterns/users/{userId}
GET /api/v1/patterns/users/{userId}/type/{utilityType}
```

---

## 3단계: AI 추천 생성

### API
```http
POST /api/v1/recommendations/users/{userId}/generate
```

### 추천 생성 로직

#### 1) 증가 추세 감지
```java
if ("증가".equals(pattern.getTrend())) {
    // 사용량 절감 추천
    // 행동 변화 추천
}
```

**생성되는 추천:**
- **USAGE_REDUCTION**: 사용량 절감 방안 (예상 절감: 15%)
- **BEHAVIOR_CHANGE**: 행동 변화 제안 (예상 절감: 10%)

#### 2) 피크 사용량 감지
```java
if (peakUsage > averageUsage * 2) {
    // 시간대 이동 추천
}
```

**생성되는 추천:**
- **TIME_SHIFT**: 경부하 시간대 사용 (예상 절감: 20%)

#### 3) 전기 사용량 과다
```java
if (utilityType == ELECTRICITY && averageUsage > 300) {
    // 가전 업그레이드 추천
}
```

**생성되는 추천:**
- **APPLIANCE_UPGRADE**: 에너지 효율 가전 교체 (예상 절감: 25%)

#### 4) 기본 추천
모든 유틸리티에 대해 항상 생성:
- **TARIFF_OPTIMIZATION**: 요금제 최적화 (예상 절감: 8%)

### 추천 예시

#### 전기 - 증가 추세
```json
{
  "utilityType": "ELECTRICITY",
  "recType": "USAGE_REDUCTION",
  "recommendationText": "최근 전기 사용량이 증가하고 있습니다. 대기전력 차단과 불필요한 조명 끄기를 실천해보세요.",
  "expectedSavings": 15000,
  "implementationDifficulty": "보통"
}
```

#### 수도 - 행동 변화
```json
{
  "utilityType": "WATER",
  "recType": "BEHAVIOR_CHANGE",
  "recommendationText": "양치질이나 설거지 시 물을 받아서 사용하면 수도 사용량을 크게 줄일 수 있습니다.",
  "expectedSavings": 8000,
  "implementationDifficulty": "쉬움"
}
```

### 조회 API
```http
GET /api/v1/recommendations/users/{userId}
GET /api/v1/recommendations/users/{userId}/unapplied
```

---

## 4단계: 추천 적용

### API
```http
PATCH /api/v1/recommendations/{recId}/apply
```

### 처리
```java
recommendation.markAsApplied();
// isApplied: false → true
```

사용자가 추천을 확인하고 적용했음을 표시합니다.

---

## 5단계: 절감 효과 추적

### 추적 시작
```http
POST /api/v1/savings/users/{userId}/recommendations/{recId}/start
```

**처리:**
1. 지난 달 사용 비용을 **기준 비용(baselineCost)**으로 설정
2. 현재 월의 추적 데이터 생성
3. 초기값:
   - actualUsage: 0
   - actualCost: 0
   - savingsAchieved: 0

### 추적 업데이트
```http
PATCH /api/v1/savings/{trackingId}/update
```

**처리:**
1. 현재 월의 실제 사용량 조회
2. 실제 비용 계산
3. **절감액 계산**: `baselineCost - actualCost`
4. 데이터 업데이트

### 예시

#### 추적 시작 (10월)
```json
{
  "trackingMonth": "2025-10-01",
  "baselineCost": 50000,  // 9월 비용
  "actualCost": 0,
  "savingsAchieved": 0
}
```

#### 추적 업데이트 (10월 말)
```json
{
  "trackingMonth": "2025-10-01",
  "baselineCost": 50000,
  "actualCost": 42000,
  "savingsAchieved": 8000  // 8천원 절감!
}
```

### 통계 조회
```http
GET /api/v1/savings/users/{userId}
GET /api/v1/savings/users/{userId}/period?startMonth=2025-01-01&endMonth=2025-12-31
GET /api/v1/savings/users/{userId}/total
```

---

## 자동화 시스템

### 1. 월별 청구서 자동 생성
```java
@Scheduled(cron = "0 0 0 1 * *")  // 매월 1일 00:00
public void generateMonthlyBills()
```

**처리:**
- 전월 사용량 데이터 집계
- 유틸리티 타입별 청구서 생성
- 납부 기한: 익월 15일

### 2. 예산 임계값 자동 알림
```java
// UsageData 추가 시 자동 실행
checkThresholdAndCreateAlert()
```

**처리:**
- 월 누적 사용 요금 계산
- 사용자 설정 임계값과 비교
- 초과 시 알림 자동 생성

---

## 데이터베이스 관계

```
User (사용자)
 ├─ UsageData (사용량 데이터) [1:N]
 ├─ UsagePattern (패턴 분석) [1:N]
 ├─ Recommendation (추천) [1:N]
 ├─ SavingsTracking (절감 추적) [1:N]
 └─ MonthlyBill (월별 청구서) [1:N]

Recommendation (추천)
 └─ SavingsTracking (절감 추적) [1:N]
```

---

## 전체 사용 시나리오

### 시나리오: 신규 사용자의 첫 달

#### Week 1: 데이터 수집
```http
POST /api/v1/usage-data/users/1
{
  "utilityType": "ELECTRICITY",
  "usageAmount": 150,
  "currentCharge": 15000,
  "measuredAt": "2025-10-01T10:00:00"
}
```

- 일주일 동안 매일 데이터 수집
- 예산 임계값 자동 체크

#### Week 2: 패턴 분석
```http
POST /api/v1/patterns/users/1/analyze
```

**결과:**
- DAILY 패턴: 평균 150kWh, 피크 200kWh
- 추세: "안정"

#### Week 3: AI 추천 생성
```http
POST /api/v1/recommendations/users/1/generate
```

**생성된 추천:**
1. 요금제 최적화 (예상 절감: 8%)
2. 시간대 이동 (예상 절감: 20%)

#### Week 4: 추천 적용 및 추적 시작
```http
# 추천 적용
PATCH /api/v1/recommendations/5/apply

# 절감 추적 시작
POST /api/v1/savings/users/1/recommendations/5/start
```

#### Next Month: 효과 확인
```http
# 절감 추적 업데이트
PATCH /api/v1/savings/1/update

# 결과 확인
GET /api/v1/savings/users/1/total
```

**결과:**
- 기준 비용: 45,000원
- 실제 비용: 38,000원
- 절감액: 7,000원 (15.6% 절감)

---

## 향후 확장 계획

### Phase 3.5: 실제 AI 모델 적용
현재는 룰 기반 추천 엔진이지만, 향후 PyTorch 기반 머신러닝 모델로 전환 예정:

1. **데이터 수집 및 전처리**
   - 사용 패턴 데이터 축적
   - Python/Pandas로 전처리

2. **모델 학습**
   - PyTorch 기반 시계열 예측 모델
   - 사용자별 맞춤형 추천 모델

3. **추천 엔진 통합**
   - Spring Boot ↔ Python 연동
   - REST API 또는 gRPC 통신

4. **지속적 학습**
   - 사용자 피드백 수집
   - 모델 재학습 및 개선

---

## API 요약

| 기능 | Method | Endpoint |
|------|--------|----------|
| 패턴 분석 실행 | POST | `/api/v1/patterns/users/{userId}/analyze` |
| 패턴 조회 | GET | `/api/v1/patterns/users/{userId}` |
| 추천 생성 | POST | `/api/v1/recommendations/users/{userId}/generate` |
| 미적용 추천 조회 | GET | `/api/v1/recommendations/users/{userId}/unapplied` |
| 추천 적용 | PATCH | `/api/v1/recommendations/{recId}/apply` |
| 절감 추적 시작 | POST | `/api/v1/savings/users/{userId}/recommendations/{recId}/start` |
| 절감 추적 업데이트 | PATCH | `/api/v1/savings/{trackingId}/update` |
| 총 절감액 조회 | GET | `/api/v1/savings/users/{userId}/total` |

---

## 참고 문서

- [README.md](../README.md) - 프로젝트 전체 개요
- [PROJECT_TARGET.pdf](../PROJECT_TARGET.pdf) - 작품 소개서
- [Database Schema](../README.md#데이터베이스-구조) - DB 구조