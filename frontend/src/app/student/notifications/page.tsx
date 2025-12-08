'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { 
  Bell, 
  Mail, 
  MessageSquare, 
  CheckCircle,
  AlertCircle,
  Info,
  Loader2,
  CheckCheck,
  Filter,
  Settings,
  Smartphone
} from 'lucide-react'
import { useAuthStore } from '@/store/auth'
import { StudentRoute } from '@/components/route-guard'
import { studentAPI } from '@/lib/apiClient'
import { toast } from 'sonner'
import StudentNavigation from '@/components/student-navigation'

interface StudentNotification {
  id: number
  title: string
  message: string
  type: string
  isRead: boolean
  createdAt: string
  actionUrl: string
  actionText: string
}

interface StudentNotificationSettings {
  emailNotifications: boolean
  smsNotifications: boolean
  pushNotifications: boolean
  bookingConfirmations: boolean
  sessionReminders: boolean
  paymentNotifications: boolean
  feedbackReminders: boolean
  promotionalEmails: boolean
  reminderMinutesBeforeSession: number
  preferredNotificationTime: string
}

export default function StudentNotificationsPage() {
  const [activeTab, setActiveTab] = useState('notifications')
  const [loading, setLoading] = useState(true)
  const [notifications, setNotifications] = useState<StudentNotification[]>([])
  const [settings, setSettings] = useState<StudentNotificationSettings | null>(null)
  const [unreadCount, setUnreadCount] = useState(0)
  const [markingAsRead, setMarkingAsRead] = useState<number | null>(null)
  
  const user = useAuthStore((state) => state.user)
  const router = useRouter()

  useEffect(() => {
    fetchNotificationData()
  }, [])

  const fetchNotificationData = async () => {
    try {
      setLoading(true)
      
      // Fetch notifications
      const notificationsData = await studentAPI.getNotifications()
      setNotifications(notificationsData)
      
      // Fetch unread notifications for count
      const unreadData = await studentAPI.getUnreadNotifications()
      setUnreadCount(unreadData.length)
      
      // Mock settings for now (no API endpoint yet)
      setSettings({
        emailNotifications: true,
        smsNotifications: false,
        pushNotifications: true,
        bookingConfirmations: true,
        sessionReminders: true,
        paymentNotifications: true,
        feedbackReminders: false,
        promotionalEmails: false,
        reminderMinutesBeforeSession: 30,
        preferredNotificationTime: '09:00'
      })
      
    } catch (error) {
      console.error('Error fetching notification data:', error)
      toast.error('Failed to load notifications')
    } finally {
      setLoading(false)
    }
  }

  const handleMarkAsRead = async (notificationId: number) => {
    try {
      setMarkingAsRead(notificationId)
      await studentAPI.markAsRead(notificationId)
      
      // Update local state
      setNotifications(prev => prev.map(n => 
        n.id === notificationId ? { ...n, isRead: true } : n
      ))
      setUnreadCount(prev => Math.max(0, prev - 1))
      
      toast.success('Notification marked as read')
    } catch (error) {
      console.error('Error marking notification as read:', error)
      toast.error('Failed to mark notification as read')
    } finally {
      setMarkingAsRead(null)
    }
  }

  const handleMarkAllAsRead = async () => {
    try {
      await studentAPI.markAllAsRead()
      
      // Update local state
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })))
      setUnreadCount(0)
      
      toast.success('All notifications marked as read')
    } catch (error) {
      console.error('Error marking all notifications as read:', error)
      toast.error('Failed to mark all notifications as read')
    }
  }

  const handleUpdateSettings = async (updatedSettings: StudentNotificationSettings) => {
    try {
      await studentAPI.updateNotificationSettings(updatedSettings)
      setSettings(updatedSettings)
      toast.success('Notification settings updated successfully')
    } catch (error) {
      console.error('Error updating notification settings:', error)
      toast.error('Failed to update notification settings')
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

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'BOOKING_CONFIRMED':
        return <CheckCircle className="h-5 w-5 text-green-600" />
      case 'SESSION_REMINDER':
        return <Bell className="h-5 w-5 text-blue-600" />
      case 'PAYMENT_DUE':
        return <AlertCircle className="h-5 w-5 text-yellow-600" />
      case 'FEEDBACK_REMINDER':
        return <MessageSquare className="h-5 w-5 text-purple-600" />
      default:
        return <Info className="h-5 w-5 text-gray-600" />
    }
  }

  const getNotificationTypeLabel = (type: string) => {
    switch (type) {
      case 'BOOKING_CONFIRMED':
        return 'Booking Confirmed'
      case 'SESSION_REMINDER':
        return 'Session Reminder'
      case 'PAYMENT_DUE':
        return 'Payment Due'
      case 'FEEDBACK_REMINDER':
        return 'Feedback Reminder'
      default:
        return 'General'
    }
  }

  if (loading) {
    return (
      <StudentRoute>
        <StudentNavigation />
        <div className="min-h-screen bg-gray-50 py-8">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex items-center justify-center h-64">
              <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
              <span className="ml-2 text-gray-600">Loading notifications...</span>
            </div>
          </div>
        </div>
      </StudentRoute>
    )
  }

  return (
    <StudentRoute>
      <StudentNavigation />
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Header */}
          <div className="mb-8">
            <div className="flex justify-between items-center">
              <div>
                <h1 className="text-3xl font-bold text-gray-900">Notifications</h1>
                <p className="text-gray-600">Stay updated with your learning journey</p>
              </div>
              <div className="flex gap-2">
                {unreadCount > 0 && (
                  <Button
                    variant="outline"
                    onClick={handleMarkAllAsRead}
                    className="flex items-center gap-2"
                  >
                    <CheckCheck className="h-4 w-4" />
                    Mark All Read
                  </Button>
                )}
                <Button
                  variant="outline"
                  onClick={() => setActiveTab('settings')}
                  className="flex items-center gap-2"
                >
                  <Settings className="h-4 w-4" />
                  Settings
                </Button>
              </div>
            </div>
          </div>

          {/* Main Content Tabs */}
          <div className="space-y-6">
            {activeTab === 'notifications' && (
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Bell className="h-5 w-5 text-blue-600" />
                    Notifications
                    {unreadCount > 0 && (
                      <Badge variant="outline" className="bg-blue-50 text-blue-700 border-blue-200">
                        {unreadCount} unread
                      </Badge>
                    )}
                  </CardTitle>
                  <CardDescription>Your recent notifications and updates</CardDescription>
                </CardHeader>
                <CardContent>
                  {notifications.length === 0 ? (
                    <div className="text-center py-12">
                      <Bell className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                      <h3 className="text-lg font-medium text-gray-900 mb-2">No notifications</h3>
                      <p className="text-gray-500">You&apos;re all caught up! New notifications will appear here.</p>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {notifications.map((notification) => (
                        <div
                          key={notification.id}
                          className={`border rounded-lg p-4 cursor-pointer transition-colors ${
                            notification.isRead 
                              ? 'bg-gray-50 border-gray-200' 
                              : 'bg-blue-50 border-blue-200'
                          }`}
                          onClick={() => handleMarkAsRead(notification.id)}
                        >
                          <div className="flex items-start gap-3">
                            <div className="flex-shrink-0">
                              {getNotificationIcon(notification.type)}
                            </div>
                            <div className="flex-1 min-w-0">
                              <div className="flex items-center justify-between mb-1">
                                <h4 className="font-medium text-gray-900">{notification.title}</h4>
                                <div className="flex items-center gap-2">
                                  <Badge variant="outline" className="text-xs">
                                    {getNotificationTypeLabel(notification.type)}
                                  </Badge>
                                  {!notification.isRead && (
                                    <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                                  )}
                                </div>
                              </div>
                              <p className="text-sm text-gray-600 mb-2">{notification.message}</p>
                              <div className="flex items-center justify-between">
                                <p className="text-xs text-gray-500">
                                  {formatDateTime(notification.createdAt)}
                                </p>
                                <Button
                                  size="sm"
                                  variant="outline"
                                  onClick={(e) => {
                                    e.stopPropagation()
                                    router.push(notification.actionUrl)
                                  }}
                                >
                                  {notification.actionText}
                                </Button>
                              </div>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            )}

            {activeTab === 'settings' && settings && (
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Settings className="h-5 w-5 text-gray-600" />
                    Notification Settings
                  </CardTitle>
                  <CardDescription>Customize your notification preferences</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-6">
                    {/* General Notification Settings */}
                    <div>
                      <h3 className="text-lg font-medium mb-4">General Settings</h3>
                      <div className="space-y-4">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-3">
                            <Mail className="h-5 w-5 text-gray-600" />
                            <div>
                              <Label htmlFor="email-notifications">Email Notifications</Label>
                              <p className="text-sm text-gray-500">Receive notifications via email</p>
                            </div>
                          </div>
                          <Switch
                            id="email-notifications"
                            checked={settings.emailNotifications}
                            onCheckedChange={(checked) => 
                              handleUpdateSettings({ ...settings, emailNotifications: checked })
                            }
                          />
                        </div>

                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-3">
                            <MessageSquare className="h-5 w-5 text-gray-600" />
                            <div>
                              <Label htmlFor="sms-notifications">SMS Notifications</Label>
                              <p className="text-sm text-gray-500">Receive notifications via SMS</p>
                            </div>
                          </div>
                          <Switch
                            id="sms-notifications"
                            checked={settings.smsNotifications}
                            onCheckedChange={(checked) => 
                              handleUpdateSettings({ ...settings, smsNotifications: checked })
                            }
                          />
                        </div>

                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-3">
                            <Smartphone className="h-5 w-5 text-gray-600" />
                            <div>
                              <Label htmlFor="push-notifications">Push Notifications</Label>
                              <p className="text-sm text-gray-500">Receive push notifications on your device</p>
                            </div>
                          </div>
                          <Switch
                            id="push-notifications"
                            checked={settings.pushNotifications}
                            onCheckedChange={(checked) => 
                              handleUpdateSettings({ ...settings, pushNotifications: checked })
                            }
                          />
                        </div>
                      </div>
                    </div>

                    {/* Specific Notification Types */}
                    <div>
                      <h3 className="text-lg font-medium mb-4">Notification Types</h3>
                      <div className="space-y-4">
                        <div className="flex items-center justify-between">
                          <div>
                            <Label htmlFor="booking-confirmations">Booking Confirmations</Label>
                            <p className="text-sm text-gray-500">Get notified when your bookings are confirmed</p>
                          </div>
                          <Switch
                            id="booking-confirmations"
                            checked={settings.bookingConfirmations}
                            onCheckedChange={(checked) => 
                              handleUpdateSettings({ ...settings, bookingConfirmations: checked })
                            }
                          />
                        </div>

                        <div className="flex items-center justify-between">
                          <div>
                            <Label htmlFor="session-reminders">Session Reminders</Label>
                            <p className="text-sm text-gray-500">Get reminded before your sessions</p>
                          </div>
                          <Switch
                            id="session-reminders"
                            checked={settings.sessionReminders}
                            onCheckedChange={(checked) => 
                              handleUpdateSettings({ ...settings, sessionReminders: checked })
                            }
                          />
                        </div>

                        <div className="flex items-center justify-between">
                          <div>
                            <Label htmlFor="payment-notifications">Payment Notifications</Label>
                            <p className="text-sm text-gray-500">Get notified about payment updates</p>
                          </div>
                          <Switch
                            id="payment-notifications"
                            checked={settings.paymentNotifications}
                            onCheckedChange={(checked) => 
                              handleUpdateSettings({ ...settings, paymentNotifications: checked })
                            }
                          />
                        </div>

                        <div className="flex items-center justify-between">
                          <div>
                            <Label htmlFor="feedback-reminders">Feedback Reminders</Label>
                            <p className="text-sm text-gray-500">Get reminded to provide session feedback</p>
                          </div>
                          <Switch
                            id="feedback-reminders"
                            checked={settings.feedbackReminders}
                            onCheckedChange={(checked) => 
                              handleUpdateSettings({ ...settings, feedbackReminders: checked })
                            }
                          />
                        </div>

                        <div className="flex items-center justify-between">
                          <div>
                            <Label htmlFor="promotional-emails">Promotional Emails</Label>
                            <p className="text-sm text-gray-500">Receive promotional offers and updates</p>
                          </div>
                          <Switch
                            id="promotional-emails"
                            checked={settings.promotionalEmails}
                            onCheckedChange={(checked) => 
                              handleUpdateSettings({ ...settings, promotionalEmails: checked })
                            }
                          />
                        </div>
                      </div>
                    </div>

                    {/* Reminder Settings */}
                    <div>
                      <h3 className="text-lg font-medium mb-4">Reminder Settings</h3>
                      <div className="space-y-4">
                        <div>
                          <Label htmlFor="reminder-minutes">Reminder Time Before Session</Label>
                          <p className="text-sm text-gray-500 mb-2">
                            Get reminded {settings.reminderMinutesBeforeSession} minutes before your session
                          </p>
                          <select
                            id="reminder-minutes"
                            value={settings.reminderMinutesBeforeSession}
                            onChange={(e) => 
                              handleUpdateSettings({ 
                                ...settings, 
                                reminderMinutesBeforeSession: parseInt(e.target.value) 
                              })
                            }
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                          >
                            <option value={5}>5 minutes</option>
                            <option value={15}>15 minutes</option>
                            <option value={30}>30 minutes</option>
                            <option value={60}>1 hour</option>
                            <option value={120}>2 hours</option>
                          </select>
                        </div>

                        <div>
                          <Label htmlFor="preferred-time">Preferred Notification Time</Label>
                          <p className="text-sm text-gray-500 mb-2">
                            Receive non-urgent notifications at {settings.preferredNotificationTime}
                          </p>
                          <input
                            id="preferred-time"
                            type="time"
                            value={settings.preferredNotificationTime}
                            onChange={(e) => 
                              handleUpdateSettings({ 
                                ...settings, 
                                preferredNotificationTime: e.target.value 
                              })
                            }
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                          />
                        </div>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            )}
          </div>
        </div>
      </div>
    </StudentRoute>
  )
}
