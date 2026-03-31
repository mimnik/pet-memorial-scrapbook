import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import path from 'path'

const CLOUD_FRONTEND_URL = 'http://47.119.131.127:25565/'

const cloudUrlPlugin = {
  name: 'cloud-url-hint',
  configureServer(server: { printUrls: () => void }) {
    const originalPrintUrls = server.printUrls
    server.printUrls = () => {
      originalPrintUrls()
      console.log(`  ➜  Cloud:   ${CLOUD_FRONTEND_URL}`)
    }
  },
}

export default defineConfig({
  plugins: [vue(), cloudUrlPlugin],
  test: {
    environment: 'jsdom',
    globals: true,
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    host: '127.0.0.1',
    port: 3000,
    strictPort: true,
    origin: 'http://47.119.131.127:25565',
    hmr: {
      host: '47.119.131.127',
      clientPort: 25565,
      protocol: 'ws',
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
