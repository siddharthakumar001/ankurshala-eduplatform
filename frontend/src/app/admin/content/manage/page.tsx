'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Badge } from '@/components/ui/badge'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Switch } from '@/components/ui/switch'
import { toast } from 'sonner'
import AdminLayoutSimple from '@/components/admin-layout-simple'
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
  List,
  StickyNote
} from 'lucide-react'

// Import hooks for boards, grades, subjects, chapters, topics, and topic notes
import {
  useBoards,
  useCreateBoard,
  useUpdateBoard,
  useDeleteBoard,
  useGrades,
  useBoardsDropdown,
  useCreateGrade,
  useUpdateGrade,
  useDeleteGrade,
  useSubjects,
  useGradesDropdown,
  useCreateSubject,
  useUpdateSubject,
  useDeleteSubject,
  useChapters,
  useSubjectsDropdown,
  useCreateChapter,
  useUpdateChapter,
  useDeleteChapter,
  useTopics,
  useChaptersDropdown,
  useCreateTopic,
  useUpdateTopic,
  useDeleteTopic,
  useTopicNotes,
  useTopicsDropdown,
  useCreateTopicNote,
  useUpdateTopicNote,
  useDeleteTopicNote
} from '@/hooks/useAdminContent'

import {
  BoardDto,
  GradeDto,
  SubjectDto,
  ChapterDto,
  TopicDto,
  TopicNoteDto,
  PageRequest,
  CreateBoardRequest,
  UpdateBoardRequest,
  CreateGradeRequest,
  UpdateGradeRequest,
  CreateSubjectRequest,
  UpdateSubjectRequest,
  CreateChapterRequest,
  UpdateChapterRequest,
  CreateTopicRequest,
  UpdateTopicRequest,
  CreateTopicNoteRequest,
  UpdateTopicNoteRequest,
  BoardDropdownDto,
  GradeDropdownDto,
  SubjectDropdownDto,
  ChapterDropdownDto,
  TopicDropdownDto
} from '@/services/adminContentService'

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

  return (
    <AdminLayoutSimple>
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto p-6">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900">Content Management</h1>
            <p className="text-gray-600 mt-2">
              Manage educational content including boards, grades, subjects, chapters, topics, and notes
            </p>
          </div>

          <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
            <TabsList className="grid w-full grid-cols-6">
              <TabsTrigger value="boards" className="flex items-center gap-2">
                <BookOpen className="w-4 h-4" />
                Boards
              </TabsTrigger>
              <TabsTrigger value="grades" className="flex items-center gap-2">
                <GraduationCap className="w-4 h-4" />
                Grades
              </TabsTrigger>
              <TabsTrigger value="subjects" className="flex items-center gap-2">
                <Users className="w-4 h-4" />
                Subjects
              </TabsTrigger>
              <TabsTrigger value="chapters" className="flex items-center gap-2">
                <FileText className="w-4 h-4" />
                Chapters
              </TabsTrigger>
              <TabsTrigger value="topics" className="flex items-center gap-2">
                <List className="w-4 h-4" />
                Topics
              </TabsTrigger>
              <TabsTrigger value="topicnotes" className="flex items-center gap-2">
                <StickyNote className="w-4 h-4" />
                Topic Notes
              </TabsTrigger>
            </TabsList>

            <TabsContent value="boards">
              <BoardsTab />
            </TabsContent>

            <TabsContent value="grades">
              <GradesTab />
            </TabsContent>

            <TabsContent value="subjects">
              <SubjectsTab />
            </TabsContent>

            <TabsContent value="chapters">
              <ChaptersTab />
            </TabsContent>

            <TabsContent value="topics">
              <TopicsTab />
            </TabsContent>

            <TabsContent value="topicnotes">
              <TopicNotesTab />
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </AdminLayoutSimple>
  )
}

// Boards Tab Component
function BoardsTab() {
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all')
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [editingBoard, setEditingBoard] = useState<BoardDto | null>(null)
  const [formData, setFormData] = useState<CreateBoardRequest>({ name: '', active: true })

  // API hooks
  const { data: boardsData, isLoading, error, refetch } = useBoards({
    page: currentPage,
    size: pageSize,
    search: searchTerm || undefined,
    active: statusFilter === 'all' ? undefined : statusFilter === 'active'
  })

  const createBoardMutation = useCreateBoard()
  const updateBoardMutation = useUpdateBoard()
  const deleteBoardMutation = useDeleteBoard()

  const boards = boardsData?.content || []
  const totalElements = boardsData?.totalElements || 0

  // Handle filter changes
  const onFilterChange = () => {
    setCurrentPage(0)
    refetch()
  }

  // Handle create button click
  const handleCreateClick = () => {
    setFormData({ name: '', active: true })
    setIsCreateDialogOpen(true)
  }

  // Handle edit button click
  const handleEditClick = (board: BoardDto) => {
    setEditingBoard(board)
    setFormData({ name: board.name, active: board.active })
    setIsEditDialogOpen(true)
  }

  // Handle create submit
  const handleCreateSubmit = async () => {
    try {
      await createBoardMutation.mutateAsync(formData)
      setIsCreateDialogOpen(false)
      setFormData({ name: '', active: true })
      refetch()
    } catch (error) {
      console.error('Create board failed:', error)
    }
  }

  // Handle update submit
  const handleUpdateSubmit = async () => {
    if (!editingBoard) return
    
    try {
      await updateBoardMutation.mutateAsync({
        id: editingBoard.id,
        request: formData
      })
      setIsEditDialogOpen(false)
      setEditingBoard(null)
      refetch()
    } catch (error) {
      console.error('Update board failed:', error)
    }
  }

  // Handle delete
  const handleDelete = async (board: BoardDto) => {
    if (confirm(`Are you sure you want to delete "${board.name}"?`)) {
      try {
        await deleteBoardMutation.mutateAsync({ id: board.id, force: false })
        refetch()
      } catch (error) {
        console.error('Delete board failed:', error)
      }
    }
  }

  // Filter change handlers
  useEffect(() => {
    onFilterChange()
  }, [searchTerm, statusFilter])

  if (isLoading) {
    return <div className="flex justify-center items-center h-64">Loading boards...</div>
  }

  if (error) {
    console.error('BoardsTab: Error details:', error)
    return (
      <div className="text-red-500 text-center space-y-2">
        <div>Error loading boards: {error.message}</div>
        <details className="text-sm text-gray-600">
          <summary className="cursor-pointer">Debug Information</summary>
          <div className="mt-2 text-left">
            <div><strong>Error Type:</strong> {error.name}</div>
            <div><strong>Message:</strong> {error.message}</div>
            <div><strong>Stack:</strong> <pre className="text-xs">{error.stack}</pre></div>
          </div>
        </details>
      </div>
    )
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex justify-between items-center">
          <CardTitle className="flex items-center gap-2">
            <BookOpen className="w-5 h-5" />
            Education Boards ({totalElements})
          </CardTitle>
          <Button onClick={handleCreateClick}>
            <Plus className="w-4 h-4 mr-2" />
            Add Board
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {/* Filters */}
        <div className="flex gap-4 mb-6">
          <div className="relative flex-1">
            <Search className="w-4 h-4 absolute left-3 top-3 text-gray-400" />
            <Input
              placeholder="Search boards..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
          <Select value={statusFilter} onValueChange={(value: any) => setStatusFilter(value)}>
            <SelectTrigger className="w-40">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Status</SelectItem>
              <SelectItem value="active">Active</SelectItem>
              <SelectItem value="inactive">Inactive</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Boards List */}
        <div className="space-y-4">
          {boards.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              No boards found. Create your first board to get started.
            </div>
          ) : (
            boards.map((board) => (
              <div key={board.id} className="border rounded-lg p-4 flex justify-between items-center">
                <div className="flex items-center gap-4">
                  <div>
                    <h3 className="font-semibold">{board.name}</h3>
                    <p className="text-sm text-gray-500">
                      Created: {new Date(board.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                  <Badge variant={board.active ? "default" : "secondary"}>
                    {board.active ? "Active" : "Inactive"}
                  </Badge>
                </div>
                <div className="flex gap-2">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleEditClick(board)}
                  >
                    <Edit className="w-4 h-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleDelete(board)}
                    className="text-red-500 hover:text-red-700"
                  >
                    <Trash2 className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Pagination */}
        {totalElements > pageSize && (
          <div className="flex justify-between items-center mt-6">
            <div className="text-sm text-gray-500">
              Showing {currentPage * pageSize + 1} to {Math.min((currentPage + 1) * pageSize, totalElements)} of {totalElements} results
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                disabled={currentPage === 0}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage(currentPage + 1)}
                disabled={(currentPage + 1) * pageSize >= totalElements}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </CardContent>

      {/* Create Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create New Board</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="name">Board Name</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter board name"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="active"
                checked={formData.active}
                onCheckedChange={(checked) => setFormData({ ...formData, active: checked })}
              />
              <Label htmlFor="active">Active</Label>
            </div>
            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => setIsCreateDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button
                onClick={handleCreateSubmit}
                disabled={!formData.name.trim() || createBoardMutation.isPending}
              >
                {createBoardMutation.isPending ? 'Creating...' : 'Create Board'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Board</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="edit-name">Board Name</Label>
              <Input
                id="edit-name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter board name"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="edit-active"
                checked={formData.active}
                onCheckedChange={(checked) => setFormData({ ...formData, active: checked })}
              />
              <Label htmlFor="edit-active">Active</Label>
            </div>
            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => setIsEditDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button
                onClick={handleUpdateSubmit}
                disabled={!formData.name.trim() || updateBoardMutation.isPending}
              >
                {updateBoardMutation.isPending ? 'Updating...' : 'Update Board'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </Card>
  )
}

// Grades Tab Component
function GradesTab() {
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all')
  const [selectedBoardFilter, setSelectedBoardFilter] = useState<number | null>(null)
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [editingGrade, setEditingGrade] = useState<GradeDto | null>(null)
  const [formData, setFormData] = useState<CreateGradeRequest>({ 
    name: '', 
    displayName: '', 
    boardId: 0, 
    active: true 
  })

  // API hooks
  const { data: gradesData, isLoading, error, refetch } = useGrades({
    page: currentPage,
    size: pageSize,
    search: searchTerm || undefined,
    active: statusFilter === 'all' ? undefined : statusFilter === 'active',
    boardId: selectedBoardFilter || undefined
  })

  const { data: boardsDropdown } = useBoardsDropdown()
  const createGradeMutation = useCreateGrade()
  const updateGradeMutation = useUpdateGrade()
  const deleteGradeMutation = useDeleteGrade()

  const grades = gradesData?.content || []
  const totalElements = gradesData?.totalElements || 0

  // Handle filter changes
  const onFilterChange = () => {
    setCurrentPage(0)
    refetch()
  }

  // Handle create button click
  const handleCreateClick = () => {
    setFormData({ name: '', displayName: '', boardId: 0, active: true })
    setIsCreateDialogOpen(true)
  }

  // Handle edit button click
  const handleEditClick = (grade: GradeDto) => {
    setEditingGrade(grade)
    setFormData({ 
      name: grade.name, 
      displayName: grade.displayName, 
      boardId: grade.boardId, 
      active: grade.active 
    })
    setIsEditDialogOpen(true)
  }

  // Handle create submit
  const handleCreateSubmit = async () => {
    try {
      await createGradeMutation.mutateAsync(formData)
      setIsCreateDialogOpen(false)
      setFormData({ name: '', displayName: '', boardId: 0, active: true })
      refetch()
    } catch (error) {
      console.error('Create grade failed:', error)
    }
  }

  // Handle update submit
  const handleUpdateSubmit = async () => {
    if (!editingGrade) return
    
    try {
      await updateGradeMutation.mutateAsync({
        id: editingGrade.id,
        request: formData
      })
      setIsEditDialogOpen(false)
      setEditingGrade(null)
      refetch()
    } catch (error) {
      console.error('Update grade failed:', error)
    }
  }

  // Handle delete
  const handleDelete = async (grade: GradeDto) => {
    if (confirm(`Are you sure you want to delete "${grade.name}"?`)) {
      try {
        await deleteGradeMutation.mutateAsync({ id: grade.id, force: false })
        refetch()
      } catch (error) {
        console.error('Delete grade failed:', error)
      }
    }
  }

  // Filter change handlers
  useEffect(() => {
    onFilterChange()
  }, [searchTerm, statusFilter, selectedBoardFilter])

  if (isLoading) {
    return <div className="flex justify-center items-center h-64">Loading grades...</div>
  }

  if (error) {
    console.error('GradesTab: Error details:', error)
    return (
      <div className="text-red-500 text-center space-y-2">
        <div>Error loading grades: {error.message}</div>
        <details className="text-sm text-gray-600">
          <summary className="cursor-pointer">Debug Information</summary>
          <div className="mt-2 text-left">
            <div><strong>Error Type:</strong> {error.name}</div>
            <div><strong>Message:</strong> {error.message}</div>
            <div><strong>Stack:</strong> <pre className="text-xs">{error.stack}</pre></div>
          </div>
        </details>
      </div>
    )
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex justify-between items-center">
          <CardTitle className="flex items-center gap-2">
            <GraduationCap className="w-5 h-5" />
            Grade Levels ({totalElements})
          </CardTitle>
          <Button onClick={handleCreateClick}>
            <Plus className="w-4 h-4 mr-2" />
            Add Grade
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {/* Filters */}
        <div className="flex gap-4 mb-6">
          <div className="relative flex-1">
            <Search className="w-4 h-4 absolute left-3 top-3 text-gray-400" />
            <Input
              placeholder="Search grades..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
          <Select value={selectedBoardFilter?.toString() || 'all'} onValueChange={(value) => setSelectedBoardFilter(value === 'all' ? null : parseInt(value))}>
            <SelectTrigger className="w-48">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Boards</SelectItem>
              {boardsDropdown?.map((board) => (
                <SelectItem key={board.id} value={board.id.toString()}>
                  {board.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={statusFilter} onValueChange={(value: any) => setStatusFilter(value)}>
            <SelectTrigger className="w-40">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Status</SelectItem>
              <SelectItem value="active">Active</SelectItem>
              <SelectItem value="inactive">Inactive</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Grades List */}
        <div className="space-y-4">
          {grades.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              No grades found. Create your first grade to get started.
            </div>
          ) : (
            grades.map((grade) => (
              <div key={grade.id} className="border rounded-lg p-4 flex justify-between items-center">
                <div className="flex items-center gap-4">
                  <div>
                    <h3 className="font-semibold">{grade.displayName}</h3>
                    <p className="text-sm text-gray-500">Name: {grade.name}</p>
                    <p className="text-sm text-gray-500">
                      Board: {boardsDropdown?.find(b => b.id === grade.boardId)?.name || 'Unknown'}
                    </p>
                    <p className="text-sm text-gray-500">
                      Created: {new Date(grade.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                  <Badge variant={grade.active ? "default" : "secondary"}>
                    {grade.active ? "Active" : "Inactive"}
                  </Badge>
                </div>
                <div className="flex gap-2">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleEditClick(grade)}
                  >
                    <Edit className="w-4 h-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleDelete(grade)}
                    className="text-red-500 hover:text-red-700"
                  >
                    <Trash2 className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Pagination */}
        {totalElements > pageSize && (
          <div className="flex justify-between items-center mt-6">
            <div className="text-sm text-gray-500">
              Showing {currentPage * pageSize + 1} to {Math.min((currentPage + 1) * pageSize, totalElements)} of {totalElements} results
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                disabled={currentPage === 0}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage(currentPage + 1)}
                disabled={(currentPage + 1) * pageSize >= totalElements}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </CardContent>

      {/* Create Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create New Grade</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="board">Board</Label>
              <Select value={formData.boardId.toString()} onValueChange={(value) => setFormData({ ...formData, boardId: parseInt(value) })}>
                <SelectTrigger>
                  <SelectValue placeholder="Select a board" />
                </SelectTrigger>
                <SelectContent>
                  {boardsDropdown?.map((board) => (
                    <SelectItem key={board.id} value={board.id.toString()}>
                      {board.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="name">Grade Name</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter grade name (e.g., grade-9)"
              />
            </div>
            <div>
              <Label htmlFor="displayName">Display Name</Label>
              <Input
                id="displayName"
                value={formData.displayName}
                onChange={(e) => setFormData({ ...formData, displayName: e.target.value })}
                placeholder="Enter display name (e.g., Grade 9)"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="active"
                checked={formData.active}
                onCheckedChange={(checked) => setFormData({ ...formData, active: checked })}
              />
              <Label htmlFor="active">Active</Label>
            </div>
            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => setIsCreateDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button
                onClick={handleCreateSubmit}
                disabled={!formData.name.trim() || !formData.displayName.trim() || formData.boardId === 0 || createGradeMutation.isPending}
              >
                {createGradeMutation.isPending ? 'Creating...' : 'Create Grade'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Grade</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="edit-board">Board</Label>
              <Select value={formData.boardId.toString()} onValueChange={(value) => setFormData({ ...formData, boardId: parseInt(value) })}>
                <SelectTrigger>
                  <SelectValue placeholder="Select a board" />
                </SelectTrigger>
                <SelectContent>
                  {boardsDropdown?.map((board) => (
                    <SelectItem key={board.id} value={board.id.toString()}>
                      {board.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="edit-name">Grade Name</Label>
              <Input
                id="edit-name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter grade name"
              />
            </div>
            <div>
              <Label htmlFor="edit-displayName">Display Name</Label>
              <Input
                id="edit-displayName"
                value={formData.displayName}
                onChange={(e) => setFormData({ ...formData, displayName: e.target.value })}
                placeholder="Enter display name"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="edit-active"
                checked={formData.active}
                onCheckedChange={(checked) => setFormData({ ...formData, active: checked })}
              />
              <Label htmlFor="edit-active">Active</Label>
            </div>
            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => setIsEditDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button
                onClick={handleUpdateSubmit}
                disabled={!formData.name.trim() || !formData.displayName.trim() || formData.boardId === 0 || updateGradeMutation.isPending}
              >
                {updateGradeMutation.isPending ? 'Updating...' : 'Update Grade'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </Card>
  )
}

// Subjects Tab Component
function SubjectsTab() {
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all')
  const [selectedBoardFilter, setSelectedBoardFilter] = useState<number | null>(null)
  const [selectedGradeFilter, setSelectedGradeFilter] = useState<number | null>(null)
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [editingSubject, setEditingSubject] = useState<SubjectDto | null>(null)
  const [formData, setFormData] = useState<CreateSubjectRequest>({ 
    name: '', 
    boardId: 0,
    gradeId: 0, 
    active: true 
  })

  // API hooks
  const { data: subjectsData, isLoading, error, refetch } = useSubjects({
    page: currentPage,
    size: pageSize,
    search: searchTerm || undefined,
    active: statusFilter === 'all' ? undefined : statusFilter === 'active',
    boardId: selectedBoardFilter || undefined,
    gradeId: selectedGradeFilter || undefined
  })

  const { data: boardsDropdown } = useBoardsDropdown()
  const { data: gradesDropdown } = useGradesDropdown(selectedBoardFilter || undefined)
  const createSubjectMutation = useCreateSubject()
  const updateSubjectMutation = useUpdateSubject()
  const deleteSubjectMutation = useDeleteSubject()

  const subjects = subjectsData?.content || []
  const totalElements = subjectsData?.totalElements || 0

  // Handle filter changes
  const onFilterChange = () => {
    setCurrentPage(0)
    refetch()
  }

  // Handle create button click
  const handleCreateClick = () => {
    setFormData({ name: '', boardId: 0, gradeId: 0, active: true })
    setIsCreateDialogOpen(true)
  }

  // Handle edit button click
  const handleEditClick = (subject: SubjectDto) => {
    setEditingSubject(subject)
    setFormData({ 
      name: subject.name, 
      boardId: subject.boardId,
      gradeId: subject.gradeId, 
      active: subject.active 
    })
    setIsEditDialogOpen(true)
  }

  // Handle create submit
  const handleCreateSubmit = async () => {
    try {
      await createSubjectMutation.mutateAsync(formData)
      setIsCreateDialogOpen(false)
      setFormData({ name: '', gradeId: 0, boardId: 0, active: true })
      refetch()
    } catch (error) {
      console.error('Create subject failed:', error)
    }
  }

  // Handle update submit
  const handleUpdateSubmit = async () => {
    if (!editingSubject) return
    
    try {
      await updateSubjectMutation.mutateAsync({
        id: editingSubject.id,
        request: formData
      })
      setIsEditDialogOpen(false)
      setEditingSubject(null)
      refetch()
    } catch (error) {
      console.error('Update subject failed:', error)
    }
  }

  // Handle delete
  const handleDelete = async (subject: SubjectDto) => {
    if (confirm(`Are you sure you want to delete "${subject.name}"?`)) {
      try {
        await deleteSubjectMutation.mutateAsync({ id: subject.id, force: false })
        refetch()
      } catch (error) {
        console.error('Delete subject failed:', error)
      }
    }
  }

  // Reset grade filter when board filter changes
  useEffect(() => {
    setSelectedGradeFilter(null)
  }, [selectedBoardFilter])

  // Filter change handlers
  useEffect(() => {
    onFilterChange()
  }, [searchTerm, statusFilter, selectedBoardFilter, selectedGradeFilter])

  if (isLoading) {
    return <div className="flex justify-center items-center h-64">Loading subjects...</div>
  }

  if (error) {
    console.error('SubjectsTab: Error details:', error)
    return (
      <div className="text-red-500 text-center space-y-2">
        <div>Error loading subjects: {error.message}</div>
        <details className="text-sm text-gray-600">
          <summary className="cursor-pointer">Debug Information</summary>
          <div className="mt-2 text-left">
            <div><strong>Error Type:</strong> {error.name}</div>
            <div><strong>Message:</strong> {error.message}</div>
            <div><strong>Stack:</strong> <pre className="text-xs">{error.stack}</pre></div>
          </div>
        </details>
      </div>
    )
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex justify-between items-center">
          <CardTitle className="flex items-center gap-2">
            <Users className="w-5 h-5" />
            Subjects ({totalElements})
          </CardTitle>
          <Button onClick={handleCreateClick}>
            <Plus className="w-4 h-4 mr-2" />
            Add Subject
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {/* Filters */}
        <div className="flex gap-4 mb-6">
          <div className="relative flex-1">
            <Search className="w-4 h-4 absolute left-3 top-3 text-gray-400" />
            <Input
              placeholder="Search subjects..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
          <Select value={selectedBoardFilter?.toString() || 'all'} onValueChange={(value) => setSelectedBoardFilter(value === 'all' ? null : parseInt(value))}>
            <SelectTrigger className="w-48">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Boards</SelectItem>
              {boardsDropdown?.map((board) => (
                <SelectItem key={board.id} value={board.id.toString()}>
                  {board.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={selectedGradeFilter?.toString() || 'all'} onValueChange={(value) => setSelectedGradeFilter(value === 'all' ? null : parseInt(value))}>
            <SelectTrigger className="w-48">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Grades</SelectItem>
              {gradesDropdown?.map((grade) => (
                <SelectItem key={grade.id} value={grade.id.toString()}>
                  {grade.displayName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={statusFilter} onValueChange={(value: any) => setStatusFilter(value)}>
            <SelectTrigger className="w-40">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Status</SelectItem>
              <SelectItem value="active">Active</SelectItem>
              <SelectItem value="inactive">Inactive</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Subjects List */}
        <div className="space-y-4">
          {subjects.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              No subjects found. Create your first subject to get started.
            </div>
          ) : (
            subjects.map((subject) => (
              <div key={subject.id} className="border rounded-lg p-4 flex justify-between items-center">
                <div className="flex items-center gap-4">
                  <div>
                    <h3 className="font-semibold">{subject.name}</h3>
                    <p className="text-sm text-gray-500">Name: {subject.name}</p>
                    <p className="text-sm text-gray-500">
                      Grade: {gradesDropdown?.find(g => g.id === subject.gradeId)?.displayName || 'Unknown'}
                    </p>
                    <p className="text-sm text-gray-500">
                      Created: {new Date(subject.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                  <Badge variant={subject.active ? "default" : "secondary"}>
                    {subject.active ? "Active" : "Inactive"}
                  </Badge>
                </div>
                <div className="flex gap-2">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleEditClick(subject)}
                  >
                    <Edit className="w-4 h-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleDelete(subject)}
                    className="text-red-500 hover:text-red-700"
                  >
                    <Trash2 className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Pagination */}
        {totalElements > pageSize && (
          <div className="flex justify-between items-center mt-6">
            <div className="text-sm text-gray-500">
              Showing {currentPage * pageSize + 1} to {Math.min((currentPage + 1) * pageSize, totalElements)} of {totalElements} results
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                disabled={currentPage === 0}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage(currentPage + 1)}
                disabled={(currentPage + 1) * pageSize >= totalElements}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </CardContent>

      {/* Create Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create New Subject</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="grade">Grade</Label>
              <Select value={formData.gradeId.toString()} onValueChange={(value) => setFormData({ ...formData, gradeId: parseInt(value) })}>
                <SelectTrigger>
                  <SelectValue placeholder="Select a grade" />
                </SelectTrigger>
                <SelectContent>
                  {gradesDropdown?.map((grade) => (
                    <SelectItem key={grade.id} value={grade.id.toString()}>
                      {grade.displayName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="name">Subject Name</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter subject name (e.g., Physics)"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="active"
                checked={formData.active}
                onCheckedChange={(checked) => setFormData({ ...formData, active: checked })}
              />
              <Label htmlFor="active">Active</Label>
            </div>
            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => setIsCreateDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button
                onClick={handleCreateSubmit}
                disabled={!formData.name.trim() || formData.gradeId === 0 || createSubjectMutation.isPending}
              >
                {createSubjectMutation.isPending ? 'Creating...' : 'Create Subject'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Subject</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="edit-grade">Grade</Label>
              <Select value={formData.gradeId.toString()} onValueChange={(value) => setFormData({ ...formData, gradeId: parseInt(value) })}>
                <SelectTrigger>
                  <SelectValue placeholder="Select a grade" />
                </SelectTrigger>
                <SelectContent>
                  {gradesDropdown?.map((grade) => (
                    <SelectItem key={grade.id} value={grade.id.toString()}>
                      {grade.displayName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="edit-name">Subject Name</Label>
              <Input
                id="edit-name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter subject name"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="edit-active"
                checked={formData.active}
                onCheckedChange={(checked) => setFormData({ ...formData, active: checked })}
              />
              <Label htmlFor="edit-active">Active</Label>
            </div>
            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => setIsEditDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button
                onClick={handleUpdateSubmit}
                disabled={!formData.name.trim() || formData.gradeId === 0 || updateSubjectMutation.isPending}
              >
                {updateSubjectMutation.isPending ? 'Updating...' : 'Update Subject'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </Card>
  )
}

// Chapters Tab Component
function ChaptersTab() {
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all')
  const [selectedBoard, setSelectedBoard] = useState<number>(0)
  const [selectedGrade, setSelectedGrade] = useState<number>(0)
  const [selectedSubject, setSelectedSubject] = useState<number>(0)
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [editingChapter, setEditingChapter] = useState<ChapterDto | null>(null)
  const [formData, setFormData] = useState<CreateChapterRequest>({ 
    name: '', 
    subjectId: 0, 
    boardId: 0, 
    gradeId: 0, 
    active: true 
  })

  // API hooks
  const { data: chaptersData, isLoading, error, refetch } = useChapters({
    page: currentPage,
    size: pageSize,
    search: searchTerm || undefined,
    subjectId: selectedSubject || undefined,
    active: statusFilter === 'all' ? undefined : statusFilter === 'active'
  })

  const { data: boardsDropdown } = useBoardsDropdown()
  const { data: gradesDropdown } = useGradesDropdown(selectedBoard || undefined)
  const { data: subjectsDropdown } = useSubjectsDropdown(selectedGrade || undefined)

  const createChapterMutation = useCreateChapter()
  const updateChapterMutation = useUpdateChapter()
  const deleteChapterMutation = useDeleteChapter()

  const chapters = chaptersData?.content || []
  const totalElements = chaptersData?.totalElements || 0

  // Handle filter changes
  const onFilterChange = () => {
    setCurrentPage(0)
    refetch()
  }

  // Handle create button click
  const handleCreateClick = () => {
    if (selectedSubject && selectedGrade && selectedBoard) {
      setFormData({ 
        name: '', 
        subjectId: selectedSubject, 
        boardId: selectedBoard,
        gradeId: selectedGrade,
        active: true 
      })
      setIsCreateDialogOpen(true)
    }
  }

  // Handle edit button click
  const handleEditClick = (chapter: ChapterDto) => {
    setEditingChapter(chapter)
    setFormData({ 
      name: chapter.name, 
      subjectId: chapter.subjectId, 
      boardId: chapter.boardId,
      gradeId: 0, // Note: chapters don't have gradeId in DTO
      active: chapter.active 
    })
    setIsEditDialogOpen(true)
  }

  // Handle create submit
  const handleCreateSubmit = async () => {
    try {
      await createChapterMutation.mutateAsync(formData)
      setIsCreateDialogOpen(false)
      setFormData({ 
        name: '', 
        subjectId: 0, 
        boardId: 0, 
        gradeId: 0, 
        active: true 
      })
      refetch()
      toast.success('Chapter created successfully')
    } catch (error) {
      console.error('Create chapter failed:', error)
      toast.error('Failed to create chapter')
    }
  }

  // Handle update submit
  const handleUpdateSubmit = async () => {
    if (!editingChapter) return
    
    try {
      await updateChapterMutation.mutateAsync({
        id: editingChapter.id,
        request: { name: formData.name, subjectId: formData.subjectId, active: formData.active }
      })
      setIsEditDialogOpen(false)
      setEditingChapter(null)
      setFormData({ 
        name: '', 
        subjectId: 0, 
        boardId: 0, 
        gradeId: 0, 
        active: true 
      })
      refetch()
      toast.success('Chapter updated successfully')
    } catch (error) {
      console.error('Update chapter failed:', error)
      toast.error('Failed to update chapter')
    }
  }

  // Handle delete
  const handleDelete = async (chapter: ChapterDto) => {
    if (confirm(`Are you sure you want to delete "${chapter.name}"?`)) {
      try {
        await deleteChapterMutation.mutateAsync({ id: chapter.id })
        refetch()
        toast.success('Chapter deleted successfully')
      } catch (error) {
        console.error('Delete chapter failed:', error)
        toast.error('Failed to delete chapter')
      }
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <span>Chapters Management</span>
          <Button onClick={handleCreateClick} disabled={!selectedSubject}>
            <Plus className="w-4 h-4 mr-2" />
            Add Chapter
          </Button>
        </CardTitle>
      </CardHeader>
      <CardContent>
        {/* Filters */}
        <div className="grid grid-cols-1 md:grid-cols-5 gap-4 mb-6">
          <div>
            <Input
              placeholder="Search chapters..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full"
            />
          </div>
          <Select value={selectedBoard.toString()} onValueChange={(value) => {
            const boardId = parseInt(value)
            setSelectedBoard(boardId)
            setSelectedGrade(0)
            setSelectedSubject(0)
          }}>
            <SelectTrigger>
              <SelectValue placeholder="Select Board" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="0">All Boards</SelectItem>
              {boardsDropdown?.map((board) => (
                <SelectItem key={board.id} value={board.id.toString()}>
                  {board.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={selectedGrade.toString()} onValueChange={(value) => {
            const gradeId = parseInt(value)
            setSelectedGrade(gradeId)
            setSelectedSubject(0)
          }} disabled={!selectedBoard}>
            <SelectTrigger>
              <SelectValue placeholder="Select Grade" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="0">All Grades</SelectItem>
              {gradesDropdown?.map((grade) => (
                <SelectItem key={grade.id} value={grade.id.toString()}>
                  {grade.displayName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={selectedSubject.toString()} onValueChange={(value) => {
            setSelectedSubject(parseInt(value))
            onFilterChange()
          }} disabled={!selectedGrade}>
            <SelectTrigger>
              <SelectValue placeholder="Select Subject" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="0">All Subjects</SelectItem>
              {subjectsDropdown?.map((subject) => (
                <SelectItem key={subject.id} value={subject.id.toString()}>
                  {subject.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={statusFilter} onValueChange={(value: 'all' | 'active' | 'inactive') => {
            setStatusFilter(value)
            onFilterChange()
          }}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Status</SelectItem>
              <SelectItem value="active">Active</SelectItem>
              <SelectItem value="inactive">Inactive</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Chapters List */}
        {isLoading ? (
          <div className="text-center py-8">
            <p className="text-gray-500">Loading chapters...</p>
          </div>
        ) : error ? (
          <div className="text-center py-8">
            <p className="text-red-500">Error loading chapters</p>
          </div>
        ) : chapters.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-gray-500">
              {selectedSubject ? 'No chapters found for the selected filters' : 'Please select a subject to view chapters'}
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {chapters.map((chapter) => (
              <Card key={chapter.id} className="border border-gray-200">
                <CardContent className="p-4">
                  <div className="flex justify-between items-start mb-2">
                    <h3 className="font-semibold">{chapter.name}</h3>
                    <div className="flex gap-1">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleEditClick(chapter)}
                      >
                        <Edit className="w-4 h-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDelete(chapter)}
                        className="text-red-600 hover:text-red-700"
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  <div className="text-sm text-gray-600 space-y-1">
                    <p>Subject: {chapter.subjectName || 'Unknown'}</p>
                    <p>Board: {boardsDropdown?.find(b => b.id === chapter.boardId)?.name || 'Unknown'}</p>
                  </div>
                  <div className="mt-2">
                    <Badge variant={chapter.active ? 'default' : 'secondary'}>
                      {chapter.active ? 'Active' : 'Inactive'}
                    </Badge>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}

        {/* Pagination */}
        {totalElements > pageSize && (
          <div className="flex justify-between items-center mt-6">
            <div className="text-sm text-gray-600">
              Showing {chapters.length} of {totalElements} chapters
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setCurrentPage(currentPage - 1)
                  refetch()
                }}
                disabled={currentPage === 0}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setCurrentPage(currentPage + 1)
                  refetch()
                }}
                disabled={(currentPage + 1) * pageSize >= totalElements}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </CardContent>

      {/* Create Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Create Chapter</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="subject-select">Subject</Label>
              <Select value={formData.subjectId.toString()} onValueChange={(value) => 
                setFormData({ ...formData, subjectId: parseInt(value) })
              }>
                <SelectTrigger>
                  <SelectValue placeholder="Select a subject" />
                </SelectTrigger>
                <SelectContent>
                  {subjectsDropdown?.map((subject) => (
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
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter chapter name"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="active"
                checked={formData.active}
                onCheckedChange={(checked) => setFormData({ ...formData, active: checked })}
              />
              <Label htmlFor="active">Active</Label>
            </div>
            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => setIsCreateDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button
                onClick={handleCreateSubmit}
                disabled={!formData.name.trim() || formData.subjectId === 0 || createChapterMutation.isPending}
              >
                {createChapterMutation.isPending ? 'Creating...' : 'Create Chapter'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Edit Chapter</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="edit-name">Chapter Name</Label>
              <Input
                id="edit-name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter chapter name"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="edit-active"
                checked={formData.active}
                onCheckedChange={(checked) => setFormData({ ...formData, active: checked })}
              />
              <Label htmlFor="edit-active">Active</Label>
            </div>
            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => setIsEditDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button
                onClick={handleUpdateSubmit}
                disabled={!formData.name.trim() || updateChapterMutation.isPending}
              >
                {updateChapterMutation.isPending ? 'Updating...' : 'Update Chapter'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </Card>
  )
}

// Topics Tab Component  
function TopicsTab() {
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all')
  const [selectedBoard, setSelectedBoard] = useState<number>(0)
  const [selectedGrade, setSelectedGrade] = useState<number>(0)
  const [selectedSubject, setSelectedSubject] = useState<number>(0)
  const [selectedChapter, setSelectedChapter] = useState<number>(0)
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [editingTopic, setEditingTopic] = useState<TopicDto | null>(null)
  const [formData, setFormData] = useState<CreateTopicRequest>({ 
    title: '', 
    chapterId: 0,
    boardId: 0,
    gradeId: 0,
    subjectId: 0,
    description: '', 
    summary: '', 
    expectedTimeMins: 0, 
    active: true 
  })

  // API hooks
  const { data: topicsData, isLoading, error, refetch } = useTopics({
    page: currentPage,
    size: pageSize,
    search: searchTerm || undefined,
    chapterId: selectedChapter || undefined,
    subjectId: selectedSubject || undefined,
    active: statusFilter === 'all' ? undefined : statusFilter === 'active'
  })

  const { data: boardsDropdown } = useBoardsDropdown()
  const { data: gradesDropdown } = useGradesDropdown(selectedBoard || undefined)
  const { data: subjectsDropdown } = useSubjectsDropdown(selectedGrade || undefined)
  const { data: chaptersDropdown } = useChaptersDropdown(selectedSubject || undefined)

  const createTopicMutation = useCreateTopic()
  const updateTopicMutation = useUpdateTopic()
  const deleteTopicMutation = useDeleteTopic()

  const topics = topicsData?.content || []
  const totalElements = topicsData?.totalElements || 0

  // Handle filter changes
  const onFilterChange = () => {
    setCurrentPage(0)
    refetch()
  }

  // Handle create button click
  const handleCreateClick = () => {
    if (selectedChapter && selectedSubject && selectedGrade && selectedBoard) {
      setFormData({ 
        title: '', 
        chapterId: selectedChapter,
        boardId: selectedBoard,
        gradeId: selectedGrade,
        subjectId: selectedSubject,
        description: '', 
        summary: '', 
        expectedTimeMins: 0, 
        active: true 
      })
      setIsCreateDialogOpen(true)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <span>Topics Management</span>
          <Button onClick={handleCreateClick} disabled={!selectedChapter}>
            <Plus className="w-4 h-4 mr-2" />
            Add Topic
          </Button>
        </CardTitle>
      </CardHeader>
      <CardContent>
        {/* Filters */}
        <div className="grid grid-cols-1 md:grid-cols-6 gap-4 mb-6">
          <div>
            <Input
              placeholder="Search topics..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full"
            />
          </div>
          <Select value={selectedBoard.toString()} onValueChange={(value) => {
            const boardId = parseInt(value)
            setSelectedBoard(boardId)
            setSelectedGrade(0)
            setSelectedSubject(0)
            setSelectedChapter(0)
          }}>
            <SelectTrigger>
              <SelectValue placeholder="Select Board" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="0">All Boards</SelectItem>
              {boardsDropdown?.map((board) => (
                <SelectItem key={board.id} value={board.id.toString()}>
                  {board.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={selectedGrade.toString()} onValueChange={(value) => {
            const gradeId = parseInt(value)
            setSelectedGrade(gradeId)
            setSelectedSubject(0)
            setSelectedChapter(0)
          }} disabled={!selectedBoard}>
            <SelectTrigger>
              <SelectValue placeholder="Select Grade" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="0">All Grades</SelectItem>
              {gradesDropdown?.map((grade) => (
                <SelectItem key={grade.id} value={grade.id.toString()}>
                  {grade.displayName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={selectedSubject.toString()} onValueChange={(value) => {
            const subjectId = parseInt(value)
            setSelectedSubject(subjectId)
            setSelectedChapter(0)
          }} disabled={!selectedGrade}>
            <SelectTrigger>
              <SelectValue placeholder="Select Subject" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="0">All Subjects</SelectItem>
              {subjectsDropdown?.map((subject) => (
                <SelectItem key={subject.id} value={subject.id.toString()}>
                  {subject.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={selectedChapter.toString()} onValueChange={(value) => {
            setSelectedChapter(parseInt(value))
            onFilterChange()
          }} disabled={!selectedSubject}>
            <SelectTrigger>
              <SelectValue placeholder="Select Chapter" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="0">All Chapters</SelectItem>
              {chaptersDropdown?.map((chapter) => (
                <SelectItem key={chapter.id} value={chapter.id.toString()}>
                  {chapter.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={statusFilter} onValueChange={(value: 'all' | 'active' | 'inactive') => {
            setStatusFilter(value)
            onFilterChange()
          }}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Status</SelectItem>
              <SelectItem value="active">Active</SelectItem>
              <SelectItem value="inactive">Inactive</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Topics List */}
        {isLoading ? (
          <div className="text-center py-8">
            <p className="text-gray-500">Loading topics...</p>
          </div>
        ) : error ? (
          <div className="text-center py-8">
            <p className="text-red-500">Error loading topics</p>
          </div>
        ) : topics.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-gray-500">
              {selectedChapter ? 'No topics found for the selected filters' : 'Please select a chapter to view topics'}
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {topics.map((topic) => (
              <Card key={topic.id} className="border border-gray-200">
                <CardContent className="p-4">
                  <div className="flex justify-between items-start mb-2">
                    <h3 className="font-semibold">{topic.title}</h3>
                    <div className="flex gap-1">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => {
                          setEditingTopic(topic)
                          setFormData({ 
                            title: topic.title, 
                            chapterId: topic.chapterId,
                            boardId: topic.boardId,
                            gradeId: 0, // Note: topics don't have gradeId in DTO
                            subjectId: topic.subjectId,
                            description: topic.description || '',
                            summary: topic.summary || '',
                            expectedTimeMins: topic.expectedTimeMins || 0,
                            active: topic.active 
                          })
                          setIsEditDialogOpen(true)
                        }}
                      >
                        <Edit className="w-4 h-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => {
                          if (confirm(`Are you sure you want to delete "${topic.title}"?`)) {
                            deleteTopicMutation.mutateAsync({ id: topic.id }).then(() => {
                              refetch()
                              toast.success('Topic deleted successfully')
                            }).catch(() => {
                              toast.error('Failed to delete topic')
                            })
                          }
                        }}
                        className="text-red-600 hover:text-red-700"
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  <div className="text-sm text-gray-600 space-y-1">
                    <p>Chapter: {topic.chapterName || 'Unknown'}</p>
                    <p>Subject: {topic.subjectName || 'Unknown'}</p>
                    {topic.expectedTimeMins && (
                      <p>Duration: {topic.expectedTimeMins} mins</p>
                    )}
                  </div>
                  {topic.description && (
                    <p className="text-sm text-gray-600 mt-2 line-clamp-2">
                      {topic.description}
                    </p>
                  )}
                  <div className="mt-2">
                    <Badge variant={topic.active ? 'default' : 'secondary'}>
                      {topic.active ? 'Active' : 'Inactive'}
                    </Badge>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </CardContent>

      {/* Create Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle>Create Topic</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="title">Topic Title</Label>
              <Input
                id="title"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                placeholder="Enter topic title"
              />
            </div>
            <div>
              <Label htmlFor="description">Description (Optional)</Label>
              <Input
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Enter description"
              />
            </div>
            <div>
              <Label htmlFor="expectedTimeMins">Expected Time (Minutes, Optional)</Label>
              <Input
                id="expectedTimeMins"
                type="number"
                value={formData.expectedTimeMins || ''}
                onChange={(e) => setFormData({ ...formData, expectedTimeMins: parseInt(e.target.value) || 0 })}
                placeholder="Enter expected time in minutes"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="active"
                checked={formData.active}
                onCheckedChange={(checked) => setFormData({ ...formData, active: checked })}
              />
              <Label htmlFor="active">Active</Label>
            </div>
            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => setIsCreateDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button
                onClick={async () => {
                  try {
                    await createTopicMutation.mutateAsync(formData)
                    setIsCreateDialogOpen(false)
                    setFormData({ 
                      title: '', 
                      chapterId: 0,
                      boardId: 0,
                      gradeId: 0,
                      subjectId: 0,
                      description: '', 
                      summary: '', 
                      expectedTimeMins: 0, 
                      active: true 
                    })
                    refetch()
                    toast.success('Topic created successfully')
                  } catch (error) {
                    console.error('Create topic failed:', error)
                    toast.error('Failed to create topic')
                  }
                }}
                disabled={!formData.title.trim() || createTopicMutation.isPending}
              >
                {createTopicMutation.isPending ? 'Creating...' : 'Create Topic'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle>Edit Topic</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="edit-title">Topic Title</Label>
              <Input
                id="edit-title"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                placeholder="Enter topic title"
              />
            </div>
            <div>
              <Label htmlFor="edit-description">Description (Optional)</Label>
              <Input
                id="edit-description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Enter description"
              />
            </div>
            <div>
              <Label htmlFor="edit-expectedTimeMins">Expected Time (Minutes, Optional)</Label>
              <Input
                id="edit-expectedTimeMins"
                type="number"
                value={formData.expectedTimeMins || ''}
                onChange={(e) => setFormData({ ...formData, expectedTimeMins: parseInt(e.target.value) || 0 })}
                placeholder="Enter expected time in minutes"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                id="edit-active"
                checked={formData.active}
                onCheckedChange={(checked) => setFormData({ ...formData, active: checked })}
              />
              <Label htmlFor="edit-active">Active</Label>
            </div>
            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => setIsEditDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button
                onClick={async () => {
                  if (!editingTopic) return
                  
                  try {
                    await updateTopicMutation.mutateAsync({
                      id: editingTopic.id,
                      request: { 
                        title: formData.title, 
                        description: formData.description,
                        summary: formData.summary,
                        expectedTimeMins: formData.expectedTimeMins,
                        boardId: formData.boardId,
                        subjectId: formData.subjectId,
                        chapterId: formData.chapterId,
                        active: formData.active 
                      }
                    })
                    setIsEditDialogOpen(false)
                    setEditingTopic(null)
                    refetch()
                    toast.success('Topic updated successfully')
                  } catch (error) {
                    console.error('Update topic failed:', error)
                    toast.error('Failed to update topic')
                  }
                }}
                disabled={!formData.title.trim() || updateTopicMutation.isPending}
              >
                {updateTopicMutation.isPending ? 'Updating...' : 'Update Topic'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </Card>
  )
}

// Topic Notes Tab Component
function TopicNotesTab() {
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all')
  const [selectedBoard, setSelectedBoard] = useState<number>(0)
  const [selectedGrade, setSelectedGrade] = useState<number>(0)
  const [selectedSubject, setSelectedSubject] = useState<number>(0)
  const [selectedChapter, setSelectedChapter] = useState<number>(0)
  const [selectedTopic, setSelectedTopic] = useState<number>(0)
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)

  // API hooks
  const { data: topicNotesData, isLoading, error, refetch } = useTopicNotes({
    page: currentPage,
    size: pageSize,
    search: searchTerm || undefined,
    topicId: selectedTopic || undefined,
    active: statusFilter === 'all' ? undefined : statusFilter === 'active'
  })

  const { data: boardsDropdown } = useBoardsDropdown()
  const { data: gradesDropdown } = useGradesDropdown(selectedBoard || undefined)
  const { data: subjectsDropdown } = useSubjectsDropdown(selectedGrade || undefined)
  const { data: chaptersDropdown } = useChaptersDropdown(selectedSubject || undefined)
  const { data: topicsDropdown } = useTopicsDropdown(selectedChapter || undefined)

  const topicNotes = topicNotesData?.content || []
  const totalElements = topicNotesData?.totalElements || 0

  // Handle filter changes
  const onFilterChange = () => {
    setCurrentPage(0)
    refetch()
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <span>Topic Notes Management</span>
          <Button disabled={!selectedTopic}>
            <Plus className="w-4 h-4 mr-2" />
            Add Topic Note
          </Button>
        </CardTitle>
      </CardHeader>
      <CardContent>
        {/* Filters */}
        <div className="grid grid-cols-1 md:grid-cols-7 gap-4 mb-6">
          <div>
            <Input
              placeholder="Search notes..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full"
            />
          </div>
          <Select value={selectedBoard.toString()} onValueChange={(value) => {
            const boardId = parseInt(value)
            setSelectedBoard(boardId)
            setSelectedGrade(0)
            setSelectedSubject(0)
            setSelectedChapter(0)
            setSelectedTopic(0)
          }}>
            <SelectTrigger>
              <SelectValue placeholder="Select Board" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="0">All Boards</SelectItem>
              {boardsDropdown?.map((board) => (
                <SelectItem key={board.id} value={board.id.toString()}>
                  {board.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={selectedGrade.toString()} onValueChange={(value) => {
            const gradeId = parseInt(value)
            setSelectedGrade(gradeId)
            setSelectedSubject(0)
            setSelectedChapter(0)
            setSelectedTopic(0)
          }} disabled={!selectedBoard}>
            <SelectTrigger>
              <SelectValue placeholder="Select Grade" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="0">All Grades</SelectItem>
              {gradesDropdown?.map((grade) => (
                <SelectItem key={grade.id} value={grade.id.toString()}>
                  {grade.displayName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={selectedSubject.toString()} onValueChange={(value) => {
            const subjectId = parseInt(value)
            setSelectedSubject(subjectId)
            setSelectedChapter(0)
            setSelectedTopic(0)
          }} disabled={!selectedGrade}>
            <SelectTrigger>
              <SelectValue placeholder="Select Subject" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="0">All Subjects</SelectItem>
              {subjectsDropdown?.map((subject) => (
                <SelectItem key={subject.id} value={subject.id.toString()}>
                  {subject.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={selectedChapter.toString()} onValueChange={(value) => {
            const chapterId = parseInt(value)
            setSelectedChapter(chapterId)
            setSelectedTopic(0)
          }} disabled={!selectedSubject}>
            <SelectTrigger>
              <SelectValue placeholder="Select Chapter" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="0">All Chapters</SelectItem>
              {chaptersDropdown?.map((chapter) => (
                <SelectItem key={chapter.id} value={chapter.id.toString()}>
                  {chapter.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={selectedTopic.toString()} onValueChange={(value) => {
            setSelectedTopic(parseInt(value))
            onFilterChange()
          }} disabled={!selectedChapter}>
            <SelectTrigger>
              <SelectValue placeholder="Select Topic" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="0">All Topics</SelectItem>
              {topicsDropdown?.map((topic) => (
                <SelectItem key={topic.id} value={topic.id.toString()}>
                  {topic.title}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={statusFilter} onValueChange={(value: 'all' | 'active' | 'inactive') => {
            setStatusFilter(value)
            onFilterChange()
          }}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Status</SelectItem>
              <SelectItem value="active">Active</SelectItem>
              <SelectItem value="inactive">Inactive</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Topic Notes List */}
        {isLoading ? (
          <div className="text-center py-8">
            <p className="text-gray-500">Loading topic notes...</p>
          </div>
        ) : error ? (
          <div className="text-center py-8">
            <p className="text-red-500">Error loading topic notes</p>
          </div>
        ) : topicNotes.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-gray-500">
              {selectedTopic ? 'No topic notes found for the selected filters' : 'Please select a topic to view notes'}
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-4">
            {topicNotes.map((note) => (
              <Card key={note.id} className="border border-gray-200">
                <CardContent className="p-4">
                  <div className="flex justify-between items-start mb-2">
                    <h3 className="font-semibold">{note.title}</h3>
                    <div className="flex gap-1">
                      <Button variant="ghost" size="sm">
                        <Edit className="w-4 h-4" />
                      </Button>
                      <Button variant="ghost" size="sm" className="text-red-600 hover:text-red-700">
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  <div className="text-sm text-gray-600 mb-2">
                    <p>Topic: {note.topicTitle || 'Unknown'}</p>
                  </div>
                  <div className="text-sm text-gray-800 max-h-20 overflow-hidden">
                    {note.content}
                  </div>
                  <div className="mt-2 flex justify-between">
                    <Badge variant={note.active ? 'default' : 'secondary'}>
                      {note.active ? 'Active' : 'Inactive'}
                    </Badge>
                    <Button variant="outline" size="sm">
                      View Full
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
