# ChatSearch Platform - 대화 검색 플랫폼

> 대용량 채팅 메시지의 실시간 검색을 위한 고성능 검색 플랫폼

[![Java](https://img.shields.io/badge/Java-17-red.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![ElasticSearch](https://img.shields.io/badge/ElasticSearch-8.11-blue.svg)](https://www.elastic.co/)
[![Kafka](https://img.shields.io/badge/Kafka-7.5-black.svg)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-ready-blue.svg)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-ready-blue.svg)](https://kubernetes.io/)

## 📋 목차

- [프로젝트 소개](#프로젝트-소개)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [시스템 아키텍처](#시스템-아키텍처)
- [데이터 모델](#데이터-모델)
- [샤딩 전략](#샤딩-전략)
- [성능 최적화](#성능-최적화)
- [실행 방법](#실행-방법)
- [API 문서](#api-문서)
- [모니터링](#모니터링)

## 🎯 프로젝트 소개

**ChatSearch Platform**은 카카오톡과 같은 메시징 서비스에서 대용량 채팅 메시지를 빠르고 정확하게 검색할 수 있는 검색 플랫폼입니다.

### 개발 배경

- 메시징 서비스의 메시지 데이터는 기하급수적으로 증가
- 사용자는 과거 대화 내용을 빠르게 찾고자 함
- 기존 RDB만으로는 대용량 텍스트 검색 성능 한계
- 실시간 인덱싱 및 분산 검색 필요

### 해결 과제

1. **대용량 데이터 처리**: 수억 건의 메시지 효율적 저장 및 검색
2. **실시간 검색**: 메시지 생성 즉시 검색 가능
3. **한글 검색 최적화**: 형태소 분석 기반 정확한 검색
4. **확장성**: 수평적 확장 가능한 아키텍처
5. **고가용성**: 장애 대응 및 무중단 서비스

## ✨ 주요 기능

### 1. 실시간 메시지 검색

- **전문 검색 (Full-Text Search)**: ElasticSearch 기반 고속 검색
- **형태소 분석**: Nori 분석기를 활용한 한글 자연어 검색
- **다양한 검색 옵션**:
  - 전체 메시지 검색
  - 채팅방별 검색
  - 발신자별 검색
  - 기간별 검색
  - 복합 조건 검색

### 2. 데이터 파이프라인

- **Kafka 기반 이벤트 스트리밍**: 메시지 생성 이벤트를 Kafka로 전송
- **비동기 인덱싱**: Consumer가 메시지를 ElasticSearch에 비동기 인덱싱
- **확장 가능한 Consumer Group**: 처리량에 따라 Consumer 수평 확장

### 3. 배치 처리

- **Spring Batch**: 대용량 데이터 마이그레이션 및 인덱스 재생성
- **청크 기반 처리**: 1,000건 단위로 효율적 처리
- **재처리 전략**: 실패한 작업 자동 재시도

### 4. 샤딩 전략

- **2단계 샤딩**:
  - 1차: 사용자 ID 기반 (16개 샤드)
  - 2차: 시간 기반 월별 파티셔닝
- **균등 분산**: Consistent Hashing으로 데이터 균등 분배
- **검색 성능 향상**: 샤드별 병렬 검색

## 🛠 기술 스택

### Backend

- **Java 17**: 최신 LTS 버전
- **Spring Boot 3.2.1**: 프레임워크
- **Spring Data JPA**: ORM
- **QueryDSL**: 타입 세이프 쿼리
- **Spring Batch**: 배치 처리
- **Spring Security**: 인증/인가

### Database & Search

- **PostgreSQL**: 메인 데이터베이스
- **ElasticSearch 8.11**: 검색 엔진
- **Redis**: 캐싱 및 세션 관리

### Message Queue

- **Apache Kafka**: 이벤트 스트리밍

### DevOps & Infrastructure

- **Docker & Docker Compose**: 컨테이너화
- **Kubernetes**: 오케스트레이션
- **Prometheus**: 메트릭 수집
- **Grafana**: 모니터링 대시보드
- **Kibana**: ElasticSearch 데이터 시각화

### Build & CI/CD

- **Gradle 8.5**: 빌드 도구
- **Git**: 버전 관리

## 🏗 시스템 아키텍처

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP
       ▼
┌─────────────────────────────────────────────┐
│         chat-search-api (Spring Boot)       │
│  ┌──────────────┐      ┌─────────────────┐ │
│  │ REST API     │      │ Kafka Producer  │ │
│  │ Controller   │      │                 │ │
│  └──────────────┘      └─────────────────┘ │
└────────┬─────────────────┬──────────────────┘
         │                 │
         │                 ▼
         │          ┌─────────────┐
         │          │    Kafka    │
         │          └──────┬──────┘
         │                 │
         ▼                 ▼
┌──────────────┐   ┌──────────────────────────┐
│ PostgreSQL   │   │  chat-search-indexer     │
│  (Sharded)   │   │  ┌────────────────────┐  │
│              │   │  │ Kafka Consumer     │  │
│  - message_0 │   │  │ ElasticSearch      │  │
│  - message_1 │   │  │ Indexing Service   │  │
│  - ...       │   │  └────────────────────┘  │
│  - message_15│   └──────────────────────────┘
└──────────────┘              │
                              ▼
         ┌────────────────────────────────────┐
         │      ElasticSearch Cluster         │
         │  ┌──────────────────────────────┐  │
         │  │ Index: message-202401        │  │
         │  │ Index: message-202402        │  │
         │  │ Index: message-202403        │  │
         │  │ ...                          │  │
         │  └──────────────────────────────┘  │
         └────────────────────────────────────┘

         ┌────────────────────────────────────┐
         │   chat-search-batch (Spring Batch) │
         │  ┌──────────────────────────────┐  │
         │  │ Message Migration Job        │  │
         │  │ Index Rebuild Job            │  │
         │  └──────────────────────────────┘  │
         └────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│          Monitoring Stack                   │
│  ┌──────────┐  ┌──────────┐  ┌───────────┐ │
│  │Prometheus│─▶│ Grafana  │  │  Kibana   │ │
│  └──────────┘  └──────────┘  └───────────┘ │
└─────────────────────────────────────────────┘
```

## 📊 데이터 모델

### RDB (PostgreSQL)

```sql
-- 사용자 테이블
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    display_name VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- 채팅방 테이블
CREATE TABLE chat_rooms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    room_type VARCHAR(20) NOT NULL,
    owner_id BIGINT NOT NULL,
    member_count INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- 메시지 테이블 (샤딩됨)
-- 실제로는 message_0_202401, message_1_202401 등으로 분할
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL,
    file_url VARCHAR(1000),
    is_deleted BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    INDEX idx_room_created (room_id, created_at),
    INDEX idx_sender_created (sender_id, created_at)
);
```

### ElasticSearch

```json
{
  "mappings": {
    "properties": {
      "message_id": { "type": "long" },
      "room_id": { "type": "long" },
      "sender_id": { "type": "long" },
      "content": {
        "type": "text",
        "analyzer": "nori_analyzer"
      },
      "message_type": { "type": "keyword" },
      "is_deleted": { "type": "boolean" },
      "created_at": { "type": "date" }
    }
  }
}
```

## 🔀 샤딩 전략

### 1차 샤딩: 사용자 ID 기반

```java
public static int getShardNumber(Long userId) {
    return userId.hashCode() % 16;  // 16개 샤드
}
```

- **목적**: 데이터 균등 분산
- **샤드 수**: 16개
- **장점**:
  - 특정 사용자의 메시지는 항상 같은 샤드
  - 사용자별 검색 성능 최적화

### 2차 파티셔닝: 시간 기반 월별

```java
public static String getMonthKey(LocalDateTime dateTime) {
    return dateTime.format("yyyyMM");  // 예: "202401"
}
```

- **목적**: 시간 범위 검색 최적화
- **파티션 단위**: 월별
- **장점**:
  - 오래된 데이터 효율적 아카이빙
  - 기간별 검색 성능 향상

### 최종 테이블명 구조

```
message_{shard_number}_{month_key}

예시:
- message_0_202401  (사용자 ID % 16 = 0, 2024년 1월)
- message_5_202401  (사용자 ID % 16 = 5, 2024년 1월)
- message_0_202402  (사용자 ID % 16 = 0, 2024년 2월)
```

## ⚡ 성능 최적화

### 1. 데이터베이스 최적화

- **인덱스 전략**:
  - 복합 인덱스: (room_id, created_at)
  - 복합 인덱스: (sender_id, created_at)
- **커넥션 풀**: HikariCP (max: 10)
- **배치 삽입**: JDBC Batch Size 1000

### 2. ElasticSearch 최적화

- **샤드 구성**: 인덱스당 3개 샤드
- **레플리카**: 1개 (고가용성)
- **Refresh Interval**: 1초
- **형태소 분석**: Nori Analyzer

### 3. Kafka 최적화

- **Batch 처리**: 500건 단위
- **Producer Compression**: LZ4
- **Consumer Group**: 3개 스레드

### 4. 캐싱 전략

- **Redis 캐시**:
  - 검색 결과 캐싱 (TTL: 5분)
  - 사용자 세션 관리

### 예상 성능

| 항목 | 성능 |
|------|------|
| 검색 응답 시간 | 평균 50ms 이하 |
| 인덱싱 처리량 | 초당 10,000건 이상 |
| 배치 처리량 | 시간당 100만 건 이상 |
| 동시 사용자 | 10,000명 이상 |

## 🚀 실행 방법

### 1. Docker Compose로 전체 시스템 실행

```bash
# 인프라 실행 (PostgreSQL, Kafka, ElasticSearch 등)
docker-compose up -d

# 애플리케이션 빌드
./gradlew clean build

# API 서버 실행
java -jar chat-search-api/build/libs/chat-search-api-1.0.0.jar

# Indexer 실행
java -jar chat-search-indexer/build/libs/chat-search-indexer-1.0.0.jar

# Batch 실행
java -jar chat-search-batch/build/libs/chat-search-batch-1.0.0.jar
```

### 2. Kubernetes 배포

```bash
# ConfigMap & Secret 생성
kubectl apply -f k8s/configmap.yml

# API 배포
kubectl apply -f k8s/api-deployment.yml

# 서비스 확인
kubectl get pods
kubectl get services
```

## 📚 API 문서

### Swagger UI

애플리케이션 실행 후 아래 URL에서 API 문서 확인:

```
http://localhost:8080/swagger-ui.html
```

### 주요 API 엔드포인트

#### 1. 전체 메시지 검색

```bash
GET /api/v1/messages/search?keyword=안녕하세요&page=0&size=20
```

#### 2. 채팅방별 검색

```bash
GET /api/v1/messages/search/room/1?keyword=회의&page=0&size=20
```

#### 3. 기간별 검색

```bash
GET /api/v1/messages/search/date-range?keyword=프로젝트&startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
```

#### 4. 복합 조건 검색

```bash
POST /api/v1/messages/search/advanced
Content-Type: application/json

{
  "keyword": "미팅",
  "roomId": 1,
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-12-31T23:59:59",
  "page": 0,
  "size": 20
}
```

## 📈 모니터링

### Prometheus 메트릭

```
http://localhost:9090
```

주요 메트릭:
- JVM 메모리 사용량
- HTTP 요청 처리 시간
- Kafka Consumer Lag
- ElasticSearch 인덱싱 속도

### Grafana 대시보드

```
http://localhost:3000
ID: admin / PW: admin
```

### Kibana (ElasticSearch)

```
http://localhost:5601
```

## 🏗 프로젝트 구조

```
chat-search-platform/
├── chat-search-common/          # 공통 모듈
│   └── src/main/java/com/chatsearch/common/
│       ├── dto/                 # 공통 DTO
│       └── util/                # 유틸리티 클래스
│
├── chat-search-domain/          # 도메인 모듈
│   └── src/main/java/com/chatsearch/domain/
│       ├── entity/              # JPA 엔티티
│       ├── document/            # ElasticSearch 문서
│       └── repository/          # Repository
│
├── chat-search-api/             # REST API 모듈
│   └── src/main/java/com/chatsearch/api/
│       ├── controller/          # REST Controller
│       ├── service/             # 비즈니스 로직
│       └── dto/                 # API DTO
│
├── chat-search-indexer/         # 인덱서 모듈
│   └── src/main/java/com/chatsearch/indexer/
│       ├── consumer/            # Kafka Consumer
│       ├── service/             # 인덱싱 서비스
│       └── config/              # Kafka, ES 설정
│
├── chat-search-batch/           # 배치 모듈
│   └── src/main/java/com/chatsearch/batch/
│       └── job/                 # Batch Job 설정
│
├── docker-compose.yml           # Docker Compose 설정
├── k8s/                         # Kubernetes 매니페스트
└── monitoring/                  # 모니터링 설정
```

## 🎓 학습 포인트

이 프로젝트를 통해 다음과 같은 기술과 경험을 습득했습니다:

1. **대용량 데이터 처리**: 샤딩과 파티셔닝을 통한 확장성 확보
2. **검색 엔진**: ElasticSearch 인덱스 설계 및 최적화
3. **이벤트 기반 아키텍처**: Kafka를 활용한 비동기 처리
4. **배치 처리**: Spring Batch로 대용량 데이터 마이그레이션
5. **컨테이너 오케스트레이션**: Docker와 Kubernetes 활용
6. **모니터링**: Prometheus, Grafana를 통한 시스템 관찰성

## 📝 라이선스

이 프로젝트는 포트폴리오 목적으로 제작되었습니다.

---

## 👨‍💻 개발자

**백엔드 개발자 Ghoti**

- Email: peobae@gmail.com
- Blog: https://gamulgamulgamulchi.tistory.com/
- 경력: 6년차 백엔드 개발자

이 프로젝트는 실제 프로덕션 레벨의 검색 플랫폼을 구현하며,
카카오톡과 같은 대규모 메시징 서비스의 검색 기능을 목표로 개발되었습니다.
