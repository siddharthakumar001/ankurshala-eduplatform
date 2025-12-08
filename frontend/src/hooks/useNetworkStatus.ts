'use client';

import { useState, useEffect } from 'react';

interface NetworkStatus {
  isOnline: boolean;
  isSlowConnection: boolean;
  connectionType: string;
}

export const useNetworkStatus = (): NetworkStatus => {
  const [networkStatus, setNetworkStatus] = useState<NetworkStatus>({
    isOnline: true,
    isSlowConnection: false,
    connectionType: 'unknown'
  });

  useEffect(() => {
    const updateNetworkStatus = () => {
      const isOnline = navigator.onLine;
      const connection = (navigator as any).connection || (navigator as any).mozConnection || (navigator as any).webkitConnection;
      
      setNetworkStatus({
        isOnline,
        isSlowConnection: connection ? connection.effectiveType === 'slow-2g' || connection.effectiveType === '2g' : false,
        connectionType: connection ? connection.effectiveType : 'unknown'
      });
    };

    updateNetworkStatus();

    window.addEventListener('online', updateNetworkStatus);
    window.addEventListener('offline', updateNetworkStatus);

    const connection = (navigator as any).connection || (navigator as any).mozConnection || (navigator as any).webkitConnection;
    if (connection) {
      connection.addEventListener('change', updateNetworkStatus);
    }

    return () => {
      window.removeEventListener('online', updateNetworkStatus);
      window.removeEventListener('offline', updateNetworkStatus);
      if (connection) {
        connection.removeEventListener('change', updateNetworkStatus);
      }
    };
  }, []);

  return networkStatus;
};
