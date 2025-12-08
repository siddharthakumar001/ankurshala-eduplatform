'use client';

import { useState, useCallback } from 'react';

interface ClipboardResult {
  text: string | null;
  error: string | null;
  copyToClipboard: (text: string) => Promise<void>;
  readFromClipboard: () => Promise<void>;
  clearClipboard: () => void;
}

export const useClipboard = (): ClipboardResult => {
  const [text, setText] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const copyToClipboard = useCallback(async (textToCopy: string) => {
    try {
      if (!navigator.clipboard) {
        throw new Error('Clipboard API not supported');
      }

      await navigator.clipboard.writeText(textToCopy);
      setText(textToCopy);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to copy to clipboard');
      setText(null);
    }
  }, []);

  const readFromClipboard = useCallback(async () => {
    try {
      if (!navigator.clipboard) {
        throw new Error('Clipboard API not supported');
      }

      const clipboardText = await navigator.clipboard.readText();
      setText(clipboardText);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to read from clipboard');
      setText(null);
    }
  }, []);

  const clearClipboard = useCallback(() => {
    setText(null);
    setError(null);
  }, []);

  return {
    text,
    error,
    copyToClipboard,
    readFromClipboard,
    clearClipboard
  };
};
