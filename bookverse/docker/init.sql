-- 데이터베이스 초기 설정
USE bookverse;

-- 카테고리 초기 데이터
INSERT INTO categories (name, description, created_at, updated_at) VALUES
('소설', '국내외 소설', NOW(), NOW()),
('경제/경영', '경제 및 경영 도서', NOW(), NOW()),
('자기계발', '자기계발 도서', NOW(), NOW()),
('IT/컴퓨터', 'IT 및 프로그래밍 도서', NOW(), NOW()),
('인문/사회', '인문학 및 사회과학 도서', NOW(), NOW());

-- 관리자 계정 (비밀번호: Admin123!)
-- BCrypt 해시는 실제 애플리케이션에서 생성됨
INSERT INTO users (email, password, name, phone_number, role, status, created_at, updated_at) VALUES
('admin@bookverse.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '관리자', '010-1234-5678', 'ROLE_ADMIN', 'ACTIVE', NOW(), NOW());
