'use client'

import { useState, useEffect, useCallback } from 'react'
import DashboardLayout from '@/components/layout/DashboardLayout'
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
  AlertCircle,
  Loader2
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
import { api } from '@/utils/api'

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
  boardId: number
}

interface Subject {
  id: number
  name: string
  gradeId: number
  boardId: number
}

interface Chapter {
  id: number
  name: string
  subjectId: number
  gradeId: number
  boardId: number
}

interface Topic {
  id: number
  title: string
  code?: string
  chapterId: number
  subjectId: number
  gradeId: number
  boardId: number
}

export default function AdminPricingPage() {
  const [pricingRules, setPricingRules] = useState<PricingRule[]>([])
  const [boards, setBoards] = useState<Board[]>([])
  const [grades, setGrades] = useState<Grade[]>([])
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [chapters, setChapters] = useState<Chapter[]>([])
  const [topics, setTopics] = useState<Topic[]>([])
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const [showCreateDialog, setShowCreateDialog] = useState(false)
  const [showEditDialog, setShowEditDialog] = useState(false)
  const [showTestDialog, setShowTestDialog] = useState(false)
  const [editingRule, setEditingRule] = useState<PricingRule | null>(null)
  const [testResult, setTestResult] = useState<PricingRule | null>(null)
  const [error, setError] = useState('')
  
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

  const fetchPricingRules = useCallback(async () => {
    try {
      const response = await api.get('/admin/pricing')
      const data = response.data as any
      setPricingRules(data.content || data || [])
    } catch (error) {
      console.error('Error fetching pricing rules:', error)
      throw error
    }
  }, [])

  const fetchBoards = useCallback(async () => {
    try {
      const response = await api.get('/admin/content/boards')
      const data = response.data as any
      setBoards(data.content || data || [])
    } catch (error) {
      console.error('Error fetching boards:', error)
      throw error
    }
  }, [])

  const fetchGrades = useCallback(async () => {
    try {
      const response = await api.get('/admin/content/grades/dropdown')
      const data = response.data as any
      setGrades(data || [])
    } catch (error) {
      console.error('Error fetching grades:', error)
      throw error
    }
  }, [])

  const fetchSubjects = useCallback(async () => {
    try {
      const response = await api.get('/admin/content/subjects/dropdown')
      const data = response.data as any
      setSubjects(data || [])
    } catch (error) {
      console.error('Error fetching subjects:', error)
      throw error
    }
  }, [])

  const fetchChapters = useCallback(async () => {
    try {
      const response = await api.get('/admin/content/chapters/dropdown')
      const data = response.data as any
      setChapters(data || [])
    } catch (error) {
      console.error('Error fetching chapters:', error)
      throw error
    }
  }, [])

  const fetchTopics = useCallback(async () => {
    try {
      const response = await api.get('/admin/content/topics/dropdown')
      const data = response.data as any
      setTopics(data || [])
    } catch (error) {
      console.error('Error fetching topics:', error)
      throw error
    }
  }, [])

  const fetchAllData = useCallback(async () => {
    try {
      setLoading(true)
      setError('')
      
      // Fetch data in parallel, but handle individual failures gracefully
      const results = await Promise.allSettled([
        fetchPricingRules(),
        fetchBoards(),
        fetchGrades(),
        fetchSubjects(),
        fetchChapters(),
        fetchTopics()
      ])
      
      // Check if any critical operations failed
      const failedOperations = results.filter(result => result.status === 'rejected')
      if (failedOperations.length > 0) {
        console.warn('Some data fetching operations failed:', failedOperations)
        // Still show the page with partial data
      }
      
    } catch (error) {
      console.error('Error fetching data:', error)
      setError('Failed to load data. Please try again.')
      toast.error('Failed to load pricing data')
    } finally {
      setLoading(false)
    }
  }, [fetchPricingRules, fetchBoards, fetchGrades, fetchSubjects, fetchChapters, fetchTopics])

  useEffect(() => {
    fetchAllData()
  }, [fetchAllData])

  const handleCreateRule = async () => {
    try {
      setSubmitting(true)
      setError('')
      
      const payload = {
        boardId: formData.boardId && formData.boardId !== 'all' ? parseInt(formData.boardId) : null,
        gradeId: formData.gradeId && formData.gradeId !== 'all' ? parseInt(formData.gradeId) : null,
        subjectId: formData.subjectId && formData.subjectId !== 'all' ? parseInt(formData.subjectId) : null,
        chapterId: formData.chapterId && formData.chapterId !== 'all' ? parseInt(formData.chapterId) : null,
        topicId: formData.topicId && formData.topicId !== 'all' ? parseInt(formData.topicId) : null,
        hourlyRate: parseFloat(formData.hourlyRate),
        active: formData.active
      }

      await api.post('/admin/pricing', payload)
      
      setShowCreateDialog(false)
      setFormData({
        boardId: 'all',
        gradeId: 'all',
        subjectId: 'all',
        chapterId: 'all',
        topicId: 'all',
        hourlyRate: '',
        active: true
      })
      
      toast.success('Pricing rule created successfully')
      await fetchPricingRules()
    } catch (error: any) {
      console.error('Error creating pricing rule:', error)
      const errorMessage = error.response?.data?.message || 'Failed to create pricing rule'
      setError(errorMessage)
      toast.error(errorMessage)
    } finally {
      setSubmitting(false)
    }
  }

  const handleEditRule = async () => {
    if (!editingRule) return

    try {
      setSubmitting(true)
      setError('')
      
      const payload = {
        boardId: formData.boardId && formData.boardId !== 'all' ? parseInt(formData.boardId) : null,
        gradeId: formData.gradeId && formData.gradeId !== 'all' ? parseInt(formData.gradeId) : null,
        subjectId: formData.subjectId && formData.subjectId !== 'all' ? parseInt(formData.subjectId) : null,
        chapterId: formData.chapterId && formData.chapterId !== 'all' ? parseInt(formData.chapterId) : null,
        topicId: formData.topicId && formData.topicId !== 'all' ? parseInt(formData.topicId) : null,
        hourlyRate: parseFloat(formData.hourlyRate),
        active: formData.active
      }

      await api.put(`/admin/pricing/${editingRule.id}`, payload)
      
      setShowEditDialog(false)
      setEditingRule(null)
      setFormData({
        boardId: 'all',
        gradeId: 'all',
        subjectId: 'all',
        chapterId: 'all',
        topicId: 'all',
        hourlyRate: '',
        active: true
      })
      
      toast.success('Pricing rule updated successfully')
      await fetchPricingRules()
    } catch (error: any) {
      console.error('Error updating pricing rule:', error)
      const errorMessage = error.response?.data?.message || 'Failed to update pricing rule'
      setError(errorMessage)
      toast.error(errorMessage)
    } finally {
      setSubmitting(false)
    }
  }

  const handleDeleteRule = async (id: number) => {
    if (!confirm('Are you sure you want to delete this pricing rule?')) return

    try {
      await api.delete(`/admin/pricing/${id}`)
      toast.success('Pricing rule deleted successfully')
      await fetchPricingRules()
    } catch (error: any) {
      console.error('Error deleting pricing rule:', error)
      const errorMessage = error.response?.data?.message || 'Failed to delete pricing rule'
      toast.error(errorMessage)
    }
  }

  const handleToggleStatus = async (id: number) => {
    try {
      await api.patch(`/admin/pricing/${id}/active`)
      toast.success('Pricing rule status updated')
      await fetchPricingRules()
    } catch (error: any) {
      console.error('Error toggling pricing rule status:', error)
      const errorMessage = error.response?.data?.message || 'Failed to update pricing rule status'
      toast.error(errorMessage)
    }
  }

  const handleTestPricing = async () => {
    try {
      setSubmitting(true)
      setError('')
      
      const params = new URLSearchParams()
      if (testFormData.boardId && testFormData.boardId !== 'all') params.append('boardId', testFormData.boardId)
      if (testFormData.gradeId && testFormData.gradeId !== 'all') params.append('gradeId', testFormData.gradeId)
      if (testFormData.subjectId && testFormData.subjectId !== 'all') params.append('subjectId', testFormData.subjectId)
      if (testFormData.chapterId && testFormData.chapterId !== 'all') params.append('chapterId', testFormData.chapterId)
      if (testFormData.topicId && testFormData.topicId !== 'all') params.append('topicId', testFormData.topicId)

      const response = await api.get(`/admin/pricing/resolve?${params}`)
      const data = response.data as any
      
      setTestResult(data.rule)
      setShowTestDialog(true)
      toast.success('Pricing test completed')
    } catch (error: any) {
      console.error('Error testing pricing:', error)
      const errorMessage = error.response?.data?.message || 'Failed to test pricing resolution'
      setError(errorMessage)
      toast.error(errorMessage)
    } finally {
      setSubmitting(false)
    }
  }

  const clearError = () => setError('')

  const openCreateDialog = () => {
    setFormData({
      boardId: 'all',
      gradeId: 'all',
      subjectId: 'all',
      chapterId: 'all',
      topicId: 'all',
      hourlyRate: '',
      active: true
    })
    clearError()
    setShowCreateDialog(true)
  }
  const openEditDialog = (rule: PricingRule) => {
    setEditingRule(rule)
    setFormData({
      boardId: rule.boardId?.toString() || 'all',
      gradeId: rule.gradeId?.toString() || 'all',
      subjectId: rule.subjectId?.toString() || 'all',
      chapterId: rule.chapterId?.toString() || 'all',
      topicId: rule.topicId?.toString() || 'all',
      hourlyRate: rule.hourlyRate.toString(),
      active: rule.active
    })
    clearError()
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

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR'
    }).format(amount)
  }

  const filteredRules = pricingRules.filter(rule => {
    const scope = getScopeDescription(rule).toLowerCase()
    return scope.includes(searchTerm.toLowerCase()) || 
           rule.hourlyRate.toString().includes(searchTerm)
  })

  if (loading) {
    return (
      <DashboardLayout role="admin">
        <div className="flex items-center justify-center h-64">
          <div className="text-center glass-panel rounded-2xl border border-white/40 px-8 py-6">
            <Loader2 className="h-8 w-8 animate-spin text-ankur-primary mx-auto mb-2" />
            <p className="text-gray-600 dark:text-gray-300">Loading pricing rules...</p>
          </div>
        </div>
      </DashboardLayout>
    )
  }

  if (error) {
    return (
      <DashboardLayout role="admin">
        <div className="flex items-center justify-center h-64">
          <div className="text-center glass-panel rounded-2xl border border-red-200/60 dark:border-red-500/30 px-8 py-6 bg-red-50/70 dark:bg-red-500/10">
            <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
            <p className="text-red-600 dark:text-red-200 mb-4">{error}</p>
            <Button onClick={fetchAllData} variant="outline">
              Try Again
            </Button>
          </div>
        </div>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout role="admin">
      <div className="space-y-6 relative">
        <div className="pointer-events-none absolute inset-0 -z-10">
          <div className="absolute -top-16 right-0 h-72 w-72 rounded-full bg-ankur-primary/10 blur-3xl" />
          <div className="absolute bottom-10 left-8 h-64 w-64 rounded-full bg-ankur-accent/10 blur-3xl" />
        </div>
        {/* Header */}
        <div className="page-header flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-white">Pricing Management</h1>
            <p className="text-white/80">Manage pricing rules and rates</p>
          </div>
          <div className="flex flex-wrap gap-2">
            <Button 
              variant="outline" 
              onClick={() => setShowTestDialog(true)}
              className="flex items-center space-x-2"
            >
              <Calculator className="h-4 w-4" />
              <span>Test Pricing</span>
            </Button>
            <Button 
              onClick={openCreateDialog}
              className="flex items-center space-x-2 btn-primary"
            >
              <Plus className="h-4 w-4" />
              <span>New Rule</span>
            </Button>
          </div>
        </div>

        {/* Search and Filters */}
        <Card className="p-6 glass">
          <div className="flex flex-col md:flex-row md:items-center md:space-x-4 gap-4">
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
            <div className="text-sm text-gray-500 dark:text-gray-300">
              {filteredRules.length} rules found
            </div>
          </div>
        </Card>

        {/* Pricing Rules Table */}
        <Card className="p-6 glass">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-ankur-secondary dark:text-white">Pricing Rules</h3>
              <div className="text-sm text-gray-500 dark:text-gray-300">
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
              <div className="overflow-x-auto">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                        Scope
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                        Hourly Rate
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                        Created
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                        Status
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-white/40 dark:divide-white/10">
                    {filteredRules.map((rule) => (
                      <tr key={rule.id} className="hover:bg-white/60 dark:hover:bg-slate-900/60">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm font-medium text-gray-900 dark:text-white">
                            {getScopeDescription(rule)}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center">
                            <DollarSign className="h-4 w-4 text-green-500 mr-1" />
                            <span className="text-sm font-medium text-gray-900 dark:text-white">
                              {formatCurrency(rule.hourlyRate)}/hour
                            </span>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                          {new Date(rule.createdAt).toLocaleDateString()}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <Badge 
                            variant={rule.active ? "default" : "secondary"}
                            className={rule.active ? "bg-green-100/80 text-green-800 dark:bg-green-900/30 dark:text-green-200" : ""}
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
          <DialogContent className="max-w-2xl glass-panel border border-white/30 dark:border-white/10">
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
                <Label htmlFor="hourlyRate">Hourly Rate (INR)</Label>
                <Input
                  id="hourlyRate"
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={formData.hourlyRate}
                  onChange={(e) => setFormData({...formData, hourlyRate: e.target.value})}
                  placeholder="Enter hourly rate"
                  className={error && !formData.hourlyRate ? 'border-red-500' : ''}
                />
                {error && !formData.hourlyRate && (
                  <p className="text-red-500 text-sm mt-1">Hourly rate is required</p>
                )}
              </div>
              {error && (
                <div className="bg-red-50/70 dark:bg-red-500/10 border border-red-200/60 dark:border-red-500/30 rounded-lg p-3">
                  <p className="text-red-800 dark:text-red-200 text-sm">{error}</p>
                </div>
              )}
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => { setShowCreateDialog(false); clearError(); }} disabled={submitting}>
                Cancel
              </Button>
              <Button onClick={handleCreateRule} disabled={!formData.hourlyRate || submitting}>
                {submitting ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Creating...
                  </>
                ) : (
                  'Create Rule'
                )}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Edit Rule Dialog */}
        <Dialog open={showEditDialog} onOpenChange={setShowEditDialog}>
          <DialogContent className="max-w-2xl glass-panel border border-white/30 dark:border-white/10">
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
                <Label htmlFor="hourlyRate">Hourly Rate (INR)</Label>
                <Input
                  id="hourlyRate"
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={formData.hourlyRate}
                  onChange={(e) => setFormData({...formData, hourlyRate: e.target.value})}
                  placeholder="Enter hourly rate"
                  className={error && !formData.hourlyRate ? 'border-red-500' : ''}
                />
                {error && !formData.hourlyRate && (
                  <p className="text-red-500 text-sm mt-1">Hourly rate is required</p>
                )}
              </div>
              {error && (
                <div className="bg-red-50/70 dark:bg-red-500/10 border border-red-200/60 dark:border-red-500/30 rounded-lg p-3">
                  <p className="text-red-800 dark:text-red-200 text-sm">{error}</p>
                </div>
              )}
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => { setShowEditDialog(false); clearError(); }} disabled={submitting}>
                Cancel
              </Button>
              <Button onClick={handleEditRule} disabled={!formData.hourlyRate || submitting}>
                {submitting ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Updating...
                  </>
                ) : (
                  'Update Rule'
                )}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Test Pricing Dialog */}
        <Dialog open={showTestDialog} onOpenChange={setShowTestDialog}>
          <DialogContent className="max-w-2xl glass-panel border border-white/30 dark:border-white/10">
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
                <div className="mt-6 p-4 bg-green-50/70 dark:bg-green-900/20 border border-green-200/60 dark:border-green-800 rounded-lg">
                  <h4 className="font-semibold text-green-800 dark:text-green-200 mb-2">Pricing Rule Found</h4>
                  <div className="space-y-2 text-sm text-gray-700 dark:text-gray-200">
                    <div><strong>Scope:</strong> {getScopeDescription(testResult)}</div>
                    <div><strong>Rate:</strong> {formatCurrency(testResult.hourlyRate)}/hour</div>
                    <div><strong>Status:</strong> {testResult.active ? 'Active' : 'Inactive'}</div>
                  </div>
                </div>
              )}
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setShowTestDialog(false)} disabled={submitting}>
                Close
              </Button>
              <Button onClick={handleTestPricing} disabled={submitting}>
                {submitting ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Testing...
                  </>
                ) : (
                  <>
                    <Calculator className="h-4 w-4 mr-2" />
                    Test Resolution
                  </>
                )}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
    </DashboardLayout>
  )
}
