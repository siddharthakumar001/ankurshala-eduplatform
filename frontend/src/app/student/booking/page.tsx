'use client'

import { useState, useEffect, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { StudentRoute } from '@/components/route-guard'
import { contentService, SubjectDropdown, ChapterDropdown, TopicDropdown, TopicDetail } from '@/services/contentService'
import { bookingService, BookingQuote } from '@/services/bookingService'
import { studentAPI } from '@/lib/apiClient'
import { toast } from 'sonner'
import { 
  BookOpen, 
  Calendar, 
  Clock, 
  DollarSign,
  CheckCircle,
  AlertCircle,
  Loader2,
  ArrowLeft,
  ArrowRight
} from 'lucide-react'

// Main export wrapped in Suspense
export default function StudentBookingPage() {
  return (
    <Suspense fallback={
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-ankur-primary" />
      </div>
    }>
      <BookingContent />
    </Suspense>
  )
}

function BookingContent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [step, setStep] = useState(1)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  
  // Form data
  const [selectedSubjectId, setSelectedSubjectId] = useState<number | null>(null)
  const [selectedChapterId, setSelectedChapterId] = useState<number | null>(null)
  const [selectedTopicId, setSelectedTopicId] = useState<number | null>(null)
  const [selectedTopic, setSelectedTopic] = useState<TopicDetail | null>(null)
  const [selectedDate, setSelectedDate] = useState<string>('')
  const [selectedTime, setSelectedTime] = useState<string>('')
  const [selectedTeacherId, setSelectedTeacherId] = useState<number | null>(null)
  const [notes, setNotes] = useState<string>('')
  
  // API data
  const [subjects, setSubjects] = useState<SubjectDropdown[]>([])
  const [chapters, setChapters] = useState<ChapterDropdown[]>([])
  const [topics, setTopics] = useState<TopicDropdown[]>([])
  const [bookingQuote, setBookingQuote] = useState<BookingQuote | null>(null)
  const [availableTeachers, setAvailableTeachers] = useState<any[]>([])
  const [studentGradeId, setStudentGradeId] = useState<number | null>(null)

  // Load student profile to get gradeId
  useEffect(() => {
    const loadStudentProfile = async () => {
      try {
        const profile = await studentAPI.getProfile()
        if (profile.gradeId) {
          setStudentGradeId(profile.gradeId)
        }
      } catch (error) {
        console.error('Failed to load student profile:', error)
      }
    }
    loadStudentProfile()
  }, [])

  // Pre-fill from URL params (from discover page)
  useEffect(() => {
    const topicId = searchParams.get('topicId')
    if (topicId) {
      loadTopicAndPath(parseInt(topicId))
    }
  }, [searchParams])

  // Load subjects when gradeId is available
  useEffect(() => {
    if (studentGradeId) {
      loadSubjects(studentGradeId)
    }
  }, [studentGradeId])

  const loadSubjects = async (gradeId: number) => {
    try {
      const subjectsList = await contentService.getSubjectsByGrade(gradeId)
      setSubjects(subjectsList)
    } catch (error) {
      console.error('Failed to load subjects:', error)
      toast.error('Failed to load subjects')
    }
  }

  const loadTopicAndPath = async (topicId: number) => {
    try {
      setIsLoading(true)
      const topicDetail = await contentService.getTopicById(topicId)
      const path = await contentService.getTopicFullPath(topicId)
      
      setSelectedSubjectId(path.subject.id)
      setSelectedChapterId(path.chapter.id)
      setSelectedTopicId(topicId)
      setSelectedTopic(topicDetail)
      
      // Load related data
      await loadChapters(path.subject.id)
      await loadTopics(path.chapter.id)
    } catch (error: any) {
      toast.error('Failed to load topic details')
      console.error(error)
    } finally {
      setIsLoading(false)
    }
  }

  const loadChapters = async (subjectId: number) => {
    try {
      const chapterList = await contentService.getChaptersBySubject(subjectId)
      setChapters(chapterList)
    } catch (error) {
      console.error('Failed to load chapters:', error)
    }
  }

  const loadTopics = async (chapterId: number) => {
    try {
      const topicList = await contentService.getTopicsByChapter(chapterId)
      setTopics(topicList)
    } catch (error) {
      console.error('Failed to load topics:', error)
    }
  }

  const handleChapterChange = async (chapterId: string) => {
    const id = parseInt(chapterId)
    setSelectedChapterId(id)
    setSelectedTopicId(null)
    setSelectedTopic(null)
    await loadTopics(id)
  }

  const handleTopicChange = async (topicId: string) => {
    const id = parseInt(topicId)
    setSelectedTopicId(id)
    try {
      const topicDetail = await contentService.getTopicById(id)
      setSelectedTopic(topicDetail)
    } catch (error) {
      toast.error('Failed to load topic details')
    }
  }

  const handleGetQuote = async () => {
    if (!selectedTopicId || !selectedDate || !selectedTime || !selectedTeacherId) {
      setError('Please fill all required fields')
      return
    }

    setIsLoading(true)
    setError(null)

    try {
      const quote = await bookingService.getQuote({
        topicId: selectedTopicId,
        date: selectedDate,
        startTime: selectedTime,
        teacherId: selectedTeacherId
      })

      if (!quote.bufferOk) {
        toast.warning('Warning: This booking is too close to your previous class')
      }

      setBookingQuote(quote)
      setStep(2)
    } catch (err: any) {
      const message = err.response?.data?.message || 'Failed to get booking quote'
      setError(message)
      toast.error(message)
    } finally {
      setIsLoading(false)
    }
  }

  const handleCreateBooking = async () => {
    if (!selectedTopicId || !selectedDate || !selectedTime || !selectedTeacherId) {
      setError('Missing required booking information')
      return
    }

    setIsLoading(true)
    setError(null)

    try {
      const booking = await bookingService.createBooking({
        topicId: selectedTopicId,
        date: selectedDate,
        startTime: selectedTime,
        teacherId: selectedTeacherId,
        notes: notes || undefined
      })

      toast.success('Booking request submitted successfully!')
      setStep(3)
      
      // Redirect to calendar after 2 seconds
      setTimeout(() => {
        router.push('/student/calendar')
      }, 2000)
    } catch (err: any) {
      const message = err.response?.data?.message || 'Failed to create booking'
      setError(message)
      toast.error(message)
    } finally {
      setIsLoading(false)
    }
  }

  // Generate time slots (9 AM to 8 PM, 30-min intervals)
  const generateTimeSlots = () => {
    const slots = []
    for (let hour = 9; hour <= 20; hour++) {
      for (let min = 0; min < 60; min += 30) {
        const time = `${hour.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}`
        slots.push(time)
      }
    }
    return slots
  }

  const timeSlots = generateTimeSlots()

  // Get minimum date (today)
  const today = new Date().toISOString().split('T')[0]

  return (
    <StudentRoute>
      <div className="container mx-auto py-8 px-4 max-w-4xl">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Book a Class</h1>
            <p className="text-gray-600 dark:text-gray-400">Schedule a session with expert teachers</p>
          </div>
          <Badge variant="outline" className="text-blue-600 border-blue-300 dark:bg-blue-900 dark:text-blue-200">
            Step {step} of 3
          </Badge>
        </div>

        {error && (
          <Alert variant="destructive" className="mb-6">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {/* Step 1: Topic & Schedule Selection */}
        {step === 1 && (
          <Card className="dark:bg-gray-800">
            <CardHeader>
              <CardTitle className="flex items-center dark:text-white">
                <BookOpen className="h-5 w-5 mr-2 text-blue-500" />
                Select Topic & Schedule
              </CardTitle>
              <CardDescription className="dark:text-gray-400">
                Choose what you want to learn and when
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Topic Selection */}
              {selectedTopic ? (
                <div className="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                  <div className="flex items-start justify-between">
                    <div>
                      <h3 className="font-semibold text-lg dark:text-white">{selectedTopic.title}</h3>
                      <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                        {selectedTopic.subjectName} • {selectedTopic.chapterName}
                      </p>
                      {selectedTopic.summary && (
                        <p className="text-sm text-gray-700 dark:text-gray-300 mt-2">{selectedTopic.summary}</p>
                      )}
                      <div className="flex items-center mt-2 text-sm text-gray-600 dark:text-gray-400">
                        <Clock className="h-4 w-4 mr-1" />
                        {selectedTopic.expectedMinutes} minutes
                      </div>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => router.push('/student/discover')}
                      className="dark:text-gray-300"
                    >
                      Change
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="text-center p-8 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg">
                  <BookOpen className="h-12 w-12 mx-auto text-gray-400 mb-3" />
                  <p className="text-gray-600 dark:text-gray-400 mb-4">No topic selected</p>
                  <Button onClick={() => router.push('/student/discover')} className="dark:bg-blue-600 dark:hover:bg-blue-700">
                    Browse Topics
                  </Button>
                </div>
              )}

              {/* Manual Topic Selection (if not from discover) */}
              {!selectedTopic && chapters.length > 0 && (
                <>
                  <div>
                    <Label htmlFor="chapter" className="dark:text-gray-300">Chapter *</Label>
                    <Select onValueChange={handleChapterChange} value={selectedChapterId?.toString()}>
                      <SelectTrigger className="dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                        <SelectValue placeholder="Select a chapter" />
                      </SelectTrigger>
                      <SelectContent className="dark:bg-gray-700">
                        {chapters.map((chapter) => (
                          <SelectItem key={chapter.id} value={chapter.id.toString()}>
                            {chapter.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  {selectedChapterId && topics.length > 0 && (
                    <div>
                      <Label htmlFor="topic" className="dark:text-gray-300">Topic *</Label>
                      <Select onValueChange={handleTopicChange} value={selectedTopicId?.toString()}>
                        <SelectTrigger className="dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                          <SelectValue placeholder="Select a topic" />
                        </SelectTrigger>
                        <SelectContent className="dark:bg-gray-700">
                          {topics.map((topic) => (
                            <SelectItem key={topic.id} value={topic.id.toString()}>
                              {topic.title}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                  )}
                </>
              )}

              {/* Date & Time Selection */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="date" className="dark:text-gray-300">
                    <Calendar className="inline h-4 w-4 mr-1" />
                    Date *
                  </Label>
                  <input
                    type="date"
                    id="date"
                    min={today}
                    value={selectedDate}
                    onChange={(e) => setSelectedDate(e.target.value)}
                    className="w-full px-3 py-2 border rounded-md dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                    required
                  />
                </div>

                <div>
                  <Label htmlFor="time" className="dark:text-gray-300">
                    <Clock className="inline h-4 w-4 mr-1" />
                    Start Time *
                  </Label>
                  <Select onValueChange={setSelectedTime} value={selectedTime}>
                    <SelectTrigger className="dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                      <SelectValue placeholder="Select time" />
                    </SelectTrigger>
                    <SelectContent className="dark:bg-gray-700 max-h-60">
                      {timeSlots.map((slot) => (
                        <SelectItem key={slot} value={slot}>
                          {slot}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              {/* Teacher ID Selection (temporary - will be replaced with teacher search) */}
              <div>
                <Label htmlFor="teacherId" className="dark:text-gray-300">Teacher ID *</Label>
                <input
                  type="number"
                  id="teacherId"
                  value={selectedTeacherId || ''}
                  onChange={(e) => setSelectedTeacherId(parseInt(e.target.value))}
                  placeholder="Enter teacher ID"
                  className="w-full px-3 py-2 border rounded-md dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  required
                />
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  Temporary: Use teacher ID from teacher search feature
                </p>
              </div>

              {/* Notes */}
              <div>
                <Label htmlFor="notes" className="dark:text-gray-300">Notes (Optional)</Label>
                <Textarea
                  id="notes"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="Any special requirements or questions for the teacher..."
                  className="dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  rows={3}
                />
              </div>

              <div className="flex justify-end gap-3 pt-4">
                <Button
                  onClick={handleGetQuote}
                  disabled={!selectedTopicId || !selectedDate || !selectedTime || !selectedTeacherId || isLoading}
                  className="dark:bg-blue-600 dark:hover:bg-blue-700"
                >
                  {isLoading ? (
                    <>
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      Getting Quote...
                    </>
                  ) : (
                    <>
                      Get Quote
                      <ArrowRight className="h-4 w-4 ml-2" />
                    </>
                  )}
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Step 2: Review & Confirm */}
        {step === 2 && bookingQuote && selectedTopic && (
          <Card className="dark:bg-gray-800">
            <CardHeader>
              <CardTitle className="flex items-center dark:text-white">
                <CheckCircle className="h-5 w-5 mr-2 text-green-500" />
                Review Booking Details
              </CardTitle>
              <CardDescription className="dark:text-gray-400">
                Please review your booking before confirming
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Booking Summary */}
              <div className="space-y-4">
                <div className="flex items-start justify-between p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                  <div>
                    <h3 className="font-semibold text-lg dark:text-white">{selectedTopic.title}</h3>
                    <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                      {selectedTopic.subjectName} • {selectedTopic.chapterName}
                    </p>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="flex items-center text-gray-700 dark:text-gray-300">
                    <Calendar className="h-4 w-4 mr-2" />
                    <span>{new Date(selectedDate).toLocaleDateString()}</span>
                  </div>
                  <div className="flex items-center text-gray-700 dark:text-gray-300">
                    <Clock className="h-4 w-4 mr-2" />
                    <span>{selectedTime} - {new Date(bookingQuote.endTimeISO).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                  </div>
                </div>

                <div className="flex items-center justify-between p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                  <div className="flex items-center text-gray-700 dark:text-gray-300">
                    <DollarSign className="h-5 w-5 mr-2 text-blue-500" />
                    <span className="font-medium">Estimated Price</span>
                  </div>
                  <span className="text-xl font-bold text-blue-600 dark:text-blue-400">
                    {bookingQuote.price.currency} {bookingQuote.price.min} - {bookingQuote.price.max}
                  </span>
                </div>

                {notes && (
                  <div className="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                    <p className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Your Notes:</p>
                    <p className="text-sm text-gray-600 dark:text-gray-400">{notes}</p>
                  </div>
                )}
              </div>

              <div className="flex justify-between gap-3 pt-4">
                <Button
                  variant="outline"
                  onClick={() => setStep(1)}
                  disabled={isLoading}
                  className="dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                >
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Back
                </Button>
                <Button
                  onClick={handleCreateBooking}
                  disabled={isLoading}
                  className="dark:bg-blue-600 dark:hover:bg-blue-700"
                >
                  {isLoading ? (
                    <>
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      Creating Booking...
                    </>
                  ) : (
                    <>
                      Confirm Booking
                      <CheckCircle className="h-4 w-4 ml-2" />
                    </>
                  )}
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Step 3: Success */}
        {step === 3 && (
          <Card className="dark:bg-gray-800">
            <CardContent className="text-center py-12">
              <CheckCircle className="h-16 w-16 mx-auto text-green-500 mb-4" />
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">Booking Request Submitted!</h2>
              <p className="text-gray-600 dark:text-gray-400 mb-6">
                Your booking request has been sent to the teacher. You'll be notified once it's confirmed.
              </p>
              <div className="flex justify-center gap-4">
                <Button
                  variant="outline"
                  onClick={() => router.push('/student/dashboard')}
                  className="dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                >
                  Go to Dashboard
                </Button>
                <Button
                  onClick={() => router.push('/student/calendar')}
                  className="dark:bg-blue-600 dark:hover:bg-blue-700"
                >
                  View Calendar
                </Button>
              </div>
            </CardContent>
          </Card>
        )}
      </div>
    </StudentRoute>
  )
}
