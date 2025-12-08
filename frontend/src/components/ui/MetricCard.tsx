'use client';

import React from 'react';
import { LucideIcon, TrendingUp, TrendingDown, Minus } from 'lucide-react';

interface MetricCardProps {
  title: string;
  value: string | number;
  change?: number;
  changeLabel?: string;
  icon: LucideIcon;
  iconBgColor?: string;
  iconColor?: string;
  href?: string;
}

export default function MetricCard({
  title,
  value,
  change,
  changeLabel = 'from last month',
  icon: Icon,
  iconBgColor = 'bg-ankur-primary',
  iconColor = 'text-white',
  href,
}: MetricCardProps) {
  const getTrendIcon = () => {
    if (change === undefined || change === 0) return <Minus className="w-4 h-4" />;
    return change > 0 ? <TrendingUp className="w-4 h-4" /> : <TrendingDown className="w-4 h-4" />;
  };

  const getTrendColor = () => {
    if (change === undefined || change === 0) return 'text-gray-500 bg-gray-100';
    return change > 0 ? 'text-green-600 bg-green-50' : 'text-red-600 bg-red-50';
  };

  const Content = (
    <div className="bg-white rounded-xl p-6 border border-gray-100 shadow-sm hover:shadow-md transition-all duration-200 group">
      <div className="flex items-start justify-between mb-4">
        <div className={`w-12 h-12 rounded-xl ${iconBgColor} flex items-center justify-center ${iconColor} group-hover:scale-110 transition-transform duration-200`}>
          <Icon className="w-6 h-6" />
        </div>
        {change !== undefined && (
          <div className={`flex items-center gap-1 px-2.5 py-1 rounded-full text-sm font-medium ${getTrendColor()}`}>
            {getTrendIcon()}
            <span>{Math.abs(change)}%</span>
          </div>
        )}
      </div>
      <div>
        <p className="text-gray-500 text-sm font-medium mb-1">{title}</p>
        <p className="text-2xl font-bold text-gray-900">{value}</p>
        {changeLabel && change !== undefined && (
          <p className="text-xs text-gray-400 mt-1">{changeLabel}</p>
        )}
      </div>
    </div>
  );

  if (href) {
    return (
      <a href={href} className="block">
        {Content}
      </a>
    );
  }

  return Content;
}
