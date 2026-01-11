'use client'

import { useState, useRef, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import StudentRoute from '@/components/StudentRoute'
import { studentAPI } from '@/lib/apiClient'
import { useToast } from '@/hooks/use-toast'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
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
  Sparkles
} from 'lucide-react'

interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
  suggestedActions?: {
    type: 'GENERATE_NOTES' | 'START_PRACTICE' | 'START_FOCUS' | 'VIEW_TOPIC'
    label: string
    topicId?: number
    topicName?: string
  }[]
  citations?: {
    chunkId: number
    content: string
    topicName: string
    chapterName: string
  }[]
}

function AITutorPageContent() {
  const router = useRouter()
  const { toast } = useToast()
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const textareaRef = useRef<HTMLTextAreaElement>(null)

  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [isStreaming, setIsStreaming] = useState(false)
  const [voiceMode, setVoiceMode] = useState(false)
  const [sessionId] = useState(`session-${Date.now()}`)

  // Auto-scroll to bottom when messages change
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

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
        sessionId
      )

      // After streaming completes, fetch full response with actions/citations
      const fullResponse = await studentAPI.sendChatMessage(userMessage.content, sessionId)
      
      setMessages(prev => prev.map(msg => 
        msg.id === assistantMessageId 
          ? {
              ...msg,
              content: fullResponse.answer,
              suggestedActions: fullResponse.suggestedActions,
              citations: fullResponse.references
            }
          : msg
      ))

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

  const handleSuggestedAction = (action: Message['suggestedActions'][0]) => {
    switch (action.type) {
      case 'GENERATE_NOTES':
        router.push(`/student/notes?generate=true&topicId=${action.topicId}&topicName=${encodeURIComponent(action.topicName || '')}`)
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
      default:
        toast({ title: 'Action', description: `Action: ${action.label}` })
    }
  }

  const handleSaveAsNotes = async (message: Message) => {
    try {
      toast({
        title: 'Saving...',
        description: 'Creating notes from this conversation.'
      })
      
      // Extract topic info from citations if available
      const topicId = message.citations?.[0]?.chunkId
      const topicName = message.citations?.[0]?.topicName || 'AI Tutor Notes'
      
      router.push(`/student/notes?generate=true&content=${encodeURIComponent(message.content)}&topicName=${encodeURIComponent(topicName)}`)
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
    setVoiceMode(!voiceMode)
    toast({
      title: voiceMode ? 'Voice Mode Off' : 'Voice Mode On',
      description: voiceMode 
        ? 'Switched to text input'
        : 'In DEV mode: Type your message to simulate voice input'
    })
  }

  const getActionIcon = (type: string) => {
    switch (type) {
      case 'GENERATE_NOTES': return <BookOpen className="h-4 w-4" />
      case 'START_PRACTICE': return <Brain className="h-4 w-4" />
      case 'START_FOCUS': return <Target className="h-4 w-4" />
      case 'VIEW_TOPIC': return <Eye className="h-4 w-4" />
      default: return <Sparkles className="h-4 w-4" />
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-white to-blue-50 p-6" data-testid="ai-tutor-page">
      <div className="max-w-5xl mx-auto">
        {/* Header */}
        <div className="mb-6" data-testid="ai-tutor-header">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">AI Tutor</h1>
          <p className="text-gray-600">Ask questions, get explanations, and learn interactively</p>
          <Badge variant="secondary" className="mt-2">
            <Sparkles className="h-3 w-3 mr-1" />
            DEV Mode: Deterministic Responses
          </Badge>
        </div>

        <Card data-testid="ai-tutor-chat-container">
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle>Chat with AI Tutor</CardTitle>
                <CardDescription>
                  Ask about any topic, concept, or problem. I&apos;ll provide explanations with relevant examples.
                </CardDescription>
              </div>
              <Button
                variant={voiceMode ? 'default' : 'outline'}
                size="sm"
                onClick={toggleVoiceMode}
                data-testid="ai-tutor-voice-toggle"
              >
                {voiceMode ? <Mic className="h-4 w-4 mr-2" /> : <MicOff className="h-4 w-4 mr-2" />}
                {voiceMode ? 'Voice On' : 'Voice Off'}
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            {/* Messages */}
            <div 
              className="h-[500px] overflow-y-auto mb-4 space-y-4 p-4 bg-gray-50 rounded-lg"
              data-testid="ai-tutor-messages"
            >
              {messages.length === 0 && (
                <div className="text-center text-gray-500 mt-20" data-testid="ai-tutor-empty">
                  <Sparkles className="h-12 w-12 mx-auto mb-4 text-purple-400" />
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
                        ? 'bg-blue-600 text-white'
                        : 'bg-white border border-gray-200 text-gray-900'
                    }`}
                  >
                    <div className="whitespace-pre-wrap">{message.content}</div>
                    
                    {/* Citations */}
                    {message.citations && message.citations.length > 0 && (
                      <div className="mt-3 pt-3 border-t border-gray-300">
                        <p className="text-xs font-semibold text-gray-600 mb-2">Sources:</p>
                        <div className="space-y-1">
                          {message.citations.map((citation, idx) => (
                            <div 
                              key={citation.chunkId}
                              className="text-xs text-gray-700 bg-gray-100 p-2 rounded"
                              data-testid={`ai-tutor-citation-${idx}`}
                            >
                              <span className="font-medium">{citation.topicName}</span>
                              <span className="text-gray-500"> • {citation.chapterName}</span>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Suggested Actions */}
                    {message.suggestedActions && message.suggestedActions.length > 0 && (
                      <div className="mt-3 pt-3 border-t border-gray-300">
                        <p className="text-xs font-semibold text-gray-600 mb-2">Suggested Actions:</p>
                        <div className="flex flex-wrap gap-2">
                          {message.suggestedActions.map((action, idx) => (
                            <Button
                              key={idx}
                              size="sm"
                              variant="outline"
                              onClick={() => handleSuggestedAction(action)}
                              className="text-xs"
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
                          className="text-xs text-gray-600 hover:text-gray-900"
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
                  <div className="bg-white border border-gray-200 rounded-lg p-4">
                    <Loader2 className="h-4 w-4 animate-spin text-purple-600" />
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
                placeholder={voiceMode ? "Type to simulate voice input (DEV mode)..." : "Ask a question..."}
                className="min-h-[80px] resize-none"
                disabled={isStreaming}
                data-testid="ai-tutor-input"
              />
              <Button
                onClick={handleSendMessage}
                disabled={!input.trim() || isStreaming}
                size="lg"
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
            <div className="mt-4 text-sm text-gray-600" data-testid="ai-tutor-tips">
              <p className="font-medium mb-1">💡 Tips:</p>
              <ul className="list-disc list-inside space-y-1 text-xs">
                <li>Ask specific questions about topics you&apos;re studying</li>
                <li>Request explanations with examples</li>
                <li>Use suggested actions to generate notes, start practice, or focus sessions</li>
                <li>Voice mode (DEV): Type your message to simulate speech-to-text</li>
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
