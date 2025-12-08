'use client';

import { useState, useCallback, useRef } from 'react';

interface MicrophoneConstraints {
  audio?: boolean | {
    echoCancellation?: boolean;
    noiseSuppression?: boolean;
    autoGainControl?: boolean;
  };
}

interface MicrophoneResult {
  stream: MediaStream | null;
  isActive: boolean;
  error: string | null;
  startMicrophone: (constraints?: MicrophoneConstraints) => Promise<void>;
  stopMicrophone: () => void;
  audioRef: React.RefObject<HTMLAudioElement>;
}

export const useMicrophone = (): MicrophoneResult => {
  const [stream, setStream] = useState<MediaStream | null>(null);
  const [isActive, setIsActive] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const audioRef = useRef<HTMLAudioElement>(null);

  const startMicrophone = useCallback(async (constraints: MicrophoneConstraints = { audio: true }) => {
    try {
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        throw new Error('Microphone not supported');
      }

      const mediaStream = await navigator.mediaDevices.getUserMedia(constraints);
      setStream(mediaStream);
      setIsActive(true);
      setError(null);

      if (audioRef.current) {
        audioRef.current.srcObject = mediaStream;
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to access microphone');
      setIsActive(false);
    }
  }, []);

  const stopMicrophone = useCallback(() => {
    if (stream) {
      stream.getTracks().forEach(track => track.stop());
      setStream(null);
      setIsActive(false);
      
      if (audioRef.current) {
        audioRef.current.srcObject = null;
      }
    }
  }, [stream]);

  return {
    stream,
    isActive,
    error,
    startMicrophone,
    stopMicrophone,
    audioRef
  };
};
