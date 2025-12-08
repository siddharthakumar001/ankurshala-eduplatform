'use client';

import { useState, useMemo } from 'react';

type SortDirection = 'asc' | 'desc';

interface SortOptions<T> {
  initialField?: keyof T;
  initialDirection?: SortDirection;
}

interface SortResult<T> {
  sortField: keyof T | null;
  sortDirection: SortDirection;
  sortedItems: T[];
  sort: (field: keyof T) => void;
  clearSort: () => void;
}

export const useSort = <T>(
  items: T[],
  options: SortOptions<T> = {}
): SortResult<T> => {
  const [sortField, setSortField] = useState<keyof T | null>(options.initialField || null);
  const [sortDirection, setSortDirection] = useState<SortDirection>(options.initialDirection || 'asc');

  const sortedItems = useMemo(() => {
    if (!sortField) {
      return items;
    }

    return [...items].sort((a, b) => {
      const aValue = a[sortField];
      const bValue = b[sortField];

      // Handle null/undefined values
      if (aValue === null || aValue === undefined) {
        return sortDirection === 'asc' ? 1 : -1;
      }
      if (bValue === null || bValue === undefined) {
        return sortDirection === 'asc' ? -1 : 1;
      }

      // Handle different data types
      if (typeof aValue === 'string' && typeof bValue === 'string') {
        const comparison = aValue.localeCompare(bValue);
        return sortDirection === 'asc' ? comparison : -comparison;
      }

      if (typeof aValue === 'number' && typeof bValue === 'number') {
        const comparison = aValue - bValue;
        return sortDirection === 'asc' ? comparison : -comparison;
      }

      if (aValue instanceof Date && bValue instanceof Date) {
        const comparison = aValue.getTime() - bValue.getTime();
        return sortDirection === 'asc' ? comparison : -comparison;
      }

      // Fallback to string comparison
      const comparison = String(aValue).localeCompare(String(bValue));
      return sortDirection === 'asc' ? comparison : -comparison;
    });
  }, [items, sortField, sortDirection]);

  const sort = (field: keyof T) => {
    if (sortField === field) {
      // Toggle direction if same field
      setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc');
    } else {
      // Set new field with ascending direction
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const clearSort = () => {
    setSortField(null);
    setSortDirection('asc');
  };

  return {
    sortField,
    sortDirection,
    sortedItems,
    sort,
    clearSort
  };
};
