# API 명세서

## 기본 정보

- **Base URL**: `/api`
- **인증**: `X-Merchant-Id` 헤더로 가맹점 식별

## 공통 응답 형식

### 성공 응답
```json
{
  "success": true,
  "code": "0000",
  "message": "성공",
  "data": { ... },
  "timestamp": "2024-12-15T10:30:00"
}
```

### 실패 응답
```json
{
  "success": false,
  "code": "P001",
  "message": "결제 정보를 찾을 수 없습니다",
  "data": null,
  "timestamp": "2024-12-15T10:30:00"
}
```

## 에러 코드

| 코드 | 설명 |
|------|------|
| C001 | 잘못된 입력값 |
| C002 | 서버 오류 |
| P001 | 결제 정보 없음 |
| P002 | 이미 승인된 결제 |
| P003 | 이미 취소된 결제 |
| P004 | 취소 금액 초과 |
| P005 | 결제 승인 실패 |
| P006 | 결제 취소 실패 |
| P010 | 중복 주문번호 |
| M001 | 가맹점 정보 없음 |
| M002 | 비활성 가맹점 |

---

## 1. 결제 승인

### Endpoint
```
POST /v1/payments/approve
```

### Headers
| 이름 | 필수 | 설명 |
|------|------|------|
| X-Merchant-Id | Y | 가맹점 ID |
| Content-Type | Y | application/json |

### Request Body
```json
{
  "orderId": "ORD-2024-001",
  "amount": 50000,
  "paymentMethod": "CARD",
  "cardNumber": "9410123456789012",
  "expiryDate": "1226",
  "birthDate": "90",
  "cardPassword": "12",
  "installmentMonths": 0,
  "productName": "테스트 상품",
  "buyerName": "홍길동",
  "buyerEmail": "hong@example.com",
  "buyerPhone": "01012345678",
  "idempotencyKey": "unique-key-12345"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| orderId | String | Y | 주문번호 (최대 64자) |
| amount | Number | Y | 결제금액 (100~100,000,000) |
| paymentMethod | String | Y | 결제수단 (CARD) |
| cardNumber | String | Y | 카드번호 (14~16자리) |
| expiryDate | String | Y | 유효기간 (MMYY) |
| birthDate | String | N | 생년월일 앞 2자리 |
| cardPassword | String | N | 비밀번호 앞 2자리 |
| installmentMonths | Number | N | 할부개월 (0~12) |
| productName | String | N | 상품명 |
| buyerName | String | N | 구매자명 |
| buyerEmail | String | N | 이메일 |
| buyerPhone | String | N | 전화번호 |
| idempotencyKey | String | N | 멱등성 키 |

### Response
```json
{
  "success": true,
  "code": "0000",
  "message": "결제가 승인되었습니다",
  "data": {
    "transactionId": "TXN202412150001",
    "orderId": "ORD-2024-001",
    "amount": 50000,
    "status": "APPROVED",
    "paymentMethod": "CARD",
    "cardNumber": "941012****9012",
    "cardCompany": "삼성카드",
    "installmentMonths": 0,
    "approvalNumber": "12345678",
    "approvedAt": "2024-12-15T10:30:00"
  },
  "timestamp": "2024-12-15T10:30:00"
}
```

---

## 2. 결제 취소

### Endpoint
```
POST /v1/payments/cancel
```

### Request Body
```json
{
  "transactionId": "TXN202412150001",
  "cancelAmount": 20000,
  "cancelReason": "고객 요청",
  "idempotencyKey": "cancel-unique-key"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| transactionId | String | Y | 결제 트랜잭션 ID |
| cancelAmount | Number | N | 취소금액 (미입력 시 전체 취소) |
| cancelReason | String | N | 취소사유 |
| idempotencyKey | String | N | 멱등성 키 |

### Response
```json
{
  "success": true,
  "code": "0000",
  "message": "결제가 취소되었습니다",
  "data": {
    "transactionId": "TXN202412150001",
    "cancelTransactionId": "TXN202412150002",
    "canceledAmount": 20000,
    "totalCanceledAmount": 20000,
    "remainingAmount": 30000,
    "status": "PARTIAL_CANCELED",
    "canceledAt": "2024-12-15T11:00:00"
  },
  "timestamp": "2024-12-15T11:00:00"
}
```

---

## 3. 결제 상세 조회

### Endpoint
```
GET /v1/payments/{transactionId}
```

### Path Parameters
| 이름 | 설명 |
|------|------|
| transactionId | 결제 트랜잭션 ID |

### Response
```json
{
  "success": true,
  "code": "0000",
  "message": "성공",
  "data": {
    "transactionId": "TXN202412150001",
    "merchantId": "M20231201001",
    "orderId": "ORD-2024-001",
    "amount": 50000,
    "canceledAmount": 20000,
    "cancelableAmount": 30000,
    "status": "PARTIAL_CANCELED",
    "paymentMethod": "CARD",
    "cardNumber": "941012****9012",
    "cardCompany": "삼성카드",
    "installmentMonths": 0,
    "approvalNumber": "12345678",
    "approvedAt": "2024-12-15T10:30:00",
    "productName": "테스트 상품",
    "buyerName": "홍길동",
    "buyerEmail": "hon***@example.com",
    "buyerPhone": "010****5678",
    "createdAt": "2024-12-15T10:30:00"
  },
  "timestamp": "2024-12-15T12:00:00"
}
```

---

## 4. 결제 목록 조회

### Endpoint
```
GET /v1/payments
```

### Query Parameters
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| status | String | N | 결제 상태 필터 |
| orderId | String | N | 주문번호 검색 |
| startDate | DateTime | N | 조회 시작일시 |
| endDate | DateTime | N | 조회 종료일시 |
| page | Number | N | 페이지 번호 (0부터) |
| size | Number | N | 페이지 크기 (기본 20) |

### Response
```json
{
  "success": true,
  "code": "0000",
  "message": "성공",
  "data": {
    "content": [
      {
        "transactionId": "TXN202412150001",
        "orderId": "ORD-2024-001",
        "amount": 50000,
        "status": "APPROVED",
        ...
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  },
  "timestamp": "2024-12-15T12:00:00"
}
```
