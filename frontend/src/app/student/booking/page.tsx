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
import { contentService, SubjectDropdown, TopicDropdown, TopicDetail } from '@/services/contentService'
import { bookingService, BookingQuote, AvailableSlot } from '@/services/bookingService'
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

interface TopicOption extends TopicDropdown {
  chapterName?: string
}

// Two-component pattern: StudentRoute wraps inner content to ensure auth before API calls
export default function StudentBookingPage() {
  return (
    <StudentRoute>
      <Suspense fallback={
        <div className="flex items-center justify-center min-h-[400px]">
          <Loader2 className="h-8 w-8 animate-spin text-ankur-primary" />
        </div>
      }>
        <BookingContent />
      </Suspense>
    </StudentRoute>
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
  const [selectedTopicId, setSelectedTopicId] = useState<number | null>(null)
  const [selectedTopic, setSelectedTopic] = useState<TopicDetail | null>(null)
  const [selectedDate, setSelectedDate] = useState<string>('')
  const [selectedTime, setSelectedTime] = useState<string>('')
  const [notes, setNotes] = useState<string>('')
  const [durationMinutes, setDurationMinutes] = useState<number>(60)
  
  // API data
  const [subjects, setSubjects] = useState<SubjectDropdown[]>([])
  const [topics, setTopics] = useState<TopicOption[]>([])
  const [bookingQuote, setBookingQuote] = useState<BookingQuote | null>(null)
  const [studentGradeId, setStudentGradeId] = useState<number | null>(null)
  const [subjectsLoading, setSubjectsLoading] = useState(false)
  const [topicsLoading, setTopicsLoading] = useState(false)
  const [subjectsNotice, setSubjectsNotice] = useState('')
  const [topicsNotice, setTopicsNotice] = useState('')
  const [availabilityNotice, setAvailabilityNotice] = useState<string | null>(null)
  const [availabilitySlots, setAvailabilitySlots] = useState<AvailableSlot[]>([])
  const [availabilityLoading, setAvailabilityLoading] = useState(false)

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

  useEffect(() => {
    if (!selectedTopic) return
    const expectedMinutes = selectedTopic.expectedMinutes || 60
    setDurationMinutes(Math.min(expectedMinutes, 60))
  }, [selectedTopic])

  useEffect(() => {
    setAvailabilityNotice(null)
    setAvailabilitySlots([])
  }, [selectedTopicId, selectedDate, selectedTime])

  const loadNextAvailableSlots = async () => {
    if (!selectedDate || !selectedTime || !durationMinutes) {
      return
    }

    try {
      setAvailabilityLoading(true)
      const slots = await bookingService.getNextAvailableSlots({
        startTime: buildStartTime(),
        durationMinutes,
        timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
        limit: 3
      })
      setAvailabilitySlots(slots)
    } catch (error) {
      console.error('Failed to load next available slots:', error)
      setAvailabilitySlots([])
    } finally {
      setAvailabilityLoading(false)
    }
  }

  const loadSubjects = async (gradeId: number) => {
    try {
      setSubjectsLoading(true)
      setSubjectsNotice('')
      const subjectsList = await contentService.getSubjectsByGrade(gradeId)
      setSubjects(subjectsList)
      if (subjectsList.length === 0) {
        setSubjectsNotice('Content for your grade is coming soon.')
      }
    } catch (error) {
      console.error('Failed to load subjects:', error)
      setSubjects([])
      setSubjectsNotice('Content for your grade is coming soon.')
    } finally {
      setSubjectsLoading(false)
    }
  }

  const loadTopicAndPath = async (topicId: number) => {
    try {
      setIsLoading(true)
      const topicDetail = await contentService.getTopicById(topicId)
      const path = await contentService.getTopicFullPath(topicId)
      
      setSelectedSubjectId(path.subject.id)
      setSelectedTopicId(topicId)
      setSelectedTopic(topicDetail)
      
      // Load related data
      await loadTopicsForSubject(path.subject.id)
    } catch (error: any) {
      toast.error('Failed to load topic details')
      console.error(error)
    } finally {
      setIsLoading(false)
    }
  }

  const loadTopicsForSubject = async (subjectId: number) => {
    try {
      setTopicsLoading(true)
      setTopicsNotice('')
      const chapterList = await contentService.getChaptersBySubject(subjectId)

      if (chapterList.length === 0) {
        setTopics([])
        setTopicsNotice('Topics for this subject are coming soon.')
        return
      }

      const topicGroups = await Promise.all(
        chapterList.map(async (chapter) => {
          const chapterTopics = await contentService.getTopicsByChapter(chapter.id)
          return chapterTopics.map((topic) => ({
            ...topic,
            chapterName: chapter.name
          }))
        })
      )

      const flattened = topicGroups.flat()
      setTopics(flattened)
      if (flattened.length === 0) {
        setTopicsNotice('Topics for this subject are coming soon.')
      }
    } catch (error) {
      console.error('Failed to load topics:', error)
      setTopics([])
      setTopicsNotice('Topics for this subject are coming soon.')
    } finally {
      setTopicsLoading(false)
    }
  }

  const handleSubjectChange = async (subjectId: string) => {
    const id = parseInt(subjectId)
    setSelectedSubjectId(id)
    setSelectedTopicId(null)
    setSelectedTopic(null)
    setBookingQuote(null)
    setStep(1)
    await loadTopicsForSubject(id)
  }

  const handleTopicChange = async (topicId: string) => {
    const id = parseInt(topicId)
    setSelectedTopicId(id)
    try {
      const topicDetail = await contentService.getTopicById(id)
      setSelectedTopic(topicDetail)
      setBookingQuote(null)
      setStep(1)
    } catch (error) {
      toast.error('Failed to load topic details')
    }
  }

  const buildStartTime = () => `${selectedDate}T${selectedTime}:00`

  const handleGetQuote = async () => {
    if (!selectedTopicId || !selectedDate || !selectedTime || !durationMinutes) {
      setError('Please fill all required fields')
      return
    }

    setIsLoading(true)
    setError(null)
    setAvailabilityNotice(null)
    setAvailabilitySlots([])

    try {
      const quote = await bookingService.getQuote({
        topicId: selectedTopicId,
        startTime: buildStartTime(),
        durationMinutes,
        timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
      })

      if (!quote.bufferOk) {
        toast.warning('Warning: This booking is too close to your previous class')
      }

      setBookingQuote(quote)
      setAvailabilityNotice(null)
      setStep(2)
    } catch (err: any) {
      const message = err.response?.data?.message || 'Failed to calculate price range'
      setError(message)
      if (message.toLowerCase().includes('no teachers')) {
        setAvailabilityNotice(message)
        loadNextAvailableSlots()
      }
      toast.error(message)
    } finally {
      setIsLoading(false)
    }
  }

  const handleCreateBooking = async () => {
    if (!selectedTopicId || !selectedDate || !selectedTime || !durationMinutes) {
      setError('Missing required booking information')
      return
    }

    setIsLoading(true)
    setError(null)
    setAvailabilityNotice(null)
    setAvailabilitySlots([])

    try {
      await bookingService.createBooking({
        topicId: selectedTopicId,
        startTime: buildStartTime(),
        durationMinutes,
        studentNotes: notes || undefined,
        timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
      })

      toast.success('Booking request submitted successfully!')
      setAvailabilityNotice(null)
      setStep(3)
    } catch (err: any) {
      const message = err.response?.data?.message || 'Failed to create booking'
      setError(message)
      if (message.toLowerCase().includes('no teachers')) {
        setAvailabilityNotice(message)
        loadNextAvailableSlots()
      }
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
    <div className="space-y-6">
        <div className="page-header flex items-center justify-between">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-white">Book a Class</h1>
            <p className="text-sm text-white/80 mt-1">Choose a topic, pick a time, and we will match a teacher</p>
          </div>
          <Badge variant="outline" className="border-white/40 text-white">
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
          <Card className="glass-panel border border-white/30 dark:border-white/10">
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
                <div className="p-4 bg-blue-50/70 dark:bg-blue-900/20 rounded-lg border border-blue-100/60 dark:border-blue-900/40">
                  <div className="flex items-start justify-between">
                    <div>
                      <h3 className="font-semibold text-lg dark:text-white">{selectedTopic.title}</h3>
                      <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                        {selectedTopic.subjectName} - {selectedTopic.chapterName}
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
                <div className="text-center p-8 border-2 border-dashed border-white/40 dark:border-white/10 rounded-lg">
                  <BookOpen className="h-12 w-12 mx-auto text-gray-400 mb-3" />
                  <p className="text-gray-600 dark:text-gray-400 mb-4">No topic selected</p>
                  <Button onClick={() => router.push('/student/discover')} className="btn-primary">
                    Browse Topics
                  </Button>
                </div>
              )}

              {/* Manual Topic Selection (if not from discover) */}
              {!selectedTopic && (
                <>
                  <div>
                    <Label htmlFor="subject" className="dark:text-gray-300">Subject *</Label>
                    <Select onValueChange={handleSubjectChange} value={selectedSubjectId?.toString() || ''}>
                      <SelectTrigger className="dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                        <SelectValue placeholder={subjectsLoading ? 'Loading subjects...' : 'Select a subject'} />
                      </SelectTrigger>
                      <SelectContent className="dark:bg-gray-700">
                        {subjects.map((subject) => (
                          <SelectItem key={subject.id} value={subject.id.toString()}>
                            {subject.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {!subjectsLoading && subjects.length === 0 && (
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                        {subjectsNotice || 'Content for your grade is coming soon.'}
                      </p>
                    )}
                  </div>

                  <div>
                    <Label htmlFor="topic" className="dark:text-gray-300">Topic *</Label>
                    <Select
                      onValueChange={handleTopicChange}
                      value={selectedTopicId?.toString() || ''}
                      disabled={!selectedSubjectId || topicsLoading || topics.length === 0}
                    >
                      <SelectTrigger className="dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                        <SelectValue placeholder={topicsLoading ? 'Loading topics...' : 'Select a topic'} />
                      </SelectTrigger>
                      <SelectContent className="dark:bg-gray-700">
                        {topics.map((topic) => (
                          <SelectItem key={topic.id} value={topic.id.toString()}>
                            {topic.title}{topic.chapterName ? ` - ${topic.chapterName}` : ''}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {!topicsLoading && selectedSubjectId && topics.length === 0 && (
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                        {topicsNotice || 'Topics for this subject are coming soon.'}
                      </p>
                    )}
                  </div>
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
                    className="w-full px-3 py-2 border border-white/40 rounded-md bg-white/70 dark:bg-slate-900/70 dark:border-white/10 dark:text-white"
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

              <div>
                <Label htmlFor="duration" className="dark:text-gray-300">Session Duration *</Label>
                <input
                  id="duration"
                  value={selectedTopic ? `${durationMinutes} minutes` : 'Select a topic to see duration'}
                  readOnly
                  className="w-full px-3 py-2 border rounded-md bg-white/70 dark:bg-slate-900/70 dark:border-white/10 dark:text-white"
                />
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  Duration is set by the topic and capped at 60 minutes.
                </p>
              </div>

              {availabilityNotice && (
                <div className="rounded-lg border border-amber-200/70 bg-amber-50/70 px-4 py-3 text-amber-900 dark:border-amber-900/40 dark:bg-amber-900/20 dark:text-amber-200">
                  <div className="flex items-start gap-3">
                    <AlertCircle className="h-5 w-5 mt-0.5" />
                    <div>
                      <p className="font-medium">No teachers available for this slot</p>
                      <p className="text-sm opacity-90">{availabilityNotice}</p>
                      <p className="text-xs mt-1 opacity-80">Try another date or time to continue.</p>
                    </div>
                  </div>
                  <div className="mt-3 rounded-md bg-white/70 dark:bg-slate-900/60 p-3">
                    <p className="text-xs font-semibold uppercase tracking-wide text-amber-700 dark:text-amber-200">
                      Next available slots
                    </p>
                    {availabilityLoading ? (
                      <div className="flex items-center gap-2 text-xs text-amber-700 dark:text-amber-200 mt-2">
                        <Loader2 className="h-3 w-3 animate-spin" />
                        Checking upcoming times...
                      </div>
                    ) : availabilitySlots.length > 0 ? (
                      <div className="mt-2 space-y-1 text-sm">
                        {availabilitySlots.map((slot, index) => {
                          const start = new Date(slot.startTime)
                          const end = new Date(slot.endTime)
                          return (
                            <div key={`${slot.startTime}-${index}`} className="flex items-center justify-between text-amber-900 dark:text-amber-100">
                              <span>{start.toLocaleDateString()} · {start.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                              <span className="text-xs opacity-70">to {end.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                            </div>
                          )
                        })}
                      </div>
                    ) : (
                      <p className="mt-2 text-xs text-amber-700 dark:text-amber-200">
                        No upcoming slots found yet. Please try again later.
                      </p>
                    )}
                  </div>
                </div>
              )}

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
                  disabled={!selectedTopicId || !selectedDate || !selectedTime || !durationMinutes || isLoading}
                  className="btn-primary"
                >
                  {isLoading ? (
                    <>
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      Calculating Price...
                    </>
                  ) : (
                    <>
                      See Price Range
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
          <Card className="glass-panel border border-white/30 dark:border-white/10">
            <CardHeader>
              <CardTitle className="flex items-center dark:text-white">
                <CheckCircle className="h-5 w-5 mr-2 text-green-500" />
                Review & Confirm
              </CardTitle>
              <CardDescription className="dark:text-gray-400">
                Confirm to notify teachers and lock your slot
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Booking Summary */}
              <div className="space-y-4">
                <div className="flex items-start justify-between p-4 bg-white/70 dark:bg-slate-900/70 rounded-lg border border-white/30 dark:border-white/10">
                  <div>
                    <h3 className="font-semibold text-lg dark:text-white">{selectedTopic.title}</h3>
                    <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                      {selectedTopic.subjectName} - {selectedTopic.chapterName}
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
                    <span>{selectedTime} - {new Date(bookingQuote.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                  </div>
                </div>

                <div className="flex items-center justify-between p-4 bg-blue-50/70 dark:bg-blue-900/20 rounded-lg border border-blue-100/60 dark:border-blue-900/40">
                  <div className="flex items-center text-gray-700 dark:text-gray-300">
                    <DollarSign className="h-5 w-5 mr-2 text-blue-500" />
                    <span className="font-medium">Estimated Price Range</span>
                  </div>
                  <span className="text-xl font-bold text-blue-600 dark:text-blue-400">
                    {bookingQuote.price.currency} {bookingQuote.price.min} - {bookingQuote.price.max}
                  </span>
                </div>
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  Pricing adjusts with topic complexity, teacher experience, and availability.
                </p>

                {notes && (
                  <div className="p-4 bg-white/70 dark:bg-slate-900/70 rounded-lg border border-white/30 dark:border-white/10">
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
                  className="border-white/40 hover:bg-white/60 dark:hover:bg-slate-900/60"
                >
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Back
                </Button>
                <Button
                  onClick={handleCreateBooking}
                  disabled={isLoading}
                  className="btn-primary"
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
          <Card className="glass-panel border border-white/30 dark:border-white/10">
            <CardContent className="text-center py-12">
              <CheckCircle className="h-16 w-16 mx-auto text-green-500 mb-4" />
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">Booking Request Submitted!</h2>
              <p className="text-gray-600 dark:text-gray-400 mb-6">
                We have notified available teachers. The first to accept will take your class, and you will see the teacher details instantly.
              </p>
              <p className="text-sm text-gray-500 dark:text-gray-400 mb-6">
                If no teacher accepts this slot, we will ask you to pick another time.
              </p>
              <div className="flex justify-center gap-4">
                <Button
                  variant="outline"
                  onClick={() => router.push('/student/dashboard')}
                  className="border-white/40 hover:bg-white/60 dark:hover:bg-slate-900/60"
                >
                  Go to Dashboard
                </Button>
                <Button
                  onClick={() => router.push('/student/calendar')}
                  className="btn-primary"
                >
                  View Calendar
                </Button>
              </div>
            </CardContent>
          </Card>
        )}
      </div>
  )
}
