'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { 
  Play, 
  Square, 
  MessageSquare, 
  Star, 
  Clock, 
  BookOpen,
  CheckCircle,
  Loader2,
  ThumbsUp,
  ThumbsDown,
  DollarSign
} from 'lucide-react'
import { toast } from 'sonner'
import { api } from '@/utils/api'
import { TeacherRoute } from '@/components/route-guard'

interface SessionBooking {
  id: number
  studentId: number
  studentName: string
  topicId: number
  topicTitle: string
  subjectName?: string
  startTime: string
  endTime: string
  durationMinutes: number
  status: string
  priceMin: number
  priceMax: number
  priceCurrency?: string
  studentNotes?: string
}

interface SessionFeedback {
  bookingId: number
  sessionRating: number
  studentEngagement: string
  sessionNotes: string
  improvementSuggestions: string
  wouldRecommend: boolean
}

export default function TeacherSessionManagementPage() {
  const [loading, setLoading] = useState(true)
  const [sessions, setSessions] = useState<SessionBooking[]>([])
  const [activeSession, setActiveSession] = useState<SessionBooking | null>(null)
  const [sessionNotes, setSessionNotes] = useState('')
  const [showFeedbackDialog, setShowFeedbackDialog] = useState<number | null>(null)
  const [feedback, setFeedback] = useState<SessionFeedback>({
    bookingId: 0,
    sessionRating: 5,
    studentEngagement: '',
    sessionNotes: '',
    improvementSuggestions: '',
    wouldRecommend: true
  })

  useEffect(() => {
    fetchSessions()
  }, [])

  const fetchSessions = async () => {
    try {
      setLoading(true)
      
      const response = await api.get<SessionBooking[]>('/teacher/bookings')
      const allBookings = response.data || []
      
      // Filter for sessions that can be managed (accepted, in progress, completed)
      const sessionBookings = allBookings.filter((booking) => 
        booking.status === 'ACCEPTED' || booking.status === 'IN_PROGRESS' || booking.status === 'COMPLETED'
      )
      
      setSessions(sessionBookings)
      const inProgress = sessionBookings.find((booking) => booking.status === 'IN_PROGRESS')
      setActiveSession(inProgress || null)
      
    } catch (error) {
      console.error('Error fetching sessions:', error)
      toast.error('Failed to load sessions')
    } finally {
      setLoading(false)
    }
  }

  const handleStartSession = async (bookingId: number) => {
    try {
      await api.post(`/teacher/sessions/${bookingId}/start`)
      toast.success('Session started successfully')
      setActiveSession(sessions.find(s => s.id === bookingId) || null)
      fetchSessions() // Refresh data
    } catch (error) {
      console.error('Error starting session:', error)
      toast.error('Failed to start session')
    }
  }

  const handleEndSession = async (bookingId: number) => {
    try {
      await api.post(`/teacher/sessions/${bookingId}/end`, {
        sessionNotes
      })
      toast.success('Session ended successfully')
      setActiveSession(null)
      setSessionNotes('')
      fetchSessions() // Refresh data
    } catch (error) {
      console.error('Error ending session:', error)
      toast.error('Failed to end session')
    }
  }

  const handleSubmitFeedback = async () => {
    try {
      await api.post('/teacher/sessions/feedback', feedback)
      toast.success('Session feedback submitted successfully')
      setShowFeedbackDialog(null)
      setFeedback({
        bookingId: 0,
        sessionRating: 5,
        studentEngagement: '',
        sessionNotes: '',
        improvementSuggestions: '',
        wouldRecommend: true
      })
      fetchSessions() // Refresh data
    } catch (error) {
      console.error('Error submitting feedback:', error)
      toast.error('Failed to submit feedback')
    }
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

  const formatCurrency = (amount: number | string | null | undefined, currency: string = 'INR') => {
    const safeAmount = typeof amount === 'number' ? amount : Number(amount || 0)
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency
    }).format(safeAmount || 0)
  }

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACCEPTED':
        return <Badge className="badge-warning">Ready to Start</Badge>
      case 'IN_PROGRESS':
        return <Badge className="badge-info">In Progress</Badge>
      case 'COMPLETED':
        return <Badge className="badge-success">Completed</Badge>
      default:
        return <Badge variant="outline" className="border-white/40 bg-white/70 text-slate-700">{status}</Badge>
    }
  }

  const canStartSession = (session: SessionBooking) => {
    const now = new Date()
    const startTime = new Date(session.startTime)
    const timeDiff = startTime.getTime() - now.getTime()
    const minutesDiff = timeDiff / (1000 * 60)
    
    // Can start 15 minutes before or after the scheduled time
    return minutesDiff >= -15 && minutesDiff <= 15 && session.status === 'ACCEPTED'
  }

  if (loading) {
    return (
      <TeacherRoute>
        <div className="space-y-6">
          <div className="glass-panel p-6 flex items-center justify-center">
            <Loader2 className="h-6 w-6 animate-spin text-emerald-500" />
            <span className="ml-2 text-slate-600 dark:text-slate-200">Loading sessions...</span>
          </div>
        </div>
      </TeacherRoute>
    )
  }

  return (
    <TeacherRoute>
      <div className="space-y-6">
        <div className="page-header">
          <h1 className="text-2xl md:text-3xl font-bold text-white">Session Management</h1>
          <p className="text-white/80">Start, manage, and provide feedback for your teaching sessions</p>
        </div>

          {/* Active Session Alert */}
          {activeSession && (
            <Card className="glass-panel border border-emerald-200/40">
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-3 h-3 bg-emerald-500 rounded-full animate-pulse"></div>
                    <div>
                      <h3 className="font-medium text-slate-900 dark:text-white">Session in Progress</h3>
                      <p className="text-sm text-slate-600 dark:text-slate-300">
                        Teaching {activeSession.studentName} - {activeSession.topicTitle}
                      </p>
                    </div>
                  </div>
                  <Button
                    onClick={() => handleEndSession(activeSession.id)}
                    variant="outline"
                    className="btn-outline text-red-600 border-red-200 hover:border-red-300 hover:text-red-700"
                  >
                    <Square className="h-4 w-4 mr-2" />
                    End Session
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Session Notes for Active Session */}
          {activeSession && (
            <Card className="glass-panel border border-white/40">
              <CardHeader>
                <CardTitle>Session Notes</CardTitle>
                <CardDescription>Add notes during the session</CardDescription>
              </CardHeader>
              <CardContent>
                <Textarea
                  placeholder="Add session notes, student progress, or important observations..."
                  value={sessionNotes}
                  onChange={(e) => setSessionNotes(e.target.value)}
                  className="input-modern min-h-[120px] resize-none"
                  rows={4}
                />
              </CardContent>
            </Card>
          )}

          {/* Sessions List */}
          <div className="space-y-4">
            {sessions.length === 0 ? (
              <Card className="glass-panel border border-white/40">
                <CardContent className="text-center py-12">
                  <BookOpen className="h-12 w-12 text-slate-300 mx-auto mb-4" />
                  <h3 className="text-lg font-medium text-slate-900 dark:text-white mb-2">No sessions found</h3>
                  <p className="text-slate-500 dark:text-slate-300">No accepted or completed sessions available.</p>
                </CardContent>
              </Card>
            ) : (
              sessions.map((session) => (
                <Card key={session.id} className="glass-panel border border-white/40">
                  <CardContent className="p-6">
                    <div className="flex justify-between items-start mb-4">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          <h3 className="text-lg font-medium text-slate-900 dark:text-white">{session.studentName}</h3>
                          {getStatusBadge(session.status)}
                        </div>
                        <p className="text-slate-600 dark:text-slate-300 mb-1">
                          {session.topicTitle}{session.subjectName ? ` • ${session.subjectName}` : ''}
                        </p>
                        <div className="flex items-center gap-4 text-sm text-slate-500 dark:text-slate-300">
                          <span className="flex items-center gap-1">
                            <Clock className="h-4 w-4" />
                            {formatDateTime(session.startTime)}
                          </span>
                          <span className="flex items-center gap-1">
                            <Clock className="h-4 w-4" />
                            {session.durationMinutes} minutes
                          </span>
                          <span className="flex items-center gap-1">
                            <DollarSign className="h-4 w-4" />
                            {formatCurrency(session.priceMin, session.priceCurrency)} - {formatCurrency(session.priceMax, session.priceCurrency)}
                          </span>
                        </div>
                        {session.studentNotes && (
                          <div className="mt-3 p-3 glass rounded-lg border border-white/30">
                            <p className="text-sm text-slate-600 dark:text-slate-300">
                              <strong>Student Notes:</strong> {session.studentNotes}
                            </p>
                          </div>
                        )}
                      </div>
                      
                      <div className="flex gap-2">
                        {session.status === 'ACCEPTED' && canStartSession(session) && (
                          <Button
                            onClick={() => handleStartSession(session.id)}
                            className="btn-primary"
                          >
                            <Play className="h-4 w-4 mr-2" />
                            Start Session
                          </Button>
                        )}
                        
                        {session.status === 'ACCEPTED' && !canStartSession(session) && (
                          <div className="text-sm text-slate-500 dark:text-slate-300 text-center">
                            <p>Session can be started</p>
                            <p>15 min before/after</p>
                            <p>scheduled time</p>
                          </div>
                        )}
                        
                        {session.status === 'COMPLETED' && (
                          <Dialog open={showFeedbackDialog === session.id} onOpenChange={(open) => setShowFeedbackDialog(open ? session.id : null)}>
                            <DialogTrigger asChild>
                              <Button
                                variant="outline"
                                className="btn-outline"
                                onClick={() => setFeedback({
                                  bookingId: session.id,
                                  sessionRating: 5,
                                  studentEngagement: '',
                                  sessionNotes: '',
                                  improvementSuggestions: '',
                                  wouldRecommend: true
                                })}
                              >
                                <MessageSquare className="h-4 w-4 mr-2" />
                                Add Feedback
                              </Button>
                            </DialogTrigger>
                            <DialogContent className="modal-content max-w-2xl">
                              <DialogHeader>
                                <DialogTitle>Session Feedback</DialogTitle>
                              </DialogHeader>
                              <SessionFeedbackForm
                                session={session}
                                feedback={feedback}
                                setFeedback={setFeedback}
                                onSubmit={handleSubmitFeedback}
                                onCancel={() => setShowFeedbackDialog(null)}
                              />
                            </DialogContent>
                          </Dialog>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))
            )}
          </div>
      </div>
    </TeacherRoute>
  )
}

// Session Feedback Form Component
function SessionFeedbackForm({ 
  session, 
  feedback, 
  setFeedback, 
  onSubmit, 
  onCancel 
}: { 
  session: SessionBooking
  feedback: SessionFeedback
  setFeedback: (feedback: SessionFeedback) => void
  onSubmit: () => void
  onCancel: () => void
}) {
  const handleRatingChange = (rating: number) => {
    setFeedback({ ...feedback, sessionRating: rating })
  }

  const handleRecommendationChange = (recommend: boolean) => {
    setFeedback({ ...feedback, wouldRecommend: recommend })
  }

  return (
    <div className="space-y-6">
      <div>
        <Label>Session Rating</Label>
        <div className="flex items-center gap-2 mt-2">
          {[1, 2, 3, 4, 5].map((rating) => (
            <button
              key={rating}
              onClick={() => handleRatingChange(rating)}
              className={`p-2 rounded-full transition-colors ${
                feedback.sessionRating >= rating
                  ? 'text-yellow-400 bg-yellow-100/70'
                  : 'text-slate-300 hover:text-yellow-400'
              }`}
            >
              <Star className="h-6 w-6 fill-current" />
            </button>
          ))}
          <span className="ml-2 text-sm text-slate-600 dark:text-slate-300">
            {feedback.sessionRating}/5 stars
          </span>
        </div>
      </div>

      <div>
        <Label htmlFor="student-engagement">Student Engagement</Label>
        <Textarea
          id="student-engagement"
          placeholder="How was the student's engagement during the session?"
          value={feedback.studentEngagement}
          onChange={(e) => setFeedback({ ...feedback, studentEngagement: e.target.value })}
          className="input-modern min-h-[100px] resize-none"
          rows={3}
        />
      </div>

      <div>
        <Label htmlFor="session-notes">Session Notes</Label>
        <Textarea
          id="session-notes"
          placeholder="Key points covered, student progress, areas of improvement..."
          value={feedback.sessionNotes}
          onChange={(e) => setFeedback({ ...feedback, sessionNotes: e.target.value })}
          className="input-modern min-h-[120px] resize-none"
          rows={4}
        />
      </div>

      <div>
        <Label htmlFor="improvement-suggestions">Improvement Suggestions</Label>
        <Textarea
          id="improvement-suggestions"
          placeholder="Suggestions for the student's continued learning..."
          value={feedback.improvementSuggestions}
          onChange={(e) => setFeedback({ ...feedback, improvementSuggestions: e.target.value })}
          className="input-modern min-h-[100px] resize-none"
          rows={3}
        />
      </div>

      <div>
        <Label>Would you recommend this student for future sessions?</Label>
        <div className="flex items-center gap-4 mt-2">
          <button
            onClick={() => handleRecommendationChange(true)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg border transition-colors ${
              feedback.wouldRecommend
                ? 'bg-emerald-500/10 border-emerald-300/40 text-emerald-600'
                : 'border-white/40 text-slate-600 dark:text-slate-300 hover:bg-white/40'
            }`}
          >
            <ThumbsUp className="h-4 w-4" />
            Yes
          </button>
          <button
            onClick={() => handleRecommendationChange(false)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg border transition-colors ${
              !feedback.wouldRecommend
                ? 'bg-red-500/10 border-red-300/40 text-red-600'
                : 'border-white/40 text-slate-600 dark:text-slate-300 hover:bg-white/40'
            }`}
          >
            <ThumbsDown className="h-4 w-4" />
            No
          </button>
        </div>
      </div>

      <div className="flex gap-2">
        <Button onClick={onSubmit} className="btn-primary flex-1">
          <CheckCircle className="h-4 w-4 mr-2" />
          Submit Feedback
        </Button>
        <Button variant="outline" onClick={onCancel} className="btn-outline flex-1">
          Cancel
        </Button>
      </div>
    </div>
  )
}
