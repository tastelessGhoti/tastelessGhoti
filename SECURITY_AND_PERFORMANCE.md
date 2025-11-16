# 보안 및 성능 최적화 가이드

전자상거래 API 시스템의 보안 강화 및 성능 최적화 내역 정리

---

## 보안 강화

### 인증 및 권한

**JWT 토큰 보안**
- 시크릿 키를 환경변수로 관리 (하드코딩 제거)
- HS512 알고리즘 사용
- 토큰 만료 시간: Access 1시간, Refresh 7일
- 토큰 검증 강화 (만료, 서명, 형식)

**비밀번호**
- BCrypt strength를 12로 상향 (기본 10보다 강력)
- 비밀번호 정책: 최소 8자, 영문+숫자+특수문자 필수

```java
@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$")
private String password;
```

### CORS 설정

기존에는 모든 origin을 허용했는데, 환경변수로 관리하도록 변경했습니다.

```java
// 환경변수로 허용 도메인 관리
String allowedOrigins = System.getenv("ALLOWED_ORIGINS");
if (allowedOrigins != null) {
    configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
} else {
    // 개발 환경에서만 localhost 허용
    configuration.setAllowedOriginPatterns(Arrays.asList(
        "http://localhost:*",
        "http://127.0.0.1:*"
    ));
}
```

운영에서는:
```bash
export ALLOWED_ORIGINS="https://your-domain.com,https://admin.your-domain.com"
```

### Rate Limiting

Bucket4j를 사용해서 API 호출 횟수 제한을 구현했습니다.

- 일반 API: 분당 100회, 초당 20회
- 로그인 API: 분당 5회 (브루트포스 공격 방지)

초과 시 429 응답과 함께 재시도 가능 시간을 헤더로 전달합니다.

```
X-Rate-Limit-Remaining: 95
X-Rate-Limit-Retry-After-Seconds: 45
```

### 보안 헤더

```java
// Clickjacking, XSS, MIME 스니핑 방지
.addHeaderWriter(new XFrameOptionsHeaderWriter(SAMEORIGIN))
.xssProtection(xss -> xss.headerValue("1; mode=block"))
.contentTypeOptions(options -> options.disable())
```

### 환경별 설정

개발/운영 환경을 분리했습니다.

**개발 (application-dev.yml)**
- H2 Console 활성화
- SQL 로그 출력
- DEBUG 레벨 로깅

**운영 (application-prod.yml)**
- H2 Console 비활성화
- SQL 로그 최소화
- INFO 레벨 로깅
- 파일 로그 저장 (30일 보관)

### 민감 정보 관리

환경변수로 관리:
- JWT_SECRET
- DB_PASSWORD
- REDIS_PASSWORD
- ALLOWED_ORIGINS

---

## 성능 최적화

### 데이터베이스 인덱스

조회 성능 향상을 위해 자주 사용되는 컬럼에 인덱스를 추가했습니다.

**User**
```java
@Index(name = "idx_user_email", columnList = "email", unique = true)
@Index(name = "idx_user_status", columnList = "status")
@Index(name = "idx_user_created_at", columnList = "createdAt")
```

**Product**
```java
@Index(name = "idx_product_category", columnList = "category")
@Index(name = "idx_product_status", columnList = "status")
@Index(name = "idx_product_category_status", columnList = "category, status")  // 복합 인덱스
@Index(name = "idx_product_name", columnList = "name")
```

**Order**
```java
@Index(name = "idx_order_user_id", columnList = "user_id")
@Index(name = "idx_order_status", columnList = "status")
@Index(name = "idx_order_user_status", columnList = "user_id, status")  // 복합 인덱스
```

### N+1 문제 해결

```yaml
# application.yml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100
```

주문 조회 시 Fetch Join 사용:
```java
@Query("SELECT DISTINCT o FROM Order o " +
       "LEFT JOIN FETCH o.orderItems oi " +
       "LEFT JOIN FETCH oi.product " +
       "WHERE o.id = :orderId")
Order findByIdWithItems(@Param("orderId") Long orderId);
```

이렇게 하면 N+1 쿼리가 1개의 쿼리로 줄어듭니다.

### 캐싱 전략

Redis 기반으로 도메인별 TTL을 다르게 설정했습니다.

```java
// 상품: 30분 (자주 안 바뀜)
cacheConfigurations.put("products",
    defaultConfig.entryTtl(Duration.ofMinutes(30)));

// 사용자: 5분 (실시간성 중요)
cacheConfigurations.put("users",
    defaultConfig.entryTtl(Duration.ofMinutes(5)));

// 주문: 3분 (실시간성 매우 중요)
cacheConfigurations.put("orders",
    defaultConfig.entryTtl(Duration.ofMinutes(3)));
```

사용 예시:
```java
@Cacheable(value = "products", key = "#productId")
public ProductResponse getProduct(Long productId) { ... }

@CacheEvict(value = "products", key = "#productId")
public ProductResponse updateProduct(Long productId, ...) { ... }
```

### 커넥션 풀 (HikariCP)

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
```

개발 환경에서는 10개, 운영에서는 트래픽에 따라 20-50개 정도 권장합니다.

### OSIV 비활성화

```yaml
spring:
  jpa:
    open-in-view: false
```

불필요한 DB 커넥션 유지를 방지하고 트랜잭션 범위를 명확하게 합니다.

### 배치 처리

```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
```

대량 INSERT/UPDATE 시 네트워크 왕복 횟수가 줄어듭니다.

---

## 환경 설정

### 개발 환경

```bash
export SPRING_PROFILES_ACTIVE=dev
./gradlew bootRun
```

### 운영 환경

```bash
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET=your-production-secret
export DB_URL=jdbc:mysql://localhost:3306/ecommerce
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=your-password
export REDIS_PASSWORD=your-redis-password
export ALLOWED_ORIGINS=https://your-domain.com

./gradlew bootRun
```

### Docker Compose 예시

```yaml
version: '3.8'
services:
  app:
    build: .
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - JWT_SECRET=${JWT_SECRET}
      - DB_URL=jdbc:mysql://mysql:3306/ecommerce
    ports:
      - "8080:8080"

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_DATABASE=ecommerce
      - MYSQL_PASSWORD=${DB_PASSWORD}

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}
```

---

## 모니터링

### Actuator 엔드포인트

```bash
# 헬스체크 (인증 불필요)
curl http://localhost:8080/actuator/health

# 메트릭 (관리자 권한 필요)
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/prometheus
```

### 주요 메트릭

```bash
# JVM 메모리
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# HTTP 요청 수
curl http://localhost:8080/actuator/metrics/http.server.requests

# 커넥션 풀
curl http://localhost:8080/actuator/metrics/hikaricp.connections

# 캐시 히트율
curl http://localhost:8080/actuator/metrics/cache.gets
```

---

## 예상 성능 개선

| 항목 | Before | After | 개선율 |
|------|--------|-------|--------|
| 상품 조회 (캐시) | 50ms | 5ms | 90% |
| 주문 목록 (N+1) | 500ms | 50ms | 90% |
| 카테고리 조회 | 200ms | 20ms | 90% |

실제 환경에서는 트래픽 패턴에 따라 다를 수 있습니다.

---

## 배포 전 체크리스트

- [ ] JWT_SECRET 환경변수 설정
- [ ] DB 비밀번호 강력하게 설정
- [ ] CORS 도메인 제한 확인
- [ ] H2 Console 비활성화 (운영)
- [ ] SQL 로그 비활성화 (운영)
- [ ] HTTPS 설정
- [ ] Rate Limiting 동작 확인
- [ ] 백업 전략 수립

---

## 참고

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Docs](https://spring.io/projects/spring-security)
- [HikariCP GitHub](https://github.com/brettwooldridge/HikariCP)
- [Redis Caching Patterns](https://redis.io/docs/manual/patterns/)
- [Bucket4j](https://github.com/bucket4j/bucket4j)

---

Last updated: 2025-11-17
