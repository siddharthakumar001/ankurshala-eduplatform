'use client';

import { useState, useCallback, useEffect } from 'react';

interface WebComponentsResult {
  isSupported: boolean;
  error: string | null;
  defineCustomElement: (name: string, constructor: CustomElementConstructor) => boolean;
  getCustomElement: (name: string) => CustomElementConstructor | undefined;
  whenDefined: (name: string) => Promise<CustomElementConstructor>;
  upgrade: (element: Element) => void;
  getCustomElements: () => string[];
}

export const useWebComponents = (): WebComponentsResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = typeof customElements !== 'undefined';

  const defineCustomElement = useCallback((
    name: string, 
    constructor: CustomElementConstructor
  ): boolean => {
    if (!isSupported) {
      setError('Custom Elements not supported');
      return false;
    }

    try {
      customElements.define(name, constructor);
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to define custom element');
      return false;
    }
  }, [isSupported]);

  const getCustomElement = useCallback((name: string): CustomElementConstructor | undefined => {
    if (!isSupported) {
      setError('Custom Elements not supported');
      return undefined;
    }

    try {
      return customElements.get(name);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get custom element');
      return undefined;
    }
  }, [isSupported]);

  const whenDefined = useCallback(async (name: string): Promise<CustomElementConstructor> => {
    if (!isSupported) {
      setError('Custom Elements not supported');
      throw new Error('Custom Elements not supported');
    }

    try {
      return await customElements.whenDefined(name);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to wait for custom element');
      throw err;
    }
  }, [isSupported]);

  const upgrade = useCallback((element: Element): void => {
    if (!isSupported) {
      setError('Custom Elements not supported');
      return;
    }

    try {
      customElements.upgrade(element);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to upgrade element');
    }
  }, [isSupported]);

  const getCustomElements = useCallback((): string[] => {
    if (!isSupported) {
      setError('Custom Elements not supported');
      return [];
    }

    try {
      // This is a workaround since there's no direct API to get all defined custom elements
      const elements: string[] = [];
      const allElements = document.querySelectorAll('*');
      
      allElements.forEach(element => {
        const tagName = element.tagName.toLowerCase();
        if (tagName.includes('-') && !elements.includes(tagName)) {
          elements.push(tagName);
        }
      });
      
      return elements;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get custom elements');
      return [];
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    defineCustomElement,
    getCustomElement,
    whenDefined,
    upgrade,
    getCustomElements
  };
};
