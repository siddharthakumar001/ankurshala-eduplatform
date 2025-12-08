'use client';

import { useState, useCallback } from 'react';

interface SpeechSynthesisOptions {
  rate?: number;
  pitch?: number;
  volume?: number;
  voice?: SpeechSynthesisVoice;
}

interface SpeechSynthesisResult {
  isSpeaking: boolean;
  isPaused: boolean;
  voices: SpeechSynthesisVoice[];
  speak: (text: string, options?: SpeechSynthesisOptions) => void;
  pause: () => void;
  resume: () => void;
  stop: () => void;
  error: string | null;
}

export const useSpeechSynthesis = (): SpeechSynthesisResult => {
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const [voices, setVoices] = useState<SpeechSynthesisVoice[]>([]);
  const [error, setError] = useState<string | null>(null);

  const loadVoices = useCallback(() => {
    if ('speechSynthesis' in window) {
      setVoices(speechSynthesis.getVoices());
    }
  }, []);

  const speak = useCallback((text: string, options: SpeechSynthesisOptions = {}) => {
    if (!('speechSynthesis' in window)) {
      setError('Speech synthesis not supported');
      return;
    }

    try {
      const utterance = new SpeechSynthesisUtterance(text);
      
      utterance.rate = options.rate || 1;
      utterance.pitch = options.pitch || 1;
      utterance.volume = options.volume || 1;
      utterance.voice = options.voice || null;

      utterance.onstart = () => {
        setIsSpeaking(true);
        setIsPaused(false);
        setError(null);
      };

      utterance.onend = () => {
        setIsSpeaking(false);
        setIsPaused(false);
      };

      utterance.onerror = (event) => {
        setError(`Speech synthesis error: ${event.error}`);
        setIsSpeaking(false);
        setIsPaused(false);
      };

      speechSynthesis.speak(utterance);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Speech synthesis failed');
    }
  }, []);

  const pause = useCallback(() => {
    if ('speechSynthesis' in window) {
      speechSynthesis.pause();
      setIsPaused(true);
    }
  }, []);

  const resume = useCallback(() => {
    if ('speechSynthesis' in window) {
      speechSynthesis.resume();
      setIsPaused(false);
    }
  }, []);

  const stop = useCallback(() => {
    if ('speechSynthesis' in window) {
      speechSynthesis.cancel();
      setIsSpeaking(false);
      setIsPaused(false);
    }
  }, []);

  // Load voices when component mounts
  useState(() => {
    loadVoices();
    if ('speechSynthesis' in window) {
      speechSynthesis.addEventListener('voiceschanged', loadVoices);
    }
  });

  return {
    isSpeaking,
    isPaused,
    voices,
    speak,
    pause,
    resume,
    stop,
    error
  };
};
