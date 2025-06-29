import { authStore } from '$lib/stores/auth.js';
import { get } from 'svelte/store';

const API_BASE_URL = 'http://localhost:8080/api/v1';

export class ProductApi {
    /**
     * 매장의 판매 가능한 상품 조회 (POS용)
     */
    static async getProductsForPos(storeId) {
        try {
            const auth = get(authStore);
            if (!auth.token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${API_BASE_URL}/products/stores/${storeId}/pos`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${auth.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            return data.data || [];
        } catch (error) {
            console.error('Failed to fetch products for POS:', error);
            throw error;
        }
    }

    /**
     * 매장의 모든 상품 조회
     */
    static async getProductsByStore(storeId, activeOnly = false, availableOnly = false) {
        try {
            const auth = get(authStore);
            if (!auth.token) {
                throw new Error('No authentication token');
            }

            const params = new URLSearchParams();
            if (activeOnly) params.append('activeOnly', 'true');
            if (availableOnly) params.append('availableOnly', 'true');

            const response = await fetch(`${API_BASE_URL}/products/stores/${storeId}?${params}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${auth.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            return data.data || [];
        } catch (error) {
            console.error('Failed to fetch products:', error);
            throw error;
        }
    }

    /**
     * 페이징된 상품 목록 조회
     */
    static async getProductsWithPaging(storeId, page = 0, size = 20, sortBy = 'displayOrder', sortDirection = 'ASC') {
        try {
            const auth = get(authStore);
            if (!auth.token) {
                throw new Error('No authentication token');
            }

            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString(),
                sortBy,
                sortDirection
            });

            const response = await fetch(`${API_BASE_URL}/products/stores/${storeId}/paged?${params}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${auth.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            return data.data || { products: [], totalCount: 0, page: 0, size: 0, totalPages: 0 };
        } catch (error) {
            console.error('Failed to fetch products with paging:', error);
            throw error;
        }
    }

    /**
     * 카테고리별 상품 조회
     */
    static async getProductsByCategory(storeId, category) {
        try {
            const auth = get(authStore);
            if (!auth.token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${API_BASE_URL}/products/stores/${storeId}/categories/${encodeURIComponent(category)}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${auth.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            return data.data || [];
        } catch (error) {
            console.error('Failed to fetch products by category:', error);
            throw error;
        }
    }

    /**
     * 상품 검색
     */
    static async searchProducts(storeId, searchTerm) {
        try {
            const auth = get(authStore);
            if (!auth.token) {
                throw new Error('No authentication token');
            }

            const params = new URLSearchParams({
                searchTerm
            });

            const response = await fetch(`${API_BASE_URL}/products/stores/${storeId}/search?${params}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${auth.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            return data.data || [];
        } catch (error) {
            console.error('Failed to search products:', error);
            throw error;
        }
    }

    /**
     * 바코드로 상품 조회
     */
    static async getProductByBarcode(storeId, barcode) {
        try {
            const auth = get(authStore);
            if (!auth.token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${API_BASE_URL}/products/stores/${storeId}/barcode/${encodeURIComponent(barcode)}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${auth.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            return data.data;
        } catch (error) {
            console.error('Failed to fetch product by barcode:', error);
            throw error;
        }
    }

    /**
     * 상품 생성
     */
    static async createProduct(storeId, productData) {
        try {
            const auth = get(authStore);
            if (!auth.token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${API_BASE_URL}/products/stores/${storeId}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${auth.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(productData)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            return data.data;
        } catch (error) {
            console.error('Failed to create product:', error);
            throw error;
        }
    }

    /**
     * 상품 수정
     */
    static async updateProduct(productId, productData) {
        try {
            const auth = get(authStore);
            if (!auth.token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${API_BASE_URL}/products/${productId}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${auth.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(productData)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            return data.data;
        } catch (error) {
            console.error('Failed to update product:', error);
            throw error;
        }
    }

    /**
     * 상품 삭제
     */
    static async deleteProduct(productId) {
        try {
            const auth = get(authStore);
            if (!auth.token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${API_BASE_URL}/products/${productId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${auth.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return true;
        } catch (error) {
            console.error('Failed to delete product:', error);
            throw error;
        }
    }

    /**
     * 재고 조정
     */
    static async adjustStock(productId, quantity) {
        try {
            const auth = get(authStore);
            if (!auth.token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${API_BASE_URL}/products/${productId}/stock`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${auth.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ quantity })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            return data.data;
        } catch (error) {
            console.error('Failed to adjust stock:', error);
            throw error;
        }
    }

    /**
     * 상품 판매 (재고 차감)
     */
    static async sellProducts(salesItems) {
        try {
            const auth = get(authStore);
            if (!auth.token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${API_BASE_URL}/products/sell`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${auth.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ salesItems })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            return data.data;
        } catch (error) {
            console.error('Failed to sell products:', error);
            throw error;
        }
    }

    /**
     * 재고 부족 상품 조회
     */
    static async getLowStockProducts(storeId) {
        try {
            const auth = get(authStore);
            if (!auth.token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${API_BASE_URL}/products/stores/${storeId}/low-stock`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${auth.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            return data.data || [];
        } catch (error) {
            console.error('Failed to fetch low stock products:', error);
            throw error;
        }
    }
}
