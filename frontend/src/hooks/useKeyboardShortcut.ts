'use client';

import { useEffect, useCallback } from 'react';

interface KeyboardShortcutOptions {
  ctrlKey?: boolean;
  altKey?: boolean;
  shiftKey?: boolean;
  metaKey?: boolean;
  preventDefault?: boolean;
}

export const useKeyboardShortcut = (
  key: string,
  callback: () => void,
  options: KeyboardShortcutOptions = {}
) => {
  const {
    ctrlKey = false,
    altKey = false,
    shiftKey = false,
    metaKey = false,
    preventDefault = true
  } = options;

  const handleKeyDown = useCallback((event: KeyboardEvent) => {
    if (
      event.key === key &&
      event.ctrlKey === ctrlKey &&
      event.altKey === altKey &&
      event.shiftKey === shiftKey &&
      event.metaKey === metaKey
    ) {
      if (preventDefault) {
        event.preventDefault();
      }
      callback();
    }
  }, [key, callback, ctrlKey, altKey, shiftKey, metaKey, preventDefault]);

  useEffect(() => {
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [handleKeyDown]);
};
