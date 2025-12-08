'use client';

import { useState, useCallback, useRef } from 'react';

interface WebWakeLockResult {
  isSupported: boolean;
  error: string | null;
  requestWakeLock: (type?: WakeLockType) => Promise<WakeLockSentinel | null>;
  releaseWakeLock: () => Promise<boolean>;
  isWakeLockActive: boolean;
  getWakeLockType: () => WakeLockType | null;
}

export const useWebWakeLock = (): WebWakeLockResult => {
  const [error, setError] = useState<string | null>(null);
  const [isWakeLockActive, setIsWakeLockActive] = useState(false);
  const wakeLockRef = useRef<WakeLockSentinel | null>(null);
  const isSupported = typeof navigator !== 'undefined' && 'wakeLock' in navigator;

  const requestWakeLock = useCallback(async (
    type: WakeLockType = 'screen'
  ): Promise<WakeLockSentinel | null> => {
    if (!isSupported) {
      setError('Wake Lock API not supported');
      return null;
    }

    try {
      const wakeLock = await navigator.wakeLock.request(type);
      wakeLockRef.current = wakeLock;
      setIsWakeLockActive(true);
      setError(null);

      wakeLock.addEventListener('release', () => {
        setIsWakeLockActive(false);
        wakeLockRef.current = null;
      });

      return wakeLock;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to request wake lock');
      return null;
    }
  }, [isSupported]);

  const releaseWakeLock = useCallback(async (): Promise<boolean> => {
    if (!isSupported) {
      setError('Wake Lock API not supported');
      return false;
    }

    try {
      if (wakeLockRef.current) {
        await wakeLockRef.current.release();
        wakeLockRef.current = null;
        setIsWakeLockActive(false);
        return true;
      }
      return false;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to release wake lock');
      return false;
    }
  }, [isSupported]);

  const getWakeLockType = useCallback((): WakeLockType | null => {
    if (!isSupported) {
      setError('Wake Lock API not supported');
      return null;
    }

    try {
      return wakeLockRef.current?.type || null;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get wake lock type');
      return null;
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    requestWakeLock,
    releaseWakeLock,
    isWakeLockActive,
    getWakeLockType
  };
};
