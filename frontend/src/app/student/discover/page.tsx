'use client'

import { useState, useEffect, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Badge } from '@/components/ui/badge'
import { useAuthStore } from '@/store/auth'
import { StudentRoute } from '@/components/route-guard'
import { BookOpen, Clock, Plus, Loader2, Search, GraduationCap } from 'lucide-react'
import { contentAPI, studentAPI } from '@/lib/apiClient'
import { toast } from 'sonner'

interface Subject {
  id: number
  name: string
}

interface Chapter {
  id: number
  name: string
  chapterNumber: number
}

interface Topic {
  id: number
  name: string
  description: string
  expectedTimeMins: number
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
}

interface StudentProfile {
  educationalBoard?: string
  classLevel?: string
  gradeLevel?: string
}

// Two-component pattern: prevents API calls before auth is verified
export default function StudentDiscoverPage() {
  return (
    <StudentRoute>
      <DiscoverContent />
    </StudentRoute>
  )
}

function DiscoverContent() {
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [chapters, setChapters] = useState<Chapter[]>([])
  const [topics, setTopics] = useState<Topic[]>([])
  
  const [studentProfile, setStudentProfile] = useState<StudentProfile | null>(null)
  const [selectedSubject, setSelectedSubject] = useState<string>('')
  const [selectedChapter, setSelectedChapter] = useState<string>('')
  const [selectedTopic, setSelectedTopic] = useState<Topic | null>(null)
  
  const [loading, setLoading] = useState(false)
  const [profileLoading, setProfileLoading] = useState(true)
  const [addingToList, setAddingToList] = useState(false)
  
  const router = useRouter()
  const { user } = useAuthStore()

  // Load student profile on mount - auth already verified by StudentRoute
  useEffect(() => {
    loadStudentProfile()
  }, [])

  // Load subjects when profile is loaded
  useEffect(() => {
    if (studentProfile?.educationalBoard && (studentProfile?.classLevel || studentProfile?.gradeLevel)) {
      loadSubjectsForProfile()
    }
  }, [studentProfile])

  useEffect(() => {
    if (selectedSubject) {
      loadChapters()
    } else {
      setChapters([])
      setSelectedChapter('')
    }
  }, [selectedSubject])

  useEffect(() => {
    if (selectedChapter) {
      loadTopics()
    } else {
      setTopics([])
      setSelectedTopic(null)
    }
  }, [selectedChapter])

  const loadStudentProfile = async () => {
    try {
      setProfileLoading(true)
      const data = await studentAPI.getProfile()
      setStudentProfile(data)
    } catch (error) {
      console.error('Failed to load student profile:', error)
      toast.error('Failed to load your profile')
    } finally {
      setProfileLoading(false)
    }
  }

  const loadSubjectsForProfile = useCallback(async () => {
    if (!studentProfile?.educationalBoard) return
    
    const grade = studentProfile.classLevel || studentProfile.gradeLevel
    if (!grade) return

    try {
      setLoading(true)
      // Get subjects directly using board and grade names from profile
      const data = await contentAPI.getSubjectsByBoardAndGrade(
        studentProfile.educationalBoard,
        grade
      )
      setSubjects(data)
      
      if (data.length === 0) {
        toast.info('No subjects available for your board and grade yet')
      }
    } catch (error: any) {
      console.error('Failed to load subjects:', error)
      // If direct lookup fails, try the fallback approach
      if (error.response?.status === 404) {
        toast.error('No content found for your board and grade combination')
      } else {
        toast.error('Failed to load subjects')
      }
    } finally {
      setLoading(false)
    }
  }, [studentProfile])

  const loadChapters = useCallback(async () => {
    try {
      setLoading(true)
      const data = await contentAPI.getChaptersBySubject(Number(selectedSubject))
      setChapters(data)
    } catch (error) {
      console.error('Failed to load chapters:', error)
      toast.error('Failed to load chapters')
    } finally {
      setLoading(false)
    }
  }, [selectedSubject])

  const loadTopics = useCallback(async () => {
    try {
      setLoading(true)
      const data = await contentAPI.getTopicsByChapter(Number(selectedChapter))
      setTopics(data)
    } catch (error) {
      console.error('Failed to load topics:', error)
      toast.error('Failed to load topics')
    } finally {
      setLoading(false)
    }
  }, [selectedChapter])

  const handleAddToStudyList = async (topic: Topic) => {
    try {
      setAddingToList(true)
      await studentAPI.addToStudyList(topic.id)
      toast.success('Topic added to study list successfully!')
    } catch (error: any) {
      console.error('Failed to add to study list:', error)
      if (error.response?.status === 409) {
        toast.error('Topic already in your study list')
      } else {
        toast.error('Failed to add topic to study list')
      }
    } finally {
      setAddingToList(false)
    }
  }

  const handleBookClass = (topic: Topic) => {
    router.push(`/student/booking?topicId=${topic.id}`)
  }

  const getDifficultyColor = (difficulty: string) => {
    switch (difficulty) {
      case 'EASY':
        return 'bg-emerald-100 text-emerald-800 border-emerald-200'
      case 'MEDIUM':
        return 'bg-amber-100 text-amber-800 border-amber-200'
      case 'HARD':
        return 'bg-red-100 text-red-800 border-red-200'
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200'
    }
  }

  const formatGradeDisplay = (grade?: string) => {
    if (!grade) return ''
    // Convert "GRADE_7" to "Grade 7", "CLASS_8" to "Class 8", etc.
    return grade
      .replace(/_/g, ' ')
      .replace(/^(\w)/, (c) => c.toUpperCase())
      .replace(/(\s\w)/g, (c) => c.toLowerCase())
  }

  if (profileLoading) {
    return (
      <div className="p-6 flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <Loader2 className="h-8 w-8 animate-spin text-emerald-600 mx-auto mb-4" />
          <p className="text-gray-600">Loading your personalized content...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="p-6 space-y-6" data-testid="discover-page">
        {/* Header with personalized info */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Content Discovery</h1>
            <p className="text-sm text-gray-500 mt-1">EXPLORE AND DISCOVER NEW TOPICS</p>
          </div>
          
          {/* Student's Board & Grade Badge */}
          {studentProfile && (
            <div className="flex items-center space-x-2 bg-gradient-to-r from-emerald-50 to-teal-50 px-4 py-2 rounded-lg border border-emerald-200">
              <GraduationCap className="h-5 w-5 text-emerald-600" />
              <div className="text-sm">
                <span className="font-medium text-emerald-700">
                  {studentProfile.educationalBoard}
                </span>
                <span className="text-emerald-600"> • </span>
                <span className="text-emerald-600">
                  {formatGradeDisplay(studentProfile.classLevel || studentProfile.gradeLevel)}
                </span>
              </div>
            </div>
          )}
        </div>

        {/* Subject and Chapter Selectors (Board/Grade are auto-selected) */}
        <Card className="rounded-2xl border-gray-100 shadow-sm">
          <CardHeader className="border-b border-gray-100 bg-gray-50/50">
            <CardTitle className="text-base flex items-center space-x-2">
              <Search className="h-5 w-5 text-emerald-600" />
              <span>Find Your Topic</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="p-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Subject
                </label>
                <Select 
                  value={selectedSubject} 
                  onValueChange={setSelectedSubject}
                  disabled={subjects.length === 0}
                >
                  <SelectTrigger>
                    <SelectValue placeholder={subjects.length > 0 ? "Select Subject" : "Loading subjects..."} />
                  </SelectTrigger>
                  <SelectContent>
                    {subjects.map(subject => (
                      <SelectItem key={subject.id} value={subject.id.toString()}>
                        {subject.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Chapter
                </label>
                <Select 
                  value={selectedChapter} 
                  onValueChange={setSelectedChapter}
                  disabled={!selectedSubject}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select Chapter" />
                  </SelectTrigger>
                  <SelectContent>
                    {chapters.map(chapter => (
                      <SelectItem key={chapter.id} value={chapter.id.toString()}>
                        {chapter.chapterNumber}. {chapter.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            {loading && (
              <div className="flex items-center justify-center py-4 mt-4">
                <Loader2 className="h-6 w-6 animate-spin text-emerald-600" />
                <span className="ml-2 text-sm text-gray-600">Loading...</span>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Topics List */}
        {topics.length > 0 && (
          <div className="space-y-4">
            <h2 className="text-lg font-semibold text-gray-900">
              Available Topics ({topics.length})
            </h2>
            
            {topics.map(topic => (
              <Card 
                key={topic.id} 
                className={`rounded-2xl border-gray-100 shadow-sm hover:shadow-md transition-all cursor-pointer ${
                  selectedTopic?.id === topic.id ? 'ring-2 ring-emerald-500' : ''
                }`}
                onClick={() => setSelectedTopic(topic)}
              >
                <CardContent className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-3 mb-2">
                        <h3 className="text-lg font-semibold text-gray-900">{topic.name}</h3>
                        <Badge className={getDifficultyColor(topic.difficulty)}>
                          {topic.difficulty}
                        </Badge>
                      </div>
                      
                      <p className="text-sm text-gray-600 mb-3">{topic.description}</p>
                      
                      <div className="flex items-center space-x-4 text-sm text-gray-500">
                        <div className="flex items-center space-x-1">
                          <Clock className="h-4 w-4" />
                          <span>{topic.expectedTimeMins} minutes</span>
                        </div>
                      </div>
                    </div>

                    <div className="flex flex-col space-y-2 ml-4">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={(e) => {
                          e.stopPropagation()
                          handleAddToStudyList(topic)
                        }}
                        disabled={addingToList}
                        className="whitespace-nowrap"
                      >
                        {addingToList ? (
                          <>
                            <Loader2 className="h-4 w-4 mr-1 animate-spin" />
                            Adding...
                          </>
                        ) : (
                          <>
                            <Plus className="h-4 w-4 mr-1" />
                            Add to List
                          </>
                        )}
                      </Button>
                      <Button
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation()
                          handleBookClass(topic)
                        }}
                        className="bg-gradient-to-r from-emerald-500 to-teal-500 text-white whitespace-nowrap"
                      >
                        <BookOpen className="h-4 w-4 mr-1" />
                        Book Class
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}

        {/* Empty State - No Subject Selected */}
        {subjects.length > 0 && !selectedSubject && (
          <Card className="rounded-2xl border-gray-100 shadow-sm">
            <CardContent className="p-12 text-center">
              <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-gradient-to-br from-emerald-500 to-teal-500 flex items-center justify-center">
                <Search className="h-8 w-8 text-white" />
              </div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">Start Exploring</h3>
              <p className="text-gray-500">
                Select a subject and chapter to discover available topics for your grade
              </p>
            </CardContent>
          </Card>
        )}

        {/* No Subjects Available */}
        {!loading && subjects.length === 0 && studentProfile && (
          <Card className="rounded-2xl border-gray-100 shadow-sm">
            <CardContent className="p-12 text-center">
              <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-amber-100 flex items-center justify-center">
                <BookOpen className="h-8 w-8 text-amber-600" />
              </div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">No Content Available Yet</h3>
              <p className="text-gray-500">
                Content for {studentProfile.educationalBoard} - {formatGradeDisplay(studentProfile.classLevel || studentProfile.gradeLevel)} is being prepared
              </p>
              <p className="text-sm text-gray-400 mt-2">
                Please check back later or contact support
              </p>
            </CardContent>
          </Card>
        )}

        {/* No Topics in Selected Chapter */}
        {selectedChapter && topics.length === 0 && !loading && (
          <Card className="rounded-2xl border-gray-100 shadow-sm">
            <CardContent className="p-12 text-center">
              <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-gray-100 flex items-center justify-center">
                <BookOpen className="h-8 w-8 text-gray-400" />
              </div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">No Topics Found</h3>
              <p className="text-gray-500">
                No topics available for the selected chapter yet
              </p>
            </CardContent>
          </Card>
        )}
      </div>
  )
}
