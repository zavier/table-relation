import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  build: {
    // 直接输出到后端 classpath 下的 static 目录，由 Maven 打包进 jar
    outDir: '../target/classes/static',
    emptyOutDir: true
  },
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
