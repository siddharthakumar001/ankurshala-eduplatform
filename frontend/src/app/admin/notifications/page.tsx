'use client'

import { useState, useEffect, useCallback } from 'react'
import DashboardLayout from '@/components/layout/DashboardLayout'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { 
  Bell, 
  Send, 
  Plus, 
  Download, 
  Search,
  Eye,
  CheckCircle,
  XCircle,
  Clock,
  AlertCircle,
  Users,
  Mail,
  Smartphone
} from 'lucide-react'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { toast } from 'sonner'
import { api } from '@/utils/api'
import { Loader2 } from 'lucide-react'

interface Notification {
  id: number
  userId?: number
  userEmail?: string
  title: string
  body: string
  audience: string
  delivery: string
  status: string
  createdAt: string
  sentAt?: string
}

interface NotificationStats {
  totalNotifications: number
  queuedNotifications: number
  sentNotifications: number
  failedNotifications: number
  notificationsLast30Days: number
}

export default function AdminNotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [stats, setStats] = useState<NotificationStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [audienceFilter, setAudienceFilter] = useState('all')
  const [statusFilter, setStatusFilter] = useState('all')
  const [showComposeDialog, setShowComposeDialog] = useState(false)
  const [showPreviewDialog, setShowPreviewDialog] = useState(false)
  const [sending, setSending] = useState(false)
  const [broadcastResult, setBroadcastResult] = useState<any>(null)
  const [formError, setFormError] = useState('')
  const [loadError, setLoadError] = useState('')
  const [statsError, setStatsError] = useState('')
  const [targetMode, setTargetMode] = useState<'broadcast' | 'specific'>('broadcast')
  const [targetType, setTargetType] = useState<'email' | 'userId'>('email')
  const [targetValue, setTargetValue] = useState('')
  
  // Form states
  const [formData, setFormData] = useState({
    title: '',
    body: '',
    audience: 'STUDENT',
    delivery: 'IN_APP'
  })

  const fetchNotifications = useCallback(async () => {
    try {
      setLoadError('')
      const params = new URLSearchParams()
      if (audienceFilter && audienceFilter !== 'all') params.append('audience', audienceFilter)
      if (statusFilter && statusFilter !== 'all') params.append('status', statusFilter)
      
      const response = await api.get(`/admin/notifications?${params}`)
      const data = response.data as any
      const notificationsList = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : []
      setNotifications(notificationsList)
    } catch (error) {
      console.error('Error fetching notifications:', error)
      setNotifications([]) // Set empty array on error
      setLoadError('Failed to load notifications. Please try again.')
    }
  }, [audienceFilter, statusFilter])

  const fetchStats = useCallback(async () => {
    try {
      setStatsError('')
      const response = await api.get('/admin/notifications/statistics')
      const data = response.data as any
      setStats(data)
    } catch (error) {
      console.error('Error fetching notification stats:', error)
      // Set default stats on error
      setStats({
        totalNotifications: 0,
        queuedNotifications: 0,
        sentNotifications: 0,
        failedNotifications: 0,
        notificationsLast30Days: 0
      })
      setStatsError('Failed to load notification statistics.')
    }
  }, [])

  const fetchAllData = useCallback(async () => {
    setLoading(true)
    setLoadError('')
    setStatsError('')
    await Promise.allSettled([
      fetchNotifications(),
      fetchStats()
    ])
    setLoading(false)
  }, [fetchNotifications, fetchStats])

  useEffect(() => {
    fetchAllData()
  }, [fetchAllData])

  const handleSendNotification = async () => {
    try {
      if (targetMode === 'specific') {
        const trimmed = targetValue.trim()
        if (!trimmed) {
          setFormError('Please provide a target email or user ID.')
          return
        }
        if (targetType === 'userId' && Number.isNaN(Number(trimmed))) {
          setFormError('Please provide a valid numeric user ID.')
          return
        }
      }

      setSending(true)
      setFormError('')

      const payload = {
        ...formData,
        audience: targetMode === 'specific' ? 'ALL' : formData.audience,
        targetUserId: targetMode === 'specific' && targetType === 'userId' ? Number(targetValue.trim()) : undefined,
        targetEmail: targetMode === 'specific' && targetType === 'email' ? targetValue.trim() : undefined
      }

      const response = await api.post('/admin/notifications/broadcast', payload)
      const result = response.data as any
      
      setBroadcastResult(result)
      setShowComposeDialog(false)
      setFormData({
        title: '',
        body: '',
        audience: 'STUDENT',
        delivery: 'IN_APP'
      })
      setTargetMode('broadcast')
      setTargetType('email')
      setTargetValue('')
      
      toast.success('Notification sent successfully!')
      await fetchNotifications()
      await fetchStats()
    } catch (error: any) {
      console.error('Error sending notification:', error)
      const errorMessage = error.response?.data?.message || 'Failed to send notification'
      setFormError(errorMessage)
      toast.error(errorMessage)
    } finally {
      setSending(false)
    }
  }

  const clearError = () => {
    setFormError('')
  }

  const openComposeDialog = () => {
    clearError()
    setShowComposeDialog(true)
  }

  const closeComposeDialog = () => {
    clearError()
    setShowComposeDialog(false)
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'SENT':
        return <CheckCircle className="h-4 w-4 text-green-500" />
      case 'FAILED':
        return <XCircle className="h-4 w-4 text-red-500" />
      case 'PENDING':
        return <Clock className="h-4 w-4 text-yellow-500" />
      case 'DELIVERED':
      case 'READ':
        return <CheckCircle className="h-4 w-4 text-blue-500" />
      default:
        return <AlertCircle className="h-4 w-4 text-gray-500" />
    }
  }

  const getStatusBadge = (status: string) => {
    const baseClasses = "inline-flex px-2 py-1 text-xs font-semibold rounded-full"
    switch (status) {
      case 'SENT':
        return `${baseClasses} bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200`
      case 'FAILED':
        return `${baseClasses} bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200`
      case 'PENDING':
        return `${baseClasses} bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200`
      case 'DELIVERED':
      case 'READ':
        return `${baseClasses} bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200`
      default:
        return `${baseClasses} bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200`
    }
  }

  const getAudienceBadge = (audience: string) => {
    const baseClasses = "inline-flex px-2 py-1 text-xs font-semibold rounded-full"
    switch (audience) {
      case 'STUDENT':
        return `${baseClasses} bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200`
      case 'TEACHER':
        return `${baseClasses} bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200`
      case 'ALL':
        return `${baseClasses} bg-indigo-100 text-indigo-800 dark:bg-indigo-900 dark:text-indigo-200`
      case 'ADMIN':
        return `${baseClasses} bg-emerald-100 text-emerald-800 dark:bg-emerald-900 dark:text-emerald-200`
      default:
        return `${baseClasses} bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200`
    }
  }

  const getDeliveryIcon = (delivery: string) => {
    switch (delivery) {
      case 'EMAIL':
        return <Mail className="h-4 w-4 text-blue-500" />
      case 'IN_APP':
        return <Smartphone className="h-4 w-4 text-green-500" />
      case 'IN_APP_EMAIL':
        return (
          <div className="flex space-x-1">
            <Mail className="h-3 w-3 text-blue-500" />
            <Smartphone className="h-3 w-3 text-green-500" />
          </div>
        )
      default:
        return <Bell className="h-4 w-4 text-gray-500" />
    }
  }

  const getDeliveryLabel = (delivery: string) => {
    switch (delivery) {
      case 'IN_APP':
        return 'In-app'
      case 'EMAIL':
        return 'Email'
      case 'IN_APP_EMAIL':
        return 'In-app + Email'
      default:
        return delivery.replace(/_/g, ' ').toLowerCase()
    }
  }

  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    const now = new Date()
    const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60))
    
    if (diffInHours < 1) {
      return 'Just now'
    } else if (diffInHours < 24) {
      return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`
    } else {
      const diffInDays = Math.floor(diffInHours / 24)
      return `${diffInDays} day${diffInDays > 1 ? 's' : ''} ago`
    }
  }

  const filteredNotifications = notifications.filter(notification => {
    const matchesSearch = (notification.title || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
                         (notification.body || '').toLowerCase().includes(searchTerm.toLowerCase())
    const matchesAudience = audienceFilter === 'all' || notification.audience === audienceFilter
    const matchesStatus = statusFilter === 'all' || notification.status === statusFilter
    
    return matchesSearch && matchesAudience && matchesStatus
  })

  const targetInvalid = targetMode === 'specific' && (
    !targetValue.trim() ||
    (targetType === 'userId' && Number.isNaN(Number(targetValue.trim())))
  )

  if (loading) {
    return (
      <DashboardLayout role="admin">
        <div className="flex items-center justify-center h-64">
          <div className="text-center glass-panel rounded-2xl border border-white/40 px-8 py-6">
            <Loader2 className="h-8 w-8 animate-spin text-ankur-primary mx-auto mb-2" />
            <p className="text-gray-600 dark:text-gray-300">Loading notifications...</p>
          </div>
        </div>
      </DashboardLayout>
    )
  }


  return (
    <DashboardLayout role="admin">
      <div className="space-y-6 relative">
        <div className="pointer-events-none absolute inset-0 -z-10">
          <div className="absolute -top-20 right-10 h-64 w-64 rounded-full bg-ankur-primary/10 blur-3xl" />
          <div className="absolute bottom-0 left-6 h-72 w-72 rounded-full bg-ankur-accent/10 blur-3xl" />
        </div>
        {/* Header */}
        <div className="page-header flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-white">Notifications</h1>
            <p className="text-white/80">Send notifications to students and teachers</p>
          </div>
          <Button 
            onClick={openComposeDialog}
            className="flex items-center space-x-2 btn-primary"
          >
            <Plus className="h-4 w-4" />
            <span>Compose</span>
          </Button>
        </div>
        {loadError && (
          <div className="glass rounded-2xl p-4 border border-red-200/60 dark:border-red-500/30 bg-red-50/70 dark:bg-red-500/10 flex items-center justify-between">
            <div className="flex items-center gap-3 text-red-700 dark:text-red-200">
              <AlertCircle className="h-5 w-5" />
              <span className="text-sm">{loadError}</span>
            </div>
            <Button onClick={fetchAllData} variant="outline" size="sm">
              Try Again
            </Button>
          </div>
        )}

        {/* Stats Cards */}
        {stats && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
            <Card className="p-6 glass hover-lift">
              <div className="flex items-center">
                <div className="p-2 bg-blue-100 dark:bg-blue-900 rounded-lg">
                  <Bell className="h-6 w-6 text-blue-600 dark:text-blue-400" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">
                    {stats.totalNotifications}
                  </p>
                </div>
              </div>
            </Card>

            <Card className="p-6 glass hover-lift">
              <div className="flex items-center">
                <div className="p-2 bg-green-100 dark:bg-green-900 rounded-lg">
                  <CheckCircle className="h-6 w-6 text-green-600 dark:text-green-400" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Sent</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">
                    {stats.sentNotifications}
                  </p>
                </div>
              </div>
            </Card>

            <Card className="p-6 glass hover-lift">
              <div className="flex items-center">
                <div className="p-2 bg-yellow-100 dark:bg-yellow-900 rounded-lg">
                  <Clock className="h-6 w-6 text-yellow-600 dark:text-yellow-400" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Queued</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">
                    {stats.queuedNotifications}
                  </p>
                </div>
              </div>
            </Card>

            <Card className="p-6 glass hover-lift">
              <div className="flex items-center">
                <div className="p-2 bg-red-100 dark:bg-red-900 rounded-lg">
                  <XCircle className="h-6 w-6 text-red-600 dark:text-red-400" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Failed</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">
                    {stats.failedNotifications}
                  </p>
                </div>
              </div>
            </Card>

            <Card className="p-6 glass hover-lift">
              <div className="flex items-center">
                <div className="p-2 bg-purple-100 dark:bg-purple-900 rounded-lg">
                  <Users className="h-6 w-6 text-purple-600 dark:text-purple-400" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Last 30 Days</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">
                    {stats.notificationsLast30Days}
                  </p>
                </div>
              </div>
            </Card>
          </div>
        )}
        {statsError && (
          <div className="glass rounded-2xl p-4 border border-amber-200/60 dark:border-amber-500/30 bg-amber-50/70 dark:bg-amber-500/10 flex items-center justify-between">
            <div className="flex items-center gap-3 text-amber-700 dark:text-amber-200">
              <AlertCircle className="h-5 w-5" />
              <span className="text-sm">{statsError}</span>
            </div>
            <Button onClick={fetchStats} variant="outline" size="sm">
              Retry
            </Button>
          </div>
        )}

        {/* Search and Filters */}
        <Card className="p-6 glass">
          <div className="flex flex-col md:flex-row md:items-center md:space-x-4 gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                <Input
                  placeholder="Search notifications..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            <div className="flex flex-wrap gap-2">
              <Select value={audienceFilter} onValueChange={setAudienceFilter}>
                <SelectTrigger className="w-40">
                  <SelectValue placeholder="Audience" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Audiences</SelectItem>
                  <SelectItem value="STUDENT">Students</SelectItem>
                  <SelectItem value="TEACHER">Teachers</SelectItem>
                  <SelectItem value="ALL">All Users</SelectItem>
                </SelectContent>
              </Select>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="w-40">
                  <SelectValue placeholder="Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="SENT">Sent</SelectItem>
                  <SelectItem value="PENDING">Queued</SelectItem>
                  <SelectItem value="FAILED">Failed</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="text-sm text-gray-500 dark:text-gray-300">
              {filteredNotifications.length} notifications
            </div>
          </div>
        </Card>

        {/* Notification History */}
        <Card className="p-6 glass">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-ankur-secondary dark:text-white">Notification History</h3>
              <div className="flex space-x-2">
                <Button variant="outline" size="sm" className="flex items-center space-x-2">
                  <Download className="h-4 w-4" />
                  <span>Export</span>
                </Button>
              </div>
            </div>
            
            {filteredNotifications.length === 0 ? (
              <div className="text-center py-8">
                <Bell className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-500 dark:text-gray-400">No notifications found</p>
                <p className="text-sm text-gray-400 dark:text-gray-500 mt-1">
                  Send your first notification to get started
                </p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                        Notification
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                        Audience
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                        Delivery
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                        Sent
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                        Status
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-white/40 dark:divide-white/10">
                    {filteredNotifications.map((notification) => (
                      <tr key={notification.id} className="hover:bg-white/60 dark:hover:bg-slate-900/60">
                        <td className="px-6 py-4">
                          <div className="flex items-start">
                            <Bell className="h-5 w-5 text-blue-500 mr-3 mt-0.5" />
                            <div>
                              <div className="text-sm font-medium text-gray-900 dark:text-white">
                                {notification.title}
                              </div>
                              <div className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                                {notification.body.length > 100 
                                  ? `${notification.body.substring(0, 100)}...` 
                                  : notification.body}
                              </div>
                              {notification.userEmail && (
                                <div className="text-xs text-gray-400 mt-1">
                                  To: {notification.userEmail}
                                </div>
                              )}
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <Badge className={getAudienceBadge(notification.audience)}>
                            {notification.audience}
                          </Badge>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center space-x-2">
                            {getDeliveryIcon(notification.delivery)}
                            <span className="text-sm text-gray-900 dark:text-white">
                              {getDeliveryLabel(notification.delivery)}
                            </span>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                          {formatDate(notification.createdAt)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center space-x-2">
                            {getStatusIcon(notification.status)}
                            <Badge className={getStatusBadge(notification.status)}>
                              {notification.status}
                            </Badge>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </Card>

        {/* Compose Notification Dialog */}
        <Dialog open={showComposeDialog} onOpenChange={closeComposeDialog}>
          <DialogContent className="max-w-2xl glass-panel border border-white/30 dark:border-white/10">
            <DialogHeader>
              <DialogTitle>Compose Notification</DialogTitle>
              <DialogDescription>
                Send a notification to students and teachers
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              {formError && (
                <div className="p-3 bg-red-50/70 dark:bg-red-500/10 border border-red-200/60 dark:border-red-500/30 rounded-md">
                  <div className="flex">
                    <AlertCircle className="h-5 w-5 text-red-400 mr-2" />
                    <p className="text-sm text-red-600 dark:text-red-200">{formError}</p>
                  </div>
                </div>
              )}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="recipient">Recipient</Label>
                  <Select value={targetMode} onValueChange={(value: 'broadcast' | 'specific') => {
                    setTargetMode(value)
                    if (value === 'specific') {
                      setFormData({ ...formData, audience: 'ALL' })
                    }
                  }}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select recipient" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="broadcast">Broadcast</SelectItem>
                      <SelectItem value="specific">Specific User</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label htmlFor="delivery">Delivery Method</Label>
                  <Select value={formData.delivery} onValueChange={(value) => setFormData({...formData, delivery: value})}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select delivery" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="IN_APP">In-App Only</SelectItem>
                      <SelectItem value="EMAIL">Email Only</SelectItem>
                      <SelectItem value="IN_APP_EMAIL">In-App & Email</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="audience">Target Audience</Label>
                  <Select
                    value={formData.audience}
                    onValueChange={(value) => setFormData({ ...formData, audience: value })}
                    disabled={targetMode === 'specific'}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select audience" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="STUDENT">Students Only</SelectItem>
                      <SelectItem value="TEACHER">Teachers Only</SelectItem>
                      <SelectItem value="ALL">Students & Teachers</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label htmlFor="target">
                    {targetType === 'userId' ? 'User ID' : 'User Email'}
                  </Label>
                  <div className="flex gap-2">
                    <Select value={targetType} onValueChange={(value: 'email' | 'userId') => setTargetType(value)}>
                      <SelectTrigger className="w-32">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="email">Email</SelectItem>
                        <SelectItem value="userId">User ID</SelectItem>
                      </SelectContent>
                    </Select>
                    <Input
                      id="target"
                      value={targetValue}
                      onChange={(e) => setTargetValue(e.target.value)}
                      placeholder={targetType === 'userId' ? 'Enter user ID' : 'Enter email'}
                      disabled={targetMode !== 'specific'}
                    />
                  </div>
                  {targetMode === 'broadcast' && (
                    <p className="text-xs text-gray-500 mt-1">Switch to specific user to enable this field.</p>
                  )}
                </div>
              </div>
              <div>
                <Label htmlFor="title">Title</Label>
                <Input
                  id="title"
                  value={formData.title}
                  onChange={(e) => setFormData({...formData, title: e.target.value})}
                  placeholder="Enter notification title"
                />
              </div>
              <div>
                <Label htmlFor="body">Message</Label>
                <Textarea
                  id="body"
                  rows={4}
                  value={formData.body}
                  onChange={(e) => setFormData({...formData, body: e.target.value})}
                  placeholder="Enter notification message"
                />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setShowPreviewDialog(true)}>
                <Eye className="h-4 w-4 mr-2" />
                Preview
              </Button>
              <Button 
                onClick={handleSendNotification} 
                disabled={!formData.title || !formData.body || sending || targetInvalid}
              >
                {sending ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Sending...
                  </>
                ) : (
                  <>
                    <Send className="h-4 w-4 mr-2" />
                    Send Notification
                  </>
                )}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Preview Dialog */}
        <Dialog open={showPreviewDialog} onOpenChange={setShowPreviewDialog}>
          <DialogContent className="max-w-lg glass-panel border border-white/30 dark:border-white/10">
            <DialogHeader>
              <DialogTitle>Notification Preview</DialogTitle>
              <DialogDescription>
                This is how your notification will appear
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="border border-white/30 dark:border-white/10 rounded-lg p-4 bg-white/70 dark:bg-slate-900/60">
                <div className="flex items-start space-x-3">
                  <Bell className="h-5 w-5 text-blue-500 mt-0.5" />
                  <div className="flex-1">
                    <h4 className="font-semibold text-gray-900 dark:text-white">
                      {formData.title || 'Notification Title'}
                    </h4>
                    <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                      {formData.body || 'Notification message content...'}
                    </p>
                    <div className="flex items-center space-x-4 mt-3 text-xs text-gray-500">
                      <span>
                        Audience: {targetMode === 'specific' ? 'Specific user' : formData.audience}
                      </span>
                      {targetMode === 'specific' && targetValue.trim() && (
                        <span>Target: {targetValue.trim()}</span>
                      )}
                      <span>Delivery: {getDeliveryLabel(formData.delivery)}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setShowPreviewDialog(false)}>
                Close
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Broadcast Result Dialog */}
        {broadcastResult && (
          <Dialog open={!!broadcastResult} onOpenChange={() => setBroadcastResult(null)}>
          <DialogContent className="glass-panel border border-white/30 dark:border-white/10">
            <DialogHeader>
              <DialogTitle>Notification Sent Successfully</DialogTitle>
              <DialogDescription>
                Your notification has been broadcast to the target audience
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="text-center p-4 bg-green-50/70 dark:bg-green-900/20 rounded-lg">
                    <Users className="h-8 w-8 text-green-600 mx-auto mb-2" />
                    <p className="text-2xl font-bold text-green-600">{broadcastResult.totalUsers}</p>
                    <p className="text-sm text-gray-600 dark:text-gray-300">Total Users</p>
                  </div>
                  <div className="text-center p-4 bg-blue-50/70 dark:bg-blue-900/20 rounded-lg">
                    <Smartphone className="h-8 w-8 text-blue-600 mx-auto mb-2" />
                    <p className="text-2xl font-bold text-blue-600">{broadcastResult.inAppSent}</p>
                    <p className="text-sm text-gray-600 dark:text-gray-300">In-App Sent</p>
                  </div>
                </div>
                {broadcastResult.emailSent > 0 && (
                  <div className="text-center p-4 bg-purple-50/70 dark:bg-purple-900/20 rounded-lg">
                    <Mail className="h-8 w-8 text-purple-600 mx-auto mb-2" />
                    <p className="text-2xl font-bold text-purple-600">{broadcastResult.emailSent}</p>
                    <p className="text-sm text-gray-600 dark:text-gray-300">Emails Sent</p>
                  </div>
                )}
                {broadcastResult.failed > 0 && (
                  <div className="text-center p-4 bg-red-50/70 dark:bg-red-900/20 rounded-lg">
                    <XCircle className="h-8 w-8 text-red-600 mx-auto mb-2" />
                    <p className="text-2xl font-bold text-red-600">{broadcastResult.failed}</p>
                    <p className="text-sm text-gray-600 dark:text-gray-300">Failed</p>
                  </div>
                )}
              </div>
              <DialogFooter>
                <Button onClick={() => setBroadcastResult(null)}>
                  Close
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        )}
      </div>
    </DashboardLayout>
  )
}
