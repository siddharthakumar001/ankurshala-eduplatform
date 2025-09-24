'use client'

import AdminLayoutSimple from '@/components/admin-layout-simple'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Bell, Send, Plus, Download, Filter } from 'lucide-react'

export default function AdminNotificationsPage() {
  return (
    <AdminLayoutSimple>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Notifications</h1>
            <p className="text-gray-600 dark:text-gray-400">Send notifications to students and teachers</p>
          </div>
          <Button className="flex items-center space-x-2">
            <Plus className="h-4 w-4" />
            <span>Compose</span>
          </Button>
        </div>

        {/* Compose Notification */}
        <Card className="p-6">
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Compose Notification</h3>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Target Audience
                </label>
                <select className="w-full border border-gray-300 dark:border-gray-600 rounded-md px-3 py-2 dark:bg-gray-700 dark:text-white">
                  <option>All Students</option>
                  <option>All Teachers</option>
                  <option>Specific Users</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Notification Type
                </label>
                <select className="w-full border border-gray-300 dark:border-gray-600 rounded-md px-3 py-2 dark:bg-gray-700 dark:text-white">
                  <option>Information</option>
                  <option>Alert</option>
                  <option>Announcement</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Title
              </label>
              <input
                type="text"
                placeholder="Enter notification title..."
                className="w-full border border-gray-300 dark:border-gray-600 rounded-md px-3 py-2 dark:bg-gray-700 dark:text-white"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Message
              </label>
              <textarea
                rows={4}
                placeholder="Enter notification message..."
                className="w-full border border-gray-300 dark:border-gray-600 rounded-md px-3 py-2 dark:bg-gray-700 dark:text-white"
              />
            </div>

            <div className="flex justify-end space-x-3">
              <Button variant="outline">Preview</Button>
              <Button className="flex items-center space-x-2">
                <Send className="h-4 w-4" />
                <span>Send Notification</span>
              </Button>
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
                  <Filter className="h-4 w-4" />
                  <span>Filter</span>
                </Button>
                <Button variant="outline" size="sm" className="flex items-center space-x-2">
                  <Download className="h-4 w-4" />
                  <span>Export</span>
                </Button>
              </div>
            </div>
            
            {/* History Table */}
            <div className="overflow-hidden border border-gray-200 dark:border-gray-700 rounded-lg">
              <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead className="bg-gray-50 dark:bg-gray-800">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                      Notification
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                      Target
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                      Type
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
                  {[
                    { 
                      title: 'New Course Available', 
                      message: 'Check out our new mathematics course for Grade 8 students...',
                      target: 'All Students',
                      type: 'Information',
                      sent: '2 hours ago',
                      status: 'Delivered'
                    },
                    { 
                      title: 'System Maintenance Notice', 
                      message: 'The platform will be under maintenance from 2 AM to 4 AM...',
                      target: 'All Users',
                      type: 'Alert',
                      sent: '1 day ago',
                      status: 'Delivered'
                    },
                    { 
                      title: 'Teacher Training Session', 
                      message: 'Join us for a training session on new features...',
                      target: 'All Teachers',
                      type: 'Announcement',
                      sent: '3 days ago',
                      status: 'Delivered'
                    },
                  ].map((notification, i) => (
                    <tr key={i}>
                      <td className="px-6 py-4">
                        <div className="flex items-start">
                          <Bell className="h-5 w-5 text-blue-500 mr-3 mt-0.5" />
                          <div>
                            <div className="text-sm font-medium text-gray-900 dark:text-white">
                              {notification.title}
                            </div>
                            <div className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                              {notification.message}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                        {notification.target}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200">
                          {notification.type}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                        {notification.sent}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200">
                          {notification.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </Card>
      </div>
    </AdminLayoutSimple>
  )
}
