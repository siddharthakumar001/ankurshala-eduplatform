'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
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
      await studentAPI.addToStudyList(parseInt(newTopicId), newNotes || undefined)
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

  const handleUpdateNotes = async (itemId: number, notes: string, status: 'ADDED' | 'IN_PROGRESS' | 'DONE') => {
    try {
      await studentAPI.updateStudyListNote(itemId, notes, status)
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
    <div className="space-y-6">
      {/* Header */}
      <div className="page-header">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-white">My Study List</h1>
            <p className="text-sm text-white/80 mt-1">Organize your learning journey</p>
          </div>
          <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
            <DialogTrigger asChild>
              <Button className="btn-primary">
                <Plus className="h-4 w-4 mr-2" />
                Add Topic
              </Button>
            </DialogTrigger>
            <DialogContent className="modal-content">
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
                    className="input-modern"
                  />
                  <p className="text-xs text-slate-500 dark:text-slate-300 mt-1">Find topic ID from Content Discovery</p>
                </div>
                <div>
                  <Label htmlFor="notes">Notes (Optional)</Label>
                  <Textarea
                    id="notes"
                    placeholder="Add personal notes about this topic..."
                    value={newNotes}
                    onChange={(e) => setNewNotes(e.target.value)}
                    rows={3}
                    className="input-modern"
                  />
                </div>
                <div className="flex justify-end space-x-2">
                  <Button variant="outline" className="btn-outline" onClick={() => setIsAddDialogOpen(false)}>
                    Cancel
                  </Button>
                  <Button
                    onClick={handleAddItem}
                    disabled={!newTopicId || submitting}
                    className="btn-primary"
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
      </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <Card className="glass-panel">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-slate-500 dark:text-slate-300 uppercase tracking-wide mb-1">Total Items</p>
                  <p className="text-3xl font-bold text-slate-900 dark:text-white">{items.length}</p>
                </div>
                <div className="w-14 h-14 rounded-xl bg-emerald-500/10 flex items-center justify-center">
                  <ListTodo className="h-7 w-7 text-emerald-500" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="glass-panel">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-slate-500 dark:text-slate-300 uppercase tracking-wide mb-1">Added</p>
                  <p className="text-3xl font-bold text-slate-900 dark:text-white">{countByStatus('ADDED')}</p>
                </div>
                <div className="w-14 h-14 rounded-xl bg-sky-500/10 flex items-center justify-center">
                  <BookmarkPlus className="h-7 w-7 text-sky-500" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="glass-panel">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-slate-500 dark:text-slate-300 uppercase tracking-wide mb-1">In Progress</p>
                  <p className="text-3xl font-bold text-slate-900 dark:text-white">{countByStatus('IN_PROGRESS')}</p>
                </div>
                <div className="w-14 h-14 rounded-xl bg-amber-500/10 flex items-center justify-center">
                  <Clock className="h-7 w-7 text-amber-500" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="glass-panel">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-slate-500 dark:text-slate-300 uppercase tracking-wide mb-1">Completed</p>
                  <p className="text-3xl font-bold text-slate-900 dark:text-white">{countByStatus('DONE')}</p>
                </div>
                <div className="w-14 h-14 rounded-xl bg-emerald-500/10 flex items-center justify-center">
                  <CheckCircle className="h-7 w-7 text-emerald-500" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Status Tabs */}
        <Card className="glass-panel border border-white/40">
          <CardContent className="p-0">
            <div className="flex border-b border-white/20">
              {(['ALL', 'ADDED', 'IN_PROGRESS', 'DONE'] as const).map((tab) => (
                <button
                  key={tab}
                  onClick={() => setActiveTab(tab)}
                  className={`flex-1 px-4 py-3 text-sm font-medium transition-colors ${
                    activeTab === tab
                      ? 'text-[#0F9D58] border-b-2 border-[#0F9D58] bg-white/60'
                      : 'text-slate-600 dark:text-slate-200 hover:text-slate-900 dark:hover:text-white hover:bg-white/30'
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
              <Card key={item.id} className="glass-panel border border-white/40 hover-lift transition-shadow">
                <CardContent className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-3 mb-2">
                        <h3 className="text-lg font-semibold text-slate-900 dark:text-white">{item.topicName}</h3>
                        <Badge className={`${getStatusColor(item.status)} flex items-center space-x-1`}>
                          {getStatusIcon(item.status)}
                          <span>{item.status.replace('_', ' ')}</span>
                        </Badge>
                      </div>
                      
                      <div className="flex items-center space-x-2 text-sm text-slate-500 dark:text-slate-300 mb-3">
                        <BookOpen className="h-4 w-4" />
                        <span>{item.subjectName}</span>
                        <span>-</span>
                        <span>{item.chapterName}</span>
                      </div>

                      {editingItem?.id === item.id ? (
                        <div className="mt-3 space-y-2">
                          <Textarea
                            value={editingItem.notes || ''}
                            onChange={(e) => setEditingItem({ ...editingItem, notes: e.target.value })}
                            placeholder="Add notes..."
                            rows={2}
                            className="input-modern"
                          />
                          <div className="flex space-x-2">
                            <Button
                              size="sm"
                              onClick={() => handleUpdateNotes(item.id, editingItem.notes || '', item.status)}
                              className="btn-primary h-9 px-4 text-sm"
                            >
                              Save
                            </Button>
                            <Button
                              size="sm"
                              variant="outline"
                              className="btn-outline h-9 px-4 text-sm"
                              onClick={() => setEditingItem(null)}
                            >
                              Cancel
                            </Button>
                          </div>
                        </div>
                      ) : (
                        <>
                          {item.notes && (
                            <p className="text-sm text-slate-600 dark:text-slate-200 glass rounded-lg p-3 mb-3">
                              {item.notes}
                            </p>
                          )}
                          <p className="text-xs text-slate-400 dark:text-slate-300">
                            Added {formatDate(item.addedAt)} - Updated {formatDate(item.lastUpdatedAt)}
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
                              className="btn-primary h-9 px-4 text-sm"
                            >
                              Start Learning
                            </Button>
                          )}
                          {item.status === 'IN_PROGRESS' && (
                            <Button
                              size="sm"
                              onClick={() => handleMarkAsDone(item.id)}
                              className="btn-primary h-9 px-4 text-sm"
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
                        className="btn-outline h-9 px-4 text-sm"
                        onClick={() => setEditingItem(item)}
                      >
                        <Edit className="h-4 w-4 mr-1" />
                        Edit Notes
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        className="btn-outline h-9 px-4 text-sm"
                        onClick={() => router.push(`/student/booking?topicId=${item.topicId}`)}
                      >
                        Book Class
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        className="btn-outline h-9 px-4 text-sm text-red-600 hover:text-red-700"
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
            <Card className="glass-panel border border-white/40">
              <CardContent className="p-12 text-center">
                <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-slate-500/10 flex items-center justify-center">
                  <ListTodo className="h-8 w-8 text-slate-400" />
                </div>
                <h3 className="text-lg font-medium text-slate-900 dark:text-white mb-2">No items in this category</h3>
                <p className="text-slate-500 dark:text-slate-300 mb-4">
                  {activeTab === 'ALL' 
                    ? 'Start adding topics to your study list' 
                    : `No items with status \"${activeTab.replace('_', ' ')}\"`}
                </p>
                <Button 
                  onClick={() => setIsAddDialogOpen(true)}
                  className="btn-primary"
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
