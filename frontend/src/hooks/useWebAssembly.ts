'use client';

import { useState, useCallback } from 'react';

interface WebAssemblyResult {
  isSupported: boolean;
  error: string | null;
  instantiate: (source: BufferSource) => Promise<WebAssembly.Instance | null>;
  instantiateStreaming: (source: Response | Promise<Response>) => Promise<WebAssembly.Instance | null>;
  compile: (source: BufferSource) => Promise<WebAssembly.Module | null>;
  compileStreaming: (source: Response | Promise<Response>) => Promise<WebAssembly.Module | null>;
  validate: (source: BufferSource) => boolean;
}

export const useWebAssembly = (): WebAssemblyResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = typeof WebAssembly !== 'undefined';

  const instantiate = useCallback(async (
    source: BufferSource
  ): Promise<WebAssembly.Instance | null> => {
    if (!isSupported) {
      setError('WebAssembly not supported');
      return null;
    }

    try {
      const wasmModule = await WebAssembly.compile(source);
      const instance = await WebAssembly.instantiate(wasmModule);
      return instance;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to instantiate WebAssembly');
      return null;
    }
  }, [isSupported]);

  const instantiateStreaming = useCallback(async (
    source: Response | Promise<Response>
  ): Promise<WebAssembly.Instance | null> => {
    if (!isSupported) {
      setError('WebAssembly not supported');
      return null;
    }

    try {
      const result = await WebAssembly.instantiateStreaming(source);
      return result.instance;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to instantiate WebAssembly stream');
      return null;
    }
  }, [isSupported]);

  const compile = useCallback(async (
    source: BufferSource
  ): Promise<WebAssembly.Module | null> => {
    if (!isSupported) {
      setError('WebAssembly not supported');
      return null;
    }

    try {
      const wasmModule = await WebAssembly.compile(source);
      return wasmModule;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to compile WebAssembly');
      return null;
    }
  }, [isSupported]);

  const compileStreaming = useCallback(async (
    source: Response | Promise<Response>
  ): Promise<WebAssembly.Module | null> => {
    if (!isSupported) {
      setError('WebAssembly not supported');
      return null;
    }

    try {
      const wasmModule = await WebAssembly.compileStreaming(source);
      return wasmModule;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to compile WebAssembly stream');
      return null;
    }
  }, [isSupported]);

  const validate = useCallback((source: BufferSource): boolean => {
    if (!isSupported) {
      setError('WebAssembly not supported');
      return false;
    }

    try {
      return WebAssembly.validate(source);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to validate WebAssembly');
      return false;
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    instantiate,
    instantiateStreaming,
    compile,
    compileStreaming,
    validate
  };
};
