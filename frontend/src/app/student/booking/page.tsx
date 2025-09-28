'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { useAuthStore } from '@/store/auth'
import { StudentRoute } from '@/components/route-guard'
import { Calendar, Clock, DollarSign, CheckCircle, AlertCircle, ArrowLeft } from 'lucide-react'

const bookingSchema = z.object({
  topicId: z.number().min(1, 'Please select a topic'),
  startTime: z.string().min(1, 'Start time is required'),
  durationMinutes: z.number().min(30).max(120),
  studentNotes: z.string().optional(),
  timezone: z.string().optional()
})

type BookingForm = z.infer<typeof bookingSchema>

interface Topic {
  id: number
  title: string
  description?: string
  expectedTimeMins: number
  chapter: {
    id: number
    name: string
    subject: {
      id: number
      name: string
    }
  }
}

interface BookingQuote {
  expectedMinutes: number
  endTime: string
  bufferOk: boolean
  price: {
    currency: string
    min: number
    max: number
    ruleId?: number
  }
}

export default function StudentBookingPage() {
  const [step, setStep] = useState<'select' | 'schedule' | 'confirm'>('select')
  const [topics, setTopics] = useState<Topic[]>([])
  const [selectedTopic, setSelectedTopic] = useState<Topic | null>(null)
  const [quote, setQuote] = useState<BookingQuote | null>(null)
  const [loading, setLoading] = useState(false)
  const [quoteLoading, setQuoteLoading] = useState(false)
  
  const router = useRouter()
  const { user } = useAuthStore()

  const form = useForm<BookingForm>({
    resolver: zodResolver(bookingSchema),
    defaultValues: {
      durationMinutes: 60,
      timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
    }
  })

  useEffect(() => {
    if (!user) {
      router.push('/login')
      return
    }
    loadTopics()
  }, [user, router])

  const loadTopics = async () => {
    try {
      // TODO: Replace with actual API call
      // const topicsData = await studentAPI.getTopics()
      
      // Mock data for now
      setTopics([
        {
          id: 1,
          title: 'Quadratic Equations',
          description: 'Learn to solve quadratic equations using various methods',
          expectedTimeMins: 60,
          chapter: {
            id: 1,
            name: 'Algebra',
            subject: {
              id: 1,
              name: 'Mathematics'
            }
          }
        },
        {
          id: 2,
          title: 'Trigonometry Basics',
          description: 'Introduction to trigonometric functions and identities',
          expectedTimeMins: 90,
          chapter: {
            id: 2,
            name: 'Trigonometry',
            subject: {
              id: 1,
              name: 'Mathematics'
            }
          }
        },
        {
          id: 3,
          title: 'Chemical Bonding',
          description: 'Understanding ionic and covalent bonds',
          expectedTimeMins: 75,
          chapter: {
            id: 3,
            name: 'Chemical Bonding',
            subject: {
              id: 2,
              name: 'Chemistry'
            }
          }
        }
      ])
    } catch (error) {
      toast.error('Failed to load topics')
    }
  }

  const handleTopicSelect = (topic: Topic) => {
    setSelectedTopic(topic)
    form.setValue('topicId', topic.id)
    form.setValue('durationMinutes', topic.expectedTimeMins)
    setStep('schedule')
  }

  const handleScheduleChange = async () => {
    const formData = form.getValues()
    if (!formData.startTime || !formData.durationMinutes) return

    setQuoteLoading(true)
    try {
      // TODO: Replace with actual API call
      // const quoteData = await studentAPI.getBookingQuote({
      //   topicId: formData.topicId,
      //   startTime: formData.startTime,
      //   durationMinutes: formData.durationMinutes,
      //   timezone: formData.timezone
      // })
      
      // Mock quote data
      const mockQuote: BookingQuote = {
        expectedMinutes: selectedTopic?.expectedTimeMins || 60,
        endTime: new Date(new Date(formData.startTime).getTime() + formData.durationMinutes * 60000).toISOString(),
        bufferOk: true,
        price: {
          currency: 'INR',
          min: 500,
          max: 500,
          ruleId: 1
        }
      }
      
      setQuote(mockQuote)
    } catch (error) {
      toast.error('Failed to get booking quote')
    } finally {
      setQuoteLoading(false)
    }
  }

  const handleConfirmBooking = async () => {
    const formData = form.getValues()
    setLoading(true)
    
    try {
      // TODO: Replace with actual API call
      // const booking = await studentAPI.createBooking(formData)
      
      toast.success('Class booked successfully!')
      router.push('/student/dashboard')
    } catch (error) {
      toast.error('Failed to book class')
    } finally {
      setLoading(false)
    }
  }

  const formatTime = (timeString: string) => {
    return new Date(timeString).toLocaleString()
  }

  return (
    <StudentRoute>
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Header */}
          <div className="mb-8 flex items-center space-x-4">
            <Button 
              variant="outline" 
              size="sm"
              onClick={() => router.back()}
              className="flex items-center space-x-2"
            >
              <ArrowLeft className="h-4 w-4" />
              <span>Back</span>
            </Button>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Book a Class</h1>
              <p className="text-gray-600">Schedule your learning session</p>
            </div>
          </div>

          {/* Progress Steps */}
          <div className="mb-8">
            <div className="flex items-center space-x-4">
              <div className={`flex items-center space-x-2 ${step === 'select' ? 'text-emerald-600' : 'text-gray-400'}`}>
                <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                  step === 'select' ? 'bg-emerald-600 text-white' : 'bg-gray-200'
                }`}>
                  1
                </div>
                <span className="font-medium">Select Topic</span>
              </div>
              <div className="flex-1 h-px bg-gray-200"></div>
              <div className={`flex items-center space-x-2 ${step === 'schedule' ? 'text-emerald-600' : 'text-gray-400'}`}>
                <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                  step === 'schedule' ? 'bg-emerald-600 text-white' : 'bg-gray-200'
                }`}>
                  2
                </div>
                <span className="font-medium">Schedule</span>
              </div>
              <div className="flex-1 h-px bg-gray-200"></div>
              <div className={`flex items-center space-x-2 ${step === 'confirm' ? 'text-emerald-600' : 'text-gray-400'}`}>
                <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                  step === 'confirm' ? 'bg-emerald-600 text-white' : 'bg-gray-200'
                }`}>
                  3
                </div>
                <span className="font-medium">Confirm</span>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Main Content */}
            <div className="lg:col-span-2">
              {step === 'select' && (
                <Card>
                  <CardHeader>
                    <CardTitle>Select a Topic</CardTitle>
                    <CardDescription>Choose the topic you want to learn</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-4">
                      {topics.map((topic) => (
                        <div 
                          key={topic.id}
                          className="border rounded-lg p-4 hover:bg-gray-50 cursor-pointer transition-colors"
                          onClick={() => handleTopicSelect(topic)}
                        >
                          <div className="flex justify-between items-start">
                            <div className="flex-1">
                              <h3 className="font-semibold text-lg">{topic.title}</h3>
                              <p className="text-gray-600 mb-2">{topic.description}</p>
                              <div className="flex items-center space-x-4 text-sm text-gray-500">
                                <span>{topic.chapter.subject.name}</span>
                                <span>•</span>
                                <span>{topic.chapter.name}</span>
                                <span>•</span>
                                <div className="flex items-center space-x-1">
                                  <Clock className="h-4 w-4" />
                                  <span>{topic.expectedTimeMins} min</span>
                                </div>
                              </div>
                            </div>
                            <Button size="sm" variant="outline">
                              Select
                            </Button>
                          </div>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              )}

              {step === 'schedule' && selectedTopic && (
                <Card>
                  <CardHeader>
                    <CardTitle>Schedule Your Class</CardTitle>
                    <CardDescription>Set the date, time, and duration for your session</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <form className="space-y-6">
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                          <Label htmlFor="startTime">Start Time *</Label>
                          <Input
                            id="startTime"
                            type="datetime-local"
                            {...form.register('startTime')}
                            onChange={(e) => {
                              form.setValue('startTime', e.target.value)
                              handleScheduleChange()
                            }}
                          />
                        </div>

                        <div>
                          <Label htmlFor="duration">Duration (minutes) *</Label>
                          <Select
                            value={form.watch('durationMinutes')?.toString()}
                            onValueChange={(value) => {
                              form.setValue('durationMinutes', parseInt(value))
                              handleScheduleChange()
                            }}
                          >
                            <SelectTrigger>
                              <SelectValue placeholder="Select duration" />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value="30">30 minutes</SelectItem>
                              <SelectItem value="45">45 minutes</SelectItem>
                              <SelectItem value="60">60 minutes</SelectItem>
                              <SelectItem value="90">90 minutes</SelectItem>
                              <SelectItem value="120">120 minutes</SelectItem>
                            </SelectContent>
                          </Select>
                        </div>
                      </div>

                      <div>
                        <Label htmlFor="notes">Notes (Optional)</Label>
                        <Textarea
                          id="notes"
                          placeholder="Any specific requirements or questions..."
                          {...form.register('studentNotes')}
                        />
                      </div>

                      <div className="flex space-x-4">
                        <Button 
                          type="button" 
                          variant="outline"
                          onClick={() => setStep('select')}
                        >
                          Back
                        </Button>
                        <Button 
                          type="button"
                          onClick={() => setStep('confirm')}
                          disabled={!quote || !quote.bufferOk}
                        >
                          Continue
                        </Button>
                      </div>
                    </form>
                  </CardContent>
                </Card>
              )}

              {step === 'confirm' && selectedTopic && quote && (
                <Card>
                  <CardHeader>
                    <CardTitle>Confirm Your Booking</CardTitle>
                    <CardDescription>Review your booking details before confirming</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-6">
                      <div className="border rounded-lg p-4">
                        <h3 className="font-semibold text-lg mb-2">{selectedTopic.title}</h3>
                        <p className="text-gray-600 mb-4">{selectedTopic.description}</p>
                        <div className="flex items-center space-x-4 text-sm text-gray-500">
                          <span>{selectedTopic.chapter.subject.name}</span>
                          <span>•</span>
                          <span>{selectedTopic.chapter.name}</span>
                        </div>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="flex items-center space-x-2">
                          <Calendar className="h-5 w-5 text-gray-400" />
                          <div>
                            <p className="text-sm font-medium">Start Time</p>
                            <p className="text-sm text-gray-600">{formatTime(form.getValues('startTime'))}</p>
                          </div>
                        </div>
                        <div className="flex items-center space-x-2">
                          <Clock className="h-5 w-5 text-gray-400" />
                          <div>
                            <p className="text-sm font-medium">Duration</p>
                            <p className="text-sm text-gray-600">{form.getValues('durationMinutes')} minutes</p>
                          </div>
                        </div>
                      </div>

                      {form.getValues('studentNotes') && (
                        <div>
                          <p className="text-sm font-medium mb-1">Notes</p>
                          <p className="text-sm text-gray-600">{form.getValues('studentNotes')}</p>
                        </div>
                      )}

                      <div className="flex space-x-4">
                        <Button 
                          type="button" 
                          variant="outline"
                          onClick={() => setStep('schedule')}
                        >
                          Back
                        </Button>
                        <Button 
                          type="button"
                          onClick={handleConfirmBooking}
                          disabled={loading}
                          className="bg-emerald-600 hover:bg-emerald-700"
                        >
                          {loading ? 'Booking...' : 'Confirm Booking'}
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )}
            </div>

            {/* Sidebar */}
            <div className="space-y-6">
              {/* Selected Topic Summary */}
              {selectedTopic && (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Selected Topic</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      <h4 className="font-medium">{selectedTopic.title}</h4>
                      <p className="text-sm text-gray-600">{selectedTopic.description}</p>
                      <div className="flex items-center space-x-2 text-sm text-gray-500">
                        <Clock className="h-4 w-4" />
                        <span>{selectedTopic.expectedTimeMins} minutes</span>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )}

              {/* Booking Quote */}
              {quote && (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Booking Summary</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-600">Duration</span>
                        <span className="text-sm font-medium">{form.getValues('durationMinutes')} min</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-600">Price</span>
                        <span className="text-sm font-medium">₹{quote.price.min}</span>
                      </div>
                      <div className="border-t pt-3">
                        <div className="flex justify-between">
                          <span className="font-medium">Total</span>
                          <span className="font-medium">₹{quote.price.min}</span>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )}

              {/* Buffer Status */}
              {quote && (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Availability</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center space-x-2">
                      {quote.bufferOk ? (
                        <>
                          <CheckCircle className="h-5 w-5 text-emerald-600" />
                          <span className="text-sm text-emerald-600">Time slot available</span>
                        </>
                      ) : (
                        <>
                          <AlertCircle className="h-5 w-5 text-red-600" />
                          <span className="text-sm text-red-600">Time slot conflicts</span>
                        </>
                      )}
                    </div>
                  </CardContent>
                </Card>
              )}
            </div>
          </div>
        </div>
      </div>
    </StudentRoute>
  )
}
