'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { 
  CreditCard, 
  DollarSign, 
  Calendar, 
  CheckCircle, 
  Clock, 
  AlertCircle,
  Download,
  Eye,
  Loader2,
  Plus,
  Settings
} from 'lucide-react'
import { toast } from 'sonner'
import { useAuthStore } from '@/store/auth'
import { api } from '@/utils/api'
import { StudentRoute } from '@/components/route-guard'

interface StudentBillingSummary {
  totalAmount: number
  paidAmount: number
  pendingAmount: number
  overdueAmount: number
  totalInvoices: number
  paidInvoices: number
  pendingInvoices: number
  overdueInvoices: number
  lastPaymentDate: string | null
  monthlySpending: number
  yearlySpending: number
  defaultPaymentMethod: {
    id: number
    type: string
    lastFourDigits: string
    cardBrand: string
    isDefault: boolean
  }
}

interface StudentPayment {
  id: number
  bookingId: number
  bookingTitle: string
  teacherName: string
  sessionDate: string
  sessionDuration: number
  amount: number
  discountAmount: number
  finalAmount: number
  paymentStatus: string
  paymentMethod: string
  transactionId: string | null
  paidAt: string | null
  dueDate: string
  invoiceUrl: string
  receiptUrl: string | null
}

interface StudentPaymentMethod {
  id: number
  type: string
  lastFourDigits: string
  cardBrand: string
  upiId: string
  bankName: string
  isDefault: boolean
  createdAt: string
  updatedAt: string
}

// Two-component pattern: prevents API calls before auth is verified
export default function StudentPaymentPage() {
  return (
    <StudentRoute>
      <PaymentContent />
    </StudentRoute>
  )
}

function PaymentContent() {
  const [activeTab, setActiveTab] = useState('overview')
  const [loading, setLoading] = useState(true)
  const [billingSummary, setBillingSummary] = useState<StudentBillingSummary | null>(null)
  const [payments, setPayments] = useState<StudentPayment[]>([])
  const [paymentMethods, setPaymentMethods] = useState<StudentPaymentMethod[]>([])
  const [processingPayment, setProcessingPayment] = useState<number | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  
  const user = useAuthStore((state) => state.user)
  const router = useRouter()

  useEffect(() => {
    fetchPaymentData()
  }, [])

  const fetchPaymentData = async () => {
    try {
      setLoading(true)
      setErrorMessage(null)
      
      // Fetch billing summary
      const summaryResponse = await api.get('/student/payments/summary')
      setBillingSummary(summaryResponse.data as any)
      
      // Fetch payment history
      const paymentsResponse = await api.get('/student/payments/history')
      setPayments((paymentsResponse.data as any).content || [])
      
      // Fetch payment methods
      const methodsResponse = await api.get('/student/payments/methods')
      setPaymentMethods((methodsResponse.data as any) || [])
      
    } catch (error) {
      console.error('Error fetching payment data:', error)
      setErrorMessage('We could not load your payment data right now. Please refresh or try again in a moment.')
      toast.error('Payments are temporarily unavailable. Retrying later usually resolves this.')
    } finally {
      setLoading(false)
    }
  }

  const handleProcessPayment = async (paymentId: number, amount: number) => {
    try {
      setProcessingPayment(paymentId)
      
      const response = await api.post('/student/payments/process', {
        bookingId: paymentId,
        amount: amount,
        paymentMethodId: String(billingSummary?.defaultPaymentMethod.id || 1)
      })
      
      const responseData = response.data as any
      if (responseData.success) {
        if (responseData.paymentUrl) {
          // Redirect to payment URL
          window.open(responseData.paymentUrl, '_blank')
          toast.success('Redirecting to payment gateway...')
        } else {
          toast.success('Payment processed successfully')
        }
        fetchPaymentData() // Refresh data
      } else {
        toast.error(responseData.message || 'Payment failed')
      }
    } catch (error) {
      console.error('Error processing payment:', error)
      toast.error('Failed to process payment')
    } finally {
      setProcessingPayment(null)
    }
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR'
    }).format(amount)
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

type MetricTone = 'emerald' | 'amber' | 'red' | 'white'

function MetricPill({ label, value, tone = 'white' }: { label: string; value: string; tone?: MetricTone }) {
  const toneClasses: Record<MetricTone, string> = {
    white: 'bg-white/15 text-white',
    emerald: 'bg-emerald-500/30 text-white',
    amber: 'bg-amber-500/30 text-white',
    red: 'bg-red-500/30 text-white'
  }
  return (
    <div className={`rounded-2xl px-4 py-3 backdrop-blur-sm border border-white/20 ${toneClasses[tone]}`}>
      <p className="text-xs uppercase tracking-wide text-white/80">{label}</p>
      <p className="text-lg font-semibold">{value}</p>
    </div>
  )
}

  const getPaymentStatusBadge = (status: string) => {
    switch (status) {
      case 'PAID':
        return <Badge variant="outline" className="bg-green-50 text-green-700 border-green-200">Paid</Badge>
      case 'PENDING':
        return <Badge variant="outline" className="bg-yellow-50 text-yellow-700 border-yellow-200">Pending</Badge>
      case 'OVERDUE':
        return <Badge variant="outline" className="bg-red-50 text-red-700 border-red-200">Overdue</Badge>
      case 'WAIVED':
        return <Badge variant="outline" className="bg-blue-50 text-blue-700 border-blue-200">Waived</Badge>
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  const getPaymentMethodIcon = (type: string) => {
    switch (type) {
      case 'CARD':
        return <CreditCard className="h-4 w-4" />
      case 'UPI':
        return <DollarSign className="h-4 w-4" />
      case 'NET_BANKING':
        return <CreditCard className="h-4 w-4" />
      default:
        return <CreditCard className="h-4 w-4" />
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-transparent py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-center h-64">
            <div className="glass-panel rounded-2xl border border-white/40 px-6 py-4 flex items-center">
              <Loader2 className="h-8 w-8 animate-spin text-ankur-primary" />
              <span className="ml-2 text-gray-600 dark:text-gray-300">Loading payment data...</span>
            </div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-transparent py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
          {/* Hero */}
          <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-emerald-600 via-emerald-500 to-sky-600 text-white shadow-lg">
            <div className="absolute inset-0 bg-white/10 blur-3xl" />
            <div className="relative px-6 py-6 sm:px-8 sm:py-8 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <div>
                <p className="text-sm uppercase tracking-[0.18em] text-white/80">Student Workspace</p>
                <h1 className="text-3xl font-bold mt-1">Payments & Billing</h1>
                <p className="text-white/80 mt-1">Manage your payments, wallet and billing preferences.</p>
              </div>
              {billingSummary && (
                <div className="grid grid-cols-3 gap-3 w-full sm:w-auto">
                  <MetricPill label="Total" value={formatCurrency(billingSummary.totalAmount)} />
                  <MetricPill label="Pending" value={formatCurrency(billingSummary.pendingAmount)} tone="amber" />
                  <MetricPill label="Overdue" value={formatCurrency(billingSummary.overdueAmount)} tone="red" />
                </div>
              )}
            </div>
          </div>

          {errorMessage && (
            <Card className="glass-panel border border-white/40">
              <CardContent className="py-4 flex items-center gap-3 text-amber-700">
                <AlertCircle className="h-5 w-5" />
                <div>
                  <p className="font-medium">Payments are temporarily unavailable.</p>
                  <p className="text-sm text-amber-700/80">{errorMessage}</p>
                </div>
                <div className="ml-auto">
                  <Button variant="outline" size="sm" className="btn-outline" onClick={fetchPaymentData}>
                    Retry
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Billing Summary Cards */}
          {billingSummary && (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
              <Card className="glass-panel border border-white/40">
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">Total Spent</CardTitle>
                  <DollarSign className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{formatCurrency(billingSummary.totalAmount)}</div>
                  <p className="text-xs text-muted-foreground">
                    {billingSummary.totalInvoices} invoices
                  </p>
                </CardContent>
              </Card>

              <Card className="glass-panel border border-white/40">
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">Monthly Spending</CardTitle>
                  <Calendar className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{formatCurrency(billingSummary.monthlySpending)}</div>
                  <p className="text-xs text-muted-foreground">
                    This month
                  </p>
                </CardContent>
              </Card>

              <Card className="glass-panel border border-white/40">
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">Pending Payments</CardTitle>
                  <Clock className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{formatCurrency(billingSummary.pendingAmount)}</div>
                  <p className="text-xs text-muted-foreground">
                    {billingSummary.pendingInvoices} pending
                  </p>
                </CardContent>
              </Card>

              <Card className="glass-panel border border-white/40">
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">Overdue</CardTitle>
                  <AlertCircle className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{formatCurrency(billingSummary.overdueAmount)}</div>
                  <p className="text-xs text-muted-foreground">
                    {billingSummary.overdueInvoices} overdue
                  </p>
                </CardContent>
              </Card>
            </div>
          )}

          {/* Main Content Tabs */}
          <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
            <TabsList className="grid w-full grid-cols-3 glass-panel rounded-2xl p-2 border border-white/40 dark:border-white/10">
              <TabsTrigger value="overview">Overview</TabsTrigger>
              <TabsTrigger value="history">Payment History</TabsTrigger>
              <TabsTrigger value="methods">Payment Methods</TabsTrigger>
            </TabsList>

            {/* Overview Tab */}
            <TabsContent value="overview" className="space-y-6">
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Recent Payments */}
                <Card className="glass-panel border border-white/40">
                  <CardHeader>
                    <CardTitle>Recent Payments</CardTitle>
                    <CardDescription>Your latest payment transactions</CardDescription>
                  </CardHeader>
                  <CardContent>
                    {payments.length === 0 ? (
                      <p className="text-gray-500 text-center py-4">No payments found</p>
                    ) : (
                      <div className="space-y-4">
                        {payments.slice(0, 5).map((payment) => (
                          <div key={payment.id} className="border border-white/40 dark:border-white/10 rounded-lg p-4 bg-white/70 dark:bg-slate-900/70">
                            <div className="flex justify-between items-start mb-2">
                              <div>
                                <h4 className="font-medium">{payment.bookingTitle}</h4>
                                <p className="text-sm text-gray-600">{payment.teacherName}</p>
                                <p className="text-xs text-gray-500">{formatDate(payment.sessionDate)}</p>
                              </div>
                              <div className="text-right">
                                <p className="font-medium">{formatCurrency(payment.finalAmount)}</p>
                                {getPaymentStatusBadge(payment.paymentStatus)}
                              </div>
                            </div>
                            <div className="flex items-center gap-4 text-sm text-gray-600">
                              <span className="flex items-center gap-1">
                                <Clock className="h-4 w-4" />
                                {payment.sessionDuration} min
                              </span>
                              {payment.transactionId && (
                                <span className="flex items-center gap-1">
                                  <CreditCard className="h-4 w-4" />
                                  {payment.paymentMethod}
                                </span>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </CardContent>
                </Card>

                {/* Default Payment Method */}
                <Card className="glass-panel border border-white/40">
                  <CardHeader>
                    <CardTitle>Default Payment Method</CardTitle>
                    <CardDescription>Your preferred payment method</CardDescription>
                  </CardHeader>
                  <CardContent>
                    {billingSummary?.defaultPaymentMethod ? (
                      <div className="border border-white/40 dark:border-white/10 rounded-lg p-4 bg-white/70 dark:bg-slate-900/70">
                        <div className="flex items-center gap-3">
                          {getPaymentMethodIcon(billingSummary.defaultPaymentMethod.type)}
                          <div>
                            <h4 className="font-medium">
                              {billingSummary.defaultPaymentMethod.type} ending in {billingSummary.defaultPaymentMethod.lastFourDigits}
                            </h4>
                            <p className="text-sm text-gray-600">{billingSummary.defaultPaymentMethod.cardBrand}</p>
                          </div>
                          <Badge variant="outline" className="ml-auto">Default</Badge>
                        </div>
                        <Button variant="outline" className="w-full mt-4 border-white/40 hover:bg-white/60 dark:hover:bg-slate-900/60">
                          <Settings className="h-4 w-4 mr-2" />
                          Manage Payment Methods
                        </Button>
                      </div>
                    ) : (
                      <div className="text-center py-8">
                        <CreditCard className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                        <h3 className="text-lg font-medium text-gray-900 mb-2">No Payment Method</h3>
                        <p className="text-gray-500 mb-4">Add a payment method to get started</p>
                        <Button>
                          <Plus className="h-4 w-4 mr-2" />
                          Add Payment Method
                        </Button>
                      </div>
                    )}
                  </CardContent>
                </Card>
              </div>
            </TabsContent>

            {/* Payment History Tab */}
            <TabsContent value="history" className="space-y-6">
              <Card className="glass-panel border border-white/40">
                <CardHeader>
                  <CardTitle>Payment History</CardTitle>
                  <CardDescription>All your payment transactions</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {payments.map((payment) => (
                      <div key={payment.id} className="border border-white/40 dark:border-white/10 rounded-lg p-4 bg-white/70 dark:bg-slate-900/70">
                        <div className="flex justify-between items-start mb-2">
                          <div>
                            <h4 className="font-medium">{payment.bookingTitle}</h4>
                            <p className="text-sm text-gray-600">{payment.teacherName}</p>
                            <p className="text-xs text-gray-500">{formatDateTime(payment.sessionDate)}</p>
                          </div>
                          <div className="text-right">
                            <p className="font-medium">{formatCurrency(payment.finalAmount)}</p>
                            {getPaymentStatusBadge(payment.paymentStatus)}
                          </div>
                        </div>
                        <div className="flex items-center gap-4 text-sm text-gray-600">
                          <span className="flex items-center gap-1">
                            <Clock className="h-4 w-4" />
                            {payment.sessionDuration} minutes
                          </span>
                          {payment.transactionId && (
                            <span className="flex items-center gap-1">
                              <CreditCard className="h-4 w-4" />
                              {payment.paymentMethod} - {payment.transactionId}
                            </span>
                          )}
                          {payment.paidAt && (
                            <span className="flex items-center gap-1">
                              <CheckCircle className="h-4 w-4" />
                              Paid on {formatDate(payment.paidAt)}
                            </span>
                          )}
                        </div>
                        <div className="flex gap-2 mt-4">
                          {payment.paymentStatus === 'PENDING' && (
                            <Button
                              size="sm"
                              onClick={() => handleProcessPayment(payment.bookingId, payment.finalAmount)}
                              disabled={processingPayment === payment.bookingId}
                            >
                              {processingPayment === payment.bookingId ? (
                                <>
                                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                  Processing...
                                </>
                              ) : (
                                <>
                                  <CreditCard className="h-4 w-4 mr-2" />
                                  Pay Now
                                </>
                              )}
                            </Button>
                          )}
                          <Button size="sm" variant="outline">
                            <Download className="h-4 w-4 mr-2" />
                            Invoice
                          </Button>
                          {payment.receiptUrl && (
                            <Button size="sm" variant="outline">
                              <Eye className="h-4 w-4 mr-2" />
                              Receipt
                            </Button>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* Payment Methods Tab */}
            <TabsContent value="methods" className="space-y-6">
              <Card className="glass-panel border border-white/40">
                <CardHeader>
                  <CardTitle>Payment Methods</CardTitle>
                  <CardDescription>Manage your payment methods</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {paymentMethods.map((method) => (
                      <div key={method.id} className="border border-white/40 dark:border-white/10 rounded-lg p-4 bg-white/70 dark:bg-slate-900/70">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-3">
                            {getPaymentMethodIcon(method.type)}
                            <div>
                              <h4 className="font-medium">
                                {method.type} {method.lastFourDigits && `ending in ${method.lastFourDigits}`}
                                {method.upiId && `- ${method.upiId}`}
                              </h4>
                              <p className="text-sm text-gray-600">
                                {method.cardBrand || method.bankName}
                              </p>
                              <p className="text-xs text-gray-500">
                                Added on {formatDate(method.createdAt)}
                              </p>
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            {method.isDefault && (
                              <Badge variant="outline" className="bg-blue-50 text-blue-700 border-blue-200">
                                Default
                              </Badge>
                            )}
                            <Button size="sm" variant="outline">
                              <Settings className="h-4 w-4 mr-2" />
                              Edit
                            </Button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                  <Button className="w-full mt-4">
                    <Plus className="h-4 w-4 mr-2" />
                    Add Payment Method
                  </Button>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
      </div>
    </div>
  )
}
