-- 확장자 차단 정책 테이블
CREATE TABLE IF NOT EXISTS blocked_extension (
    id          BIGSERIAL,
    extension   VARCHAR(20)     NOT NULL,
    type        VARCHAR(10)     NOT NULL,
    is_blocked  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_blocked_extension PRIMARY KEY (id),
    CONSTRAINT chk_type CHECK (type IN ('FIXED', 'CUSTOM'))
);

-- 대소문자 구분 없는 중복 방지: 애플리케이션에서 소문자로 정규화하지만 DB에서도 방어
CREATE UNIQUE INDEX IF NOT EXISTS uq_extension_lower ON blocked_extension (LOWER(extension));

-- 고정 확장자 초기 데이터 (기본 uncheck 상태)
INSERT INTO blocked_extension (extension, type, is_blocked)
VALUES
    ('bat', 'FIXED', false),
    ('cmd', 'FIXED', false),
    ('com', 'FIXED', false),
    ('cpl', 'FIXED', false),
    ('exe', 'FIXED', false),
    ('scr', 'FIXED', false),
    ('js',  'FIXED', false)
ON CONFLICT (LOWER(extension)) DO NOTHING;
