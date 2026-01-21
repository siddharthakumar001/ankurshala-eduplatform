'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import { useRouter } from 'next/navigation'
import AuthGuard from '@/components/AuthGuard'
import SessionManager from '@/components/SessionManager'
import DashboardLayout from '@/components/layout/DashboardLayout'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Checkbox } from '@/components/ui/checkbox'
import { AlertCircle, Loader2 } from 'lucide-react'
import { useAuthStore } from '@/store/auth'
import { adminAPI } from '@/lib/apiClient'

interface AdminProfile {
  id: number
  firstName?: string
  middleName?: string
  lastName?: string
  mobileNumber?: string
  alternateMobileNumber?: string
  contactEmail?: string
  department?: string
  designation?: string
  employeeId?: string
  joiningDate?: string
  reportingManager?: string
  workLocation?: string
  emergencyContact?: string
  profilePhotoUrl?: string
  permissions?: string
  lastLoginAt?: string
  isActive: boolean
}

type MessageState = {
  type: 'success' | 'error'
  text: string
}

export default function AdminProfilePage() {
  const router = useRouter()
  const { user, logout } = useAuthStore()
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState<MessageState | null>(null)
  const [profile, setProfile] = useState<AdminProfile | null>(null)

  const fetchProfile = useCallback(async () => {
    try {
      setLoading(true)
      const data = await adminAPI.getProfile()
      setProfile(data)
    } catch (error) {
      console.error('Error fetching admin profile:', error)
      if (error instanceof Error && error.message.includes('401')) {
        logout()
        router.push('/login')
      } else {
        setMessage({ type: 'error', text: 'Error loading profile.' })
      }
    } finally {
      setLoading(false)
    }
  }, [logout, router])

  useEffect(() => {
    if (!user) return
    fetchProfile()
  }, [user, fetchProfile])

  const updateProfile = async () => {
    if (!profile) return

    try {
      setSaving(true)
      const data = await adminAPI.updateProfile(profile)
      setProfile(data)
      setMessage({ type: 'success', text: 'Profile updated successfully.' })
      setTimeout(() => setMessage(null), 3000)
    } catch (error) {
      console.error('Error updating admin profile:', error)
      if (error instanceof Error && error.message.includes('401')) {
        logout()
        router.push('/login')
      } else {
        setMessage({ type: 'error', text: 'Failed to update profile.' })
      }
    } finally {
      setSaving(false)
    }
  }

  const permissionsText = useMemo(() => {
    if (!profile?.permissions) return ''
    try {
      return JSON.stringify(JSON.parse(profile.permissions), null, 2)
    } catch {
      return profile.permissions
    }
  }, [profile?.permissions])

  const renderLoading = () => (
    <DashboardLayout role="admin">
      <div className="flex items-center justify-center h-64">
        <div className="text-center glass-panel rounded-2xl border border-white/40 px-8 py-6">
          <Loader2 className="h-8 w-8 animate-spin text-ankur-primary mx-auto mb-2" />
          <p className="text-gray-600 dark:text-gray-300">Loading admin profile...</p>
        </div>
      </div>
    </DashboardLayout>
  )

  const renderError = () => (
    <DashboardLayout role="admin">
      <div className="flex items-center justify-center h-64">
        <div className="text-center glass-panel rounded-2xl border border-red-200/60 dark:border-red-500/30 px-8 py-6 bg-red-50/70 dark:bg-red-500/10">
          <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
          <p className="text-red-600 dark:text-red-200 mb-4">Failed to load admin profile.</p>
          <Button variant="outline" onClick={fetchProfile}>
            Try Again
          </Button>
        </div>
      </div>
    </DashboardLayout>
  )

  if (loading) {
    return (
      <AuthGuard requiredRoles={['ADMIN']}>
        <SessionManager showSessionInfo={false}>
          {renderLoading()}
        </SessionManager>
      </AuthGuard>
    )
  }

  if (!profile) {
    return (
      <AuthGuard requiredRoles={['ADMIN']}>
        <SessionManager showSessionInfo={false}>
          {renderError()}
        </SessionManager>
      </AuthGuard>
    )
  }

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
              <p className="text-white/80">Manage your administrative profile and information.</p>
            </div>

            {message?.text && (
              <div
                className={`rounded-2xl border p-4 ${
                  message.type === 'success'
                    ? 'bg-green-50/70 border-green-200/60 text-green-800 dark:bg-green-900/20 dark:text-green-200 dark:border-green-800'
                    : 'bg-red-50/70 border-red-200/60 text-red-800 dark:bg-red-500/10 dark:text-red-200 dark:border-red-500/30'
                }`}
              >
                {message.text}
              </div>
            )}

            <Card className="glass">
              <CardHeader>
                <CardTitle>Personal Information</CardTitle>
                <CardDescription>Keep your contact details up to date.</CardDescription>
              </CardHeader>
              <CardContent className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="firstName">First Name</Label>
                  <Input
                    id="firstName"
                    value={profile.firstName || ''}
                    onChange={(e) => setProfile({ ...profile, firstName: e.target.value })}
                    placeholder="Enter first name"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="middleName">Middle Name</Label>
                  <Input
                    id="middleName"
                    value={profile.middleName || ''}
                    onChange={(e) => setProfile({ ...profile, middleName: e.target.value })}
                    placeholder="Enter middle name (optional)"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="lastName">Last Name</Label>
                  <Input
                    id="lastName"
                    value={profile.lastName || ''}
                    onChange={(e) => setProfile({ ...profile, lastName: e.target.value })}
                    placeholder="Enter last name"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="mobileNumber">Mobile Number</Label>
                  <Input
                    id="mobileNumber"
                    value={profile.mobileNumber || ''}
                    onChange={(e) => setProfile({ ...profile, mobileNumber: e.target.value })}
                    placeholder="Enter mobile number"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="alternateMobileNumber">Alternate Mobile</Label>
                  <Input
                    id="alternateMobileNumber"
                    value={profile.alternateMobileNumber || ''}
                    onChange={(e) => setProfile({ ...profile, alternateMobileNumber: e.target.value })}
                    placeholder="Enter alternate mobile (optional)"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="contactEmail">Contact Email</Label>
                  <Input
                    id="contactEmail"
                    type="email"
                    value={profile.contactEmail || ''}
                    onChange={(e) => setProfile({ ...profile, contactEmail: e.target.value })}
                    placeholder="Enter contact email"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="profilePhotoUrl">Profile Photo URL</Label>
                  <Input
                    id="profilePhotoUrl"
                    value={profile.profilePhotoUrl || ''}
                    onChange={(e) => setProfile({ ...profile, profilePhotoUrl: e.target.value })}
                    placeholder="Enter profile photo URL"
                  />
                </div>
              </CardContent>
            </Card>

            <Card className="glass">
              <CardHeader>
                <CardTitle>Professional Information</CardTitle>
                <CardDescription>Manage administrative details and reporting.</CardDescription>
              </CardHeader>
              <CardContent className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="employeeId">Employee ID</Label>
                  <Input
                    id="employeeId"
                    value={profile.employeeId || ''}
                    onChange={(e) => setProfile({ ...profile, employeeId: e.target.value })}
                    placeholder="Enter employee ID"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="department">Department</Label>
                  <Input
                    id="department"
                    value={profile.department || ''}
                    onChange={(e) => setProfile({ ...profile, department: e.target.value })}
                    placeholder="Enter department"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="designation">Designation</Label>
                  <Input
                    id="designation"
                    value={profile.designation || ''}
                    onChange={(e) => setProfile({ ...profile, designation: e.target.value })}
                    placeholder="Enter designation"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="joiningDate">Joining Date</Label>
                  <Input
                    id="joiningDate"
                    type="date"
                    value={profile.joiningDate || ''}
                    onChange={(e) => setProfile({ ...profile, joiningDate: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="reportingManager">Reporting Manager</Label>
                  <Input
                    id="reportingManager"
                    value={profile.reportingManager || ''}
                    onChange={(e) => setProfile({ ...profile, reportingManager: e.target.value })}
                    placeholder="Enter reporting manager name"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="workLocation">Work Location</Label>
                  <Input
                    id="workLocation"
                    value={profile.workLocation || ''}
                    onChange={(e) => setProfile({ ...profile, workLocation: e.target.value })}
                    placeholder="Enter work location"
                  />
                </div>
              </CardContent>
            </Card>

            <Card className="glass">
              <CardHeader>
                <CardTitle>Emergency Information</CardTitle>
                <CardDescription>Provide a contact for urgent issues.</CardDescription>
              </CardHeader>
              <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="emergencyContact">Emergency Contact</Label>
                  <Textarea
                    id="emergencyContact"
                    value={profile.emergencyContact || ''}
                    onChange={(e) => setProfile({ ...profile, emergencyContact: e.target.value })}
                    placeholder="Enter emergency contact details"
                  />
                </div>
              </CardContent>
            </Card>

            <Card className="glass">
              <CardHeader>
                <CardTitle>Account Status</CardTitle>
                <CardDescription>Activate or review account state.</CardDescription>
              </CardHeader>
              <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="flex items-center space-x-3">
                  <Checkbox
                    id="isActive"
                    checked={profile.isActive}
                    onCheckedChange={(checked) =>
                      setProfile({ ...profile, isActive: Boolean(checked) })
                    }
                  />
                  <Label htmlFor="isActive">Active Account</Label>
                </div>
                {profile.lastLoginAt && (
                  <div className="space-y-2">
                    <Label>Last Login</Label>
                    <div className="text-sm text-gray-600 dark:text-gray-400">
                      {new Date(profile.lastLoginAt).toLocaleString()}
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>

            {permissionsText && (
              <Card className="glass">
                <CardHeader>
                  <CardTitle>Permissions</CardTitle>
                  <CardDescription>Read-only view of administrative permissions.</CardDescription>
                </CardHeader>
                <CardContent>
                  <pre className="text-sm text-gray-700 dark:text-gray-200 whitespace-pre-wrap bg-white/70 dark:bg-slate-900/70 border border-white/30 dark:border-white/10 rounded-lg p-4">
                    {permissionsText}
                  </pre>
                </CardContent>
              </Card>
            )}

            <div className="flex justify-end">
              <Button onClick={updateProfile} disabled={saving} className="btn-primary">
                {saving ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Saving...
                  </>
                ) : (
                  'Save Profile'
                )}
              </Button>
            </div>
          </div>
        </DashboardLayout>
      </SessionManager>
    </AuthGuard>
  )
}
