import { writable } from 'svelte/store';

function createToastStore() {
  const { subscribe, update } = writable([]);

  return {
    subscribe,
    
    add(toast) {
      const id = Date.now() + Math.random();
      const newToast = {
        id,
        type: 'info',
        duration: 5000,
        closeable: true,
        ...toast
      };

      update(toasts => [...toasts, newToast]);

      // 자동 제거
      if (newToast.duration > 0) {
        setTimeout(() => {
          this.remove(id);
        }, newToast.duration);
      }

      return id;
    },

    remove(id) {
      update(toasts => toasts.filter(t => t.id !== id));
    },

    clear() {
      update(() => []);
    },

    // 편의 메서드들
    success(message, options = {}) {
      return this.add({ type: 'success', message, ...options });
    },

    error(message, options = {}) {
      return this.add({ type: 'error', message, duration: 0, ...options });
    },

    warning(message, options = {}) {
      return this.add({ type: 'warning', message, ...options });
    },

    info(message, options = {}) {
      return this.add({ type: 'info', message, ...options });
    }
  };
}

export const toastStore = createToastStore();
