'use client';

import { useCallback } from 'react';

interface PrintResult {
  print: (content?: string) => void;
  printElement: (element: HTMLElement) => void;
}

export const usePrint = (): PrintResult => {
  const print = useCallback((content?: string) => {
    if (content) {
      const printWindow = window.open('', '_blank');
      if (printWindow) {
        printWindow.document.write(`
          <html>
            <head>
              <title>Print</title>
              <style>
                body { font-family: Arial, sans-serif; margin: 20px; }
                @media print { body { margin: 0; } }
              </style>
            </head>
            <body>
              ${content}
            </body>
          </html>
        `);
        printWindow.document.close();
        printWindow.print();
      }
    } else {
      window.print();
    }
  }, []);

  const printElement = useCallback((element: HTMLElement) => {
    const printWindow = window.open('', '_blank');
    if (printWindow) {
      printWindow.document.write(`
        <html>
          <head>
            <title>Print</title>
            <style>
              body { font-family: Arial, sans-serif; margin: 20px; }
              @media print { body { margin: 0; } }
            </style>
          </head>
          <body>
            ${element.outerHTML}
          </body>
        </html>
      `);
      printWindow.document.close();
      printWindow.print();
    }
  }, []);

  return {
    print,
    printElement
  };
};
