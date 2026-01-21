'use client'

import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Loader2, Bell } from 'lucide-react'
import { api } from '@/utils/api'
import { toast } from 'sonner'
import { TeacherRoute } from '@/components/route-guard'

interface NotificationItem {
  id: number
  type: string
  title: string
  message: string
  read: boolean
  createdAt: string
}

export default function TeacherNotificationsPage() {
  const [loading, setLoading] = useState(true)
  const [notifications, setNotifications] = useState<NotificationItem[]>([])
  const [unreadCount, setUnreadCount] = useState(0)

  const fetchNotifications = async () => {
    try {
      setLoading(true)
      const [notificationsResponse, unreadResponse] = await Promise.all([
        api.get<NotificationItem[]>('/notifications'),
        api.get<number>('/notifications/unread-count')
      ])
      setNotifications(notificationsResponse.data || [])
      setUnreadCount(unreadResponse.data || 0)
    } catch (error) {
      console.error('Error fetching notifications:', error)
      toast.error('Failed to load notifications')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchNotifications()
  }, [])

  const markAllAsRead = async () => {
    try {
      await api.post('/notifications/mark-read', {})
      toast.success('All notifications marked as read')
      fetchNotifications()
    } catch (error) {
      console.error('Error marking notifications:', error)
      toast.error('Failed to mark notifications as read')
    }
  }

  const markAsRead = async (notificationId: number) => {
    try {
      await api.post('/notifications/mark-read', { notificationIds: [notificationId] })
      setNotifications((prev) =>
        prev.map((item) => (item.id === notificationId ? { ...item, read: true } : item))
      )
      setUnreadCount((prev) => Math.max(prev - 1, 0))
    } catch (error) {
      console.error('Error marking notification as read:', error)
      toast.error('Failed to mark notification as read')
    }
  }

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  if (loading) {
    return (
      <TeacherRoute>
        <div className="space-y-6">
          <div className="glass-panel p-6 flex items-center justify-center">
            <Loader2 className="h-6 w-6 animate-spin text-emerald-500" />
            <span className="ml-2 text-slate-600 dark:text-slate-200">Loading notifications...</span>
          </div>
        </div>
      </TeacherRoute>
    )
  }

  return (
    <TeacherRoute>
      <div className="space-y-6">
        <div className="page-header">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              <h1 className="text-2xl md:text-3xl font-bold text-white">Notifications</h1>
              <p className="text-white/80">Stay updated with booking and platform updates</p>
            </div>
            <div className="flex items-center gap-2">
              <Badge variant="outline" className="border-white/40 bg-white/10 text-white">
                {unreadCount} unread
              </Badge>
              <Button
                variant="outline"
                className="btn-outline"
                onClick={markAllAsRead}
                disabled={unreadCount === 0}
              >
                Mark all read
              </Button>
            </div>
          </div>
        </div>

        <Card className="glass-panel border border-white/40">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Bell className="h-5 w-5 text-emerald-500" />
              Recent Notifications
            </CardTitle>
            <CardDescription>Your latest updates and alerts</CardDescription>
          </CardHeader>
          <CardContent>
            {notifications.length === 0 ? (
              <div className="text-center py-12">
                <p className="text-slate-500 dark:text-slate-300">You are all caught up.</p>
              </div>
            ) : (
              <div className="space-y-4">
                {notifications.map((notification) => (
                  <div
                    key={notification.id}
                    className={`rounded-2xl p-4 border ${
                      notification.read
                        ? 'glass border-white/30'
                        : 'glass border-emerald-300/50 bg-emerald-500/10'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <div className="flex items-center gap-2 mb-1">
                          <h3 className="font-semibold text-slate-900 dark:text-white">{notification.title}</h3>
                          {!notification.read && (
                            <Badge variant="outline" className="border-emerald-300/60 bg-emerald-500/10 text-emerald-600">
                              New
                            </Badge>
                          )}
                        </div>
                        <p className="text-slate-700 dark:text-slate-200 text-sm">{notification.message}</p>
                        <p className="text-xs text-slate-500 dark:text-slate-300 mt-2">{formatDateTime(notification.createdAt)}</p>
                      </div>
                      {!notification.read && (
                        <Button
                          variant="outline"
                          size="sm"
                          className="btn-outline"
                          onClick={() => markAsRead(notification.id)}
                        >
                          Mark read
                        </Button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </TeacherRoute>
  )
}
