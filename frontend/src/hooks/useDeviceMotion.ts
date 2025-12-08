'use client';

import { useState, useEffect } from 'react';

interface DeviceMotion {
  acceleration: {
    x: number | null;
    y: number | null;
    z: number | null;
  };
  accelerationIncludingGravity: {
    x: number | null;
    y: number | null;
    z: number | null;
  };
  rotationRate: {
    alpha: number | null;
    beta: number | null;
    gamma: number | null;
  };
  interval: number | null;
}

interface DeviceMotionResult {
  motion: DeviceMotion;
  isSupported: boolean;
  error: string | null;
  requestPermission: () => Promise<boolean>;
}

export const useDeviceMotion = (): DeviceMotionResult => {
  const [motion, setMotion] = useState<DeviceMotion>({
    acceleration: { x: null, y: null, z: null },
    accelerationIncludingGravity: { x: null, y: null, z: null },
    rotationRate: { alpha: null, beta: null, gamma: null },
    interval: null
  });
  const [isSupported, setIsSupported] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const requestPermission = async (): Promise<boolean> => {
    if (!('DeviceMotionEvent' in window)) {
      return false;
    }

    if ('requestPermission' in DeviceMotionEvent) {
      try {
        const permission = await (DeviceMotionEvent as any).requestPermission();
        return permission === 'granted';
      } catch (err) {
        setError('Failed to request permission');
        return false;
      }
    }

    return true;
  };

  useEffect(() => {
    if (!('DeviceMotionEvent' in window)) {
      setIsSupported(false);
      setError('Device motion not supported');
      return;
    }

    setIsSupported(true);

    const handleMotionChange = (event: DeviceMotionEvent) => {
      setMotion({
        acceleration: {
          x: event.acceleration?.x || null,
          y: event.acceleration?.y || null,
          z: event.acceleration?.z || null
        },
        accelerationIncludingGravity: {
          x: event.accelerationIncludingGravity?.x || null,
          y: event.accelerationIncludingGravity?.y || null,
          z: event.accelerationIncludingGravity?.z || null
        },
        rotationRate: {
          alpha: event.rotationRate?.alpha || null,
          beta: event.rotationRate?.beta || null,
          gamma: event.rotationRate?.gamma || null
        },
        interval: event.interval
      });
    };

    window.addEventListener('devicemotion', handleMotionChange);

    return () => {
      window.removeEventListener('devicemotion', handleMotionChange);
    };
  }, []);

  return {
    motion,
    isSupported,
    error,
    requestPermission
  };
};
