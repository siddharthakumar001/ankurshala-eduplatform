'use client';

import { useState, useMemo } from 'react';

type FilterFunction<T> = (item: T) => boolean;

interface FilterResult<T> {
  filters: Map<string, FilterFunction<T>>;
  filteredItems: T[];
  addFilter: (key: string, filter: FilterFunction<T>) => void;
  removeFilter: (key: string) => void;
  clearFilters: () => void;
  hasFilter: (key: string) => boolean;
}

export const useFilter = <T>(items: T[]): FilterResult<T> => {
  const [filters, setFilters] = useState<Map<string, FilterFunction<T>>>(new Map());

  const filteredItems = useMemo(() => {
    if (filters.size === 0) {
      return items;
    }

    return items.filter(item => {
      for (const filter of filters.values()) {
        if (!filter(item)) {
          return false;
        }
      }
      return true;
    });
  }, [items, filters]);

  const addFilter = (key: string, filter: FilterFunction<T>) => {
    setFilters(prev => new Map(prev).set(key, filter));
  };

  const removeFilter = (key: string) => {
    setFilters(prev => {
      const newFilters = new Map(prev);
      newFilters.delete(key);
      return newFilters;
    });
  };

  const clearFilters = () => {
    setFilters(new Map());
  };

  const hasFilter = (key: string) => {
    return filters.has(key);
  };

  return {
    filters,
    filteredItems,
    addFilter,
    removeFilter,
    clearFilters,
    hasFilter
  };
};
