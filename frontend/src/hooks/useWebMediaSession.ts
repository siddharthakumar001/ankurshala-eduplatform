'use client';

import { useState, useCallback, useEffect } from 'react';

interface MediaSessionMetadata {
  title?: string;
  artist?: string;
  album?: string;
  artwork?: MediaImage[];
}

interface MediaSessionAction {
  action: string;
  handler: () => void;
}

interface WebMediaSessionResult {
  isSupported: boolean;
  error: string | null;
  setMetadata: (metadata: MediaSessionMetadata) => void;
  setActionHandler: (action: string, handler: () => void) => void;
  setPositionState: (state: any) => void;
  setPlaybackState: (state: string) => void;
  clearActionHandlers: () => void;
  getMetadata: () => MediaSessionMetadata | null;
  getPlaybackState: () => string | null;
  getPositionState: () => any | null;
}

export const useWebMediaSession = (): WebMediaSessionResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = typeof navigator !== 'undefined' && 'mediaSession' in navigator;

  const setMetadata = useCallback((metadata: MediaSessionMetadata): void => {
    if (!isSupported) {
      setError('Media Session API not supported');
      return;
    }

    try {
      navigator.mediaSession.metadata = new MediaMetadata(metadata);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to set metadata');
    }
  }, [isSupported]);

  const setActionHandler = useCallback((
    action: string, 
    handler: () => void
  ): void => {
    if (!isSupported) {
      setError('Media Session API not supported');
      return;
    }

    try {
      navigator.mediaSession.setActionHandler(action as any, handler);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to set action handler');
    }
  }, [isSupported]);

  const setPositionState = useCallback((state: any): void => {
    if (!isSupported) {
      setError('Media Session API not supported');
      return;
    }

    try {
      navigator.mediaSession.setPositionState(state);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to set position state');
    }
  }, [isSupported]);

  const setPlaybackState = useCallback((state: string): void => {
    if (!isSupported) {
      setError('Media Session API not supported');
      return;
    }

    try {
      navigator.mediaSession.playbackState = state as any;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to set playback state');
    }
  }, [isSupported]);

  const clearActionHandlers = useCallback((): void => {
    if (!isSupported) {
      setError('Media Session API not supported');
      return;
    }

    try {
      navigator.mediaSession.setActionHandler('play', null);
      navigator.mediaSession.setActionHandler('pause', null);
      navigator.mediaSession.setActionHandler('stop', null);
      navigator.mediaSession.setActionHandler('seekbackward', null);
      navigator.mediaSession.setActionHandler('seekforward', null);
      navigator.mediaSession.setActionHandler('seekto', null);
      navigator.mediaSession.setActionHandler('previoustrack', null);
      navigator.mediaSession.setActionHandler('nexttrack', null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to clear action handlers');
    }
  }, [isSupported]);

  const getMetadata = useCallback((): MediaSessionMetadata | null => {
    if (!isSupported) {
      setError('Media Session API not supported');
      return null;
    }

    try {
      const metadata = navigator.mediaSession.metadata;
      if (!metadata) return null;

      return {
        title: metadata.title,
        artist: metadata.artist,
        album: metadata.album,
        artwork: metadata.artwork ? [...metadata.artwork] : undefined
      };
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get metadata');
      return null;
    }
  }, [isSupported]);

  const getPlaybackState = useCallback((): string | null => {
    if (!isSupported) {
      setError('Media Session API not supported');
      return null;
    }

    try {
      return navigator.mediaSession.playbackState;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get playback state');
      return null;
    }
  }, [isSupported]);

  const getPositionState = useCallback((): any | null => {
    if (!isSupported) {
      setError('Media Session API not supported');
      return null;
    }

    try {
      // Position state is not directly accessible, so we return null
      return null;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get position state');
      return null;
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    setMetadata,
    setActionHandler,
    setPositionState,
    setPlaybackState,
    clearActionHandlers,
    getMetadata,
    getPlaybackState,
    getPositionState
  };
};
