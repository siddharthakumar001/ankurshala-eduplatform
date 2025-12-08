'use client';

import React from 'react';
import { LucideIcon, ArrowRight } from 'lucide-react';

interface QuickActionCardProps {
  title: string;
  description?: string;
  icon: LucideIcon;
  iconBgColor?: string;
  iconColor?: string;
  onClick?: () => void;
  href?: string;
}

export default function QuickActionCard({
  title,
  description,
  icon: Icon,
  iconBgColor = 'bg-gray-50',
  iconColor = 'text-gray-600',
  onClick,
  href,
}: QuickActionCardProps) {
  const Content = (
    <div className="bg-white rounded-xl p-5 border border-gray-100 shadow-sm hover:shadow-md hover:border-ankur-primary/30 transition-all duration-200 cursor-pointer group">
      <div className="flex flex-col items-center text-center gap-3">
        <div className={`w-14 h-14 rounded-xl ${iconBgColor} flex items-center justify-center ${iconColor} group-hover:scale-110 transition-transform duration-200`}>
          <Icon className="w-7 h-7" />
        </div>
        <div>
          <h3 className="font-semibold text-gray-900 group-hover:text-ankur-primary transition-colors">
            {title}
          </h3>
          {description && (
            <p className="text-sm text-gray-500 mt-1">{description}</p>
          )}
        </div>
        <ArrowRight className="w-5 h-5 text-gray-300 group-hover:text-ankur-primary group-hover:translate-x-1 transition-all duration-200" />
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

  return (
    <button onClick={onClick} className="block w-full text-left">
      {Content}
    </button>
  );
}
