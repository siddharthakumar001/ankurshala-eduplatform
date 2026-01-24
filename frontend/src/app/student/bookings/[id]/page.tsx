'use client'

import { useState, useEffect, use } from 'react'
import { useRouter } from 'next/navigation'
import { StudentRoute } from '@/components/route-guard'
import { bookingService, BookingResponse, FeePreviewResponse } from '@/services/bookingService'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Textarea } from '@/components/ui/textarea'
import { FeePreviewModal } from '@/components/ui/confirmation-modal'
import { toast } from 'sonner'
import {
  Calendar,
  Clock,
  User,
  Video,
  Loader2,
  ArrowLeft,
  MessageSquare,
  Save
} from 'lucide-react'

function BookingDetailContent({ bookingId }: { bookingId: number }) {
  const router = useRouter()
  const [booking, setBooking] = useState<BookingResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [noteContent, setNoteContent] = useState('')
  const [savingNote, setSavingNote] = useState(false)
  const [rescheduleDate, setRescheduleDate] = useState('')
  const [rescheduleTime, setRescheduleTime] = useState('')
  const [rescheduleDuration, setRescheduleDuration] = useState(60)
  const [rescheduling, setRescheduling] = useState(false)
  const [joining, setJoining] = useState(false)
  const [cancelling, setCancelling] = useState(false)
  const [feePreview, setFeePreview] = useState<FeePreviewResponse | null>(null)
  const [feeAction, setFeeAction] = useState<'RESCHEDULE' | 'CANCEL' | null>(null)
  const [feeModalOpen, setFeeModalOpen] = useState(false)
  const [feePreviewLoading, setFeePreviewLoading] = useState(false)
  const [pendingCancelReason, setPendingCancelReason] = useState('')

  useEffect(() => {
    const loadBooking = async () => {
      try {
        setLoading(true)
        const data = await bookingService.getBooking(bookingId)
        setBooking(data)
        if (data.startTime) {
          const start = new Date(data.startTime)
          setRescheduleDate(start.toISOString().split('T')[0])
          setRescheduleTime(start.toTimeString().slice(0, 5))
          setRescheduleDuration(data.durationMinutes || 60)
        }
      } catch (error) {
        console.error('Failed to load booking:', error)
        toast.error('Failed to load booking details')
      } finally {
        setLoading(false)
      }
    }

    loadBooking()
  }, [bookingId])

  const canModify = booking?.status === 'PENDING' || booking?.status === 'ACCEPTED' || booking?.status === 'CONFIRMED'
  const canJoin = booking?.status === 'ACCEPTED' || booking?.status === 'CONFIRMED'

  const handleAddNote = async () => {
    if (!noteContent.trim()) return
    try {
      setSavingNote(true)
      await bookingService.addNotes(bookingId, noteContent.trim())
      toast.success('Notes saved')
      setNoteContent('')
    } catch (error) {
      console.error('Failed to save notes:', error)
      toast.error('Failed to save notes')
    } finally {
      setSavingNote(false)
    }
  }

  const performReschedule = async () => {
    if (!rescheduleDate || !rescheduleTime) {
      toast.error('Select a new date and time')
      return
    }

    try {
      setRescheduling(true)
      await bookingService.rescheduleBooking(bookingId, {
        newStartTime: `${rescheduleDate}T${rescheduleTime}:00`,
        newDurationMinutes: rescheduleDuration,
        reason: 'Student reschedule request',
        timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
      })
      toast.success('Reschedule request sent')
      const data = await bookingService.getBooking(bookingId)
      setBooking(data)
    } catch (error) {
      console.error('Failed to reschedule:', error)
      toast.error('Failed to reschedule booking')
    } finally {
      setRescheduling(false)
    }
  }

  const handleJoin = async () => {
    try {
      setJoining(true)
      const response = await bookingService.joinSession(bookingId)
      if (response.sessionUrl) {
        window.open(response.sessionUrl, '_blank')
      } else {
        toast.message('Session details received', { description: response.sessionId })
      }
    } catch (error) {
      console.error('Failed to join session:', error)
      toast.error('Failed to join session')
    } finally {
      setJoining(false)
    }
  }

  const performCancel = async () => {
    if (!pendingCancelReason) {
      toast.error('Please provide a reason for cancellation.')
      return
    }

    try {
      setCancelling(true)
      await bookingService.cancelBooking(bookingId, { reason: pendingCancelReason })
      toast.success('Booking cancelled')
      const data = await bookingService.getBooking(bookingId)
      setBooking(data)
    } catch (error) {
      console.error('Failed to cancel booking:', error)
      toast.error('Failed to cancel booking')
    } finally {
      setCancelling(false)
      setPendingCancelReason('')
    }
  }

  const requestFeePreview = async (action: 'RESCHEDULE' | 'CANCEL') => {
    try {
      setFeePreviewLoading(true)
      const preview = await bookingService.getFeePreview(bookingId, { action })
      setFeePreview(preview)
      setFeeAction(action)
      setFeeModalOpen(true)
    } catch (error) {
      console.error('Failed to load fee preview:', error)
      toast.error('Failed to load fee preview')
    } finally {
      setFeePreviewLoading(false)
    }
  }

  const handleReschedule = async () => {
    if (!canModify) {
      return
    }
    if (!rescheduleDate || !rescheduleTime) {
      toast.error('Select a new date and time')
      return
    }
    await requestFeePreview('RESCHEDULE')
  }

  const handleCancel = async () => {
    const reason = prompt('Please provide a reason for cancellation.')
    if (!reason) return

    setPendingCancelReason(reason)
    await requestFeePreview('CANCEL')
  }

  const handleFeeConfirm = async () => {
    setFeeModalOpen(false)
    if (feeAction === 'RESCHEDULE') {
      await performReschedule()
      return
    }
    if (feeAction === 'CANCEL') {
      await performCancel()
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="glass-panel rounded-2xl border border-white/40 px-6 py-4">
          <Loader2 className="h-8 w-8 animate-spin text-ankur-primary" />
        </div>
      </div>
    )
  }

  if (!booking) {
    return (
      <Card className="glass-panel border-dashed border-2 border-white/40 dark:border-white/10">
        <CardContent className="p-12 text-center">
          <h3 className="text-lg font-semibold text-ankur-secondary dark:text-white mb-2">Booking not found</h3>
          <p className="text-gray-600 dark:text-gray-300">This booking may have been removed.</p>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="container mx-auto py-8 px-4 max-w-4xl">
      <FeePreviewModal
        isOpen={feeModalOpen}
        onClose={() => {
          setFeeModalOpen(false)
          setFeePreview(null)
          setFeeAction(null)
        }}
        onConfirm={handleFeeConfirm}
        action={feeAction || 'CANCEL'}
        originalAmount={booking?.priceMin || 0}
        feeAmount={feePreview?.fee || 0}
        finalAmount={Math.max((booking?.priceMin || 0) - (feePreview?.fee || 0), 0)}
        reason={feePreview?.reason || 'Fee details'}
        currency={feePreview?.currency || booking?.priceCurrency || 'INR'}
        isLoading={feePreviewLoading || rescheduling || cancelling}
      />
      <div className="page-header flex items-center gap-3 mb-6">
        <Button variant="ghost" size="icon" onClick={() => router.back()} className="text-white hover:text-white">
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold text-white">Booking Details</h1>
          <p className="text-white/80">Manage your class request</p>
        </div>
      </div>

      <Card className="mb-6 glass-panel border border-white/40">
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>{booking.topicTitle}</CardTitle>
              <CardDescription>
                {booking.teacherName ? `Teacher: ${booking.teacherName}` : 'Teacher matching in progress'}
              </CardDescription>
            </div>
            <Badge variant="outline">{booking.status}</Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex flex-wrap gap-4 text-sm text-gray-600 dark:text-gray-300">
            <div className="flex items-center gap-2">
              <Calendar className="h-4 w-4" />
              <span>{new Date(booking.startTime).toLocaleDateString()}</span>
            </div>
            <div className="flex items-center gap-2">
              <Clock className="h-4 w-4" />
              <span>
                {new Date(booking.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} -
                {new Date(booking.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </span>
            </div>
            {booking.teacherName && (
              <div className="flex items-center gap-2">
                <User className="h-4 w-4" />
                <span>{booking.teacherName}</span>
              </div>
            )}
          </div>

          {canJoin && (
            <Button onClick={handleJoin} disabled={joining} className="btn-primary">
              {joining ? <Loader2 className="h-4 w-4 animate-spin" /> : <Video className="h-4 w-4 mr-2" />}
              Join Session
            </Button>
          )}
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card className="glass-panel border border-white/40">
          <CardHeader>
            <CardTitle>Reschedule</CardTitle>
            <CardDescription>Request a new time for this class</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <input
              type="date"
              value={rescheduleDate}
              onChange={(e) => setRescheduleDate(e.target.value)}
              className="w-full px-3 py-2 border border-white/40 rounded-md bg-white/70 dark:bg-slate-900/70 dark:border-white/10 dark:text-white"
              disabled={!canModify}
            />
            <input
              type="time"
              value={rescheduleTime}
              onChange={(e) => setRescheduleTime(e.target.value)}
              className="w-full px-3 py-2 border border-white/40 rounded-md bg-white/70 dark:bg-slate-900/70 dark:border-white/10 dark:text-white"
              disabled={!canModify}
            />
            <select
              value={rescheduleDuration}
              onChange={(e) => setRescheduleDuration(parseInt(e.target.value))}
              className="w-full px-3 py-2 border border-white/40 rounded-md bg-white/70 dark:bg-slate-900/70 dark:border-white/10 dark:text-white"
              disabled={!canModify}
            >
              <option value={30}>30 minutes</option>
              <option value={45}>45 minutes</option>
              <option value={60}>60 minutes</option>
              <option value={90}>90 minutes</option>
              <option value={120}>120 minutes</option>
            </select>
            <Button
              variant="outline"
              onClick={handleReschedule}
              disabled={!canModify || rescheduling}
            >
              {rescheduling ? <Loader2 className="h-4 w-4 animate-spin" /> : 'Request Reschedule'}
            </Button>
          </CardContent>
        </Card>

        <Card className="glass-panel border border-white/40">
          <CardHeader>
            <CardTitle>Notes</CardTitle>
            <CardDescription>Add reminders for your class</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <Textarea
              value={noteContent}
              onChange={(e) => setNoteContent(e.target.value)}
              placeholder="Add notes for your teacher"
              className="bg-white/70 dark:bg-slate-900/70 border-white/40 dark:border-white/10 dark:text-white"
            />
            <Button onClick={handleAddNote} disabled={savingNote || !noteContent.trim()}>
              {savingNote ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4 mr-2" />}
              Save Notes
            </Button>
          </CardContent>
        </Card>
      </div>

      <Card className="mt-6 glass-panel border border-white/40">
        <CardHeader>
          <CardTitle>Cancel Booking</CardTitle>
          <CardDescription>Only cancel if you no longer need this class</CardDescription>
        </CardHeader>
        <CardContent>
          <Button variant="destructive" onClick={handleCancel} disabled={!canModify || cancelling}>
            {cancelling ? <Loader2 className="h-4 w-4 animate-spin" /> : 'Cancel Booking'}
          </Button>
        </CardContent>
      </Card>
    </div>
  )
}

export default function BookingDetailsPage({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params)
  const bookingId = parseInt(resolvedParams.id)

  return (
    <StudentRoute>
      <BookingDetailContent bookingId={bookingId} />
    </StudentRoute>
  )
}
