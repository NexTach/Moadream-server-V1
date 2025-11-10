# AI 통합 가이드

## 개요

이 프로젝트는 Spring AI와 Ollama를 활용하여 AI 기반 에너지 절약 추천 시스템을 제공합니다.

## 시스템 구조

```
┌─────────────────────────────────────────────────┐
│                Spring Boot App                  │
│  ┌──────────────────────────────────────────┐   │
│  │  RecommendationService                   │   │
│  │  ├─ AI 추천 (우선)                       │   │
│  │  └─ 룰 기반 추천 (폴백)                  │   │
│  └──────────────────────────────────────────┘   │
│                      ↓                           │
│  ┌──────────────────────────────────────────┐   │
│  │  AIRecommendationService                 │   │
│  │  (Spring AI ChatClient)                  │   │
│  └──────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
                      ↓
         HTTP (http://ollama:11434)
                      ↓
┌─────────────────────────────────────────────────┐
│              Ollama Container                   │
│  ┌──────────────────────────────────────────┐   │
│  │  모델: GPT-OSS-20B (20GB+ RAM)          │   │
│  │  또는  Gemma 2:2B  (<20GB RAM)          │   │
│  └──────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
```

## 주요 기능

### 1. 리소스 기반 자동 모델 선택

docker-compose의 `ollama-init` 서비스가 시스템 메모리를 감지하여 자동으로 모델을 선택합니다:

- **20GB 이상 RAM**: `gpt-oss:20b` (21B 파라미터, 고성능)
- **20GB 미만 RAM**: `gemma2:2b` (2B 파라미터, 경량화)

### 2. AI 기반 추천 생성

`AIRecommendationService`는 사용자의 에너지 사용 패턴을 분석하여 맞춤형 절약 방안을 생성합니다:

- 사용 패턴 데이터를 프롬프트로 변환
- Ollama를 통해 AI 추천 생성
- 자연어 응답을 구조화된 데이터로 파싱
- 실패 시 룰 기반 추천으로 폴백

### 3. Graceful Degradation

AI가 비활성화되거나 실패하면 기존 룰 기반 추천 시스템으로 자동 전환됩니다.

## 설정

### application-deploy.yaml

```yaml
spring.ai:
  ollama:
    base-url: ${SPRING_AI_OLLAMA_BASE_URL:http://localhost:11434}
    chat:
      options:
        model: ${SPRING_AI_OLLAMA_CHAT_MODEL:gemma2:2b}
        temperature: 0.7
        num-predict: 1024
      enabled: true
```

### 환경 변수

- `SPRING_AI_OLLAMA_BASE_URL`: Ollama 서버 URL (기본값: http://localhost:11434)
- `SPRING_AI_OLLAMA_CHAT_MODEL`: 사용할 모델 이름 (기본값: gemma2:2b)

## 배포

### docker-compose로 배포

```bash
# 전체 스택 실행 (MySQL, Ollama, App)
docker-compose up -d

# 로그 확인
docker-compose logs -f ollama
docker-compose logs -f app

# 중지
docker-compose down
```

### 모델 초기화 확인

```bash
# Ollama 모델 목록 확인
docker exec -it moadream-ollama ollama list

# 모델 직접 다운로드 (필요 시)
docker exec -it moadream-ollama ollama pull gemma2:2b
```

## 성능 최적화

### GPU 활용 (선택 사항)

NVIDIA GPU가 있는 경우 docker-compose.yml에서 주석 해제:

```yaml
ollama:
  deploy:
    resources:
      reservations:
        devices:
          - driver: nvidia
            count: 1
            capabilities: [gpu]
```

### 모델별 메모리 요구사항

| 모델 | 파라미터 | 메모리 요구량 | 추천 RAM |
|------|---------|-------------|---------|
| gemma2:2b | 2B | ~4GB | 8GB+ |
| gpt-oss:20b | 21B | ~16GB | 24GB+ |

## API 사용 예시

### 1. 패턴 분석

```bash
POST /api/v1/patterns/users/1/analyze
```

### 2. AI 추천 생성

```bash
POST /api/v1/recommendations/users/1/generate
```

응답 예시:
```json
[
  {
    "recId": 1,
    "utilityType": "ELECTRICITY",
    "recType": "USAGE_REDUCTION",
    "recommendationText": "대기전력 차단으로 전기 절약. 사용하지 않는 가전제품의 플러그를 뽑거나 멀티탭을 사용하여 대기전력을 차단하세요.",
    "expectedSavings": 15000,
    "implementationDifficulty": "쉬움",
    "isApplied": false,
    "createdAt": "2025-01-15T10:00:00"
  }
]
```

## 트러블슈팅

### 1. Ollama 연결 실패

```bash
# Ollama 상태 확인
docker ps | grep ollama

# Ollama 로그 확인
docker logs moadream-ollama

# 네트워크 확인
docker exec -it moadream-app curl http://ollama:11434/api/tags
```

### 2. 모델 다운로드 실패

```bash
# 수동으로 모델 다운로드
docker exec -it moadream-ollama ollama pull gemma2:2b

# 디스크 공간 확인
df -h
```

### 3. AI 추천 비활성화

일시적으로 AI를 비활성화하려면:

```yaml
# application-deploy.yaml
spring.ai:
  ollama:
    chat:
      enabled: false  # AI 비활성화, 룰 기반만 사용
```

## 확장 가능성

### Phase 2: 추가 AI 기능

1. **이상 탐지**: 급격한 사용량 변화 감지 및 알림
2. **사용량 예측**: 다음 달 예상 사용량 및 요금 예측
3. **질의응답**: "왜 이번 달 전기세가 올랐나요?" 같은 자연어 질문 처리

### Phase 3: 고급 모델 통합

- GPT-4 등 외부 API 통합
- Fine-tuning: 실제 사용자 데이터로 모델 재학습
- Multi-modal: 이미지 기반 절약 팁 (예: 에너지 효율 라벨 분석)

## 참고 문서

- [Spring AI 공식 문서](https://docs.spring.io/spring-ai/reference/)
- [Ollama 모델 라이브러리](https://ollama.com/library)
- [GPT-OSS-20B 모델 카드](https://huggingface.co/openai/gpt-oss-20b)