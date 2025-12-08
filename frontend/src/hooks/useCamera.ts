'use client';

import { useState, useCallback, useRef } from 'react';

interface CameraConstraints {
  video?: boolean | {
    width?: number;
    height?: number;
    facingMode?: 'user' | 'environment';
  };
  audio?: boolean;
}

interface CameraResult {
  stream: MediaStream | null;
  isActive: boolean;
  error: string | null;
  startCamera: (constraints?: CameraConstraints) => Promise<void>;
  stopCamera: () => void;
  takePhoto: () => string | null;
  videoRef: React.RefObject<HTMLVideoElement>;
}

export const useCamera = (): CameraResult => {
  const [stream, setStream] = useState<MediaStream | null>(null);
  const [isActive, setIsActive] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const videoRef = useRef<HTMLVideoElement>(null);

  const startCamera = useCallback(async (constraints: CameraConstraints = { video: true }) => {
    try {
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        throw new Error('Camera not supported');
      }

      const mediaStream = await navigator.mediaDevices.getUserMedia(constraints);
      setStream(mediaStream);
      setIsActive(true);
      setError(null);

      if (videoRef.current) {
        videoRef.current.srcObject = mediaStream;
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to access camera');
      setIsActive(false);
    }
  }, []);

  const stopCamera = useCallback(() => {
    if (stream) {
      stream.getTracks().forEach(track => track.stop());
      setStream(null);
      setIsActive(false);
      
      if (videoRef.current) {
        videoRef.current.srcObject = null;
      }
    }
  }, [stream]);

  const takePhoto = useCallback((): string | null => {
    if (!videoRef.current || !isActive) {
      return null;
    }

    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');
    
    if (!context) {
      return null;
    }

    canvas.width = videoRef.current.videoWidth;
    canvas.height = videoRef.current.videoHeight;
    
    context.drawImage(videoRef.current, 0, 0);
    
    return canvas.toDataURL('image/jpeg');
  }, [isActive]);

  return {
    stream,
    isActive,
    error,
    startCamera,
    stopCamera,
    takePhoto,
    videoRef
  };
};
