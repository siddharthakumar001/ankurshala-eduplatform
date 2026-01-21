'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { Input } from '@/components/ui/input';
import { Switch } from '@/components/ui/switch';
import { 
  Brain, 
  Target, 
  Clock, 
  Play, 
  StopCircle,
  CheckCircle,
  Settings,
  Zap,
  Timer,
  Send,
  Sparkles,
  TrendingUp,
  Award
} from 'lucide-react';
import { studentAPI, contentAPI } from '@/lib/apiClient';
import { toast } from 'sonner';
import { StudentRoute } from '@/components/route-guard';

interface FocusSettings {
  focusEnabled: boolean;
  defaultSprintMinutes: number;
  languagePref: string;
  reminderEnabled: boolean;
  soundEnabled: boolean;
}

interface FocusSession {
  id: number;
  topicId: number;
  topicTitle: string;
  subjectName: string;
  goalText: string;
  sprintMinutes: number;
  status: 'ACTIVE' | 'PAUSED' | 'ENDED' | 'ABANDONED';
  currentStep: number;
  stepsCompleted: number;
  questionsAnswered: number;
  questionsCorrect: number;
  startedAt: string;
  checkins: Checkin[];
}

interface Checkin {
  id: number;
  stepNumber: number;
  explanation: string;
  question: string;
  questionType: string;
  options?: string[];
  studentResponseText?: string;
  responseCorrect?: boolean;
  feedback?: string;
}

interface FocusStats {
  totalSessions: number;
  completedSessions: number;
  totalFocusTimeMinutes: number;
  totalQuestionsAnswered: number;
  totalQuestionsCorrect: number;
  averageAccuracy: number;
}

interface Topic {
  id: number;
  title: string;
}

interface Chapter {
  id: number;
  name: string;
}

interface Subject {
  id: number;
  name: string;
}

export default function StudentFocusPage() {
  return (
    <StudentRoute>
      <FocusContent />
    </StudentRoute>
  );
}

function FocusContent() {
  const [settings, setSettings] = useState<FocusSettings | null>(null);
  const [activeSession, setActiveSession] = useState<FocusSession | null>(null);
  const [stats, setStats] = useState<FocusStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [showStartModal, setShowStartModal] = useState(false);
  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [isStarting, setIsStarting] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  // Start session form state
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [chapters, setChapters] = useState<Chapter[]>([]);
  const [topics, setTopics] = useState<Topic[]>([]);
  const [selectedSubject, setSelectedSubject] = useState<number | null>(null);
  const [selectedChapter, setSelectedChapter] = useState<number | null>(null);
  const [selectedTopic, setSelectedTopic] = useState<number | null>(null);
  const [goalText, setGoalText] = useState('');
  const [sprintMinutes, setSprintMinutes] = useState(15);
  
  // Active session state
  const [currentCheckin, setCurrentCheckin] = useState<Checkin | null>(null);
  const [responseText, setResponseText] = useState('');
  const [elapsedTime, setElapsedTime] = useState(0);

  useEffect(() => {
    loadInitialData();
  }, []);

  // Timer effect for active session
  useEffect(() => {
    let interval: NodeJS.Timeout;
    if (activeSession?.status === 'ACTIVE' && activeSession.startedAt) {
      interval = setInterval(() => {
        const start = new Date(activeSession.startedAt).getTime();
        const now = Date.now();
        setElapsedTime(Math.floor((now - start) / 1000));
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [activeSession]);

  const loadInitialData = async () => {
    try {
      setIsLoading(true);
      const [settingsRes, activeRes, statsRes] = await Promise.all([
        studentAPI.getFocusSettings(),
        studentAPI.getActiveFocusSession(),
        studentAPI.getFocusStats()
      ]);
      
      setSettings(settingsRes.data);
      setActiveSession(activeRes.data);
      setStats(statsRes.data);
      
      if (activeRes.data?.checkins?.length > 0) {
        setCurrentCheckin(activeRes.data.checkins[activeRes.data.checkins.length - 1]);
      }
    } catch (error) {
      console.error('Failed to load focus data:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const loadSubjects = async () => {
    try {
      const boardsResponse = await contentAPI.getBoards();
      const cbseBoard = boardsResponse.data?.find((b: any) => b.name === 'CBSE');
      if (cbseBoard) {
        const gradesResponse = await contentAPI.getGradesByBoard(cbseBoard.id);
        if (gradesResponse.data?.length > 0) {
          const subjectsResponse = await contentAPI.getSubjectsByGrade(gradesResponse.data[0].id);
          setSubjects(subjectsResponse.data || []);
        }
      }
    } catch (error) {
      console.error('Failed to load subjects:', error);
    }
  };

  const handleSubjectChange = async (subjectId: number) => {
    setSelectedSubject(subjectId);
    setSelectedChapter(null);
    setSelectedTopic(null);
    setTopics([]);
    try {
      const response = await contentAPI.getChaptersBySubject(subjectId);
      setChapters(response.data || []);
    } catch (error) {
      console.error('Failed to load chapters:', error);
    }
  };

  const handleChapterChange = async (chapterId: number) => {
    setSelectedChapter(chapterId);
    setSelectedTopic(null);
    try {
      const response = await contentAPI.getTopicsByChapter(chapterId);
      setTopics(response.data || []);
    } catch (error) {
      console.error('Failed to load topics:', error);
    }
  };

  const handleOpenStartModal = () => {
    setShowStartModal(true);
    loadSubjects();
  };

  const handleStartSession = async () => {
    if (!selectedTopic || !goalText.trim()) {
      toast.error('Please select a topic and enter your goal');
      return;
    }

    try {
      setIsStarting(true);
      const response = await studentAPI.startFocusSession({
        topicId: selectedTopic,
        goalText: goalText.trim(),
        sprintMinutes,
        language: settings?.languagePref || 'en'
      });
      
      setActiveSession(response.data);
      if (response.data?.checkins?.length > 0) {
        setCurrentCheckin(response.data.checkins[0]);
      }
      setShowStartModal(false);
      setGoalText('');
      toast.success('Focus session started!');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to start session');
    } finally {
      setIsStarting(false);
    }
  };

  const handleSubmitResponse = async () => {
    if (!activeSession || !currentCheckin || !responseText.trim()) return;

    try {
      setIsSubmitting(true);
      const response = await studentAPI.submitFocusCheckin(
        activeSession.id,
        currentCheckin.stepNumber,
        responseText.trim()
      );

      if (response.data.sessionComplete) {
        setActiveSession(null);
        setCurrentCheckin(null);
        toast.success('Focus session completed! Great work!');
        loadInitialData();
      } else if (response.data.nextCheckin) {
        setCurrentCheckin(response.data.nextCheckin);
        setActiveSession(prev => prev ? {
          ...prev,
          stepsCompleted: (prev.stepsCompleted || 0) + 1,
          questionsAnswered: (prev.questionsAnswered || 0) + 1,
          questionsCorrect: response.data.isCorrect ? (prev.questionsCorrect || 0) + 1 : prev.questionsCorrect
        } : null);
      }

      setResponseText('');
      
      if (response.data.isCorrect) {
        toast.success('Correct! ' + (response.data.feedback || ''));
      } else {
        toast.info(response.data.feedback || 'Keep trying!');
      }
    } catch (error) {
      toast.error('Failed to submit response');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEndSession = async () => {
    if (!activeSession) return;

    try {
      await studentAPI.endFocusSession(activeSession.id);
      setActiveSession(null);
      setCurrentCheckin(null);
      toast.success('Session ended');
      loadInitialData();
    } catch (error) {
      toast.error('Failed to end session');
    }
  };

  const handleUpdateSettings = async (updates: Partial<FocusSettings>) => {
    try {
      const response = await studentAPI.updateFocusSettings(updates);
      setSettings(response.data);
      toast.success('Settings updated');
    } catch (error) {
      toast.error('Failed to update settings');
    }
  };

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="skeleton h-48 rounded-3xl"></div>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="skeleton h-24 rounded-2xl"></div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="page-header">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold mb-2 flex items-center gap-3">
              <Brain className="h-8 w-8" />
              Focus Mode
            </h1>
            <p className="text-white/80">
              AI-powered study sprints to help you stay focused and learn effectively
            </p>
          </div>
            <div className="flex items-center gap-3">
              <Button 
                variant="outline"
                className="btn-outline border-white/40 bg-white/10 text-white hover:bg-white/20"
                onClick={() => setShowSettingsModal(true)}
              >
                <Settings className="h-4 w-4 mr-2" />
                Settings
              </Button>
            {!activeSession && (
              <Button 
                onClick={handleOpenStartModal}
                className="btn-primary"
              >
                <Play className="h-5 w-5 mr-2" />
                Start Sprint
              </Button>
            )}
          </div>
        </div>
      </div>

      {/* Active Session */}
      {activeSession && (
        <Card className="glass-panel border border-white/40">
          <CardHeader className="border-b border-white/20">
            <div className="flex items-center justify-between">
              <div>
                <Badge className="badge-success mb-2">
                  <Zap className="h-3 w-3 mr-1" /> ACTIVE
                </Badge>
                <CardTitle className="text-xl">{activeSession.topicTitle}</CardTitle>
                <CardDescription>{activeSession.goalText}</CardDescription>
              </div>
              <div className="text-right">
                <div className="text-3xl font-mono font-bold text-emerald-500 dark:text-emerald-300">
                  {formatTime(elapsedTime)}
                </div>
                <p className="text-sm text-slate-500 dark:text-slate-300">of {activeSession.sprintMinutes} min</p>
              </div>
            </div>
          </CardHeader>
          <CardContent className="p-6">
            <div className="mb-6">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-slate-600 dark:text-slate-200">Progress</span>
                <span className="text-sm text-slate-500 dark:text-slate-300">
                  Step {activeSession.currentStep} / {activeSession.stepsCompleted} completed
                </span>
              </div>
              <Progress 
                value={(elapsedTime / (activeSession.sprintMinutes * 60)) * 100} 
                className="h-3"
              />
            </div>

            {currentCheckin && (
              <div className="space-y-4">
                <div className="glass rounded-xl p-4">
                  <h4 className="font-semibold text-slate-900 dark:text-white mb-2">
                    Step {currentCheckin.stepNumber}
                  </h4>
                  <p className="text-slate-700 dark:text-slate-200">{currentCheckin.explanation}</p>
                </div>

                <div className="glass-panel rounded-xl p-4">
                  <h4 className="font-semibold text-slate-900 dark:text-white mb-3 flex items-center gap-2">
                    <Target className="h-4 w-4 text-emerald-500" />
                    Check-in Question
                  </h4>
                  <p className="text-slate-700 dark:text-slate-200 mb-4">{currentCheckin.question}</p>

                  <div className="flex gap-2">
                    <Input
                      placeholder="Type your answer..."
                      value={responseText}
                      onChange={(e) => setResponseText(e.target.value)}
                      onKeyDown={(e) => e.key === 'Enter' && handleSubmitResponse()}
                      className="input-modern flex-1"
                    />
                    <Button 
                      onClick={handleSubmitResponse}
                      disabled={!responseText.trim() || isSubmitting}
                      className="btn-primary"
                    >
                      {isSubmitting ? (
                        <Timer className="h-4 w-4 animate-spin" />
                      ) : (
                        <Send className="h-4 w-4" />
                      )}
                    </Button>
                  </div>
                </div>
              </div>
            )}

            <div className="flex justify-end mt-6">
              <Button 
                variant="outline" 
                className="btn-outline text-red-600 hover:text-red-700"
                onClick={handleEndSession}
              >
                <StopCircle className="h-4 w-4 mr-2" />
                End Session
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Stats Cards */}
      {stats && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <Card className="glass-panel">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="h-10 w-10 bg-emerald-500/10 rounded-lg flex items-center justify-center">
                <Target className="h-5 w-5 text-emerald-500" />
              </div>
              <div>
                <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.completedSessions}</p>
                <p className="text-xs text-slate-500 dark:text-slate-300">Sessions</p>
              </div>
            </CardContent>
          </Card>
          <Card className="glass-panel">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="h-10 w-10 bg-sky-500/10 rounded-lg flex items-center justify-center">
                <Clock className="h-5 w-5 text-sky-500" />
              </div>
              <div>
                <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.totalFocusTimeMinutes}</p>
                <p className="text-xs text-slate-500 dark:text-slate-300">Minutes</p>
              </div>
            </CardContent>
          </Card>
          <Card className="glass-panel">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="h-10 w-10 bg-emerald-500/10 rounded-lg flex items-center justify-center">
                <CheckCircle className="h-5 w-5 text-emerald-500" />
              </div>
              <div>
                <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.totalQuestionsCorrect}</p>
                <p className="text-xs text-slate-500 dark:text-slate-300">Correct</p>
              </div>
            </CardContent>
          </Card>
          <Card className="glass-panel">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="h-10 w-10 bg-amber-500/10 rounded-lg flex items-center justify-center">
                <TrendingUp className="h-5 w-5 text-amber-500" />
              </div>
              <div>
                <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.averageAccuracy}%</p>
                <p className="text-xs text-slate-500 dark:text-slate-300">Accuracy</p>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* How It Works */}
      {!activeSession && (
        <Card className="glass-panel">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Sparkles className="h-5 w-5 text-emerald-500" />
              How Focus Mode Works
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div className="text-center">
                <div className="h-12 w-12 bg-emerald-500/10 rounded-full flex items-center justify-center mx-auto mb-3">
                  <Target className="h-6 w-6 text-emerald-500" />
                </div>
                <h4 className="font-semibold mb-1">1. Set Goal</h4>
                <p className="text-sm text-slate-500 dark:text-slate-300">Pick a topic and define what you want to learn</p>
              </div>
              <div className="text-center">
                <div className="h-12 w-12 bg-sky-500/10 rounded-full flex items-center justify-center mx-auto mb-3">
                  <Timer className="h-6 w-6 text-sky-500" />
                </div>
                <h4 className="font-semibold mb-1">2. Sprint</h4>
                <p className="text-sm text-slate-500 dark:text-slate-300">Focus for 10-20 minutes with AI guidance</p>
              </div>
              <div className="text-center">
                <div className="h-12 w-12 bg-emerald-500/10 rounded-full flex items-center justify-center mx-auto mb-3">
                  <CheckCircle className="h-6 w-6 text-emerald-500" />
                </div>
                <h4 className="font-semibold mb-1">3. Check-ins</h4>
                <p className="text-sm text-slate-500 dark:text-slate-300">Answer quick questions to stay on track</p>
              </div>
              <div className="text-center">
                <div className="h-12 w-12 bg-amber-500/10 rounded-full flex items-center justify-center mx-auto mb-3">
                  <Award className="h-6 w-6 text-amber-500" />
                </div>
                <h4 className="font-semibold mb-1">4. Progress</h4>
                <p className="text-sm text-slate-500 dark:text-slate-300">Build mastery and track improvements</p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Start Session Modal */}
      {showStartModal && (
        <div className="modal-overlay p-4">
          <Card className="modal-content">
            <CardHeader className="border-b border-white/20">
              <CardTitle className="flex items-center gap-2">
                <Play className="h-5 w-5 text-emerald-500" />
                Start Focus Sprint
              </CardTitle>
              <CardDescription>
                Set your learning goal and start a focused study session
              </CardDescription>
            </CardHeader>
            <CardContent className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1">Subject</label>
                <select
                  className="input-modern"
                  value={selectedSubject || ''}
                  onChange={(e) => handleSubjectChange(Number(e.target.value))}
                >
                  <option value="">Select Subject</option>
                  {subjects.map((subject) => (
                    <option key={subject.id} value={subject.id}>{subject.name}</option>
                  ))}
                </select>
              </div>

              {selectedSubject && (
                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1">Chapter</label>
                  <select
                    className="input-modern"
                    value={selectedChapter || ''}
                    onChange={(e) => handleChapterChange(Number(e.target.value))}
                  >
                    <option value="">Select Chapter</option>
                    {chapters.map((chapter) => (
                      <option key={chapter.id} value={chapter.id}>{chapter.name}</option>
                    ))}
                  </select>
                </div>
              )}

              {selectedChapter && (
                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1">Topic</label>
                  <select
                    className="input-modern"
                    value={selectedTopic || ''}
                    onChange={(e) => setSelectedTopic(Number(e.target.value))}
                  >
                    <option value="">Select Topic</option>
                    {topics.map((topic) => (
                      <option key={topic.id} value={topic.id}>{topic.title}</option>
                    ))}
                  </select>
                </div>
              )}

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1">Your Goal</label>
                <Input
                  placeholder="What do you want to learn today?"
                  value={goalText}
                  onChange={(e) => setGoalText(e.target.value)}
                  maxLength={500}
                  className="input-modern"
                />
                <p className="text-xs text-slate-400 mt-1">{goalText.length}/500</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">Sprint Duration</label>
                <div className="flex gap-2">
                  {[10, 15, 20].map((mins) => (
                    <button
                      key={mins}
                      className={`flex-1 py-2 rounded-lg border transition-all ${
                        sprintMinutes === mins 
                          ? 'border-[#0F9D58] bg-white/80 text-[#0F9D58]' 
                          : 'border-white/30 bg-white/50 text-slate-700 dark:text-slate-100 hover:border-white/60'
                      }`}
                      onClick={() => setSprintMinutes(mins)}
                    >
                      {mins} min
                    </button>
                  ))}
                </div>
              </div>
            </CardContent>
            <div className="p-4 border-t border-white/20 flex justify-end gap-2">
              <Button variant="outline" className="btn-outline" onClick={() => setShowStartModal(false)}>
                Cancel
              </Button>
              <Button 
                onClick={handleStartSession}
                disabled={!selectedTopic || !goalText.trim() || isStarting}
                className="btn-primary"
              >
                {isStarting ? 'Starting...' : 'Start Sprint'}
              </Button>
            </div>
          </Card>
        </div>
      )}

      {/* Settings Modal */}
      {showSettingsModal && settings && (
        <div className="modal-overlay p-4">
          <Card className="modal-content max-w-md">
            <CardHeader className="border-b border-white/20">
              <CardTitle className="flex items-center gap-2">
                <Settings className="h-5 w-5 text-slate-600 dark:text-slate-200" />
                Focus Settings
              </CardTitle>
            </CardHeader>
            <CardContent className="p-6 space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium">Focus Mode Enabled</p>
                  <p className="text-sm text-slate-500 dark:text-slate-300">Enable AI focus coaching</p>
                </div>
                <Switch
                  checked={settings.focusEnabled}
                  onCheckedChange={(checked) => handleUpdateSettings({ focusEnabled: checked })}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">Default Sprint Duration</label>
                <div className="flex gap-2">
                  {[10, 15, 20].map((mins) => (
                    <button
                      key={mins}
                      className={`flex-1 py-2 rounded-lg border transition-all ${
                        settings.defaultSprintMinutes === mins 
                          ? 'border-[#0F9D58] bg-white/80 text-[#0F9D58]' 
                          : 'border-white/30 bg-white/50 text-slate-700 dark:text-slate-100 hover:border-white/60'
                      }`}
                      onClick={() => handleUpdateSettings({ defaultSprintMinutes: mins })}
                    >
                      {mins} min
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">Language</label>
                <div className="flex gap-2">
                  <button
                    className={`flex-1 py-2 rounded-lg border transition-all ${
                      settings.languagePref === 'en' 
                        ? 'border-[#0F9D58] bg-white/80 text-[#0F9D58]' 
                        : 'border-white/30 bg-white/50 text-slate-700 dark:text-slate-100 hover:border-white/60'
                    }`}
                    onClick={() => handleUpdateSettings({ languagePref: 'en' })}
                  >
                    English
                  </button>
                  <button
                    className={`flex-1 py-2 rounded-lg border transition-all ${
                      settings.languagePref === 'hi' 
                        ? 'border-[#0F9D58] bg-white/80 text-[#0F9D58]' 
                        : 'border-white/30 bg-white/50 text-slate-700 dark:text-slate-100 hover:border-white/60'
                    }`}
                    onClick={() => handleUpdateSettings({ languagePref: 'hi' })}
                  >
                    Hindi
                  </button>
                </div>
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium">Reminders</p>
                  <p className="text-sm text-slate-500 dark:text-slate-300">Get notified before sprint ends</p>
                </div>
                <Switch
                  checked={settings.reminderEnabled}
                  onCheckedChange={(checked) => handleUpdateSettings({ reminderEnabled: checked })}
                />
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium">Sound Effects</p>
                  <p className="text-sm text-slate-500 dark:text-slate-300">Play sounds for check-ins</p>
                </div>
                <Switch
                  checked={settings.soundEnabled}
                  onCheckedChange={(checked) => handleUpdateSettings({ soundEnabled: checked })}
                />
              </div>
            </CardContent>
            <div className="p-4 border-t border-white/20 flex justify-end">
              <Button className="btn-primary" onClick={() => setShowSettingsModal(false)}>
                Done
              </Button>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
}

