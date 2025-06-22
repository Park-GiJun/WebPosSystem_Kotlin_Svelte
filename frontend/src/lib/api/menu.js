import { authStore } from '$lib/stores/auth.js';
import { get } from 'svelte/store';

const API_BASE_URL = 'http://localhost:9832/api/v1';

export class MenuApi {
    static async getMyMenus() {
        try {
            const auth = get(authStore);
            if (!auth.token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${API_BASE_URL}/permissions/my-menus`, {
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
            return data;
        } catch (error) {
            console.error('Failed to fetch menus:', error);
            throw error;
        }
    }

    static async checkPermission(menuCode, requiredPermission) {
        try {
            const auth = get(authStore);
            if (!auth.token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${API_BASE_URL}/permissions/check`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${auth.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    menuCode,
                    requiredPermission
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            return data.hasPermission;
        } catch (error) {
            console.error('Failed to check permission:', error);
            return false;
        }
    }

    // 메뉴를 계층 구조로 변환
    static buildMenuTree(menus) {
        const menuMap = new Map();
        const rootMenus = [];

        // 먼저 모든 메뉴를 맵에 저장
        menus.forEach(menu => {
            menuMap.set(menu.menuId, { ...menu, children: [] });
        });

        // 부모-자식 관계 설정
        menus.forEach(menu => {
            const menuItem = menuMap.get(menu.menuId);
            if (menu.parentMenuId) {
                const parent = menuMap.get(menu.parentMenuId);
                if (parent) {
                    parent.children.push(menuItem);
                }
            } else {
                rootMenus.push(menuItem);
            }
        });

        // 정렬
        const sortMenus = (menuList) => {
            menuList.sort((a, b) => a.displayOrder - b.displayOrder);
            menuList.forEach(menu => {
                if (menu.children && menu.children.length > 0) {
                    sortMenus(menu.children);
                }
            });
        };

        sortMenus(rootMenus);
        return rootMenus;
    }

    // 특정 시스템의 메뉴만 필터링
    static filterMenusBySystem(menus, systemCode) {
        return menus.filter(menu => {
            if (menu.menuCode === systemCode) {
                return true;
            }
            if (menu.parentMenuId) {
                const parent = menus.find(m => m.menuId === menu.parentMenuId);
                return parent && parent.menuCode === systemCode;
            }
            return false;
        });
    }
}
