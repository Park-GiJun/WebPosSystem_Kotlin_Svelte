// 관리자 관련 API 클라이언트
import { browser } from '$app/environment';

const API_BASE = browser ? '' : 'http://localhost:8080';

async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE}/api/v1${endpoint}`;
    
    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        },
        ...options
    };

    console.log('🌐 API 요청:', {
        url,
        method: config.method || 'GET',
        headers: config.headers
    });

    const response = await fetch(url, config);
    
    console.log('📡 API 응답:', {
        status: response.status,
        statusText: response.statusText,
        headers: Object.fromEntries(response.headers.entries())
    });
    
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        console.error('❌ API 에러 응답:', errorData);
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    console.log('✅ API 성공 응답:', data);
    return data;
}

// 사용자 관리 API
export const userApi = {
    // 사용자 목록 조회
    async getUsers(params = {}, token) {
        const queryParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                queryParams.append(key, value);
            }
        });
        
        const queryString = queryParams.toString();
        const endpoint = `/admin/users${queryString ? `?${queryString}` : ''}`;
        
        return await apiRequest(endpoint, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // 사용자 생성
    async createUser(userData, token) {
        return await apiRequest('/admin/users', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(userData)
        });
    },

    // 사용자 수정
    async updateUser(userId, userData, token) {
        return await apiRequest(`/admin/users/${userId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(userData)
        });
    },

    // 사용자 삭제
    async deleteUser(userId, token) {
        return await apiRequest(`/admin/users/${userId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // 사용자 잠금 해제
    async unlockUser(userId, token) {
        return await apiRequest(`/admin/users/${userId}/unlock`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // 역할 할당
    async assignRoles(userId, roles, token) {
        return await apiRequest(`/admin/users/${userId}/roles`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ roles })
        });
    },

    // 조직별 사용자 조회
    async getUsersByOrganization(params = {}, token) {
        const queryParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                queryParams.append(key, value);
            }
        });
        
        const queryString = queryParams.toString();
        const endpoint = `/admin/users/by-organization${queryString ? `?${queryString}` : ''}`;
        
        return await apiRequest(endpoint, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // 역할별 사용자 조회
    async getUsersByRole(params = {}, token) {
        const queryParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                queryParams.append(key, value);
            }
        });
        
        const queryString = queryParams.toString();
        const endpoint = `/admin/users/by-role${queryString ? `?${queryString}` : ''}`;
        
        return await apiRequest(endpoint, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    }
};

// 권한 관리 API
export const permissionApi = {
    // 권한 목록 조회
    async getPermissions(token) {
        return await apiRequest('/admin/permissions', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // 사용자 권한 조회
    async getUserPermissions(userId, token) {
        return await apiRequest(`/admin/permissions/user/${userId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // 조직 권한 부여
    async grantOrganizationPermission(data, token) {
        return await apiRequest('/admin/permissions/organization', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(data)
        });
    },

    // 캐시 무효화
    async invalidateCache(token) {
        return await apiRequest('/admin/permissions/cache/invalidate', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    }
};

// 조직 관리 API
export const organizationApi = {
    // 조직 목록 조회
    async getOrganizations(params = {}, token) {
        const queryParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                queryParams.append(key, value);
            }
        });
        
        const queryString = queryParams.toString();
        const endpoint = `/admin/organizations${queryString ? `?${queryString}` : ''}`;
        
        return await apiRequest(endpoint, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // 조직 생성
    async createOrganization(orgData, token) {
        return await apiRequest('/admin/organizations', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(orgData)
        });
    },

    // 조직 수정
    async updateOrganization(orgId, orgData, token) {
        return await apiRequest(`/admin/organizations/${orgId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(orgData)
        });
    },

    // 조직 삭제
    async deleteOrganization(orgId, token) {
        return await apiRequest(`/admin/organizations/${orgId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    }
};

// 감사 로그 API
export const auditLogApi = {
    // 감사 로그 목록 조회
    async getAuditLogs(params = {}, token) {
        const queryParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                queryParams.append(key, value);
            }
        });
        
        const queryString = queryParams.toString();
        const endpoint = `/admin/audit${queryString ? `?${queryString}` : ''}`;
        
        return await apiRequest(endpoint, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // 감사 로그 상세 조회
    async getAuditLogById(id, token) {
        return await apiRequest(`/admin/audit/${id}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // 특정 레코드 변경 이력 조회
    async getRecordHistory(tableName, recordId, token) {
        return await apiRequest(`/admin/audit/record/${tableName}/${recordId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // 감사 로그 통계 조회
    async getStatistics(days = 7, token) {
        return await apiRequest(`/admin/audit/statistics?days=${days}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    }
};
