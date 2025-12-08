import type { Config } from 'tailwindcss'

const config: Config = {
  darkMode: ["class"],
  content: [
    './pages/**/*.{ts,tsx}',
    './components/**/*.{ts,tsx}',
    './app/**/*.{ts,tsx}',
    './src/**/*.{ts,tsx}',
  ],
  theme: {
    container: {
      center: true,
      padding: "2rem",
      screens: {
        "2xl": "1400px",
      },
    },
    extend: {
      colors: {
        border: "hsl(var(--border))",
        input: "hsl(var(--input))",
        ring: "hsl(var(--ring))",
        background: "hsl(var(--background))",
        foreground: "hsl(var(--foreground))",
        primary: {
          DEFAULT: "hsl(var(--primary))",
          foreground: "hsl(var(--primary-foreground))",
        },
        secondary: {
          DEFAULT: "hsl(var(--secondary))",
          foreground: "hsl(var(--secondary-foreground))",
        },
        destructive: {
          DEFAULT: "hsl(var(--destructive))",
          foreground: "hsl(var(--destructive-foreground))",
        },
        muted: {
          DEFAULT: "hsl(var(--muted))",
          foreground: "hsl(var(--muted-foreground))",
        },
        accent: {
          DEFAULT: "hsl(var(--accent))",
          foreground: "hsl(var(--accent-foreground))",
        },
        popover: {
          DEFAULT: "hsl(var(--popover))",
          foreground: "hsl(var(--popover-foreground))",
        },
        card: {
          DEFAULT: "hsl(var(--card))",
          foreground: "hsl(var(--card-foreground))",
        },
        // AnkurShala Brand Colors - Modern Design
        ankur: {
          primary: "#0F9D58",      // Green primary
          "primary-dark": "#0d8a4d",
          secondary: "#1E3A5F",    // Navy sidebar
          "secondary-dark": "#152A45",
          accent: "#F5B800",       // Gold accent
          "accent-light": "#FFD700",
          success: "#10B981",      // Success green
          warning: "#F59E0B",      // Warning orange
          error: "#EF4444",        // Error red
          info: "#0EA5E9",         // Info blue
        },
        // Surface colors
        surface: {
          DEFAULT: "#F8FAFC",
          elevated: "#FFFFFF",
          overlay: "rgba(255, 255, 255, 0.95)",
        },
        // Educational Theme Colors
        education: {
          grade7: "#8B5CF6",       // Purple for Grade 7
          grade8: "#06B6D4",       // Cyan for Grade 8
          grade9: "#10B981",       // Green for Grade 9
          grade10: "#F59E0B",      // Orange for Grade 10
          grade11: "#EF4444",      // Red for Grade 11
          grade12: "#6366F1",      // Indigo for Grade 12
        },
      },
      borderRadius: {
        lg: "var(--radius)",
        md: "calc(var(--radius) - 2px)",
        sm: "calc(var(--radius) - 4px)",
      },
      keyframes: {
        "accordion-down": {
          from: { height: "0" },
          to: { height: "var(--radix-accordion-content-height)" },
        },
        "accordion-up": {
          from: { height: "var(--radix-accordion-content-height)" },
          to: { height: "0" },
        },
      },
      animation: {
        "accordion-down": "accordion-down 0.2s ease-out",
        "accordion-up": "accordion-up 0.2s ease-out",
      },
    },
  },
  plugins: [require("tailwindcss-animate")],
}

export default config
