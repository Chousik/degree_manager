import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

const port = Number(process.env.PORT || process.env.VITE_PORT || 5173);

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    host: true,
    port,
    allowedHosts: true,
    proxy: {
      '/auth-service': {
        target: 'https://fixly-meow.ru:8092',
        changeOrigin: true,
        secure: false,
      },
      '/user-service': {
        target: 'https://fixly-meow.ru:8092',
        changeOrigin: true,
        secure: false,
      },
      '/admin-service': {
        target: 'https://fixly-meow.ru:8092',
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
