'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Checkbox } from '@/components/ui/checkbox'
import { useAuthStore } from '@/store/auth'
import { StudentRoute } from '@/components/route-guard'
import { BookOpen, Clock, Plus, Search, Filter, CheckCircle, Circle, ArrowLeft } from 'lucide-react'

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
  completed: boolean
  bookmarked: boolean
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
}

export default function StudentStudyListPage() {
  const [topics, setTopics] = useState<Topic[]>([])
  const [filteredTopics, setFilteredTopics] = useState<Topic[]>([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [subjectFilter, setSubjectFilter] = useState('all')
  const [difficultyFilter, setDifficultyFilter] = useState('all')
  const [statusFilter, setStatusFilter] = useState('all')
  
  const router = useRouter()
  const { user } = useAuthStore()

  useEffect(() => {
    if (!user) {
      router.push('/login')
      return
    }
    loadTopics()
  }, [user, router])

  useEffect(() => {
    filterTopics()
  }, [topics, searchTerm, subjectFilter, difficultyFilter, statusFilter])

  const loadTopics = async () => {
    try {
      // TODO: Replace with actual API call
      // const topicsData = await studentAPI.getStudyList()
      
      // Mock data for now
      const mockTopics: Topic[] = [
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
          },
          completed: true,
          bookmarked: true,
          difficulty: 'MEDIUM'
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
          },
          completed: false,
          bookmarked: false,
          difficulty: 'HARD'
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
          },
          completed: false,
          bookmarked: true,
          difficulty: 'MEDIUM'
        },
        {
          id: 4,
          title: 'Photosynthesis',
          description: 'Process of photosynthesis in plants',
          expectedTimeMins: 45,
          chapter: {
            id: 4,
            name: 'Plant Biology',
            subject: {
              id: 3,
              name: 'Biology'
            }
          },
          completed: true,
          bookmarked: false,
          difficulty: 'EASY'
        },
        {
          id: 5,
          title: 'World War II',
          description: 'Major events and causes of World War II',
          expectedTimeMins: 120,
          chapter: {
            id: 5,
            name: 'Modern History',
            subject: {
              id: 4,
              name: 'History'
            }
          },
          completed: false,
          bookmarked: false,
          difficulty: 'MEDIUM'
        }
      ]
      
      setTopics(mockTopics)
    } catch (error) {
      console.error('Failed to load topics:', error)
    } finally {
      setLoading(false)
    }
  }

  const filterTopics = () => {
    let filtered = topics

    // Filter by search term
    if (searchTerm) {
      filtered = filtered.filter(topic =>
        topic.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        topic.description?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        topic.chapter.subject.name.toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    // Filter by subject
    if (subjectFilter !== 'all') {
      filtered = filtered.filter(topic => topic.chapter.subject.name === subjectFilter)
    }

    // Filter by difficulty
    if (difficultyFilter !== 'all') {
      filtered = filtered.filter(topic => topic.difficulty === difficultyFilter)
    }

    // Filter by status
    if (statusFilter !== 'all') {
      if (statusFilter === 'completed') {
        filtered = filtered.filter(topic => topic.completed)
      } else if (statusFilter === 'pending') {
        filtered = filtered.filter(topic => !topic.completed)
      } else if (statusFilter === 'bookmarked') {
        filtered = filtered.filter(topic => topic.bookmarked)
      }
    }

    setFilteredTopics(filtered)
  }

  const handleToggleComplete = async (topicId: number) => {
    try {
      // TODO: Replace with actual API call
      // await studentAPI.toggleTopicComplete(topicId)
      
      setTopics(topics.map(topic =>
        topic.id === topicId
          ? { ...topic, completed: !topic.completed }
          : topic
      ))
    } catch (error) {
      console.error('Failed to toggle completion:', error)
    }
  }

  const handleToggleBookmark = async (topicId: number) => {
    try {
      // TODO: Replace with actual API call
      // await studentAPI.toggleTopicBookmark(topicId)
      
      setTopics(topics.map(topic =>
        topic.id === topicId
          ? { ...topic, bookmarked: !topic.bookmarked }
          : topic
      ))
    } catch (error) {
      console.error('Failed to toggle bookmark:', error)
    }
  }

  const handleQuickBook = (topic: Topic) => {
    // Navigate to booking page with pre-selected topic
    router.push(`/student/booking?topicId=${topic.id}`)
  }

  const getDifficultyColor = (difficulty: string) => {
    switch (difficulty) {
      case 'EASY':
        return 'bg-green-100 text-green-800'
      case 'MEDIUM':
        return 'bg-yellow-100 text-yellow-800'
      case 'HARD':
        return 'bg-red-100 text-red-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  const getSubjects = () => {
    const subjects = Array.from(new Set(topics.map(topic => topic.chapter.subject.name)))
    return subjects.sort()
  }

  const completedCount = topics.filter(topic => topic.completed).length
  const bookmarkedCount = topics.filter(topic => topic.bookmarked).length
  const totalTime = topics.reduce((sum, topic) => sum + topic.expectedTimeMins, 0)
  const completedTime = topics
    .filter(topic => topic.completed)
    .reduce((sum, topic) => sum + topic.expectedTimeMins, 0)

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg">Loading study list...</div>
      </div>
    )
  }

  return (
    <StudentRoute>
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
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
              <h1 className="text-3xl font-bold text-gray-900">Study List</h1>
              <p className="text-gray-600">Track your learning progress and book classes</p>
            </div>
          </div>

          {/* Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Total Topics</CardTitle>
                <BookOpen className="h-4 w-4 text-blue-600" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-blue-600">{topics.length}</div>
                <p className="text-xs text-muted-foreground">Available topics</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Completed</CardTitle>
                <CheckCircle className="h-4 w-4 text-emerald-600" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-emerald-600">{completedCount}</div>
                <p className="text-xs text-muted-foreground">{Math.round((completedCount / topics.length) * 100)}% complete</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Bookmarked</CardTitle>
                <BookOpen className="h-4 w-4 text-yellow-600" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-yellow-600">{bookmarkedCount}</div>
                <p className="text-xs text-muted-foreground">Saved topics</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Study Time</CardTitle>
                <Clock className="h-4 w-4 text-purple-600" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-purple-600">{completedTime}h</div>
                <p className="text-xs text-muted-foreground">of {totalTime}h total</p>
              </CardContent>
            </Card>
          </div>

          {/* Filters */}
          <Card className="mb-6">
            <CardContent className="p-4">
              <div className="flex flex-col md:flex-row gap-4">
                <div className="flex-1">
                  <div className="relative">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                    <Input
                      placeholder="Search topics..."
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                      className="pl-10"
                    />
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <Filter className="h-4 w-4 text-gray-400" />
                  <Select value={subjectFilter} onValueChange={setSubjectFilter}>
                    <SelectTrigger className="w-40">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Subjects</SelectItem>
                      {getSubjects().map(subject => (
                        <SelectItem key={subject} value={subject}>{subject}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <Select value={difficultyFilter} onValueChange={setDifficultyFilter}>
                    <SelectTrigger className="w-32">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Levels</SelectItem>
                      <SelectItem value="EASY">Easy</SelectItem>
                      <SelectItem value="MEDIUM">Medium</SelectItem>
                      <SelectItem value="HARD">Hard</SelectItem>
                    </SelectContent>
                  </Select>
                  <Select value={statusFilter} onValueChange={setStatusFilter}>
                    <SelectTrigger className="w-32">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Status</SelectItem>
                      <SelectItem value="completed">Completed</SelectItem>
                      <SelectItem value="pending">Pending</SelectItem>
                      <SelectItem value="bookmarked">Bookmarked</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Topics List */}
          <div className="space-y-4">
            {filteredTopics.length > 0 ? (
              filteredTopics.map((topic) => (
                <Card key={topic.id} className="hover:shadow-md transition-shadow">
                  <CardContent className="p-6">
                    <div className="flex items-start space-x-4">
                      <div className="flex-shrink-0 pt-1">
                        <Checkbox
                          checked={topic.completed}
                          onCheckedChange={() => handleToggleComplete(topic.id)}
                        />
                      </div>
                      
                      <div className="flex-1">
                        <div className="flex items-center space-x-3 mb-2">
                          <h3 className={`text-lg font-semibold ${
                            topic.completed ? 'line-through text-gray-500' : 'text-gray-900'
                          }`}>
                            {topic.title}
                          </h3>
                          <Badge className={getDifficultyColor(topic.difficulty)}>
                            {topic.difficulty}
                          </Badge>
                          {topic.bookmarked && (
                            <Badge variant="outline" className="text-yellow-600 border-yellow-600">
                              Bookmarked
                            </Badge>
                          )}
                        </div>
                        
                        <p className="text-gray-600 mb-3">{topic.description}</p>
                        
                        <div className="flex items-center space-x-4 text-sm text-gray-500 mb-4">
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

                      <div className="flex flex-col space-y-2">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleToggleBookmark(topic.id)}
                        >
                          {topic.bookmarked ? 'Unbookmark' : 'Bookmark'}
                        </Button>
                        
                        {!topic.completed && (
                          <Button
                            size="sm"
                            className="bg-emerald-600 hover:bg-emerald-700"
                            onClick={() => handleQuickBook(topic)}
                          >
                            <Plus className="h-4 w-4 mr-1" />
                            Quick Book
                          </Button>
                        )}
                        
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => router.push(`/student/booking?topicId=${topic.id}`)}
                        >
                          View Details
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))
            ) : (
              <Card>
                <CardContent className="p-12 text-center">
                  <BookOpen className="h-12 w-12 mx-auto mb-4 text-gray-300" />
                  <h3 className="text-lg font-medium text-gray-900 mb-2">No topics found</h3>
                  <p className="text-gray-600 mb-4">
                    {searchTerm || subjectFilter !== 'all' || difficultyFilter !== 'all' || statusFilter !== 'all'
                      ? 'Try adjusting your search or filters'
                      : 'No topics available at the moment'
                    }
                  </p>
                </CardContent>
              </Card>
            )}
          </div>
        </div>
      </div>
    </StudentRoute>
  )
}
