# 전월 대비 증감률 API 사용 가이드

## 📊 개요

전기, 수도, 가스 등 각 공과금의 전월 대비 증감률을 조회할 수 있는 API입니다.

## 🎯 사용 사례

### 1️⃣ 특정 유형(전기만) 전월 대비 비교

**엔드포인트**: `GET /api/v1/bills/users/{userId}/compare/{utilityType}`

#### 예시: 1월 전기 요금 전월 대비 조회

```bash
GET /api/v1/bills/users/1/compare/ELECTRICITY?currentMonth=2024-01-01
Authorization: Bearer {your-token}
```

#### 응답 예시
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
    "usageChangeRate": 16.83,      // 사용량 16.83% 증가
    "chargeChangeRate": 12.5,       // 요금 12.5% 증가
    "usageChange": 50.5,            // 50.5kWh 증가
    "chargeChange": 5000.00,        // 5,000원 증가
    "isIncrease": true              // 증가 여부
  }
}
```

#### 해석
- 전기 사용량: 300.0 → 350.5 (16.83% ↑)
- 전기 요금: 40,000원 → 45,000원 (12.5% ↑)

---

### 2️⃣ 전체 유형 한번에 비교 (추천!)

**엔드포인트**: `GET /api/v1/bills/users/{userId}/compare-all`

#### 예시: 1월 전체 공과금 전월 대비 조회

```bash
GET /api/v1/bills/users/1/compare-all?currentMonth=2024-01-01
Authorization: Bearer {your-token}
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "data": {
    "comparisonMonth": "2024-01-01",
    "comparisons": [
      {
        "utilityType": "ELECTRICITY",
        "currentCharge": 45000.00,
        "previousCharge": 40000.00,
        "chargeChangeRate": 12.5,
        "isIncrease": true
      },
      {
        "utilityType": "WATER",
        "currentCharge": 25000.00,
        "previousCharge": 28000.00,
        "chargeChangeRate": -10.71,
        "isIncrease": false
      },
      {
        "utilityType": "GAS",
        "currentCharge": 60000.00,
        "previousCharge": 55000.00,
        "chargeChangeRate": 9.09,
        "isIncrease": true
      }
    ],
    "totalCurrentCharge": 130000.00,    // 전체 합계
    "totalPreviousCharge": 123000.00,
    "totalChargeChangeRate": 5.69,      // 전체 증감률
    "totalChargeChange": 7000.00
  }
}
```

#### 해석
- 전기: 12.5% 증가 ↑
- 수도: 10.71% 감소 ↓
- 가스: 9.09% 증가 ↑
- **전체: 5.69% 증가 (7,000원 ↑)**

---

## 💡 프론트엔드 활용 예시

### React/Vue/Angular 예시

```typescript
// 전체 비교 조회
async function fetchMonthlyComparison(userId: number, month: string) {
  const response = await fetch(
    `/api/v1/bills/users/${userId}/compare-all?currentMonth=${month}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  
  const result = await response.json();
  
  // 각 유형별 증감률 표시
  result.data.comparisons.forEach(comparison => {
    console.log(`${comparison.utilityType}: ${comparison.chargeChangeRate}%`);
  });
  
  // 전체 증감률
  console.log(`전체: ${result.data.totalChargeChangeRate}%`);
}
```

### UI 표시 예시

```
📊 1월 공과금 분석

전기 💡
  ├─ 이번 달: 45,000원
  ├─ 지난 달: 40,000원
  └─ 증감: +5,000원 (↑ 12.5%)

수도 💧
  ├─ 이번 달: 25,000원
  ├─ 지난 달: 28,000원
  └─ 증감: -3,000원 (↓ 10.71%)

가스 🔥
  ├─ 이번 달: 60,000원
  ├─ 지난 달: 55,000원
  └─ 증감: +5,000원 (↑ 9.09%)

━━━━━━━━━━━━━━━━━━━━━━━━━
전체 합계: 130,000원
전월 대비: +7,000원 (↑ 5.69%)
```

---

## 🔑 주요 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| `utilityType` | Enum | 사용량 유형 (ELECTRICITY, WATER, GAS) |
| `currentCharge` | BigDecimal | 현재 월 요금 |
| `previousCharge` | BigDecimal | 이전 월 요금 |
| `chargeChangeRate` | BigDecimal | 요금 증감률 (%) |
| `chargeChange` | BigDecimal | 요금 증감액 (원) |
| `usageChangeRate` | BigDecimal | 사용량 증감률 (%) |
| `isIncrease` | Boolean | 증가 여부 (true: 증가, false: 감소) |

---

## ⚠️ 주의사항

1. **데이터 필요 조건**: 비교하려는 현재 월과 이전 월의 청구서가 모두 존재해야 합니다.
2. **날짜 형식**: ISO 8601 형식 사용 (예: `2024-01-01`)
3. **인증 필수**: Bearer Token 필요
4. **에러 처리**:
   - 청구서가 없는 경우: `BILL_NOT_FOUND` (404)
   - 사용자가 없는 경우: `USER_NOT_FOUND` (404)

---

## 📱 실제 사용 시나리오

### 시나리오 1: 대시보드에 월별 증감률 표시
```
사용자가 앱을 열면 → 
현재 월 기준으로 compare-all API 호출 → 
각 공과금별 증감률을 그래프/차트로 표시
```

### 시나리오 2: 알림 발송
```
매월 1일 자동 실행 → 
compare-all API로 전월 대비 계산 → 
10% 이상 증가한 항목에 대해 푸시 알림 발송
```

### 시나리오 3: AI 챗봇 연동
```
사용자: "이번 달 전기세가 왜 올랐나요?"
  ↓
챗봇이 compare/ELECTRICITY API 호출
  ↓
"전월 대비 16.83% 증가했습니다. 
 사용량이 50.5kWh 늘어났어요."
```

---

## 🔗 관련 API

- [사용량 데이터 등록](/docs/API_DOCUMENTATION.md#41-사용량-데이터-등록)
- [월간 청구서 생성](/docs/API_DOCUMENTATION.md#61-청구서-생성)
- [청구서 통계 조회](/docs/API_DOCUMENTATION.md#67-청구서-통계-조회)
- [AI 챗봇](/docs/API_DOCUMENTATION.md#7-ai-챗봇-ai-chat)

---

## 📞 문의

기술 지원이 필요하신 경우:
- GitHub Issues: [프로젝트 이슈](https://github.com/your-repo/issues)
- Email: support@moadream.com

