'use client';

import { useState, useCallback } from 'react';

interface ContactInfo {
  name?: string;
  email?: string;
  tel?: string;
  address?: ContactAddress[];
  icon?: Blob[];
}

interface ContactAddress {
  country?: string;
  countryCode?: string;
  locality?: string;
  postalCode?: string;
  region?: string;
  streetAddress?: string;
  type?: string;
}

interface WebContactsResult {
  isSupported: boolean;
  error: string | null;
  selectContacts: (properties: string[]) => Promise<ContactInfo[] | null>;
  getContacts: (properties: string[]) => Promise<ContactInfo[] | null>;
  hasPermission: () => Promise<boolean>;
  requestPermission: () => Promise<boolean>;
}

export const useWebContacts = (): WebContactsResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = typeof navigator !== 'undefined' && 'contacts' in navigator;

  const selectContacts = useCallback(async (properties: string[]): Promise<ContactInfo[] | null> => {
    if (!isSupported) {
      setError('Contacts API not supported');
      return null;
    }

    try {
      const contacts = await (navigator as any).contacts.select(properties);
      return contacts;
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        return null; // User cancelled
      }
      setError(err instanceof Error ? err.message : 'Failed to select contacts');
      return null;
    }
  }, [isSupported]);

  const getContacts = useCallback(async (properties: string[]): Promise<ContactInfo[] | null> => {
    if (!isSupported) {
      setError('Contacts API not supported');
      return null;
    }

    try {
      const contacts = await (navigator as any).contacts.get(properties);
      return contacts;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get contacts');
      return null;
    }
  }, [isSupported]);

  const hasPermission = useCallback(async (): Promise<boolean> => {
    if (!isSupported) {
      setError('Contacts API not supported');
      return false;
    }

    try {
      const permission = await navigator.permissions.query({ name: 'contacts' as PermissionName });
      return permission.state === 'granted';
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to check permission');
      return false;
    }
  }, [isSupported]);

  const requestPermission = useCallback(async (): Promise<boolean> => {
    if (!isSupported) {
      setError('Contacts API not supported');
      return false;
    }

    try {
      const permission = await navigator.permissions.query({ name: 'contacts' as PermissionName });
      return permission.state === 'granted';
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to request permission');
      return false;
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    selectContacts,
    getContacts,
    hasPermission,
    requestPermission
  };
};
