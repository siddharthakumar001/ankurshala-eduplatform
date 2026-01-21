'use client'

import { useState, useEffect, use } from 'react'
import { useRouter } from 'next/navigation'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { studentAPI } from '@/lib/apiClient'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { 
  BookOpen, 
  CheckCircle2, 
  Loader2,
  Play,
  FileText,
  MessageSquare,
  Lightbulb,
  ListChecks,
  ArrowLeft,
  ChevronRight,
  PenLine,
  Brain,
  Sparkles,
  GraduationCap,
  ClipboardList,
  Send
} from 'lucide-react'
import { StudentRoute } from '@/components/route-guard'
import ReactMarkdown from 'react-markdown'

interface CompanionNote {
  id: number
  noteType: string
  content: string
  timestampInSession?: number
  aiResponse?: string
  isResolved: boolean
  createdAt: string
}

interface Companion {
  id: number
  bookingId: number
  studentId: number
  teacherId?: number
  teacherName?: string
  topicId?: number
  topicName?: string
  subjectId?: number
  subjectName?: string
  scheduledStartTime?: string
  scheduledEndTime?: string
  bookingStatus?: string
  preSessionPlanMd?: string
  warmupQuizId?: number
  warmupCompleted?: boolean
  liveNotesMd?: string
  sessionHighlights?: string[]
  questionsAsked?: string[]
  notes?: CompanionNote[]
  postSessionSummaryMd?: string
  homeworkPlanMd?: string
  recommendedTopics?: number[]
  recommendedTopicNames?: string[]
  status: string
  prepGeneratedAt?: string
  liveStartedAt?: string
  postGeneratedAt?: string
  createdAt: string
}

function CompanionPageContent({ bookingId }: { bookingId: number }) {
  const router = useRouter()
  const queryClient = useQueryClient()
  const [activeTab, setActiveTab] = useState('before')
  const [noteContent, setNoteContent] = useState('')
  const [noteType, setNoteType] = useState('NOTE')

  // Fetch companion
  const { data: companion, isLoading, refetch } = useQuery<Companion>({
    queryKey: ['companion', bookingId],
    queryFn: () => studentAPI.getCompanion(bookingId)
  })

  // Generate prep mutation
  const generatePrepMutation = useMutation({
    mutationFn: () => studentAPI.generateCompanionPrep(bookingId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['companion', bookingId] })
    }
  })

  // Add note mutation
  const addNoteMutation = useMutation({
    mutationFn: () => studentAPI.addCompanionNote(bookingId, noteContent, noteType),
    onSuccess: () => {
      setNoteContent('')
      queryClient.invalidateQueries({ queryKey: ['companion', bookingId] })
    }
  })

  // Generate post mutation
  const generatePostMutation = useMutation({
    mutationFn: () => studentAPI.generateCompanionPost(bookingId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['companion', bookingId] })
    }
  })

  // Determine which tab should be active based on booking status
  useEffect(() => {
    if (companion) {
      const status = companion.bookingStatus?.toLowerCase()
      if (status === 'completed') {
        setActiveTab('after')
      } else if (status === 'in_progress') {
        setActiveTab('during')
      } else {
        setActiveTab('before')
      }
    }
  }, [companion?.bookingStatus])

  // Fallback polling: Refresh companion data periodically if booking is not completed
  // This ensures UI updates even if WebSocket disconnects or is unavailable
  // Polling interval: 10 seconds for active bookings, 30 seconds for confirmed bookings
  useEffect(() => {
    if (!companion || companion.bookingStatus === 'COMPLETED') {
      return // Don't poll if completed
    }

    // Determine polling interval based on booking status
    const pollInterval = companion.bookingStatus === 'IN_PROGRESS' ? 10000 : 30000 // 10s for active, 30s for confirmed

    const pollTimer = setInterval(() => {
      refetch()
    }, pollInterval)

    return () => clearInterval(pollTimer)
  }, [companion?.bookingStatus, refetch])

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-20">
        <Loader2 className="w-8 h-8 animate-spin text-emerald-500" />
      </div>
    )
  }

  if (!companion) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-8">
        <Card className="glass-panel border border-white/40">
          <CardContent className="p-12 text-center">
            <FileText className="w-12 h-12 mx-auto text-slate-400 mb-4" />
            <h3 className="text-lg font-semibold text-slate-800 dark:text-slate-100 mb-2">Companion Not Found</h3>
            <p className="text-slate-500 dark:text-slate-300">This booking doesn't have a session companion yet.</p>
          </CardContent>
        </Card>
      </div>
    )
  }

  const getNoteTypeIcon = (type: string) => {
    switch (type) {
      case 'QUESTION': return <MessageSquare className="w-4 h-4 text-blue-500" />
      case 'HIGHLIGHT': return <Lightbulb className="w-4 h-4 text-yellow-500" />
      case 'ACTION_ITEM': return <ListChecks className="w-4 h-4 text-green-500" />
      default: return <PenLine className="w-4 h-4 text-slate-400 dark:text-slate-300" />
    }
  }

  const statusBadgeClass = companion.bookingStatus === 'CONFIRMED'
    ? 'border-sky-200/40 bg-sky-500/20 text-white'
    : companion.bookingStatus === 'IN_PROGRESS'
      ? 'border-emerald-200/40 bg-emerald-500/20 text-white'
      : companion.bookingStatus === 'COMPLETED'
        ? 'border-slate-200/40 bg-slate-500/20 text-white'
        : 'border-white/40 bg-white/10 text-white'

  return (
    <div className="space-y-6 max-w-5xl mx-auto px-4" data-testid="companion-page">
      {/* Header */}
      <div className="page-header">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div className="flex items-start gap-3">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => router.back()}
              className="text-white/90 hover:text-white hover:bg-white/10"
            >
              <ArrowLeft className="w-5 h-5" />
            </Button>
            <div>
              <h1 className="text-2xl md:text-3xl font-bold text-white">
                Session Companion
              </h1>
              <p className="text-white/80">
                {companion.topicName ? `${companion.topicName} - ` : ''}
                {companion.teacherName ? `with ${companion.teacherName}` : 'Your learning plan'}
              </p>
            </div>
          </div>
          <Badge variant="outline" className={`border ${statusBadgeClass}`}>
            {companion.bookingStatus}
          </Badge>
        </div>
      </div>

      {/* Session Info Card */}
      <Card className="glass-panel border border-white/40">
        <CardContent className="p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-white/70 dark:bg-slate-900/70 shadow flex items-center justify-center">
                <GraduationCap className="w-6 h-6 text-emerald-500" />
              </div>
              <div>
                <p className="font-medium text-slate-800 dark:text-slate-100">{companion.subjectName}</p>
                <p className="text-sm text-slate-500 dark:text-slate-300">{companion.topicName}</p>
              </div>
            </div>
            {companion.scheduledStartTime && (
              <div className="text-right">
                <p className="text-sm text-slate-500 dark:text-slate-300">Scheduled</p>
                <p className="font-medium text-slate-800 dark:text-slate-100">
                  {new Date(companion.scheduledStartTime).toLocaleDateString('en-US', {
                    weekday: 'short',
                    month: 'short',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                  })}
                </p>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="glass-panel grid grid-cols-3 w-full p-1">
          <TabsTrigger
            value="before"
            className="flex items-center gap-2 data-[state=active]:bg-white/70 data-[state=active]:text-slate-900 dark:data-[state=active]:bg-slate-900/80 dark:data-[state=active]:text-white"
          >
            <BookOpen className="w-4 h-4" />
            Before Class
          </TabsTrigger>
          <TabsTrigger
            value="during"
            className="flex items-center gap-2 data-[state=active]:bg-white/70 data-[state=active]:text-slate-900 dark:data-[state=active]:bg-slate-900/80 dark:data-[state=active]:text-white"
          >
            <PenLine className="w-4 h-4" />
            During Class
          </TabsTrigger>
          <TabsTrigger
            value="after"
            className="flex items-center gap-2 data-[state=active]:bg-white/70 data-[state=active]:text-slate-900 dark:data-[state=active]:bg-slate-900/80 dark:data-[state=active]:text-white"
          >
            <ClipboardList className="w-4 h-4" />
            After Class
          </TabsTrigger>
        </TabsList>

        {/* BEFORE CLASS TAB */}
        <TabsContent value="before" className="space-y-6">
          {/* Pre-session Plan */}
          <Card className="glass-panel border border-white/40">
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="flex items-center gap-2">
                  <Brain className="w-5 h-5 text-emerald-500" />
                  Pre-Session Preparation
                </CardTitle>
                {!companion.preSessionPlanMd && (
                  <Button
                    onClick={() => generatePrepMutation.mutate()}
                    disabled={generatePrepMutation.isPending}
                    className="btn-primary"
                    data-testid="generate-prep-btn"
                  >
                    {generatePrepMutation.isPending ? (
                      <>
                        <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                        Generating...
                      </>
                    ) : (
                      <>
                        <Sparkles className="w-4 h-4 mr-2" />
                        Generate Prep Plan
                      </>
                    )}
                  </Button>
                )}
              </div>
            </CardHeader>
            <CardContent>
              {companion.preSessionPlanMd ? (
                <div className="prose prose-sm max-w-none dark:prose-invert" data-testid="prep-plan-content">
                  <ReactMarkdown>{companion.preSessionPlanMd}</ReactMarkdown>
                </div>
              ) : (
                <div className="text-center py-8 text-slate-500 dark:text-slate-300">
                  <BookOpen className="w-12 h-12 mx-auto mb-4 text-slate-300" />
                  <p>Generate a prep plan to prepare for your upcoming session.</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Warmup Quiz */}
            {companion.warmupQuizId && (
              <Card className="glass-panel border border-white/40">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Play className="w-5 h-5 text-emerald-500" />
                    Warmup Quiz
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center justify-between gap-4">
                    <p className="text-slate-600 dark:text-slate-200">
                      {companion.warmupCompleted 
                        ? "You've completed the warmup quiz!" 
                        : "Complete a quick warmup quiz to refresh your memory."}
                    </p>
                    {companion.warmupCompleted ? (
                      <Badge className="badge-success">
                        <CheckCircle2 className="w-4 h-4 mr-1" />
                        Completed
                      </Badge>
                    ) : (
                      <Button variant="outline" className="btn-outline">
                        Start Quiz
                        <ChevronRight className="w-4 h-4 ml-1" />
                      </Button>
                    )}
                  </div>
                </CardContent>
              </Card>
            )}
        </TabsContent>

        {/* DURING CLASS TAB */}
        <TabsContent value="during" className="space-y-6">
          {/* Add Note */}
          <Card className="glass-panel border border-white/40">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <PenLine className="w-5 h-5 text-emerald-500" />
                Add Note
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex flex-col md:flex-row gap-4">
                <Select value={noteType} onValueChange={setNoteType}>
                  <SelectTrigger className="input-modern w-full md:w-40">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="NOTE">Note</SelectItem>
                    <SelectItem value="QUESTION">Question</SelectItem>
                    <SelectItem value="HIGHLIGHT">Highlight</SelectItem>
                    <SelectItem value="ACTION_ITEM">Action Item</SelectItem>
                  </SelectContent>
                </Select>
                <Textarea
                  value={noteContent}
                  onChange={(e) => setNoteContent(e.target.value)}
                  placeholder={
                    noteType === 'QUESTION' ? "What would you like to ask?" :
                    noteType === 'HIGHLIGHT' ? "What key point do you want to remember?" :
                    noteType === 'ACTION_ITEM' ? "What do you need to do?" :
                    "Take a note..."
                  }
                  className="input-modern flex-1 min-h-[80px]"
                />
              </div>
              <div className="flex justify-end">
                <Button
                  onClick={() => addNoteMutation.mutate()}
                  disabled={!noteContent.trim() || addNoteMutation.isPending}
                  className="btn-primary"
                  data-testid="save-note-btn"
                >
                  {addNoteMutation.isPending ? (
                    <Loader2 className="w-4 h-4 animate-spin" />
                  ) : (
                    <>
                      <Send className="w-4 h-4 mr-2" />
                      Save Note
                    </>
                  )}
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* Session Notes */}
          <Card className="glass-panel border border-white/40">
            <CardHeader>
              <CardTitle>Session Notes</CardTitle>
              <CardDescription>
                {companion.notes?.length || 0} notes captured
              </CardDescription>
            </CardHeader>
            <CardContent>
              {!companion.notes?.length ? (
                <div className="text-center py-8 text-slate-500 dark:text-slate-300">
                  <PenLine className="w-12 h-12 mx-auto mb-4 text-slate-300" />
                  <p>No notes yet. Start capturing your thoughts!</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {companion.notes.map((note) => (
                    <div 
                      key={note.id}
                      className={`glass rounded-lg border ${
                        note.noteType === 'QUESTION' ? 'border-blue-200/60' :
                        note.noteType === 'HIGHLIGHT' ? 'border-yellow-200/60' :
                        note.noteType === 'ACTION_ITEM' ? 'border-emerald-200/60' :
                        'border-white/30'
                      }`}
                    >
                      <div className="flex items-start gap-3 p-4">
                        {getNoteTypeIcon(note.noteType)}
                        <div className="flex-1">
                          <p className="text-slate-800 dark:text-slate-100">{note.content}</p>
                          {note.aiResponse && (
                            <div className="mt-2 p-3 glass rounded border border-white/40 text-sm">
                              <p className="text-xs text-emerald-600 font-medium mb-1">AI Response:</p>
                              <p className="text-slate-700 dark:text-slate-200">{note.aiResponse}</p>
                            </div>
                          )}
                          <p className="text-xs text-slate-500 dark:text-slate-300 mt-2">
                            {new Date(note.createdAt).toLocaleTimeString()}
                          </p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* AFTER CLASS TAB */}
        <TabsContent value="after" className="space-y-6">
          {/* Session Summary */}
          <Card className="glass-panel border border-white/40">
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="flex items-center gap-2">
                  <ClipboardList className="w-5 h-5 text-emerald-500" />
                  Session Summary
                </CardTitle>
                {!companion.postSessionSummaryMd && (
                  <Button
                    onClick={() => generatePostMutation.mutate()}
                    disabled={generatePostMutation.isPending}
                    className="btn-primary"
                    data-testid="generate-summary-btn"
                  >
                    {generatePostMutation.isPending ? (
                      <>
                        <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                        Generating...
                      </>
                    ) : (
                      <>
                        <Sparkles className="w-4 h-4 mr-2" />
                        Generate Summary
                      </>
                    )}
                  </Button>
                )}
              </div>
            </CardHeader>
            <CardContent>
              {companion.postSessionSummaryMd ? (
                <div className="prose prose-sm max-w-none dark:prose-invert" data-testid="post-summary-content">
                  <ReactMarkdown>{companion.postSessionSummaryMd}</ReactMarkdown>
                </div>
              ) : (
                <div className="text-center py-8 text-slate-500 dark:text-slate-300">
                  <ClipboardList className="w-12 h-12 mx-auto mb-4 text-slate-300" />
                  <p>Generate a summary after your session is completed.</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Homework */}
          {companion.homeworkPlanMd && (
            <Card className="glass-panel border border-white/40">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <FileText className="w-5 h-5 text-emerald-500" />
                  Homework Plan
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="prose prose-sm max-w-none dark:prose-invert">
                  <ReactMarkdown>{companion.homeworkPlanMd}</ReactMarkdown>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Session Highlights */}
          {companion.sessionHighlights && companion.sessionHighlights.length > 0 && (
            <Card className="glass-panel border border-white/40">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Lightbulb className="w-5 h-5 text-amber-500" />
                  Key Highlights
                </CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {companion.sessionHighlights.map((highlight, index) => (
                    <li key={index} className="flex items-start gap-2">
                      <span className="text-amber-500 mt-1">-</span>
                      <span>{highlight}</span>
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          )}

          {/* Questions Discussed */}
          {companion.questionsAsked && companion.questionsAsked.length > 0 && (
            <Card className="glass-panel border border-white/40">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <MessageSquare className="w-5 h-5 text-blue-500" />
                  Questions Discussed
                </CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {companion.questionsAsked.map((question, index) => (
                    <li key={index} className="flex items-start gap-2">
                      <span className="text-blue-500 mt-1">-</span>
                      <span>{question}</span>
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}

export default function CompanionPage({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params)
  const bookingId = parseInt(resolvedParams.id)

  return (
    <StudentRoute>
      <CompanionPageContent bookingId={bookingId} />
    </StudentRoute>
  )
}

