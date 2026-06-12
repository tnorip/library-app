import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // Spring Boot(port 8080)へのプロキシ — CORS 設定不要
      // 新しいエンドポイントを追加したらここにも追記する
      '/api':          'http://localhost:8080',
      '/reservations': 'http://localhost:8080',
      '/actuator':     'http://localhost:8080',
    },
  },
})
