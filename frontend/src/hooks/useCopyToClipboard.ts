'use client';

import { useState } from 'react';

export const useCopyToClipboard = () => {
  const [copied, setCopied] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const copyToClipboard = async (text: string) => {
    try {
      if (!navigator.clipboard) {
        throw new Error('Clipboard API not supported');
      }

      await navigator.clipboard.writeText(text);
      setCopied(true);
      setError(null);
      
      // Reset copied state after 2 seconds
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to copy');
      setCopied(false);
    }
  };

  const reset = () => {
    setCopied(false);
    setError(null);
  };

  return {
    copied,
    error,
    copyToClipboard,
    reset
  };
};
