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
      '/api': {
        target: 'https://fixly-meow.ru',
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
