'use client';

import { useState, useEffect } from 'react';

interface GeolocationPosition {
  latitude: number;
  longitude: number;
  accuracy: number;
  timestamp: number;
}

interface GeolocationError {
  code: number;
  message: string;
}

interface GeolocationResult {
  position: GeolocationPosition | null;
  error: GeolocationError | null;
  isLoading: boolean;
  getCurrentPosition: () => void;
  watchPosition: () => void;
  clearWatch: () => void;
}

export const useGeolocation = (): GeolocationResult => {
  const [position, setPosition] = useState<GeolocationPosition | null>(null);
  const [error, setError] = useState<GeolocationError | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [watchId, setWatchId] = useState<number | null>(null);

  const getCurrentPosition = () => {
    if (!navigator.geolocation) {
      setError({
        code: 0,
        message: 'Geolocation is not supported by this browser'
      });
      return;
    }

    setIsLoading(true);
    setError(null);

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setPosition({
          latitude: pos.coords.latitude,
          longitude: pos.coords.longitude,
          accuracy: pos.coords.accuracy,
          timestamp: pos.timestamp
        });
        setIsLoading(false);
      },
      (err) => {
        setError({
          code: err.code,
          message: err.message
        });
        setIsLoading(false);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 300000
      }
    );
  };

  const watchPosition = () => {
    if (!navigator.geolocation) {
      setError({
        code: 0,
        message: 'Geolocation is not supported by this browser'
      });
      return;
    }

    setIsLoading(true);
    setError(null);

    const id = navigator.geolocation.watchPosition(
      (pos) => {
        setPosition({
          latitude: pos.coords.latitude,
          longitude: pos.coords.longitude,
          accuracy: pos.coords.accuracy,
          timestamp: pos.timestamp
        });
        setIsLoading(false);
      },
      (err) => {
        setError({
          code: err.code,
          message: err.message
        });
        setIsLoading(false);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 300000
      }
    );

    setWatchId(id);
  };

  const clearWatch = () => {
    if (watchId !== null) {
      navigator.geolocation.clearWatch(watchId);
      setWatchId(null);
    }
  };

  useEffect(() => {
    return () => {
      clearWatch();
    };
  }, [watchId]);

  return {
    position,
    error,
    isLoading,
    getCurrentPosition,
    watchPosition,
    clearWatch
  };
};
