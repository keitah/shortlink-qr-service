import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Конфигурация Vite для React
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173
  }
})
