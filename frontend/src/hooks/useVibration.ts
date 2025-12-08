'use client';

import { useCallback } from 'react';

interface VibrationResult {
  vibrate: (pattern: number | number[]) => void;
  vibrateOnce: (duration: number) => void;
  vibratePattern: (pattern: number[]) => void;
  stopVibration: () => void;
  isSupported: boolean;
}

export const useVibration = (): VibrationResult => {
  const isSupported = 'vibrate' in navigator;

  const vibrate = useCallback((pattern: number | number[]) => {
    if (isSupported) {
      navigator.vibrate(pattern);
    }
  }, [isSupported]);

  const vibrateOnce = useCallback((duration: number) => {
    if (isSupported) {
      navigator.vibrate(duration);
    }
  }, [isSupported]);

  const vibratePattern = useCallback((pattern: number[]) => {
    if (isSupported) {
      navigator.vibrate(pattern);
    }
  }, [isSupported]);

  const stopVibration = useCallback(() => {
    if (isSupported) {
      navigator.vibrate(0);
    }
  }, [isSupported]);

  return {
    vibrate,
    vibrateOnce,
    vibratePattern,
    stopVibration,
    isSupported
  };
};
