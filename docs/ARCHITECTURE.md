# 아키텍처 설계 문서

## 1. 시스템 개요

본 시스템은 파트너사(가맹점)에게 결제 승인, 취소, 정산 기능을 제공하는 API 게이트웨이입니다.

### 1.1 설계 목표

- **안정성**: 결제 데이터의 정합성 보장
- **확장성**: 트래픽 증가에 대응 가능한 구조
- **유연성**: 다양한 VAN사 추가 지원

## 2. 계층 구조

```
┌─────────────────────────────────────────────────┐
│                  Controller Layer                │
│         (API 엔드포인트, 요청/응답 처리)           │
├─────────────────────────────────────────────────┤
│                   Service Layer                  │
│         (비즈니스 로직, 트랜잭션 관리)             │
├─────────────────────────────────────────────────┤
│                 Repository Layer                 │
│         (데이터 접근, 영속성 관리)                 │
├─────────────────────────────────────────────────┤
│               Infrastructure Layer               │
│       (외부 시스템 연동, 캐시, 메시징)             │
└─────────────────────────────────────────────────┘
```

## 3. 핵심 도메인 설계

### 3.1 Payment (결제)

결제의 생명주기를 관리하는 핵심 애그리거트입니다.

```
Payment
├── transactionId (PK)    # 내부 거래 식별자
├── merchantId            # 가맹점 식별자
├── orderId               # 주문번호
├── amount                # 결제금액
├── canceledAmount        # 취소된 금액
├── status                # 상태 (PENDING → APPROVED → CANCELED)
├── approvalNumber        # 승인번호
└── vanTransactionId      # VAN사 거래번호
```

**상태 전이**
```
PENDING ──(승인)──→ APPROVED ──(부분취소)──→ PARTIAL_CANCELED
    │                   │                          │
    │                   └──────(전체취소)──────────→│
    │                                              ↓
    └──(실패)──→ FAILED                        CANCELED
```

### 3.2 Merchant (가맹점)

파트너사 정보와 계약 조건을 관리합니다.

### 3.3 Settlement (정산)

가맹점별 일자별 정산 데이터를 집계합니다.

## 4. 동시성 제어

### 4.1 분산 락

동일 결제 건에 대한 동시 요청을 방지합니다.

```java
// Redisson 기반 분산 락
lockExecutor.executeWithLock("payment:" + transactionId, () -> {
    // 결제 처리 로직
});
```

**락 키 전략**
- 결제 승인: `payment:lock:{transactionId}`
- 취소 처리: 비관적 락 + 낙관적 락(@Version) 병행

### 4.2 멱등성 보장

클라이언트가 제공한 멱등성 키로 중복 요청을 필터링합니다.

```
요청 → 멱등성 키 확인 → 기존 결과 반환 or 신규 처리
```

## 5. VAN사 연동

### 5.1 추상화 구조

```
VanClient (Interface)
    ├── NiceVanClient
    ├── KisVanClient (확장)
    └── ...

VanClientFactory
    └── getClient(vanType) → VanClient
```

### 5.2 장애 대응

- 타임아웃: 30초 기본, 설정 가능
- 재시도: 최대 3회, 지수 백오프
- 서킷브레이커: 연속 실패 시 빠른 실패 (향후 적용)

## 6. 데이터 정합성

### 6.1 트랜잭션 관리

- 결제 승인/취소: `@Transactional` 적용
- VAN 통신 실패 시 롤백 및 상태 업데이트

### 6.2 대사(Reconciliation)

VAN사 데이터와 내부 데이터의 정합성을 검증합니다.

**검증 항목**
- 금액 일치 여부
- 상태 일치 여부
- 누락 건 탐지

## 7. 이벤트 처리

### 7.1 Kafka 토픽

| 토픽 | 설명 |
|------|------|
| `payment.approved` | 결제 승인 이벤트 |
| `payment.canceled` | 결제 취소 이벤트 |
| `payment.settlement` | 정산 대상 이벤트 |

### 7.2 이벤트 활용

- 정산 시스템 연동
- 알림 서비스 연동
- 로그 수집

## 8. 성능 고려사항

### 8.1 인덱스 전략

```sql
-- 결제 테이블 인덱스
CREATE INDEX idx_payment_merchant_order ON payments(merchant_id, order_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_created_at ON payments(created_at);
```

### 8.2 쿼리 최적화

- QueryDSL을 통한 동적 쿼리
- 페이징 시 count 쿼리 최적화
- fetch join으로 N+1 방지

## 9. 모니터링

### 9.1 메트릭

- Micrometer + Prometheus 연동
- 결제 성공/실패율
- 응답 시간 분포
- VAN 통신 지연

### 9.2 로깅

- 결제 요청/응답 로깅
- VAN 통신 로깅
- 예외 상황 알림

## 10. 확장 방안

### 10.1 수평 확장

- Stateless 설계로 인스턴스 확장 용이
- Redis 클러스터로 분산 락 확장
- Kafka 파티션으로 처리량 확장

### 10.2 기능 확장

- 새로운 VAN사 추가: `VanClient` 구현체 추가
- 결제 수단 추가: `PaymentMethod` enum 확장
