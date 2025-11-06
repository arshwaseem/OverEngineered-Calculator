import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import tsconfigPaths from "vite-tsconfig-paths";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss(), tsconfigPaths()],
  
  build: {
    // Chunk splitting strategy for better caching
    rollupOptions: {
      output: {
        manualChunks: {
          // React core and routing
          'react-vendor': ['react', 'react-dom', 'react-router-dom'],
          
          // Animation and UI libraries
          'ui-vendor': ['motion', 'lucide-react', 'class-variance-authority', 'clsx', 'tailwind-merge'],
          
          // Radix UI components
          'radix-vendor': ['@radix-ui/react-label', '@radix-ui/react-slot'],
          
          // Axios for API calls
          'api-vendor': ['axios'],
        },
        
        // Clean chunk names
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: 'assets/[ext]/[name]-[hash].[ext]',
      },
    },
    
    // Increase chunk size warning limit (1MB)
    chunkSizeWarningLimit: 1000,
    
    // Enable source maps for production debugging (optional)
    sourcemap: false,
    
    // Target modern browsers for smaller bundle
    target: 'es2020',
    
    // CSS code splitting
    cssCodeSplit: true,
    
    // Asset inline limit (4kb)
    assetsInlineLimit: 4096,
  },
  
  // Development server optimizations
  server: {
    // Hot module replacement
    hmr: {
      overlay: true,
    },
  },
  
  // Dependency optimization
  optimizeDeps: {
    include: [
      'react',
      'react-dom',
      'react-router-dom',
      'axios',
      'motion',
      'lucide-react',
    ],
  },
  
  // Preview server settings
  preview: {
    port: 4173,
    strictPort: true,
  },
})