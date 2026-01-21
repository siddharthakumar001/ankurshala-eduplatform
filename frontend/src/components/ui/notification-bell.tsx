'use client';

import { useState, useEffect } from 'react';
import { Bell, BellRing } from 'lucide-react';
import { useAuthStore } from '@/store/auth';
import { studentAPI } from '@/lib/apiClient';

interface Notification {
  id: number;
  type: string;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

export const NotificationBell = () => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const { user } = useAuthStore();

  const fetchNotifications = async () => {
    if (!user) return;
    
    setIsLoading(true);
    setError('');
    try {
      const data = await studentAPI.getNotifications();
      if (Array.isArray(data)) {
        setNotifications(data);
      } else {
        setNotifications(data?.content ?? []);
      }
    } catch (error) {
      console.error('Error fetching notifications:', error);
      setError('Notifications are temporarily unavailable.');
      setNotifications([]);
    } finally {
      setIsLoading(false);
    }
  };

  const markAsRead = async (notificationIds: number[]) => {
    if (!user) return;
    
    try {
      // Mark each notification as read
      for (const id of notificationIds) {
        await studentAPI.markAsRead(id);
      }
      setNotifications(prev => 
        prev.map(n => 
          notificationIds.includes(n.id) ? { ...n, isRead: true } : n
        )
      );
    } catch (error) {
      console.error('Error marking notifications as read:', error);
    }
  };

  useEffect(() => {
    fetchNotifications();
    // Poll for new notifications every 30 seconds
    const interval = setInterval(fetchNotifications, 30000);
    return () => clearInterval(interval);
  }, [user]);

  const unreadCount = notifications.filter(n => !n.isRead).length;

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg dark:text-gray-200 dark:hover:text-white dark:hover:bg-slate-800/60"
      >
        {unreadCount > 0 ? (
          <BellRing className="w-6 h-6 text-blue-600" />
        ) : (
          <Bell className="w-6 h-6" />
        )}
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 bg-white/90 rounded-lg shadow-lg border border-white/60 z-50 backdrop-blur dark:bg-slate-900/80 dark:border-white/10">
          <div className="p-4 border-b border-white/40 dark:border-white/10">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">Notifications</h3>
          </div>
          
          <div className="max-h-96 overflow-y-auto">
            {isLoading ? (
              <div className="p-4 text-center text-gray-500 dark:text-gray-300">Loading...</div>
            ) : error ? (
              <div className="p-4 text-center text-slate-600 dark:text-slate-300">
                <p className="font-medium">{error}</p>
                <button
                  onClick={fetchNotifications}
                  className="mt-2 text-sm text-blue-600 hover:text-blue-800"
                >
                  Try again
                </button>
              </div>
            ) : notifications.length === 0 ? (
              <div className="p-4 text-center text-gray-500 dark:text-gray-300">
                You're all caught up. No notifications right now.
              </div>
            ) : (
              notifications.map((notification) => (
                <div
                  key={notification.id}
                  className={`p-4 border-b border-white/40 dark:border-white/10 hover:bg-gray-50 dark:hover:bg-slate-800/60 ${
                    !notification.isRead ? 'bg-blue-50 dark:bg-blue-500/10' : ''
                  }`}
                >
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <p className="font-medium text-sm text-gray-900 dark:text-gray-100">
                        {notification.title}
                      </p>
                      <p className="text-sm text-gray-600 dark:text-gray-300 mt-1">
                        {notification.message}
                      </p>
                      <p className="text-xs text-gray-400 dark:text-gray-400 mt-2">
                        {new Date(notification.createdAt).toLocaleString()}
                      </p>
                    </div>
                    {!notification.isRead && (
                      <button
                        onClick={() => markAsRead([notification.id])}
                        className="ml-2 text-xs text-blue-600 hover:text-blue-800"
                      >
                        Mark as read
                      </button>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
          
          {notifications.length > 0 && (
            <div className="p-4 border-t border-white/40 dark:border-white/10">
              <button
                onClick={() => markAsRead(notifications.map(n => n.id))}
                className="text-sm text-blue-600 hover:text-blue-800"
              >
                Mark all as read
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
