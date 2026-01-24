'use client'

import { useState, useEffect, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Badge } from '@/components/ui/badge'
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
  
  const [subjectsLoading, setSubjectsLoading] = useState(false)
  const [chaptersLoading, setChaptersLoading] = useState(false)
  const [topicsLoading, setTopicsLoading] = useState(false)
  const [profileLoading, setProfileLoading] = useState(true)
  const [addingToList, setAddingToList] = useState(false)
  const [subjectsNotice, setSubjectsNotice] = useState('')
  const [chaptersNotice, setChaptersNotice] = useState('')
  const [topicsNotice, setTopicsNotice] = useState('')

  const router = useRouter()
  const loading = subjectsLoading || chaptersLoading || topicsLoading

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
      setSubjectsLoading(true)
      setSubjectsNotice('')
      // Get subjects directly using board and grade names from profile
      const data = await contentAPI.getSubjectsByBoardAndGrade(
        studentProfile.educationalBoard,
        grade
      )
      setSubjects(data)
      
      if (data.length === 0) {
        setSubjectsNotice(
          `Content for ${studentProfile.educationalBoard} - ${formatGradeDisplay(grade)} is coming soon.`
        )
      }
    } catch (error: any) {
      console.error('Failed to load subjects:', error)
      setSubjects([])
      setSubjectsNotice(
        `Content for ${studentProfile.educationalBoard} - ${formatGradeDisplay(grade)} is coming soon.`
      )
    } finally {
      setSubjectsLoading(false)
    }
  }, [studentProfile])

  const loadChapters = useCallback(async () => {
    try {
      setChaptersLoading(true)
      setChaptersNotice('')
      const data = await contentAPI.getChaptersBySubject(Number(selectedSubject))
      setChapters(data)
      if (data.length === 0) {
        setChaptersNotice('Chapters for this subject are coming soon.')
      }
    } catch (error) {
      console.error('Failed to load chapters:', error)
      setChapters([])
      setChaptersNotice('Chapters for this subject are coming soon.')
    } finally {
      setChaptersLoading(false)
    }
  }, [selectedSubject])

  const loadTopics = useCallback(async () => {
    try {
      setTopicsLoading(true)
      setTopicsNotice('')
      const data = await contentAPI.getTopicsByChapter(Number(selectedChapter))
      setTopics(data)
      if (data.length === 0) {
        setTopicsNotice('Topics for this chapter are coming soon.')
      }
    } catch (error) {
      console.error('Failed to load topics:', error)
      setTopics([])
      setTopicsNotice('Topics for this chapter are coming soon.')
    } finally {
      setTopicsLoading(false)
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
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="glass-panel p-8 text-center">
          <Loader2 className="h-8 w-8 animate-spin text-emerald-500 mx-auto mb-4" />
          <p className="text-slate-600 dark:text-slate-200">Loading your personalized content...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6" data-testid="discover-page">
        {/* Header with personalized info */}
        <div className="page-header">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              <h1 className="text-2xl md:text-3xl font-bold text-white">Content Discovery</h1>
              <p className="text-sm text-white/80 mt-1">Explore and discover new topics</p>
            </div>
            
            {/* Student's Board & Grade Badge */}
            {studentProfile && (
              <div className="glass flex items-center gap-3 px-4 py-2 rounded-xl border border-white/30 text-white">
                <GraduationCap className="h-5 w-5 text-white" />
                <div className="text-sm">
                  <span className="font-semibold">
                    {studentProfile.educationalBoard}
                  </span>
                  <span className="text-white/80"> - </span>
                  <span className="text-white/80">
                    {formatGradeDisplay(studentProfile.classLevel || studentProfile.gradeLevel)}
                  </span>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Subject and Chapter Selectors (Board/Grade are auto-selected) */}
        <Card className="glass-panel border border-white/40">
          <CardHeader className="border-b border-white/20">
            <CardTitle className="text-base flex items-center space-x-2">
              <Search className="h-5 w-5 text-emerald-500" />
              <span>Find Your Topic</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="p-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                  Subject
                </label>
                <Select 
                  value={selectedSubject} 
                  onValueChange={setSelectedSubject}
                  disabled={subjects.length === 0}
                >
                  <SelectTrigger className="input-modern">
                    <SelectValue
                      placeholder={
                        subjectsLoading
                          ? 'Loading subjects...'
                          : subjects.length > 0
                            ? 'Select Subject'
                            : subjectsNotice || 'Content coming soon'
                      }
                    />
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
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                  Chapter
                </label>
                <Select 
                  value={selectedChapter} 
                  onValueChange={setSelectedChapter}
                  disabled={!selectedSubject}
                >
                  <SelectTrigger className="input-modern">
                    <SelectValue
                      placeholder={
                        chaptersLoading
                          ? 'Loading chapters...'
                          : chapters.length > 0
                            ? 'Select Chapter'
                            : chaptersNotice || 'Chapters coming soon'
                      }
                    />
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
                <Loader2 className="h-6 w-6 animate-spin text-emerald-500" />
                <span className="ml-2 text-sm text-slate-600 dark:text-slate-200">Loading...</span>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Topics List */}
        {topics.length > 0 && (
          <div className="space-y-4">
            <h2 className="text-lg font-semibold text-slate-900 dark:text-white">
              Available Topics ({topics.length})
            </h2>
            
            {topics.map(topic => (
              <Card 
                key={topic.id} 
                className={`glass-panel border border-white/40 hover-lift transition-all cursor-pointer ${
                  selectedTopic?.id === topic.id ? 'ring-2 ring-emerald-400/70' : ''
                }`}
                onClick={() => setSelectedTopic(topic)}
              >
                <CardContent className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-3 mb-2">
                        <h3 className="text-lg font-semibold text-slate-900 dark:text-white">{topic.name}</h3>
                        <Badge className={getDifficultyColor(topic.difficulty)}>
                          {topic.difficulty}
                        </Badge>
                      </div>
                      
                      <p className="text-sm text-slate-600 dark:text-slate-200 mb-3">{topic.description}</p>
                      
                      <div className="flex items-center space-x-4 text-sm text-slate-500 dark:text-slate-300">
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
                        className="btn-outline h-9 px-4 text-sm whitespace-nowrap"
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
                        className="btn-primary h-9 px-4 text-sm whitespace-nowrap"
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
          <Card className="glass-panel border border-white/40">
            <CardContent className="p-12 text-center">
              <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-emerald-500/15 flex items-center justify-center">
                <Search className="h-8 w-8 text-emerald-500" />
              </div>
              <h3 className="text-lg font-medium text-slate-900 dark:text-white mb-2">Start Exploring</h3>
              <p className="text-slate-500 dark:text-slate-300">
                Select a subject and chapter to discover available topics for your grade
              </p>
            </CardContent>
          </Card>
        )}

        {/* No Chapters Available */}
        {selectedSubject && chapters.length === 0 && !chaptersLoading && (
          <Card className="glass-panel border border-white/40">
            <CardContent className="p-12 text-center">
              <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-amber-500/15 flex items-center justify-center">
                <BookOpen className="h-8 w-8 text-amber-500" />
              </div>
              <h3 className="text-lg font-medium text-slate-900 dark:text-white mb-2">Chapters Coming Soon</h3>
              <p className="text-slate-500 dark:text-slate-300">
                {chaptersNotice || 'We are preparing chapters for this subject.'}
              </p>
            </CardContent>
          </Card>
        )}

        {/* No Subjects Available */}
        {!loading && subjects.length === 0 && studentProfile && (
          <Card className="glass-panel border border-white/40">
            <CardContent className="p-12 text-center">
              <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-amber-500/15 flex items-center justify-center">
                <BookOpen className="h-8 w-8 text-amber-500" />
              </div>
              <h3 className="text-lg font-medium text-slate-900 dark:text-white mb-2">Content Coming Soon</h3>
              <p className="text-slate-500 dark:text-slate-300">
                {subjectsNotice || `We are preparing content for ${studentProfile.educationalBoard} - ${formatGradeDisplay(studentProfile.classLevel || studentProfile.gradeLevel)}.`}
              </p>
            </CardContent>
          </Card>
        )}

        {/* No Topics in Selected Chapter */}
        {selectedChapter && topics.length === 0 && !loading && (
          <Card className="glass-panel border border-white/40">
            <CardContent className="p-12 text-center">
              <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-slate-500/10 flex items-center justify-center">
                <BookOpen className="h-8 w-8 text-slate-400" />
              </div>
              <h3 className="text-lg font-medium text-slate-900 dark:text-white mb-2">Topics Coming Soon</h3>
              <p className="text-slate-500 dark:text-slate-300">
                {topicsNotice || 'We are preparing topics for this chapter.'}
              </p>
            </CardContent>
          </Card>
        )}
      </div>
  )
}
