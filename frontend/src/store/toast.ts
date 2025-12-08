'use client';

import { create } from 'zustand';

interface Toast {
  id: string;
  type: 'success' | 'error' | 'info' | 'warning';
  title: string;
  message?: string;
  duration?: number;
}

interface ToastStore {
  toasts: Toast[];
  addToast: (toast: Omit<Toast, 'id'>) => void;
  removeToast: (id: string) => void;
  clearToasts: () => void;
}

export const useToastStore = create<ToastStore>((set) => ({
  toasts: [],
  addToast: (toast) => {
    const id = Math.random().toString(36).substr(2, 9);
    set((state) => ({
      toasts: [...state.toasts, { ...toast, id }],
    }));
  },
  removeToast: (id) => {
    set((state) => ({
      toasts: state.toasts.filter((toast) => toast.id !== id),
    }));
  },
  clearToasts: () => {
    set({ toasts: [] });
  },
}));

// Convenience functions for different toast types
export const toast = {
  success: (title: string, message?: string, duration?: number) => {
    useToastStore.getState().addToast({
      type: 'success',
      title,
      message,
      duration,
    });
  },
  error: (title: string, message?: string, duration?: number) => {
    useToastStore.getState().addToast({
      type: 'error',
      title,
      message,
      duration,
    });
  },
  info: (title: string, message?: string, duration?: number) => {
    useToastStore.getState().addToast({
      type: 'info',
      title,
      message,
      duration,
    });
  },
  warning: (title: string, message?: string, duration?: number) => {
    useToastStore.getState().addToast({
      type: 'warning',
      title,
      message,
      duration,
    });
  },
};
