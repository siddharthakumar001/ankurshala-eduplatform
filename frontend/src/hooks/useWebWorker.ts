'use client';

import { useState, useCallback, useRef, useEffect } from 'react';

interface WebWorkerResult {
  isSupported: boolean;
  isRunning: boolean;
  error: string | null;
  createWorker: (script: string | Function) => Worker | null;
  terminateWorker: () => void;
  postMessage: (message: any) => void;
  onMessage: (callback: (event: MessageEvent) => void) => void;
  onError: (callback: (event: ErrorEvent) => void) => void;
}

export const useWebWorker = (): WebWorkerResult => {
  const [isRunning, setIsRunning] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const workerRef = useRef<Worker | null>(null);
  const isSupported = typeof Worker !== 'undefined';

  const createWorker = useCallback((script: string | Function): Worker | null => {
    if (!isSupported) {
      setError('Web Workers not supported');
      return null;
    }

    try {
      let worker: Worker;
      
      if (typeof script === 'function') {
        const blob = new Blob([`(${script.toString()})()`], { type: 'application/javascript' });
        const url = URL.createObjectURL(blob);
        worker = new Worker(url);
        URL.revokeObjectURL(url);
      } else {
        worker = new Worker(script);
      }

      workerRef.current = worker;
      setIsRunning(true);
      setError(null);

      return worker;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create worker');
      return null;
    }
  }, [isSupported]);

  const terminateWorker = useCallback((): void => {
    if (workerRef.current) {
      workerRef.current.terminate();
      workerRef.current = null;
      setIsRunning(false);
    }
  }, []);

  const postMessage = useCallback((message: any): void => {
    if (workerRef.current && isRunning) {
      workerRef.current.postMessage(message);
    }
  }, [isRunning]);

  const onMessage = useCallback((callback: (event: MessageEvent) => void): void => {
    if (workerRef.current) {
      workerRef.current.onmessage = callback;
    }
  }, []);

  const onError = useCallback((callback: (event: ErrorEvent) => void): void => {
    if (workerRef.current) {
      workerRef.current.onerror = callback;
    }
  }, []);

  useEffect(() => {
    return () => {
      terminateWorker();
    };
  }, [terminateWorker]);

  return {
    isSupported,
    isRunning,
    error,
    createWorker,
    terminateWorker,
    postMessage,
    onMessage,
    onError
  };
};
