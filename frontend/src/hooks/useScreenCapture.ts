'use client';

import { useState, useCallback } from 'react';

interface ScreenCaptureResult {
  isSupported: boolean;
  error: string | null;
  captureScreen: () => Promise<string | null>;
  captureWindow: () => Promise<string | null>;
  captureTab: () => Promise<string | null>;
}

export const useScreenCapture = (): ScreenCaptureResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = 'getDisplayMedia' in navigator.mediaDevices;

  const captureScreen = useCallback(async (): Promise<string | null> => {
    if (!isSupported) {
      setError('Screen capture not supported');
      return null;
    }

    try {
      const stream = await navigator.mediaDevices.getDisplayMedia({
        video: true
      });

      const video = document.createElement('video');
      video.srcObject = stream;
      video.play();

      return new Promise((resolve) => {
        video.onloadedmetadata = () => {
          const canvas = document.createElement('canvas');
          const context = canvas.getContext('2d');
          
          if (!context) {
            resolve(null);
            return;
          }

          canvas.width = video.videoWidth;
          canvas.height = video.videoHeight;
          
          context.drawImage(video, 0, 0);
          
          const dataURL = canvas.toDataURL('image/png');
          
          stream.getTracks().forEach(track => track.stop());
          resolve(dataURL);
        };
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Screen capture failed');
      return null;
    }
  }, [isSupported]);

  const captureWindow = useCallback(async (): Promise<string | null> => {
    if (!isSupported) {
      setError('Screen capture not supported');
      return null;
    }

    try {
      const stream = await navigator.mediaDevices.getDisplayMedia({
        video: true
      });

      const video = document.createElement('video');
      video.srcObject = stream;
      video.play();

      return new Promise((resolve) => {
        video.onloadedmetadata = () => {
          const canvas = document.createElement('canvas');
          const context = canvas.getContext('2d');
          
          if (!context) {
            resolve(null);
            return;
          }

          canvas.width = video.videoWidth;
          canvas.height = video.videoHeight;
          
          context.drawImage(video, 0, 0);
          
          const dataURL = canvas.toDataURL('image/png');
          
          stream.getTracks().forEach(track => track.stop());
          resolve(dataURL);
        };
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Window capture failed');
      return null;
    }
  }, [isSupported]);

  const captureTab = useCallback(async (): Promise<string | null> => {
    if (!isSupported) {
      setError('Screen capture not supported');
      return null;
    }

    try {
      const stream = await navigator.mediaDevices.getDisplayMedia({
        video: true
      });

      const video = document.createElement('video');
      video.srcObject = stream;
      video.play();

      return new Promise((resolve) => {
        video.onloadedmetadata = () => {
          const canvas = document.createElement('canvas');
          const context = canvas.getContext('2d');
          
          if (!context) {
            resolve(null);
            return;
          }

          canvas.width = video.videoWidth;
          canvas.height = video.videoHeight;
          
          context.drawImage(video, 0, 0);
          
          const dataURL = canvas.toDataURL('image/png');
          
          stream.getTracks().forEach(track => track.stop());
          resolve(dataURL);
        };
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Tab capture failed');
      return null;
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    captureScreen,
    captureWindow,
    captureTab
  };
};
