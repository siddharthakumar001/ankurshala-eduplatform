'use client';

import { useState, useMemo } from 'react';

interface PaginationOptions {
  initialPage?: number;
  pageSize?: number;
  totalItems?: number;
}

interface PaginationResult<T> {
  currentPage: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  items: T[];
  paginatedItems: T[];
  hasNextPage: boolean;
  hasPreviousPage: boolean;
  goToPage: (page: number) => void;
  nextPage: () => void;
  previousPage: () => void;
  setPageSize: (size: number) => void;
  setItems: (items: T[]) => void;
  setTotalItems: (total: number) => void;
}

export const usePagination = <T>(options: PaginationOptions = {}): PaginationResult<T> => {
  const {
    initialPage = 1,
    pageSize = 10,
    totalItems = 0
  } = options;

  const [currentPage, setCurrentPage] = useState(initialPage);
  const [currentPageSize, setCurrentPageSize] = useState(pageSize);
  const [currentTotalItems, setCurrentTotalItems] = useState(totalItems);
  const [items, setItems] = useState<T[]>([]);

  const totalPages = useMemo(() => {
    return Math.ceil(currentTotalItems / currentPageSize);
  }, [currentTotalItems, currentPageSize]);

  const paginatedItems = useMemo(() => {
    const startIndex = (currentPage - 1) * currentPageSize;
    const endIndex = startIndex + currentPageSize;
    return items.slice(startIndex, endIndex);
  }, [items, currentPage, currentPageSize]);

  const hasNextPage = currentPage < totalPages;
  const hasPreviousPage = currentPage > 1;

  const goToPage = (page: number) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
    }
  };

  const nextPage = () => {
    if (hasNextPage) {
      setCurrentPage(prev => prev + 1);
    }
  };

  const previousPage = () => {
    if (hasPreviousPage) {
      setCurrentPage(prev => prev - 1);
    }
  };

  const setPageSize = (size: number) => {
    setCurrentPageSize(size);
    setCurrentPage(1); // Reset to first page when page size changes
  };

  const setTotalItems = (total: number) => {
    setCurrentTotalItems(total);
    // Adjust current page if it's beyond the new total pages
    const newTotalPages = Math.ceil(total / currentPageSize);
    if (currentPage > newTotalPages && newTotalPages > 0) {
      setCurrentPage(newTotalPages);
    }
  };

  return {
    currentPage,
    pageSize: currentPageSize,
    totalItems: currentTotalItems,
    totalPages,
    items,
    paginatedItems,
    hasNextPage,
    hasPreviousPage,
    goToPage,
    nextPage,
    previousPage,
    setPageSize,
    setItems,
    setTotalItems
  };
};
