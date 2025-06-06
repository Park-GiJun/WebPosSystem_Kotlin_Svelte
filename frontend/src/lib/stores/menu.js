import { writable } from 'svelte/store';
import { MenuApi } from '$lib/api/menu.js';

function createMenuStore() {
    const { subscribe, set, update } = writable({
        menus: [],
        permissions: [],
        loading: false,
        error: null
    });

    return {
        subscribe,
        
        async loadMenus() {
            update(state => ({ ...state, loading: true, error: null }));
            
            try {
                const data = await MenuApi.getMyMenus();
                update(state => ({
                    ...state,
                    menus: data.menus || [],
                    permissions: data.permissions || [],
                    loading: false
                }));
            } catch (error) {
                update(state => ({
                    ...state,
                    loading: false,
                    error: error.message
                }));
            }
        },

        getSystemMenus(systemCode) {
            return new Promise((resolve) => {
                const unsubscribe = this.subscribe(state => {
                    if (!state.loading && state.menus.length > 0) {
                        const systemMenus = MenuApi.filterMenusBySystem(state.menus, systemCode);
                        const menuTree = MenuApi.buildMenuTree(systemMenus);
                        
                        // 하위 메뉴만 반환 (시스템 카테고리 제외)
                        const childMenus = menuTree.find(menu => menu.menuCode === systemCode)?.children || [];
                        
                        resolve(childMenus);
                        unsubscribe();
                    }
                });
            });
        },

        hasPermission(menuCode, permissionType) {
            return new Promise((resolve) => {
                const unsubscribe = this.subscribe(state => {
                    if (!state.loading) {
                        const permission = state.permissions.find(p => p.menuCode === menuCode);
                        if (!permission) {
                            resolve(false);
                        } else {
                            switch (permissionType.toLowerCase()) {
                                case 'read':
                                    resolve(permission.hasRead);
                                    break;
                                case 'write':
                                    resolve(permission.hasWrite);
                                    break;
                                case 'delete':
                                    resolve(permission.hasDelete);
                                    break;
                                case 'admin':
                                    resolve(permission.hasAdmin);
                                    break;
                                default:
                                    resolve(false);
                            }
                        }
                        unsubscribe();
                    }
                });
            });
        },

        reset() {
            set({
                menus: [],
                permissions: [],
                loading: false,
                error: null
            });
        }
    };
}

export const menuStore = createMenuStore();
