'use client'

import { usePathname } from 'next/navigation'
import Navbar from './navbar'

export default function ConditionalNavbar() {
  const pathname = usePathname()
  
  // Hide navbar on homepage, login page, and signup pages
  if (pathname === '/' || pathname === '/login' || pathname === '/register-student' || pathname === '/register-teacher') {
    return null
  }
  
  return <Navbar />
}
