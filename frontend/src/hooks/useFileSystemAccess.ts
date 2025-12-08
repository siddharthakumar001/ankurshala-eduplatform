'use client';

import { useState, useCallback } from 'react';

interface FileSystemAccessResult {
  isSupported: boolean;
  error: string | null;
  openFile: (options?: { types?: string[] }) => Promise<File | null>;
  saveFile: (content: string, options?: { suggestedName?: string; types?: string[] }) => Promise<boolean>;
  openDirectory: () => Promise<FileSystemDirectoryHandle | null>;
}

export const useFileSystemAccess = (): FileSystemAccessResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = 'showOpenFilePicker' in window && 'showSaveFilePicker' in window;

  const openFile = useCallback(async (options: { types?: string[] } = {}): Promise<File | null> => {
    if (!isSupported) {
      setError('File system access not supported');
      return null;
    }

    try {
      const [fileHandle] = await (window as any).showOpenFilePicker({
        types: options.types ? [{
          description: 'Files',
          accept: options.types.reduce((acc, type) => {
            acc[type] = [`.${type}`];
            return acc;
          }, {} as Record<string, string[]>)
        }] : undefined
      });

      const file = await fileHandle.getFile();
      return file;
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        return null; // User cancelled
      }
      setError(err instanceof Error ? err.message : 'Failed to open file');
      return null;
    }
  }, [isSupported]);

  const saveFile = useCallback(async (
    content: string, 
    options: { suggestedName?: string; types?: string[] } = {}
  ): Promise<boolean> => {
    if (!isSupported) {
      setError('File system access not supported');
      return false;
    }

    try {
      const fileHandle = await (window as any).showSaveFilePicker({
        suggestedName: options.suggestedName || 'untitled.txt',
        types: options.types ? [{
          description: 'Files',
          accept: options.types.reduce((acc, type) => {
            acc[type] = [`.${type}`];
            return acc;
          }, {} as Record<string, string[]>)
        }] : undefined
      });

      const writable = await fileHandle.createWritable();
      await writable.write(content);
      await writable.close();
      
      return true;
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        return false; // User cancelled
      }
      setError(err instanceof Error ? err.message : 'Failed to save file');
      return false;
    }
  }, [isSupported]);

  const openDirectory = useCallback(async (): Promise<FileSystemDirectoryHandle | null> => {
    if (!isSupported) {
      setError('File system access not supported');
      return null;
    }

    try {
      const directoryHandle = await (window as any).showDirectoryPicker();
      return directoryHandle;
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        return null; // User cancelled
      }
      setError(err instanceof Error ? err.message : 'Failed to open directory');
      return null;
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    openFile,
    saveFile,
    openDirectory
  };
};
