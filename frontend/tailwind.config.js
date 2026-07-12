/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'brand': {
          DEFAULT: '#6610f2', // Electric Indigo
          light: '#8540f5',
          dark: '#520dc2',
        },
        'accent': {
          DEFAULT: '#10b981', // Emerald Green
          light: '#34d399',
          dark: '#059669',
        },
        'surface': {
          DEFAULT: '#ffffff', // Pure White
          dark: '#1e293b', // Deep Charcoal
          muted: '#f1f5f9', // Soft Slate
        }
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
      boxShadow: {
        'neomorphic': '5px 5px 10px #e2e8f0, -5px -5px 10px #ffffff',
        'neomorphic-dark': '5px 5px 10px #0f172a, -5px -5px 10px #1e293b',
      }
    },
  },
  plugins: [],
}
