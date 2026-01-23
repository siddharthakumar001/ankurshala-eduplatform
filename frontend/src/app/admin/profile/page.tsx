'use client'

import AuthGuard from '@/components/AuthGuard'
import SessionManager from '@/components/SessionManager'
import DashboardLayout from '@/components/layout/DashboardLayout'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useAuthStore } from '@/store/auth'

export default function AdminProfilePage() {
  const { user } = useAuthStore()

  return (
    <AuthGuard requiredRoles={['ADMIN']}>
      <SessionManager showSessionInfo={false}>
        <DashboardLayout role="admin">
          <div className="space-y-6 relative">
            <div className="pointer-events-none absolute inset-0 -z-10">
              <div className="absolute -top-20 right-6 h-72 w-72 rounded-full bg-ankur-primary/10 blur-3xl" />
              <div className="absolute bottom-0 left-6 h-64 w-64 rounded-full bg-ankur-accent/10 blur-3xl" />
            </div>
            <div className="page-header">
              <h1 className="text-2xl font-bold text-white">Admin Profile</h1>
              <p className="text-white/80">Administrative accounts are managed centrally.</p>
            </div>

            <Card className="glass">
              <CardHeader>
                <CardTitle>Managed by System</CardTitle>
                <CardDescription>
                  Admin profile details are provisioned in the backend. Contact the platform team for updates.
                </CardDescription>
              </CardHeader>
              <CardContent className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm text-gray-700 dark:text-gray-200">
                <div>
                  <p className="text-xs uppercase tracking-wide text-gray-500 dark:text-gray-400">Name</p>
                  <p className="font-semibold">{user?.name || 'Admin'}</p>
                </div>
                <div>
                  <p className="text-xs uppercase tracking-wide text-gray-500 dark:text-gray-400">Email</p>
                  <p className="font-semibold">{user?.email || '—'}</p>
                </div>
                <div>
                  <p className="text-xs uppercase tracking-wide text-gray-500 dark:text-gray-400">Role</p>
                  <p className="font-semibold">Admin</p>
                </div>
              </CardContent>
            </Card>
          </div>
        </DashboardLayout>
      </SessionManager>
    </AuthGuard>
  )
}
