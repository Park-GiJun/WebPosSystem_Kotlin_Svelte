# WebPos 시스템

Hexagonal Architecture 기반의 WebPos 전체 시스템입니다.

## 🏗️ 아키텍처

### 백엔드 (Spring Boot + Kotlin)
- **Hexagonal Architecture** 적용
- **Spring WebFlux** (Reactive)
- **R2DBC MySQL** (Reactive Database)
- **Redis** (캐싱 및 세션)
- **Kafka** (이벤트 스트리밍)
- **JWT** 인증
- **권한 기반 접근 제어**

### 프론트엔드 (Svelte + TailwindCSS)
- **SvelteKit** 프레임워크
- **TailwindCSS** 스타일링
- **Lucide** 아이콘
- **탭 기반 멀티페이지 인터페이스**
- **반응형 디자인**

## 🚀 시작하기

### 1. 백엔드 실행

```bash
cd backend

# 환경 설정
cp .env.example .env
# .env 파일 수정 필요

# 의존성이 있는 서비스들 실행 (Docker Compose)
docker-compose up -d

# 애플리케이션 실행
./gradlew bootRun
```

### 2. 프론트엔드 실행

```bash
cd frontend

# 의존성 설치
npm install

# 개발 서버 실행
npm run dev
```

## 📱 시스템 구성

### 주요 기능 모듈

1. **슈퍼어드민 시스템**
   - 사용자 관리
   - 권한 관리
   - 시스템 설정
   - 감사 로그

2. **영업정보시스템**
   - 본사 관리
   - 매장 관리
   - 매출 분석
   - 재고 관리

3. **POS 시스템**
   - 판매 처리
   - 상품 관리
   - 고객 관리
   - POS 설정

### 권한 관리 시스템

- **4단계 권한**: READ, WRITE, DELETE, ADMIN
- **다차원 권한 부여**:
  - 사용자별 직접 권한
  - 역할(Role) 기반 권한
  - 매장별 권한
  - 본사별 권한
- **메뉴 기반 접근 제어**
- **동적 메뉴 구성**

## 🔐 사용자 역할

| 역할 | 설명 | 기본 권한 |
|------|------|----------|
| `SUPER_ADMIN` | 최고 관리자 | 모든 시스템 접근 |
| `SYSTEM_ADMIN` | 시스템 관리자 | 영업정보, POS 시스템 |
| `HQ_MANAGER` | 본사 관리자 | 영업정보시스템 |
| `STORE_MANAGER` | 매장 관리자 | POS 시스템 |
| `AREA_MANAGER` | 지역 관리자 | 담당 지역 매장들 |
| `USER` | 일반 사용자 | POS 판매 기능만 |

## 🛠️ 개발 환경

### 백엔드 요구사항
- Java 21
- MySQL 8.0+
- Redis 6.0+
- Kafka (선택사항)

### 프론트엔드 요구사항
- Node.js 18+
- npm 또는 yarn

## 📊 데이터베이스 스키마

주요 테이블:
- `users` - 사용자 정보
- `menus` - 메뉴 구조
- `permissions` - 권한 매핑
- `stores` - 매장 정보
- `pos_systems` - POS 시스템
- `headquarters` - 본사 정보

## 🔧 주요 기술 스택

### 백엔드
- **Framework**: Spring Boot 3.3.5
- **Language**: Kotlin 1.9.25
- **Database**: MySQL (R2DBC)
- **Cache**: Redis
- **Message Queue**: Apache Kafka
- **Security**: Spring Security + JWT
- **API Documentation**: SpringDoc OpenAPI

### 프론트엔드
- **Framework**: SvelteKit
- **Styling**: TailwindCSS
- **Icons**: Lucide Svelte
- **Build Tool**: Vite

## 🎨 UI/UX 특징

- **현대적인 디자인**: 깔끔하고 직관적인 인터페이스
- **반응형**: 모바일/태블릿/데스크톱 지원
- **다크모드 준비**: 향후 다크모드 지원 가능
- **접근성**: WCAG 2.1 가이드라인 준수
- **탭 기반 네비게이션**: 멀티태스킹 지원

## 🔒 보안 특징

- **JWT 기반 인증**
- **역할 기반 접근 제어 (RBAC)**
- **메뉴별 세밀한 권한 제어**
- **API 레벨 권한 검증**
- **감사 로그**
- **세션 관리**

## 📝 라이선스

MIT License

## 🤝 기여하기

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📞 지원

질문이나 문제가 있으시면 이슈를 생성해주세요.
