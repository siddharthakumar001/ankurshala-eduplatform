'use client';

import { useState, useCallback } from 'react';

interface WebFileSystemAccessResult {
  isSupported: boolean;
  error: string | null;
  showOpenFilePicker: (options?: any) => Promise<any[] | null>;
  showSaveFilePicker: (options?: any) => Promise<any | null>;
  showDirectoryPicker: (options?: any) => Promise<any | null>;
  getFileHandle: (name: string, options?: any) => Promise<any | null>;
  getDirectoryHandle: (name: string, options?: any) => Promise<any | null>;
  requestStorageAccess: () => Promise<boolean>;
  hasStorageAccess: () => Promise<boolean>;
}

export const useWebFileSystemAccess = (): WebFileSystemAccessResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = typeof window !== 'undefined' && 'showOpenFilePicker' in window;

  const showOpenFilePicker = useCallback(async (
    options?: any
  ): Promise<any[] | null> => {
    if (!isSupported) {
      setError('File System Access API not supported');
      return null;
    }

    try {
      const handles = await (window as any).showOpenFilePicker(options);
      return handles;
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        return null; // User cancelled
      }
      setError(err instanceof Error ? err.message : 'Failed to open file picker');
      return null;
    }
  }, [isSupported]);

  const showSaveFilePicker = useCallback(async (
    options?: any
  ): Promise<any | null> => {
    if (!isSupported) {
      setError('File System Access API not supported');
      return null;
    }

    try {
      const handle = await (window as any).showSaveFilePicker(options);
      return handle;
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        return null; // User cancelled
      }
      setError(err instanceof Error ? err.message : 'Failed to save file picker');
      return null;
    }
  }, [isSupported]);

  const showDirectoryPicker = useCallback(async (
    options?: any
  ): Promise<any | null> => {
    if (!isSupported) {
      setError('File System Access API not supported');
      return null;
    }

    try {
      const handle = await (window as any).showDirectoryPicker(options);
      return handle;
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        return null; // User cancelled
      }
      setError(err instanceof Error ? err.message : 'Failed to open directory picker');
      return null;
    }
  }, [isSupported]);

  const getFileHandle = useCallback(async (
    name: string, 
    options?: any
  ): Promise<any | null> => {
    if (!isSupported) {
      setError('File System Access API not supported');
      return null;
    }

    try {
      const handle = await (window as any).getFileHandle(name, options);
      return handle;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get file handle');
      return null;
    }
  }, [isSupported]);

  const getDirectoryHandle = useCallback(async (
    name: string, 
    options?: any
  ): Promise<any | null> => {
    if (!isSupported) {
      setError('File System Access API not supported');
      return null;
    }

    try {
      const handle = await (window as any).getDirectoryHandle(name, options);
      return handle;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get directory handle');
      return null;
    }
  }, [isSupported]);

  const requestStorageAccess = useCallback(async (): Promise<boolean> => {
    if (!isSupported) {
      setError('File System Access API not supported');
      return false;
    }

    try {
      const granted = await (window as any).requestStorageAccess();
      return granted;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to request storage access');
      return false;
    }
  }, [isSupported]);

  const hasStorageAccess = useCallback(async (): Promise<boolean> => {
    if (!isSupported) {
      setError('File System Access API not supported');
      return false;
    }

    try {
      const hasAccess = await (window as any).hasStorageAccess();
      return hasAccess;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to check storage access');
      return false;
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    showOpenFilePicker,
    showSaveFilePicker,
    showDirectoryPicker,
    getFileHandle,
    getDirectoryHandle,
    requestStorageAccess,
    hasStorageAccess
  };
};
