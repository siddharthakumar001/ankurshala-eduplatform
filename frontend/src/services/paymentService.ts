/**
 * Payment Service
 * Handles Razorpay payment integration for bookings
 */

import { api } from '@/utils/api'

// =========================== TYPE DEFINITIONS ===========================

export interface CreatePaymentOrderRequest {
  bookingId: number
  amount: number
  currency?: string // Default: INR
  notes?: string
}

export interface PaymentOrderResponse {
  orderId: string
  amount: number
  currency: string
  keyId: string // Razorpay key_id for frontend
  receipt: string
  status: string
  orderDetails: any
  bookingId: number
  message: string
}

export interface VerifyPaymentRequest {
  orderId: string
  paymentId: string
  signature: string
  bookingId: number
}

export interface VerifyPaymentResponse {
  success: boolean
  status: string // VERIFIED, FAILED, PENDING
  message: string
  transactionId?: string
  paymentId?: string
  orderId?: string
  amount?: number
  currency?: string
  paidAt?: string
  bookingId: number
  receiptUrl?: string
}

export interface BillingSummary {
  totalAmount: number
  paidAmount: number
  pendingAmount: number
  overdueAmount: number
  totalInvoices: number
  paidInvoices: number
  pendingInvoices: number
  overdueInvoices: number
  lastPaymentDate?: string
  monthlySpending: number
  yearlySpending: number
  defaultPaymentMethod?: PaymentMethod
}

export interface PaymentMethod {
  id: number
  type: 'CARD' | 'UPI' | 'NET_BANKING' | 'WALLET'
  lastFourDigits?: string
  cardBrand?: string
  upiId?: string
  default: boolean
  createdAt: string
  updatedAt: string
}

export interface PaymentHistory {
  id: number
  bookingId: number
  bookingTitle: string
  teacherName: string
  sessionDate: string
  sessionDuration: number
  amount: number
  discountAmount: number
  finalAmount: number
  paymentStatus: 'PAID' | 'PENDING' | 'FAILED' | 'REFUNDED'
  paymentMethod?: string
  transactionId?: string
  paidAt?: string
  dueDate: string
  invoiceUrl?: string
  receiptUrl?: string
}

// Declare Razorpay on window for TypeScript
declare global {
  interface Window {
    Razorpay: any
  }
}

// =========================== API SERVICE CLASS ===========================

class PaymentService {
  private baseUrl = '/student/payments'

  /**
   * Create a Razorpay payment order
   */
  async createPaymentOrder(request: CreatePaymentOrderRequest): Promise<PaymentOrderResponse> {
    const url = `${this.baseUrl}/create-order`
    console.log('PaymentService: Creating payment order:', request)

    try {
      const response = await api.post<PaymentOrderResponse>(url, request)
      console.log('PaymentService: Order created:', response.data.orderId)
      return response.data
    } catch (error) {
      console.error('PaymentService: Order creation failed:', error)
      throw error
    }
  }

  /**
   * Verify payment after Razorpay checkout
   */
  async verifyPayment(request: VerifyPaymentRequest): Promise<VerifyPaymentResponse> {
    const url = `${this.baseUrl}/verify`
    console.log('PaymentService: Verifying payment:', request.paymentId)

    try {
      const response = await api.post<VerifyPaymentResponse>(url, request)
      console.log('PaymentService: Payment verified:', response.data.success)
      return response.data
    } catch (error) {
      console.error('PaymentService: Payment verification failed:', error)
      throw error
    }
  }

  /**
   * Get billing summary
   */
  async getBillingSummary(): Promise<BillingSummary> {
    const url = `${this.baseUrl}/summary`
    console.log('PaymentService: Getting billing summary')

    try {
      const response = await api.get<BillingSummary>(url)
      console.log('PaymentService: Billing summary loaded')
      return response.data
    } catch (error) {
      console.error('PaymentService: Billing summary load failed:', error)
      throw error
    }
  }

  /**
   * Get payment history
   */
  async getPaymentHistory(page: number = 0, size: number = 20): Promise<{
    content: PaymentHistory[]
    totalElements: number
    totalPages: number
  }> {
    const url = `${this.baseUrl}/history?page=${page}&size=${size}`
    console.log('PaymentService: Getting payment history')

    try {
      const response = await api.get<any>(url)
      console.log('PaymentService: Payment history loaded:', response.data.content.length)
      return response.data
    } catch (error) {
      console.error('PaymentService: Payment history load failed:', error)
      throw error
    }
  }

  /**
   * Get payment methods
   */
  async getPaymentMethods(): Promise<PaymentMethod[]> {
    const url = `${this.baseUrl}/methods`
    console.log('PaymentService: Getting payment methods')

    try {
      const response = await api.get<PaymentMethod[]>(url)
      console.log('PaymentService: Payment methods loaded:', response.data.length)
      return response.data
    } catch (error) {
      console.error('PaymentService: Payment methods load failed:', error)
      throw error
    }
  }

  /**
   * Initialize and open Razorpay checkout
   */
  async openRazorpayCheckout(
    order: PaymentOrderResponse,
    options: {
      name?: string
      description?: string
      email?: string
      contact?: string
      onSuccess?: (response: any) => void
      onFailure?: (error: any) => void
    }
  ): Promise<any> {
    return new Promise((resolve, reject) => {
      // Load Razorpay script if not already loaded
      if (!window.Razorpay) {
        const script = document.createElement('script')
        script.src = 'https://checkout.razorpay.com/v1/checkout.js'
        script.async = true
        script.onload = () => this.initializeCheckout(order, options, resolve, reject)
        script.onerror = () => reject(new Error('Failed to load Razorpay SDK'))
        document.body.appendChild(script)
      } else {
        this.initializeCheckout(order, options, resolve, reject)
      }
    })
  }

  private initializeCheckout(
    order: PaymentOrderResponse,
    options: any,
    resolve: Function,
    reject: Function
  ) {
    const razorpayOptions = {
      key: order.keyId,
      amount: order.amount * 100, // Convert to paise
      currency: order.currency,
      name: options.name || 'Ankurshala',
      description: options.description || 'Booking Payment',
      order_id: order.orderId,
      prefill: {
        email: options.email,
        contact: options.contact,
      },
      theme: {
        color: '#3B82F6', // Blue color
      },
      handler: async (response: any) => {
        console.log('Payment successful:', response)
        if (options.onSuccess) {
          options.onSuccess(response)
        }
        resolve(response)
      },
      modal: {
        ondismiss: () => {
          console.log('Payment cancelled by user')
          const error = new Error('Payment cancelled')
          if (options.onFailure) {
            options.onFailure(error)
          }
          reject(error)
        },
      },
    }

    const razorpay = new window.Razorpay(razorpayOptions)
    razorpay.on('payment.failed', (response: any) => {
      console.error('Payment failed:', response.error)
      if (options.onFailure) {
        options.onFailure(response.error)
      }
      reject(response.error)
    })

    razorpay.open()
  }

  /**
   * Complete payment flow (create order + open checkout + verify)
   */
  async processPayment(
    bookingId: number,
    amount: number,
    userDetails: {
      name: string
      email: string
      contact: string
    }
  ): Promise<VerifyPaymentResponse> {
    try {
      // Step 1: Create order
      console.log('Step 1: Creating payment order')
      const order = await this.createPaymentOrder({
        bookingId,
        amount,
        currency: 'INR',
        notes: `Payment for booking #${bookingId}`,
      })

      // Step 2: Open Razorpay checkout
      console.log('Step 2: Opening Razorpay checkout')
      const paymentResponse = await this.openRazorpayCheckout(order, {
        name: 'Ankurshala Education',
        description: `Booking Payment #${bookingId}`,
        email: userDetails.email,
        contact: userDetails.contact,
      })

      // Step 3: Verify payment
      console.log('Step 3: Verifying payment')
      const verification = await this.verifyPayment({
        orderId: paymentResponse.razorpay_order_id,
        paymentId: paymentResponse.razorpay_payment_id,
        signature: paymentResponse.razorpay_signature,
        bookingId,
      })

      console.log('Payment flow completed successfully')
      return verification
    } catch (error) {
      console.error('Payment flow failed:', error)
      throw error
    }
  }
}

// Export singleton instance
export const paymentService = new PaymentService()
