'use client';

import { useState, useCallback, useRef } from 'react';

interface WebAnimationsResult {
  isSupported: boolean;
  error: string | null;
  animate: (element: Element, keyframes: Keyframe[], options?: KeyframeAnimationOptions) => Animation | null;
  getAnimations: (element?: Element) => Animation[];
  pauseAll: (element?: Element) => void;
  resumeAll: (element?: Element) => void;
  cancelAll: (element?: Element) => void;
  finishAll: (element?: Element) => void;
}

export const useWebAnimations = (): WebAnimationsResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = typeof Element !== 'undefined' && 'animate' in Element.prototype;

  const animate = useCallback((
    element: Element, 
    keyframes: Keyframe[], 
    options?: KeyframeAnimationOptions
  ): Animation | null => {
    if (!isSupported) {
      setError('Web Animations API not supported');
      return null;
    }

    try {
      return element.animate(keyframes, options);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to animate element');
      return null;
    }
  }, [isSupported]);

  const getAnimations = useCallback((element?: Element): Animation[] => {
    if (!isSupported) {
      setError('Web Animations API not supported');
      return [];
    }

    try {
      if (element) {
        return element.getAnimations();
      } else {
        return document.getAnimations();
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get animations');
      return [];
    }
  }, [isSupported]);

  const pauseAll = useCallback((element?: Element): void => {
    if (!isSupported) {
      setError('Web Animations API not supported');
      return;
    }

    try {
      const animations = element ? element.getAnimations() : document.getAnimations();
      animations.forEach(animation => animation.pause());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to pause animations');
    }
  }, [isSupported]);

  const resumeAll = useCallback((element?: Element): void => {
    if (!isSupported) {
      setError('Web Animations API not supported');
      return;
    }

    try {
      const animations = element ? element.getAnimations() : document.getAnimations();
      animations.forEach(animation => animation.play());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to resume animations');
    }
  }, [isSupported]);

  const cancelAll = useCallback((element?: Element): void => {
    if (!isSupported) {
      setError('Web Animations API not supported');
      return;
    }

    try {
      const animations = element ? element.getAnimations() : document.getAnimations();
      animations.forEach(animation => animation.cancel());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to cancel animations');
    }
  }, [isSupported]);

  const finishAll = useCallback((element?: Element): void => {
    if (!isSupported) {
      setError('Web Animations API not supported');
      return;
    }

    try {
      const animations = element ? element.getAnimations() : document.getAnimations();
      animations.forEach(animation => animation.finish());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to finish animations');
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    animate,
    getAnimations,
    pauseAll,
    resumeAll,
    cancelAll,
    finishAll
  };
};
