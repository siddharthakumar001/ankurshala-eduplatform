'use client'

import { useState, useEffect, use } from 'react'
import { useRouter } from 'next/navigation'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { studentAPI } from '@/lib/apiClient'
import { useAuthStore } from '@/stores/authStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { 
  BookOpen, 
  Clock, 
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
        <Loader2 className="w-8 h-8 animate-spin text-indigo-600" />
      </div>
    )
  }

  if (!companion) {
    return (
      <div className="container mx-auto px-4 py-8">
        <Card className="border-dashed border-2 border-gray-300">
          <CardContent className="p-12 text-center">
            <FileText className="w-12 h-12 mx-auto text-gray-400 mb-4" />
            <h3 className="text-lg font-semibold text-gray-700 mb-2">Companion Not Found</h3>
            <p className="text-gray-500">This booking doesn't have a session companion yet.</p>
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
      default: return <PenLine className="w-4 h-4 text-gray-500" />
    }
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-5xl" data-testid="companion-page">
      {/* Header */}
      <div className="flex items-center gap-4 mb-6">
        <Button variant="ghost" size="icon" onClick={() => router.back()}>
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <div className="flex-1">
          <h1 className="text-2xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
            Session Companion
          </h1>
          <p className="text-gray-600">
            {companion.topicName && `${companion.topicName} • `}
            {companion.teacherName && `with ${companion.teacherName}`}
          </p>
        </div>
        <Badge 
          variant="outline" 
          className={`
            ${companion.bookingStatus === 'CONFIRMED' ? 'border-blue-500 text-blue-700 bg-blue-50' : ''}
            ${companion.bookingStatus === 'IN_PROGRESS' ? 'border-green-500 text-green-700 bg-green-50' : ''}
            ${companion.bookingStatus === 'COMPLETED' ? 'border-gray-500 text-gray-700 bg-gray-50' : ''}
          `}
        >
          {companion.bookingStatus}
        </Badge>
      </div>

      {/* Session Info Card */}
      <Card className="mb-6 bg-gradient-to-r from-indigo-50 to-purple-50 border-indigo-200">
        <CardContent className="p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-white shadow flex items-center justify-center">
                <GraduationCap className="w-6 h-6 text-indigo-600" />
              </div>
              <div>
                <p className="font-medium text-gray-800">{companion.subjectName}</p>
                <p className="text-sm text-gray-600">{companion.topicName}</p>
              </div>
            </div>
            {companion.scheduledStartTime && (
              <div className="text-right">
                <p className="text-sm text-gray-500">Scheduled</p>
                <p className="font-medium text-gray-800">
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
        <TabsList className="grid grid-cols-3 w-full mb-6">
          <TabsTrigger value="before" className="flex items-center gap-2">
            <BookOpen className="w-4 h-4" />
            Before Class
          </TabsTrigger>
          <TabsTrigger value="during" className="flex items-center gap-2">
            <PenLine className="w-4 h-4" />
            During Class
          </TabsTrigger>
          <TabsTrigger value="after" className="flex items-center gap-2">
            <ClipboardList className="w-4 h-4" />
            After Class
          </TabsTrigger>
        </TabsList>

        {/* BEFORE CLASS TAB */}
        <TabsContent value="before" className="space-y-6">
          {/* Pre-session Plan */}
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="flex items-center gap-2">
                  <Brain className="w-5 h-5 text-indigo-600" />
                  Pre-Session Preparation
                </CardTitle>
                {!companion.preSessionPlanMd && (
                  <Button
                    onClick={() => generatePrepMutation.mutate()}
                    disabled={generatePrepMutation.isPending}
                    className="bg-gradient-to-r from-indigo-500 to-purple-600"
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
                <div className="prose prose-sm max-w-none" data-testid="prep-plan-content">
                  <ReactMarkdown>{companion.preSessionPlanMd}</ReactMarkdown>
                </div>
              ) : (
                <div className="text-center py-8 text-gray-500">
                  <BookOpen className="w-12 h-12 mx-auto mb-4 text-gray-300" />
                  <p>Generate a prep plan to prepare for your upcoming session.</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Warmup Quiz */}
          {companion.warmupQuizId && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Play className="w-5 h-5 text-green-600" />
                  Warmup Quiz
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <p className="text-gray-600">
                    {companion.warmupCompleted 
                      ? "You've completed the warmup quiz!" 
                      : "Complete a quick warmup quiz to refresh your memory."}
                  </p>
                  {companion.warmupCompleted ? (
                    <Badge className="bg-green-100 text-green-700">
                      <CheckCircle2 className="w-4 h-4 mr-1" />
                      Completed
                    </Badge>
                  ) : (
                    <Button variant="outline">
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
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <PenLine className="w-5 h-5 text-indigo-600" />
                Add Note
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex gap-4">
                <Select value={noteType} onValueChange={setNoteType}>
                  <SelectTrigger className="w-40">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="NOTE">📝 Note</SelectItem>
                    <SelectItem value="QUESTION">❓ Question</SelectItem>
                    <SelectItem value="HIGHLIGHT">💡 Highlight</SelectItem>
                    <SelectItem value="ACTION_ITEM">✅ Action Item</SelectItem>
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
                  className="flex-1 min-h-[80px]"
                />
              </div>
              <div className="flex justify-end">
                <Button
                  onClick={() => addNoteMutation.mutate()}
                  disabled={!noteContent.trim() || addNoteMutation.isPending}
                  className="bg-gradient-to-r from-indigo-500 to-purple-600"
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
          <Card>
            <CardHeader>
              <CardTitle>Session Notes</CardTitle>
              <CardDescription>
                {companion.notes?.length || 0} notes captured
              </CardDescription>
            </CardHeader>
            <CardContent>
              {!companion.notes?.length ? (
                <div className="text-center py-8 text-gray-500">
                  <PenLine className="w-12 h-12 mx-auto mb-4 text-gray-300" />
                  <p>No notes yet. Start capturing your thoughts!</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {companion.notes.map((note) => (
                    <div 
                      key={note.id}
                      className={`p-4 rounded-lg border ${
                        note.noteType === 'QUESTION' ? 'bg-blue-50 border-blue-200' :
                        note.noteType === 'HIGHLIGHT' ? 'bg-yellow-50 border-yellow-200' :
                        note.noteType === 'ACTION_ITEM' ? 'bg-green-50 border-green-200' :
                        'bg-gray-50 border-gray-200'
                      }`}
                    >
                      <div className="flex items-start gap-3">
                        {getNoteTypeIcon(note.noteType)}
                        <div className="flex-1">
                          <p className="text-gray-800">{note.content}</p>
                          {note.aiResponse && (
                            <div className="mt-2 p-3 bg-white rounded border text-sm">
                              <p className="text-xs text-indigo-600 font-medium mb-1">AI Response:</p>
                              <p className="text-gray-700">{note.aiResponse}</p>
                            </div>
                          )}
                          <p className="text-xs text-gray-500 mt-2">
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
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="flex items-center gap-2">
                  <ClipboardList className="w-5 h-5 text-indigo-600" />
                  Session Summary
                </CardTitle>
                {!companion.postSessionSummaryMd && (
                  <Button
                    onClick={() => generatePostMutation.mutate()}
                    disabled={generatePostMutation.isPending}
                    className="bg-gradient-to-r from-indigo-500 to-purple-600"
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
                <div className="prose prose-sm max-w-none" data-testid="post-summary-content">
                  <ReactMarkdown>{companion.postSessionSummaryMd}</ReactMarkdown>
                </div>
              ) : (
                <div className="text-center py-8 text-gray-500">
                  <ClipboardList className="w-12 h-12 mx-auto mb-4 text-gray-300" />
                  <p>Generate a summary after your session is completed.</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Homework */}
          {companion.homeworkPlanMd && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <FileText className="w-5 h-5 text-purple-600" />
                  Homework Plan
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="prose prose-sm max-w-none">
                  <ReactMarkdown>{companion.homeworkPlanMd}</ReactMarkdown>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Session Highlights */}
          {companion.sessionHighlights && companion.sessionHighlights.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Lightbulb className="w-5 h-5 text-yellow-500" />
                  Key Highlights
                </CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {companion.sessionHighlights.map((highlight, index) => (
                    <li key={index} className="flex items-start gap-2">
                      <span className="text-yellow-500 mt-1">•</span>
                      <span>{highlight}</span>
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          )}

          {/* Questions Discussed */}
          {companion.questionsAsked && companion.questionsAsked.length > 0 && (
            <Card>
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
                      <span className="text-blue-500 mt-1">?</span>
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

