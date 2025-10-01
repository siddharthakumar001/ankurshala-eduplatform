'use client'

import AuthGuard from './AuthGuard'

interface RouteGuardProps {
  children: React.ReactNode
}

export function StudentRoute({ children }: RouteGuardProps) {
  return (
    <AuthGuard requiredRoles={['STUDENT']}>
      {children}
    </AuthGuard>
  )
}

export function TeacherRoute({ children }: RouteGuardProps) {
  return (
    <AuthGuard requiredRoles={['TEACHER']}>
      {children}
    </AuthGuard>
  )
}

export function AdminRoute({ children }: RouteGuardProps) {
  return (
    <AuthGuard requiredRoles={['ADMIN']}>
      {children}
    </AuthGuard>
  )
}

export function AuthenticatedRoute({ children }: RouteGuardProps) {
  return (
    <AuthGuard>
      {children}
    </AuthGuard>
  )
}
