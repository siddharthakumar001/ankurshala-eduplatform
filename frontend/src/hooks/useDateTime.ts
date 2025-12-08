'use client';

import { useMemo } from 'react';

export const useDateTime = () => {
  const formatDate = (date: string | Date, options?: Intl.DateTimeFormatOptions) => {
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    return dateObj.toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      ...options
    });
  };

  const formatTime = (date: string | Date, options?: Intl.DateTimeFormatOptions) => {
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    return dateObj.toLocaleTimeString('en-IN', {
      hour: '2-digit',
      minute: '2-digit',
      ...options
    });
  };

  const formatDateTime = (date: string | Date, options?: Intl.DateTimeFormatOptions) => {
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    return dateObj.toLocaleString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      ...options
    });
  };

  const getRelativeTime = (date: string | Date) => {
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - dateObj.getTime()) / 1000);

    if (diffInSeconds < 60) {
      return 'Just now';
    } else if (diffInSeconds < 3600) {
      const minutes = Math.floor(diffInSeconds / 60);
      return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
    } else if (diffInSeconds < 86400) {
      const hours = Math.floor(diffInSeconds / 3600);
      return `${hours} hour${hours > 1 ? 's' : ''} ago`;
    } else if (diffInSeconds < 2592000) {
      const days = Math.floor(diffInSeconds / 86400);
      return `${days} day${days > 1 ? 's' : ''} ago`;
    } else {
      return formatDate(dateObj);
    }
  };

  const isToday = (date: string | Date) => {
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    const today = new Date();
    return dateObj.toDateString() === today.toDateString();
  };

  const isTomorrow = (date: string | Date) => {
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return dateObj.toDateString() === tomorrow.toDateString();
  };

  const isPast = (date: string | Date) => {
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    return dateObj.getTime() < new Date().getTime();
  };

  const isFuture = (date: string | Date) => {
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    return dateObj.getTime() > new Date().getTime();
  };

  const addMinutes = (date: string | Date, minutes: number) => {
    const dateObj = typeof date === 'string' ? new Date(date) : new Date(date);
    dateObj.setMinutes(dateObj.getMinutes() + minutes);
    return dateObj;
  };

  const addHours = (date: string | Date, hours: number) => {
    const dateObj = typeof date === 'string' ? new Date(date) : new Date(date);
    dateObj.setHours(dateObj.getHours() + hours);
    return dateObj;
  };

  const addDays = (date: string | Date, days: number) => {
    const dateObj = typeof date === 'string' ? new Date(date) : new Date(date);
    dateObj.setDate(dateObj.getDate() + days);
    return dateObj;
  };

  const getTimeSlots = (startTime: string, endTime: string, durationMinutes: number = 30) => {
    const slots = [];
    const start = new Date(`2000-01-01T${startTime}`);
    const end = new Date(`2000-01-01T${endTime}`);
    
    let current = new Date(start);
    while (current < end) {
      const slotEnd = new Date(current);
      slotEnd.setMinutes(slotEnd.getMinutes() + durationMinutes);
      
      if (slotEnd <= end) {
        slots.push({
          start: current.toTimeString().slice(0, 5),
          end: slotEnd.toTimeString().slice(0, 5)
        });
      }
      
      current.setMinutes(current.getMinutes() + durationMinutes);
    }
    
    return slots;
  };

  const getWeekdayName = (weekday: number) => {
    const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    return days[weekday] || 'Unknown';
  };

  const getWeekdayShortName = (weekday: number) => {
    const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    return days[weekday] || 'Unknown';
  };

  return {
    formatDate,
    formatTime,
    formatDateTime,
    getRelativeTime,
    isToday,
    isTomorrow,
    isPast,
    isFuture,
    addMinutes,
    addHours,
    addDays,
    getTimeSlots,
    getWeekdayName,
    getWeekdayShortName
  };
};
