import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// base '/matolimp/' — проект-сайт GitHub Pages по адресу
// https://<user>.github.io/matolimp/ . Для локального dev база '/'.
export default defineConfig(({ command }) => ({
  base: command === 'build' ? '/matolimp/' : '/',
  plugins: [react()],
  build: { outDir: 'dist' },
}))
