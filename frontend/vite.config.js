import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
	plugins: [sveltekit()],
	server: {
		port: 8080,
		host: true,
		proxy: {
			'/api': {
				target: 'http://localhost:9832',
				changeOrigin: true
			}
		}
	},
	preview: {
		port: 8080,
		host: true,
		allowedHosts: ['gijun.net', 'localhost']
	},
	ssr: {
		noExternal: ['lucide-svelte']
	},
	optimizeDeps: {
		include: ['lucide-svelte'],
		exclude: []
	},
	build: {
		target: 'esnext',
		sourcemap: false
	}
});
