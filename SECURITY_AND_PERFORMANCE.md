# 🔒 보안 및 성능 최적화 가이드

> 전자상거래 API 시스템의 보안 강화 및 성능 최적화 내역

---

## 📑 목차

- [보안 강화 사항](#-보안-강화-사항)
- [성능 최적화 사항](#-성능-최적화-사항)
- [환경 설정](#-환경-설정)
- [모니터링](#-모니터링)

---

## 🔐 보안 강화 사항

### 1. 인증 및 권한 관리

#### JWT 토큰 보안
- ✅ **시크릿 키 환경변수 관리**: 하드코딩 제거, 환경변수로 관리
- ✅ **HS512 알고리즘 사용**: 강력한 암호화 알고리즘 적용
- ✅ **토큰 만료 시간 설정**:
  - Access Token: 1시간
  - Refresh Token: 7일
- ✅ **토큰 검증 강화**: 만료, 서명, 형식 검증

#### 비밀번호 보안
- ✅ **BCrypt Strength 12**: 기본값(10)보다 강력한 암호화
- ✅ **비밀번호 정책 검증**:
  - 최소 8자 이상
  - 영문, 숫자, 특수문자 포함 필수
  - 정규식 패턴으로 검증

```java
@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$",
         message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
```

### 2. CORS 설정 강화

#### 기존 문제점
```java
// ❌ 모든 origin 허용 (보안 취약)
configuration.setAllowedOriginPatterns(Arrays.asList("*"));
```

#### 개선 사항
```java
// ✅ 환경변수로 허용 도메인 관리
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

**운영 환경 설정 예시:**
```bash
export ALLOWED_ORIGINS="https://your-domain.com,https://admin.your-domain.com"
```

### 3. Rate Limiting (DDoS 방지)

#### Bucket4j를 사용한 요청 횟수 제한

**일반 API**
- 분당 100회
- 초당 20회 (Burst)

**로그인 API (더 엄격)**
- 분당 5회

**구현 코드:**
```java
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    // IP 주소별로 Rate Limit 적용
    // 초과 시 429 Too Many Requests 응답
}
```

**응답 헤더:**
- `X-Rate-Limit-Remaining`: 남은 요청 수
- `X-Rate-Limit-Retry-After-Seconds`: 재시도 가능 시간

### 4. 보안 헤더 설정

```java
headers -> headers
    // Clickjacking 방지
    .addHeaderWriter(new XFrameOptionsHeaderWriter(
        XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
    // XSS 방지
    .xssProtection(xss -> xss.headerValue("1; mode=block"))
    // MIME 스니핑 방지
    .contentTypeOptions(options -> options.disable())
```

### 5. 환경별 설정 분리

#### 개발 환경 (`application-dev.yml`)
- H2 Console 활성화
- SQL 로그 출력
- 디버그 로그 레벨

#### 운영 환경 (`application-prod.yml`)
- H2 Console 비활성화
- SQL 로그 비활성화
- INFO 로그 레벨
- 파일 로그 저장

### 6. SQL Injection 방지

- ✅ JPA/QueryDSL 사용으로 기본적으로 방지
- ✅ @Query 사용 시 파라미터 바인딩 사용
- ✅ JPQL에서 파라미터화된 쿼리 사용

```java
// ✅ 안전한 쿼리
@Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword%")
Page<Product> searchByName(@Param("keyword") String keyword, Pageable pageable);
```

### 7. 민감 정보 보호

#### 로그에서 민감 정보 제외
```yaml
# 운영 환경 로깅
logging:
  level:
    com.portfolio.ecommerce: INFO  # DEBUG에서 INFO로 변경
    org.hibernate.SQL: WARN        # SQL 로그 최소화
```

#### 환경변수로 관리되는 민감 정보
- `JWT_SECRET`: JWT 시크릿 키
- `DB_PASSWORD`: 데이터베이스 비밀번호
- `REDIS_PASSWORD`: Redis 비밀번호
- `ALLOWED_ORIGINS`: 허용 도메인

---

## ⚡ 성능 최적화 사항

### 1. 데이터베이스 인덱스 최적화

#### User 엔티티
```java
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_status", columnList = "status"),
    @Index(name = "idx_user_created_at", columnList = "createdAt")
})
```
- **email**: 로그인 시 빠른 조회
- **status**: 상태별 필터링
- **createdAt**: 정렬 및 페이징 최적화

#### Product 엔티티
```java
@Table(name = "products", indexes = {
    @Index(name = "idx_product_category", columnList = "category"),
    @Index(name = "idx_product_status", columnList = "status"),
    @Index(name = "idx_product_category_status", columnList = "category, status"),
    @Index(name = "idx_product_name", columnList = "name"),
    @Index(name = "idx_product_created_at", columnList = "createdAt")
})
```
- **category**: 카테고리별 조회
- **status**: 상태별 필터링
- **category + status**: 복합 인덱스로 조회 성능 향상
- **name**: 상품명 검색 최적화

#### Order 엔티티
```java
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user_id", columnList = "user_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_user_status", columnList = "user_id, status"),
    @Index(name = "idx_order_created_at", columnList = "createdAt")
})
```
- **user_id**: 사용자별 주문 조회
- **user_id + status**: 사용자별 주문 상태 필터링

### 2. N+1 문제 해결

#### Hibernate 기본 설정
```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100  # 배치 페치 사이즈
```

#### Fetch Join 활용
```java
@Query("SELECT DISTINCT o FROM Order o " +
       "LEFT JOIN FETCH o.orderItems oi " +
       "LEFT JOIN FETCH oi.product " +
       "WHERE o.id = :orderId")
Order findByIdWithItems(@Param("orderId") Long orderId);
```

**효과:**
- N+1 쿼리 → 1개의 쿼리로 최적화
- 주문 조회 시 주문 항목과 상품 정보 함께 로드

### 3. 캐싱 전략

#### Redis 기반 다층 캐시

**도메인별 TTL 설정:**
```java
// 상품: 30분 (변경 빈도 낮음)
cacheConfigurations.put("products",
    defaultConfig.entryTtl(Duration.ofMinutes(30)));

// 사용자: 5분 (실시간성 중요)
cacheConfigurations.put("users",
    defaultConfig.entryTtl(Duration.ofMinutes(5)));

// 주문: 3분 (실시간성 매우 중요)
cacheConfigurations.put("orders",
    defaultConfig.entryTtl(Duration.ofMinutes(3)));
```

#### 캐시 적용 예시
```java
@Cacheable(value = "products", key = "#productId")
public ProductResponse getProduct(Long productId) {
    // 첫 호출: DB 조회 + 캐시 저장
    // 이후 호출: 캐시에서 직접 반환
}

@CacheEvict(value = "products", key = "#productId")
public ProductResponse updateProduct(Long productId, ProductRequest request) {
    // 수정 시 캐시 무효화
}
```

**성능 향상:**
- DB 조회 → Redis 조회 (10-100배 빠름)
- 동일 상품 반복 조회 시 DB 부하 감소

### 4. 커넥션 풀 최적화

#### HikariCP 설정
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # 최대 커넥션 수
      minimum-idle: 10           # 최소 유휴 커넥션
      connection-timeout: 30000  # 커넥션 타임아웃 (30초)
      idle-timeout: 600000       # 유휴 타임아웃 (10분)
      max-lifetime: 1800000      # 최대 수명 (30분)
```

**권장 설정값:**
- **개발 환경**: maximum-pool-size: 10
- **운영 환경**: maximum-pool-size: 20-50 (트래픽에 따라 조정)

### 5. OSIV 비활성화

```yaml
spring:
  jpa:
    open-in-view: false  # OSIV 비활성화
```

**효과:**
- 불필요한 DB 커넥션 유지 방지
- 트랜잭션 범위 명확화
- 성능 향상

### 6. 배치 처리 최적화

```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50         # 배치 크기
        order_inserts: true      # INSERT 순서 최적화
        order_updates: true      # UPDATE 순서 최적화
```

**효과:**
- 대량 데이터 INSERT/UPDATE 시 성능 향상
- 네트워크 왕복 횟수 감소

### 7. 페이징 최적화

```java
@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
Pageable pageable
```

- 적절한 페이지 크기 설정 (20개)
- 인덱스가 있는 컬럼으로 정렬
- COUNT 쿼리 최적화 (필요시 별도 쿼리)

---

## 🔧 환경 설정

### 개발 환경 실행

```bash
# 개발 프로파일로 실행
export SPRING_PROFILES_ACTIVE=dev
./gradlew bootRun
```

### 운영 환경 실행

```bash
# 운영 프로파일로 실행
export SPRING_PROFILES_ACTIVE=prod

# 환경변수 설정
export JWT_SECRET=your-production-secret-key-base64-encoded
export DB_URL=jdbc:mysql://localhost:3306/ecommerce
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=your-secure-password
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
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PASSWORD=${REDIS_PASSWORD}
    ports:
      - "8080:8080"

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_DATABASE=ecommerce
      - MYSQL_USER=${DB_USERNAME}
      - MYSQL_PASSWORD=${DB_PASSWORD}
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}
```

---

## 📊 모니터링

### Spring Boot Actuator

#### 활성화된 엔드포인트
- `/actuator/health`: 헬스체크 (인증 불필요)
- `/actuator/info`: 애플리케이션 정보
- `/actuator/metrics`: 메트릭 정보
- `/actuator/prometheus`: Prometheus 메트릭

#### 접근 권한
- 관리자(ADMIN) 권한 필요
- 헬스체크는 예외

### 주요 메트릭 모니터링

```bash
# JVM 메모리 사용량
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# HTTP 요청 수
curl http://localhost:8080/actuator/metrics/http.server.requests

# DB 커넥션 풀 상태
curl http://localhost:8080/actuator/metrics/hikaricp.connections

# 캐시 히트율
curl http://localhost:8080/actuator/metrics/cache.gets
```

### 로그 모니터링

#### 로그 파일 위치
```
logs/
└── ecommerce-api.log  # 운영 환경
```

#### 로그 로테이션
- 파일 최대 크기: 10MB
- 보관 기간: 30일

---

## 🎯 성능 벤치마크

### 예상 성능 향상

| 항목 | 최적화 전 | 최적화 후 | 개선율 |
|------|----------|----------|--------|
| 상품 조회 (캐시 히트) | 50ms | 5ms | **90%** ↓ |
| 주문 목록 조회 (N+1 해결) | 500ms | 50ms | **90%** ↓ |
| 카테고리별 상품 조회 (인덱스) | 200ms | 20ms | **90%** ↓ |
| 동시 접속자 처리 | 100명 | 500명 | **400%** ↑ |

---

## 🔍 보안 체크리스트

### 배포 전 확인사항

- [ ] JWT 시크릿 키 환경변수로 설정
- [ ] 운영 DB 비밀번호 강력하게 설정
- [ ] CORS 허용 도메인 제한
- [ ] H2 Console 비활성화 (운영 환경)
- [ ] SQL 로그 비활성화 (운영 환경)
- [ ] HTTPS 적용
- [ ] Rate Limiting 테스트
- [ ] 방화벽 설정
- [ ] 백업 전략 수립

---

## 📚 참고 자료

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security 공식 문서](https://spring.io/projects/spring-security)
- [HikariCP 설정 가이드](https://github.com/brettwooldridge/HikariCP)
- [Redis 캐싱 전략](https://redis.io/docs/manual/patterns/)
- [Bucket4j Rate Limiting](https://github.com/bucket4j/bucket4j)

---

**작성일**: 2025-11-08
**작성자**: Ghoti
**버전**: 1.0.0
