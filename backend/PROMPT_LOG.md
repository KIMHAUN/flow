# PROMPT_LOG.md

## AI 활용 개요

이번 과제에서는 ChatGPT를 주된 프롬프팅 도구로 사용하고, Amazon Q Developer를 코드 생성 및 구현 보조 도구로 활용했다. 두 AI의 응답을 교차 검증하여 한쪽의 제안이 다른 쪽에서 다르게 평가되는 경우 실제 요구사항과 구현 결과를 기준으로 최종 판단했다.

AI가 제안한 내용을 그대로 적용하기보다 실제 요구사항과 구현 범위를 기준으로 적용 여부를 판단하고, 필요한 경우 추가 질문을 통해 결과를 구체화했다.

---

## 1. 요구사항 분석

### Prompt

> 이 과제는 확장자 제한 기능으로 보이는데, "무엇을 고려했고 왜 그렇게 판단했는가"의 깊이를 평가한다고 한다. 단순 CRUD 외에 어떤 부분을 고려해야 할까?

### 의도

기능 목록을 바로 구현하기 전에 요구사항에서 명시적으로 요구하지 않은 보안·운영·예외 상황을 먼저 파악하고자 했다.

### 결과 / 판단

AI가 서버 검증, 입력값 정규화, 동시성, 파일 내용 검증 등의 관점을 제안했다.

이 중 과제 요구사항과 직접 관련된 항목은 설계에 반영하고, 구현 비용이 큰 항목은 `CONSIDERATIONS.md`에 고려사항으로 남기기로 결정했다.

---

## 2. 파일 확장자 검증 범위 탐색

### Prompt

> 복합 확장자 중 차단할 만한 것이 있는가?
>
> Magic Number는 무엇인가?

### 의도

파일명을 기반으로 한 확장자 검사가 실제 보안 관점에서 충분한지 확인하고자 했다.

### 결과 / 판단

확장자는 파일명의 일부일 뿐 실제 파일 형식을 보장하지 않는다는 점을 확인했다.

Magic Number 검사까지 구현하는 방안도 검토했지만 과제의 핵심 요구사항은 확장자 기반 정책 관리이므로 현재 구현에서는 제외했다.

대신 실제 운영 환경에서는 Apache Tika 등의 파일 시그니처 검사를 추가할 수 있음을 `CONSIDERATIONS.md`에 문서화했다.

---

## 3. 요구사항 구조화 및 설계

### Prompt

> 요구사항을 기능 요구사항과 비기능 요구사항으로 나누고, 구현 전에 결정해야 할 정책을 정리해줘.

### 의도

바로 코드를 생성하기보다 요구사항을 기능/보안/운영 관점으로 분해하여 구현 범위를 먼저 결정하고자 했다.

### 결과 / 판단

다음 영역으로 문제를 분리했다.

- 확장자 정책 관리
- 파일 업로드
- 입력값 검증
- DB 정합성
- 예외 처리
- 운영/보안

이를 기준으로 Backend Service와 API 구조를 설계했다.

---

## 4. DB Schema 검토

### Prompt

> 다음 PostgreSQL schema를 과제 요구사항과 동시성/데이터 무결성 관점에서 평가해줘.
>
> [blocked_extension schema]

### 의도

애플리케이션 검증만으로 데이터 무결성을 보장하지 않고 DB 레벨에서도 방어할 필요가 있는지 검토했다.

### 결과 / 판단

애플리케이션에서 extension을 소문자로 정규화하면서 DB에서도 중복을 방지하도록 Unique 제약을 적용했다.

추가로 `LOWER(extension)` 기반 Unique Index를 사용해 직접 DB 접근이나 애플리케이션 버그 상황에서도 중복을 방어하도록 했다.

---

## 5. 동시성 검토

### Prompt

> 커스텀 확장자를 최대 200개로 제한할 때 트랜잭션과 잠금으로 동시성까지 제어하는 것이 필요한가?

### 의도

`count → insert` 사이에 동시에 요청이 발생할 경우 200개 제한을 초과할 가능성이 있는지 검토했다.

### 결과 / 판단

Race Condition이 발생할 수 있음을 확인했다.

과제 규모와 실제 트래픽을 고려하여 구현 여부를 검토했고, 최종적으로 `@Lock(PESSIMISTIC_WRITE)`를 사용한 `countCustomForUpdate()`로 비관적 잠금을 적용하여 count와 insert 사이에 다른 요청이 끼어들지 못하도록 구현했다.

---

## 6. 파일 저장 방식 검토

### Prompt

> 업로드 파일을 서버 폴더에 저장할 때 원본 파일명을 그대로 사용하는 것이 안전한가?

### 의도

단순히 업로드 성공 여부뿐 아니라 서버 파일 시스템에 저장할 때 발생할 수 있는 위험을 검토했다.

### 결과 / 판단

Path Traversal과 파일명 충돌 가능성을 확인했다.

초기에는 `UUID + 원래 확장자` 형태로 저장하도록 구현했으나, 이후 파일 저장 자체가 과제 핵심 요구사항이 아님을 판단하여 저장 없이 확장자 검사만 수행하는 방식으로 변경했다.

---

## 7. 업로드 목록 DB 저장 여부

### Prompt

> 업로드한 파일이 upload 폴더에 들어가고 있는데 이 목록도 DB에 저장해서 보여주는 것이 좋을까?

### 의도

추가 기능이 과제 완성도를 높이는지, 불필요하게 범위만 증가시키는지 판단하고자 했다.

### 결과 / 판단

업로드 이력은 요구사항에 없으며 이를 추가하면 별도 Entity/API/UI까지 필요해진다.

YAGNI 관점에서 구현하지 않고 핵심 요구사항인 정책 관리와 실제 업로드 차단에 집중하기로 했다.

---

## 8. 배포 및 Secret 관리

### Prompt

> Neon PostgreSQL 주소에 비밀번호가 포함되어 있는데 GitHub에 코드를 제출해야 할 경우 어떻게 관리해야 하나?

### 의도

공개 Repository와 배포 환경에서 DB Credential이 노출되지 않도록 구성하기 위해 확인했다.

### 결과 / 판단

DB 접속 정보는 환경변수로 분리했다.

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

`.env`는 `.gitignore`에 추가하고 실제 배포 환경에서는 Render Environment Variable을 사용했다.

---

## 9. 배포 장애 해결

### Prompt

> Render에서 `./mvnw: No such file or directory`, `JAVA_HOME is not defined` 오류가 발생한다. Spring Boot의 Build/Start Command를 어떻게 구성해야 하나?

### 의도

로컬에서 정상 동작한 애플리케이션이 배포 환경에서 실패한 원인을 확인하고자 했다.

### 결과 / 판단

Root Directory 미설정과 Java Runtime 미지정 문제임을 확인했다.

`render.yaml`에 `runtime: java`를 명시하여 Render가 Java 환경으로 빌드하도록 구성했다.

```yaml
runtime: java
buildCommand: ./mvnw clean package -DskipTests
startCommand: java -jar target/backend-0.0.1-SNAPSHOT.jar
```

---

## 10. Frontend / Backend 연동 문제 해결

### Prompt

> 배포 후 CORS 오류가 발생한다. 요청 URL이 `//api/extensions`로 호출되고 있다.

### 의도

배포된 React와 Spring Boot 간 API 호출 실패 원인을 분석하고자 했다.

### 결과 / 판단

두 가지 문제가 동시에 존재했다.

1. `VITE_API_URL` 환경변수 미설정 시 `/api` 앞에 빈 문자열이 붙어 `//api` 경로가 생성됨
2. Backend CORS 설정에 운영 Frontend Origin 미포함

API Base URL을 `import.meta.env.VITE_API_URL || ''` 방식으로 수정하고, CORS를 환경변수(`CORS_ALLOWED_ORIGINS`)로 분리하여 운영/로컬 환경을 구분했다.

---

## 11. 추가적인 운영 Edge Case 탐색

### Prompt

> 과제에서 이미 제시한 고려사항 말고 실제 운영에서 발생할 수 있는 다른 문제는 무엇이 있을까?

### 의도

과제 문서에 나열된 예시를 단순히 따라가는 것이 아니라 요구사항 밖의 문제를 직접 탐색하고자 했다.

### 결과 / 판단

다음 상황을 추가로 검토했다.

- 업로드 처리 중 정책 변경 → 요청 시점의 정책 기준으로 처리되도록 구현
- `.env`, `.gitignore` 같은 Hidden File → 확장자 없는 파일로 처리, 현재 정책상 허용
- 동시 요청에 의한 200개 제한 Race Condition → 비관적 잠금으로 해결
- 이중 확장자(`file.exe.txt`) → 마지막 확장자 기준으로 처리, 판단 근거를 `CONSIDERATIONS.md`에 기록

---

## 사용 도구 / 라이브러리

| 도구 | 사용 목적 |
|---|---|
| ChatGPT | 요구사항 분석, Edge Case 탐색, 설계 검토, 고려사항 문서화 (주 프롬프팅 도구) |
| Amazon Q Developer | 코드 생성, 구현 보조, 배포 오류 분석 (교차 검증 도구) |
| Spring Boot | REST API 및 서버 사이드 파일 검증 |
| Spring Data JPA | 확장자 정책 영속성 관리 |
| PostgreSQL / Neon | 확장자 정책 데이터 저장 |
| React + Vite | 정책 관리 및 파일 업로드 UI |
| Fetch API | Frontend-Backend API 통신 |
| Render | Frontend/Backend 배포 |
| Git / GitHub | 버전 관리 및 과제 제출 |

별도의 MCP 또는 AI Agent Plugin은 사용하지 않았다.

---

## 판단 근거 회고

AI는 특히 요구사항에서 바로 드러나지 않는 보안·운영 Edge Case를 탐색하고, 배포 과정에서 발생한 오류의 원인을 좁히는 데 유용했다.

### 그대로 활용한 부분

- Secret을 환경변수로 분리하는 방식
- 서버 사이드 확장자 검증
- CORS 환경 분리
- `LOWER(extension)` Unique Index로 DB 이중 방어

일반적인 Backend/Deployment 관행과 일치하고 현재 요구사항에도 적합하다고 판단했다.

### 수정해서 활용한 부분

AI는 UUID 기반 파일 저장, Magic Number 검사, Audit Log, Redis Cache 등을 제안했다.

파일 저장은 초기에 구현했으나 과제 핵심이 정책 강제 여부임을 판단하여 저장 없이 확장자 검사만 수행하는 방식으로 변경했다. 나머지는 구현 비용 대비 과제 범위에 맞지 않아 `CONSIDERATIONS.md`에 향후 개선사항으로 기록했다.

동시성 처리는 AI가 Optimistic Lock과 SERIALIZABLE Transaction을 제안했으나, 단순하고 확실한 비관적 잠금(`PESSIMISTIC_WRITE`) 방식을 선택했다.

### 사용하지 않은 부분

업로드 파일 목록을 별도 DB 테이블로 관리하는 방안도 검토했지만, 과제의 핵심은 파일 관리 시스템이 아니라 **확장자 정책이 실제 업로드에서 강제되는지**라고 판단하여 제외했다.

### AI 결과를 검증하면서 발견한 점

ChatGPT와 Amazon Q Developer의 응답을 교차 검증하는 방식을 사용했다. ChatGPT로 설계 방향과 고려사항을 탐색하고, Amazon Q Developer로 실제 코드를 생성한 뒤 두 결과가 다를 경우 실제 동작과 요구사항을 기준으로 판단했다.

예를 들어 동시성 처리에서 ChatGPT는 Optimistic Lock을 우선 제안했고 Amazon Q Developer는 비관적 잠금을 적용했는데, 과제 규모와 단순성을 고려하여 비관적 잠금을 최종 선택했다.

배포 과정에서는 단순히 Build Command를 변경하는 것만으로 해결되지 않았고, 실제 Render 로그에서 Node Runtime으로 실행되고 있음을 확인한 뒤 `render.yaml`로 Java Runtime을 명시하는 방식으로 전환했다.

결과적으로 AI는 최종 의사결정자가 아니라 **문제 탐색 → 대안 제시 → 검증** 과정의 보조 도구로 사용했다.
