# 📚 BookVerse - 온라인 서점 플랫폼

> 엔터프라이즈급 온라인 서점 백엔드 시스템

## 프로젝트 소개

BookVerse는 **실무 환경을 고려한 엔터프라이즈급 온라인 서점 플랫폼**입니다.
Spring Boot 3.x와 최신 Java 17을 기반으로 설계되었으며, 확장성과 성능을 고려한 아키텍처를 적용했습니다.

### 주요 기능

- ✅ **사용자 인증/인가**: JWT 기반 Stateless 인증, Spring Security
- ✅ **도서 관리**: 도서 등록/조회/수정/삭제, 카테고리별 분류
- ✅ **고급 검색**: QueryDSL을 활용한 동적 쿼리 및 다중 조건 검색
- ✅ **주문 시스템**: 주문 생성/조회/취소, 재고 관리, 결제 연동 구조
- ✅ **리뷰 시스템**: 도서 리뷰 및 평점 관리
- ✅ **캐싱 최적화**: Redis를 활용한 도서 조회 및 베스트셀러 캐싱
- ✅ **API 문서화**: Swagger/OpenAPI 자동 문서 생성

## 기술 스택

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.5
- **Security**: Spring Security + JWT (jjwt 0.12.3)
- **ORM**: Spring Data JPA + QueryDSL 5.0
- **Database**: MySQL 8.0, H2 (테스트)
- **Cache**: Redis 7
- **Build**: Gradle
- **Test**: JUnit 5, Mockito

### Infrastructure
- **Containerization**: Docker, Docker Compose
- **API Documentation**: SpringDoc OpenAPI 3

## 프로젝트 구조

```
bookverse/
├── src/
│   ├── main/
│   │   ├── java/com/bookverse/
│   │   │   ├── common/              # 공통 모듈 (BaseEntity, ApiResponse)
│   │   │   ├── config/              # 설정 (Security, Redis, QueryDSL, Swagger)
│   │   │   ├── domain/
│   │   │   │   ├── user/            # 사용자 도메인
│   │   │   │   ├── book/            # 도서 도메인
│   │   │   │   ├── category/        # 카테고리 도메인
│   │   │   │   ├── order/           # 주문 도메인
│   │   │   │   └── review/          # 리뷰 도메인
│   │   │   ├── exception/           # 예외 처리
│   │   │   └── security/            # JWT 인증
│   │   └── resources/
│   │       └── application.yml      # 환경별 설정
│   └── test/                        # 단위 테스트
├── docker/
│   ├── docker-compose.yml           # Docker 컨테이너 설정
│   └── init.sql                     # DB 초기화 스크립트
└── build.gradle                     # 의존성 관리
```

## 시작하기

### 사전 요구사항

- Java 17 이상
- Docker & Docker Compose
- Git

### 1. 프로젝트 클론

```bash
git clone <repository-url>
cd bookverse
```

### 2. 환경 설정

Docker를 사용하여 MySQL과 Redis를 실행합니다:

```bash
cd docker
docker-compose up -d
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는

```bash
./gradlew build
java -jar build/libs/bookverse-1.0.0.jar
```

### 4. API 문서 확인

애플리케이션 실행 후 Swagger UI에서 API를 확인할 수 있습니다:

```
http://localhost:8080/swagger-ui.html
```

## 주요 API 엔드포인트

### 인증 (Authentication)
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/signin` - 로그인
- `GET /api/auth/check-email` - 이메일 중복 확인

### 도서 (Books)
- `GET /api/books/search` - 도서 검색 (제목, 저자, 출판사, 카테고리 등)
- `GET /api/books/{bookId}` - 도서 상세 조회
- `GET /api/books/bestsellers` - 베스트셀러 조회
- `GET /api/books/new-releases` - 신간 도서 조회
- `POST /api/books` - 도서 등록 (관리자)

### 주문 (Orders)
- `POST /api/orders` - 주문 생성
- `GET /api/orders/my` - 내 주문 목록 조회
- `GET /api/orders/{orderId}` - 주문 상세 조회
- `POST /api/orders/{orderId}/cancel` - 주문 취소

## 핵심 구현 사항

### 1. 레이어드 아키텍처
```
Controller → Service → Repository → Entity
```
명확한 계층 분리로 유지보수성과 테스트 용이성 확보

### 2. QueryDSL 동적 쿼리
```java
public Page<Book> searchBooks(BookSearchCondition condition, Pageable pageable) {
    // 제목, 저자, 출판사, 가격 범위 등 다중 조건 검색
    // BooleanExpression을 활용한 동적 쿼리 생성
}
```

### 3. Redis 캐싱 전략
```java
@Cacheable(value = "books", key = "#bookId")
public BookResponse getBook(Long bookId) {
    // 자주 조회되는 도서 정보는 Redis에 캐싱하여 DB 부하 감소
}
```

### 4. JWT 기반 인증
- Access Token (1시간) + Refresh Token (7일)
- Stateless 방식으로 서버 확장성 향상
- Spring Security FilterChain에 통합

### 5. 트랜잭션 관리
```java
@Transactional
public Long createOrder(String userEmail, OrderCreateRequest request) {
    // 주문 생성 시 재고 감소, 판매량 증가 등 복수 작업을 하나의 트랜잭션으로 처리
}
```

### 6. 예외 처리 전략
- 비즈니스 예외를 ErrorCode Enum으로 체계적 관리
- @RestControllerAdvice를 통한 전역 예외 처리
- 클라이언트 친화적인 에러 응답 포맷

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트만 실행
./gradlew test --tests BookServiceTest
```

## 환경 변수

### application.yml 프로파일
- `local`: H2 인메모리 DB (개발)
- `dev`: MySQL (개발 서버)
- `prod`: MySQL (운영 서버, 환경변수 필요)

### 운영 환경 필수 환경변수
```bash
DB_URL=jdbc:mysql://localhost:3306/bookverse
DB_USERNAME=bookverse
DB_PASSWORD=bookverse123
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=your-256-bit-secret-key
```

## 성능 최적화

1. **N+1 문제 해결**
   - `@EntityGraph` 및 `JOIN FETCH` 활용
   - Batch Size 설정 (100)

2. **캐싱 전략**
   - 도서 상세 조회: Redis 캐시
   - 베스트셀러/신간: Redis 캐시 (TTL 1시간)

3. **쿼리 최적화**
   - QueryDSL로 필요한 컬럼만 조회
   - 페이징 처리 (Page, Pageable)
   - 인덱스 활용 (title, author, isbn 등)

## 보안

- **비밀번호**: BCrypt 암호화
- **JWT**: HS256 알고리즘, 256bit Secret Key
- **CORS**: Origin 패턴 기반 접근 제어
- **SQL Injection**: JPA/QueryDSL Prepared Statement 사용

## 개발자 정보

- **개발자**: Ghoti
- **경력**: 백엔드 개발 6년차
- **Email**: peobae@gmail.com
- **Blog**: https://gamulgamulgamulchi.tistory.com

## 라이선스

이 프로젝트는 포트폴리오 목적으로 제작되었습니다.
