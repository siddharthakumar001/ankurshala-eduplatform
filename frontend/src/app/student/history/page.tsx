'use client'

import { useState, useEffect, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Badge } from '@/components/ui/badge'
import { bookingService, BookingResponse } from '@/services/bookingService'
import { StudentRoute } from '@/components/route-guard'
import { toast } from 'sonner'
import { Search, ChevronLeft, ChevronRight, Calendar, Clock, User, DollarSign, FileText, Loader2 } from 'lucide-react'

// Two-component pattern: prevents API calls before auth is verified
export default function BookingHistoryPage() {
  return (
    <StudentRoute>
      <HistoryContent />
    </StudentRoute>
  )
}

function HistoryContent() {
  const router = useRouter()
  const [bookings, setBookings] = useState<BookingResponse[]>([])
  const [filteredBookings, setFilteredBookings] = useState<BookingResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [searchQuery, setSearchQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState('all')

  const loadBookingHistory = useCallback(async () => {
    try {
      setLoading(true)
      const response = await bookingService.getBookingHistory(currentPage, 20)
      setBookings(response.content)
      setTotalPages(response.totalPages)
      setTotalElements(response.totalElements)
    } catch (error) {
      console.error('Failed to load booking history:', error)
      toast.error('Failed to load booking history')
    } finally {
      setLoading(false)
    }
  }, [currentPage])

  useEffect(() => {
    loadBookingHistory()
  }, [loadBookingHistory])

  const filterBookings = useCallback(() => {
    let filtered = [...bookings]

    if (searchQuery) {
      filtered = filtered.filter(booking =>
        booking.topicTitle.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (booking.teacherName || '').toLowerCase().includes(searchQuery.toLowerCase())
      )
    }

    if (statusFilter !== 'all') {
      filtered = filtered.filter(booking => booking.status === statusFilter)
    }

    setFilteredBookings(filtered)
  }, [bookings, searchQuery, statusFilter])

  useEffect(() => {
    filterBookings()
  }, [filterBookings])

  const formatDateTime = (startTime: string) => {
    return new Date(startTime).toLocaleString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
      case 'CONFIRMED':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200'
      case 'CANCELLED':
        return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
      case 'ACCEPTED':
      case 'IN_PROGRESS':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
      case 'PENDING':
        return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200'
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200'
    }
  }

  const handleViewDetails = (bookingId: number) => {
    router.push(`/student/bookings/${bookingId}`)
  }

  const handleRateSession = (bookingId: number) => {
    router.push(`/student/bookings/${bookingId}?rate=true`)
  }

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      setCurrentPage(currentPage + 1)
    }
  }

  const handlePrevPage = () => {
    if (currentPage > 0) {
      setCurrentPage(currentPage - 1)
    }
  }

  const completedCount = bookings.filter(b => b.status === 'COMPLETED').length
  const confirmedCount = bookings.filter(b => b.status === 'CONFIRMED').length

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Loader2 className="h-8 w-8 animate-spin text-emerald-600" />
      </div>
    )
  }

  return (
    <div className="space-y-6 max-w-6xl mx-auto px-4">
        <div className="page-header">
          <h1 className="text-3xl font-bold text-white">Booking History</h1>
          <p className="text-white/80">View and manage your past bookings</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <Card className="glass-panel">
            <CardHeader className="pb-3">
              <CardDescription className="text-slate-500 dark:text-slate-300">Total Bookings</CardDescription>
              <CardTitle className="text-3xl text-slate-900 dark:text-white">{totalElements}</CardTitle>
            </CardHeader>
          </Card>
          <Card className="glass-panel">
            <CardHeader className="pb-3">
              <CardDescription className="text-slate-500 dark:text-slate-300">Completed</CardDescription>
              <CardTitle className="text-3xl text-emerald-500">{completedCount}</CardTitle>
            </CardHeader>
          </Card>
          <Card className="glass-panel">
            <CardHeader className="pb-3">
              <CardDescription className="text-slate-500 dark:text-slate-300">Confirmed</CardDescription>
              <CardTitle className="text-3xl text-sky-500">{confirmedCount}</CardTitle>
            </CardHeader>
          </Card>
        </div>

        <Card className="glass-panel border border-white/40 mb-6">
          <CardContent className="pt-6">
            <div className="flex flex-col sm:flex-row gap-4">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400 h-4 w-4" />
                <Input
                  placeholder="Search by topic or teacher name..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="input-modern pl-10"
                />
              </div>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="input-modern w-full sm:w-[200px]">
                  <SelectValue placeholder="Filter by status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="COMPLETED">Completed</SelectItem>
                  <SelectItem value="CONFIRMED">Confirmed</SelectItem>
                  <SelectItem value="CANCELLED">Cancelled</SelectItem>
                  <SelectItem value="ACCEPTED">Accepted</SelectItem>
                  <SelectItem value="PENDING">Pending</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </CardContent>
        </Card>

        {filteredBookings.length === 0 ? (
          <Card className="glass-panel border border-white/40">
            <CardContent className="text-center py-12">
              <Calendar className="h-16 w-16 mx-auto text-slate-400 mb-4" />
              <h3 className="text-xl font-semibold mb-2 text-slate-900 dark:text-white">No Bookings Found</h3>
              <p className="text-slate-600 dark:text-slate-300 mb-6">
                {searchQuery || statusFilter !== 'all' 
                  ? 'No bookings match your search criteria.'
                  : "You haven't booked any classes yet."}
              </p>
              {!searchQuery && statusFilter === 'all' && (
                <Button className="btn-primary" onClick={() => router.push('/student/booking')}>
                  Book Your First Class
                </Button>
              )}
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-4">
            {filteredBookings.map((booking) => (
              <Card key={booking.id} className="glass-panel border border-white/40">
                <CardContent className="p-6">
                  <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
                    <div className="flex-1 space-y-3">
                      <div className="flex items-start justify-between">
                        <div>
                          <h3 className="text-lg font-semibold mb-1 text-slate-900 dark:text-white">{booking.topicTitle}</h3>
                          <Badge className={getStatusColor(booking.status)}>
                            {booking.status}
                          </Badge>
                        </div>
                      </div>

                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">
                        <div className="flex items-center text-slate-600 dark:text-slate-300">
                          <User className="h-4 w-4 mr-2" />
                          <span>Teacher: {booking.teacherName}</span>
                        </div>
                        <div className="flex items-center text-slate-600 dark:text-slate-300">
                          <Calendar className="h-4 w-4 mr-2" />
                          <span>{formatDateTime(booking.startTime)}</span>
                        </div>
                        <div className="flex items-center text-slate-600 dark:text-slate-300">
                          <Clock className="h-4 w-4 mr-2" />
                          <span>Duration: {booking.durationMinutes} minutes</span>
                        </div>
                        <div className="flex items-center text-slate-600 dark:text-slate-300">
                          <DollarSign className="h-4 w-4 mr-2" />
                          <span>Price: {booking.priceCurrency || 'INR'} {booking.priceMin ?? 0}</span>
                        </div>
                      </div>

                      {booking.studentNotes && (
                        <div className="flex items-start text-sm text-slate-600 dark:text-slate-300 mt-2">
                          <FileText className="h-4 w-4 mr-2 mt-0.5 flex-shrink-0" />
                          <span>{booking.studentNotes}</span>
                        </div>
                      )}
                    </div>

                    <div className="flex flex-col gap-2 sm:ml-4">
                      <Button
                        variant="outline"
                        size="sm"
                        className="btn-outline h-9 px-4 text-sm"
                        onClick={() => handleViewDetails(booking.id)}
                      >
                        View Details
                      </Button>
                      {booking.status === 'COMPLETED' && (
                        <Button
                          size="sm"
                          className="btn-primary h-9 px-4 text-sm"
                          onClick={() => handleRateSession(booking.id)}
                        >
                          Rate Session
                        </Button>
                      )}
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}

        {totalPages > 1 && (
          <div className="flex items-center justify-between mt-6">
            <p className="text-sm text-slate-600 dark:text-slate-300">
              Page {currentPage + 1} of {totalPages} ({totalElements} total bookings)
            </p>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={handlePrevPage}
                disabled={currentPage === 0}
                className="btn-outline h-9 px-4 text-sm"
              >
                <ChevronLeft className="h-4 w-4 mr-1" />
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={handleNextPage}
                disabled={currentPage >= totalPages - 1}
                className="btn-outline h-9 px-4 text-sm"
              >
                Next
                <ChevronRight className="h-4 w-4 ml-1" />
              </Button>
            </div>
          </div>
        )}
      </div>
  )
}
