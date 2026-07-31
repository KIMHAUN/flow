# Frontend

React + Vite 기반 파일 확장자 차단 정책 관리 UI

## 기술 스택

- React 19
- Vite
- Fetch API

## 로컬 실행

백엔드 서버(`http://localhost:8080`)가 먼저 실행되어 있어야 한다.

```bash
npm install
npm run dev
```

`http://localhost:5173`에서 실행된다.

로컬에서는 Vite 프록시(`/api` → `http://localhost:8080`)를 통해 백엔드와 통신하므로 별도 환경변수 설정이 필요 없다.

## 빌드

```bash
npm run build
```

`dist/` 폴더에 정적 파일이 생성된다.

## 배포

Render Static Site로 배포한다. `render.yaml` 참고.

환경변수 설정 필요:

| 키 | 설명 |
|----|------|
| VITE_API_URL | 백엔드 서버 URL (예: `https://your-backend.onrender.com`) |
