-- 데이터베이스 초기화 스크립트

CREATE DATABASE IF NOT EXISTS payment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE payment_db;

-- 가맹점 테이블
CREATE TABLE IF NOT EXISTS merchants (
    merchant_id VARCHAR(20) PRIMARY KEY,
    merchant_name VARCHAR(100) NOT NULL,
    api_key VARCHAR(64) NOT NULL UNIQUE,
    secret_key VARCHAR(128) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    business_number VARCHAR(12),
    representative_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    fee_rate DECIMAL(5, 4),
    settlement_cycle_days INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_merchant_api_key (api_key),
    INDEX idx_merchant_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 결제 테이블
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(32) NOT NULL UNIQUE,
    merchant_id VARCHAR(20) NOT NULL,
    order_id VARCHAR(64) NOT NULL,
    amount DECIMAL(12, 0) NOT NULL,
    canceled_amount DECIMAL(12, 0) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    card_number VARCHAR(20),
    card_company VARCHAR(20),
    installment_months INT,
    approval_number VARCHAR(20),
    approved_at DATETIME,
    van_transaction_id VARCHAR(64),
    product_name VARCHAR(100),
    buyer_name VARCHAR(50),
    buyer_email VARCHAR(100),
    buyer_phone VARCHAR(20),
    fail_reason VARCHAR(500),
    version BIGINT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_payment_merchant_order (merchant_id, order_id),
    INDEX idx_payment_status (status),
    INDEX idx_payment_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 결제 취소 이력 테이블
CREATE TABLE IF NOT EXISTS payment_cancel_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    cancel_transaction_id VARCHAR(32) NOT NULL UNIQUE,
    cancel_amount DECIMAL(12, 0) NOT NULL,
    cancel_reason VARCHAR(200),
    van_cancel_id VARCHAR(64),
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cancel_payment_id (payment_id),
    INDEX idx_cancel_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 정산 테이블
CREATE TABLE IF NOT EXISTS settlements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id VARCHAR(20) NOT NULL,
    settlement_date DATE NOT NULL,
    total_amount DECIMAL(15, 0) NOT NULL,
    total_fee DECIMAL(15, 0) NOT NULL,
    net_amount DECIMAL(15, 0) NOT NULL,
    transaction_count INT NOT NULL,
    cancel_amount DECIMAL(15, 0) NOT NULL,
    cancel_count INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_merchant_date (merchant_id, settlement_date),
    INDEX idx_settlement_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 테스트용 가맹점 데이터
INSERT INTO merchants (merchant_id, merchant_name, api_key, secret_key, status, business_number, fee_rate, settlement_cycle_days)
VALUES
    ('M20231201001', '테스트 쇼핑몰', 'test-api-key-12345678', 'secret-key-12345678', 'ACTIVE', '1234567890', 0.0250, 3),
    ('M20231201002', '샘플 스토어', 'sample-api-key-87654321', 'secret-key-87654321', 'ACTIVE', '9876543210', 0.0230, 5);
