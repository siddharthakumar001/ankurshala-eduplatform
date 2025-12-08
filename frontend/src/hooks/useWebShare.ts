'use client';

import { useState, useCallback } from 'react';

interface WebShareData {
  title?: string;
  text?: string;
  url?: string;
  files?: File[];
}

interface WebShareResult {
  isSupported: boolean;
  error: string | null;
  share: (data: WebShareData) => Promise<boolean>;
  canShare: (data: WebShareData) => boolean;
}

export const useWebShare = (): WebShareResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = 'share' in navigator;

  const share = useCallback(async (data: WebShareData): Promise<boolean> => {
    if (!isSupported) {
      setError('Web Share API not supported');
      return false;
    }

    try {
      await navigator.share(data);
      return true;
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        return false; // User cancelled
      }
      setError(err instanceof Error ? err.message : 'Share failed');
      return false;
    }
  }, [isSupported]);

  const canShare = useCallback((data: WebShareData): boolean => {
    if (!isSupported) {
      return false;
    }

    try {
      return navigator.canShare(data);
    } catch {
      return false;
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    share,
    canShare
  };
};
