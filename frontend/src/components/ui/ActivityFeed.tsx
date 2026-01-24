'use client';

import React from 'react';
import { CheckCircle, AlertCircle, XCircle, Clock } from 'lucide-react';

export type ActivityType = 'success' | 'warning' | 'error' | 'info' | 'pending';

interface ActivityItem {
  id: string;
  type: ActivityType;
  title: string;
  description?: string;
  timestamp: string;
  user?: string;
}

interface ActivityFeedProps {
  items: ActivityItem[];
  maxItems?: number;
  showViewAll?: boolean;
  onViewAll?: () => void;
}

const activityConfig = {
  success: {
    icon: CheckCircle,
    dotColor: 'bg-green-500',
    iconColor: 'text-green-500',
  },
  warning: {
    icon: AlertCircle,
    dotColor: 'bg-yellow-500',
    iconColor: 'text-yellow-500',
  },
  error: {
    icon: XCircle,
    dotColor: 'bg-red-500',
    iconColor: 'text-red-500',
  },
  info: {
    icon: Clock,
    dotColor: 'bg-blue-500',
    iconColor: 'text-blue-500',
  },
  pending: {
    icon: Clock,
    dotColor: 'bg-gray-400',
    iconColor: 'text-gray-400',
  },
};

export default function ActivityFeed({
  items,
  maxItems = 5,
  showViewAll = true,
  onViewAll,
}: ActivityFeedProps) {
  const displayItems = items.slice(0, maxItems);

  return (
    <div className="bg-white rounded-xl border border-gray-100 shadow-sm">
      <div className="px-6 py-4 border-b border-gray-100">
        <h3 className="font-semibold text-gray-900">Recent Activity</h3>
      </div>
      <div className="divide-y divide-gray-100">
        {displayItems.map((item) => {
          const config = activityConfig[item.type];
          const Icon = config.icon;
          
          return (
            <div key={item.id} className="px-6 py-4 hover:bg-gray-50 transition-colors">
              <div className="flex items-start gap-3">
                <div className={`mt-0.5 w-2 h-2 rounded-full ${config.dotColor} flex-shrink-0`} />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">{item.title}</p>
                  {item.description && (
                    <p className="text-sm text-gray-500 mt-0.5 line-clamp-2">{item.description}</p>
                  )}
                  <div className="flex items-center gap-2 mt-1.5">
                    <span className="text-xs text-gray-400">{item.timestamp}</span>
                    {item.user && (
                      <>
                        <span className="text-gray-300">|</span>
                        <span className="text-xs text-gray-500">{item.user}</span>
                      </>
                    )}
                  </div>
                </div>
                <Icon className={`w-5 h-5 ${config.iconColor} flex-shrink-0`} />
              </div>
            </div>
          );
        })}
      </div>
      {showViewAll && items.length > maxItems && (
        <div className="px-6 py-3 border-t border-gray-100">
          <button
            onClick={onViewAll}
            className="text-sm text-ankur-primary hover:text-ankur-primary-dark font-medium"
          >
            View all activity &gt;
          </button>
        </div>
      )}
      {items.length === 0 && (
        <div className="px-6 py-8 text-center">
          <Clock className="w-10 h-10 text-gray-300 mx-auto mb-2" />
          <p className="text-gray-500 text-sm">No recent activity</p>
        </div>
      )}
    </div>
  );
}
