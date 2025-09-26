'use client'

import { useState, useEffect } from 'react'
import AdminLayoutSimple from '@/components/admin-layout-simple'
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
  Filter, 
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
  
  // Form states
  const [formData, setFormData] = useState({
    title: '',
    body: '',
    audience: 'STUDENT',
    delivery: 'IN_APP'
  })

  useEffect(() => {
    const checkAuthAndFetch = () => {
      const token = localStorage.getItem('accessToken')
      if (token) {
        fetchNotifications()
        fetchStats()
      } else {
        setTimeout(checkAuthAndFetch, 100)
      }
    }
    checkAuthAndFetch()
  }, [])

  const fetchNotifications = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      const params = new URLSearchParams()
      if (audienceFilter) params.append('audience', audienceFilter)
      if (statusFilter) params.append('status', statusFilter)
      
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/notifications?${params}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (response.ok) {
        const data = await response.json()
        setNotifications(data.content || data)
      }
    } catch (error) {
      console.error('Error fetching notifications:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchStats = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/notifications/stats`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (response.ok) {
        const data = await response.json()
        setStats(data)
      }
    } catch (error) {
      console.error('Error fetching notification stats:', error)
    }
  }

  const handleSendNotification = async () => {
    try {
      setSending(true)
      const token = localStorage.getItem('accessToken')
      
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/notifications/broadcast`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
      })

      if (response.ok) {
        const result = await response.json()
        setBroadcastResult(result)
        setShowComposeDialog(false)
        setFormData({
          title: '',
          body: '',
          audience: 'STUDENT',
          delivery: 'IN_APP'
        })
        fetchNotifications()
        fetchStats()
      }
    } catch (error) {
      console.error('Error sending notification:', error)
    } finally {
      setSending(false)
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'SENT':
        return <CheckCircle className="h-4 w-4 text-green-500" />
      case 'FAILED':
        return <XCircle className="h-4 w-4 text-red-500" />
      case 'QUEUED':
        return <Clock className="h-4 w-4 text-yellow-500" />
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
      case 'QUEUED':
        return `${baseClasses} bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200`
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
      case 'BOTH':
        return `${baseClasses} bg-indigo-100 text-indigo-800 dark:bg-indigo-900 dark:text-indigo-200`
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
      case 'BOTH':
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
    const matchesSearch = notification.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         notification.body.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesAudience = !audienceFilter || notification.audience === audienceFilter
    const matchesStatus = !statusFilter || notification.status === statusFilter
    
    return matchesSearch && matchesAudience && matchesStatus
  })

  if (loading) {
    return (
      <AdminLayoutSimple>
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-2"></div>
            <p className="text-gray-600 dark:text-gray-400">Loading notifications...</p>
          </div>
        </div>
      </AdminLayoutSimple>
    )
  }

  return (
    <AdminLayoutSimple>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Notifications</h1>
            <p className="text-gray-600 dark:text-gray-400">Send notifications to students and teachers</p>
          </div>
          <Button 
            onClick={() => setShowComposeDialog(true)}
            className="flex items-center space-x-2"
          >
            <Plus className="h-4 w-4" />
            <span>Compose</span>
          </Button>
        </div>

        {/* Stats Cards */}
        {stats && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
            <Card className="p-6">
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

            <Card className="p-6">
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

            <Card className="p-6">
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

            <Card className="p-6">
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

            <Card className="p-6">
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

        {/* Search and Filters */}
        <Card className="p-6">
          <div className="flex items-center space-x-4">
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
            <div className="flex space-x-2">
              <Select value={audienceFilter} onValueChange={setAudienceFilter}>
                <SelectTrigger className="w-40">
                  <SelectValue placeholder="Audience" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Audiences</SelectItem>
                  <SelectItem value="STUDENT">Students</SelectItem>
                  <SelectItem value="TEACHER">Teachers</SelectItem>
                  <SelectItem value="BOTH">Both</SelectItem>
                </SelectContent>
              </Select>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="w-40">
                  <SelectValue placeholder="Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="SENT">Sent</SelectItem>
                  <SelectItem value="QUEUED">Queued</SelectItem>
                  <SelectItem value="FAILED">Failed</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="text-sm text-gray-500 dark:text-gray-400">
              {filteredNotifications.length} notifications
            </div>
          </div>
        </Card>

        {/* Notification History */}
        <Card className="p-6">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Notification History</h3>
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
              <div className="overflow-hidden border border-gray-200 dark:border-gray-700 rounded-lg">
                <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                  <thead className="bg-gray-50 dark:bg-gray-800">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Notification
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Audience
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Delivery
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Sent
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Status
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
                    {filteredNotifications.map((notification) => (
                      <tr key={notification.id}>
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
                              {notification.delivery.replace('_', ' ')}
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
        <Dialog open={showComposeDialog} onOpenChange={setShowComposeDialog}>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>Compose Notification</DialogTitle>
              <DialogDescription>
                Send a notification to students and teachers
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="audience">Target Audience</Label>
                  <Select value={formData.audience} onValueChange={(value) => setFormData({...formData, audience: value})}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select audience" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="STUDENT">Students Only</SelectItem>
                      <SelectItem value="TEACHER">Teachers Only</SelectItem>
                      <SelectItem value="BOTH">Students & Teachers</SelectItem>
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
                      <SelectItem value="BOTH">In-App & Email</SelectItem>
                    </SelectContent>
                  </Select>
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
                disabled={!formData.title || !formData.body || sending}
              >
                {sending ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
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
          <DialogContent className="max-w-lg">
            <DialogHeader>
              <DialogTitle>Notification Preview</DialogTitle>
              <DialogDescription>
                This is how your notification will appear
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="border rounded-lg p-4 bg-gray-50 dark:bg-gray-800">
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
                      <span>Audience: {formData.audience}</span>
                      <span>Delivery: {formData.delivery.replace('_', ' ')}</span>
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
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Notification Sent Successfully</DialogTitle>
                <DialogDescription>
                  Your notification has been broadcast to the target audience
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="text-center p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                    <Users className="h-8 w-8 text-green-600 mx-auto mb-2" />
                    <p className="text-2xl font-bold text-green-600">{broadcastResult.totalUsers}</p>
                    <p className="text-sm text-gray-600">Total Users</p>
                  </div>
                  <div className="text-center p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                    <Smartphone className="h-8 w-8 text-blue-600 mx-auto mb-2" />
                    <p className="text-2xl font-bold text-blue-600">{broadcastResult.inAppSent}</p>
                    <p className="text-sm text-gray-600">In-App Sent</p>
                  </div>
                </div>
                {broadcastResult.emailSent > 0 && (
                  <div className="text-center p-4 bg-purple-50 dark:bg-purple-900/20 rounded-lg">
                    <Mail className="h-8 w-8 text-purple-600 mx-auto mb-2" />
                    <p className="text-2xl font-bold text-purple-600">{broadcastResult.emailSent}</p>
                    <p className="text-sm text-gray-600">Emails Sent</p>
                  </div>
                )}
                {broadcastResult.failed > 0 && (
                  <div className="text-center p-4 bg-red-50 dark:bg-red-900/20 rounded-lg">
                    <XCircle className="h-8 w-8 text-red-600 mx-auto mb-2" />
                    <p className="text-2xl font-bold text-red-600">{broadcastResult.failed}</p>
                    <p className="text-sm text-gray-600">Failed</p>
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
    </AdminLayoutSimple>
  )
}
