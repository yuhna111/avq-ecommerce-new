import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Frontend chạy cổng 5173, chuyển /api sang backend Spring Boot cổng 8080 (CLAUDE.md mục 2).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
