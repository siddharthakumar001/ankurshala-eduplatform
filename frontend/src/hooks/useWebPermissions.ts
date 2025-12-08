'use client';

import { useState, useCallback, useEffect } from 'react';

interface WebPermissionsResult {
  isSupported: boolean;
  error: string | null;
  queryPermission: (name: PermissionName) => Promise<PermissionState | null>;
  requestPermission: (name: PermissionName) => Promise<PermissionState | null>;
  revokePermission: (name: PermissionName) => Promise<boolean>;
  getAllPermissions: () => Promise<PermissionStatus[]>;
  hasPermission: (name: PermissionName) => Promise<boolean>;
  onPermissionChange: (name: PermissionName, callback: (state: PermissionState) => void) => void;
  offPermissionChange: (name: PermissionName, callback: (state: PermissionState) => void) => void;
}

export const useWebPermissions = (): WebPermissionsResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = typeof navigator !== 'undefined' && 'permissions' in navigator;

  const queryPermission = useCallback(async (
    name: PermissionName
  ): Promise<PermissionState | null> => {
    if (!isSupported) {
      setError('Permissions API not supported');
      return null;
    }

    try {
      const permission = await navigator.permissions.query({ name });
      return permission.state;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to query permission');
      return null;
    }
  }, [isSupported]);

  const requestPermission = useCallback(async (
    name: PermissionName
  ): Promise<PermissionState | null> => {
    if (!isSupported) {
      setError('Permissions API not supported');
      return null;
    }

    try {
      const permission = await navigator.permissions.query({ name });
      return permission.state;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to request permission');
      return null;
    }
  }, [isSupported]);

  const revokePermission = useCallback(async (name: PermissionName): Promise<boolean> => {
    if (!isSupported) {
      setError('Permissions API not supported');
      return false;
    }

    // Note: Permissions API doesn't support revoking permissions programmatically
    // User must revoke permissions through browser settings
    setError('Revoking permissions is not supported by the browser. User must revoke through browser settings.');
    return false;
  }, [isSupported]);

  const getAllPermissions = useCallback(async (): Promise<PermissionStatus[]> => {
    if (!isSupported) {
      setError('Permissions API not supported');
      return [];
    }

    try {
      const permissions = await navigator.permissions.query({ name: 'all' as PermissionName });
      return Array.isArray(permissions) ? permissions : [permissions];
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get all permissions');
      return [];
    }
  }, [isSupported]);

  const hasPermission = useCallback(async (name: PermissionName): Promise<boolean> => {
    if (!isSupported) {
      setError('Permissions API not supported');
      return false;
    }

    try {
      const permission = await navigator.permissions.query({ name });
      return permission.state === 'granted';
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to check permission');
      return false;
    }
  }, [isSupported]);

  const onPermissionChange = useCallback((
    name: PermissionName, 
    callback: (state: PermissionState) => void
  ): void => {
    if (!isSupported) {
      setError('Permissions API not supported');
      return;
    }

    try {
      navigator.permissions.query({ name }).then(permission => {
        permission.addEventListener('change', () => {
          callback(permission.state);
        });
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to add permission change listener');
    }
  }, [isSupported]);

  const offPermissionChange = useCallback((
    name: PermissionName, 
    callback: (state: PermissionState) => void
  ): void => {
    if (!isSupported) {
      setError('Permissions API not supported');
      return;
    }

    try {
      navigator.permissions.query({ name }).then(permission => {
        permission.removeEventListener('change', () => {
          callback(permission.state);
        });
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to remove permission change listener');
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    queryPermission,
    requestPermission,
    revokePermission,
    getAllPermissions,
    hasPermission,
    onPermissionChange,
    offPermissionChange
  };
};
