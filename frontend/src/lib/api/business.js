// 비즈니스 관련 API 클라이언트
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

    const response = await fetch(url, config);
    
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
}

// 매장 관리 API
export const storeApi = {
    // 매장 목록 조회
    async getStores(params = {}, token) {
        const queryParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                queryParams.append(key, value);
            }
        });
        
        const queryString = queryParams.toString();
        const endpoint = `/business/stores${queryString ? `?${queryString}` : ''}`;
        
        return await apiRequest(endpoint, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // 매장 상세 조회
    async getStore(storeId, token) {
        return await apiRequest(`/business/stores/${storeId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // 매장 생성
    async createStore(storeData, token) {
        return await apiRequest('/business/stores', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(storeData)
        });
    },

    // 매장 수정
    async updateStore(storeId, storeData, token) {
        return await apiRequest(`/business/stores/${storeId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(storeData)
        });
    },

    // 매장 삭제
    async deleteStore(storeId, token) {
        return await apiRequest(`/business/stores/${storeId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // 지역 목록 조회
    async getRegions(token) {
        return await apiRequest('/business/stores/regions', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // 본사 목록 조회
    async getHeadquarters(token) {
        return await apiRequest('/business/stores/headquarters', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    }
};

// POS 관리 API
export const posApi = {
    // POS 목록 조회
    async getPosSystems(params = {}, token) {
        const queryParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                queryParams.append(key, value);
            }
        });
        
        const queryString = queryParams.toString();
        const endpoint = `/business/pos${queryString ? `?${queryString}` : ''}`;
        
        return await apiRequest(endpoint, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // POS 생성
    async createPosSystem(posData, token) {
        return await apiRequest('/business/pos', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(posData)
        });
    },

    // POS 점검 시작
    async startMaintenance(posId, token) {
        return await apiRequest(`/business/pos/${posId}/maintenance`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // POS 점검 완료
    async completeMaintenance(posId, token) {
        return await apiRequest(`/business/pos/${posId}/complete-maintenance`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    }
};

// 대시보드 API
export const dashboardApi = {
    // 대시보드 통계 조회
    async getStats(token) {
        return await apiRequest('/dashboard/stats', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    }
};
