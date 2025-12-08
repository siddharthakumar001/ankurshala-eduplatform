'use client';

import { useState, useCallback } from 'react';

interface WebClipboardResult {
  isSupported: boolean;
  error: string | null;
  readText: () => Promise<string | null>;
  writeText: (text: string) => Promise<boolean>;
  read: () => Promise<ClipboardItems | null>;
  write: (items: ClipboardItems) => Promise<boolean>;
  hasPermission: () => Promise<boolean>;
  requestPermission: () => Promise<boolean>;
}

export const useWebClipboard = (): WebClipboardResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = typeof navigator !== 'undefined' && 'clipboard' in navigator;

  const readText = useCallback(async (): Promise<string | null> => {
    if (!isSupported) {
      setError('Clipboard API not supported');
      return null;
    }

    try {
      const text = await navigator.clipboard.readText();
      return text;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to read text from clipboard');
      return null;
    }
  }, [isSupported]);

  const writeText = useCallback(async (text: string): Promise<boolean> => {
    if (!isSupported) {
      setError('Clipboard API not supported');
      return false;
    }

    try {
      await navigator.clipboard.writeText(text);
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to write text to clipboard');
      return false;
    }
  }, [isSupported]);

  const read = useCallback(async (): Promise<ClipboardItems | null> => {
    if (!isSupported) {
      setError('Clipboard API not supported');
      return null;
    }

    try {
      const items = await navigator.clipboard.read();
      return items;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to read from clipboard');
      return null;
    }
  }, [isSupported]);

  const write = useCallback(async (items: ClipboardItems): Promise<boolean> => {
    if (!isSupported) {
      setError('Clipboard API not supported');
      return false;
    }

    try {
      await navigator.clipboard.write(items);
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to write to clipboard');
      return false;
    }
  }, [isSupported]);

  const hasPermission = useCallback(async (): Promise<boolean> => {
    if (!isSupported) {
      setError('Clipboard API not supported');
      return false;
    }

    try {
      const permission = await navigator.permissions.query({ name: 'clipboard-read' as PermissionName });
      return permission.state === 'granted';
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to check permission');
      return false;
    }
  }, [isSupported]);

  const requestPermission = useCallback(async (): Promise<boolean> => {
    if (!isSupported) {
      setError('Clipboard API not supported');
      return false;
    }

    try {
      const permission = await navigator.permissions.query({ name: 'clipboard-read' as PermissionName });
      return permission.state === 'granted';
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to request permission');
      return false;
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    readText,
    writeText,
    read,
    write,
    hasPermission,
    requestPermission
  };
};
