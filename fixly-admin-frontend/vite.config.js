import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  server: {
    host: true,
    port: Number(process.env.PORT || process.env.VITE_PORT || 5174),
    allowedHosts: true,
    proxy: {
      '/auth-service': {
        target: 'https://fixly-meow.ru:8092',
        changeOrigin: true,
        secure: false,
      },
      '/admin-service': {
        target: 'https://fixly-meow.ru:8092',
        changeOrigin: true,
        secure: false,
      },
      '/user-service': {
        target: 'https://fixly-meow.ru:8092',
        changeOrigin: true,
        secure: false,
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
