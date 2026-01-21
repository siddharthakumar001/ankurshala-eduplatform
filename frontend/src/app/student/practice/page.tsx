'use client'

import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { studentAPI } from '@/lib/apiClient'
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
      <div className="max-w-3xl mx-auto px-4 py-8">
        <Card className="glass-panel border border-white/40">
          <CardHeader className="border-b border-white/20">
            <CardTitle className="flex items-center gap-2 text-slate-900 dark:text-white">
              <BookOpen className="w-5 h-5 text-emerald-500" />
              {activePractice.topicName}
            </CardTitle>
            <CardDescription className="text-slate-500 dark:text-slate-300">
              Answer all questions to complete this practice
            </CardDescription>
          </CardHeader>
          <CardContent className="p-6 space-y-6">
            <Progress 
              value={(Object.keys(answers).length / activePractice.questions.length) * 100} 
              className="h-2"
            />
            <p className="text-sm text-slate-500 dark:text-slate-300 text-center">
              {Object.keys(answers).length} of {activePractice.questions.length} answered
            </p>

            {activePractice.questions.map((question, index) => (
              <Card key={question.id} className="glass-panel border border-white/40">
                <CardContent className="p-4">
                  <div className="flex items-start gap-3 mb-4">
                    <span className="flex-shrink-0 w-8 h-8 rounded-full bg-emerald-500/10 text-emerald-500 flex items-center justify-center font-semibold text-sm">
                      {index + 1}
                    </span>
                    <p className="font-medium text-slate-800 dark:text-slate-100 pt-1">{question.questionText}</p>
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
                            ? 'border-emerald-400 bg-white/80' 
                            : 'border-white/30 bg-white/50 hover:border-white/60'}`}
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
                className="btn-outline"
              >
                Cancel
              </Button>
              <Button 
                onClick={handleSubmitPractice}
                disabled={!allAnswered || submitPracticeMutation.isPending}
                className="btn-primary"
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
      <div className="max-w-3xl mx-auto px-4 py-8">
        <Card className="glass-panel border border-white/40 overflow-hidden">
          <CardHeader className="border-b border-white/20 text-center" data-testid="practice-result">
            <div className="flex items-center justify-center mb-4">
              {isGood ? (
                <Trophy className="w-16 h-16 text-amber-500" />
              ) : (
                <Target className="w-16 h-16 text-emerald-500" />
              )}
            </div>
            <CardTitle className="text-center text-3xl text-slate-900 dark:text-white" data-testid="practice-score">
              {percentage.toFixed(0)}% Score
            </CardTitle>
            <CardDescription className="text-center text-slate-500 dark:text-slate-300">
              {submittedResult.questionsCorrect} of {submittedResult.questionsAnswered} correct
            </CardDescription>
          </CardHeader>
          <CardContent className="p-6 space-y-6">
            {/* Mastery Update */}
            <div className="flex items-center justify-between p-4 glass rounded-lg">
              <div className="flex items-center gap-3">
                <TrendingUp className={`w-5 h-5 ${(submittedResult.masteryDelta || 0) >= 0 ? 'text-green-600' : 'text-red-600'}`} />
                <span className="font-medium">Mastery Update</span>
              </div>
              <div className="text-right">
                <span className={`font-bold ${(submittedResult.masteryDelta || 0) >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                  {(submittedResult.masteryDelta || 0) >= 0 ? '+' : ''}{((submittedResult.masteryDelta || 0) * 100).toFixed(1)}%
                </span>
                <p className="text-sm text-slate-500 dark:text-slate-300">
                  New: {((submittedResult.newMasteryScore || 0) * 100).toFixed(0)}%
                </p>
              </div>
            </div>

            {/* Feedback */}
            <div className="p-4 glass rounded-lg">
              <p className="text-slate-700 dark:text-slate-200">{submittedResult.feedback}</p>
            </div>

            {/* Question Results */}
            <div className="space-y-3">
              <h3 className="font-semibold text-slate-800 dark:text-slate-100">Question Review</h3>
              {submittedResult.questionResults.map((result, index) => (
                <div 
                  key={result.questionId}
                  className={`p-4 rounded-lg border ${result.correct ? 'border-emerald-200/60 bg-emerald-500/5' : 'border-red-200/60 bg-red-500/5'}`}
                >
                  <div className="flex items-start gap-3">
                    {result.correct ? (
                      <CheckCircle2 className="w-5 h-5 text-emerald-500 flex-shrink-0 mt-0.5" />
                    ) : (
                      <XCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
                    )}
                    <div className="flex-1">
                      <p className="font-medium text-slate-800 dark:text-slate-100">{result.questionText}</p>
                      {!result.correct && (
                        <div className="mt-2 space-y-1 text-sm">
                          <p className="text-red-600">Your answer: Option {String.fromCharCode(65 + parseInt(result.selectedAnswer))}</p>
                          <p className="text-green-600">Correct: {result.correctAnswer}</p>
                        </div>
                      )}
                      {result.explanation && (
                        <p className="mt-2 text-sm text-slate-600 dark:text-slate-200 bg-white/50 p-2 rounded">
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
              <div className="p-4 glass rounded-lg">
                <p className="text-slate-700 dark:text-slate-200">{submittedResult.nextRecommendation.message}</p>
              </div>
            )}

            <Button 
              onClick={() => {
                setSubmittedResult(null)
                refetchToday()
              }}
              className="btn-primary w-full"
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
    <div className="space-y-6" data-testid="practice-page">
      {/* Header */}
      <div className="page-header">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold text-white">
              Daily Practice
            </h1>
            <p className="text-white/80 mt-1">
              Strengthen your weak topics with daily micro-quizzes
            </p>
          </div>
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2 text-white/90">
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
              className="btn-outline border-white/40 bg-white/10 text-white hover:bg-white/20"
              onClick={() => setShowSettings(!showSettings)}
            >
              <Settings2 className="w-4 h-4" />
            </Button>
          </div>
        </div>
      </div>

      {/* Settings Panel */}
      {showSettings && preferences && (
        <Card className="glass-panel border border-white/40">
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
                <SelectTrigger className="input-modern">
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
                <SelectTrigger className="input-modern">
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
                <SelectTrigger className="input-modern">
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
        <Card className="glass-panel">
          <CardContent className="p-4 flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-amber-500/10 flex items-center justify-center">
              <Flame className="w-6 h-6 text-amber-500" />
            </div>
            <div>
              <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.currentStreak}</p>
              <p className="text-xs text-slate-500 dark:text-slate-300">Day Streak</p>
            </div>
          </CardContent>
        </Card>
        <Card className="glass-panel">
          <CardContent className="p-4 flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 flex items-center justify-center">
              <CheckCircle2 className="w-6 h-6 text-emerald-500" />
            </div>
            <div>
              <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.totalQuestionsCorrect}</p>
              <p className="text-xs text-slate-500 dark:text-slate-300">Questions Correct</p>
            </div>
          </CardContent>
        </Card>
        <Card className="glass-panel">
          <CardContent className="p-4 flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-sky-500/10 flex items-center justify-center">
              <Target className="w-6 h-6 text-sky-500" />
            </div>
            <div>
              <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.averageAccuracy?.toFixed(0) || 0}%</p>
              <p className="text-xs text-slate-500 dark:text-slate-300">Accuracy</p>
            </div>
          </CardContent>
        </Card>
        <Card className="glass-panel">
          <CardContent className="p-4 flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-indigo-500/10 flex items-center justify-center">
              <Trophy className="w-6 h-6 text-indigo-500" />
            </div>
            <div>
              <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.longestStreak}</p>
              <p className="text-xs text-slate-500 dark:text-slate-300">Best Streak</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="glass-panel grid grid-cols-2 w-full max-w-md p-1 mb-6">
          <TabsTrigger
            value="today"
            className="flex items-center gap-2 data-[state=active]:bg-white/70 data-[state=active]:text-slate-900 dark:data-[state=active]:bg-slate-900/80 dark:data-[state=active]:text-white"
          >
            <BookOpen className="w-4 h-4" />
            Today's Practice
          </TabsTrigger>
          <TabsTrigger
            value="history"
            className="flex items-center gap-2 data-[state=active]:bg-white/70 data-[state=active]:text-slate-900 dark:data-[state=active]:bg-slate-900/80 dark:data-[state=active]:text-white"
          >
            <History className="w-4 h-4" />
            History
          </TabsTrigger>
        </TabsList>

        <TabsContent value="today">
          {todayLoading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="w-8 h-8 animate-spin text-emerald-500" />
            </div>
          ) : !preferences?.enabled ? (
            <Card className="glass-panel border border-white/40">
              <CardContent className="p-12 text-center">
                <Bell className="w-12 h-12 mx-auto text-slate-400 mb-4" />
                <h3 className="text-lg font-semibold text-slate-800 dark:text-white mb-2">Daily Practice is Disabled</h3>
                <p className="text-slate-500 dark:text-slate-300 mb-4">Enable daily practice to get personalized micro-quizzes based on your weak topics.</p>
                <Button className="btn-primary" onClick={togglePracticeEnabled}>
                  Enable Daily Practice
                </Button>
              </CardContent>
            </Card>
          ) : todayPractice?.items.length === 0 ? (
            <Card className="glass-panel border border-white/40">
              <CardContent className="p-12 text-center">
                <CheckCircle2 className="w-12 h-12 mx-auto text-emerald-500 mb-4" />
                <h3 className="text-lg font-semibold text-slate-800 dark:text-white mb-2">No Practice Items Today</h3>
                <p className="text-slate-500 dark:text-slate-300">Great job! You've mastered your topics or practice items will be generated soon.</p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {/* Progress Summary */}
              {todayPractice && (
                <Card className="glass-panel border border-white/40">
                  <CardContent className="p-4">
                    <div className="flex items-center justify-between mb-2">
                      <span className="font-medium text-slate-800 dark:text-white">Today's Progress</span>
                      <span className="text-sm text-slate-500 dark:text-slate-300">
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
                  className={`glass-panel border border-white/40 transition-all ${
                    item.status === 'COMPLETED' 
                      ? 'border-emerald-200/60 bg-emerald-500/5' 
                      : item.status === 'SKIPPED'
                        ? 'opacity-70'
                        : 'hover-lift'
                  }`}
                >
                  <CardContent className="p-4">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-4">
                        <div className={`w-12 h-12 rounded-full flex items-center justify-center ${
                          item.status === 'COMPLETED' 
                            ? 'bg-emerald-500/10' 
                            : item.status === 'SKIPPED'
                              ? 'bg-slate-500/10'
                              : 'bg-emerald-500/10'
                        }`}>
                          {item.status === 'COMPLETED' ? (
                            <CheckCircle2 className="w-6 h-6 text-emerald-500" />
                          ) : item.status === 'SKIPPED' ? (
                            <SkipForward className="w-6 h-6 text-slate-400" />
                          ) : (
                            <BookOpen className="w-6 h-6 text-emerald-500" />
                          )}
                        </div>
                        <div>
                          <h3 className="font-semibold text-slate-900 dark:text-white">{item.topicName}</h3>
                          <p className="text-sm text-slate-500 dark:text-slate-300">{item.subjectName}</p>
                        </div>
                      </div>
                      <div className="flex items-center gap-3">
                        {item.status === 'COMPLETED' && item.score !== undefined && (
                          <Badge variant="secondary" className="bg-emerald-100 text-emerald-700">
                            {item.score.toFixed(0)}%
                          </Badge>
                        )}
                        {item.status === 'PENDING' && (
                          <div className="flex gap-2">
                            <Button
                              variant="outline"
                              size="sm"
                              className="btn-outline h-9 px-3"
                              onClick={() => handleSkipPractice(item.id)}
                              disabled={skipPracticeMutation.isPending}
                            >
                              <SkipForward className="w-4 h-4" />
                            </Button>
                          <Button
                            onClick={() => handleStartPractice(item.id)}
                            disabled={startPracticeMutation.isPending}
                            className="btn-primary h-9 px-4 text-sm"
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
                            className="btn-outline h-9 px-4 text-sm"
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
              <Loader2 className="w-8 h-8 animate-spin text-emerald-500" />
            </div>
          ) : !history?.days?.length ? (
            <Card className="glass-panel border border-white/40">
              <CardContent className="p-12 text-center">
                <History className="w-12 h-12 mx-auto text-slate-400 mb-4" />
                <h3 className="text-lg font-semibold text-slate-800 dark:text-white mb-2">No Practice History</h3>
                <p className="text-slate-500 dark:text-slate-300">Complete some daily practice sessions to see your history here.</p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-6">
              {history.days.map((day: any) => (
                <Card key={day.date} className="glass-panel border border-white/40 overflow-hidden">
                  <CardHeader className="border-b border-white/20 py-3">
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
                          <Badge className="bg-emerald-100 text-emerald-700">
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
                          className="flex items-center justify-between py-2 border-b border-white/10 last:border-0"
                        >
                          <div className="flex items-center gap-3">
                            {item.status === 'COMPLETED' ? (
                              <CheckCircle2 className="w-4 h-4 text-green-500" />
                            ) : (
                              <XCircle className="w-4 h-4 text-slate-400" />
                            )}
                            <span className="text-sm">{item.topicName}</span>
                          </div>
                          {item.score !== undefined && (
                            <span className="text-sm font-medium text-slate-600 dark:text-slate-200">
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

