'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import { useAuthStore } from '@/store/auth'
import { StudentRoute } from '@/components/route-guard'
import { BookOpen, Clock, Plus, CheckCircle, Circle, Trash2, Edit, BookmarkPlus, ListTodo, Loader2 } from 'lucide-react'
import { studentAPI } from '@/lib/apiClient'
import { toast } from 'sonner'

interface StudyListItem {
  id: number
  topicId: number
  topicName: string
  chapterName: string
  subjectName: string
  status: 'ADDED' | 'IN_PROGRESS' | 'DONE'
  notes?: string
  addedAt: string
  lastUpdatedAt: string
}

// Two-component pattern: prevents API calls before auth is verified
export default function StudentStudyListPage() {
  return (
    <StudentRoute>
      <StudyListContent />
    </StudentRoute>
  )
}

function StudyListContent() {
  const [items, setItems] = useState<StudyListItem[]>([])
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState<'ALL' | 'ADDED' | 'IN_PROGRESS' | 'DONE'>('ALL')
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<StudyListItem | null>(null)
  const [newTopicId, setNewTopicId] = useState('')
  const [newNotes, setNewNotes] = useState('')
  const [submitting, setSubmitting] = useState(false)
  
  const router = useRouter()
  const { user } = useAuthStore()

  // Load study list on mount - auth already verified by StudentRoute
  useEffect(() => {
    loadStudyList()
  }, [])

  const loadStudyList = async () => {
    try {
      const data = await studentAPI.getStudyList()
      setItems(data)
    } catch (error) {
      console.error('Failed to load study list:', error)
      toast.error('Failed to load study list')
    } finally {
      setLoading(false)
    }
  }

  const handleAddItem = async () => {
    if (!newTopicId) return
    
    setSubmitting(true)
    try {
      await studentAPI.addToStudyList(parseInt(newTopicId))
      toast.success('Topic added successfully!')
      setIsAddDialogOpen(false)
      setNewTopicId('')
      setNewNotes('')
      loadStudyList()
    } catch (error: any) {
      console.error('Failed to add item:', error)
      if (error.response?.status === 409) {
        toast.error('Topic already in your study list')
      } else {
        toast.error('Failed to add topic')
      }
    } finally {
      setSubmitting(false)
    }
  }

  const handleUpdateStatus = async (itemId: number, newStatus: 'ADDED' | 'IN_PROGRESS' | 'DONE') => {
    try {
      await studentAPI.updateStudyListStatus(itemId, newStatus)
      toast.success('Status updated successfully!')
      loadStudyList()
    } catch (error) {
      console.error('Failed to update status:', error)
      toast.error('Failed to update status')
    }
  }

  const handleUpdateNotes = async (itemId: number, notes: string) => {
    try {
      await studentAPI.updateStudyListNote(itemId, notes)
      toast.success('Notes updated successfully!')
      setEditingItem(null)
      loadStudyList()
    } catch (error) {
      console.error('Failed to update notes:', error)
      toast.error('Failed to update notes')
    }
  }

  const handleMarkAsDone = async (itemId: number) => {
    try {
      await studentAPI.updateStudyListStatus(itemId, 'DONE')
      toast.success('Marked as done!')
      loadStudyList()
    } catch (error) {
      console.error('Failed to mark as done:', error)
      toast.error('Failed to mark as done')
    }
  }

  const handleRemoveItem = async (itemId: number) => {
    if (!confirm('Are you sure you want to remove this item from your study list?')) return
    
    try {
      await studentAPI.removeFromStudyList(itemId)
      toast.success('Topic removed from study list')
      loadStudyList()
    } catch (error) {
      console.error('Failed to remove item:', error)
      toast.error('Failed to remove item')
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ADDED':
        return 'bg-blue-100 text-blue-800 border-blue-200'
      case 'IN_PROGRESS':
        return 'bg-amber-100 text-amber-800 border-amber-200'
      case 'DONE':
        return 'bg-emerald-100 text-emerald-800 border-emerald-200'
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200'
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'ADDED':
        return <BookmarkPlus className="h-4 w-4" />
      case 'IN_PROGRESS':
        return <Circle className="h-4 w-4" />
      case 'DONE':
        return <CheckCircle className="h-4 w-4" />
      default:
        return <ListTodo className="h-4 w-4" />
    }
  }

  const filteredItems = activeTab === 'ALL' 
    ? items 
    : items.filter(item => item.status === activeTab)

  const countByStatus = (status: string) => 
    items.filter(item => item.status === status).length

  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    const now = new Date()
    const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60))
    
    if (diffInHours < 1) return 'Just now'
    if (diffInHours < 24) return `${diffInHours}h ago`
    if (diffInHours < 48) return 'Yesterday'
    return date.toLocaleDateString()
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Loader2 className="h-8 w-8 animate-spin text-emerald-600" />
      </div>
    )
  }

  return (
    <div className="p-6 space-y-6">
        {/* Header */}
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">My Study List</h1>
            <p className="text-sm text-gray-500 mt-1">ORGANIZE YOUR LEARNING JOURNEY</p>
          </div>
          <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
            <DialogTrigger asChild>
              <Button className="bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600 text-white shadow-md">
                <Plus className="h-4 w-4 mr-2" />
                Add Topic
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Add Topic to Study List</DialogTitle>
                <DialogDescription>
                  Add a topic to track your learning progress
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4 mt-4">
                <div>
                  <Label htmlFor="topicId">Topic ID</Label>
                  <Input
                    id="topicId"
                    type="number"
                    placeholder="Enter topic ID"
                    value={newTopicId}
                    onChange={(e) => setNewTopicId(e.target.value)}
                  />
                  <p className="text-xs text-gray-500 mt-1">Find topic ID from Content Discovery</p>
                </div>
                <div>
                  <Label htmlFor="notes">Notes (Optional)</Label>
                  <Textarea
                    id="notes"
                    placeholder="Add personal notes about this topic..."
                    value={newNotes}
                    onChange={(e) => setNewNotes(e.target.value)}
                    rows={3}
                  />
                </div>
                <div className="flex justify-end space-x-2">
                  <Button variant="outline" onClick={() => setIsAddDialogOpen(false)}>
                    Cancel
                  </Button>
                  <Button 
                    onClick={handleAddItem}
                    disabled={!newTopicId || submitting}
                    className="bg-gradient-to-r from-emerald-500 to-teal-500"
                  >
                    {submitting ? (
                      <>
                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        Adding...
                      </>
                    ) : (
                      'Add to List'
                    )}
                  </Button>
                </div>
              </div>
            </DialogContent>
          </Dialog>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <Card className="rounded-2xl border-gray-100 shadow-sm">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-500 uppercase tracking-wide mb-1">Total Items</p>
                  <p className="text-3xl font-bold text-gray-900">{items.length}</p>
                </div>
                <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-blue-500 to-indigo-500 flex items-center justify-center shadow-md">
                  <ListTodo className="h-7 w-7 text-white" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="rounded-2xl border-gray-100 shadow-sm">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-500 uppercase tracking-wide mb-1">Added</p>
                  <p className="text-3xl font-bold text-gray-900">{countByStatus('ADDED')}</p>
                </div>
                <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-blue-400 to-blue-500 flex items-center justify-center shadow-md">
                  <BookmarkPlus className="h-7 w-7 text-white" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="rounded-2xl border-gray-100 shadow-sm">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-500 uppercase tracking-wide mb-1">In Progress</p>
                  <p className="text-3xl font-bold text-gray-900">{countByStatus('IN_PROGRESS')}</p>
                </div>
                <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center shadow-md">
                  <Clock className="h-7 w-7 text-white" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="rounded-2xl border-gray-100 shadow-sm">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-500 uppercase tracking-wide mb-1">Completed</p>
                  <p className="text-3xl font-bold text-gray-900">{countByStatus('DONE')}</p>
                </div>
                <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-emerald-500 to-teal-500 flex items-center justify-center shadow-md">
                  <CheckCircle className="h-7 w-7 text-white" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Status Tabs */}
        <Card className="rounded-2xl border-gray-100 shadow-sm">
          <CardContent className="p-0">
            <div className="flex border-b border-gray-200">
              {(['ALL', 'ADDED', 'IN_PROGRESS', 'DONE'] as const).map((tab) => (
                <button
                  key={tab}
                  onClick={() => setActiveTab(tab)}
                  className={`flex-1 px-4 py-3 text-sm font-medium transition-colors ${
                    activeTab === tab
                      ? 'text-emerald-600 border-b-2 border-emerald-600 bg-emerald-50/50'
                      : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                  }`}
                >
                  {tab === 'ALL' ? 'All Items' : tab.replace('_', ' ')}
                  <span className="ml-2 text-xs">
                    ({tab === 'ALL' ? items.length : countByStatus(tab)})
                  </span>
                </button>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Study List Items */}
        <div className="space-y-4">
          {filteredItems.length > 0 ? (
            filteredItems.map((item) => (
              <Card key={item.id} className="rounded-2xl border-gray-100 shadow-sm hover:shadow-md transition-shadow">
                <CardContent className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-3 mb-2">
                        <h3 className="text-lg font-semibold text-gray-900">{item.topicName}</h3>
                        <Badge className={`${getStatusColor(item.status)} flex items-center space-x-1`}>
                          {getStatusIcon(item.status)}
                          <span>{item.status.replace('_', ' ')}</span>
                        </Badge>
                      </div>
                      
                      <div className="flex items-center space-x-2 text-sm text-gray-500 mb-3">
                        <BookOpen className="h-4 w-4" />
                        <span>{item.subjectName}</span>
                        <span>•</span>
                        <span>{item.chapterName}</span>
                      </div>

                      {editingItem?.id === item.id ? (
                        <div className="mt-3 space-y-2">
                          <Textarea
                            value={editingItem.notes || ''}
                            onChange={(e) => setEditingItem({ ...editingItem, notes: e.target.value })}
                            placeholder="Add notes..."
                            rows={2}
                          />
                          <div className="flex space-x-2">
                            <Button
                              size="sm"
                              onClick={() => handleUpdateNotes(item.id, editingItem.notes || '')}
                              className="bg-gradient-to-r from-emerald-500 to-teal-500"
                            >
                              Save
                            </Button>
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => setEditingItem(null)}
                            >
                              Cancel
                            </Button>
                          </div>
                        </div>
                      ) : (
                        <>
                          {item.notes && (
                            <p className="text-sm text-gray-600 bg-gray-50 rounded-lg p-3 mb-3">
                              {item.notes}
                            </p>
                          )}
                          <p className="text-xs text-gray-400">
                            Added {formatDate(item.addedAt)} • Updated {formatDate(item.lastUpdatedAt)}
                          </p>
                        </>
                      )}
                    </div>

                    <div className="flex flex-col space-y-2 ml-4">
                      {item.status !== 'DONE' && (
                        <>
                          {item.status === 'ADDED' && (
                            <Button
                              size="sm"
                              onClick={() => handleUpdateStatus(item.id, 'IN_PROGRESS')}
                              className="bg-gradient-to-r from-amber-500 to-orange-500 text-white"
                            >
                              Start Learning
                            </Button>
                          )}
                          {item.status === 'IN_PROGRESS' && (
                            <Button
                              size="sm"
                              onClick={() => handleMarkAsDone(item.id)}
                              className="bg-gradient-to-r from-emerald-500 to-teal-500 text-white"
                            >
                              <CheckCircle className="h-4 w-4 mr-1" />
                              Mark Done
                            </Button>
                          )}
                        </>
                      )}
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => setEditingItem(item)}
                      >
                        <Edit className="h-4 w-4 mr-1" />
                        Edit Notes
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => router.push(`/student/booking?topicId=${item.topicId}`)}
                      >
                        Book Class
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        className="text-red-600 hover:bg-red-50 hover:border-red-200"
                        onClick={() => handleRemoveItem(item.id)}
                      >
                        <Trash2 className="h-4 w-4 mr-1" />
                        Remove
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))
          ) : (
            <Card className="rounded-2xl border-gray-100 shadow-sm">
              <CardContent className="p-12 text-center">
                <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-gray-100 flex items-center justify-center">
                  <ListTodo className="h-8 w-8 text-gray-400" />
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">No items in this category</h3>
                <p className="text-gray-500 mb-4">
                  {activeTab === 'ALL' 
                    ? 'Start adding topics to your study list' 
                    : `No items with status "${activeTab.replace('_', ' ')}"`}
                </p>
                <Button 
                  onClick={() => setIsAddDialogOpen(true)}
                  className="bg-gradient-to-r from-emerald-500 to-teal-500"
                >
                  <Plus className="h-4 w-4 mr-2" />
                  Add Your First Topic
                </Button>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
  )
}
