// 샘플 데이터 API 클라이언트
import { authStore } from '$lib/stores/auth.js';
import { get } from 'svelte/store';

const API_BASE_URL = 'http://localhost:9832/api/v1/sample';

export class SampleApi {
    /**
     * 샘플 매장 생성
     */
    static async createSampleStore() {
        try {
            const response = await fetch(`${API_BASE_URL}/stores`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            return data.data;
        } catch (error) {
            console.error('Failed to create sample store:', error);
            throw error;
        }
    }

    /**
     * 샘플 상품 생성
     */
    static async createSampleProducts(storeId) {
        try {
            const response = await fetch(`${API_BASE_URL}/stores/${storeId}/products`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            return data.data;
        } catch (error) {
            console.error('Failed to create sample products:', error);
            throw error;
        }
    }

    /**
     * 전체 샘플 데이터 생성 (매장 + 상품)
     */
    static async createAllSampleData() {
        try {
            const response = await fetch(`${API_BASE_URL}/all`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            return data.data;
        } catch (error) {
            console.error('Failed to create sample data:', error);
            throw error;
        }
    }
}
