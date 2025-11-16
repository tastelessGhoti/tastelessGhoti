# E-Commerce API

Spring Boot 기반의 전자상거래 RESTful API 시스템

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)

---

## 프로젝트 소개

실무 수준의 전자상거래 API 시스템입니다. 계층형 아키텍처, JWT 인증, Redis 캐싱, Rate Limiting 등 실제 서비스에 필요한 기능들을 구현했습니다.

### 주요 특징

- 계층형 아키텍처 (Controller-Service-Repository)
- 도메인 주도 설계 (User, Product, Order, Payment)
- JWT 기반 인증 (Spring Security)
- 전역 예외 처리
- Swagger/OpenAPI 문서화
- Redis 캐싱
- Rate Limiting (DDoS 방지)
- 데이터베이스 인덱스 최적화
- N+1 쿼리 최적화

---

## 주요 기능

### 사용자
- 회원가입 / 로그인
- JWT 토큰 인증
- 권한 관리 (USER, ADMIN)

### 상품
- CRUD 작업
- 카테고리별 조회
- 검색 기능
- 재고 관리
- Redis 캐싱

### 주문
- 주문 생성/조회
- 상태 관리 (대기, 확인, 배송, 완료, 취소)
- 재고 자동 차감/복구
- 트랜잭션 처리

### 결제
- 결제 정보 관리
- 다양한 결제 수단
- 결제 상태 추적

---

## 기술 스택

**Backend**
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security + JWT
- QueryDSL
- Redis
- Bucket4j (Rate Limiting)

**Database**
- H2 (개발)
- MySQL (운영)

**Test**
- JUnit 5
- Mockito
- AssertJ

**Tools**
- Gradle
- Swagger/OpenAPI
- Spring Boot Actuator

---

## 시스템 아키텍처

```
Client
  ↓
Security Filter (JWT + Rate Limit)
  ↓
Controller (API)
  ↓
Service (Business Logic)
  ↓
Repository (Data Access)
  ↓
Database / Redis
```

---

## 시작하기

### 요구사항

- Java 17+
- Gradle 8.x+
- Redis (선택, 캐싱 사용 시)

### 설치

```bash
git clone https://github.com/yourusername/ecommerce-api.git
cd ecommerce-api

# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

### 접속

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

---

## API 문서

Swagger UI에서 확인: http://localhost:8080/swagger-ui.html

### 주요 엔드포인트

**인증**
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/login` - 로그인

**상품**
- `GET /api/products` - 상품 목록
- `GET /api/products/{id}` - 상품 상세
- `GET /api/products/search?keyword={keyword}` - 검색
- `POST /api/products` - 상품 등록 (관리자)
- `PUT /api/products/{id}` - 수정 (관리자)
- `DELETE /api/products/{id}` - 삭제 (관리자)

**주문**
- `POST /api/orders` - 주문 생성
- `GET /api/orders/my` - 내 주문 목록
- `GET /api/orders/{id}` - 주문 상세
- `POST /api/orders/{id}/cancel` - 주문 취소

### 사용 예시

```bash
# 회원가입
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123!",
    "name": "홍길동"
  }'

# 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123!"
  }'

# 주문 (토큰 필요)
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "orderItems": [{"productId": 1, "quantity": 2}]
  }'
```

---

## 테스트

```bash
# 전체 테스트
./gradlew test

# 특정 테스트
./gradlew test --tests ProductServiceTest
```

---

## 프로젝트 구조

```
src/
├── main/
│   ├── java/com/portfolio/ecommerce/
│   │   ├── EcommerceApplication.java
│   │   ├── common/                    # 공통 클래스
│   │   ├── config/                    # 설정
│   │   │   ├── SecurityConfig.java
│   │   │   ├── CacheConfig.java
│   │   │   └── RateLimitConfig.java
│   │   ├── security/                  # 보안
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── RateLimitFilter.java
│   │   ├── exception/                 # 예외 처리
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ErrorCode.java
│   │   └── domain/                    # 도메인
│   │       ├── user/
│   │       ├── product/
│   │       ├── order/
│   │       └── payment/
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
└── test/                              # 테스트
```

---

## 주요 구현 내용

### JWT 인증
- HS512 알고리즘
- Access/Refresh 토큰 분리
- 환경변수로 시크릿 관리

### 예외 처리
- `@RestControllerAdvice` 사용
- 일관된 에러 응답
- 비즈니스/시스템 예외 분리

### 성능 최적화
- 데이터베이스 인덱스 (email, category, status 등)
- Redis 캐싱 (도메인별 TTL)
- N+1 문제 해결 (Fetch Join, batch_fetch_size)
- HikariCP 커넥션 풀

### 보안
- Rate Limiting (Bucket4j)
- CORS 설정 (환경변수)
- BCrypt 암호화 (strength 12)
- 보안 헤더 (XSS, Clickjacking 방지)

---

## 환경 설정

### 개발

```bash
export SPRING_PROFILES_ACTIVE=dev
./gradlew bootRun
```

### 운영

```bash
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET=your-secret
export DB_URL=jdbc:mysql://localhost:3306/ecommerce
export DB_PASSWORD=your-password
export ALLOWED_ORIGINS=https://your-domain.com

./gradlew bootRun
```

자세한 내용은 [SECURITY_AND_PERFORMANCE.md](SECURITY_AND_PERFORMANCE.md) 참고

---

## 개선 예정

- QueryDSL 동적 쿼리
- 이벤트 기반 아키텍처
- 통합 테스트 추가
- Docker Compose 환경
- CI/CD 파이프라인

---

## Contact

- Email: peobae@gmail.com
- Blog: https://gamulgamulgamulchi.tistory/

---

Last updated: 2025-11-17
