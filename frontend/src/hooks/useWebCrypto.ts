'use client';

import { useState, useCallback } from 'react';

interface WebCryptoResult {
  isSupported: boolean;
  error: string | null;
  generateKey: (algorithm: string, extractable: boolean, keyUsages: string[]) => Promise<CryptoKey | CryptoKeyPair | null>;
  encrypt: (algorithm: string, key: CryptoKey, data: ArrayBuffer) => Promise<ArrayBuffer | null>;
  decrypt: (algorithm: string, key: CryptoKey, data: ArrayBuffer) => Promise<ArrayBuffer | null>;
  sign: (algorithm: string, key: CryptoKey, data: ArrayBuffer) => Promise<ArrayBuffer | null>;
  verify: (algorithm: string, key: CryptoKey, signature: ArrayBuffer, data: ArrayBuffer) => Promise<boolean>;
  digest: (algorithm: string, data: ArrayBuffer) => Promise<ArrayBuffer | null>;
  randomBytes: (length: number) => Uint8Array | null;
}

export const useWebCrypto = (): WebCryptoResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = typeof crypto !== 'undefined' && 'subtle' in crypto;

  const generateKey = useCallback(async (
    algorithm: string, 
    extractable: boolean, 
    keyUsages: string[]
  ): Promise<CryptoKey | CryptoKeyPair | null> => {
    if (!isSupported) {
      setError('Web Crypto API not supported');
      return null;
    }

    try {
      const key = await crypto.subtle.generateKey(
        { name: algorithm },
        extractable,
        keyUsages as KeyUsage[]
      );
      return key;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to generate key');
      return null;
    }
  }, [isSupported]);

  const encrypt = useCallback(async (
    algorithm: string, 
    key: CryptoKey, 
    data: ArrayBuffer
  ): Promise<ArrayBuffer | null> => {
    if (!isSupported) {
      setError('Web Crypto API not supported');
      return null;
    }

    try {
      const encrypted = await crypto.subtle.encrypt(
        { name: algorithm },
        key,
        data
      );
      return encrypted;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to encrypt data');
      return null;
    }
  }, [isSupported]);

  const decrypt = useCallback(async (
    algorithm: string, 
    key: CryptoKey, 
    data: ArrayBuffer
  ): Promise<ArrayBuffer | null> => {
    if (!isSupported) {
      setError('Web Crypto API not supported');
      return null;
    }

    try {
      const decrypted = await crypto.subtle.decrypt(
        { name: algorithm },
        key,
        data
      );
      return decrypted;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to decrypt data');
      return null;
    }
  }, [isSupported]);

  const sign = useCallback(async (
    algorithm: string, 
    key: CryptoKey, 
    data: ArrayBuffer
  ): Promise<ArrayBuffer | null> => {
    if (!isSupported) {
      setError('Web Crypto API not supported');
      return null;
    }

    try {
      const signature = await crypto.subtle.sign(
        { name: algorithm },
        key,
        data
      );
      return signature;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to sign data');
      return null;
    }
  }, [isSupported]);

  const verify = useCallback(async (
    algorithm: string, 
    key: CryptoKey, 
    signature: ArrayBuffer, 
    data: ArrayBuffer
  ): Promise<boolean> => {
    if (!isSupported) {
      setError('Web Crypto API not supported');
      return false;
    }

    try {
      const isValid = await crypto.subtle.verify(
        { name: algorithm },
        key,
        signature,
        data
      );
      return isValid;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to verify signature');
      return false;
    }
  }, [isSupported]);

  const digest = useCallback(async (
    algorithm: string, 
    data: ArrayBuffer
  ): Promise<ArrayBuffer | null> => {
    if (!isSupported) {
      setError('Web Crypto API not supported');
      return null;
    }

    try {
      const hash = await crypto.subtle.digest(algorithm, data);
      return hash;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create digest');
      return null;
    }
  }, [isSupported]);

  const randomBytes = useCallback((length: number): Uint8Array | null => {
    if (!isSupported) {
      setError('Web Crypto API not supported');
      return null;
    }

    try {
      const array = new Uint8Array(length);
      crypto.getRandomValues(array);
      return array;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to generate random bytes');
      return null;
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    generateKey,
    encrypt,
    decrypt,
    sign,
    verify,
    digest,
    randomBytes
  };
};
