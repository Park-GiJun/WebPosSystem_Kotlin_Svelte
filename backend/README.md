# WebPos Backend

Hexagonal Architecture 기반의 WebPos 백엔드 서버입니다.

## 🚀 빠른 시작

### 1. 환경변수 설정

```bash
# .env.example을 복사하여 .env 파일 생성
cp .env.example .env

# .env 파일을 편집하여 실제 값들로 수정
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

## 📋 환경변수 설정

모든 설정은 환경변수로 관리됩니다. `.env` 파일을 사용하거나 시스템 환경변수로 설정할 수 있습니다.

### 필수 설정

| 환경변수 | 설명 | 예시 |
|---------|------|------|
| `DB_USERNAME` | 데이터베이스 사용자명 | `root` |
| `DB_PASSWORD` | 데이터베이스 비밀번호 | `password123` |
| `JWT_SECRET` | JWT 서명용 시크릿 키 (256비트 이상) | `your-super-secret-key...` |

### 선택적 설정

| 환경변수 | 설명 | 기본값 |
|---------|------|--------|
| `APP_NAME` | 애플리케이션 이름 | `webpos` |
| `SERVER_PORT` | 서버 포트 | `8080` |
| `DB_HOST` | 데이터베이스 호스트 | `localhost` |
| `DB_PORT` | 데이터베이스 포트 | `3306` |
| `DB_NAME` | 데이터베이스 이름 | `webpos` |
| `REDIS_HOST` | Redis 호스트 | `localhost` |
| `REDIS_PORT` | Redis 포트 | `6379` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka 서버 주소 | `localhost:9092` |

## 🔧 개발환경 설정

### 전제조건

- Java 21
- MySQL 8.0+
- Redis 6.0+
- Kafka (선택사항)

### IDE 설정

1. IntelliJ IDEA 권장
2. Kotlin 플러그인 활성화
3. EnvFile 플러그인 설치 (`.env` 파일 지원)

### 환경별 프로파일

| 프로파일 | 용도 | 설정 방법 |
|---------|------|----------|
| `local` | 로컬 개발 | `SPRING_PROFILES_ACTIVE=local` |
| `dev` | 개발 서버 | `SPRING_PROFILES_ACTIVE=dev` |
| `staging` | 스테이징 | `SPRING_PROFILES_ACTIVE=staging` |
| `prod` | 프로덕션 | `SPRING_PROFILES_ACTIVE=prod` |

## 🔒 보안 주의사항

### 프로덕션 환경에서 반드시 변경해야 할 값들:

⚠️ **JWT_SECRET**: 최소 256비트의 안전한 랜덤 문자열  
⚠️ **DB_PASSWORD**: 강력한 데이터베이스 비밀번호  
⚠️ **DEFAULT_ADMIN_PASSWORD_HASH**: 기본 관리자 계정 비밀번호 해시  

### 환경변수 보안:

- `.env` 파일은 절대 Git에 커밋하지 마세요
- 프로덕션에서는 시스템 환경변수나 비밀 관리 도구 사용
- 컨테이너 환경에서는 Kubernetes Secrets 사용

## 📖 API 문서

애플리케이션 실행 후:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api-docs

### 🎨 Swagger UI 커스텀 기능

WebPos API 문서는 다양한 편의 기능을 제공합니다:

#### 🚀 기본 기능
- **API 그룹화**: 기능별로 탭으로 구분된 API 그룹
- **다크모드**: 우상단 🌙 버튼으로 다크모드 토글
- **즐겨찾기**: 각 API에 ⭐ 버튼으로 북마크 기능
- **샘플 데이터**: 📝 버튼으로 빠른 테스트 데이터 입력

#### ⌨️ 단축키
- `Ctrl + /`: 도움말 표시
- `Ctrl + E`: 모든 섹션 펼치기
- `Ctrl + R`: 모든 섹션 접기
- `Ctrl + T`: 상단으로 이동
- `Ctrl + F`: 페이지 내 검색

#### 📋 API 그룹
- **🔐 인증 & 권한**: 로그인, 토큰 관리
- **👥 사용자 관리**: 사용자 계정 관리
- **🏢 조직 관리**: 본사/매장 관리
- **📋 메뉴 & 권한**: 메뉴 및 권한 설정
- **🛍️ 상품 관리**: 상품 및 카테고리
- **💰 POS 시스템**: POS 관련 기능
- **⚙️ 시스템**: 헬스체크 및 모니터링

## 🩺 헬스체크

- **Spring Actuator**: http://localhost:8080/actuator/health
- **Custom Health**: http://localhost:8080/api/v1/health

## 🐳 Docker 실행

```bash
# Docker Compose로 전체 환경 실행
docker-compose up -d

# 애플리케이션만 실행
docker run -p 8080:8080 --env-file .env webpos:latest
```

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "ClassName"
```

## 📦 빌드 및 배포

```bash
# JAR 파일 빌드
./gradlew bootJar

# Docker 이미지 빌드
docker build -t webpos:latest .
```

## 🤝 기여하기

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 라이선스

MIT License - 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.
