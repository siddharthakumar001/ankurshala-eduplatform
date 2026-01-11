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
        booking.teacherName.toLowerCase().includes(searchQuery.toLowerCase())
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

  const formatDateTime = (date: string, time: string) => {
    return `${new Date(date).toLocaleDateString()} at ${time}`
  }

  const calculateDuration = (startTime: string, endTime: string) => {
    const start = new Date(`2000-01-01T${startTime}`)
    const end = new Date(`2000-01-01T${endTime}`)
    return Math.round((end.getTime() - start.getTime()) / (1000 * 60))
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
      case 'CONFIRMED':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200'
      case 'CANCELLED':
        return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
      case 'RESCHEDULED':
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
    <div className="container mx-auto py-8 px-4">
        <div className="mb-8">
          <h1 className="text-3xl font-bold mb-2 dark:text-white">Booking History</h1>
          <p className="text-gray-600 dark:text-gray-400">View and manage your past bookings</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <Card className="dark:bg-gray-800">
            <CardHeader className="pb-3">
              <CardDescription className="dark:text-gray-400">Total Bookings</CardDescription>
              <CardTitle className="text-3xl dark:text-white">{totalElements}</CardTitle>
            </CardHeader>
          </Card>
          <Card className="dark:bg-gray-800">
            <CardHeader className="pb-3">
              <CardDescription className="dark:text-gray-400">Completed</CardDescription>
              <CardTitle className="text-3xl text-green-600 dark:text-green-400">{completedCount}</CardTitle>
            </CardHeader>
          </Card>
          <Card className="dark:bg-gray-800">
            <CardHeader className="pb-3">
              <CardDescription className="dark:text-gray-400">Confirmed</CardDescription>
              <CardTitle className="text-3xl text-blue-600 dark:text-blue-400">{confirmedCount}</CardTitle>
            </CardHeader>
          </Card>
        </div>

        <Card className="mb-6 dark:bg-gray-800">
          <CardContent className="pt-6">
            <div className="flex flex-col sm:flex-row gap-4">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                <Input
                  placeholder="Search by topic or teacher name..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10 dark:bg-gray-700 dark:text-white dark:border-gray-600"
                />
              </div>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="w-full sm:w-[200px] dark:bg-gray-700 dark:text-white dark:border-gray-600">
                  <SelectValue placeholder="Filter by status" />
                </SelectTrigger>
                <SelectContent className="dark:bg-gray-700">
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="COMPLETED">Completed</SelectItem>
                  <SelectItem value="CONFIRMED">Confirmed</SelectItem>
                  <SelectItem value="CANCELLED">Cancelled</SelectItem>
                  <SelectItem value="RESCHEDULED">Rescheduled</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </CardContent>
        </Card>

        {filteredBookings.length === 0 ? (
          <Card className="dark:bg-gray-800">
            <CardContent className="text-center py-12">
              <Calendar className="h-16 w-16 mx-auto text-gray-400 mb-4" />
              <h3 className="text-xl font-semibold mb-2 dark:text-white">No Bookings Found</h3>
              <p className="text-gray-600 dark:text-gray-400 mb-6">
                {searchQuery || statusFilter !== 'all' 
                  ? 'No bookings match your search criteria.'
                  : "You haven't booked any classes yet."}
              </p>
              {!searchQuery && statusFilter === 'all' && (
                <Button onClick={() => router.push('/student/booking')} className="dark:bg-blue-600 dark:hover:bg-blue-700">
                  Book Your First Class
                </Button>
              )}
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-4">
            {filteredBookings.map((booking) => (
              <Card key={booking.id} className="dark:bg-gray-800 dark:border-gray-700">
                <CardContent className="p-6">
                  <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
                    <div className="flex-1 space-y-3">
                      <div className="flex items-start justify-between">
                        <div>
                          <h3 className="text-lg font-semibold mb-1 dark:text-white">{booking.topicTitle}</h3>
                          <Badge className={getStatusColor(booking.status)}>
                            {booking.status}
                          </Badge>
                        </div>
                      </div>

                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">
                        <div className="flex items-center text-gray-600 dark:text-gray-400">
                          <User className="h-4 w-4 mr-2" />
                          <span>Teacher: {booking.teacherName}</span>
                        </div>
                        <div className="flex items-center text-gray-600 dark:text-gray-400">
                          <Calendar className="h-4 w-4 mr-2" />
                          <span>{formatDateTime(booking.date, booking.startTime)}</span>
                        </div>
                        <div className="flex items-center text-gray-600 dark:text-gray-400">
                          <Clock className="h-4 w-4 mr-2" />
                          <span>Duration: {calculateDuration(booking.startTime, booking.endTime)} minutes</span>
                        </div>
                        <div className="flex items-center text-gray-600 dark:text-gray-400">
                          <DollarSign className="h-4 w-4 mr-2" />
                          <span>Price: {booking.currency} {booking.price}</span>
                        </div>
                      </div>

                      {booking.notes && (
                        <div className="flex items-start text-sm text-gray-600 dark:text-gray-400 mt-2">
                          <FileText className="h-4 w-4 mr-2 mt-0.5 flex-shrink-0" />
                          <span>{booking.notes}</span>
                        </div>
                      )}
                    </div>

                    <div className="flex flex-col gap-2 sm:ml-4">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleViewDetails(booking.id)}
                        className="dark:bg-gray-700 dark:text-white dark:border-gray-600 dark:hover:bg-gray-600"
                      >
                        View Details
                      </Button>
                      {booking.status === 'COMPLETED' && (
                        <Button
                          variant="default"
                          size="sm"
                          onClick={() => handleRateSession(booking.id)}
                          className="dark:bg-blue-600 dark:hover:bg-blue-700"
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
            <p className="text-sm text-gray-600 dark:text-gray-400">
              Page {currentPage + 1} of {totalPages} ({totalElements} total bookings)
            </p>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={handlePrevPage}
                disabled={currentPage === 0}
                className="dark:bg-gray-700 dark:text-white dark:border-gray-600 dark:hover:bg-gray-600"
              >
                <ChevronLeft className="h-4 w-4 mr-1" />
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={handleNextPage}
                disabled={currentPage >= totalPages - 1}
                className="dark:bg-gray-700 dark:text-white dark:border-gray-600 dark:hover:bg-gray-600"
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
