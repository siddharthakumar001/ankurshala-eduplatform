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
  FileText
} from 'lucide-react'

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
  const [activeTab, setActiveTab] = useState('boards')
  const [searchTerm, setSearchTerm] = useState('')
  const [boards, setBoards] = useState<Board[]>([])
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [chapters, setChapters] = useState<Chapter[]>([])
  const [topics, setTopics] = useState<Topic[]>([])
  const [topicNotes, setTopicNotes] = useState<TopicNote[]>([])
  const [loading, setLoading] = useState(true)
  
  // Dialog states
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<any>(null)
  const [formData, setFormData] = useState<any>({})

  useEffect(() => {
    // Wait for authentication token to be available
    const checkAuthAndLoad = () => {
      const token = localStorage.getItem('accessToken')
      if (token) {
        // Load all data needed for forms
        fetchBoards()
        fetchSubjects()
        fetchChapters()
        fetchTopics()
        fetchTopicNotes()
        loadData() // Load data for current tab
      } else {
        // Retry after a short delay
        setTimeout(checkAuthAndLoad, 100)
      }
    }
    checkAuthAndLoad()
  }, [activeTab])

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
          await fetchSubjects()
          break
        case 'chapters':
          await fetchChapters()
          break
        case 'topics':
          await fetchTopics()
          break
        case 'notes':
          await fetchTopicNotes()
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
    const token = localStorage.getItem('accessToken')
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/boards?size=100&active=true`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (response.ok) {
      const data = await response.json()
      setBoards(data.content || data)
    }
  }

  const fetchSubjects = async () => {
    const token = localStorage.getItem('accessToken')
    console.log('Fetching subjects with token:', token ? 'present' : 'missing')
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/subjects?size=100&active=true`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    console.log('Subjects API response status:', response.status)
    if (response.ok) {
      const data = await response.json()
      console.log('Subjects data received:', data)
      setSubjects(data.content || data)
    } else {
      console.error('Subjects API error:', response.status, response.statusText)
    }
  }

  const fetchChapters = async () => {
    const token = localStorage.getItem('accessToken')
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/chapters?size=100&active=true`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (response.ok) {
      const data = await response.json()
      setChapters(data.content || data)
    }
  }

  const fetchTopics = async () => {
    const token = localStorage.getItem('accessToken')
    console.log('Fetching topics with token:', token ? 'present' : 'missing')
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/topics?size=100&active=true`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    console.log('Topics API response status:', response.status)
    if (response.ok) {
      const data = await response.json()
      console.log('Topics data received:', data)
      setTopics(data.content || data)
    } else {
      console.error('Topics API error:', response.status, response.statusText)
    }
  }

  const fetchTopicNotes = async () => {
    const token = localStorage.getItem('accessToken')
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/notes?size=100&active=true`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (response.ok) {
      const data = await response.json()
      setTopicNotes(data.content || data)
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
      const apiData = { ...formData }
      
      // For notes, ensure attachments is explicitly null if not provided
      if (activeTab === 'notes' && !apiData.attachments) {
        apiData.attachments = null
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

  const filteredData = getCurrentData().filter(item => 
    (item as any).name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (item as any).title?.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const renderTable = () => {
    const data = filteredData
    
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
                        <div>
                          <div className="font-medium">{(item as any).name || (item as any).title}</div>
                          {(item as any).description && (
                            <div className="text-sm text-gray-500">{(item as any).description}</div>
                          )}
                        </div>
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
                        <Badge variant={item.active ? 'default' : 'secondary'} data-testid="status-badge">
                          {item.active ? 'Active' : 'Inactive'}
                        </Badge>
                      </td>
                      <td className="p-4 text-sm text-gray-500">
                        {new Date(item.createdAt).toLocaleDateString()}
                      </td>
                      <td className="p-4">
                        <div className="flex items-center space-x-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setEditingItem(item)
                              setFormData(item)
                              setIsEditDialogOpen(true)
                            }}
                          >
                            <Edit className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleToggleActive(item.id, !item.active)}
                          >
                            {item.active ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleDelete(item.id)}
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
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
        if (activeTab === 'subjects' && !formData.name?.trim()) {
          console.log('Validation failed: subject name is required', { formData })
          toast.error('Subject name is required')
          return
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
              <Label htmlFor="subjectId">Subject</Label>
              <Select
                value={formData.subjectId?.toString() || ''}
                onValueChange={(value) => setFormData({...formData, subjectId: parseInt(value)})}
              >
                <SelectTrigger data-testid="subject-select">
                  <SelectValue placeholder="Select subject" />
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
              <Label htmlFor="chapterId">Chapter</Label>
              <Select
                value={formData.chapterId?.toString() || ''}
                onValueChange={(value) => setFormData({...formData, chapterId: parseInt(value)})}
              >
                <SelectTrigger data-testid="chapter-select">
                  <SelectValue placeholder="Select chapter" />
                </SelectTrigger>
                <SelectContent>
                  {chapters.map(chapter => (
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
              <Label htmlFor="code">Topic Code (Optional)</Label>
              <Input
                id="code"
                value={formData.code || ''}
                onChange={(e) => setFormData({...formData, code: e.target.value})}
                placeholder="Enter topic code"
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
              <Label htmlFor="topicId">Topic</Label>
              <Select
                value={formData.topicId?.toString() || ''}
                onValueChange={(value) => setFormData({...formData, topicId: parseInt(value)})}
              >
                <SelectTrigger data-testid="topic-select">
                  <SelectValue placeholder="Select topic" />
                </SelectTrigger>
                <SelectContent>
                  {topics.map(topic => (
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
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Content Management</h1>
            <p className="text-gray-600 dark:text-gray-400">Manage educational content hierarchy</p>
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
      </div>
    </AdminLayoutSimple>
  )
}
