'use client';

import { useState, useCallback, useEffect } from 'react';

interface WebPerformanceResult {
  isSupported: boolean;
  error: string | null;
  getNavigationTiming: () => PerformanceNavigationTiming | null;
  getResourceTiming: () => PerformanceResourceTiming[];
  getPaintTiming: () => PerformanceEntry[];
  getLayoutShift: () => PerformanceEntry[];
  getLargestContentfulPaint: () => PerformanceEntry | null;
  getFirstInputDelay: () => PerformanceEntry | null;
  getCumulativeLayoutShift: () => number;
  getFirstContentfulPaint: () => PerformanceEntry | null;
  measure: (name: string, startMark?: string, endMark?: string) => PerformanceMeasure | null;
  mark: (name: string) => void;
  getEntriesByType: (type: string) => PerformanceEntry[];
  getEntriesByName: (name: string) => PerformanceEntry[];
  clearMarks: (name?: string) => void;
  clearMeasures: (name?: string) => void;
}

export const useWebPerformance = (): WebPerformanceResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = typeof performance !== 'undefined';

  const getNavigationTiming = useCallback((): PerformanceNavigationTiming | null => {
    if (!isSupported) {
      setError('Performance API not supported');
      return null;
    }

    try {
      const entries = performance.getEntriesByType('navigation') as PerformanceNavigationTiming[];
      return entries[0] || null;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get navigation timing');
      return null;
    }
  }, [isSupported]);

  const getResourceTiming = useCallback((): PerformanceResourceTiming[] => {
    if (!isSupported) {
      setError('Performance API not supported');
      return [];
    }

    try {
      return performance.getEntriesByType('resource') as PerformanceResourceTiming[];
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get resource timing');
      return [];
    }
  }, [isSupported]);

  const getPaintTiming = useCallback((): PerformanceEntry[] => {
    if (!isSupported) {
      setError('Performance API not supported');
      return [];
    }

    try {
      return performance.getEntriesByType('paint');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get paint timing');
      return [];
    }
  }, [isSupported]);

  const getLayoutShift = useCallback((): PerformanceEntry[] => {
    if (!isSupported) {
      setError('Performance API not supported');
      return [];
    }

    try {
      return performance.getEntriesByType('layout-shift');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get layout shift');
      return [];
    }
  }, [isSupported]);

  const getLargestContentfulPaint = useCallback((): PerformanceEntry | null => {
    if (!isSupported) {
      setError('Performance API not supported');
      return null;
    }

    try {
      const entries = performance.getEntriesByType('largest-contentful-paint');
      return entries[entries.length - 1] || null;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get largest contentful paint');
      return null;
    }
  }, [isSupported]);

  const getFirstInputDelay = useCallback((): PerformanceEntry | null => {
    if (!isSupported) {
      setError('Performance API not supported');
      return null;
    }

    try {
      const entries = performance.getEntriesByType('first-input') as PerformanceEventTiming[];
      return entries[0] || null;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get first input delay');
      return null;
    }
  }, [isSupported]);

  const getCumulativeLayoutShift = useCallback((): number => {
    if (!isSupported) {
      setError('Performance API not supported');
      return 0;
    }

    try {
      const entries = performance.getEntriesByType('layout-shift');
      return entries.reduce((sum, entry) => sum + ((entry as any).value || 0), 0);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get cumulative layout shift');
      return 0;
    }
  }, [isSupported]);

  const getFirstContentfulPaint = useCallback((): PerformanceEntry | null => {
    if (!isSupported) {
      setError('Performance API not supported');
      return null;
    }

    try {
      const entries = performance.getEntriesByType('paint');
      return entries.find(entry => entry.name === 'first-contentful-paint') || null;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get first contentful paint');
      return null;
    }
  }, [isSupported]);

  const measure = useCallback((
    name: string, 
    startMark?: string, 
    endMark?: string
  ): PerformanceMeasure | null => {
    if (!isSupported) {
      setError('Performance API not supported');
      return null;
    }

    try {
      return performance.measure(name, startMark, endMark);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to measure performance');
      return null;
    }
  }, [isSupported]);

  const mark = useCallback((name: string): void => {
    if (!isSupported) {
      setError('Performance API not supported');
      return;
    }

    try {
      performance.mark(name);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to mark performance');
    }
  }, [isSupported]);

  const getEntriesByType = useCallback((type: string): PerformanceEntry[] => {
    if (!isSupported) {
      setError('Performance API not supported');
      return [];
    }

    try {
      return performance.getEntriesByType(type);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get entries by type');
      return [];
    }
  }, [isSupported]);

  const getEntriesByName = useCallback((name: string): PerformanceEntry[] => {
    if (!isSupported) {
      setError('Performance API not supported');
      return [];
    }

    try {
      return performance.getEntriesByName(name);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get entries by name');
      return [];
    }
  }, [isSupported]);

  const clearMarks = useCallback((name?: string): void => {
    if (!isSupported) {
      setError('Performance API not supported');
      return;
    }

    try {
      performance.clearMarks(name);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to clear marks');
    }
  }, [isSupported]);

  const clearMeasures = useCallback((name?: string): void => {
    if (!isSupported) {
      setError('Performance API not supported');
      return;
    }

    try {
      performance.clearMeasures(name);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to clear measures');
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    getNavigationTiming,
    getResourceTiming,
    getPaintTiming,
    getLayoutShift,
    getLargestContentfulPaint,
    getFirstInputDelay,
    getCumulativeLayoutShift,
    getFirstContentfulPaint,
    measure,
    mark,
    getEntriesByType,
    getEntriesByName,
    clearMarks,
    clearMeasures
  };
};
