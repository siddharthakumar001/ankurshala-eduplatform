'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Badge } from '@/components/ui/badge'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Switch } from '@/components/ui/switch'
import { toast } from 'sonner'
import AdminLayoutSimple from '@/components/admin-layout-simple'
import { api } from '@/utils/api'
import AuthGuard from '@/components/AuthGuard'
import SessionManager from '@/components/SessionManager'
import { 
  Plus, 
  Search, 
  Edit, 
  Trash2, 
  Eye, 
  EyeOff,
  BookOpen,
  GraduationCap,
  Users,
  FileText,
  XCircle,
  AlertTriangle,
  AlertCircle
} from 'lucide-react'

// Spring Data Page interface
interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  numberOfElements: number
  empty: boolean
}

interface Board {
  id: number
  name: string
  active: boolean
  createdAt: string
}

interface Subject {
  id: number
  name: string
  active: boolean
  createdAt: string
  boardId: number 
}

interface Chapter {
  id: number
  name: string
  subjectId: number
  active: boolean
  createdAt: string
}

interface Topic {
  id: number
  title: string
  code?: string
  description?: string
  summary?: string
  expectedTimeMins: number
  chapterId: number
  chapterName?: string
  subjectName?: string
  boardId: number
  subjectId: number
  active: boolean
  createdAt: string
}

interface TopicNote {
  id: number
  title: string
  content: string
  topicId: number
  active: boolean
  createdAt: string
}

export default function ContentManagePage() {
  return (
    <AuthGuard requiredRoles={['ADMIN']}>
      <SessionManager showSessionInfo={true}>
        <ContentManagePageContent />
      </SessionManager>
    </AuthGuard>
  )
}

function ContentManagePageContent() {
  const [activeTab, setActiveTab] = useState('boards')
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all')
  const [boards, setBoards] = useState<Board[]>([])
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [chapters, setChapters] = useState<Chapter[]>([])
  const [topics, setTopics] = useState<Topic[]>([])
  const [topicNotes, setTopicNotes] = useState<TopicNote[]>([])
  const [loading, setLoading] = useState(true)
  
  // Pagination state for subjects
  const [subjectsCurrentPage, setSubjectsCurrentPage] = useState(0)
  const [subjectsPageSize, setSubjectsPageSize] = useState(5)
  const [subjectsTotalPages, setSubjectsTotalPages] = useState(0)
  const [subjectsTotalElements, setSubjectsTotalElements] = useState(0)
  
  // Pagination state for chapters
  const [chaptersCurrentPage, setChaptersCurrentPage] = useState(0)
  const [chaptersPageSize, setChaptersPageSize] = useState(5)
  const [chaptersTotalPages, setChaptersTotalPages] = useState(0)
  const [chaptersTotalElements, setChaptersTotalElements] = useState(0)
  
  // Pagination state for topics
  const [topicsCurrentPage, setTopicsCurrentPage] = useState(0)
  const [topicsPageSize, setTopicsPageSize] = useState(5)
  const [topicsTotalPages, setTopicsTotalPages] = useState(0)
  const [topicsTotalElements, setTopicsTotalElements] = useState(0)
  
  // Pagination state for topic notes
  const [notesCurrentPage, setNotesCurrentPage] = useState(0)
  const [notesPageSize, setNotesPageSize] = useState(5)
  const [notesTotalPages, setNotesTotalPages] = useState(0)
  const [notesTotalElements, setNotesTotalElements] = useState(0)
  
  // Dialog states
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<any>(null)
  const [editingBoardId, setEditingBoardId] = useState<number | null>(null)
  const [editingSubjectId, setEditingSubjectId] = useState<number | null>(null)
  const [editingChapterId, setEditingChapterId] = useState<number | null>(null)
  const [editingTopicId, setEditingTopicId] = useState<number | null>(null)
  const [editingNoteId, setEditingNoteId] = useState<number | null>(null)
  const [editFormData, setEditFormData] = useState<any>({})
  const [formData, setFormData] = useState<any>({})
  const [showDeleteConfirmation, setShowDeleteConfirmation] = useState(false)
  const [deletionImpact, setDeletionImpact] = useState<any>(null)
  const [boardToDelete, setBoardToDelete] = useState<any>(null)
  const [isDeletionConfirmed, setIsDeletionConfirmed] = useState(false)

  // Pagination handlers for subjects
  const handleSubjectsPageChange = (newPage: number) => {
    setSubjectsCurrentPage(newPage)
    fetchSubjects(newPage, subjectsPageSize)
  }

  const handleSubjectsPageSizeChange = (newSize: number) => {
    setSubjectsPageSize(newSize)
    setSubjectsCurrentPage(0) // Reset to first page
    fetchSubjects(0, newSize)
  }

  const handleSubjectsSearchAndFilter = () => {
    setSubjectsCurrentPage(0) // Reset to first page when searching/filtering
    fetchSubjects(0, subjectsPageSize)
  }

  // Pagination handlers for chapters
  const handleChaptersPageChange = (newPage: number) => {
    setChaptersCurrentPage(newPage)
    fetchChapters(newPage, chaptersPageSize)
  }

  const handleChaptersPageSizeChange = (newSize: number) => {
    setChaptersPageSize(newSize)
    setChaptersCurrentPage(0) // Reset to first page
    fetchChapters(0, newSize)
  }

  const handleChaptersSearchAndFilter = () => {
    setChaptersCurrentPage(0) // Reset to first page when searching/filtering
    fetchChapters(0, chaptersPageSize)
  }

  // Pagination handlers for topics
  const handleTopicsPageChange = (newPage: number) => {
    setTopicsCurrentPage(newPage)
    fetchTopics(newPage, topicsPageSize)
  }

  const handleTopicsPageSizeChange = (newSize: number) => {
    setTopicsPageSize(newSize)
    setTopicsCurrentPage(0) // Reset to first page
    fetchTopics(0, newSize)
  }

  const handleTopicsSearchAndFilter = () => {
    setTopicsCurrentPage(0) // Reset to first page when searching/filtering
    fetchTopics(0, topicsPageSize)
  }

  // Pagination handlers for notes
  const handleNotesPageChange = (newPage: number) => {
    setNotesCurrentPage(newPage)
    fetchTopicNotes(newPage, notesPageSize)
  }

  const handleNotesPageSizeChange = (newSize: number) => {
    setNotesPageSize(newSize)
    setNotesCurrentPage(0) // Reset to first page
    fetchTopicNotes(0, newSize)
  }

  const handleNotesSearchAndFilter = () => {
    setNotesCurrentPage(0) // Reset to first page when searching/filtering
    fetchTopicNotes(0, notesPageSize)
  }

  useEffect(() => {
    // Wait for authentication token to be available
    const checkAuthAndLoad = () => {
      const token = localStorage.getItem('accessToken')
      if (token) {
        // Load all data needed for forms
        fetchBoards()
        fetchSubjects(0, 5) // Start with page 0, size 5
        fetchChapters(0, 5) // Start with page 0, size 5
        fetchTopics(0, 5) // Start with page 0, size 5
        fetchTopicNotes(0, 5) // Start with page 0, size 5
        loadData() // Load data for current tab
      } else {
        // Retry after a short delay
        setTimeout(checkAuthAndLoad, 100)
      }
    }
    checkAuthAndLoad()
  }, [activeTab])

  // Handle search and filter changes for subjects pagination
  useEffect(() => {
    if (activeTab === 'subjects') {
      handleSubjectsSearchAndFilter()
    }
  }, [searchTerm, statusFilter])

  // Handle search and filter changes for chapters pagination
  useEffect(() => {
    if (activeTab === 'chapters') {
      handleChaptersSearchAndFilter()
    }
  }, [searchTerm, statusFilter])

  // Handle search and filter changes for topics pagination
  useEffect(() => {
    if (activeTab === 'topics') {
      handleTopicsSearchAndFilter()
    }
  }, [searchTerm, statusFilter])

  // Handle search and filter changes for notes pagination
  useEffect(() => {
    if (activeTab === 'notes') {
      handleNotesSearchAndFilter()
    }
  }, [searchTerm, statusFilter])

  const loadData = async () => {
    setLoading(true)
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        console.error('No access token found')
        return
      }

      // Fetch data based on active tab
      switch (activeTab) {
        case 'boards':
          await fetchBoards()
          break
        case 'subjects':
          await fetchSubjects(subjectsCurrentPage, subjectsPageSize)
          break
        case 'chapters':
          await fetchChapters(chaptersCurrentPage, chaptersPageSize)
          break
        case 'topics':
          await fetchTopics(topicsCurrentPage, topicsPageSize)
          break
        case 'notes':
          await fetchTopicNotes(notesCurrentPage, notesPageSize)
          break
      }
    } catch (error) {
      console.error('Error loading data:', error)
      toast.error('Failed to load data')
    } finally {
      setLoading(false)
    }
  }

  const fetchBoards = async () => {
    try {
      // The API may return either a Page<Board> or a plain Board[]
      const response = await api.get<Page<Board> | Board[]>('/admin/content/boards?size=100')
      const data = response.data as any
      const list: Board[] = Array.isArray(data) ? data : (data?.content ?? [])
      setBoards(list)
    } catch (error) {
      console.error('Error fetching boards:', error)
      toast.error('Failed to fetch boards')
    }
  }

  const fetchSubjects = async (page: number = subjectsCurrentPage, size: number = subjectsPageSize) => {
    const token = localStorage.getItem('accessToken')
    console.log('Fetching subjects with token:', token ? 'present' : 'missing')
    
    // Build query parameters
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy: 'name',
      sortDir: 'asc'
    })
    
    // Add active filter if not 'all'
    if (statusFilter !== 'all') {
      params.append('active', statusFilter === 'active' ? 'true' : 'false')
    }
    
    // Add search term if provided
    if (searchTerm.trim()) {
      params.append('search', searchTerm.trim())
    }
    
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/subjects?${params}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    console.log('Subjects API response status:', response.status)
    if (response.ok) {
      const data = await response.json()
      console.log('Subjects data received:', data)
      setSubjects(data.content || data)
      
      // Update pagination info
      setSubjectsTotalPages(data.totalPages || 0)
      setSubjectsTotalElements(data.totalElements || 0)
      setSubjectsCurrentPage(data.number || 0)
    } else {
      console.error('Subjects API error:', response.status, response.statusText)
    }
  }

  const fetchChapters = async (page: number = chaptersCurrentPage, size: number = chaptersPageSize) => {
    const token = localStorage.getItem('accessToken')
    console.log('Fetching chapters with token:', token ? 'present' : 'missing')
    
    // Build query parameters
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy: 'name',
      sortDir: 'asc'
    })
    
    // Add active filter if not 'all'
    if (statusFilter !== 'all') {
      params.append('active', statusFilter === 'active' ? 'true' : 'false')
    }
    
    // Add search term if provided
    if (searchTerm.trim()) {
      params.append('search', searchTerm.trim())
    }
    
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/chapters?${params}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    console.log('Chapters API response status:', response.status)
    if (response.ok) {
      const data = await response.json()
      console.log('Chapters data received:', data)
      setChapters(data.content || data)
      
      // Update pagination info
      setChaptersTotalPages(data.totalPages || 0)
      setChaptersTotalElements(data.totalElements || 0)
      setChaptersCurrentPage(data.number || 0)
    } else {
      console.error('Chapters API error:', response.status, response.statusText)
    }
  }

  const fetchTopics = async (page = 0, size = 5) => {
    const token = localStorage.getItem('accessToken')
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy: 'title',
      sortDir: 'asc'
    })
    
    if (statusFilter !== 'all') {
      params.append('active', statusFilter === 'active' ? 'true' : 'false')
    }
    
    if (searchTerm) {
      params.append('search', searchTerm)
    }
    
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/topics?${params}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    
    if (response.ok) {
      const data = await response.json()
      setTopics(data.content || data)
      
      // Update pagination info
      setTopicsTotalPages(data.totalPages || 0)
      setTopicsTotalElements(data.totalElements || 0)
      setTopicsCurrentPage(data.number || 0)
    } else {
      console.error('Topics API error:', response.status, response.statusText)
    }
  }

  const fetchTopicNotes = async (page: number = 0, size: number = 5) => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) return

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        sort: 'createdAt,desc'
      })

      // Add search filter if provided
      if (searchTerm) {
        params.append('search', searchTerm)
      }

      // Add status filter if not 'all'
      if (statusFilter !== 'all') {
        params.append('active', statusFilter === 'active' ? 'true' : 'false')
      }

      const response = await fetch(`${baseUrl}/admin/content/notes?${params}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })

      if (response.ok) {
        const data = await response.json()
        setTopicNotes(data.content || [])
        setNotesTotalPages(data.totalPages || 0)
        setNotesTotalElements(data.totalElements || 0)
        setNotesCurrentPage(data.number || 0)
      } else {
        console.error('Notes API error:', response.status, response.statusText)
      }
    } catch (error) {
      console.error('Error fetching notes:', error)
    }
  }

  const handleCreate = async () => {
    console.log('=== handleCreate START ===')
    console.log('handleCreate called with formData:', formData)
    console.log('activeTab:', activeTab)
    console.log('isCreateDialogOpen:', isCreateDialogOpen)
    
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        console.log('No token found')
        toast.error('Authentication required')
        return
      }

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const endpoint = `${baseUrl}/admin/content/${activeTab}`
      
      console.log('API Configuration:', {
        NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL,
        baseUrl,
        endpoint,
        formData,
        activeTab
      })
      
      // Prepare data for API call
      let apiData = { ...formData }
      
      // For notes, send only the required fields expected by the backend
      if (activeTab === 'notes') {
        apiData = {
          title: formData.title,
          content: formData.content,
          topicId: formData.topicId,
          attachments: formData.attachments || null,
          active: formData.active ?? true
        }
      }
      
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(apiData)
      })

      console.log('Response status:', response.status)
      
      if (response.ok) {
        const result = await response.json()
        console.log('Created item:', result)
        const itemType = activeTab.slice(0, -1) // Remove 's' from end
        console.log('Item type for message:', itemType)
        toast.success(`${itemType} created successfully`)
        setIsCreateDialogOpen(false)
        setFormData({})
        console.log('Calling loadData() to refresh table')
        loadData()
      } else {
        const errorText = await response.text()
        console.error('API Error Response:', errorText)
        try {
          const error = JSON.parse(errorText)
          console.error('Parsed API Error:', error)
          toast.error(error.message || 'Failed to create item')
        } catch (parseError) {
          console.error('Failed to parse error response:', parseError)
          toast.error(`Failed to create item (${response.status})`)
        }
      }
    } catch (error) {
      console.error('Error creating item:', error)
      toast.error('Failed to create item')
    }
    console.log('=== handleCreate END ===')
  }

  const handleBoardEdit = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication required')
        return
      }

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const endpoint = `${baseUrl}/admin/content/boards/${editingBoardId}`
      
      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(editFormData)
      })

      if (response.ok) {
        toast.success('Board updated successfully')
        setEditingBoardId(null)
        setEditFormData({})
        loadData()
      } else {
        const error = await response.json()
        toast.error(error.message || 'Failed to update board')
      }
    } catch (error) {
      console.error('Error updating board:', error)
      toast.error('Failed to update board')
    }
  }

  const handleSubjectEdit = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication required')
        return
      }

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const endpoint = `${baseUrl}/admin/content/subjects/${editingSubjectId}`
      
      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(editFormData)
      })

      if (response.ok) {
        toast.success('Subject updated successfully')
        setEditingSubjectId(null)
        setEditFormData({})
        loadData()
      } else {
        const error = await response.json()
        toast.error(error.message || 'Failed to update subject')
      }
    } catch (error) {
      console.error('Error updating subject:', error)
      toast.error('Failed to update subject')
    }
  }

  const handleChapterEdit = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication required')
        return
      }

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const endpoint = `${baseUrl}/admin/content/chapters/${editingChapterId}`
      
      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(editFormData)
      })

      if (response.ok) {
        toast.success('Chapter updated successfully')
        setEditingChapterId(null)
        setEditFormData({})
        loadData()
      } else {
        const error = await response.json()
        toast.error(error.message || 'Failed to update chapter')
      }
    } catch (error) {
      console.error('Error updating chapter:', error)
      toast.error('Failed to update chapter')
    }
  }

  const handleTopicEdit = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication required')
        return
      }

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const endpoint = `${baseUrl}/admin/content/topics/${editingTopicId}`
      
      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(editFormData)
      })

      if (response.ok) {
        toast.success('Topic updated successfully')
        setEditingTopicId(null)
        setEditFormData({})
        loadData()
      } else {
        const error = await response.json()
        toast.error(error.message || 'Failed to update topic')
      }
    } catch (error) {
      console.error('Error updating topic:', error)
      toast.error('Failed to update topic')
    }
  }

  const handleNotesEdit = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication required')
        return
      }

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const endpoint = `${baseUrl}/admin/content/notes/${editingNoteId}`
      
      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(editFormData)
      })

      if (response.ok) {
        toast.success('Note updated successfully')
        setEditingNoteId(null)
        setEditFormData({})
        loadData()
      } else {
        const error = await response.json()
        toast.error(error.message || 'Failed to update note')
      }
    } catch (error) {
      console.error('Error updating note:', error)
      toast.error('Failed to update note')
    }
  }

  const handleEditCancel = () => {
    setEditingBoardId(null)
    setEditingSubjectId(null)
    setEditingChapterId(null)
    setEditingTopicId(null)
    setEditingNoteId(null)
    setEditFormData({})
  }

  const handleDeleteClick = async (board: any) => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication required')
        return
      }

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const response = await fetch(`${baseUrl}/admin/content/boards/${board.id}/deletion-impact`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })

      if (response.ok) {
        const impact = await response.json()
        setDeletionImpact(impact)
        setBoardToDelete(board)
        setShowDeleteConfirmation(true)
      } else {
        toast.error('Failed to get deletion impact information')
      }
    } catch (error) {
      console.error('Error getting deletion impact:', error)
      toast.error('Failed to get deletion impact information')
    }
  }

  const handleConfirmDelete = async () => {
    // Check if user has confirmed the deletion
    if (!isDeletionConfirmed) {
      const itemType = activeTab === 'boards' ? 'board' : activeTab === 'subjects' ? 'subject' : activeTab === 'chapters' ? 'chapter' : activeTab === 'topics' ? 'topic' : 'note'
      toast.error(`Please confirm that you understand this action will permanently delete the ${itemType} and all associated content`)
      return
    }

    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication required')
        return
      }

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      let endpoint
      if (activeTab === 'boards') {
        endpoint = `${baseUrl}/admin/content/boards/${boardToDelete.id}?force=true`
      } else if (activeTab === 'subjects') {
        endpoint = `${baseUrl}/admin/content/subjects/${boardToDelete.id}?force=true`
      } else if (activeTab === 'chapters') {
        endpoint = `${baseUrl}/admin/content/chapters/${boardToDelete.id}?force=true`
      } else if (activeTab === 'topics') {
        endpoint = `${baseUrl}/admin/content/topics/${boardToDelete.id}?force=true`
      } else if (activeTab === 'notes') {
        endpoint = `${baseUrl}/admin/content/notes/${boardToDelete.id}`
      }
      
      const response = await fetch(endpoint, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      })

      if (response.ok) {
        const itemType = activeTab === 'boards' ? 'Board' : activeTab === 'subjects' ? 'Subject' : activeTab === 'chapters' ? 'Chapter' : activeTab === 'topics' ? 'Topic' : 'Note'
        toast.success(`${itemType} and all associated content deleted successfully`)
        setShowDeleteConfirmation(false)
        setDeletionImpact(null)
        setBoardToDelete(null)
        setIsDeletionConfirmed(false)
        loadData()
      } else {
        const error = await response.json()
        const itemType = activeTab === 'boards' ? 'board' : activeTab === 'subjects' ? 'subject' : activeTab === 'chapters' ? 'chapter' : activeTab === 'topics' ? 'topic' : 'note'
        toast.error(error.message || `Failed to delete ${itemType}`)
      }
    } catch (error) {
      console.error('Error deleting item:', error)
      const itemType = activeTab === 'boards' ? 'board' : activeTab === 'subjects' ? 'subject' : activeTab === 'chapters' ? 'chapter' : activeTab === 'topics' ? 'topic' : 'note'
      toast.error(`Failed to delete ${itemType}`)
    }
  }

  const handleSubjectDeleteClick = async (subject: any) => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication required')
        return
      }

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const response = await fetch(`${baseUrl}/admin/content/subjects/${subject.id}/deletion-impact`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })

      if (response.ok) {
        const impact = await response.json()
        setDeletionImpact(impact)
        setBoardToDelete(subject) // Reusing the same state for subject
        setShowDeleteConfirmation(true)
      } else {
        toast.error('Failed to get deletion impact information')
      }
    } catch (error) {
      console.error('Error getting deletion impact:', error)
      toast.error('Failed to get deletion impact information')
    }
  }

  const handleChapterDeleteClick = async (chapter: any) => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication required')
        return
      }

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const response = await fetch(`${baseUrl}/admin/content/chapters/${chapter.id}/deletion-impact`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })

      if (response.ok) {
        const impact = await response.json()
        setDeletionImpact(impact)
        setBoardToDelete(chapter) // Reusing the same state for chapter
        setShowDeleteConfirmation(true)
      } else {
        toast.error('Failed to get deletion impact information')
      }
    } catch (error) {
      console.error('Error getting deletion impact:', error)
      toast.error('Failed to get deletion impact information')
    }
  }

  const handleTopicDeleteClick = async (topic: any) => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication required')
        return
      }

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const response = await fetch(`${baseUrl}/admin/content/topics/${topic.id}/deletion-impact`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })

      if (response.ok) {
        const impact = await response.json()
        setDeletionImpact(impact)
        setBoardToDelete(topic) // Reusing the same state for topic
        setShowDeleteConfirmation(true)
      } else {
        toast.error('Failed to get deletion impact information')
      }
    } catch (error) {
      console.error('Error getting deletion impact:', error)
      toast.error('Failed to get deletion impact information')
    }
  }

  const handleNotesDeleteClick = async (note: any) => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication required')
        return
      }

      // For notes, we don't need to check deletion impact since they're at the end of the relationship chain
      // We can directly show a simple confirmation dialog
      setDeletionImpact({
        canDelete: true,
        totalImpact: 1,
        noteId: note.id,
        noteTitle: note.title,
        warnings: ['This note will be permanently deleted.']
      })
      setBoardToDelete(note) // Reusing the same state for note
      setShowDeleteConfirmation(true)
    } catch (error) {
      console.error('Error preparing note deletion:', error)
      toast.error('Failed to prepare note deletion')
    }
  }

  const handleCancelDelete = () => {
    setShowDeleteConfirmation(false)
    setDeletionImpact(null)
    setBoardToDelete(null)
    setIsDeletionConfirmed(false)
  }

  const handleEdit = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication required')
        return
      }

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const endpoint = `${baseUrl}/admin/content/${activeTab}/${editingItem.id}`
      
      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
      })

      if (response.ok) {
        toast.success(`${activeTab.slice(0, -1)} updated successfully`)
        setIsEditDialogOpen(false)
        setEditingItem(null)
        setFormData({})
        loadData()
      } else {
        const error = await response.json()
        toast.error(error.message || 'Failed to update item')
      }
    } catch (error) {
      console.error('Error updating item:', error)
      toast.error('Failed to update item')
    }
  }

  const handleToggleActive = async (id: number, active: boolean) => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication required')
        return
      }

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const endpoint = `${baseUrl}/admin/content/${activeTab}/${id}/active`
      
      const response = await fetch(endpoint, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ active })
      })

      if (response.ok) {
        toast.success(`Status updated successfully`)
        loadData()
      } else {
        const error = await response.json()
        toast.error(error.message || 'Failed to update status')
      }
    } catch (error) {
      console.error('Error updating status:', error)
      toast.error('Failed to update status')
    }
  }

  const handleDelete = async (id: number, force = false) => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication required')
        return
      }

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const endpoint = `${baseUrl}/admin/content/${activeTab}/${id}?force=${force}`
      
      const response = await fetch(endpoint, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })

      if (response.ok) {
        toast.success(`Item ${force ? 'permanently deleted' : 'deleted'} successfully`)
        loadData()
      } else {
        const error = await response.json()
        toast.error(error.message || 'Failed to delete item')
      }
    } catch (error) {
      console.error('Error deleting item:', error)
      toast.error('Failed to delete item')
    }
  }

  const formatTime = (minutes: number) => {
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    if (hours > 0) {
      return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`
    }
    return `${mins}m`
  }

  const getCurrentData = () => {
    switch (activeTab) {
      case 'boards': return boards
      case 'subjects': return subjects
      case 'chapters': return chapters
      case 'topics': return topics
      case 'notes': return topicNotes
      default: return []
    }
  }

  const filteredData = () => {
    // For subjects, chapters, topics, and notes, use server-side pagination, so return the data as-is
    if (activeTab === 'subjects') {
      return subjects
    }
    if (activeTab === 'chapters') {
      return chapters
    }
    if (activeTab === 'topics') {
      return topics
    }
    if (activeTab === 'notes') {
      return topicNotes
    }
    
    // For other tabs, use client-side filtering
    return getCurrentData().filter(item => {
      // Search filter
      const matchesSearch = (item as any).name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                           (item as any).title?.toLowerCase().includes(searchTerm.toLowerCase())
      
      // Status filter (only apply to boards tab)
      let matchesStatus = true
      if (activeTab === 'boards') {
        if (statusFilter === 'active') {
          matchesStatus = item.active === true
        } else if (statusFilter === 'inactive') {
          matchesStatus = item.active === false
        }
        // 'all' matches everything
      }
      
      return matchesSearch && matchesStatus
    })
  }

  const renderTable = () => {
    const data = filteredData()
    
    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
              <Input
                placeholder={`Search ${activeTab}...`}
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 w-64"
              />
            </div>
            {activeTab === 'boards' && (
              <Select value={statusFilter} onValueChange={(value: 'all' | 'active' | 'inactive') => setStatusFilter(value)}>
                <SelectTrigger className="w-40">
                  <SelectValue placeholder="Filter by status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Boards</SelectItem>
                  <SelectItem value="active">Active Only</SelectItem>
                  <SelectItem value="inactive">Inactive Only</SelectItem>
                </SelectContent>
              </Select>
            )}
          </div>
          <Dialog open={isCreateDialogOpen} onOpenChange={(open) => {
            console.log('Dialog open state changed:', open)
            if (open) {
              console.log('Dialog opening, resetting form data for tab:', activeTab)
              // Reset form data with appropriate defaults for each tab
              const defaultFormData = { active: true }
              setFormData(defaultFormData)
            }
            setIsCreateDialogOpen(open)
          }}>
            <DialogTrigger asChild>
              <Button onClick={() => {
                console.log('Add button clicked, opening dialog for tab:', activeTab)
                setFormData({ active: true }) // Reset form data with default values
                setIsCreateDialogOpen(true)
              }}>
                <Plus className="h-4 w-4 mr-2" />
                Add {activeTab.slice(0, -1)}
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Create {activeTab.slice(0, -1)}</DialogTitle>
              </DialogHeader>
              {renderCreateForm()}
            </DialogContent>
          </Dialog>
        </div>

        <Card>
          <CardContent className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="border-b">
                  <tr>
                    <th className="text-left p-4 font-medium">Name</th>
                    {activeTab === 'topics' && (
                      <>
                        <th className="text-left p-4 font-medium">Code</th>
                        <th className="text-left p-4 font-medium">Time</th>
                      </>
                    )}
                    {activeTab === 'notes' && (
                      <th className="text-left p-4 font-medium">Topic</th>
                    )}
                    <th className="text-left p-4 font-medium">Status</th>
                    <th className="text-left p-4 font-medium">Created</th>
                    <th className="text-left p-4 font-medium">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {data.map((item) => (
                    <tr key={item.id} className="border-b hover:bg-gray-50 dark:hover:bg-gray-800">
                      <td className="p-4">
                        {(activeTab === 'boards' && editingBoardId === item.id) || 
                         (activeTab === 'subjects' && editingSubjectId === item.id) ||
                         (activeTab === 'chapters' && editingChapterId === item.id) ||
                         (activeTab === 'topics' && editingTopicId === item.id) ||
                         (activeTab === 'notes' && editingNoteId === item.id) ? (
                          <div className="space-y-2">
                            {activeTab === 'topics' ? (
                              <div className="space-y-3">
                                <div>
                                  <Input
                                    value={editFormData.title || ''}
                                    onChange={(e) => setEditFormData({...editFormData, title: e.target.value})}
                                    placeholder="Topic title"
                                    className="w-full"
                                  />
                                </div>
                                <div>
                                  <Input
                                    type="number"
                                    value={editFormData.expectedTimeMins || ''}
                                    onChange={(e) => setEditFormData({...editFormData, expectedTimeMins: parseInt(e.target.value) || 0})}
                                    placeholder="Expected time (minutes)"
                                    className="w-full"
                                  />
                                </div>
                                <div>
                                  <Textarea
                                    value={editFormData.description || ''}
                                    onChange={(e) => setEditFormData({...editFormData, description: e.target.value})}
                                    placeholder="Description"
                                    className="w-full"
                                    rows={2}
                                  />
                                </div>
                                <div>
                                  <Textarea
                                    value={editFormData.summary || ''}
                                    onChange={(e) => setEditFormData({...editFormData, summary: e.target.value})}
                                    placeholder="Summary"
                                    className="w-full"
                                    rows={2}
                                  />
                                </div>
                              </div>
                            ) : activeTab === 'notes' ? (
                              <div className="space-y-3">
                                <div>
                                  <Input
                                    value={editFormData.title || ''}
                                    onChange={(e) => setEditFormData({...editFormData, title: e.target.value})}
                                    placeholder="Note title"
                                    className="w-full"
                                  />
                                </div>
                                <div>
                                  <Textarea
                                    value={editFormData.content || ''}
                                    onChange={(e) => setEditFormData({...editFormData, content: e.target.value})}
                                    placeholder="Note content"
                                    className="w-full"
                                    rows={3}
                                  />
                                </div>
                              </div>
                            ) : (
                              <Input
                                value={editFormData.name || ''}
                                onChange={(e) => setEditFormData({...editFormData, name: e.target.value})}
                                placeholder={`${activeTab.slice(0, -1)} name`}
                                className="w-full"
                              />
                            )}
                          </div>
                        ) : (
                          <div>
                            <div className="font-medium">{(item as any).name || (item as any).title}</div>
                            {(item as any).description && (
                              <div className="text-sm text-gray-500">{(item as any).description}</div>
                            )}
                          </div>
                        )}
                      </td>
                      {activeTab === 'topics' && (
                        <>
                          <td className="p-4">{(item as any).code || '-'}</td>
                          <td className="p-4">{formatTime((item as any).expectedTimeMins)}</td>
                        </>
                      )}
                      {activeTab === 'notes' && (
                        <td className="p-4">
                          {topics.find(t => t.id === (item as any).topicId)?.title || '-'}
                        </td>
                      )}
                      <td className="p-4">
                        {(activeTab === 'boards' && editingBoardId === item.id) || 
                         (activeTab === 'subjects' && editingSubjectId === item.id) ||
                         (activeTab === 'chapters' && editingChapterId === item.id) ||
                         (activeTab === 'topics' && editingTopicId === item.id) ||
                         (activeTab === 'notes' && editingNoteId === item.id) ? (
                          <div className="flex items-center space-x-2">
                            <Switch
                              checked={editFormData.active ?? item.active}
                              onCheckedChange={(checked) => setEditFormData({...editFormData, active: checked})}
                            />
                            <span className="text-sm">{editFormData.active ? 'Active' : 'Inactive'}</span>
                          </div>
                        ) : (
                          <Badge variant={item.active ? 'default' : 'secondary'} data-testid="status-badge">
                            {item.active ? 'Active' : 'Inactive'}
                          </Badge>
                        )}
                      </td>
                      <td className="p-4 text-sm text-gray-500">
                        {new Date(item.createdAt).toLocaleDateString()}
                      </td>
                      <td className="p-4">
                        {(activeTab === 'boards' && editingBoardId === item.id) || 
                         (activeTab === 'subjects' && editingSubjectId === item.id) ||
                         (activeTab === 'chapters' && editingChapterId === item.id) ||
                         (activeTab === 'topics' && editingTopicId === item.id) ||
                         (activeTab === 'notes' && editingNoteId === item.id) ? (
                          <div className="flex items-center space-x-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={handleEditCancel}
                            >
                              Cancel
                            </Button>
                            <Button
                              size="sm"
                              onClick={activeTab === 'boards' ? handleBoardEdit : 
                                       activeTab === 'subjects' ? handleSubjectEdit : 
                                       activeTab === 'chapters' ? handleChapterEdit :
                                       activeTab === 'topics' ? handleTopicEdit :
                                       handleNotesEdit}
                            >
                              Update
                            </Button>
                          </div>
                        ) : (
                          <div className="flex items-center space-x-2">
                            <Button
                              variant="ghost"
                              size="sm"
                              title={`Edit ${activeTab.slice(0, -1)}`}
                              onClick={() => {
                                if (activeTab === 'boards') {
                                  setEditingBoardId(item.id)
                                  setEditFormData({ name: (item as any).name, active: item.active })
                                } else if (activeTab === 'subjects') {
                                  setEditingSubjectId(item.id)
                                  setEditFormData({ name: (item as any).name, active: item.active })
                                } else if (activeTab === 'chapters') {
                                  setEditingChapterId(item.id)
                                  setEditFormData({ name: (item as any).name, active: item.active })
                                } else if (activeTab === 'topics') {
                                  setEditingTopicId(item.id)
                                  setEditFormData({ 
                                    title: (item as any).title, 
                                    code: (item as any).code,
                                    description: (item as any).description,
                                    summary: (item as any).summary,
                                    expectedTimeMins: (item as any).expectedTimeMins,
                                    active: item.active,
                                    boardId: (item as any).boardId,
                                    subjectId: (item as any).subjectId,
                                    chapterId: (item as any).chapterId
                                  })
                                } else if (activeTab === 'notes') {
                                  setEditingNoteId(item.id)
                                  setEditFormData({ 
                                    title: (item as any).title, 
                                    content: (item as any).content,
                                    active: item.active,
                                    topicId: (item as any).topicId
                                  })
                                } else {
                                  setEditingItem(item)
                                  setFormData(item)
                                  setIsEditDialogOpen(true)
                                }
                              }}
                            >
                              <Edit className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              title={item.active ? `Deactivate ${activeTab.slice(0, -1)}` : `Activate ${activeTab.slice(0, -1)}`}
                              onClick={() => handleToggleActive(item.id, !item.active)}
                            >
                              {item.active ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              title={`Delete ${activeTab.slice(0, -1)}`}
                              onClick={() => {
                                if (activeTab === 'boards') {
                                  handleDeleteClick(item)
                                } else if (activeTab === 'subjects') {
                                  handleSubjectDeleteClick(item)
                                } else if (activeTab === 'chapters') {
                                  handleChapterDeleteClick(item)
                                } else if (activeTab === 'topics') {
                                  handleTopicDeleteClick(item)
                                } else if (activeTab === 'notes') {
                                  handleNotesDeleteClick(item)
                                } else {
                                  handleDelete(item.id)
                                }
                              }}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            
            {/* Pagination Controls for Subjects */}
            {activeTab === 'subjects' && subjectsTotalPages > 1 && (
              <div className="flex items-center justify-between px-4 py-3 border-t">
                <div className="flex items-center space-x-4">
                  <div className="flex items-center space-x-2">
                    <span className="text-sm text-gray-700 dark:text-gray-300">Show</span>
                    <select
                      value={subjectsPageSize}
                      onChange={(e) => handleSubjectsPageSizeChange(Number(e.target.value))}
                      className="border border-gray-300 dark:border-gray-600 rounded px-2 py-1 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                    >
                      <option value={5}>5</option>
                      <option value={10}>10</option>
                      <option value={20}>20</option>
                      <option value={50}>50</option>
                    </select>
                    <span className="text-sm text-gray-700 dark:text-gray-300">per page</span>
                  </div>
                  <div className="text-sm text-gray-700 dark:text-gray-300">
                    Showing {subjectsCurrentPage * subjectsPageSize + 1} to {Math.min((subjectsCurrentPage + 1) * subjectsPageSize, subjectsTotalElements)} of {subjectsTotalElements} results
                  </div>
                </div>
                
                <div className="flex items-center space-x-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleSubjectsPageChange(subjectsCurrentPage - 1)}
                    disabled={subjectsCurrentPage === 0}
                  >
                    Previous
                  </Button>
                  
                  <div className="flex items-center space-x-1">
                    {Array.from({ length: Math.min(5, subjectsTotalPages) }, (_, i) => {
                      let pageNum;
                      if (subjectsTotalPages <= 5) {
                        pageNum = i;
                      } else if (subjectsCurrentPage < 3) {
                        pageNum = i;
                      } else if (subjectsCurrentPage >= subjectsTotalPages - 3) {
                        pageNum = subjectsTotalPages - 5 + i;
                      } else {
                        pageNum = subjectsCurrentPage - 2 + i;
                      }
                      
                      return (
                        <Button
                          key={pageNum}
                          variant={subjectsCurrentPage === pageNum ? "default" : "outline"}
                          size="sm"
                          onClick={() => handleSubjectsPageChange(pageNum)}
                          className="w-8 h-8 p-0"
                        >
                          {pageNum + 1}
                        </Button>
                      );
                    })}
                  </div>
                  
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleSubjectsPageChange(subjectsCurrentPage + 1)}
                    disabled={subjectsCurrentPage >= subjectsTotalPages - 1}
                  >
                    Next
                  </Button>
                </div>
              </div>
            )}

            {/* Pagination Controls for Chapters */}
            {activeTab === 'chapters' && chaptersTotalPages > 1 && (
              <div className="flex items-center justify-between px-4 py-3 border-t">
                <div className="flex items-center space-x-4">
                  <div className="flex items-center space-x-2">
                    <span className="text-sm text-gray-700 dark:text-gray-300">Show</span>
                    <select
                      value={chaptersPageSize}
                      onChange={(e) => handleChaptersPageSizeChange(Number(e.target.value))}
                      className="border border-gray-300 dark:border-gray-600 rounded px-2 py-1 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                    >
                      <option value={5}>5</option>
                      <option value={10}>10</option>
                      <option value={20}>20</option>
                      <option value={50}>50</option>
                    </select>
                    <span className="text-sm text-gray-700 dark:text-gray-300">per page</span>
                  </div>
                  <div className="text-sm text-gray-700 dark:text-gray-300">
                    Showing {chaptersCurrentPage * chaptersPageSize + 1} to {Math.min((chaptersCurrentPage + 1) * chaptersPageSize, chaptersTotalElements)} of {chaptersTotalElements} results
                  </div>
                </div>
                
                <div className="flex items-center space-x-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleChaptersPageChange(chaptersCurrentPage - 1)}
                    disabled={chaptersCurrentPage === 0}
                  >
                    Previous
                  </Button>
                  
                  <div className="flex items-center space-x-1">
                    {Array.from({ length: Math.min(5, chaptersTotalPages) }, (_, i) => {
                      let pageNum;
                      if (chaptersTotalPages <= 5) {
                        pageNum = i;
                      } else if (chaptersCurrentPage < 3) {
                        pageNum = i;
                      } else if (chaptersCurrentPage >= chaptersTotalPages - 3) {
                        pageNum = chaptersTotalPages - 5 + i;
                      } else {
                        pageNum = chaptersCurrentPage - 2 + i;
                      }
                      
                      return (
                        <Button
                          key={pageNum}
                          variant={chaptersCurrentPage === pageNum ? "default" : "outline"}
                          size="sm"
                          onClick={() => handleChaptersPageChange(pageNum)}
                          className="w-8 h-8 p-0"
                        >
                          {pageNum + 1}
                        </Button>
                      );
                    })}
                  </div>
                  
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleChaptersPageChange(chaptersCurrentPage + 1)}
                    disabled={chaptersCurrentPage >= chaptersTotalPages - 1}
                  >
                    Next
                  </Button>
                </div>
              </div>
            )}

            {/* Pagination Controls for Topics */}
            {activeTab === 'topics' && topicsTotalPages > 1 && (
              <div className="flex items-center justify-between px-4 py-3 border-t">
                <div className="flex items-center space-x-4">
                  <div className="flex items-center space-x-2">
                    <span className="text-sm text-gray-700 dark:text-gray-300">Show</span>
                    <select
                      value={topicsPageSize}
                      onChange={(e) => handleTopicsPageSizeChange(Number(e.target.value))}
                      className="border border-gray-300 dark:border-gray-600 rounded px-2 py-1 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                    >
                      <option value={5}>5</option>
                      <option value={10}>10</option>
                      <option value={20}>20</option>
                      <option value={50}>50</option>
                    </select>
                    <span className="text-sm text-gray-700 dark:text-gray-300">per page</span>
                  </div>
                  <div className="text-sm text-gray-700 dark:text-gray-300">
                    Showing {topicsCurrentPage * topicsPageSize + 1} to {Math.min((topicsCurrentPage + 1) * topicsPageSize, topicsTotalElements)} of {topicsTotalElements} results
                  </div>
                </div>
                
                <div className="flex items-center space-x-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleTopicsPageChange(topicsCurrentPage - 1)}
                    disabled={topicsCurrentPage === 0}
                  >
                    Previous
                  </Button>
                  
                  <div className="flex items-center space-x-1">
                    {Array.from({ length: Math.min(5, topicsTotalPages) }, (_, i) => {
                      let pageNum;
                      if (topicsTotalPages <= 5) {
                        pageNum = i;
                      } else if (topicsCurrentPage < 3) {
                        pageNum = i;
                      } else if (topicsCurrentPage >= topicsTotalPages - 3) {
                        pageNum = topicsTotalPages - 5 + i;
                      } else {
                        pageNum = topicsCurrentPage - 2 + i;
                      }
                      
                      return (
                        <Button
                          key={pageNum}
                          variant={topicsCurrentPage === pageNum ? "default" : "outline"}
                          size="sm"
                          onClick={() => handleTopicsPageChange(pageNum)}
                          className="w-8 h-8 p-0"
                        >
                          {pageNum + 1}
                        </Button>
                      );
                    })}
                  </div>
                  
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleTopicsPageChange(topicsCurrentPage + 1)}
                    disabled={topicsCurrentPage >= topicsTotalPages - 1}
                  >
                    Next
                  </Button>
                </div>
              </div>
            )}

            {/* Pagination Controls for Notes */}
            {activeTab === 'notes' && notesTotalPages > 1 && (
              <div className="flex items-center justify-between px-4 py-3 border-t">
                <div className="flex items-center space-x-4">
                  <div className="flex items-center space-x-2">
                    <span className="text-sm text-gray-700 dark:text-gray-300">Show</span>
                    <select
                      value={notesPageSize}
                      onChange={(e) => handleNotesPageSizeChange(Number(e.target.value))}
                      className="border border-gray-300 dark:border-gray-600 rounded px-2 py-1 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                    >
                      <option value={5}>5</option>
                      <option value={10}>10</option>
                      <option value={20}>20</option>
                      <option value={50}>50</option>
                    </select>
                    <span className="text-sm text-gray-700 dark:text-gray-300">per page</span>
                  </div>
                  <div className="text-sm text-gray-700 dark:text-gray-300">
                    Showing {notesCurrentPage * notesPageSize + 1} to {Math.min((notesCurrentPage + 1) * notesPageSize, notesTotalElements)} of {notesTotalElements} results
                  </div>
                </div>
                
                <div className="flex items-center space-x-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleNotesPageChange(notesCurrentPage - 1)}
                    disabled={notesCurrentPage === 0}
                  >
                    Previous
                  </Button>
                  
                  <div className="flex items-center space-x-1">
                    {Array.from({ length: Math.min(5, notesTotalPages) }, (_, i) => {
                      let pageNum;
                      if (notesTotalPages <= 5) {
                        pageNum = i;
                      } else if (notesCurrentPage < 3) {
                        pageNum = i;
                      } else if (notesCurrentPage >= notesTotalPages - 3) {
                        pageNum = notesTotalPages - 5 + i;
                      } else {
                        pageNum = notesCurrentPage - 2 + i;
                      }
                      
                      return (
                        <Button
                          key={pageNum}
                          variant={notesCurrentPage === pageNum ? "default" : "outline"}
                          size="sm"
                          onClick={() => handleNotesPageChange(pageNum)}
                          className="w-8 h-8 p-0"
                        >
                          {pageNum + 1}
                        </Button>
                      );
                    })}
                  </div>
                  
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleNotesPageChange(notesCurrentPage + 1)}
                    disabled={notesCurrentPage >= notesTotalPages - 1}
                  >
                    Next
                  </Button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    )
  }

  const renderCreateForm = () => {
    console.log('renderCreateForm called for activeTab:', activeTab)
    console.log('Current formData:', formData)
    return (
      <form onSubmit={(e) => {
        e.preventDefault()
        console.log('=== FORM SUBMIT EVENT ===')
        console.log('Form submitted via onSubmit handler')
        console.log('Current formData:', formData)
        console.log('Current activeTab:', activeTab)
        
        // Validate required fields
        if (activeTab === 'subjects') {
          if (!formData.name?.trim()) {
            console.log('Validation failed: subject name is required', { formData })
            toast.error('Subject name is required')
            return
          }
          if (!formData.boardId) {
            console.log('Validation failed: board selection is required', { formData })
            toast.error('Please select a board')
            return
          }
        }
        
        if (activeTab === 'boards' && !formData.name?.trim()) {
          console.log('Validation failed: board name is required', { formData })
          toast.error('Board name is required')
          return
        }

        if (activeTab === 'chapters') {
          if (!formData.name?.trim()) {
            console.log('Validation failed: chapter name is required', { formData })
            toast.error('Chapter name is required')
            return
          }
          if (!formData.boardId) {
            console.log('Validation failed: board selection is required', { formData })
            toast.error('Please select a board')
            return
          }
          if (!formData.subjectId) {
            console.log('Validation failed: subject selection is required', { formData })
            toast.error('Please select a subject')
            return
          }
        }

        if (activeTab === 'topics') {
          if (!formData.title?.trim()) {
            console.log('Validation failed: topic title is required', { formData })
            toast.error('Topic title is required')
            return
          }
          if (!formData.boardId) {
            console.log('Validation failed: board selection is required', { formData })
            toast.error('Please select a board')
            return
          }
          if (!formData.subjectId) {
            console.log('Validation failed: subject selection is required', { formData })
            toast.error('Please select a subject')
            return
          }
          if (!formData.chapterId) {
            console.log('Validation failed: chapter selection is required', { formData })
            toast.error('Please select a chapter')
            return
          }
          if (!formData.expectedTimeMins || formData.expectedTimeMins <= 0) {
            console.log('Validation failed: expected time is required', { formData })
            toast.error('Expected time must be greater than 0')
            return
          }
        }

        if (activeTab === 'notes') {
          if (!formData.title?.trim()) {
            console.log('Validation failed: note title is required', { formData })
            toast.error('Note title is required')
            return
          }
          if (!formData.content?.trim()) {
            console.log('Validation failed: note content is required', { formData })
            toast.error('Note content is required')
            return
          }
          if (!formData.boardId) {
            console.log('Validation failed: board selection is required', { formData })
            toast.error('Please select a board')
            return
          }
          if (!formData.subjectId) {
            console.log('Validation failed: subject selection is required', { formData })
            toast.error('Please select a subject')
            return
          }
          if (!formData.chapterId) {
            console.log('Validation failed: chapter selection is required', { formData })
            toast.error('Please select a chapter')
            return
          }
          if (!formData.topicId) {
            console.log('Validation failed: topic selection is required', { formData })
            toast.error('Please select a topic')
            return
          }
        }
        
        console.log('Validation passed, calling handleCreate')
        handleCreate()
      }}>
        <div className="space-y-4">
        {activeTab === 'boards' && (
          <>
            <div>
              <Label htmlFor="name">Board Name</Label>
              <Input
                id="name"
                value={formData.name || ''}
                onChange={(e) => setFormData({...formData, name: e.target.value})}
                placeholder="Enter board name"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="active"
                checked={formData.active ?? true}
                onCheckedChange={(checked) => setFormData({...formData, active: checked})}
              />
              <Label htmlFor="active">Active</Label>
            </div>
          </>
        )}

        {activeTab === 'subjects' && (
          <>
            <div>
              <Label htmlFor="boardId">Board</Label>
              <Select
                value={formData.boardId?.toString() || ''}
                onValueChange={(value) => setFormData({...formData, boardId: parseInt(value)})}
              >
                <SelectTrigger data-testid="board-select">
                  <SelectValue placeholder="Select board" />
                </SelectTrigger>
                <SelectContent>
                  {boards.filter(board => board.active).map(board => (
                    <SelectItem key={board.id} value={board.id.toString()}>
                      {board.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="name">Subject Name</Label>
              <Input
                id="name"
                value={formData.name || ''}
                onChange={(e) => setFormData({...formData, name: e.target.value})}
                placeholder="Enter subject name"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="active"
                checked={formData.active ?? true}
                onCheckedChange={(checked) => setFormData({...formData, active: checked})}
              />
              <Label htmlFor="active">Active</Label>
            </div>
          </>
        )}

        {activeTab === 'chapters' && (
          <>
            <div>
              <Label htmlFor="boardId">Board</Label>
              <Select
                value={formData.boardId?.toString() || ''}
                onValueChange={(value) => {
                  setFormData({...formData, boardId: parseInt(value), subjectId: null})
                }}
              >
                <SelectTrigger data-testid="board-select">
                  <SelectValue placeholder="Select board" />
                </SelectTrigger>
                <SelectContent>
                  {boards.filter(board => board.active).map(board => (
                    <SelectItem key={board.id} value={board.id.toString()}>
                      {board.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="subjectId">Subject</Label>
              <Select
                value={formData.subjectId?.toString() || ''}
                onValueChange={(value) => setFormData({...formData, subjectId: parseInt(value)})}
                disabled={!formData.boardId}
              >
                <SelectTrigger data-testid="subject-select">
                  <SelectValue placeholder={formData.boardId ? "Select subject" : "Select board first"} />
                </SelectTrigger>
                <SelectContent>
                  {subjects.filter(subject => subject.active && subject.boardId === formData.boardId).map(subject => (
                    <SelectItem key={subject.id} value={subject.id.toString()}>
                      {subject.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="name">Chapter Name</Label>
              <Input
                id="name"
                value={formData.name || ''}
                onChange={(e) => setFormData({...formData, name: e.target.value})}
                placeholder="Enter chapter name"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="active"
                checked={formData.active ?? true}
                onCheckedChange={(checked) => setFormData({...formData, active: checked})}
              />
              <Label htmlFor="active">Active</Label>
            </div>
          </>
        )}

        {activeTab === 'topics' && (
          <>
            <div>
              <Label htmlFor="boardId">Board</Label>
              <Select
                value={formData.boardId?.toString() || ''}
                onValueChange={(value) => {
                  setFormData({...formData, boardId: parseInt(value), subjectId: null, chapterId: null})
                }}
              >
                <SelectTrigger data-testid="board-select">
                  <SelectValue placeholder="Select board" />
                </SelectTrigger>
                <SelectContent>
                  {boards.filter(board => board.active).map(board => (
                    <SelectItem key={board.id} value={board.id.toString()}>
                      {board.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="subjectId">Subject</Label>
              <Select
                value={formData.subjectId?.toString() || ''}
                onValueChange={(value) => {
                  setFormData({...formData, subjectId: parseInt(value), chapterId: null})
                }}
                disabled={!formData.boardId}
              >
                <SelectTrigger data-testid="subject-select">
                  <SelectValue placeholder="Select subject" />
                </SelectTrigger>
                <SelectContent>
                  {subjects.filter(subject => subject.active && subject.boardId === formData.boardId).map(subject => (
                    <SelectItem key={subject.id} value={subject.id.toString()}>
                      {subject.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="chapterId">Chapter</Label>
              <Select
                value={formData.chapterId?.toString() || ''}
                onValueChange={(value) => setFormData({...formData, chapterId: parseInt(value)})}
                disabled={!formData.subjectId}
              >
                <SelectTrigger data-testid="chapter-select">
                  <SelectValue placeholder="Select chapter" />
                </SelectTrigger>
                <SelectContent>
                  {chapters.filter(chapter => chapter.active && chapter.subjectId === formData.subjectId).map(chapter => (
                    <SelectItem key={chapter.id} value={chapter.id.toString()}>
                      {chapter.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="title">Topic Title</Label>
              <Input
                id="title"
                value={formData.title || ''}
                onChange={(e) => setFormData({...formData, title: e.target.value})}
                placeholder="Enter topic title"
              />
            </div>
            <div>
              <Label htmlFor="expectedTimeMins">Expected Time (minutes)</Label>
              <Input
                id="expectedTimeMins"
                type="number"
                value={formData.expectedTimeMins || ''}
                onChange={(e) => setFormData({...formData, expectedTimeMins: parseInt(e.target.value)})}
                placeholder="Enter expected time in minutes"
              />
            </div>
            <div>
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                value={formData.description || ''}
                onChange={(e) => setFormData({...formData, description: e.target.value})}
                placeholder="Enter description"
              />
            </div>
            <div>
              <Label htmlFor="summary">Summary</Label>
              <Textarea
                id="summary"
                value={formData.summary || ''}
                onChange={(e) => setFormData({...formData, summary: e.target.value})}
                placeholder="Enter summary"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="active"
                checked={formData.active ?? true}
                onCheckedChange={(checked) => setFormData({...formData, active: checked})}
              />
              <Label htmlFor="active">Active</Label>
            </div>
          </>
        )}

        {activeTab === 'notes' && (
          <>
            <div>
              <Label htmlFor="boardId">Board</Label>
              <Select
                value={formData.boardId?.toString() || ''}
                onValueChange={(value) => {
                  setFormData({...formData, boardId: parseInt(value), subjectId: null, chapterId: null, topicId: null})
                }}
              >
                <SelectTrigger data-testid="board-select">
                  <SelectValue placeholder="Select board" />
                </SelectTrigger>
                <SelectContent>
                  {boards.filter(board => board.active).map(board => (
                    <SelectItem key={board.id} value={board.id.toString()}>
                      {board.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="subjectId">Subject</Label>
              <Select
                value={formData.subjectId?.toString() || ''}
                onValueChange={(value) => {
                  setFormData({...formData, subjectId: parseInt(value), chapterId: null, topicId: null})
                }}
                disabled={!formData.boardId}
              >
                <SelectTrigger data-testid="subject-select">
                  <SelectValue placeholder={formData.boardId ? "Select subject" : "Select board first"} />
                </SelectTrigger>
                <SelectContent>
                  {subjects.filter(subject => subject.active && subject.boardId === formData.boardId).map(subject => (
                    <SelectItem key={subject.id} value={subject.id.toString()}>
                      {subject.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="chapterId">Chapter</Label>
              <Select
                value={formData.chapterId?.toString() || ''}
                onValueChange={(value) => {
                  setFormData({...formData, chapterId: parseInt(value), topicId: null})
                }}
                disabled={!formData.subjectId}
              >
                <SelectTrigger data-testid="chapter-select">
                  <SelectValue placeholder={formData.subjectId ? "Select chapter" : "Select subject first"} />
                </SelectTrigger>
                <SelectContent>
                  {chapters.filter(chapter => chapter.active && chapter.subjectId === formData.subjectId).map(chapter => (
                    <SelectItem key={chapter.id} value={chapter.id.toString()}>
                      {chapter.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="topicId">Topic</Label>
              <Select
                value={formData.topicId?.toString() || ''}
                onValueChange={(value) => setFormData({...formData, topicId: parseInt(value)})}
                disabled={!formData.chapterId}
              >
                <SelectTrigger data-testid="topic-select">
                  <SelectValue placeholder={formData.chapterId ? "Select topic" : "Select chapter first"} />
                </SelectTrigger>
                <SelectContent>
                  {topics.filter(topic => topic.active && topic.chapterId === formData.chapterId).map(topic => (
                    <SelectItem key={topic.id} value={topic.id.toString()}>
                      {topic.title}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="title">Note Title</Label>
              <Input
                id="title"
                value={formData.title || ''}
                onChange={(e) => setFormData({...formData, title: e.target.value})}
                placeholder="Enter note title"
              />
            </div>
            <div>
              <Label htmlFor="content">Content</Label>
              <Textarea
                id="content"
                value={formData.content || ''}
                onChange={(e) => setFormData({...formData, content: e.target.value})}
                placeholder="Enter note content"
                rows={6}
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="active"
                checked={formData.active ?? true}
                onCheckedChange={(checked) => setFormData({...formData, active: checked})}
              />
              <Label htmlFor="active">Active</Label>
            </div>
          </>
        )}

        <div className="flex justify-end space-x-2">
          <Button variant="outline" type="button" onClick={() => setIsCreateDialogOpen(false)}>
            Cancel
          </Button>
          <Button 
            type="submit" 
            data-testid="create-button"
          >
            Create
          </Button>
        </div>
        </div>
      </form>
    )
  }

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Content Management</h1>
            <p className="text-gray-600 dark:text-gray-400">Manage educational content hierarchy</p>
          </div>
        </div>
        <div className="animate-pulse space-y-4">
          <div className="h-16 bg-gray-200 dark:bg-gray-700 rounded"></div>
          <div className="h-16 bg-gray-200 dark:bg-gray-700 rounded"></div>
          <div className="h-16 bg-gray-200 dark:bg-gray-700 rounded"></div>
        </div>
      </div>
    )
  }

  return (
    <AdminLayoutSimple>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Content Structure Management</h1>
            <p className="text-gray-600 dark:text-gray-400">Manage educational content hierarchy and structure</p>
          </div>
        </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-5">
          <TabsTrigger value="boards" className="flex items-center gap-2">
            <BookOpen className="h-4 w-4" />
            Boards
          </TabsTrigger>
          <TabsTrigger value="subjects" className="flex items-center gap-2">
            <GraduationCap className="h-4 w-4" />
            Subjects
          </TabsTrigger>
          <TabsTrigger value="chapters" className="flex items-center gap-2">
            <FileText className="h-4 w-4" />
            Chapters
          </TabsTrigger>
          <TabsTrigger value="topics" className="flex items-center gap-2">
            <Users className="h-4 w-4" />
            Topics
          </TabsTrigger>
          <TabsTrigger value="notes" className="flex items-center gap-2">
            <FileText className="h-4 w-4" />
            Notes
          </TabsTrigger>
        </TabsList>

        <TabsContent value="boards">
          {renderTable()}
        </TabsContent>

        <TabsContent value="subjects">
          {renderTable()}
        </TabsContent>

        <TabsContent value="chapters">
          {renderTable()}
        </TabsContent>

        <TabsContent value="topics">
          {renderTable()}
        </TabsContent>

        <TabsContent value="notes">
          {renderTable()}
        </TabsContent>
      </Tabs>

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit {activeTab.slice(0, -1)}</DialogTitle>
          </DialogHeader>
          {renderCreateForm()}
          <div className="flex justify-end space-x-2">
            <Button variant="outline" onClick={() => setIsEditDialogOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleEdit}>
              Update
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Deletion Confirmation Dialog */}
      {showDeleteConfirmation && deletionImpact && boardToDelete && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-2xl w-full mx-4">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                Confirm {activeTab === 'boards' ? 'Board' : activeTab === 'subjects' ? 'Subject' : 'Chapter'} Deletion
              </h3>
              <Button variant="outline" onClick={handleCancelDelete}>
                <XCircle className="h-4 w-4" />
              </Button>
            </div>
            
            <div className="space-y-4">
              {/* Item Information */}
              <div className="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
                <h4 className="font-medium text-gray-900 dark:text-white mb-2">
                  {activeTab === 'boards' ? 'Board' : activeTab === 'subjects' ? 'Subject' : 'Chapter'} to Delete
                </h4>
                <p className="text-sm text-gray-600 dark:text-gray-400">
                  <strong>Name:</strong> {deletionImpact.boardName || deletionImpact.subjectName || deletionImpact.chapterName}
                </p>
                <p className="text-sm text-gray-600 dark:text-gray-400">
                  <strong>ID:</strong> {deletionImpact.boardId || deletionImpact.subjectId || deletionImpact.chapterId}
                </p>
              </div>

              {/* Impact Summary */}
              <div className="bg-red-50 dark:bg-red-900/20 rounded-lg p-4">
                <div className="flex items-center mb-2">
                  <AlertTriangle className="h-5 w-5 text-red-600 dark:text-red-400 mr-2" />
                  <h4 className="font-medium text-red-800 dark:text-red-200">Deletion Impact</h4>
                </div>
                <p className="text-sm text-red-700 dark:text-red-300 mb-3">
                  Deleting this {activeTab === 'boards' ? 'board' : activeTab === 'subjects' ? 'subject' : 'chapter'} will affect the following content:
                </p>
                
                <div className="space-y-2">
                  {activeTab === 'boards' && deletionImpact.pricingRulesCount > 0 && (
                    <div className="text-sm text-red-700 dark:text-red-300">
                      • <strong>{deletionImpact.pricingRulesCount}</strong> pricing rule(s) will be deleted
                    </div>
                  )}
                  {activeTab === 'boards' && deletionImpact.studentProfilesCount > 0 && (
                    <div className="text-sm text-red-700 dark:text-red-300">
                      • <strong>{deletionImpact.studentProfilesCount}</strong> student profile(s) will have their educational board cleared
                    </div>
                  )}
                  {activeTab === 'boards' && deletionImpact.courseContentCount > 0 && (
                    <div className="text-sm text-red-700 dark:text-red-300">
                      • <strong>{deletionImpact.courseContentCount}</strong> course content record(s) will have their educational board cleared
                    </div>
                  )}
                  {activeTab === 'subjects' && deletionImpact.chaptersCount > 0 && (
                    <div className="text-sm text-red-700 dark:text-red-300">
                      • <strong>{deletionImpact.chaptersCount}</strong> chapter(s) will be deleted
                    </div>
                  )}
                  {activeTab === 'chapters' && deletionImpact.topicsCount > 0 && (
                    <div className="text-sm text-red-700 dark:text-red-300">
                      • <strong>{deletionImpact.topicsCount}</strong> topic(s) will be deleted
                    </div>
                  )}
                  {activeTab === 'chapters' && deletionImpact.notesCount > 0 && (
                    <div className="text-sm text-red-700 dark:text-red-300">
                      • <strong>{deletionImpact.notesCount}</strong> note(s) will be deleted
                    </div>
                  )}
                </div>
                
                <div className="mt-3 p-3 bg-red-100 dark:bg-red-800 rounded">
                  <p className="text-sm font-medium text-red-800 dark:text-red-200">
                    Total Impact: {deletionImpact.totalImpact} record(s) will be affected
                  </p>
                </div>
              </div>

              {/* Warning Message */}
              <div className="bg-yellow-50 dark:bg-yellow-900/20 rounded-lg p-4">
                <div className="flex items-center mb-2">
                  <AlertCircle className="h-5 w-5 text-yellow-600 dark:text-yellow-400 mr-2" />
                  <h4 className="font-medium text-yellow-800 dark:text-yellow-200">Important Warning</h4>
                </div>
                <p className="text-sm text-yellow-700 dark:text-yellow-300">
                  This action cannot be undone. The {activeTab === 'boards' ? 'board' : activeTab === 'subjects' ? 'subject' : activeTab === 'chapters' ? 'chapter' : 'topic'} and all associated content will be permanently removed from the system.
                  Please ensure you have backed up any important data before proceeding.
                </p>
              </div>

              {/* Confirmation Checkbox */}
              <div className="flex items-center space-x-2">
                <input
                  type="checkbox"
                  id="confirm-deletion"
                  className="rounded border-gray-300"
                  checked={isDeletionConfirmed}
                  onChange={(e) => setIsDeletionConfirmed(e.target.checked)}
                />
                <label htmlFor="confirm-deletion" className="text-sm text-gray-700 dark:text-gray-300">
                  I understand that this action will permanently delete the {activeTab === 'boards' ? 'board' : activeTab === 'subjects' ? 'subject' : activeTab === 'chapters' ? 'chapter' : 'topic'} and all associated content
                </label>
              </div>
            </div>

            <div className="flex justify-end mt-6 space-x-3">
              <Button variant="outline" onClick={handleCancelDelete}>
                Cancel
              </Button>
              <Button 
                onClick={handleConfirmDelete}
                disabled={!isDeletionConfirmed}
                className={`text-white ${
                  isDeletionConfirmed 
                    ? 'bg-red-600 hover:bg-red-700' 
                    : 'bg-gray-400 cursor-not-allowed'
                }`}
              >
                Delete {activeTab === 'boards' ? 'Board' : activeTab === 'subjects' ? 'Subject' : activeTab === 'chapters' ? 'Chapter' : activeTab === 'topics' ? 'Topic' : 'Note'} and Associated Content
              </Button>
            </div>
          </div>
        </div>
      )}
      </div>
    </AdminLayoutSimple>
  )
}
