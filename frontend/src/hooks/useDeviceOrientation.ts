'use client';

import { useState, useEffect } from 'react';

interface DeviceOrientation {
  alpha: number | null;
  beta: number | null;
  gamma: number | null;
}

interface DeviceOrientationResult {
  orientation: DeviceOrientation;
  isSupported: boolean;
  error: string | null;
  requestPermission: () => Promise<boolean>;
}

export const useDeviceOrientation = (): DeviceOrientationResult => {
  const [orientation, setOrientation] = useState<DeviceOrientation>({
    alpha: null,
    beta: null,
    gamma: null
  });
  const [isSupported, setIsSupported] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const requestPermission = async (): Promise<boolean> => {
    if (!('DeviceOrientationEvent' in window)) {
      return false;
    }

    if ('requestPermission' in DeviceOrientationEvent) {
      try {
        const permission = await (DeviceOrientationEvent as any).requestPermission();
        return permission === 'granted';
      } catch (err) {
        setError('Failed to request permission');
        return false;
      }
    }

    return true;
  };

  useEffect(() => {
    if (!('DeviceOrientationEvent' in window)) {
      setIsSupported(false);
      setError('Device orientation not supported');
      return;
    }

    setIsSupported(true);

    const handleOrientationChange = (event: DeviceOrientationEvent) => {
      setOrientation({
        alpha: event.alpha,
        beta: event.beta,
        gamma: event.gamma
      });
    };

    window.addEventListener('deviceorientation', handleOrientationChange);

    return () => {
      window.removeEventListener('deviceorientation', handleOrientationChange);
    };
  }, []);

  return {
    orientation,
    isSupported,
    error,
    requestPermission
  };
};
