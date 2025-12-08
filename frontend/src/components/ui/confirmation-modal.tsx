'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { AlertTriangle, AlertCircle, Info, CheckCircle } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';

interface ConfirmationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  description: string;
  type?: 'warning' | 'danger' | 'info' | 'success';
  confirmText?: string;
  cancelText?: string;
  isLoading?: boolean;
}

export function ConfirmationModal({
  isOpen,
  onClose,
  onConfirm,
  title,
  description,
  type = 'warning',
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  isLoading = false,
}: ConfirmationModalProps) {
  const getIcon = () => {
    switch (type) {
      case 'danger':
        return <AlertCircle className="h-6 w-6 text-red-500" />;
      case 'warning':
        return <AlertTriangle className="h-6 w-6 text-yellow-500" />;
      case 'info':
        return <Info className="h-6 w-6 text-blue-500" />;
      case 'success':
        return <CheckCircle className="h-6 w-6 text-green-500" />;
      default:
        return <AlertTriangle className="h-6 w-6 text-yellow-500" />;
    }
  };

  const getConfirmButtonVariant = () => {
    switch (type) {
      case 'danger':
        return 'destructive';
      case 'warning':
        return 'default';
      case 'info':
        return 'default';
      case 'success':
        return 'default';
      default:
        return 'default';
    }
  };

  const handleConfirm = () => {
    onConfirm();
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <div className="flex items-center space-x-3">
            {getIcon()}
            <div>
              <DialogTitle>{title}</DialogTitle>
              <DialogDescription className="mt-2">
                {description}
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>
        <DialogFooter className="flex-col sm:flex-row gap-2">
          <Button
            variant="outline"
            onClick={onClose}
            disabled={isLoading}
            className="w-full sm:w-auto"
          >
            {cancelText}
          </Button>
          <Button
            variant={getConfirmButtonVariant()}
            onClick={handleConfirm}
            disabled={isLoading}
            className="w-full sm:w-auto"
          >
            {isLoading ? 'Processing...' : confirmText}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

interface FeePreviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  action: 'RESCHEDULE' | 'CANCEL';
  originalAmount: number;
  feeAmount: number;
  finalAmount: number;
  reason: string;
  isLoading?: boolean;
}

export function FeePreviewModal({
  isOpen,
  onClose,
  onConfirm,
  action,
  originalAmount,
  feeAmount,
  finalAmount,
  reason,
  isLoading = false,
}: FeePreviewModalProps) {
  const isRefund = finalAmount > originalAmount;
  const refundAmount = isRefund ? finalAmount - originalAmount : 0;

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center space-x-2">
            <AlertTriangle className="h-5 w-5 text-yellow-500" />
            <span>Fee Preview</span>
          </DialogTitle>
          <DialogDescription>
            {action === 'RESCHEDULE' 
              ? 'Rescheduling this booking will incur the following fees:'
              : 'Cancelling this booking will incur the following fees:'
            }
          </DialogDescription>
        </DialogHeader>
        
        <div className="space-y-4">
          <div className="bg-gray-50 p-4 rounded-lg space-y-2">
            <div className="flex justify-between text-sm">
              <span>Original Amount:</span>
              <span>₹{originalAmount.toFixed(0)}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span>{action === 'RESCHEDULE' ? 'Reschedule Fee:' : 'Cancellation Fee:'}</span>
              <span className="text-red-600">-₹{feeAmount.toFixed(0)}</span>
            </div>
            {isRefund && (
              <div className="flex justify-between text-sm">
                <span>Refund:</span>
                <span className="text-green-600">+₹{refundAmount.toFixed(0)}</span>
              </div>
            )}
            <hr />
            <div className="flex justify-between font-semibold">
              <span>Final Amount:</span>
              <span className={finalAmount > 0 ? 'text-green-600' : 'text-red-600'}>
                {finalAmount > 0 ? '+' : ''}₹{finalAmount.toFixed(0)}
              </span>
            </div>
          </div>
          
          <div className="bg-yellow-50 p-3 rounded-lg">
            <p className="text-sm text-yellow-800">
              <strong>Reason:</strong> {reason}
            </p>
          </div>
        </div>

        <DialogFooter className="flex-col sm:flex-row gap-2">
          <Button
            variant="outline"
            onClick={onClose}
            disabled={isLoading}
            className="w-full sm:w-auto"
          >
            Cancel
          </Button>
          <Button
            variant="default"
            onClick={onConfirm}
            disabled={isLoading}
            className="w-full sm:w-auto"
          >
            {isLoading ? 'Processing...' : `Confirm ${action}`}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
