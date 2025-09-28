'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { toast } from '@/hooks/use-toast'
import { useAuthStore } from '@/store/auth'
import { StudentRoute } from '@/components/route-guard'
import { Calendar, Clock, User, Star, MessageSquare, Bookmark, Search, Filter, ArrowLeft } from 'lucide-react'

interface BookingHistory {
  id: number
  topicTitle: string
  teacherName: string
  startTime: string
  endTime: string
  durationMinutes: number
  status: string
  rating?: number
  studentFeedback?: string
  teacherFeedback?: string
  bookmarked: boolean
  notes: BookingNote[]
}

interface BookingNote {
  id: number
  authorName: string
  content: string
  noteType: string
  createdAt: string
}

export default function StudentHistoryPage() {
  const [bookings, setBookings] = useState<BookingHistory[]>([])
  const [filteredBookings, setFilteredBookings] = useState<BookingHistory[]>([])
  const [selectedBooking, setSelectedBooking] = useState<BookingHistory | null>(null)
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState('all')
  const [showFeedbackModal, setShowFeedbackModal] = useState(false)
  const [feedbackText, setFeedbackText] = useState('')
  const [rating, setRating] = useState(0)
  
  const router = useRouter()
  const { user } = useAuthStore()

  useEffect(() => {
    if (!user) {
      router.push('/login')
      return
    }
    loadBookingHistory()
  }, [user, router])

  useEffect(() => {
    filterBookings()
  }, [bookings, searchTerm, statusFilter])

  const loadBookingHistory = async () => {
    try {
      // TODO: Replace with actual API call
      // const historyData = await studentAPI.getBookingHistory()
      
      // Mock data for now
      const mockBookings: BookingHistory[] = [
        {
          id: 1,
          topicTitle: 'Quadratic Equations',
          teacherName: 'Dr. Smith',
          startTime: '2024-01-10T14:00:00Z',
          endTime: '2024-01-10T15:00:00Z',
          durationMinutes: 60,
          status: 'COMPLETED',
          rating: 5,
          studentFeedback: 'Great explanation of quadratic equations!',
          teacherFeedback: 'Student was very engaged and asked good questions.',
          bookmarked: true,
          notes: [
            {
              id: 1,
              authorName: 'Dr. Smith',
              content: 'Remember to practice the quadratic formula',
              noteType: 'GENERAL',
              createdAt: '2024-01-10T15:05:00Z'
            }
          ]
        },
        {
          id: 2,
          topicTitle: 'Trigonometry Basics',
          teacherName: 'Ms. Johnson',
          startTime: '2024-01-08T10:00:00Z',
          endTime: '2024-01-08T11:30:00Z',
          durationMinutes: 90,
          status: 'COMPLETED',
          rating: 4,
          studentFeedback: 'Good session, learned a lot about sine and cosine',
          teacherFeedback: 'Student showed good understanding of basic concepts.',
          bookmarked: false,
          notes: []
        },
        {
          id: 3,
          topicTitle: 'Chemical Bonding',
          teacherName: 'Prof. Brown',
          startTime: '2024-01-05T16:00:00Z',
          endTime: '2024-01-05T17:15:00Z',
          durationMinutes: 75,
          status: 'CANCELLED',
          rating: undefined,
          studentFeedback: undefined,
          teacherFeedback: undefined,
          bookmarked: false,
          notes: []
        }
      ]
      
      setBookings(mockBookings)
    } catch (error) {
      console.error('Failed to load booking history:', error)
    } finally {
      setLoading(false)
    }
  }

  const filterBookings = () => {
    let filtered = bookings

    // Filter by search term
    if (searchTerm) {
      filtered = filtered.filter(booking =>
        booking.topicTitle.toLowerCase().includes(searchTerm.toLowerCase()) ||
        booking.teacherName.toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    // Filter by status
    if (statusFilter !== 'all') {
      filtered = filtered.filter(booking => booking.status === statusFilter)
    }

    setFilteredBookings(filtered)
  }

  const handleBookmark = async (bookingId: number) => {
    try {
      // TODO: Replace with actual API call
      // await studentAPI.bookmarkBooking(bookingId)
      
      setBookings(bookings.map(booking =>
        booking.id === bookingId
          ? { ...booking, bookmarked: !booking.bookmarked }
          : booking
      ))
      
      toast.success('Bookmark updated')
    } catch (error) {
      toast.error('Failed to update bookmark')
    }
  }

  const handleAddFeedback = async () => {
    if (!selectedBooking || !feedbackText.trim()) return

    try {
      // TODO: Replace with actual API call
      // await studentAPI.addBookingFeedback(selectedBooking.id, {
      //   rating,
      //   feedback: feedbackText
      // })
      
      setBookings(bookings.map(booking =>
        booking.id === selectedBooking.id
          ? { ...booking, rating, studentFeedback: feedbackText }
          : booking
      ))
      
      setShowFeedbackModal(false)
      setFeedbackText('')
      setRating(0)
      setSelectedBooking(null)
      
      toast.success('Feedback added successfully')
    } catch (error) {
      toast.error('Failed to add feedback')
    }
  }

  const formatTime = (timeString: string) => {
    return new Date(timeString).toLocaleString()
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-emerald-100 text-emerald-800'
      case 'CANCELLED':
        return 'bg-red-100 text-red-800'
      case 'NO_SHOW':
        return 'bg-gray-100 text-gray-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  const renderStars = (rating: number) => {
    return Array.from({ length: 5 }, (_, i) => (
      <Star
        key={i}
        className={`h-4 w-4 ${
          i < rating ? 'text-yellow-400 fill-current' : 'text-gray-300'
        }`}
      />
    ))
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg">Loading history...</div>
      </div>
    )
  }

  return (
    <StudentRoute>
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Header */}
          <div className="mb-8 flex items-center space-x-4">
            <Button 
              variant="outline" 
              size="sm"
              onClick={() => router.back()}
              className="flex items-center space-x-2"
            >
              <ArrowLeft className="h-4 w-4" />
              <span>Back</span>
            </Button>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Study History</h1>
              <p className="text-gray-600">Review your completed classes and feedback</p>
            </div>
          </div>

          {/* Filters */}
          <Card className="mb-6">
            <CardContent className="p-4">
              <div className="flex flex-col md:flex-row gap-4">
                <div className="flex-1">
                  <div className="relative">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                    <Input
                      placeholder="Search by topic or teacher..."
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                      className="pl-10"
                    />
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <Filter className="h-4 w-4 text-gray-400" />
                  <Select value={statusFilter} onValueChange={setStatusFilter}>
                    <SelectTrigger className="w-40">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Status</SelectItem>
                      <SelectItem value="COMPLETED">Completed</SelectItem>
                      <SelectItem value="CANCELLED">Cancelled</SelectItem>
                      <SelectItem value="NO_SHOW">No Show</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Bookings List */}
          <div className="space-y-4">
            {filteredBookings.length > 0 ? (
              filteredBookings.map((booking) => (
                <Card key={booking.id} className="hover:shadow-md transition-shadow">
                  <CardContent className="p-6">
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3 mb-2">
                          <h3 className="text-lg font-semibold">{booking.topicTitle}</h3>
                          <Badge className={getStatusColor(booking.status)}>
                            {booking.status}
                          </Badge>
                          {booking.bookmarked && (
                            <Bookmark className="h-4 w-4 text-emerald-600 fill-current" />
                          )}
                        </div>
                        
                        <div className="flex items-center space-x-4 text-sm text-gray-600 mb-3">
                          <div className="flex items-center space-x-1">
                            <User className="h-4 w-4" />
                            <span>{booking.teacherName}</span>
                          </div>
                          <div className="flex items-center space-x-1">
                            <Calendar className="h-4 w-4" />
                            <span>{formatTime(booking.startTime)}</span>
                          </div>
                          <div className="flex items-center space-x-1">
                            <Clock className="h-4 w-4" />
                            <span>{booking.durationMinutes} min</span>
                          </div>
                        </div>

                        {booking.rating && (
                          <div className="flex items-center space-x-2 mb-2">
                            <span className="text-sm text-gray-600">Rating:</span>
                            <div className="flex items-center space-x-1">
                              {renderStars(booking.rating)}
                            </div>
                          </div>
                        )}

                        {booking.studentFeedback && (
                          <div className="mb-2">
                            <p className="text-sm text-gray-600 mb-1">Your feedback:</p>
                            <p className="text-sm bg-blue-50 p-2 rounded">{booking.studentFeedback}</p>
                          </div>
                        )}

                        {booking.teacherFeedback && (
                          <div className="mb-2">
                            <p className="text-sm text-gray-600 mb-1">Teacher feedback:</p>
                            <p className="text-sm bg-green-50 p-2 rounded">{booking.teacherFeedback}</p>
                          </div>
                        )}

                        {booking.notes.length > 0 && (
                          <div className="mt-3">
                            <p className="text-sm text-gray-600 mb-2">Notes:</p>
                            <div className="space-y-2">
                              {booking.notes.map((note) => (
                                <div key={note.id} className="text-sm bg-gray-50 p-2 rounded">
                                  <div className="flex justify-between items-start">
                                    <p>{note.content}</p>
                                    <span className="text-xs text-gray-500 ml-2">
                                      - {note.authorName}
                                    </span>
                                  </div>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>

                      <div className="flex flex-col space-y-2 ml-4">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleBookmark(booking.id)}
                        >
                          {booking.bookmarked ? 'Unbookmark' : 'Bookmark'}
                        </Button>
                        
                        {booking.status === 'COMPLETED' && !booking.studentFeedback && (
                          <Button
                            size="sm"
                            onClick={() => {
                              setSelectedBooking(booking)
                              setShowFeedbackModal(true)
                            }}
                          >
                            Add Feedback
                          </Button>
                        )}
                        
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => setSelectedBooking(booking)}
                        >
                          View Details
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))
            ) : (
              <Card>
                <CardContent className="p-12 text-center">
                  <Calendar className="h-12 w-12 mx-auto mb-4 text-gray-300" />
                  <h3 className="text-lg font-medium text-gray-900 mb-2">No classes found</h3>
                  <p className="text-gray-600 mb-4">
                    {searchTerm || statusFilter !== 'all' 
                      ? 'Try adjusting your search or filters'
                      : 'You haven\'t completed any classes yet'
                    }
                  </p>
                  {!searchTerm && statusFilter === 'all' && (
                    <Button 
                      className="bg-emerald-600 hover:bg-emerald-700"
                      onClick={() => router.push('/student/booking')}
                    >
                      Book Your First Class
                    </Button>
                  )}
                </CardContent>
              </Card>
            )}
          </div>

          {/* Feedback Modal */}
          {showFeedbackModal && selectedBooking && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
              <Card className="w-full max-w-md mx-4">
                <CardHeader>
                  <CardTitle>Add Feedback</CardTitle>
                  <CardDescription>Share your experience for {selectedBooking.topicTitle}</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Rating
                    </label>
                    <div className="flex items-center space-x-1">
                      {Array.from({ length: 5 }, (_, i) => (
                        <button
                          key={i}
                          onClick={() => setRating(i + 1)}
                          className="focus:outline-none"
                        >
                          <Star
                            className={`h-6 w-6 ${
                              i < rating ? 'text-yellow-400 fill-current' : 'text-gray-300'
                            }`}
                          />
                        </button>
                      ))}
                    </div>
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Feedback
                    </label>
                    <Textarea
                      placeholder="Share your thoughts about the class..."
                      value={feedbackText}
                      onChange={(e) => setFeedbackText(e.target.value)}
                      rows={4}
                    />
                  </div>
                  
                  <div className="flex space-x-2 pt-4">
                    <Button
                      onClick={handleAddFeedback}
                      disabled={!feedbackText.trim() || rating === 0}
                      className="bg-emerald-600 hover:bg-emerald-700"
                    >
                      Submit Feedback
                    </Button>
                    <Button
                      variant="outline"
                      onClick={() => {
                        setShowFeedbackModal(false)
                        setSelectedBooking(null)
                        setFeedbackText('')
                        setRating(0)
                      }}
                    >
                      Cancel
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </div>
          )}
        </div>
      </div>
    </StudentRoute>
  )
}
