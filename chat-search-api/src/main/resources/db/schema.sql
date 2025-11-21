-- ChatSearch Platform 데이터베이스 스키마
-- PostgreSQL 15+

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    display_name VARCHAR(100),
    profile_image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);

-- 채팅방 테이블
CREATE TABLE IF NOT EXISTS chat_rooms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    room_type VARCHAR(20) NOT NULL,
    owner_id BIGINT NOT NULL,
    member_count INTEGER NOT NULL DEFAULT 0,
    last_message_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE INDEX idx_room_owner ON chat_rooms(owner_id);
CREATE INDEX idx_room_type ON chat_rooms(room_type);
CREATE INDEX idx_room_created ON chat_rooms(created_at);

-- 메시지 테이블 (기본 - 샤딩되지 않은 버전)
-- 실제 프로덕션에서는 message_0_202401, message_1_202401 등으로 분할
CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    file_url VARCHAR(1000),
    file_name VARCHAR(255),
    file_size BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    parent_message_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES chat_rooms(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

CREATE INDEX idx_msg_room_created ON messages(room_id, created_at);
CREATE INDEX idx_msg_sender_created ON messages(sender_id, created_at);
CREATE INDEX idx_msg_created ON messages(created_at);

-- 샤딩된 메시지 테이블 생성 함수 (예시)
-- 실제로는 애플리케이션 레벨에서 동적으로 생성
CREATE OR REPLACE FUNCTION create_message_shard(
    shard_number INT,
    month_key VARCHAR(6)
) RETURNS VOID AS $$
DECLARE
    table_name VARCHAR(50);
BEGIN
    table_name := 'messages_' || shard_number || '_' || month_key;

    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I (
            LIKE messages INCLUDING ALL
        ) INHERITS (messages)
    ', table_name);

    EXECUTE format('
        CREATE INDEX IF NOT EXISTS idx_%I_room_created
        ON %I(room_id, created_at)
    ', table_name, table_name);

    EXECUTE format('
        CREATE INDEX IF NOT EXISTS idx_%I_sender_created
        ON %I(sender_id, created_at)
    ', table_name, table_name);
END;
$$ LANGUAGE plpgsql;

-- 샘플 데이터 삽입 (개발/테스트용)
INSERT INTO users (email, username, password, display_name, status) VALUES
('user1@example.com', 'user1', '$2a$10$dummyhashedpassword1', '사용자1', 'ACTIVE'),
('user2@example.com', 'user2', '$2a$10$dummyhashedpassword2', '사용자2', 'ACTIVE'),
('user3@example.com', 'user3', '$2a$10$dummyhashedpassword3', '사용자3', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;

INSERT INTO chat_rooms (name, room_type, owner_id, member_count, status) VALUES
('개발팀 채팅방', 'GROUP', 1, 3, 'ACTIVE'),
('프로젝트 논의', 'GROUP', 1, 2, 'ACTIVE'),
('1:1 대화', 'DIRECT', 1, 2, 'ACTIVE')
ON CONFLICT DO NOTHING;
