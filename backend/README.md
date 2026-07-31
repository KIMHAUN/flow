# Backend

Spring Boot 기반 파일 확장자 차단 정책 관리 API 서버

## 기술 스택

- Java 17
- Spring Boot 3.4.5
- Spring Data JPA
- PostgreSQL (Neon)

## 로컬 실행

### 1. 환경변수 설정

`.env.example`을 참고하여 `.env` 파일을 생성한다.

```
DB_URL=jdbc:postgresql://<host>/<dbname>?sslmode=require
DB_USERNAME=<username>
DB_PASSWORD=<password>
```

### 2. DB 스키마 초기화

`src/main/resources/db/schema.sql`을 PostgreSQL에 실행한다.

```sql
-- 테이블 생성 및 고정 확장자 초기 데이터 삽입
```

### 3. 서버 실행

IDE에서 실행하는 경우 `.vscode/launch.json`에 환경변수가 설정되어 있으므로 F5로 실행한다.

터미널에서 실행하는 경우 환경변수를 직접 설정한다.

```bash
export DB_URL=...
export DB_USERNAME=...
export DB_PASSWORD=...
./mvnw spring-boot:run
```

서버는 `http://localhost:8080`에서 실행된다.

## 빌드

```bash
./mvnw clean package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

## API

| Method | URL | 설명 |
|--------|-----|------|
| GET | /api/extensions | 전체 확장자 목록 조회 |
| PATCH | /api/extensions/fixed/{extension} | 고정 확장자 차단 여부 변경 |
| POST | /api/extensions/custom | 커스텀 확장자 추가 |
| DELETE | /api/extensions/custom/{extension} | 커스텀 확장자 삭제 |
| POST | /api/upload | 파일 업로드 (확장자 검사) |

## DB 스키마

```sql
CREATE TABLE IF NOT EXISTS blocked_extension (
    id          BIGSERIAL,
    extension   VARCHAR(20)  NOT NULL,
    type        VARCHAR(10)  NOT NULL,
    is_blocked  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_blocked_extension PRIMARY KEY (id),
    CONSTRAINT chk_type CHECK (type IN ('FIXED', 'CUSTOM'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_extension_lower ON blocked_extension (LOWER(extension));
```

## 배포

Render Web Service로 배포한다. `render.yaml` 참고.

환경변수 설정 필요:

| 키 | 설명 |
|----|------|
| DB_URL | PostgreSQL 접속 URL |
| DB_USERNAME | DB 사용자명 |
| DB_PASSWORD | DB 비밀번호 |
| CORS_ALLOWED_ORIGINS | 허용할 Frontend URL |
