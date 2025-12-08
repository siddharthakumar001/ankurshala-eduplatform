'use client';

import { useEffect } from 'react';
import { useToastStore } from '@/store/toast';
import { CheckCircle, XCircle, Info, AlertTriangle, X } from 'lucide-react';

const ToastComponent = () => {
  const { toasts, removeToast } = useToastStore();

  useEffect(() => {
    toasts.forEach((toast) => {
      if (toast.duration) {
        const timer = setTimeout(() => {
          removeToast(toast.id);
        }, toast.duration);
        return () => clearTimeout(timer);
      }
    });
  }, [toasts, removeToast]);

  if (toasts.length === 0) return null;

  return (
    <div className="fixed top-4 right-4 z-50 space-y-2">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`
            flex items-start gap-3 p-4 rounded-lg shadow-lg border max-w-md
            ${toast.type === 'success' ? 'bg-green-50 border-green-200 text-green-800' : ''}
            ${toast.type === 'error' ? 'bg-red-50 border-red-200 text-red-800' : ''}
            ${toast.type === 'info' ? 'bg-blue-50 border-blue-200 text-blue-800' : ''}
            ${toast.type === 'warning' ? 'bg-yellow-50 border-yellow-200 text-yellow-800' : ''}
          `}
        >
          <div className="flex-shrink-0">
            {toast.type === 'success' && <CheckCircle className="w-5 h-5 text-green-600" />}
            {toast.type === 'error' && <XCircle className="w-5 h-5 text-red-600" />}
            {toast.type === 'info' && <Info className="w-5 h-5 text-blue-600" />}
            {toast.type === 'warning' && <AlertTriangle className="w-5 h-5 text-yellow-600" />}
          </div>
          <div className="flex-1 min-w-0">
            <p className="font-medium">{toast.title}</p>
            {toast.message && (
              <p className="text-sm mt-1 opacity-90">{toast.message}</p>
            )}
          </div>
          <button
            onClick={() => removeToast(toast.id)}
            className="flex-shrink-0 p-1 hover:bg-black/10 rounded"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      ))}
    </div>
  );
};

export default ToastComponent;