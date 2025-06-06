# WebPos 시스템 구조

## 📊 시스템 개요

WebPos는 본사/매장 중심의 계층적 권한 관리 시스템을 갖춘 POS 솔루션입니다.

## 🏢 조직 구조

### 1. 시스템 (SYSTEM)
- **슈퍼어드민**: 전체 시스템 관리
- **시스템어드민**: 시스템 운영 관리

### 2. 체인본부 (HEADQUARTERS)
- **본사 관리자**: 가맹점/직영점 관리
- **기능**:
  - 가맹점/직영점 생성 및 관리
  - 본사 직원 관리
  - 전체 매장 현황 모니터링

### 3. 개인매장 (INDIVIDUAL_STORE)
- **매장 관리자**: 개인매장 운영
- **기능**:
  - 매장 직원 관리
  - POS 시스템 관리
  - 매장 운영 전반

### 4. 체인매장 (CHAIN_STORE)
- **매장 관리자**: 체인매장 운영
- **특징**: 본사에 소속되어 관리됨
- **기능**: 개인매장과 동일하나 본사 정책 적용

## 🔐 권한 관리 체계

### 권한 레벨
1. **READ**: 읽기 권한
2. **WRITE**: 쓰기 권한 (읽기 포함)
3. **DELETE**: 삭제 권한 (쓰기 포함)
4. **ADMIN**: 관리자 권한 (모든 권한 포함)

### 권한 부여 대상
- **역할(ROLE)**: 사용자 역할별 권한
- **사용자(USER)**: 개별 사용자 권한
- **본사(HEADQUARTERS)**: 본사별 권한
- **매장(STORE)**: 매장별 권한

## 🎯 사용자 역할

### SUPER_ADMIN (최고 관리자)
- **권한**: 모든 시스템 접근
- **기능**:
  - 체인본부 생성
  - 개인매장 생성
  - 시스템 전체 관리
  - 메뉴 권한 관리

### SYSTEM_ADMIN (시스템 관리자)
- **권한**: 영업정보시스템, POS시스템
- **기능**: 시스템 운영 관리

### HQ_MANAGER (본사 관리자)
- **권한**: 영업정보시스템
- **기능**:
  - 가맹점/직영점 생성 및 관리
  - 본사 직원 관리
  - 매장 현황 모니터링

### STORE_MANAGER (매장 관리자)
- **권한**: POS시스템 전체
- **기능**:
  - 매장 직원 관리
  - POS 시스템 관리
  - 매장 운영 관리

### USER (일반 사용자)
- **권한**: POS 판매 기능만
- **기능**: 상품 판매 처리

## 🗂️ 메뉴 구조

### 슈퍼어드민 (ADMIN)
```
📋 슈퍼어드민
├── 👥 사용자 관리
├── 🔑 권한 관리
├── 🏢 조직 관리
└── ⚙️ 시스템 설정
```

### 영업정보시스템 (BUSINESS)
```
💼 영업정보시스템
├── 🏢 본사 관리
├── 🏪 매장 관리
├── 🏪 가맹점 관리 (본사 전용)
├── 📊 매출 분석
└── 📦 재고 관리
```

### POS시스템 (POS)
```
💰 POS시스템
├── 🛒 판매
├── 📦 상품 관리
├── 👥 직원 관리 (매장관리자 전용)
├── 💻 POS 시스템 관리 (매장관리자 전용)
└── ⚙️ POS 설정
```

## 🚀 주요 프로세스

### 1. 조직 생성 프로세스

#### 체인본부 생성 (슈퍼어드민)
1. 본사 정보 입력
2. 본사 관리자 계정 생성
3. 본사별 권한 설정

#### 개인매장 생성 (슈퍼어드민)
1. 매장 정보 입력
2. 매장 관리자 계정 생성
3. 매장별 권한 설정

#### 가맹점/직영점 생성 (본사 관리자)
1. 매장 정보 입력
2. 운영형태 선택 (가맹/직영)
3. 매장 관리자 계정 생성

### 2. 사용자 관리 프로세스

#### 매장 직원 추가 (매장 관리자)
1. 직원 정보 입력
2. 역할 선택 (USER/STORE_MANAGER)
3. 매장 소속으로 계정 생성

#### POS 시스템 추가 (매장 관리자)
1. POS 정보 입력
2. 하드웨어 정보 등록
3. 매장 POS 목록에 추가

## 🔧 기술 스택

### 백엔드
- **Framework**: Spring Boot 3.3.5 + Kotlin
- **Architecture**: Hexagonal Architecture
- **Database**: MySQL (R2DBC)
- **Cache**: Redis
- **Message Queue**: Apache Kafka
- **Security**: Spring Security + JWT

### 프론트엔드
- **Framework**: SvelteKit
- **Styling**: TailwindCSS
- **Icons**: Lucide Svelte
- **State Management**: Svelte Stores

## 📱 주요 화면

### 슈퍼어드민
- `/admin/organizations` - 조직 관리
- `/admin/users` - 사용자 관리
- `/admin/permissions` - 권한 관리

### 본사 관리자
- `/business/headquarters/stores` - 가맹점 관리
- `/business/reports` - 매출 분석

### 매장 관리자
- `/pos/staff` - 직원 관리
- `/pos/system` - POS 시스템 관리
- `/pos/sales` - 판매 처리

### 일반 사용자
- `/pos/sales` - 판매 처리

## 🛡️ 보안 특징

- JWT 기반 인증
- 역할 기반 접근 제어 (RBAC)
- 메뉴별 세밀한 권한 제어
- 조직별 데이터 격리
- API 레벨 권한 검증

## 📊 데이터베이스 구조

### 주요 테이블
- `users` - 사용자 정보 (조직 소속 포함)
- `headquarters` - 본사 정보
- `stores` - 매장 정보
- `pos_systems` - POS 시스템 정보
- `menus` - 메뉴 구조
- `permissions` - 권한 매핑

### 조직 관계
```sql
users.organization_type = 'HEADQUARTERS' AND users.organization_id = headquarters.hq_id
users.organization_type = 'STORE' AND users.organization_id = stores.store_id
stores.hq_id = headquarters.hq_id (체인매장인 경우)
```

## 🔄 권한 상속 구조

1. **시스템 레벨**: 모든 조직에 적용
2. **본사 레벨**: 해당 본사와 소속 매장에 적용
3. **매장 레벨**: 해당 매장에만 적용
4. **사용자 레벨**: 개별 사용자에만 적용

권한은 상위 레벨에서 하위 레벨로 상속되며, 하위 레벨에서 더 제한적인 권한 설정 가능합니다.
