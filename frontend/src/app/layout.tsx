import type { Metadata } from 'next'
import { Outfit, Plus_Jakarta_Sans } from 'next/font/google'
import './globals.css'
import Providers from '@/components/providers'
import ConditionalNavbar from '@/components/conditional-navbar'
import ErrorBoundary from '@/components/error-boundary'
import { SessionProvider } from '@/contexts/SessionContext'

const outfit = Outfit({
  subsets: ['latin'],
  variable: '--font-display',
  weight: ['400', '500', '600', '700'],
})
const jakarta = Plus_Jakarta_Sans({
  subsets: ['latin'],
  variable: '--font-sans',
  weight: ['400', '500', '600', '700'],
})

export const metadata: Metadata = {
  title: 'Ankurshala — On Demand Learning',
  description: 'AI-enabled on-demand learning for grades 7–12',
  icons: {
    icon: '/favicon.png',
    shortcut: '/favicon.png',
    apple: '/ankurshala-logo-small.png',
  },
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={`${jakarta.variable} ${outfit.variable} font-sans`}>
        <ErrorBoundary>
          <Providers>
            <SessionProvider>
              <ConditionalNavbar />
              {children}
            </SessionProvider>
          </Providers>
        </ErrorBoundary>
      </body>
    </html>
  )
}
