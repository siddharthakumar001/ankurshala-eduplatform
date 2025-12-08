'use client';

import { useState, useCallback, useEffect } from 'react';

interface WebPushResult {
  isSupported: boolean;
  permission: NotificationPermission;
  error: string | null;
  requestPermission: () => Promise<NotificationPermission>;
  subscribe: (vapidPublicKey: string) => Promise<PushSubscription | null>;
  unsubscribe: () => Promise<boolean>;
  getSubscription: () => Promise<PushSubscription | null>;
}

export const useWebPush = (): WebPushResult => {
  const [permission, setPermission] = useState<NotificationPermission>('default');
  const [error, setError] = useState<string | null>(null);
  const isSupported = 'serviceWorker' in navigator && 'PushManager' in window;

  const requestPermission = useCallback(async (): Promise<NotificationPermission> => {
    if (!isSupported) {
      setError('Web Push not supported');
      return 'denied';
    }

    try {
      const result = await Notification.requestPermission();
      setPermission(result);
      return result;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to request permission');
      return 'denied';
    }
  }, [isSupported]);

  const subscribe = useCallback(async (vapidPublicKey: string): Promise<PushSubscription | null> => {
    if (!isSupported) {
      setError('Web Push not supported');
      return null;
    }

    try {
      const registration = await navigator.serviceWorker.ready;
      const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: vapidPublicKey
      });

      return subscription;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to subscribe');
      return null;
    }
  }, [isSupported]);

  const unsubscribe = useCallback(async (): Promise<boolean> => {
    if (!isSupported) {
      setError('Web Push not supported');
      return false;
    }

    try {
      const registration = await navigator.serviceWorker.ready;
      const subscription = await registration.pushManager.getSubscription();
      
      if (subscription) {
        await subscription.unsubscribe();
        return true;
      }
      
      return false;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to unsubscribe');
      return false;
    }
  }, [isSupported]);

  const getSubscription = useCallback(async (): Promise<PushSubscription | null> => {
    if (!isSupported) {
      setError('Web Push not supported');
      return null;
    }

    try {
      const registration = await navigator.serviceWorker.ready;
      const subscription = await registration.pushManager.getSubscription();
      return subscription;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get subscription');
      return null;
    }
  }, [isSupported]);

  useEffect(() => {
    if (isSupported) {
      setPermission(Notification.permission);
    }
  }, [isSupported]);

  return {
    isSupported,
    permission,
    error,
    requestPermission,
    subscribe,
    unsubscribe,
    getSubscription
  };
};
