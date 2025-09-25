'use client'

import { useState, useEffect } from 'react'
import AdminLayoutSimple from '@/components/admin-layout-simple'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { BookOpen, Search, Filter, ToggleLeft, ToggleRight, Eye, Edit, Trash2 } from 'lucide-react'
import { toast } from 'sonner'

interface Board {
  id: number
  name: string
  active: boolean
}

interface Grade {
  id: number
  name: string
  displayName: string
  active: boolean
}

interface Subject {
  id: number
  name: string
  active: boolean
}

interface Chapter {
  id: number
  subjectId: number
  name: string
  active: boolean
}

interface Topic {
  id: number
  chapterId: number
  title: string
  code?: string
  description?: string
  summary?: string
  expectedTimeMins?: number
  active: boolean
  chapterName?: string
  subjectName?: string
}

export default function AdminContentBrowsePage() {
  const [boards, setBoards] = useState<Board[]>([])
  const [grades, setGrades] = useState<Grade[]>([])
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [chapters, setChapters] = useState<Chapter[]>([])
  const [topics, setTopics] = useState<Topic[]>([])
  
  const [selectedBoard, setSelectedBoard] = useState<number | null>(null)
  const [selectedGrade, setSelectedGrade] = useState<number | null>(null)
  const [selectedSubject, setSelectedSubject] = useState<number | null>(null)
  const [selectedChapter, setSelectedChapter] = useState<number | null>(null)
  
  const [searchTerm, setSearchTerm] = useState('')
  const [loading, setLoading] = useState(false)
  const [selectedTopics, setSelectedTopics] = useState<Set<number>>(new Set())

  useEffect(() => {
    fetchBoards()
  }, [])

  useEffect(() => {
    if (selectedBoard) {
      fetchGrades()
    }
  }, [selectedBoard])

  useEffect(() => {
    if (selectedGrade) {
      fetchSubjects()
    }
  }, [selectedGrade])

  useEffect(() => {
    if (selectedSubject) {
      fetchChapters()
    }
  }, [selectedSubject])

  useEffect(() => {
    if (selectedChapter) {
      fetchTopics()
    }
  }, [selectedChapter, searchTerm])

  const fetchBoards = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/boards`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (response.ok) {
        const data = await response.json()
        setBoards(data.content || data)
      }
    } catch (error) {
      console.error('Error fetching boards:', error)
    }
  }

  const fetchGrades = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/grades`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (response.ok) {
        const data = await response.json()
        setGrades(data.content || data)
      }
    } catch (error) {
      console.error('Error fetching grades:', error)
    }
  }

  const fetchSubjects = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/subjects`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (response.ok) {
        const data = await response.json()
        setSubjects(data.content || data)
      }
    } catch (error) {
      console.error('Error fetching subjects:', error)
    }
  }

  const fetchChapters = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/chapters?subjectId=${selectedSubject}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (response.ok) {
        const data = await response.json()
        setChapters(data.content || data)
      }
    } catch (error) {
      console.error('Error fetching chapters:', error)
    }
  }

  const fetchTopics = async () => {
    setLoading(true)
    try {
      const token = localStorage.getItem('accessToken')
      const params = new URLSearchParams()
      if (selectedChapter) params.append('chapterId', selectedChapter.toString())
      if (searchTerm) params.append('search', searchTerm)
      
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/topics?${params}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (response.ok) {
        const data = await response.json()
        setTopics(data.content || data)
      }
    } catch (error) {
      console.error('Error fetching topics:', error)
    } finally {
      setLoading(false)
    }
  }

  const formatHours = (minutes: number | undefined) => {
    if (!minutes) return '0 h'
    const hours = minutes / 60
    return hours >= 1 ? `${hours.toFixed(1)} h` : `${minutes} min`
  }

  const toggleTopicActive = async (topicId: number, active: boolean) => {
    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/topics/${topicId}/active`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ active: !active })
      })
      
      if (response.ok) {
        toast.success(`Topic ${!active ? 'activated' : 'deactivated'} successfully`)
        fetchTopics()
      } else {
        toast.error('Failed to update topic status')
      }
    } catch (error) {
      toast.error('Error updating topic status')
    }
  }

  const handleBulkAction = async (action: 'activate' | 'deactivate') => {
    if (selectedTopics.size === 0) {
      toast.error('Please select topics first')
      return
    }

    try {
      const token = localStorage.getItem('accessToken')
      const promises = Array.from(selectedTopics).map(topicId =>
        fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/topics/${topicId}/active`, {
          method: 'PATCH',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ active: action === 'activate' })
        })
      )

      await Promise.all(promises)
      toast.success(`${selectedTopics.size} topics ${action}d successfully`)
      setSelectedTopics(new Set())
      fetchTopics()
    } catch (error) {
      toast.error(`Error ${action}ing topics`)
    }
  }

  return (
    <AdminLayoutSimple>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Browse Content</h1>
            <p className="text-gray-600 dark:text-gray-400">Manage educational content hierarchy</p>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Panel - Content Hierarchy */}
          <Card className="p-6">
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Content Hierarchy</h3>
              
              {/* Search */}
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search topics..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                />
              </div>

              {/* Hierarchical Filters */}
              <div className="space-y-3">
                {/* Board Selection */}
                <div>
                  <label className="text-sm font-medium text-gray-700 dark:text-gray-300">Board</label>
                  <select 
                    value={selectedBoard || ''} 
                    onChange={(e) => {
                      setSelectedBoard(e.target.value ? Number(e.target.value) : null)
                      setSelectedGrade(null)
                      setSelectedSubject(null)
                      setSelectedChapter(null)
                    }}
                    className="w-full mt-1 border border-gray-300 dark:border-gray-600 rounded-md px-3 py-2 dark:bg-gray-700 dark:text-white"
                  >
                    <option value="">Select Board</option>
                    {boards.map(board => (
                      <option key={board.id} value={board.id}>{board.name}</option>
                    ))}
                  </select>
                </div>

                {/* Grade Selection */}
                {selectedBoard && (
                  <div>
                    <label className="text-sm font-medium text-gray-700 dark:text-gray-300">Grade</label>
                    <select 
                      value={selectedGrade || ''} 
                      onChange={(e) => {
                        setSelectedGrade(e.target.value ? Number(e.target.value) : null)
                        setSelectedSubject(null)
                        setSelectedChapter(null)
                      }}
                      className="w-full mt-1 border border-gray-300 dark:border-gray-600 rounded-md px-3 py-2 dark:bg-gray-700 dark:text-white"
                    >
                      <option value="">Select Grade</option>
                      {grades.map(grade => (
                        <option key={grade.id} value={grade.id}>{grade.displayName}</option>
                      ))}
                    </select>
                  </div>
                )}

                {/* Subject Selection */}
                {selectedGrade && (
                  <div>
                    <label className="text-sm font-medium text-gray-700 dark:text-gray-300">Subject</label>
                    <select 
                      value={selectedSubject || ''} 
                      onChange={(e) => {
                        setSelectedSubject(e.target.value ? Number(e.target.value) : null)
                        setSelectedChapter(null)
                      }}
                      className="w-full mt-1 border border-gray-300 dark:border-gray-600 rounded-md px-3 py-2 dark:bg-gray-700 dark:text-white"
                    >
                      <option value="">Select Subject</option>
                      {subjects.map(subject => (
                        <option key={subject.id} value={subject.id}>{subject.name}</option>
                      ))}
                    </select>
                  </div>
                )}

                {/* Chapter Selection */}
                {selectedSubject && (
                  <div>
                    <label className="text-sm font-medium text-gray-700 dark:text-gray-300">Chapter</label>
                    <select 
                      value={selectedChapter || ''} 
                      onChange={(e) => setSelectedChapter(e.target.value ? Number(e.target.value) : null)}
                      className="w-full mt-1 border border-gray-300 dark:border-gray-600 rounded-md px-3 py-2 dark:bg-gray-700 dark:text-white"
                    >
                      <option value="">Select Chapter</option>
                      {chapters.map(chapter => (
                        <option key={chapter.id} value={chapter.id}>{chapter.name}</option>
                      ))}
                    </select>
                  </div>
                )}
              </div>
            </div>
          </Card>

          {/* Right Panel - Topics */}
          <div className="lg:col-span-2">
            <Card className="p-6">
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                    Topics {topics.length > 0 && `(${topics.length})`}
                  </h3>
                  <div className="flex space-x-2">
                    <Button 
                      variant="outline" 
                      size="sm"
                      onClick={() => handleBulkAction('activate')}
                      disabled={selectedTopics.size === 0}
                    >
                      Bulk Activate ({selectedTopics.size})
                    </Button>
                    <Button 
                      variant="outline" 
                      size="sm"
                      onClick={() => handleBulkAction('deactivate')}
                      disabled={selectedTopics.size === 0}
                    >
                      Bulk Deactivate ({selectedTopics.size})
                    </Button>
                  </div>
                </div>

                {/* Topics Grid */}
                {loading ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {[...Array(4)].map((_, i) => (
                      <div key={i} className="border border-gray-200 dark:border-gray-700 rounded-lg p-4 animate-pulse">
                        <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded mb-2"></div>
                        <div className="h-3 bg-gray-200 dark:bg-gray-700 rounded mb-2"></div>
                        <div className="h-3 bg-gray-200 dark:bg-gray-700 rounded w-2/3"></div>
                      </div>
                    ))}
                  </div>
                ) : topics.length === 0 ? (
                  <div className="text-center py-12">
                    <BookOpen className="mx-auto h-12 w-12 text-gray-400 dark:text-gray-500" />
                    <h3 className="text-lg font-semibold text-gray-900 dark:text-white mt-4">No topics found</h3>
                    <p className="text-gray-600 dark:text-gray-400 mt-2">
                      {selectedChapter ? 'No topics in this chapter yet.' : 'Select a chapter to view topics.'}
                    </p>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {topics.map((topic) => (
                      <div key={topic.id} className="border border-gray-200 dark:border-gray-700 rounded-lg p-4 hover:shadow-md transition-shadow">
                        <div className="flex items-start justify-between mb-3">
                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={selectedTopics.has(topic.id)}
                              onChange={(e) => {
                                const newSelected = new Set(selectedTopics)
                                if (e.target.checked) {
                                  newSelected.add(topic.id)
                                } else {
                                  newSelected.delete(topic.id)
                                }
                                setSelectedTopics(newSelected)
                              }}
                              className="mr-2"
                            />
                            <BookOpen className="h-5 w-5 text-blue-500 mr-2" />
                            <h4 className="font-medium text-gray-900 dark:text-white">
                              {topic.title}
                            </h4>
                          </div>
                          <div className="flex items-center space-x-2">
                            <button
                              onClick={() => toggleTopicActive(topic.id, topic.active)}
                              className="p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded"
                            >
                              {topic.active ? (
                                <ToggleRight className="h-5 w-5 text-green-500" />
                              ) : (
                                <ToggleLeft className="h-5 w-5 text-gray-400" />
                              )}
                            </button>
                            <Button variant="outline" size="sm">
                              <Edit className="h-4 w-4" />
                            </Button>
                            <Button variant="outline" size="sm">
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </div>
                        
                        {topic.summary && (
                          <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">
                            {topic.summary}
                          </p>
                        )}
                        
                        <div className="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
                          <span>Expected Time: {formatHours(topic.expectedTimeMins)}</span>
                          {topic.code && <span>Code: {topic.code}</span>}
                        </div>
                        
                        <div className="mt-2 flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
                          <span className={`px-2 py-1 rounded-full ${topic.active ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' : 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300'}`}>
                            {topic.active ? 'Active' : 'Inactive'}
                          </span>
                          <div className="flex space-x-1">
                            <Button variant="outline" size="sm">
                              <Eye className="h-3 w-3" />
                            </Button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </Card>
          </div>
        </div>
      </div>
    </AdminLayoutSimple>
  )
}
