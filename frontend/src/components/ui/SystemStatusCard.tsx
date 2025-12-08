'use client';

import React from 'react';
import { CheckCircle, AlertTriangle, XCircle, Wifi } from 'lucide-react';

export type SystemStatus = 'operational' | 'degraded' | 'down';

interface SystemStatusItem {
  name: string;
  status: SystemStatus;
  latency?: string;
  uptime?: string;
}

interface SystemStatusCardProps {
  items: SystemStatusItem[];
  title?: string;
}

const statusConfig = {
  operational: {
    icon: CheckCircle,
    label: 'Operational',
    color: 'text-green-600',
    bgColor: 'bg-green-50',
    dotColor: 'bg-green-500',
  },
  degraded: {
    icon: AlertTriangle,
    label: 'Degraded',
    color: 'text-yellow-600',
    bgColor: 'bg-yellow-50',
    dotColor: 'bg-yellow-500',
  },
  down: {
    icon: XCircle,
    label: 'Down',
    color: 'text-red-600',
    bgColor: 'bg-red-50',
    dotColor: 'bg-red-500',
  },
};

export default function SystemStatusCard({
  items,
  title = 'System Status',
}: SystemStatusCardProps) {
  const allOperational = items.every((item) => item.status === 'operational');

  return (
    <div className="bg-white rounded-xl border border-gray-100 shadow-sm">
      <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
        <h3 className="font-semibold text-gray-900">{title}</h3>
        {allOperational && (
          <span className="flex items-center gap-1.5 text-sm text-green-600">
            <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
            All Systems Operational
          </span>
        )}
      </div>
      <div className="p-6 space-y-4">
        {items.map((item) => {
          const config = statusConfig[item.status];
          const Icon = config.icon;

          return (
            <div
              key={item.name}
              className="flex items-center justify-between py-2"
            >
              <div className="flex items-center gap-3">
                <div className={`w-2 h-2 rounded-full ${config.dotColor}`} />
                <span className="text-sm font-medium text-gray-900">{item.name}</span>
              </div>
              <div className="flex items-center gap-4">
                {item.latency && (
                  <span className="text-xs text-gray-400">{item.latency}</span>
                )}
                {item.uptime && (
                  <span className="text-xs text-gray-500">
                    <Wifi className="inline w-3 h-3 mr-1" />
                    {item.uptime}
                  </span>
                )}
                <div className={`flex items-center gap-1.5 px-2.5 py-1 rounded-full ${config.bgColor}`}>
                  <Icon className={`w-3.5 h-3.5 ${config.color}`} />
                  <span className={`text-xs font-medium ${config.color}`}>
                    {config.label}
                  </span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
