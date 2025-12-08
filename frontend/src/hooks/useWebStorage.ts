'use client';

import { useState, useCallback, useEffect } from 'react';

interface WebStorageResult {
  getItem: (key: string) => string | null;
  setItem: (key: string, value: string) => void;
  removeItem: (key: string) => void;
  clear: () => void;
  getLength: () => number;
  getKey: (index: number) => string | null;
  getAllKeys: () => string[];
  getAllItems: () => Record<string, string>;
}

export const useWebStorage = (storage: 'localStorage' | 'sessionStorage' = 'localStorage'): WebStorageResult => {
  const [storageInstance, setStorageInstance] = useState<Storage | null>(null);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      setStorageInstance(window[storage]);
    }
  }, [storage]);

  const getItem = useCallback((key: string): string | null => {
    if (!storageInstance) return null;
    try {
      return storageInstance.getItem(key);
    } catch (error) {
      console.error(`Error getting item ${key}:`, error);
      return null;
    }
  }, [storageInstance]);

  const setItem = useCallback((key: string, value: string): void => {
    if (!storageInstance) return;
    try {
      storageInstance.setItem(key, value);
    } catch (error) {
      console.error(`Error setting item ${key}:`, error);
    }
  }, [storageInstance]);

  const removeItem = useCallback((key: string): void => {
    if (!storageInstance) return;
    try {
      storageInstance.removeItem(key);
    } catch (error) {
      console.error(`Error removing item ${key}:`, error);
    }
  }, [storageInstance]);

  const clear = useCallback((): void => {
    if (!storageInstance) return;
    try {
      storageInstance.clear();
    } catch (error) {
      console.error('Error clearing storage:', error);
    }
  }, [storageInstance]);

  const getLength = useCallback((): number => {
    if (!storageInstance) return 0;
    try {
      return storageInstance.length;
    } catch (error) {
      console.error('Error getting storage length:', error);
      return 0;
    }
  }, [storageInstance]);

  const getKey = useCallback((index: number): string | null => {
    if (!storageInstance) return null;
    try {
      return storageInstance.key(index);
    } catch (error) {
      console.error(`Error getting key at index ${index}:`, error);
      return null;
    }
  }, [storageInstance]);

  const getAllKeys = useCallback((): string[] => {
    if (!storageInstance) return [];
    try {
      const keys: string[] = [];
      for (let i = 0; i < storageInstance.length; i++) {
        const key = storageInstance.key(i);
        if (key) keys.push(key);
      }
      return keys;
    } catch (error) {
      console.error('Error getting all keys:', error);
      return [];
    }
  }, [storageInstance]);

  const getAllItems = useCallback((): Record<string, string> => {
    if (!storageInstance) return {};
    try {
      const items: Record<string, string> = {};
      for (let i = 0; i < storageInstance.length; i++) {
        const key = storageInstance.key(i);
        if (key) {
          const value = storageInstance.getItem(key);
          if (value) items[key] = value;
        }
      }
      return items;
    } catch (error) {
      console.error('Error getting all items:', error);
      return {};
    }
  }, [storageInstance]);

  return {
    getItem,
    setItem,
    removeItem,
    clear,
    getLength,
    getKey,
    getAllKeys,
    getAllItems
  };
};
