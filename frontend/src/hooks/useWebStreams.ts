'use client';

import { useState, useCallback, useRef } from 'react';

interface WebStreamsResult {
  isSupported: boolean;
  error: string | null;
  createReadableStream: (source: any) => ReadableStream | null;
  createWritableStream: (sink: any) => WritableStream | null;
  createTransformStream: (transformer: any) => TransformStream | null;
  pipeTo: (readable: ReadableStream, writable: WritableStream) => Promise<void>;
  pipeThrough: (readable: ReadableStream, transform: TransformStream) => ReadableStream;
}

export const useWebStreams = (): WebStreamsResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = typeof ReadableStream !== 'undefined' && typeof WritableStream !== 'undefined';

  const createReadableStream = useCallback((source: any): ReadableStream | null => {
    if (!isSupported) {
      setError('Web Streams not supported');
      return null;
    }

    try {
      return new ReadableStream(source);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create readable stream');
      return null;
    }
  }, [isSupported]);

  const createWritableStream = useCallback((sink: any): WritableStream | null => {
    if (!isSupported) {
      setError('Web Streams not supported');
      return null;
    }

    try {
      return new WritableStream(sink);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create writable stream');
      return null;
    }
  }, [isSupported]);

  const createTransformStream = useCallback((transformer: any): TransformStream | null => {
    if (!isSupported) {
      setError('Web Streams not supported');
      return null;
    }

    try {
      return new TransformStream(transformer);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create transform stream');
      return null;
    }
  }, [isSupported]);

  const pipeTo = useCallback(async (readable: ReadableStream, writable: WritableStream): Promise<void> => {
    if (!isSupported) {
      setError('Web Streams not supported');
      return;
    }

    try {
      await readable.pipeTo(writable);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to pipe stream');
    }
  }, [isSupported]);

  const pipeThrough = useCallback((readable: ReadableStream, transform: TransformStream): ReadableStream => {
    if (!isSupported) {
      setError('Web Streams not supported');
      return readable;
    }

    try {
      return readable.pipeThrough(transform);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to pipe through stream');
      return readable;
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    createReadableStream,
    createWritableStream,
    createTransformStream,
    pipeTo,
    pipeThrough
  };
};
