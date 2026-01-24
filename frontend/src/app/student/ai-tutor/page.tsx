'use client'

import { useState, useRef, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { StudentRoute } from '@/components/route-guard'
import { studentAPI } from '@/lib/apiClient'
import { useToast } from '@/hooks/use-toast'
import { useSpeechRecognition } from '@/hooks/useSpeechRecognition'
import { useSpeechSynthesis } from '@/hooks/useSpeechSynthesis'
import { getLanguageInfo, getSupportedLanguages } from '@/lib/indianLanguages'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { 
  Send, 
  Mic, 
  MicOff, 
  Loader2, 
  BookOpen, 
  Brain, 
  Target, 
  Eye,
  Save,
  Sparkles,
  Volume2,
  VolumeX,
  Globe
} from 'lucide-react'

interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
  topicId?: number
  suggestedActions?: {
    type: 'GENERATE_NOTES' | 'START_PRACTICE' | 'START_FOCUS' | 'VIEW_TOPIC' | 'ASK_TEACHER' | 'BROWSE_TOPICS'
    label: string
    topicId?: number
    topicName?: string
  }[]
  citations?: {
    topicTitle?: string
    chapterName?: string
    subjectName?: string
    sourceType?: string
    sourceRef?: string
  }[]
}

function AITutorPageContent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { toast } = useToast()
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const textareaRef = useRef<HTMLTextAreaElement>(null)

  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [isStreaming, setIsStreaming] = useState(false)
  const [voiceMode, setVoiceMode] = useState(false)
  const [selectedLanguage, setSelectedLanguage] = useState('en')
  const [sessionId] = useState(`session-${Date.now()}`)
  const [contextTopicId, setContextTopicId] = useState<number | undefined>(undefined)
  
  // Speech recognition and synthesis hooks
  const {
    isListening,
    transcript,
    startListening,
    stopListening,
    resetTranscript,
    error: speechError
  } = useSpeechRecognition({
    continuous: true,
    language: getLanguageInfo(selectedLanguage).localeCode
  })
  
  const {
    isSpeaking,
    speak,
    stop: stopSpeaking,
    error: synthesisError
  } = useSpeechSynthesis()

  const languageInfo = getLanguageInfo(selectedLanguage)

  useEffect(() => {
    const topicIdParam = searchParams.get('topicId')
    if (topicIdParam) {
      const parsed = Number(topicIdParam)
      if (!Number.isNaN(parsed)) {
        setContextTopicId(parsed)
      }
    }
  }, [searchParams])

  // Load student profile on mount to get language preference and personalization data
  useEffect(() => {
    const loadStudentProfile = async () => {
      try {
        const profile = await studentAPI.getProfile()
        
        // Set language from profile if available
        if (profile?.language) {
          // Extract language code (e.g., 'hi-IN' -> 'hi')
          const langCode = profile.language.split('-')[0]
          if (getSupportedLanguages().some(l => l.code === langCode)) {
            setSelectedLanguage(langCode)
          }
        }
      } catch (error) {
        console.error('Failed to load student profile:', error)
        // Don't show error toast - profile might not be complete yet
      }
    }
    loadStudentProfile()
  }, [])

  // Auto-scroll to bottom when messages change
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  // Update input when speech recognition transcript changes
  useEffect(() => {
    if (voiceMode && transcript) {
      setInput(transcript)
    }
  }, [transcript, voiceMode])

  // Handle speech recognition errors
  useEffect(() => {
    if (speechError) {
      toast({
        title: 'Speech Recognition Error',
        description: speechError,
        variant: 'destructive'
      })
    }
  }, [speechError, toast])

  // Handle speech synthesis errors
  useEffect(() => {
    if (synthesisError) {
      toast({
        title: 'Speech Synthesis Error',
        description: synthesisError,
        variant: 'destructive'
      })
    }
  }, [synthesisError, toast])

  const normalizeSuggestedActions = (actions: any | undefined) => {
    if (!Array.isArray(actions) || actions.length === 0) return []

    return actions.map((action) => {
      const actionType = action.type || action.actionType
      const actionLabel = action.label || action.actionLabel
      const actionData = action.actionData || {}
      const topicId = action.topicId || actionData.topicId

      switch (actionType) {
        case 'GENERATE_NOTES':
          return { type: 'GENERATE_NOTES', label: actionLabel || 'Generate notes', topicId }
        case 'START_FOCUS':
          return { type: 'START_FOCUS', label: actionLabel || 'Start focus session', topicId }
        case 'START_PRACTICE':
        case 'TAKE_QUIZ':
          return { type: 'START_PRACTICE', label: actionLabel || 'Practice this topic', topicId }
        case 'VIEW_TOPIC':
          return { type: 'VIEW_TOPIC', label: actionLabel || 'View topic details', topicId }
        case 'ASK_TEACHER':
          return { type: 'ASK_TEACHER', label: actionLabel || 'Ask a teacher', topicId }
        case 'BROWSE_TOPICS':
          return { type: 'BROWSE_TOPICS', label: actionLabel || 'Browse topics' }
        default:
          return null
      }
    }).filter(Boolean) as Message['suggestedActions']
  }

  const normalizeCitations = (references: any[] | undefined) => {
    if (!references || references.length === 0) return []

    return references.map((reference) => ({
      topicTitle: reference.topicTitle,
      chapterName: reference.chapterName,
      subjectName: reference.subjectName,
      sourceType: reference.sourceType,
      sourceRef: reference.sourceRef
    }))
  }

  const handleSendMessage = async () => {
    if (!input.trim() || isStreaming) return

    const userMessage: Message = {
      id: `user-${Date.now()}`,
      role: 'user',
      content: input.trim(),
      timestamp: new Date()
    }

    setMessages(prev => [...prev, userMessage])
    setInput('')
    setIsStreaming(true)

    // Create assistant message placeholder
    const assistantMessageId = `assistant-${Date.now()}`
    setMessages(prev => [...prev, {
      id: assistantMessageId,
      role: 'assistant',
      content: '',
      timestamp: new Date()
    }])

    try {
      let fullContent = ''
      const activeTopicId = contextTopicId
      
      await studentAPI.streamChatMessage(
        userMessage.content,
        (chunk) => {
          fullContent += chunk
          setMessages(prev => prev.map(msg => 
            msg.id === assistantMessageId 
              ? { ...msg, content: fullContent }
              : msg
          ))
        },
        sessionId,
        activeTopicId,
        undefined,
        getLanguageInfo(selectedLanguage).localeCode
      )

      // After streaming completes, fetch full response with actions/citations
      const fullResponse = await studentAPI.sendChatMessage(
        userMessage.content, 
        sessionId,
        activeTopicId,
        undefined,
        getLanguageInfo(selectedLanguage).localeCode
      )
      const normalizedActions = normalizeSuggestedActions(fullResponse?.suggestedActions)
      const normalizedCitations = normalizeCitations(fullResponse?.references)
      const messageTopicId = normalizedActions.find((action) => action?.topicId)?.topicId ?? activeTopicId
      
      setMessages(prev => prev.map(msg => 
        msg.id === assistantMessageId 
          ? {
              ...msg,
              content: fullResponse?.message || fullContent,
              suggestedActions: normalizedActions,
              citations: normalizedCitations,
              topicId: messageTopicId
            }
          : msg
      ))

      // Auto-speak assistant response in voice mode
      if (voiceMode && (fullResponse?.message || fullContent)) {
        const langInfo = getLanguageInfo(selectedLanguage)
        speak(fullResponse?.message || fullContent, { 
          rate: 0.9, 
          pitch: 1.0,
          voice: langInfo.femaleVoice
        })
      }

    } catch (error) {
      console.error('Chat error:', error)
      toast({
        title: 'Error',
        description: 'Failed to send message. Please try again.',
        variant: 'destructive'
      })
      // Remove assistant placeholder on error
      setMessages(prev => prev.filter(msg => msg.id !== assistantMessageId))
    } finally {
      setIsStreaming(false)
      textareaRef.current?.focus()
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSendMessage()
    }
  }

  const handleSuggestedAction = async (action: Message['suggestedActions'][0]) => {
    switch (action.type) {
      case 'GENERATE_NOTES':
        if (!action.topicId) {
          toast({
            title: 'Topic not found',
            description: 'Select a topic-based suggestion to generate notes.',
            variant: 'destructive'
          })
          return
        }
        try {
          await studentAPI.generateNotes(action.topicId, 'SHORT', selectedLanguage)
          router.push('/student/notes')
        } catch (error) {
          console.error('Generate notes error:', error)
          toast({
            title: 'Failed to generate notes',
            description: 'Please try again in a moment.',
            variant: 'destructive'
          })
        }
        break
      case 'START_PRACTICE':
        router.push(`/student/practice?topicId=${action.topicId}`)
        break
      case 'START_FOCUS':
        router.push(`/student/focus?topicId=${action.topicId}`)
        break
      case 'VIEW_TOPIC':
        router.push(`/student/discover?topicId=${action.topicId}`)
        break
      case 'ASK_TEACHER':
        router.push('/student/booking')
        break
      case 'BROWSE_TOPICS':
        router.push('/student/discover')
        break
      default:
        toast({ title: 'Action', description: `Action: ${action.label}` })
    }
  }

  const handleSaveAsNotes = async (message: Message) => {
    try {
      if (!message.topicId) {
        toast({
          title: 'Topic not found',
          description: 'Pick a suggested action to anchor notes to a topic.',
          variant: 'destructive'
        })
        return
      }

      toast({
        title: 'Saving...',
        description: 'Creating notes from this conversation.'
      })

      await studentAPI.generateNotes(message.topicId, 'SHORT', selectedLanguage)
      router.push('/student/notes')
    } catch (error) {
      console.error('Save notes error:', error)
      toast({
        title: 'Error',
        description: 'Failed to save as notes.',
        variant: 'destructive'
      })
    }
  }

  const toggleVoiceMode = () => {
    const newVoiceMode = !voiceMode
    setVoiceMode(newVoiceMode)
    
    if (newVoiceMode) {
      // Start listening when voice mode is enabled
      try {
        startListening()
        toast({
          title: 'Voice Mode On',
          description: 'Listening... Speak your question'
        })
      } catch (error) {
        toast({
          title: 'Voice Mode Unavailable',
          description: 'Speech recognition is not supported in your browser',
          variant: 'destructive'
        })
        setVoiceMode(false)
      }
    } else {
      // Stop listening and speaking when voice mode is disabled
      stopListening()
      stopSpeaking()
      resetTranscript()
      toast({
        title: 'Voice Mode Off',
        description: 'Switched to text input'
      })
    }
  }

  const getActionIcon = (type: string) => {
    switch (type) {
      case 'GENERATE_NOTES': return <BookOpen className="h-4 w-4" />
      case 'START_PRACTICE': return <Brain className="h-4 w-4" />
      case 'START_FOCUS': return <Target className="h-4 w-4" />
      case 'VIEW_TOPIC': return <Eye className="h-4 w-4" />
      case 'ASK_TEACHER': return <Globe className="h-4 w-4" />
      case 'BROWSE_TOPICS': return <BookOpen className="h-4 w-4" />
      default: return <Sparkles className="h-4 w-4" />
    }
  }

  return (
    <div className="space-y-6 max-w-6xl mx-auto px-4 pb-10" data-testid="ai-tutor-page">
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-emerald-600 via-emerald-500 to-sky-600 text-white shadow-lg">
        <div className="absolute inset-0 bg-white/10 blur-3xl" />
        <div className="relative px-6 py-6 sm:px-8 sm:py-8 space-y-3" data-testid="ai-tutor-header">
          <div className="flex flex-wrap items-center gap-2 text-sm uppercase tracking-[0.14em] text-white/80">
            <span>Student Workspace</span>
            <span className="opacity-70">•</span>
            <span>AI Companion</span>
          </div>
          <div className="flex flex-col gap-3">
            <h1 className="text-3xl font-bold">AI Tutor</h1>
            <p className="text-white/80">Ask questions, get explanations, and learn interactively.</p>
            <div className="flex flex-wrap items-center gap-3">
              <Badge className="bg-white/20 text-white border border-white/30 px-3 py-1 rounded-full">
                <Sparkles className="h-3 w-3 mr-1" />
                Personalized learning assistant
              </Badge>
              <Badge variant="outline" className="border-white/30 text-white bg-white/10">
                Voice {isListening ? 'On' : 'Off'}
              </Badge>
              <Badge variant="outline" className="border-white/30 text-white bg-white/10">
                Language: {languageInfo.englishName}
              </Badge>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-5xl mx-auto space-y-6">
        <Card className="glass-panel border border-white/40" data-testid="ai-tutor-chat-container">
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle>Chat with AI Tutor</CardTitle>
                <CardDescription>
                  Ask about any topic, concept, or problem. I&apos;ll provide explanations with relevant examples.
                </CardDescription>
              </div>
              <div className="flex gap-2">
                <Button
                  variant={voiceMode ? 'default' : 'outline'}
                  size="sm"
                  onClick={toggleVoiceMode}
                  className={voiceMode ? 'btn-primary h-9 px-4 text-sm' : 'btn-outline h-9 px-4 text-sm'}
                  data-testid="ai-tutor-voice-toggle"
                >
                  {isListening ? (
                    <Mic className="h-4 w-4 mr-2 animate-pulse" />
                  ) : (
                    <MicOff className="h-4 w-4 mr-2" />
                  )}
                  {voiceMode ? (isListening ? 'Listening...' : 'Voice On') : 'Voice Off'}
                </Button>
                {isSpeaking && (
                  <Button
                    variant="outline"
                    size="sm"
                    className="btn-outline h-9 px-4 text-sm"
                    onClick={stopSpeaking}
                    data-testid="ai-tutor-stop-speaking"
                  >
                    <VolumeX className="h-4 w-4 mr-2" />
                    Stop
                  </Button>
                )}
              </div>
            </div>
          </CardHeader>
          <CardContent>
            {/* Messages */}
            <div 
              className="h-[500px] overflow-y-auto mb-4 space-y-4 p-4 glass rounded-2xl"
              data-testid="ai-tutor-messages"
            >
              {messages.length === 0 && (
                <div className="text-center text-slate-500 dark:text-slate-300 mt-20" data-testid="ai-tutor-empty">
                  <Sparkles className="h-12 w-12 mx-auto mb-4 text-emerald-400" />
                  <p className="text-lg font-medium">Start a conversation</p>
                  <p className="text-sm">Ask me anything about your subjects</p>
                </div>
              )}

              {messages.map((message) => (
                <div
                  key={message.id}
                  data-testid={`ai-tutor-message-${message.role}`}
                  className={`flex ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}
                >
                  <div
                    className={`max-w-[80%] rounded-lg p-4 ${
                      message.role === 'user'
                        ? 'bg-emerald-500 text-white'
                        : 'glass border border-white/40 text-slate-900 dark:text-slate-100'
                    }`}
                  >
                    <div className="whitespace-pre-wrap">{message.content}</div>
                    
                    {/* Citations */}
                    {message.citations && message.citations.length > 0 && (
                      <div className="mt-3 pt-3 border-t border-white/20">
                        <p className="text-xs font-semibold text-slate-600 dark:text-slate-300 mb-2">Sources:</p>
                        <div className="space-y-1">
                          {message.citations.map((citation, idx) => (
                            <div 
                              key={`${citation.topicTitle || 'source'}-${idx}`}
                              className="text-xs text-slate-700 dark:text-slate-200 bg-white/70 p-2 rounded border border-white/40"
                              data-testid={`ai-tutor-citation-${idx}`}
                            >
                              <span className="font-medium">{citation.topicTitle || 'Curriculum Source'}</span>
                              {citation.chapterName && (
                                <span className="text-slate-500 dark:text-slate-300"> - {citation.chapterName}</span>
                              )}
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Suggested Actions */}
                    {message.suggestedActions && message.suggestedActions.length > 0 && (
                      <div className="mt-3 pt-3 border-t border-white/20">
                        <p className="text-xs font-semibold text-slate-600 dark:text-slate-300 mb-2">Suggested Actions:</p>
                        <div className="flex flex-wrap gap-2">
                          {message.suggestedActions.map((action, idx) => (
                            <Button
                              key={idx}
                              size="sm"
                              variant="outline"
                              onClick={() => handleSuggestedAction(action)}
                              className="btn-outline h-8 px-3 text-xs"
                              data-testid={`ai-tutor-action-${action.type.toLowerCase()}`}
                            >
                              {getActionIcon(action.type)}
                              <span className="ml-1">{action.label}</span>
                            </Button>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Save as Notes Button */}
                    {message.role === 'assistant' && message.content && (
                      <div className="mt-2">
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => handleSaveAsNotes(message)}
                          className="text-xs text-slate-500 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white"
                          data-testid="ai-tutor-save-notes"
                        >
                          <Save className="h-3 w-3 mr-1" />
                          Save as Notes
                        </Button>
                      </div>
                    )}

                    <p className="text-xs mt-2 opacity-70">
                      {message.timestamp.toLocaleTimeString()}
                    </p>
                  </div>
                </div>
              ))}

              {/* Streaming indicator */}
              {isStreaming && messages[messages.length - 1]?.role === 'assistant' && (
                <div className="flex justify-start">
                  <div className="glass rounded-lg p-4 border border-white/40">
                    <Loader2 className="h-4 w-4 animate-spin text-emerald-500" />
                  </div>
                </div>
              )}

              <div ref={messagesEndRef} />
            </div>

            <Separator className="mb-4" />

            {/* Input */}
            <div className="flex gap-2" data-testid="ai-tutor-input-container">
              <Textarea
                ref={textareaRef}
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder={voiceMode ? (isListening ? "Listening... speak your question" : "Click mic to start listening") : "Ask a question..."}
                className="input-modern min-h-[80px] resize-none"
                disabled={isStreaming}
                data-testid="ai-tutor-input"
              />
              <Button
                onClick={handleSendMessage}
                disabled={!input.trim() || isStreaming}
                size="lg"
                className="btn-primary"
                data-testid="ai-tutor-send"
              >
                {isStreaming ? (
                  <Loader2 className="h-5 w-5 animate-spin" />
                ) : (
                  <Send className="h-5 w-5" />
                )}
              </Button>
            </div>

            {/* Quick Tips */}
            <div className="mt-4 text-sm text-slate-600 dark:text-slate-200" data-testid="ai-tutor-tips">
              <p className="font-medium mb-1">Tips:</p>
              <ul className="list-disc list-inside space-y-1 text-xs">
                <li>Ask specific questions about topics you&apos;re studying</li>
                <li>Request explanations with examples</li>
                <li>Use suggested actions to generate notes, start practice, or focus sessions</li>
                <li>Use voice mode when your device supports speech input</li>
              </ul>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

export default function AITutorPage() {
  return (
    <StudentRoute>
      <AITutorPageContent />
    </StudentRoute>
  )
}
