# 메모리 최적화 및 성능 개선 가이드

## 적용된 최적화 항목

### 1. JPA/Hibernate 최적화
- ✅ **2차 캐시 (Ehcache)**: 자주 조회되는 엔티티 캐싱
- ✅ **배치 처리**: `batch_size=50`, `fetch_size=100`
- ✅ **쿼리 최적화**: `order_inserts`, `order_updates`, `in_clause_parameter_padding`
- ✅ **배치 페치**: `@BatchSize(size=10)` - N+1 문제 해결
- ✅ **인덱스 추가**: 자주 조회되는 컬럼에 복합 인덱스 적용

### 2. 커넥션 풀 최적화 (HikariCP)
```yaml
hikari:
  maximum-pool-size: 10
  minimum-idle: 5
  connection-timeout: 30000
  idle-timeout: 600000
  max-lifetime: 1800000
  auto-commit: false
  leak-detection-threshold: 60000
```

### 3. Tomcat 서버 최적화
```yaml
tomcat:
  threads:
    max: 200
    min-spare: 10
  max-connections: 10000
  compression:
    enabled: true  # 응답 압축
```

### 4. Gradle 빌드 최적화
- ✅ 병렬 빌드 활성화
- ✅ 빌드 캐싱
- ✅ Configuration cache
- ✅ JVM 힙 메모리 최적화 (2GB)

### 5. 모니터링 추가
- ✅ Spring Boot Actuator
- ✅ Micrometer + Prometheus
- ✅ `/actuator/prometheus` 엔드포인트
- ✅ `/actuator/metrics` 엔드포인트

## 모니터링 사용법

### 메모리 사용량 확인
```bash
# 전체 메트릭
curl http://localhost:8080/actuator/metrics

# JVM 메모리
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/jvm.memory.max

# GC 통계
curl http://localhost:8080/actuator/metrics/jvm.gc.pause

# Hikari 커넥션 풀
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
curl http://localhost:8080/actuator/metrics/hikaricp.connections.idle
```

### Prometheus 연동
```bash
# Prometheus 메트릭 수집
curl http://localhost:8080/actuator/prometheus
```

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

## 성능 테스트 권장사항

### 1. JMeter/Gatling으로 부하 테스트
- 동시 사용자 수 증가시키며 메모리 사용량 모니터링
- 메모리 누수 확인

### 2. JVM 프로파일링
```bash
# 힙 덤프 생성
jmap -dump:format=b,file=heap.bin <PID>

# GC 로그 활성화 (application.yaml 또는 JVM 옵션)
-Xlog:gc*:file=gc.log:time,uptime:filecount=10,filesize=10M
```

### 3. 실시간 메모리 모니터링
```bash
# JConsole 연결
jconsole <PID>

# VisualVM 사용
visualvm
```

## 예상 성능 개선

### 메모리
- **2차 캐시**: 반복 조회 시 DB 부하 60-80% 감소
- **배치 페치**: N+1 쿼리 제거로 메모리 사용량 30-50% 감소
- **커넥션 풀 최적화**: 불필요한 커넥션 생성 방지

### 응답 시간
- **인덱스**: 검색 쿼리 속도 50-70% 개선
- **압축**: 네트워크 전송량 40-60% 감소
- **배치 처리**: 대량 INSERT/UPDATE 속도 3-5배 개선

### 처리량
- **Tomcat 스레드 풀**: 동시 요청 처리 능력 향상
- **비동기 처리**: 블로킹 작업 최소화

## 추가 최적화 고려사항

### 1. Redis 캐시 추가 (선택)
```gradle
implementation("org.springframework.boot:spring-boot-starter-data-redis")
```

### 2. 비동기 처리
```java
@Async
@EnableAsync
```

### 3. 쿼리 DSL 도입
```gradle
implementation("com.querydsl:querydsl-jpa")
```

### 4. 로깅 최적화
- 프로덕션에서는 `show-sql: false`
- Logback async appender 사용

## 주의사항

1. **2차 캐시**: 데이터 정합성 중요한 경우 주의
2. **배치 사이즈**: 너무 크면 메모리 부족 가능
3. **커넥션 풀**: 환경에 맞게 튜닝 필요
4. **모니터링**: 프로덕션에서 actuator 엔드포인트 보안 설정 필수

## 배포 전 체크리스트

- [ ] 로컬에서 성능 테스트 완료
- [ ] 메모리 누수 확인
- [ ] GC 로그 분석
- [ ] Actuator 보안 설정
- [ ] 프로덕션 설정으로 `show-sql: false`
- [ ] 인덱스 적용 확인
- [ ] 캐시 적중률 모니터링

