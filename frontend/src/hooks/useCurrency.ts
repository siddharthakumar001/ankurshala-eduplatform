'use client';

import { useMemo } from 'react';

export const useCurrency = () => {
  const formatCurrency = (amountCents: number, currency: string = 'INR') => {
    const amount = amountCents / 100;
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    }).format(amount);
  };

  const formatCurrencyWithSymbol = (amountCents: number, currency: string = 'INR') => {
    const amount = amountCents / 100;
    if (currency === 'INR') {
      return `₹${amount.toLocaleString('en-IN')}`;
    }
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: currency
    }).format(amount);
  };

  const parseCurrency = (amount: string): number => {
    // Remove currency symbols and parse
    const cleanAmount = amount.replace(/[₹$€£,]/g, '');
    const parsed = parseFloat(cleanAmount);
    return isNaN(parsed) ? 0 : parsed;
  };

  const convertToCents = (amount: number): number => {
    return Math.round(amount * 100);
  };

  const convertFromCents = (amountCents: number): number => {
    return amountCents / 100;
  };

  const calculateDiscount = (originalAmountCents: number, discountPercent: number): number => {
    return Math.round(originalAmountCents * (discountPercent / 100));
  };

  const calculateTax = (amountCents: number, taxPercent: number): number => {
    return Math.round(amountCents * (taxPercent / 100));
  };

  const calculateTotal = (amountCents: number, discountCents: number = 0, taxCents: number = 0): number => {
    return amountCents - discountCents + taxCents;
  };

  return {
    formatCurrency,
    formatCurrencyWithSymbol,
    parseCurrency,
    convertToCents,
    convertFromCents,
    calculateDiscount,
    calculateTax,
    calculateTotal
  };
};
