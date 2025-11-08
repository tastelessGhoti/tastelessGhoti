# 🛒 E-Commerce API

> **5년 경력 자바 백엔드 개발자를 위한 포트폴리오 프로젝트**
> Spring Boot 기반의 전자상거래 RESTful API 시스템

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📋 목차

- [프로젝트 소개](#-프로젝트-소개)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시스템 아키텍처](#-시스템-아키텍처)
- [시작하기](#-시작하기)
- [API 문서](#-api-문서)
- [테스트](#-테스트)
- [프로젝트 구조](#-프로젝트-구조)

---

## 🎯 프로젝트 소개

본 프로젝트는 **실무 수준의 전자상거래 API 시스템**으로, 5년 경력 백엔드 개발자의 기술 역량을 보여주기 위해 개발되었습니다.

### 핵심 특징

- ✅ **계층형 아키텍처**: Controller - Service - Repository 패턴 적용
- ✅ **도메인 주도 설계**: User, Product, Order, Payment 도메인 분리
- ✅ **JWT 기반 인증**: Spring Security + JWT 토큰 인증 구현
- ✅ **전역 예외 처리**: 일관된 에러 응답 포맷 제공
- ✅ **API 문서화**: Swagger/OpenAPI를 통한 자동 문서 생성
- ✅ **캐싱 전략**: Redis를 활용한 성능 최적화
- ✅ **테스트 코드**: 단위 테스트 및 통합 테스트 작성
- ✅ **트랜잭션 관리**: 복잡한 비즈니스 로직의 트랜잭션 처리

---

## 🚀 주요 기능

### 1. 사용자 관리 (User)
- 회원가입 / 로그인
- JWT 토큰 기반 인증
- 사용자 정보 관리
- 권한 관리 (USER, ADMIN)

### 2. 상품 관리 (Product)
- 상품 CRUD
- 카테고리별 상품 조회
- 상품 검색
- 재고 관리
- 캐싱을 통한 성능 최적화

### 3. 주문 관리 (Order)
- 주문 생성
- 주문 상태 관리 (대기, 확인, 배송, 완료, 취소)
- 주문 취소 및 재고 복구
- 주문 내역 조회

### 4. 결제 관리 (Payment)
- 결제 정보 관리
- 다양한 결제 수단 지원
- 결제 상태 추적

---

## 🛠️ 기술 스택

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.0
- **ORM**: Spring Data JPA, Hibernate
- **Security**: Spring Security, JWT
- **Database**: H2 (개발), MySQL (운영)
- **Cache**: Redis
- **Build Tool**: Gradle
- **API Documentation**: Swagger/OpenAPI

### Test
- JUnit 5
- Mockito
- AssertJ
- REST Assured

### DevOps
- Docker
- Git

---

## 🏗️ 시스템 아키텍처

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────┐
│     Spring Security Filter      │ ← JWT 인증
└─────────────┬───────────────────┘
              │
       ┌──────▼──────┐
       │ Controller  │ ← API 엔드포인트
       └──────┬──────┘
              │
       ┌──────▼──────┐
       │   Service   │ ← 비즈니스 로직
       └──────┬──────┘
              │
       ┌──────▼──────┐
       │ Repository  │ ← 데이터 접근
       └──────┬──────┘
              │
   ┌──────────┴──────────┐
   ▼                     ▼
┌──────┐            ┌───────┐
│  DB  │            │ Redis │
└──────┘            └───────┘
```

---

## 🏁 시작하기

### 사전 요구사항

- Java 17 이상
- Gradle 8.x 이상
- (선택) Redis (캐싱 사용 시)

### 설치 및 실행

1. **프로젝트 클론**
   ```bash
   git clone https://github.com/yourusername/ecommerce-api.git
   cd ecommerce-api
   ```

2. **빌드**
   ```bash
   ./gradlew build
   ```

3. **실행**
   ```bash
   ./gradlew bootRun
   ```

4. **접속**
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - H2 Console: http://localhost:8080/h2-console

### 환경 설정

`application.yml` 파일에서 다음 설정을 변경할 수 있습니다:

- 데이터베이스 연결 정보
- JWT 시크릿 키
- Redis 연결 정보
- 로깅 레벨

---

## 📚 API 문서

### Swagger UI
애플리케이션 실행 후 http://localhost:8080/swagger-ui.html 접속

### 주요 API 엔드포인트

#### 인증 (Auth)
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/login` - 로그인

#### 상품 (Product)
- `GET /api/products` - 상품 목록 조회
- `GET /api/products/{id}` - 상품 상세 조회
- `GET /api/products/search?keyword={keyword}` - 상품 검색
- `POST /api/products` - 상품 등록 (관리자)
- `PUT /api/products/{id}` - 상품 수정 (관리자)
- `DELETE /api/products/{id}` - 상품 삭제 (관리자)

#### 주문 (Order)
- `POST /api/orders` - 주문 생성
- `GET /api/orders/my` - 내 주문 목록
- `GET /api/orders/{id}` - 주문 상세 조회
- `POST /api/orders/{id}/cancel` - 주문 취소
- `POST /api/orders/{id}/confirm` - 주문 확인 (관리자)

### API 요청 예시

**회원가입**
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123!",
    "name": "홍길동",
    "phoneNumber": "010-1234-5678"
  }'
```

**로그인**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123!"
  }'
```

**상품 조회**
```bash
curl -X GET http://localhost:8080/api/products
```

**주문 생성 (인증 필요)**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "orderItems": [
      {
        "productId": 1,
        "quantity": 2
      }
    ],
    "orderMessage": "빠른 배송 부탁드립니다."
  }'
```

---

## 🧪 테스트

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests ProductServiceTest
```

### 테스트 커버리지

```bash
./gradlew jacocoTestReport
```

테스트 리포트는 `build/reports/tests/test/index.html`에서 확인 가능합니다.

---

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/
│   │   └── com/portfolio/ecommerce/
│   │       ├── EcommerceApplication.java          # 메인 클래스
│   │       ├── common/                            # 공통 클래스
│   │       │   ├── BaseEntity.java                # 엔티티 기본 클래스
│   │       │   ├── ApiResponse.java               # API 응답 포맷
│   │       │   └── PageResponse.java              # 페이징 응답 포맷
│   │       ├── config/                            # 설정 클래스
│   │       │   ├── SecurityConfig.java            # Security 설정
│   │       │   ├── JpaConfig.java                 # JPA 설정
│   │       │   └── SwaggerConfig.java             # Swagger 설정
│   │       ├── security/                          # 보안 관련
│   │       │   ├── JwtTokenProvider.java          # JWT 토큰 생성/검증
│   │       │   └── JwtAuthenticationFilter.java   # JWT 인증 필터
│   │       ├── exception/                         # 예외 처리
│   │       │   ├── GlobalExceptionHandler.java    # 전역 예외 핸들러
│   │       │   ├── BusinessException.java         # 비즈니스 예외
│   │       │   ├── ErrorCode.java                 # 에러 코드 정의
│   │       │   └── ErrorResponse.java             # 에러 응답 포맷
│   │       └── domain/                            # 도메인
│   │           ├── user/                          # 사용자 도메인
│   │           │   ├── User.java                  # 사용자 엔티티
│   │           │   ├── UserRepository.java        # 사용자 리포지토리
│   │           │   ├── dto/                       # DTO
│   │           │   ├── service/                   # 서비스
│   │           │   │   └── AuthService.java
│   │           │   └── controller/                # 컨트롤러
│   │           │       └── AuthController.java
│   │           ├── product/                       # 상품 도메인
│   │           │   ├── Product.java
│   │           │   ├── ProductRepository.java
│   │           │   ├── dto/
│   │           │   ├── service/
│   │           │   │   └── ProductService.java
│   │           │   └── controller/
│   │           │       └── ProductController.java
│   │           ├── order/                         # 주문 도메인
│   │           │   ├── Order.java
│   │           │   ├── OrderItem.java
│   │           │   ├── OrderRepository.java
│   │           │   ├── dto/
│   │           │   ├── service/
│   │           │   │   └── OrderService.java
│   │           │   └── controller/
│   │           │       └── OrderController.java
│   │           └── payment/                       # 결제 도메인
│   │               ├── Payment.java
│   │               └── PaymentRepository.java
│   └── resources/
│       └── application.yml                        # 애플리케이션 설정
└── test/                                          # 테스트 코드
    └── java/
        └── com/portfolio/ecommerce/
            ├── domain/
            │   ├── product/
            │   │   └── service/
            │   │       └── ProductServiceTest.java
            │   └── order/
            │       └── service/
            │           └── OrderServiceTest.java
```

---

## 💡 핵심 구현 내용

### 1. JWT 기반 인증 시스템
- Spring Security와 JWT를 활용한 Stateless 인증 구현
- Access Token과 Refresh Token 분리
- 토큰 유효성 검증 필터 구현

### 2. 전역 예외 처리
- `@RestControllerAdvice`를 활용한 일관된 에러 응답
- 비즈니스 예외와 시스템 예외 분리
- 상세한 에러 코드 정의

### 3. 복잡한 비즈니스 로직
- 주문 생성 시 재고 차감 및 트랜잭션 관리
- 주문 취소 시 재고 복구 로직
- 주문 상태별 전이 규칙 구현

### 4. 성능 최적화
- 캐싱을 통한 상품 조회 성능 개선
- N+1 문제 해결 (Fetch Join 활용)
- 페이징을 통한 대용량 데이터 처리

### 5. 테스트 코드
- Mockito를 활용한 단위 테스트
- 비즈니스 로직 검증
- 예외 상황 테스트

---

## 🔍 개선 예정 사항

- [ ] QueryDSL을 활용한 동적 쿼리 구현
- [ ] Redis를 통한 세션 관리
- [ ] 결제 모듈 외부 API 연동
- [ ] 이벤트 기반 아키텍처 적용 (Spring Events)
- [ ] 통합 테스트 추가
- [ ] Docker Compose를 통한 개발 환경 구성
- [ ] CI/CD 파이프라인 구축

---

## 📝 라이센스

이 프로젝트는 MIT 라이센스를 따릅니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

## 👤 개발자

**Ghoti**
- Email: peobae@gmail.com
- Blog: https://gamulgamulgamulchi.tistory/
- GitHub: https://github.com/yourusername

---

## 🙏 감사의 말

이 프로젝트를 통해 실무에서 사용되는 다양한 기술과 패턴을 학습하고 적용할 수 있었습니다.
더 나은 코드를 작성하기 위해 항상 노력하겠습니다.

---

**⭐ 이 프로젝트가 도움이 되었다면 Star를 눌러주세요!**
