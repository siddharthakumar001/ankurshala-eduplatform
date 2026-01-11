'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { studentAPI } from '@/lib/apiClient'
import { useAuthStore } from '@/stores/authStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { Switch } from "@/components/ui/switch"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { 
  BookOpen, 
  Target, 
  Clock, 
  CheckCircle2, 
  XCircle, 
  Flame,
  Trophy,
  ArrowRight,
  Play,
  SkipForward,
  Loader2,
  Settings2,
  History,
  TrendingUp,
  Bell,
  Languages
} from 'lucide-react'
import { StudentRoute } from '@/components/route-guard'

interface PracticeItem {
  id: number
  topicId: number
  topicName: string
  subjectName: string
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'SKIPPED'
  scheduledForDate: string
  quizId?: number
  score?: number
  questionsAnswered?: number
  questionsCorrect?: number
  masteryDelta?: number
}

interface TodayPractice {
  date: string
  totalItems: number
  completedItems: number
  pendingItems: number
  items: PracticeItem[]
  nextRecommendedTopicName?: string
  nextRecommendedTopicId?: number
}

interface PracticePreferences {
  enabled: boolean
  dailyQuestionCount: number
  preferredTimeLocal: string
  language: string
  notificationEnabled: boolean
}

interface QuizQuestion {
  id: number
  questionText: string
  options: string[]
  questionType: string
}

interface StartPracticeResponse {
  practiceId: number
  quizId: number
  topicName: string
  questions: QuizQuestion[]
  startedAt: string
}

interface QuestionResult {
  questionId: number
  questionText: string
  selectedAnswer: string
  correctAnswer: string
  correct: boolean
  explanation: string
}

interface SubmitPracticeResponse {
  practiceId: number
  score: number
  questionsAnswered: number
  questionsCorrect: number
  masteryDelta: number
  newMasteryScore: number
  feedback: string
  questionResults: QuestionResult[]
  nextRecommendation: {
    type: string
    topicId?: number
    topicName?: string
    message: string
  }
}

interface PracticeStats {
  totalDaysActive: number
  currentStreak: number
  longestStreak: number
  totalQuestionsAnswered: number
  totalQuestionsCorrect: number
  averageAccuracy: number
}

function PracticePageContent() {
  const router = useRouter()
  const queryClient = useQueryClient()
  const [activeTab, setActiveTab] = useState('today')
  const [activePractice, setActivePractice] = useState<StartPracticeResponse | null>(null)
  const [answers, setAnswers] = useState<Record<number, string>>({})
  const [submittedResult, setSubmittedResult] = useState<SubmitPracticeResponse | null>(null)
  const [showSettings, setShowSettings] = useState(false)

  // Fetch preferences
  const { data: preferences, isLoading: prefsLoading } = useQuery<PracticePreferences>({
    queryKey: ['practicePreferences'],
    queryFn: studentAPI.getPracticePreferences
  })

  // Fetch today's practice
  const { data: todayPractice, isLoading: todayLoading, refetch: refetchToday } = useQuery<TodayPractice>({
    queryKey: ['todayPractice'],
    queryFn: studentAPI.getTodayPractice,
    enabled: preferences?.enabled !== false
  })

  // Fetch history
  const { data: history, isLoading: historyLoading } = useQuery({
    queryKey: ['practiceHistory'],
    queryFn: () => studentAPI.getPracticeHistory(30),
    enabled: activeTab === 'history'
  })

  // Update preferences mutation
  const updatePrefsMutation = useMutation({
    mutationFn: studentAPI.updatePracticePreferences,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['practicePreferences'] })
      queryClient.invalidateQueries({ queryKey: ['todayPractice'] })
    }
  })

  // Start practice mutation
  const startPracticeMutation = useMutation({
    mutationFn: (practiceId: number) => studentAPI.startPractice(practiceId),
    onSuccess: (data: StartPracticeResponse) => {
      setActivePractice(data)
      setAnswers({})
      setSubmittedResult(null)
    }
  })

  // Submit practice mutation
  const submitPracticeMutation = useMutation({
    mutationFn: ({ practiceId, answers }: { practiceId: number; answers: { questionId: number; selectedAnswer: string }[] }) => 
      studentAPI.submitPractice(practiceId, answers),
    onSuccess: (data: SubmitPracticeResponse) => {
      setSubmittedResult(data)
      setActivePractice(null)
      queryClient.invalidateQueries({ queryKey: ['todayPractice'] })
    }
  })

  // Skip practice mutation
  const skipPracticeMutation = useMutation({
    mutationFn: (practiceId: number) => studentAPI.skipPractice(practiceId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['todayPractice'] })
    }
  })

  const handleStartPractice = (practiceId: number) => {
    startPracticeMutation.mutate(practiceId)
  }

  const handleAnswerSelect = (questionId: number, answer: string) => {
    setAnswers(prev => ({ ...prev, [questionId]: answer }))
  }

  const handleSubmitPractice = () => {
    if (!activePractice) return
    
    const formattedAnswers = Object.entries(answers).map(([qId, answer]) => ({
      questionId: parseInt(qId),
      selectedAnswer: answer
    }))
    
    submitPracticeMutation.mutate({
      practiceId: activePractice.practiceId,
      answers: formattedAnswers
    })
  }

  const handleSkipPractice = (practiceId: number) => {
    skipPracticeMutation.mutate(practiceId)
  }

  const togglePracticeEnabled = () => {
    if (preferences) {
      updatePrefsMutation.mutate({ enabled: !preferences.enabled })
    }
  }

  const stats: PracticeStats = history?.stats || {
    totalDaysActive: 0,
    currentStreak: 0,
    longestStreak: 0,
    totalQuestionsAnswered: 0,
    totalQuestionsCorrect: 0,
    averageAccuracy: 0
  }

  // Active quiz view
  if (activePractice) {
    const allAnswered = activePractice.questions.every(q => answers[q.id])
    
    return (
      <div className="container mx-auto px-4 py-8 max-w-3xl">
        <Card className="border-0 shadow-xl bg-gradient-to-br from-slate-50 to-white">
          <CardHeader className="border-b bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-t-lg">
            <CardTitle className="flex items-center gap-2">
              <BookOpen className="w-5 h-5" />
              {activePractice.topicName}
            </CardTitle>
            <CardDescription className="text-indigo-100">
              Answer all questions to complete this practice
            </CardDescription>
          </CardHeader>
          <CardContent className="p-6 space-y-6">
            <Progress 
              value={(Object.keys(answers).length / activePractice.questions.length) * 100} 
              className="h-2"
            />
            <p className="text-sm text-gray-500 text-center">
              {Object.keys(answers).length} of {activePractice.questions.length} answered
            </p>

            {activePractice.questions.map((question, index) => (
              <Card key={question.id} className="border shadow-sm">
                <CardContent className="p-4">
                  <div className="flex items-start gap-3 mb-4">
                    <span className="flex-shrink-0 w-8 h-8 rounded-full bg-indigo-100 text-indigo-700 flex items-center justify-center font-semibold text-sm">
                      {index + 1}
                    </span>
                    <p className="font-medium text-gray-800 pt-1">{question.questionText}</p>
                  </div>
                  
                  <RadioGroup
                    value={answers[question.id] || ''}
                    onValueChange={(value) => handleAnswerSelect(question.id, value)}
                    className="space-y-2 pl-11"
                  >
                    {question.options.map((option, optIndex) => (
                      <div 
                        key={optIndex} 
                        className={`flex items-center space-x-3 p-3 rounded-lg border transition-colors cursor-pointer
                          ${answers[question.id] === String(optIndex) 
                            ? 'border-indigo-500 bg-indigo-50' 
                            : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'}`}
                      >
                        <RadioGroupItem value={String(optIndex)} id={`q${question.id}-opt${optIndex}`} />
                        <Label htmlFor={`q${question.id}-opt${optIndex}`} className="flex-1 cursor-pointer">
                          {option}
                        </Label>
                      </div>
                    ))}
                  </RadioGroup>
                </CardContent>
              </Card>
            ))}

            <div className="flex justify-between pt-4">
              <Button 
                variant="outline" 
                onClick={() => setActivePractice(null)}
                disabled={submitPracticeMutation.isPending}
              >
                Cancel
              </Button>
              <Button 
                onClick={handleSubmitPractice}
                disabled={!allAnswered || submitPracticeMutation.isPending}
                className="bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700"
                data-testid="submit-practice-btn"
              >
                {submitPracticeMutation.isPending ? (
                  <>
                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    Submitting...
                  </>
                ) : (
                  <>
                    Submit Practice
                    <ArrowRight className="w-4 h-4 ml-2" />
                  </>
                )}
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  // Results view
  if (submittedResult) {
    const percentage = submittedResult.score || 0
    const isGood = percentage >= 70
    
    return (
      <div className="container mx-auto px-4 py-8 max-w-3xl">
        <Card className="border-0 shadow-xl overflow-hidden">
          <CardHeader className={`${isGood ? 'bg-gradient-to-r from-green-500 to-emerald-600' : 'bg-gradient-to-r from-orange-500 to-amber-600'} text-white`} data-testid="practice-result">
            <div className="flex items-center justify-center mb-4">
              {isGood ? (
                <Trophy className="w-16 h-16 text-yellow-300" />
              ) : (
                <Target className="w-16 h-16 text-white" />
              )}
            </div>
            <CardTitle className="text-center text-3xl" data-testid="practice-score">
              {percentage.toFixed(0)}% Score
            </CardTitle>
            <CardDescription className="text-center text-white/90">
              {submittedResult.questionsCorrect} of {submittedResult.questionsAnswered} correct
            </CardDescription>
          </CardHeader>
          <CardContent className="p-6 space-y-6">
            {/* Mastery Update */}
            <div className="flex items-center justify-between p-4 bg-gradient-to-r from-indigo-50 to-purple-50 rounded-lg">
              <div className="flex items-center gap-3">
                <TrendingUp className={`w-5 h-5 ${(submittedResult.masteryDelta || 0) >= 0 ? 'text-green-600' : 'text-red-600'}`} />
                <span className="font-medium">Mastery Update</span>
              </div>
              <div className="text-right">
                <span className={`font-bold ${(submittedResult.masteryDelta || 0) >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                  {(submittedResult.masteryDelta || 0) >= 0 ? '+' : ''}{((submittedResult.masteryDelta || 0) * 100).toFixed(1)}%
                </span>
                <p className="text-sm text-gray-500">
                  New: {((submittedResult.newMasteryScore || 0) * 100).toFixed(0)}%
                </p>
              </div>
            </div>

            {/* Feedback */}
            <div className="p-4 bg-gray-50 rounded-lg">
              <p className="text-gray-700">{submittedResult.feedback}</p>
            </div>

            {/* Question Results */}
            <div className="space-y-3">
              <h3 className="font-semibold text-gray-800">Question Review</h3>
              {submittedResult.questionResults.map((result, index) => (
                <div 
                  key={result.questionId}
                  className={`p-4 rounded-lg border ${result.correct ? 'border-green-200 bg-green-50' : 'border-red-200 bg-red-50'}`}
                >
                  <div className="flex items-start gap-3">
                    {result.correct ? (
                      <CheckCircle2 className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
                    ) : (
                      <XCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
                    )}
                    <div className="flex-1">
                      <p className="font-medium text-gray-800">{result.questionText}</p>
                      {!result.correct && (
                        <div className="mt-2 space-y-1 text-sm">
                          <p className="text-red-600">Your answer: Option {String.fromCharCode(65 + parseInt(result.selectedAnswer))}</p>
                          <p className="text-green-600">Correct: {result.correctAnswer}</p>
                        </div>
                      )}
                      {result.explanation && (
                        <p className="mt-2 text-sm text-gray-600 bg-white/50 p-2 rounded">
                          {result.explanation}
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Next Recommendation */}
            {submittedResult.nextRecommendation && (
              <div className="p-4 bg-gradient-to-r from-indigo-50 to-purple-50 rounded-lg">
                <p className="text-gray-700">{submittedResult.nextRecommendation.message}</p>
              </div>
            )}

            <Button 
              onClick={() => {
                setSubmittedResult(null)
                refetchToday()
              }}
              className="w-full bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700"
            >
              Continue
              <ArrowRight className="w-4 h-4 ml-2" />
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  // Main view
  return (
    <div className="container mx-auto px-4 py-8" data-testid="practice-page">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
            Daily Practice
          </h1>
          <p className="text-gray-600 mt-1">
            Strengthen your weak topics with daily micro-quizzes
          </p>
        </div>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <Label htmlFor="practice-enabled" className="text-sm">Practice Enabled</Label>
            <Switch
              id="practice-enabled"
              checked={preferences?.enabled}
              onCheckedChange={togglePracticeEnabled}
              disabled={updatePrefsMutation.isPending}
            />
          </div>
          <Button 
            variant="outline" 
            size="icon"
            onClick={() => setShowSettings(!showSettings)}
          >
            <Settings2 className="w-4 h-4" />
          </Button>
        </div>
      </div>

      {/* Settings Panel */}
      {showSettings && preferences && (
        <Card className="mb-6 border-indigo-200 bg-indigo-50/50">
          <CardHeader>
            <CardTitle className="text-lg">Practice Settings</CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-2">
              <Label>Daily Questions</Label>
              <Select
                value={String(preferences.dailyQuestionCount)}
                onValueChange={(value) => updatePrefsMutation.mutate({ dailyQuestionCount: parseInt(value) })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="3">3 questions</SelectItem>
                  <SelectItem value="5">5 questions</SelectItem>
                  <SelectItem value="10">10 questions</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Preferred Time</Label>
              <Select
                value={preferences.preferredTimeLocal}
                onValueChange={(value) => updatePrefsMutation.mutate({ preferredTimeLocal: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="06:00">6:00 AM</SelectItem>
                  <SelectItem value="08:00">8:00 AM</SelectItem>
                  <SelectItem value="10:00">10:00 AM</SelectItem>
                  <SelectItem value="12:00">12:00 PM</SelectItem>
                  <SelectItem value="18:00">6:00 PM</SelectItem>
                  <SelectItem value="20:00">8:00 PM</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Language</Label>
              <Select
                value={preferences.language}
                onValueChange={(value) => updatePrefsMutation.mutate({ language: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="en">English</SelectItem>
                  <SelectItem value="hi">Hindi</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Stats Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <Card className="border-0 shadow-lg bg-gradient-to-br from-orange-500 to-amber-500 text-white">
          <CardContent className="p-4 flex items-center gap-3">
            <Flame className="w-8 h-8" />
            <div>
              <p className="text-2xl font-bold">{stats.currentStreak}</p>
              <p className="text-xs opacity-90">Day Streak</p>
            </div>
          </CardContent>
        </Card>
        <Card className="border-0 shadow-lg bg-gradient-to-br from-green-500 to-emerald-500 text-white">
          <CardContent className="p-4 flex items-center gap-3">
            <CheckCircle2 className="w-8 h-8" />
            <div>
              <p className="text-2xl font-bold">{stats.totalQuestionsCorrect}</p>
              <p className="text-xs opacity-90">Questions Correct</p>
            </div>
          </CardContent>
        </Card>
        <Card className="border-0 shadow-lg bg-gradient-to-br from-blue-500 to-cyan-500 text-white">
          <CardContent className="p-4 flex items-center gap-3">
            <Target className="w-8 h-8" />
            <div>
              <p className="text-2xl font-bold">{stats.averageAccuracy?.toFixed(0) || 0}%</p>
              <p className="text-xs opacity-90">Accuracy</p>
            </div>
          </CardContent>
        </Card>
        <Card className="border-0 shadow-lg bg-gradient-to-br from-purple-500 to-pink-500 text-white">
          <CardContent className="p-4 flex items-center gap-3">
            <Trophy className="w-8 h-8" />
            <div>
              <p className="text-2xl font-bold">{stats.longestStreak}</p>
              <p className="text-xs opacity-90">Best Streak</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid grid-cols-2 w-full max-w-md mb-6">
          <TabsTrigger value="today" className="flex items-center gap-2">
            <BookOpen className="w-4 h-4" />
            Today's Practice
          </TabsTrigger>
          <TabsTrigger value="history" className="flex items-center gap-2">
            <History className="w-4 h-4" />
            History
          </TabsTrigger>
        </TabsList>

        <TabsContent value="today">
          {todayLoading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="w-8 h-8 animate-spin text-indigo-600" />
            </div>
          ) : !preferences?.enabled ? (
            <Card className="border-dashed border-2 border-gray-300">
              <CardContent className="p-12 text-center">
                <Bell className="w-12 h-12 mx-auto text-gray-400 mb-4" />
                <h3 className="text-lg font-semibold text-gray-700 mb-2">Daily Practice is Disabled</h3>
                <p className="text-gray-500 mb-4">Enable daily practice to get personalized micro-quizzes based on your weak topics.</p>
                <Button onClick={togglePracticeEnabled}>
                  Enable Daily Practice
                </Button>
              </CardContent>
            </Card>
          ) : todayPractice?.items.length === 0 ? (
            <Card className="border-dashed border-2 border-gray-300">
              <CardContent className="p-12 text-center">
                <CheckCircle2 className="w-12 h-12 mx-auto text-green-500 mb-4" />
                <h3 className="text-lg font-semibold text-gray-700 mb-2">No Practice Items Today</h3>
                <p className="text-gray-500">Great job! You've mastered your topics or practice items will be generated soon.</p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {/* Progress Summary */}
              {todayPractice && (
                <Card className="bg-gradient-to-r from-indigo-50 to-purple-50 border-indigo-200">
                  <CardContent className="p-4">
                    <div className="flex items-center justify-between mb-2">
                      <span className="font-medium text-indigo-800">Today's Progress</span>
                      <span className="text-sm text-indigo-600">
                        {todayPractice.completedItems} / {todayPractice.totalItems} completed
                      </span>
                    </div>
                    <Progress 
                      value={(todayPractice.completedItems / todayPractice.totalItems) * 100} 
                      className="h-2"
                    />
                  </CardContent>
                </Card>
              )}

              {/* Practice Items */}
              {todayPractice?.items.map((item) => (
                <Card 
                  key={item.id} 
                  className={`transition-all ${
                    item.status === 'COMPLETED' 
                      ? 'bg-green-50 border-green-200' 
                      : item.status === 'SKIPPED'
                        ? 'bg-gray-50 border-gray-200 opacity-60'
                        : 'hover:shadow-md'
                  }`}
                >
                  <CardContent className="p-4">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-4">
                        <div className={`w-12 h-12 rounded-full flex items-center justify-center ${
                          item.status === 'COMPLETED' 
                            ? 'bg-green-100' 
                            : item.status === 'SKIPPED'
                              ? 'bg-gray-100'
                              : 'bg-indigo-100'
                        }`}>
                          {item.status === 'COMPLETED' ? (
                            <CheckCircle2 className="w-6 h-6 text-green-600" />
                          ) : item.status === 'SKIPPED' ? (
                            <SkipForward className="w-6 h-6 text-gray-400" />
                          ) : (
                            <BookOpen className="w-6 h-6 text-indigo-600" />
                          )}
                        </div>
                        <div>
                          <h3 className="font-semibold text-gray-800">{item.topicName}</h3>
                          <p className="text-sm text-gray-500">{item.subjectName}</p>
                        </div>
                      </div>
                      <div className="flex items-center gap-3">
                        {item.status === 'COMPLETED' && item.score !== undefined && (
                          <Badge variant="secondary" className="bg-green-100 text-green-700">
                            {item.score.toFixed(0)}%
                          </Badge>
                        )}
                        {item.status === 'PENDING' && (
                          <div className="flex gap-2">
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleSkipPractice(item.id)}
                              disabled={skipPracticeMutation.isPending}
                            >
                              <SkipForward className="w-4 h-4" />
                            </Button>
                          <Button
                            onClick={() => handleStartPractice(item.id)}
                            disabled={startPracticeMutation.isPending}
                            className="bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700"
                            data-testid="start-practice-btn"
                          >
                            <Play className="w-4 h-4 mr-2" />
                            Start
                          </Button>
                          </div>
                        )}
                        {item.status === 'IN_PROGRESS' && (
                          <Button
                            onClick={() => handleStartPractice(item.id)}
                            variant="outline"
                          >
                            Continue
                            <ArrowRight className="w-4 h-4 ml-2" />
                          </Button>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="history">
          {historyLoading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="w-8 h-8 animate-spin text-indigo-600" />
            </div>
          ) : !history?.days?.length ? (
            <Card className="border-dashed border-2 border-gray-300">
              <CardContent className="p-12 text-center">
                <History className="w-12 h-12 mx-auto text-gray-400 mb-4" />
                <h3 className="text-lg font-semibold text-gray-700 mb-2">No Practice History</h3>
                <p className="text-gray-500">Complete some daily practice sessions to see your history here.</p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-6">
              {history.days.map((day: any) => (
                <Card key={day.date} className="overflow-hidden">
                  <CardHeader className="bg-gray-50 py-3">
                    <div className="flex items-center justify-between">
                      <CardTitle className="text-base font-medium">
                        {new Date(day.date).toLocaleDateString('en-US', { 
                          weekday: 'long', 
                          month: 'short', 
                          day: 'numeric' 
                        })}
                      </CardTitle>
                      <div className="flex items-center gap-3">
                        <Badge variant="outline">
                          {day.completedItems}/{day.totalItems} completed
                        </Badge>
                        {day.averageScore && (
                          <Badge className="bg-indigo-100 text-indigo-700">
                            {day.averageScore.toFixed(0)}% avg
                          </Badge>
                        )}
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent className="p-4">
                    <div className="space-y-2">
                      {day.items.map((item: PracticeItem) => (
                        <div 
                          key={item.id}
                          className="flex items-center justify-between py-2 border-b last:border-0"
                        >
                          <div className="flex items-center gap-3">
                            {item.status === 'COMPLETED' ? (
                              <CheckCircle2 className="w-4 h-4 text-green-500" />
                            ) : (
                              <XCircle className="w-4 h-4 text-gray-400" />
                            )}
                            <span className="text-sm">{item.topicName}</span>
                          </div>
                          {item.score !== undefined && (
                            <span className="text-sm font-medium text-gray-600">
                              {item.score.toFixed(0)}%
                            </span>
                          )}
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}

export default function PracticePage() {
  return (
    <StudentRoute>
      <PracticePageContent />
    </StudentRoute>
  )
}

