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
   - 사용자 관리 (생성, 수정, 삭제, 역할 관리)
   - 권한 관리 (메뉴별 권한, 역할별 권한)
   - 시스템 설정
   - 감사 로그

2. **영업정보시스템**
   - 본사 관리
   - 매장 관리 (체인점/개인매장 생성, 관리)
   - POS 시스템 관리 (생성, 설정, 점검)
   - 매출 분석
   - 재고 관리

3. **POS 시스템**
   - 판매 처리 (실시간 장바구니, 결제)
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
- `users` - 사용자 정보 (V2 - 향상된 보안 기능)
- `menus` - 메뉴 구조
- `permissions` - 권한 매핑
- `stores` - 매장 정보 (체인점/개인매장 지원)
- `pos_systems` - POS 시스템
- `headquarters` - 본사 정보
- `user_login_history` - 로그인 이력
- `audit_logs` - 감사 로그

## 🎯 주요 구현 기능

### 완성된 API 엔드포인트

#### 1. 인증 & 사용자 관리
- `POST /api/v1/auth/login` - 로그인
- `POST /api/v1/auth/register` - 회원가입
- `GET /api/v1/auth/me` - 사용자 정보 조회
- `GET /api/v1/admin/users` - 사용자 목록
- `POST /api/v1/admin/users` - 사용자 생성
- `PUT /api/v1/admin/users/{id}` - 사용자 수정
- `DELETE /api/v1/admin/users/{id}` - 사용자 삭제

#### 2. 권한 관리
- `GET /api/v1/permissions/my-menus` - 사용자 메뉴 권한
- `GET /api/v1/admin/permissions/menus` - 메뉴 목록
- `POST /api/v1/admin/permissions/grant` - 권한 부여
- `DELETE /api/v1/admin/permissions/revoke` - 권한 회수

#### 3. 매장 관리
- `GET /api/v1/business/stores` - 매장 목록
- `POST /api/v1/business/stores` - 매장 생성
- `PUT /api/v1/business/stores/{id}` - 매장 수정
- `DELETE /api/v1/business/stores/{id}` - 매장 삭제
- `GET /api/v1/business/stores/regions` - 지역 목록
- `GET /api/v1/business/stores/headquarters` - 본사 목록

#### 4. POS 관리
- `GET /api/v1/business/pos` - POS 목록
- `POST /api/v1/business/pos` - POS 생성
- `PUT /api/v1/business/pos/{id}` - POS 수정
- `DELETE /api/v1/business/pos/{id}` - POS 삭제
- `POST /api/v1/business/pos/{id}/maintenance` - POS 점검 시작
- `POST /api/v1/business/pos/{id}/complete-maintenance` - POS 점검 완료

### 완성된 프론트엔드 페이지

#### 1. 인증 시스템
- 로그인 페이지 (토큰 기반 인증)
- 자동 로그인 상태 확인
- 권한별 접근 제어

#### 2. 슈퍼어드민 시스템
- 사용자 관리 페이지 (목록, 생성, 편집, 삭제)
- 사용자 생성 모달 (역할 선택, 유효성 검사)
- 권한 관리 페이지 (메뉴별, 역할별 권한)
- 통계 대시보드

#### 3. 영업정보시스템
- 매장 관리 페이지 (목록, 필터링, 검색)
- 매장 생성 모달 (체인점/개인매장, 유효성 검사)
- POS 관리 페이지 (목록, 상태 관리)
- POS 생성 모달 (하드웨어 정보, 네트워크 설정)

#### 4. POS 시스템
- 판매 페이지 (상품 선택, 장바구니, 결제)
- 실시간 장바구니 관리
- 카테고리별 상품 필터링

#### 5. 공통 컴포넌트
- 모달 시스템
- 토스트 알림
- 로딩 스피너
- 탭 기반 네비게이션
- 반응형 사이드바

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
- **State Management**: Svelte Stores

## 🎨 UI/UX 특징

- **현대적인 디자인**: 깔끔하고 직관적인 인터페이스
- **반응형**: 모바일/태블릿/데스크톱 지원
- **다크모드 준비**: 향후 다크모드 지원 가능
- **접근성**: WCAG 2.1 가이드라인 준수
- **탭 기반 네비게이션**: 멀티태스킹 지원
- **실시간 피드백**: 토스트 알림, 로딩 상태
- **폼 유효성 검사**: 실시간 입력 검증

## 🔒 보안 특징

- **JWT 기반 인증**
- **역할 기반 접근 제어 (RBAC)**
- **메뉴별 세밀한 권한 제어**
- **API 레벨 권한 검증**
- **감사 로그**
- **세션 관리**
- **CORS 설정**
- **입력 데이터 유효성 검사**

## 📈 개발 진행 상황

### ✅ 완료된 기능
- [x] 사용자 인증 시스템
- [x] 권한 관리 시스템
- [x] 사용자 관리 (CRUD)
- [x] 매장 관리 (생성, 조회, 삭제)
- [x] POS 시스템 관리
- [x] 판매 시스템 (기본)
- [x] 반응형 UI
- [x] 탭 기반 네비게이션

### 🚧 진행 중인 기능
- [ ] 사용자 편집 기능
- [ ] 매장 편집 기능
- [ ] POS 편집 기능
- [ ] 권한 할당 UI
- [ ] 본사 관리 시스템

### 📋 향후 개발 예정
- [ ] 매출 분석 대시보드
- [ ] 재고 관리 시스템
- [ ] 고객 관리 시스템
- [ ] 상품 관리 시스템
- [ ] 실시간 알림 시스템
- [ ] 다크모드 지원
- [ ] 모바일 앱 지원

## 🚀 배포 가이드

### Docker를 이용한 배포

```bash
# 전체 시스템 실행
docker-compose up -d

# 백엔드만 실행
cd backend
docker build -t webpos-backend .
docker run -p 8080:8080 --env-file .env webpos-backend

# 프론트엔드 빌드
cd frontend
npm run build
```

### 환경별 설정

- **개발환경**: `SPRING_PROFILES_ACTIVE=local`
- **스테이징**: `SPRING_PROFILES_ACTIVE=dev`
- **프로덕션**: `SPRING_PROFILES_ACTIVE=prod`

## 📝 라이선스

MIT License

## 🤝 기여하기

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Create a Pull Request

## 📞 지원

질문이나 문제가 있으시면 이슈를 생성해주세요.

## 🎯 데모 계정

```
아이디: admin
비밀번호: admin123
역할: SUPER_ADMIN
```

## 📷 스크린샷

### 로그인 페이지
- 깔끔한 로그인 인터페이스
- JWT 토큰 기반 인증

### 대시보드
- 실시간 통계 카드
- 역할별 맞춤 정보

### 사용자 관리
- 사용자 목록 테이블
- 역할별 필터링
- 사용자 생성 모달

### 매장 관리
- 매장 카드 레이아웃
- 체인점/개인매장 구분
- 지역별 필터링

### POS 판매
- 실시간 장바구니
- 카테고리별 상품 분류
- 터치 친화적 UI
