/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        dark: {
          950: '#0B0F19',
          900: '#111827',
          850: '#151E32',
          800: '#1F2937',
          700: '#374151',
          600: '#4B5563',
        },
        brand: {
          red: '#FF0000',
          darkRed: '#CC0000',
          accent: '#6366F1',
          gold: '#F59E0B',
        }
      }
    },
  },
  plugins: [],
}
