'use client';

import { useState, useCallback, useEffect } from 'react';

interface StorageEstimate {
  quota: number;
  usage: number;
  available: number;
}

interface WebStorageManagerResult {
  isSupported: boolean;
  error: string | null;
  getStorageEstimate: () => Promise<StorageEstimate | null>;
  getStorageUsage: () => Promise<number>;
  getStorageQuota: () => Promise<number>;
  getStorageAvailable: () => Promise<number>;
  getStorageUsagePercentage: () => Promise<number>;
  isStorageFull: () => Promise<boolean>;
  isStorageLow: () => Promise<boolean>;
  onStorageChange: (callback: (estimate: StorageEstimate) => void) => void;
  offStorageChange: (callback: (estimate: StorageEstimate) => void) => void;
}

export const useWebStorageManager = (): WebStorageManagerResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = typeof navigator !== 'undefined' && 'storage' in navigator && 'estimate' in navigator.storage;

  const getStorageEstimate = useCallback(async (): Promise<StorageEstimate | null> => {
    if (!isSupported) {
      setError('Storage Manager API not supported');
      return null;
    }

    try {
      const estimate = await navigator.storage.estimate();
      return {
        quota: estimate.quota || 0,
        usage: estimate.usage || 0,
        available: (estimate.quota || 0) - (estimate.usage || 0)
      };
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get storage estimate');
      return null;
    }
  }, [isSupported]);

  const getStorageUsage = useCallback(async (): Promise<number> => {
    if (!isSupported) {
      setError('Storage Manager API not supported');
      return 0;
    }

    try {
      const estimate = await navigator.storage.estimate();
      return estimate.usage || 0;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get storage usage');
      return 0;
    }
  }, [isSupported]);

  const getStorageQuota = useCallback(async (): Promise<number> => {
    if (!isSupported) {
      setError('Storage Manager API not supported');
      return 0;
    }

    try {
      const estimate = await navigator.storage.estimate();
      return estimate.quota || 0;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get storage quota');
      return 0;
    }
  }, [isSupported]);

  const getStorageAvailable = useCallback(async (): Promise<number> => {
    if (!isSupported) {
      setError('Storage Manager API not supported');
      return 0;
    }

    try {
      const estimate = await navigator.storage.estimate();
      return (estimate.quota || 0) - (estimate.usage || 0);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get storage available');
      return 0;
    }
  }, [isSupported]);

  const getStorageUsagePercentage = useCallback(async (): Promise<number> => {
    if (!isSupported) {
      setError('Storage Manager API not supported');
      return 0;
    }

    try {
      const estimate = await navigator.storage.estimate();
      if (!estimate.quota) return 0;
      return ((estimate.usage || 0) / estimate.quota) * 100;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get storage usage percentage');
      return 0;
    }
  }, [isSupported]);

  const isStorageFull = useCallback(async (): Promise<boolean> => {
    if (!isSupported) {
      setError('Storage Manager API not supported');
      return false;
    }

    try {
      const estimate = await navigator.storage.estimate();
      if (!estimate.quota) return false;
      return (estimate.usage || 0) >= estimate.quota;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to check if storage is full');
      return false;
    }
  }, [isSupported]);

  const isStorageLow = useCallback(async (): Promise<boolean> => {
    if (!isSupported) {
      setError('Storage Manager API not supported');
      return false;
    }

    try {
      const estimate = await navigator.storage.estimate();
      if (!estimate.quota) return false;
      const usagePercentage = ((estimate.usage || 0) / estimate.quota) * 100;
      return usagePercentage >= 80; // Consider low if usage is 80% or more
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to check if storage is low');
      return false;
    }
  }, [isSupported]);

  const onStorageChange = useCallback((
    callback: (estimate: StorageEstimate) => void
  ): void => {
    if (!isSupported) {
      setError('Storage Manager API not supported');
      return;
    }

    // Note: StorageManager API doesn't support event listeners
    // This would need to be implemented using polling or other mechanisms
    setError('StorageManager does not support change events. Use periodic polling with getStorageEstimate() instead.');
  }, [isSupported]);

  const offStorageChange = useCallback((
    callback: (estimate: StorageEstimate) => void
  ): void => {
    if (!isSupported) {
      setError('Storage Manager API not supported');
      return;
    }

    // Note: StorageManager API doesn't support event listeners
    setError('StorageManager does not support change events.');
  }, [isSupported]);

  return {
    isSupported,
    error,
    getStorageEstimate,
    getStorageUsage,
    getStorageQuota,
    getStorageAvailable,
    getStorageUsagePercentage,
    isStorageFull,
    isStorageLow,
    onStorageChange,
    offStorageChange
  };
};
