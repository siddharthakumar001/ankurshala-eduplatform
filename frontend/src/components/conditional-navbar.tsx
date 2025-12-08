'use client'

import { usePathname } from 'next/navigation'
import Navbar from './navbar'

export default function ConditionalNavbar() {
  const pathname = usePathname()
  
  // Hide navbar on homepage, login page, signup pages, and dashboard pages
  // Dashboard pages (admin, teacher, student) have their own navigation via DashboardLayout
  const hideNavbarPaths = [
    '/',
    '/login',
    '/register-student',
    '/register-teacher',
    '/admin',
    '/teacher',
    '/student',
  ]
  
  // Check if current path starts with any of the hide paths
  const shouldHideNavbar = hideNavbarPaths.some(path => 
    pathname === path || pathname.startsWith(path + '/')
  )
  
  if (shouldHideNavbar) {
    return null
  }
  
  return <Navbar />
}