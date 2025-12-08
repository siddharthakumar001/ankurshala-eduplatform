'use client';

import { useState, useCallback, useEffect } from 'react';

interface StorageQuota {
  quota: number;
  usage: number;
  available: number;
}

interface WebStorageQuotaResult {
  isSupported: boolean;
  error: string | null;
  getStorageQuota: () => Promise<StorageQuota | null>;
  getLocalStorageQuota: () => Promise<StorageQuota | null>;
  getSessionStorageQuota: () => Promise<StorageQuota | null>;
  getIndexedDBQuota: () => Promise<StorageQuota | null>;
  getCacheStorageQuota: () => Promise<StorageQuota | null>;
  estimateStorageUsage: () => Promise<number>;
}

export const useWebStorageQuota = (): WebStorageQuotaResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = typeof navigator !== 'undefined' && 'storage' in navigator && 'estimate' in navigator.storage;

  const getStorageQuota = useCallback(async (): Promise<StorageQuota | null> => {
    if (!isSupported) {
      setError('Storage Quota API not supported');
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
      setError(err instanceof Error ? err.message : 'Failed to get storage quota');
      return null;
    }
  }, [isSupported]);

  const getLocalStorageQuota = useCallback(async (): Promise<StorageQuota | null> => {
    if (!isSupported) {
      setError('Storage Quota API not supported');
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
      setError(err instanceof Error ? err.message : 'Failed to get localStorage quota');
      return null;
    }
  }, [isSupported]);

  const getSessionStorageQuota = useCallback(async (): Promise<StorageQuota | null> => {
    if (!isSupported) {
      setError('Storage Quota API not supported');
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
      setError(err instanceof Error ? err.message : 'Failed to get sessionStorage quota');
      return null;
    }
  }, [isSupported]);

  const getIndexedDBQuota = useCallback(async (): Promise<StorageQuota | null> => {
    if (!isSupported) {
      setError('Storage Quota API not supported');
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
      setError(err instanceof Error ? err.message : 'Failed to get IndexedDB quota');
      return null;
    }
  }, [isSupported]);

  const getCacheStorageQuota = useCallback(async (): Promise<StorageQuota | null> => {
    if (!isSupported) {
      setError('Storage Quota API not supported');
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
      setError(err instanceof Error ? err.message : 'Failed to get Cache Storage quota');
      return null;
    }
  }, [isSupported]);

  const estimateStorageUsage = useCallback(async (): Promise<number> => {
    if (!isSupported) {
      setError('Storage Quota API not supported');
      return 0;
    }

    try {
      const estimate = await navigator.storage.estimate();
      return estimate.usage || 0;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to estimate storage usage');
      return 0;
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    getStorageQuota,
    getLocalStorageQuota,
    getSessionStorageQuota,
    getIndexedDBQuota,
    getCacheStorageQuota,
    estimateStorageUsage
  };
};