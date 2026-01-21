'use client';

import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { 
  Calendar,
  BookOpen,
  Target,
  Clock,
  TrendingUp,
  CheckCircle2,
  Circle,
  Sparkles,
  Play,
  FileText,
  Users,
  Zap
} from 'lucide-react';
import { studentAPI } from '@/lib/apiClient';
import { toast } from 'sonner';
import { StudentRoute } from '@/components/route-guard';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';

interface UpcomingClass {
  bookingId: number;
  teacherName: string;
  topicName: string;
  scheduledTime: string;
  companionLink: string;
}

interface WeakTopicRecommendation {
  topicId: number;
  topicName: string;
  reason: string;
  confidenceScore: number;
  prerequisiteGaps?: string[];
}

interface PracticeItem {
  practiceId: number;
  topicName: string;
  difficulty: string;
  estimatedMinutes: number;
  status: string;
}

interface ReviseNoteSuggestion {
  noteId: number;
  topicName: string;
  lastRevisedDaysAgo: number;
  priority: string;
}

interface FocusSprint {
  suggestedTopicId: number;
  suggestedTopicName: string;
  durationMinutes: number;
  reason: string;
}

interface DailyProgress {
  todayStepsCompleted: number;
  totalStepsToday: number;
  completionPercentage: number;
  currentStreak: number;
  motivationalMessage: string;
}

interface DailyPlan {
  planDate: string;
  nextClass?: UpcomingClass;
  weakTopicRecommendation?: WeakTopicRecommendation;
  practiceItems: PracticeItem[];
  reviseNoteSuggestion?: ReviseNoteSuggestion;
  focusSprint?: FocusSprint;
  progress: DailyProgress;
}

function TodayPageContent() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [completingStep, setCompletingStep] = useState<string | null>(null);

  // Fetch daily plan
  const { data: dailyPlan, isLoading } = useQuery<DailyPlan>({
    queryKey: ['dailyPlan'],
    queryFn: async () => {
      const response = await studentAPI.getDailyPlan();
      return response.data;
    },
    refetchInterval: 5 * 60 * 1000, // Refresh every 5 minutes
  });

  // Complete step mutation
  const completeStepMutation = useMutation({
    mutationFn: async ({ stepType, stepIdentifier, metadata }: { 
      stepType: string; 
      stepIdentifier: string; 
      metadata?: Record<string, any> 
    }) => {
      const response = await studentAPI.completeStep({ stepType, stepIdentifier, metadata });
      return response.data;
    },
    onSuccess: (data) => {
      toast.success(data.motivationalMessage || 'Step completed!');
      queryClient.invalidateQueries({ queryKey: ['dailyPlan'] });
      setCompletingStep(null);
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to complete step');
      setCompletingStep(null);
    },
  });

  const handleCompleteStep = (stepType: string, stepIdentifier: string, metadata?: Record<string, any>) => {
    setCompletingStep(`${stepType}-${stepIdentifier}`);
    completeStepMutation.mutate({ stepType, stepIdentifier, metadata });
  };

  const handleStartPractice = (practiceId: number) => {
    router.push('/student/practice');
  };

  const handleReviseNote = (noteId: number) => {
    router.push('/student/notes');
  };

  const handleStartFocus = (topicId: number) => {
    router.push(`/student/focus?topicId=${topicId}`);
  };

  const handleExploreWeakTopic = (topicId: number) => {
    router.push(`/student/ai-tutor?topicId=${topicId}`);
  };

  const handlePrepareForClass = (bookingId: number) => {
    router.push(`/student/bookings/${bookingId}/companion`);
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-96" data-testid="today-loading">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-500"></div>
      </div>
    );
  }

  if (!dailyPlan) {
    return (
      <div className="flex items-center justify-center h-96" data-testid="today-no-data">
        <div className="glass-panel p-8 text-center">
          <p className="text-slate-500 dark:text-slate-300 mb-4">Unable to load your daily plan</p>
          <Button className="btn-primary" onClick={() => queryClient.invalidateQueries({ queryKey: ['dailyPlan'] })}>
            Retry
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6" data-testid="today-page">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Header */}
        <div className="page-header">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-white flex items-center gap-2" data-testid="today-header">
                <Sparkles className="w-8 h-8 text-white" />
                Today's Plan
              </h1>
              <p className="text-white/80 mt-1">Your personalized learning journey for {new Date(dailyPlan.planDate).toLocaleDateString()}</p>
            </div>
          </div>
        </div>

        {/* Progress Summary */}
        <Card className="glass-panel border border-white/40" data-testid="today-progress">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <TrendingUp className="w-5 h-5 text-emerald-500" />
              Daily Progress
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-4 gap-4">
              <div>
                <p className="text-sm text-slate-500 dark:text-slate-300">Completed Today</p>
                <p className="text-2xl font-bold text-emerald-500" data-testid="progress-completed">
                  {dailyPlan.progress.todayStepsCompleted} / {dailyPlan.progress.totalStepsToday}
                </p>
              </div>
              <div>
                <p className="text-sm text-slate-500 dark:text-slate-300">Completion</p>
                <p className="text-2xl font-bold text-emerald-500" data-testid="progress-percentage">
                  {dailyPlan.progress.completionPercentage}%
                </p>
              </div>
              <div>
                <p className="text-sm text-slate-500 dark:text-slate-300">Current Streak</p>
                <p className="text-2xl font-bold text-amber-500" data-testid="progress-streak">
                  {dailyPlan.progress.currentStreak} days
                </p>
              </div>
              <div className="flex items-center">
                <p className="text-sm text-slate-600 dark:text-slate-200 italic" data-testid="progress-motivation">
                  "{dailyPlan.progress.motivationalMessage}"
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="grid lg:grid-cols-2 gap-6">
          {/* Upcoming Class */}
          {dailyPlan.nextClass && (
            <Card className="glass-panel border border-white/40" data-testid="today-next-class">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Calendar className="w-5 h-5 text-sky-500" />
                  Upcoming Class
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div>
                    <p className="text-sm text-slate-500 dark:text-slate-300">Topic</p>
                    <p className="font-semibold text-slate-900 dark:text-white" data-testid="next-class-topic">{dailyPlan.nextClass.topicName}</p>
                  </div>
                  <div>
                    <p className="text-sm text-slate-500 dark:text-slate-300">Teacher</p>
                    <p className="font-medium text-slate-900 dark:text-white" data-testid="next-class-teacher">{dailyPlan.nextClass.teacherName}</p>
                  </div>
                  <div>
                    <p className="text-sm text-slate-500 dark:text-slate-300">Time</p>
                    <p className="font-medium text-slate-900 dark:text-white" data-testid="next-class-time">
                      {new Date(dailyPlan.nextClass.scheduledTime).toLocaleString()}
                    </p>
                  </div>
                  <Button 
                    className="btn-primary w-full" 
                    onClick={() => handlePrepareForClass(dailyPlan.nextClass!.bookingId)}
                    data-testid="next-class-prepare-btn"
                  >
                    <Users className="w-4 h-4 mr-2" />
                    Prepare for Class
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Weak Topic Recommendation */}
          {dailyPlan.weakTopicRecommendation && (
            <Card className="glass-panel border border-white/40" data-testid="today-weak-topic">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Target className="w-5 h-5 text-amber-500" />
                  Recommended Focus Area
                </CardTitle>
                <CardDescription>AI-powered recommendation</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div>
                    <p className="font-semibold text-lg text-slate-900 dark:text-white" data-testid="weak-topic-name">
                      {dailyPlan.weakTopicRecommendation.topicName}
                    </p>
                    <Badge variant="outline" className="mt-1 border-white/40 bg-white/70 text-slate-700">
                      {Math.round(dailyPlan.weakTopicRecommendation.confidenceScore * 100)}% confidence
                    </Badge>
                  </div>
                  <p className="text-sm text-slate-600 dark:text-slate-200" data-testid="weak-topic-reason">
                    {dailyPlan.weakTopicRecommendation.reason}
                  </p>
                  {dailyPlan.weakTopicRecommendation.prerequisiteGaps && 
                   dailyPlan.weakTopicRecommendation.prerequisiteGaps.length > 0 && (
                    <div>
                      <p className="text-sm font-medium text-slate-700 dark:text-slate-200 mb-1">Prerequisites to review:</p>
                      <ul className="text-sm text-slate-600 dark:text-slate-300 list-disc list-inside">
                        {dailyPlan.weakTopicRecommendation.prerequisiteGaps.map((gap, idx) => (
                          <li key={idx}>{gap}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                  <Button 
                    className="btn-outline w-full" 
                    variant="outline"
                    onClick={() => handleExploreWeakTopic(dailyPlan.weakTopicRecommendation!.topicId)}
                    data-testid="weak-topic-explore-btn"
                  >
                    <Sparkles className="w-4 h-4 mr-2" />
                    Explore with AI Tutor
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}
        </div>

        {/* Practice Items */}
        {dailyPlan.practiceItems.length > 0 && (
          <Card className="glass-panel border border-white/40" data-testid="today-practice">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <BookOpen className="w-5 h-5 text-emerald-500" />
                Today's Practice ({dailyPlan.practiceItems.length})
              </CardTitle>
              <CardDescription>Spaced repetition for optimal learning</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {dailyPlan.practiceItems.map((item, idx) => (
                  <div 
                    key={item.practiceId}
                    className="flex items-center justify-between p-4 glass rounded-lg hover:bg-white/60 transition-colors"
                    data-testid={`practice-item-${idx}`}
                  >
                    <div className="flex items-center gap-3">
                      {item.status === 'COMPLETED' ? (
                        <CheckCircle2 className="w-5 h-5 text-emerald-500" />
                      ) : (
                        <Circle className="w-5 h-5 text-slate-400" />
                      )}
                      <div>
                        <p className="font-medium text-slate-900 dark:text-white" data-testid={`practice-item-${idx}-topic`}>{item.topicName}</p>
                        <div className="flex items-center gap-2 mt-1">
                          <Badge variant="secondary">{item.difficulty}</Badge>
                          <span className="text-sm text-slate-600 dark:text-slate-200 flex items-center gap-1">
                            <Clock className="w-3 h-3" />
                            {item.estimatedMinutes} min
                          </span>
                        </div>
                      </div>
                    </div>
                    {item.status !== 'COMPLETED' && (
                      <Button 
                        size="sm"
                        className="btn-primary h-9 px-4 text-sm"
                        onClick={() => handleStartPractice(item.practiceId)}
                        data-testid={`practice-item-${idx}-start-btn`}
                      >
                        <Play className="w-4 h-4 mr-1" />
                        Start
                      </Button>
                    )}
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        <div className="grid lg:grid-cols-2 gap-6">
          {/* Revise Note */}
          {dailyPlan.reviseNoteSuggestion && (
            <Card className="glass-panel border border-white/40" data-testid="today-revise-note">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <FileText className="w-5 h-5 text-emerald-500" />
                  Revise Your Notes
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div>
                    <p className="font-semibold text-slate-900 dark:text-white" data-testid="revise-note-topic">
                      {dailyPlan.reviseNoteSuggestion.topicName}
                    </p>
                    <p className="text-sm text-slate-600 dark:text-slate-200 mt-1">
                      Last revised {dailyPlan.reviseNoteSuggestion.lastRevisedDaysAgo} days ago
                    </p>
                    <Badge className="mt-2" variant={
                      dailyPlan.reviseNoteSuggestion.priority === 'HIGH' ? 'destructive' : 'secondary'
                    }>
                      {dailyPlan.reviseNoteSuggestion.priority} priority
                    </Badge>
                  </div>
                  <Button 
                    className="btn-outline w-full" 
                    variant="outline"
                    onClick={() => handleReviseNote(dailyPlan.reviseNoteSuggestion!.noteId)}
                    data-testid="revise-note-btn"
                  >
                    <FileText className="w-4 h-4 mr-2" />
                    Open Note
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Focus Sprint */}
          {dailyPlan.focusSprint && (
            <Card className="glass-panel border border-white/40" data-testid="today-focus-sprint">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Zap className="w-5 h-5 text-amber-500" />
                  Quick Focus Sprint
                </CardTitle>
                <CardDescription>{dailyPlan.focusSprint.durationMinutes}-minute deep focus session</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div>
                    <p className="font-semibold text-slate-900 dark:text-white" data-testid="focus-sprint-topic">
                      {dailyPlan.focusSprint.suggestedTopicName}
                    </p>
                    <p className="text-sm text-slate-600 dark:text-slate-200 mt-1" data-testid="focus-sprint-reason">
                      {dailyPlan.focusSprint.reason}
                    </p>
                  </div>
                  <Button 
                    className="btn-primary w-full"
                    onClick={() => handleStartFocus(dailyPlan.focusSprint!.suggestedTopicId)}
                    data-testid="focus-sprint-start-btn"
                  >
                    <Zap className="w-4 h-4 mr-2" />
                    Start {dailyPlan.focusSprint.durationMinutes}-Min Sprint
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}

export default function TodayPage() {
  return (
    <StudentRoute>
      <TodayPageContent />
    </StudentRoute>
  );
}
