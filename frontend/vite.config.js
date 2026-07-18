import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

export default defineConfig({
  plugins: [react()],
  
  // Pre-bundle heavy dependencies
  optimizeDeps: {
    include: [
      'react',
      'react-dom',
      'framer-motion',
      'lucide-react',
      'recharts',
      'react-query',
      'react-router-dom'
    ],
    esbuildOptions: {
      // Speed up dependency prebundling
      target: 'es2020'
    }
  },

  // Optimize resolution
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },

  // Dev server config
  server: {
    // Exclude directories from watcher
    watch: {
      ignored: ['**/node_modules/**', '**/.git/**', '**/dist/**']
    },
    // Optimize HMR
    hmr: {
      protocol: 'ws',
      host: 'localhost',
      port: 5173
    }
  },

  // Logging
  logLevel: 'warn'
})
