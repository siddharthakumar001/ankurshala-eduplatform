'use client';

import { useState, useEffect, useMemo } from 'react';
import { useDebounce } from './useDebounce';

interface SearchOptions<T> {
  searchFields: (keyof T)[];
  caseSensitive?: boolean;
  debounceMs?: number;
}

interface SearchResult<T> {
  searchTerm: string;
  filteredItems: T[];
  isSearching: boolean;
  setSearchTerm: (term: string) => void;
  clearSearch: () => void;
}

export const useSearch = <T>(
  items: T[],
  options: SearchOptions<T>
): SearchResult<T> => {
  const [searchTerm, setSearchTerm] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  
  const debouncedSearchTerm = useDebounce(searchTerm, options.debounceMs || 300);

  const filteredItems = useMemo(() => {
    if (!debouncedSearchTerm.trim()) {
      return items;
    }

    setIsSearching(true);
    
    const term = options.caseSensitive 
      ? debouncedSearchTerm 
      : debouncedSearchTerm.toLowerCase();

    const filtered = items.filter(item => {
      return options.searchFields.some(field => {
        const fieldValue = item[field];
        if (fieldValue === null || fieldValue === undefined) {
          return false;
        }

        const value = options.caseSensitive 
          ? String(fieldValue) 
          : String(fieldValue).toLowerCase();

        return value.includes(term);
      });
    });

    setIsSearching(false);
    return filtered;
  }, [items, debouncedSearchTerm, options.searchFields, options.caseSensitive]);

  const clearSearch = () => {
    setSearchTerm('');
  };

  return {
    searchTerm,
    filteredItems,
    isSearching,
    setSearchTerm,
    clearSearch
  };
};
