'use client';

import { useState, useCallback } from 'react';

interface PaymentRequestOptions {
  methodData: PaymentMethodData[];
  details: PaymentDetailsInit;
  options?: PaymentOptions;
}

interface WebPaymentResult {
  isSupported: boolean;
  error: string | null;
  canMakePayment: (methodData: PaymentMethodData[]) => Promise<boolean>;
  showPaymentRequest: (options: PaymentRequestOptions) => Promise<PaymentResponse | null>;
  abortPaymentRequest: () => Promise<boolean>;
  getAvailablePaymentMethods: () => Promise<PaymentMethodData[]>;
}

export const useWebPayment = (): WebPaymentResult => {
  const [error, setError] = useState<string | null>(null);
  const isSupported = typeof PaymentRequest !== 'undefined';

  const canMakePayment = useCallback(async (methodData: PaymentMethodData[]): Promise<boolean> => {
    if (!isSupported) {
      setError('Payment Request API not supported');
      return false;
    }

    try {
      const paymentRequest = new PaymentRequest(methodData, {
        total: {
          label: 'Total',
          amount: { currency: 'INR', value: '0' }
        }
      });

      const canPay = await paymentRequest.canMakePayment();
      return canPay || false;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to check payment capability');
      return false;
    }
  }, [isSupported]);

  const showPaymentRequest = useCallback(async (
    options: PaymentRequestOptions
  ): Promise<PaymentResponse | null> => {
    if (!isSupported) {
      setError('Payment Request API not supported');
      return null;
    }

    try {
      const paymentRequest = new PaymentRequest(
        options.methodData,
        options.details,
        options.options
      );

      const response = await paymentRequest.show();
      return response;
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        return null; // User cancelled
      }
      setError(err instanceof Error ? err.message : 'Payment request failed');
      return null;
    }
  }, [isSupported]);

  const abortPaymentRequest = useCallback(async (): Promise<boolean> => {
    if (!isSupported) {
      setError('Payment Request API not supported');
      return false;
    }

    try {
      // This would need to be called on an active PaymentRequest instance
      // For now, we'll return false as we don't have access to the instance
      return false;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to abort payment request');
      return false;
    }
  }, [isSupported]);

  const getAvailablePaymentMethods = useCallback(async (): Promise<PaymentMethodData[]> => {
    if (!isSupported) {
      setError('Payment Request API not supported');
      return [];
    }

    try {
      // Common payment methods
      const methods: PaymentMethodData[] = [
        {
          supportedMethods: 'basic-card',
          data: {
            supportedNetworks: ['visa', 'mastercard', 'amex', 'discover']
          }
        },
        {
          supportedMethods: 'https://apple.com/apple-pay',
          data: {
            version: 3,
            merchantIdentifier: 'merchant.com.example',
            merchantCapabilities: ['supports3DS'],
            supportedNetworks: ['visa', 'mastercard', 'amex'],
            countryCode: 'IN'
          }
        },
        {
          supportedMethods: 'https://google.com/pay',
          data: {
            environment: 'TEST',
            apiVersion: 2,
            apiVersionMinor: 0,
            allowedPaymentMethods: [{
              type: 'CARD',
              parameters: {
                allowedAuthMethods: ['PAN_ONLY', 'CRYPTOGRAM_3DS'],
                allowedCardNetworks: ['AMEX', 'DISCOVER', 'JCB', 'MASTERCARD', 'VISA']
              }
            }]
          }
        }
      ];

      return methods;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get payment methods');
      return [];
    }
  }, [isSupported]);

  return {
    isSupported,
    error,
    canMakePayment,
    showPaymentRequest,
    abortPaymentRequest,
    getAvailablePaymentMethods
  };
};
