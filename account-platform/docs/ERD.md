# ERD (Entity Relationship Diagram)

## 테이블 구조

```mermaid
erDiagram
    MEMBER ||--o{ LOGIN_HISTORY : has
    MEMBER ||--o{ TERMS_AGREEMENT : has
    MEMBER ||--o{ KYC_VERIFICATION : has
    TERMS ||--o{ TERMS_AGREEMENT : referenced

    MEMBER {
        bigint id PK
        varchar(88) ci UK "연계정보 (고유식별자)"
        varchar(64) di "중복가입확인정보"
        varchar(50) name "이름"
        varchar(20) phone_number "전화번호"
        varchar(100) email "이메일"
        varchar(8) birth_date "생년월일 (YYYYMMDD)"
        varchar(20) status "회원상태"
        datetime last_login_at "마지막 로그인"
        datetime withdrawn_at "탈퇴일시"
        datetime suspended_at "정지일시"
        varchar(500) suspension_reason "정지사유"
        datetime created_at
        datetime updated_at
    }

    LOGIN_HISTORY {
        bigint id PK
        bigint member_id FK
        varchar(45) ip_address "접속 IP"
        varchar(500) user_agent
        varchar(200) device_info "디바이스 정보"
        varchar(20) login_result "로그인 결과"
        varchar(200) failure_reason "실패 사유"
        datetime created_at
        datetime updated_at
    }

    TERMS {
        bigint id PK
        varchar(50) terms_code "약관 코드"
        varchar(200) title "약관 제목"
        text content "약관 내용"
        int version "약관 버전"
        boolean is_required "필수 여부"
        boolean is_active "활성화 여부"
        date effective_date "시행일"
        int display_order "노출 순서"
        datetime created_at
        datetime updated_at
    }

    TERMS_AGREEMENT {
        bigint id PK
        bigint member_id FK
        bigint terms_id FK
        datetime agreed_at "동의일시"
        varchar(45) agreed_ip "동의 IP"
        datetime withdrawn_at "철회일시"
        datetime created_at
        datetime updated_at
    }

    KYC_VERIFICATION {
        bigint id PK
        bigint member_id FK
        varchar(30) verification_type "인증 유형"
        varchar(20) status "인증 상태"
        varchar(20) level "인증 레벨"
        varchar(30) id_card_type "신분증 종류"
        varchar(64) id_card_number_hash "신분증번호 해시"
        varchar(50) verified_name "인증된 이름"
        varchar(8) verified_birth_date "인증된 생년월일"
        datetime verified_at "인증완료일시"
        date expires_at "만료일"
        varchar(500) rejection_reason "거절 사유"
        int retry_count "재시도 횟수"
        datetime created_at
        datetime updated_at
    }
```

## 인덱스 설계

### member
| 인덱스명 | 컬럼 | 용도 |
|---------|------|------|
| PRIMARY | id | PK |
| idx_member_ci | ci | CI 기반 회원 조회 (로그인) |
| idx_member_status | status | 상태별 회원 조회 |
| idx_member_phone | phone_number | 전화번호 기반 조회 |

### login_history
| 인덱스명 | 컬럼 | 용도 |
|---------|------|------|
| PRIMARY | id | PK |
| idx_login_history_member | member_id, created_at DESC | 회원별 로그인 이력 조회 |

### terms
| 인덱스명 | 컬럼 | 용도 |
|---------|------|------|
| PRIMARY | id | PK |
| idx_terms_code_version | terms_code, version (UNIQUE) | 약관 버전 조회 |
| idx_terms_active | is_active, effective_date | 활성 약관 목록 조회 |

### terms_agreement
| 인덱스명 | 컬럼 | 용도 |
|---------|------|------|
| PRIMARY | id | PK |
| idx_terms_agreement_member | member_id, terms_id | 회원별 동의 현황 조회 |
| idx_terms_agreement_terms | terms_id | 약관별 동의 통계 |

### kyc_verification
| 인덱스명 | 컬럼 | 용도 |
|---------|------|------|
| PRIMARY | id | PK |
| idx_kyc_member | member_id | 회원별 KYC 조회 |
| idx_kyc_status | status | 상태별 조회 (심사 대기 목록 등) |

## Redis 키 구조

### Refresh Token
```
Key: refresh_token:{uuid}
Value: {
  "id": "uuid",
  "memberId": 12345,
  "tokenValue": "jwt-token-value",
  "deviceInfo": "iPhone 15",
  "ipAddress": "192.168.1.1"
}
TTL: 7 days
```

### Token Blacklist
```
Key: token:blacklist:{jwt-token-value}
Value: "logout"
TTL: 토큰의 남은 유효시간
```

## 데이터 정합성

### 제약조건
- member.ci: UNIQUE (중복 가입 방지)
- terms.terms_code + terms.version: UNIQUE (동일 버전 중복 방지)
- kyc_verification.retry_count: 최대 3회로 애플리케이션 레벨 제한

### Soft Delete
- 회원 탈퇴: withdrawn_at 기록, 실제 데이터 삭제 안함 (법적 보관 의무)
- 약관 동의 철회: withdrawn_at 기록

## 파티셔닝 고려사항

대용량 데이터 시 고려할 파티셔닝 전략:

### login_history
- 시간 기반 파티셔닝 (월별)
- 3개월 이상 데이터는 아카이빙

### terms_agreement
- member_id 기반 해시 파티셔닝
- 또는 agreed_at 기준 시간 파티셔닝
