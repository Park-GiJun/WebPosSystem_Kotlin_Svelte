# Redis를 이용한 User Menu Permission 캐싱 시스템

## 개요

이 시스템은 사용자의 메뉴 권한 정보를 Redis에 캐싱하여 성능을 향상시키고, 빈번한 데이터베이스 조회를 줄이는 것을 목표로 합니다.

## 구성 요소

### 1. PermissionCacheRepository

사용자 메뉴 권한 정보의 Redis 캐싱을 담당하는 컴포넌트입니다.

**주요 기능:**
- 사용자 메뉴 권한 캐싱 및 조회
- 메뉴 계층 구조 캐싱
- 사용자 권한 요약 정보 캐싱
- 권한 변경 시 관련 캐시 무효화

**캐시 키 구조:**
```
user:menu:permission:{username}     # 사용자별 메뉴 권한
menu:hierarchy                      # 메뉴 계층 구조
user:permission:summary:{username}  # 사용자 권한 요약
```

**캐시 TTL:**
- 메뉴 권한: 30분
- 메뉴 계층: 1시간
- 권한 요약: 30분

### 2. PermissionService 수정사항

기존 PermissionService에 Redis 캐싱 로직이 추가되었습니다.

**캐싱이 적용된 메서드:**
- `getUserMenus()`: 사용자 메뉴 권한 조회 시 캐시 우선 확인
- `checkUserPermission()`: 권한 체크 시 캐시된 권한 요약 정보 활용
- `grantPermission()`: 권한 부여 시 관련 캐시 무효화
- `revokePermission()`: 권한 취소 시 관련 캐시 무효화

**새로 추가된 메서드:**
- `refreshUserPermissionCache()`: 특정 사용자 캐시 갱신
- `refreshAllPermissionCache()`: 모든 사용자 캐시 갱신
- `refreshMenuRelatedCache()`: 특정 메뉴 관련 캐시 갱신

### 3. PermissionController 캐시 관리 API

캐시를 관리할 수 있는 새로운 API 엔드포인트가 추가되었습니다.

**엔드포인트:**
```
POST /api/v1/permissions/cache/refresh
```

**요청 예시:**
```json
{
  "type": "USER",
  "targetId": "admin"
}
```

**갱신 타입:**
- `USER`: 특정 사용자의 권한 캐시 갱신
- `MENU`: 특정 메뉴와 관련된 모든 캐시 갱신
- `ALL`: 모든 사용자의 권한 캐시 갱신

## 설정

### Redis 설정

**application-local.yml:**
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

**application-prod.yml:**
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}
      lettuce:
        pool:
          max-active: 50
          max-idle: 20
          min-idle: 10
```

### 환경변수 (프로덕션)

```bash
REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password
```

## 캐시 무효화 전략

### 자동 무효화

권한 변경 시 자동으로 관련 캐시가 무효화됩니다:

1. **사용자 권한 변경**: 해당 사용자의 모든 권한 캐시 무효화
2. **역할 권한 변경**: 모든 사용자의 권한 캐시 무효화
3. **메뉴 변경**: 메뉴 관련 모든 캐시 무효화

### 수동 무효화

관리자가 필요에 따라 수동으로 캐시를 갱신할 수 있습니다:

```bash
# 특정 사용자 캐시 갱신
curl -X POST /api/v1/permissions/cache/refresh \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"type": "USER", "targetId": "admin"}'

# 모든 캐시 갱신
curl -X POST /api/v1/permissions/cache/refresh \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"type": "ALL"}'
```

## 성능 최적화

### 캐시 히트율 모니터링

Redis 캐시의 히트율을 모니터링하여 성능을 확인할 수 있습니다:

```bash
redis-cli info stats | grep keyspace_hits
redis-cli info stats | grep keyspace_misses
```

### 메모리 사용량 모니터링

```bash
redis-cli info memory
```

## 트러블슈팅

### 캐시 동기화 문제

권한이 변경되었는데 반영되지 않는 경우:

1. 해당 사용자의 캐시 수동 갱신
2. 로그에서 캐시 무효화 로그 확인
3. Redis 연결 상태 확인

### Redis 연결 실패

Redis 서버가 다운된 경우에도 시스템은 정상 동작하며, 데이터베이스에서 직접 조회합니다.

**확인 방법:**
```bash
# Health check endpoint
curl /actuator/health/redis

# Redis 서버 상태 확인
redis-cli ping
```

## 로깅

캐시 관련 로그는 DEBUG 레벨로 기록됩니다:

```
DEBUG com.gijun.backend.application.service.PermissionService - Retrieved user menu permissions from cache for user: admin
DEBUG com.gijun.backend.application.service.PermissionService - Cached user menu permissions for user: admin
INFO  com.gijun.backend.application.service.PermissionService - Permission granted and related caches invalidated for USER:admin on menu:DASHBOARD
```

## 보안 고려사항

1. **캐시 데이터 암호화**: 민감한 권한 정보는 Redis에 암호화되어 저장됩니다
2. **캐시 접근 제한**: Redis 서버에 대한 접근은 네트워크 레벨에서 제한되어야 합니다
3. **TTL 설정**: 캐시 데이터의 TTL을 적절히 설정하여 보안 위험을 최소화합니다

## 향후 개선사항

1. **분산 캐시 무효화**: 멀티 인스턴스 환경에서의 캐시 동기화
2. **캐시 예열**: 시스템 시작 시 주요 사용자의 권한 정보 미리 캐싱
3. **권한 계산 최적화**: 복잡한 권한 계산 로직의 캐싱
4. **통계 및 모니터링**: 캐시 히트율, 응답시간 등의 메트릭 수집
