'use client'

import { useState, useEffect } from 'react'
import AdminLayoutSimple from '@/components/admin-layout-simple'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { 
  DollarSign, 
  Plus, 
  Edit, 
  Trash2, 
  Calculator, 
  Search,
  Filter,
  MoreHorizontal,
  CheckCircle,
  XCircle,
  AlertCircle
} from 'lucide-react'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { toast } from 'sonner'

interface PricingRule {
  id: number
  boardId?: number
  boardName?: string
  gradeId?: number
  gradeName?: string
  subjectId?: number
  subjectName?: string
  chapterId?: number
  chapterName?: string
  topicId?: number
  topicTitle?: string
  hourlyRate: number
  active: boolean
  createdAt: string
  updatedAt: string
}

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
  name: string
  active: boolean
}

interface Topic {
  id: number
  title: string
  active: boolean
}

export default function AdminPricingPage() {
  const [pricingRules, setPricingRules] = useState<PricingRule[]>([])
  const [boards, setBoards] = useState<Board[]>([])
  const [grades, setGrades] = useState<Grade[]>([])
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [chapters, setChapters] = useState<Chapter[]>([])
  const [topics, setTopics] = useState<Topic[]>([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [showCreateDialog, setShowCreateDialog] = useState(false)
  const [showEditDialog, setShowEditDialog] = useState(false)
  const [showTestDialog, setShowTestDialog] = useState(false)
  const [editingRule, setEditingRule] = useState<PricingRule | null>(null)
  const [testResult, setTestResult] = useState<PricingRule | null>(null)
  
  // Form states
  const [formData, setFormData] = useState({
    boardId: 'all',
    gradeId: 'all',
    subjectId: 'all',
    chapterId: 'all',
    topicId: 'all',
    hourlyRate: '',
    active: true
  })

  // Test form states
  const [testFormData, setTestFormData] = useState({
    boardId: 'all',
    gradeId: 'all',
    subjectId: 'all',
    chapterId: 'all',
    topicId: 'all'
  })

  useEffect(() => {
    const checkAuthAndFetch = () => {
      const token = localStorage.getItem('accessToken')
      if (token) {
        fetchPricingRules()
        fetchBoards()
        fetchGrades()
        fetchSubjects()
        fetchChapters()
        fetchTopics()
      } else {
        setTimeout(checkAuthAndFetch, 100)
      }
    }
    checkAuthAndFetch()
  }, [])

  const fetchPricingRules = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      console.log('Fetching pricing rules with token:', token ? 'present' : 'missing')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/pricing`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      console.log('Pricing rules API response status:', response.status)
      if (response.ok) {
        const data = await response.json()
        console.log('Pricing rules data received:', data)
        setPricingRules(data.content || data)
      } else {
        console.error('Pricing rules API error:', response.status, response.statusText)
      }
    } catch (error) {
      console.error('Error fetching pricing rules:', error)
    } finally {
      setLoading(false)
    }
  }

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
      console.log('Fetching grades with token:', token ? 'present' : 'missing')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/grades-list`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      console.log('Grades API response status:', response.status)
      if (response.ok) {
        const data = await response.json()
        console.log('Grades data received:', data)
        setGrades(data)
      } else {
        console.error('Grades API error:', response.status, response.statusText)
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
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/chapters`, {
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
    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/topics`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (response.ok) {
        const data = await response.json()
        setTopics(data.content || data)
      }
    } catch (error) {
      console.error('Error fetching topics:', error)
    }
  }

  const handleCreateRule = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      const payload = {
        boardId: formData.boardId ? parseInt(formData.boardId) : null,
        gradeId: formData.gradeId ? parseInt(formData.gradeId) : null,
        subjectId: formData.subjectId ? parseInt(formData.subjectId) : null,
        chapterId: formData.chapterId ? parseInt(formData.chapterId) : null,
        topicId: formData.topicId ? parseInt(formData.topicId) : null,
        hourlyRate: parseFloat(formData.hourlyRate),
        active: formData.active
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/pricing`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      })

      if (response.ok) {
        setShowCreateDialog(false)
        setFormData({
          boardId: '',
          gradeId: '',
          subjectId: '',
          chapterId: '',
          topicId: '',
          hourlyRate: '',
          active: true
        })
        fetchPricingRules()
      }
    } catch (error) {
      console.error('Error creating pricing rule:', error)
    }
  }

  const handleEditRule = async () => {
    if (!editingRule) return

    try {
      const token = localStorage.getItem('accessToken')
      const payload = {
        boardId: formData.boardId ? parseInt(formData.boardId) : null,
        gradeId: formData.gradeId ? parseInt(formData.gradeId) : null,
        subjectId: formData.subjectId ? parseInt(formData.subjectId) : null,
        chapterId: formData.chapterId ? parseInt(formData.chapterId) : null,
        topicId: formData.topicId ? parseInt(formData.topicId) : null,
        hourlyRate: parseFloat(formData.hourlyRate),
        active: formData.active
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/pricing/${editingRule.id}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      })

      if (response.ok) {
        setShowEditDialog(false)
        setEditingRule(null)
        setFormData({
          boardId: '',
          gradeId: '',
          subjectId: '',
          chapterId: '',
          topicId: '',
          hourlyRate: '',
          active: true
        })
        fetchPricingRules()
      }
    } catch (error) {
      console.error('Error updating pricing rule:', error)
    }
  }

  const handleDeleteRule = async (id: number) => {
    if (!confirm('Are you sure you want to delete this pricing rule?')) return

    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/pricing/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      })

      if (response.ok) {
        fetchPricingRules()
      }
    } catch (error) {
      console.error('Error deleting pricing rule:', error)
    }
  }

  const handleToggleStatus = async (id: number) => {
    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/pricing/${id}/active`, {
        method: 'PATCH',
        headers: { 'Authorization': `Bearer ${token}` }
      })

      if (response.ok) {
        fetchPricingRules()
      }
    } catch (error) {
      console.error('Error toggling pricing rule status:', error)
    }
  }

  const handleTestPricing = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      const params = new URLSearchParams()
      if (testFormData.boardId) params.append('boardId', testFormData.boardId)
      if (testFormData.gradeId) params.append('gradeId', testFormData.gradeId)
      if (testFormData.subjectId) params.append('subjectId', testFormData.subjectId)
      if (testFormData.chapterId) params.append('chapterId', testFormData.chapterId)
      if (testFormData.topicId) params.append('topicId', testFormData.topicId)

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/pricing/resolve?${params}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })

      if (response.ok) {
        const data = await response.json()
        setTestResult(data.rule)
        setShowTestDialog(true)
      }
    } catch (error) {
      console.error('Error testing pricing:', error)
    }
  }

  const openEditDialog = (rule: PricingRule) => {
    setEditingRule(rule)
    setFormData({
      boardId: rule.boardId?.toString() || '',
      gradeId: rule.gradeId?.toString() || '',
      subjectId: rule.subjectId?.toString() || '',
      chapterId: rule.chapterId?.toString() || '',
      topicId: rule.topicId?.toString() || '',
      hourlyRate: rule.hourlyRate.toString(),
      active: rule.active
    })
    setShowEditDialog(true)
  }

  const getScopeDescription = (rule: PricingRule) => {
    const parts = []
    if (rule.boardName) parts.push(rule.boardName)
    if (rule.gradeName) parts.push(rule.gradeName)
    if (rule.subjectName) parts.push(rule.subjectName)
    if (rule.chapterName) parts.push(rule.chapterName)
    if (rule.topicTitle) parts.push(rule.topicTitle)
    return parts.join(' - ') || 'All'
  }

  const filteredRules = pricingRules.filter(rule => {
    const scope = getScopeDescription(rule).toLowerCase()
    return scope.includes(searchTerm.toLowerCase()) || 
           rule.hourlyRate.toString().includes(searchTerm)
  })

  if (loading) {
    return (
      <AdminLayoutSimple>
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-2"></div>
            <p className="text-gray-600 dark:text-gray-400">Loading pricing rules...</p>
          </div>
        </div>
      </AdminLayoutSimple>
    )
  }

  return (
    <AdminLayoutSimple>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Pricing Management</h1>
            <p className="text-gray-600 dark:text-gray-400">Manage pricing rules and rates</p>
          </div>
          <div className="flex space-x-2">
            <Button 
              variant="outline" 
              onClick={() => setShowTestDialog(true)}
              className="flex items-center space-x-2"
            >
              <Calculator className="h-4 w-4" />
              <span>Test Pricing</span>
            </Button>
            <Button 
              onClick={() => setShowCreateDialog(true)}
              className="flex items-center space-x-2"
            >
              <Plus className="h-4 w-4" />
              <span>New Rule</span>
            </Button>
          </div>
        </div>

        {/* Search and Filters */}
        <Card className="p-6">
          <div className="flex items-center space-x-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                <Input
                  placeholder="Search pricing rules..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            <div className="text-sm text-gray-500 dark:text-gray-400">
              {filteredRules.length} rules found
            </div>
          </div>
        </Card>

        {/* Pricing Rules Table */}
        <Card className="p-6">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Pricing Rules</h3>
              <div className="text-sm text-gray-500 dark:text-gray-400">
                {pricingRules.filter(r => r.active).length} active rules
              </div>
            </div>
            
            {filteredRules.length === 0 ? (
              <div className="text-center py-8">
                <AlertCircle className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-500 dark:text-gray-400">No pricing rules found</p>
                <p className="text-sm text-gray-400 dark:text-gray-500 mt-1">
                  Create your first pricing rule to get started
                </p>
              </div>
            ) : (
              <div className="overflow-hidden border border-gray-200 dark:border-gray-700 rounded-lg">
                <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                  <thead className="bg-gray-50 dark:bg-gray-800">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Scope
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Hourly Rate
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Created
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Status
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
                    {filteredRules.map((rule) => (
                      <tr key={rule.id}>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm font-medium text-gray-900 dark:text-white">
                            {getScopeDescription(rule)}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center">
                            <DollarSign className="h-4 w-4 text-green-500 mr-1" />
                            <span className="text-sm font-medium text-gray-900 dark:text-white">
                              ₹{rule.hourlyRate}/hour
                            </span>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                          {new Date(rule.createdAt).toLocaleDateString()}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <Badge 
                            variant={rule.active ? "default" : "secondary"}
                            className={rule.active ? "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200" : ""}
                          >
                            {rule.active ? 'Active' : 'Inactive'}
                          </Badge>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                          <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                              <Button variant="ghost" size="sm">
                                <MoreHorizontal className="h-4 w-4" />
                              </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                              <DropdownMenuItem onClick={() => openEditDialog(rule)}>
                                <Edit className="h-4 w-4 mr-2" />
                                Edit
                              </DropdownMenuItem>
                              <DropdownMenuItem onClick={() => handleToggleStatus(rule.id)}>
                                {rule.active ? (
                                  <>
                                    <XCircle className="h-4 w-4 mr-2" />
                                    Deactivate
                                  </>
                                ) : (
                                  <>
                                    <CheckCircle className="h-4 w-4 mr-2" />
                                    Activate
                                  </>
                                )}
                              </DropdownMenuItem>
                              <DropdownMenuItem 
                                onClick={() => handleDeleteRule(rule.id)}
                                className="text-red-600"
                              >
                                <Trash2 className="h-4 w-4 mr-2" />
                                Delete
                              </DropdownMenuItem>
                            </DropdownMenuContent>
                          </DropdownMenu>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </Card>

        {/* Create Rule Dialog */}
        <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>Create Pricing Rule</DialogTitle>
              <DialogDescription>
                Define a new pricing rule for specific content scope
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="board">Board</Label>
                  <Select value={formData.boardId} onValueChange={(value) => setFormData({...formData, boardId: value})}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select board" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Boards</SelectItem>
                      {boards.map((board) => (
                        <SelectItem key={board.id} value={board.id.toString()}>
                          {board.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label htmlFor="grade">Grade</Label>
                  <Select value={formData.gradeId} onValueChange={(value) => setFormData({...formData, gradeId: value})}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select grade" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Grades</SelectItem>
                      {grades.map((grade) => (
                        <SelectItem key={grade.id} value={grade.id.toString()}>
                          {grade.displayName}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label htmlFor="subject">Subject</Label>
                  <Select value={formData.subjectId} onValueChange={(value) => setFormData({...formData, subjectId: value})}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select subject" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Subjects</SelectItem>
                      {subjects.map((subject) => (
                        <SelectItem key={subject.id} value={subject.id.toString()}>
                          {subject.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label htmlFor="chapter">Chapter</Label>
                  <Select value={formData.chapterId} onValueChange={(value) => setFormData({...formData, chapterId: value})}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select chapter" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Chapters</SelectItem>
                      {chapters.map((chapter) => (
                        <SelectItem key={chapter.id} value={chapter.id.toString()}>
                          {chapter.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <div>
                <Label htmlFor="topic">Topic</Label>
                <Select value={formData.topicId} onValueChange={(value) => setFormData({...formData, topicId: value})}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select topic" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Topics</SelectItem>
                    {topics.map((topic) => (
                      <SelectItem key={topic.id} value={topic.id.toString()}>
                        {topic.title}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="hourlyRate">Hourly Rate (₹)</Label>
                <Input
                  id="hourlyRate"
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={formData.hourlyRate}
                  onChange={(e) => setFormData({...formData, hourlyRate: e.target.value})}
                  placeholder="Enter hourly rate"
                />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setShowCreateDialog(false)}>
                Cancel
              </Button>
              <Button onClick={handleCreateRule} disabled={!formData.hourlyRate}>
                Create Rule
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Edit Rule Dialog */}
        <Dialog open={showEditDialog} onOpenChange={setShowEditDialog}>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>Edit Pricing Rule</DialogTitle>
              <DialogDescription>
                Update the pricing rule details
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="board">Board</Label>
                  <Select value={formData.boardId} onValueChange={(value) => setFormData({...formData, boardId: value})}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select board" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Boards</SelectItem>
                      {boards.map((board) => (
                        <SelectItem key={board.id} value={board.id.toString()}>
                          {board.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label htmlFor="grade">Grade</Label>
                  <Select value={formData.gradeId} onValueChange={(value) => setFormData({...formData, gradeId: value})}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select grade" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Grades</SelectItem>
                      {grades.map((grade) => (
                        <SelectItem key={grade.id} value={grade.id.toString()}>
                          {grade.displayName}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label htmlFor="subject">Subject</Label>
                  <Select value={formData.subjectId} onValueChange={(value) => setFormData({...formData, subjectId: value})}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select subject" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Subjects</SelectItem>
                      {subjects.map((subject) => (
                        <SelectItem key={subject.id} value={subject.id.toString()}>
                          {subject.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label htmlFor="chapter">Chapter</Label>
                  <Select value={formData.chapterId} onValueChange={(value) => setFormData({...formData, chapterId: value})}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select chapter" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Chapters</SelectItem>
                      {chapters.map((chapter) => (
                        <SelectItem key={chapter.id} value={chapter.id.toString()}>
                          {chapter.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <div>
                <Label htmlFor="topic">Topic</Label>
                <Select value={formData.topicId} onValueChange={(value) => setFormData({...formData, topicId: value})}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select topic" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Topics</SelectItem>
                    {topics.map((topic) => (
                      <SelectItem key={topic.id} value={topic.id.toString()}>
                        {topic.title}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="hourlyRate">Hourly Rate (₹)</Label>
                <Input
                  id="hourlyRate"
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={formData.hourlyRate}
                  onChange={(e) => setFormData({...formData, hourlyRate: e.target.value})}
                  placeholder="Enter hourly rate"
                />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setShowEditDialog(false)}>
                Cancel
              </Button>
              <Button onClick={handleEditRule} disabled={!formData.hourlyRate}>
                Update Rule
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Test Pricing Dialog */}
        <Dialog open={showTestDialog} onOpenChange={setShowTestDialog}>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>Test Pricing Resolution</DialogTitle>
              <DialogDescription>
                Test which pricing rule applies for specific content scope
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="testBoard">Board</Label>
                  <Select value={testFormData.boardId} onValueChange={(value) => setTestFormData({...testFormData, boardId: value})}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select board" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Boards</SelectItem>
                      {boards.map((board) => (
                        <SelectItem key={board.id} value={board.id.toString()}>
                          {board.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label htmlFor="testGrade">Grade</Label>
                  <Select value={testFormData.gradeId} onValueChange={(value) => setTestFormData({...testFormData, gradeId: value})}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select grade" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Grades</SelectItem>
                      {grades.map((grade) => (
                        <SelectItem key={grade.id} value={grade.id.toString()}>
                          {grade.displayName}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label htmlFor="testSubject">Subject</Label>
                  <Select value={testFormData.subjectId} onValueChange={(value) => setTestFormData({...testFormData, subjectId: value})}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select subject" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Subjects</SelectItem>
                      {subjects.map((subject) => (
                        <SelectItem key={subject.id} value={subject.id.toString()}>
                          {subject.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label htmlFor="testChapter">Chapter</Label>
                  <Select value={testFormData.chapterId} onValueChange={(value) => setTestFormData({...testFormData, chapterId: value})}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select chapter" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Chapters</SelectItem>
                      {chapters.map((chapter) => (
                        <SelectItem key={chapter.id} value={chapter.id.toString()}>
                          {chapter.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <div>
                <Label htmlFor="testTopic">Topic</Label>
                <Select value={testFormData.topicId} onValueChange={(value) => setTestFormData({...testFormData, topicId: value})}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select topic" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Topics</SelectItem>
                    {topics.map((topic) => (
                      <SelectItem key={topic.id} value={topic.id.toString()}>
                        {topic.title}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              
              {testResult && (
                <div className="mt-6 p-4 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
                  <h4 className="font-semibold text-green-800 dark:text-green-200 mb-2">Pricing Rule Found</h4>
                  <div className="space-y-2 text-sm">
                    <div><strong>Scope:</strong> {getScopeDescription(testResult)}</div>
                    <div><strong>Rate:</strong> ₹{testResult.hourlyRate}/hour</div>
                    <div><strong>Status:</strong> {testResult.active ? 'Active' : 'Inactive'}</div>
                  </div>
                </div>
              )}
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setShowTestDialog(false)}>
                Close
              </Button>
              <Button onClick={handleTestPricing}>
                <Calculator className="h-4 w-4 mr-2" />
                Test Resolution
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
    </AdminLayoutSimple>
  )
}
