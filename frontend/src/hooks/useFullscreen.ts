'use client';

import { useState, useEffect, useCallback } from 'react';

interface FullscreenResult {
  isFullscreen: boolean;
  enterFullscreen: () => Promise<void>;
  exitFullscreen: () => Promise<void>;
  toggleFullscreen: () => Promise<void>;
  error: string | null;
}

export const useFullscreen = (): FullscreenResult => {
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const enterFullscreen = useCallback(async () => {
    try {
      if (!document.fullscreenElement) {
        await document.documentElement.requestFullscreen();
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to enter fullscreen');
    }
  }, []);

  const exitFullscreen = useCallback(async () => {
    try {
      if (document.fullscreenElement) {
        await document.exitFullscreen();
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to exit fullscreen');
    }
  }, []);

  const toggleFullscreen = useCallback(async () => {
    if (isFullscreen) {
      await exitFullscreen();
    } else {
      await enterFullscreen();
    }
  }, [isFullscreen, enterFullscreen, exitFullscreen]);

  useEffect(() => {
    const handleFullscreenChange = () => {
      setIsFullscreen(!!document.fullscreenElement);
      setError(null);
    };

    document.addEventListener('fullscreenchange', handleFullscreenChange);
    document.addEventListener('fullscreenerror', (e) => {
      setError('Fullscreen error occurred');
    });

    return () => {
      document.removeEventListener('fullscreenchange', handleFullscreenChange);
      document.removeEventListener('fullscreenerror', () => {});
    };
  }, []);

  return {
    isFullscreen,
    enterFullscreen,
    exitFullscreen,
    toggleFullscreen,
    error
  };
};
