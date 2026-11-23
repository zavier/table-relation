import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 这里替换为实际的后端服务地址
        changeOrigin: true,
        rewrite: (path) => path
      }
    }
  }
})
