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
  AlertCircle,
  TreePine
} from 'lucide-react'

// Import our new typed API services and hooks
import {
  useBoards,
  useBoardsDropdown,
  useCreateBoard,
  useUpdateBoard,
  useDeleteBoard,
  useBoardDeletionImpact,
  useGrades,
  useGradesDropdown,
  useCreateGrade,
  useUpdateGrade,
  useDeleteGrade,
  useSubjects,
  useSubjectsDropdown,
  useCreateSubject,
  useUpdateSubject,
  useDeleteSubject,
  useSubjectDeletionImpact,
  useChapters,
  useChaptersDropdown,
  useCreateChapter,
  useUpdateChapter,
  useDeleteChapter,
  useTopics,
  useTopicsDropdown,
  useCreateTopic,
  useUpdateTopic,
  useDeleteTopic,
  useTopicNotes,
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
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all')
  
  // Hierarchical filter states
  const [selectedBoardFilter, setSelectedBoardFilter] = useState<number | null>(null)
  const [selectedGradeFilter, setSelectedGradeFilter] = useState<number | null>(null)
  const [selectedSubjectFilter, setSelectedSubjectFilter] = useState<number | null>(null)
  const [selectedChapterFilter, setSelectedChapterFilter] = useState<number | null>(null)
  const [selectedTopicFilter, setSelectedTopicFilter] = useState<number | null>(null)
  
  // Pagination states
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  
  // Dialog states
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false)
  const [isContentTreeDialogOpen, setIsContentTreeDialogOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<any>(null)
  const [deletingItem, setDeletingItem] = useState<any>(null)
  const [formData, setFormData] = useState<any>({})

  // Content tree state
  const [contentTreeBoardId, setContentTreeBoardId] = useState<number | null>(null)

  // Reset pagination when filters change
  const handleFilterChange = () => {
    setCurrentPage(0)
  }

  // Reset filters when switching tabs
  const resetFilters = () => {
    setSelectedBoardFilter(null)
    setSelectedGradeFilter(null)
    setSelectedSubjectFilter(null)
    setSelectedChapterFilter(null)
    setSelectedTopicFilter(null)
    setCurrentPage(0)
    setSearchTerm('')
    setStatusFilter('all')
  }

  useEffect(() => {
    resetFilters()
  }, [activeTab])

  // Filter change handlers
  useEffect(() => {
    handleFilterChange()
  }, [searchTerm, statusFilter])

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

          {/* Content Tree Button */}
          <div className="mb-6">
            <Button 
              onClick={() => setIsContentTreeDialogOpen(true)}
              className="bg-purple-600 hover:bg-purple-700"
            >
              <TreePine className="w-4 h-4 mr-2" />
              View Content Tree
            </Button>
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
                <FileText className="w-4 h-4" />
                Topics
              </TabsTrigger>
              <TabsTrigger value="notes" className="flex items-center gap-2">
                <FileText className="w-4 h-4" />
                Topic Notes
              </TabsTrigger>
            </TabsList>

            <TabsContent value="boards">
              <BoardsTab 
                searchTerm={searchTerm}
                setSearchTerm={setSearchTerm}
                statusFilter={statusFilter}
                setStatusFilter={setStatusFilter}
                currentPage={currentPage}
                setCurrentPage={setCurrentPage}
                pageSize={pageSize}
                setPageSize={setPageSize}
                onFilterChange={handleFilterChange}
                isCreateDialogOpen={isCreateDialogOpen}
                setIsCreateDialogOpen={setIsCreateDialogOpen}
                isEditDialogOpen={isEditDialogOpen}
                setIsEditDialogOpen={setIsEditDialogOpen}
                isDeleteDialogOpen={isDeleteDialogOpen}
                setIsDeleteDialogOpen={setIsDeleteDialogOpen}
                editingItem={editingItem}
                setEditingItem={setEditingItem}
                deletingItem={deletingItem}
                setDeletingItem={setDeletingItem}
                formData={formData}
                setFormData={setFormData}
              />
            </TabsContent>

            <TabsContent value="grades">
              <GradesTab 
                searchTerm={searchTerm}
                setSearchTerm={setSearchTerm}
                statusFilter={statusFilter}
                setStatusFilter={setStatusFilter}
                selectedBoardFilter={selectedBoardFilter}
                setSelectedBoardFilter={setSelectedBoardFilter}
                currentPage={currentPage}
                setCurrentPage={setCurrentPage}
                pageSize={pageSize}
                setPageSize={setPageSize}
                onFilterChange={handleFilterChange}
                isCreateDialogOpen={isCreateDialogOpen}
                setIsCreateDialogOpen={setIsCreateDialogOpen}
                isEditDialogOpen={isEditDialogOpen}
                setIsEditDialogOpen={setIsEditDialogOpen}
                isDeleteDialogOpen={isDeleteDialogOpen}
                setIsDeleteDialogOpen={setIsDeleteDialogOpen}
                editingItem={editingItem}
                setEditingItem={setEditingItem}
                deletingItem={deletingItem}
                setDeletingItem={setDeletingItem}
                formData={formData}
                setFormData={setFormData}
              />
            </TabsContent>

            <TabsContent value="subjects">
              <SubjectsTab 
                searchTerm={searchTerm}
                setSearchTerm={setSearchTerm}
                statusFilter={statusFilter}
                setStatusFilter={setStatusFilter}
                selectedBoardFilter={selectedBoardFilter}
                setSelectedBoardFilter={setSelectedBoardFilter}
                selectedGradeFilter={selectedGradeFilter}
                setSelectedGradeFilter={setSelectedGradeFilter}
                currentPage={currentPage}
                setCurrentPage={setCurrentPage}
                pageSize={pageSize}
                setPageSize={setPageSize}
                onFilterChange={handleFilterChange}
                isCreateDialogOpen={isCreateDialogOpen}
                setIsCreateDialogOpen={setIsCreateDialogOpen}
                isEditDialogOpen={isEditDialogOpen}
                setIsEditDialogOpen={setIsEditDialogOpen}
                isDeleteDialogOpen={isDeleteDialogOpen}
                setIsDeleteDialogOpen={setIsDeleteDialogOpen}
                editingItem={editingItem}
                setEditingItem={setEditingItem}
                deletingItem={deletingItem}
                setDeletingItem={setDeletingItem}
                formData={formData}
                setFormData={setFormData}
              />
            </TabsContent>

            <TabsContent value="chapters">
              <ChaptersTab 
                searchTerm={searchTerm}
                setSearchTerm={setSearchTerm}
                statusFilter={statusFilter}
                setStatusFilter={setStatusFilter}
                selectedBoardFilter={selectedBoardFilter}
                setSelectedBoardFilter={setSelectedBoardFilter}
                selectedGradeFilter={selectedGradeFilter}
                setSelectedGradeFilter={setSelectedGradeFilter}
                selectedSubjectFilter={selectedSubjectFilter}
                setSelectedSubjectFilter={setSelectedSubjectFilter}
                currentPage={currentPage}
                setCurrentPage={setCurrentPage}
                pageSize={pageSize}
                setPageSize={setPageSize}
                onFilterChange={handleFilterChange}
                isCreateDialogOpen={isCreateDialogOpen}
                setIsCreateDialogOpen={setIsCreateDialogOpen}
                isEditDialogOpen={isEditDialogOpen}
                setIsEditDialogOpen={setIsEditDialogOpen}
                isDeleteDialogOpen={isDeleteDialogOpen}
                setIsDeleteDialogOpen={setIsDeleteDialogOpen}
                editingItem={editingItem}
                setEditingItem={setEditingItem}
                deletingItem={deletingItem}
                setDeletingItem={setDeletingItem}
                formData={formData}
                setFormData={setFormData}
              />
            </TabsContent>

            <TabsContent value="topics">
              <TopicsTab 
                searchTerm={searchTerm}
                setSearchTerm={setSearchTerm}
                statusFilter={statusFilter}
                setStatusFilter={setStatusFilter}
                selectedBoardFilter={selectedBoardFilter}
                setSelectedBoardFilter={setSelectedBoardFilter}
                selectedGradeFilter={selectedGradeFilter}
                setSelectedGradeFilter={setSelectedGradeFilter}
                selectedSubjectFilter={selectedSubjectFilter}
                setSelectedSubjectFilter={setSelectedSubjectFilter}
                selectedChapterFilter={selectedChapterFilter}
                setSelectedChapterFilter={setSelectedChapterFilter}
                currentPage={currentPage}
                setCurrentPage={setCurrentPage}
                pageSize={pageSize}
                setPageSize={setPageSize}
                onFilterChange={handleFilterChange}
                isCreateDialogOpen={isCreateDialogOpen}
                setIsCreateDialogOpen={setIsCreateDialogOpen}
                isEditDialogOpen={isEditDialogOpen}
                setIsEditDialogOpen={setIsEditDialogOpen}
                isDeleteDialogOpen={isDeleteDialogOpen}
                setIsDeleteDialogOpen={setIsDeleteDialogOpen}
                editingItem={editingItem}
                setEditingItem={setEditingItem}
                deletingItem={deletingItem}
                setDeletingItem={setDeletingItem}
                formData={formData}
                setFormData={setFormData}
              />
            </TabsContent>

            <TabsContent value="notes">
              <TopicNotesTab 
                searchTerm={searchTerm}
                setSearchTerm={setSearchTerm}
                statusFilter={statusFilter}
                setStatusFilter={setStatusFilter}
                selectedBoardFilter={selectedBoardFilter}
                setSelectedBoardFilter={setSelectedBoardFilter}
                selectedGradeFilter={selectedGradeFilter}
                setSelectedGradeFilter={setSelectedGradeFilter}
                selectedSubjectFilter={selectedSubjectFilter}
                setSelectedSubjectFilter={setSelectedSubjectFilter}
                selectedChapterFilter={selectedChapterFilter}
                setSelectedChapterFilter={setSelectedChapterFilter}
                selectedTopicFilter={selectedTopicFilter}
                setSelectedTopicFilter={setSelectedTopicFilter}
                currentPage={currentPage}
                setCurrentPage={setCurrentPage}
                pageSize={pageSize}
                setPageSize={setPageSize}
                onFilterChange={handleFilterChange}
                isCreateDialogOpen={isCreateDialogOpen}
                setIsCreateDialogOpen={setIsCreateDialogOpen}
                isEditDialogOpen={isEditDialogOpen}
                setIsEditDialogOpen={setIsEditDialogOpen}
                isDeleteDialogOpen={isDeleteDialogOpen}
                setIsDeleteDialogOpen={setIsDeleteDialogOpen}
                editingItem={editingItem}
                setEditingItem={setEditingItem}
                deletingItem={deletingItem}
                setDeletingItem={setDeletingItem}
                formData={formData}
                setFormData={setFormData}
              />
            </TabsContent>
          </Tabs>

          {/* Content Tree Dialog */}
          <ContentTreeDialog 
            isOpen={isContentTreeDialogOpen}
            setIsOpen={setIsContentTreeDialogOpen}
            selectedBoardId={contentTreeBoardId}
            setSelectedBoardId={setContentTreeBoardId}
          />
        </div>
      </div>
    </AdminLayoutSimple>
  )
}

// ========================= TAB COMPONENTS =========================

interface TabProps {
  searchTerm: string
  setSearchTerm: (term: string) => void
  statusFilter: 'all' | 'active' | 'inactive'
  setStatusFilter: (filter: 'all' | 'active' | 'inactive') => void
  currentPage: number
  setCurrentPage: (page: number) => void
  pageSize: number
  setPageSize: (size: number) => void
  onFilterChange: () => void
  isCreateDialogOpen: boolean
  setIsCreateDialogOpen: (open: boolean) => void
  isEditDialogOpen: boolean
  setIsEditDialogOpen: (open: boolean) => void
  isDeleteDialogOpen: boolean
  setIsDeleteDialogOpen: (open: boolean) => void
  editingItem: any
  setEditingItem: (item: any) => void
  deletingItem: any
  setDeletingItem: (item: any) => void
  formData: any
  setFormData: (data: any) => void
}

interface ExtendedTabProps extends TabProps {
  selectedBoardFilter?: number | null
  setSelectedBoardFilter?: (id: number | null) => void
  selectedGradeFilter?: number | null
  setSelectedGradeFilter?: (id: number | null) => void
  selectedSubjectFilter?: number | null
  setSelectedSubjectFilter?: (id: number | null) => void
  selectedChapterFilter?: number | null
  setSelectedChapterFilter?: (id: number | null) => void
  selectedTopicFilter?: number | null
  setSelectedTopicFilter?: (id: number | null) => void
}

// ========================= UTILITY COMPONENTS =========================

interface PaginationControlsProps {
  currentPage: number
  totalPages: number
  pageSize: number
  totalElements: number
  onPageChange: (page: number) => void
  onPageSizeChange: (size: number) => void
}

function PaginationControls({
  currentPage,
  totalPages,
  pageSize,
  totalElements,
  onPageChange,
  onPageSizeChange
}: PaginationControlsProps) {
  if (totalElements === 0) return null

  return (
    <div className="flex items-center justify-between mt-4">
      <div className="text-sm text-gray-500">
        Showing {currentPage * pageSize + 1} to {Math.min((currentPage + 1) * pageSize, totalElements)} of {totalElements} results
      </div>
      
      <div className="flex items-center gap-2">
        <Select value={pageSize.toString()} onValueChange={(value) => onPageSizeChange(parseInt(value))}>
          <SelectTrigger className="w-20">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="5">5</SelectItem>
            <SelectItem value="10">10</SelectItem>
            <SelectItem value="20">20</SelectItem>
            <SelectItem value="50">50</SelectItem>
          </SelectContent>
        </Select>
        
        <div className="flex gap-1">
          <Button
            variant="outline"
            size="sm"
            onClick={() => onPageChange(0)}
            disabled={currentPage === 0}
          >
            First
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => onPageChange(currentPage - 1)}
            disabled={currentPage === 0}
          >
            Previous
          </Button>
          <span className="px-3 py-2 text-sm">
            Page {currentPage + 1} of {totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            onClick={() => onPageChange(currentPage + 1)}
            disabled={currentPage >= totalPages - 1}
          >
            Next
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => onPageChange(totalPages - 1)}
            disabled={currentPage >= totalPages - 1}
          >
            Last
          </Button>
        </div>
      </div>
    </div>
  )
}

// ========================= BOARDS TAB =========================

function BoardsTab(props: TabProps) {
  const {
    searchTerm, setSearchTerm, statusFilter, setStatusFilter,
    currentPage, setCurrentPage, pageSize, setPageSize, onFilterChange,
    isCreateDialogOpen, setIsCreateDialogOpen,
    isEditDialogOpen, setIsEditDialogOpen,
    isDeleteDialogOpen, setIsDeleteDialogOpen,
    editingItem, setEditingItem, deletingItem, setDeletingItem,
    formData, setFormData
  } = props

  // Get page parameters
  const pageParams = {
    page: currentPage,
    size: pageSize,
    search: searchTerm || undefined,
    active: statusFilter === 'all' ? undefined : statusFilter === 'active'
  }

  // React Query hooks
  const { data: boardsData, isLoading, error, refetch } = useBoards(pageParams)
  const createBoardMutation = useCreateBoard()
  const updateBoardMutation = useUpdateBoard()
  const deleteBoardMutation = useDeleteBoard()
  const { data: deletionImpact } = useBoardDeletionImpact(deletingItem?.id || 0)

  const boards = boardsData?.content || []
  const totalPages = boardsData?.totalPages || 0
  const totalElements = boardsData?.totalElements || 0

  // Handle form submission for create/update
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    try {
      if (editingItem) {
        // Update
        await updateBoardMutation.mutateAsync({
          id: editingItem.id,
          ...formData
        })
        setIsEditDialogOpen(false)
        setEditingItem(null)
      } else {
        // Create
        await createBoardMutation.mutateAsync(formData)
        setIsCreateDialogOpen(false)
      }
      setFormData({})
      refetch()
    } catch (error: any) {
      console.error('Form submission error:', error)
    }
  }

  // Handle delete
  const handleDelete = async () => {
    if (!deletingItem) return
    
    try {
      await deleteBoardMutation.mutateAsync(deletingItem.id)
      setIsDeleteDialogOpen(false)
      setDeletingItem(null)
      refetch()
    } catch (error: any) {
      console.error('Delete error:', error)
    }
  }

  // Handle edit button click
  const handleEditClick = (board: BoardDto) => {
    setEditingItem(board)
    setFormData({
      name: board.name,
      active: board.active
    })
    setIsEditDialogOpen(true)
  }

  // Handle delete button click
  const handleDeleteClick = (board: BoardDto) => {
    setDeletingItem(board)
    setIsDeleteDialogOpen(true)
  }

  // Handle create button click
  const handleCreateClick = () => {
    setFormData({ name: '', active: true })
    setIsCreateDialogOpen(true)
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
        {/* Search and Filter Controls */}
        <div className="flex gap-4 mb-4">
          <div className="flex-1">
            <Input
              placeholder="Search boards..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full"
            />
          </div>
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-[180px]">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Status</SelectItem>
              <SelectItem value="active">Active Only</SelectItem>
              <SelectItem value="inactive">Inactive Only</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Boards Table */}
        <div className="space-y-4">
          {boards.map((board) => (
            <Card key={board.id} className="p-4">
              <div className="flex justify-between items-center">
                <div>
                  <h3 className="font-semibold">{board.name}</h3>
                  <div className="flex items-center gap-4 mt-1">
                    <Badge variant={board.active ? 'default' : 'secondary'}>
                      {board.active ? 'Active' : 'Inactive'}
                    </Badge>
                    {board.gradesCount !== undefined && (
                      <span className="text-sm text-gray-500">
                        {board.gradesCount} Grades
                      </span>
                    )}
                    {board.subjectsCount !== undefined && (
                      <span className="text-sm text-gray-500">
                        {board.subjectsCount} Subjects
                      </span>
                    )}
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button variant="outline" size="sm" onClick={() => handleEditClick(board)}>
                    <Edit className="w-4 h-4" />
                  </Button>
                  <Button variant="outline" size="sm" onClick={() => handleDeleteClick(board)}>
                    <Trash2 className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            </Card>
          ))}

          {boards.length === 0 && (
            <div className="text-center py-8 text-gray-500">
              No boards found
            </div>
          )}
        </div>

        {/* Pagination */}
        <PaginationControls
          currentPage={currentPage}
          totalPages={totalPages}
          pageSize={pageSize}
          totalElements={totalElements}
          onPageChange={setCurrentPage}
          onPageSizeChange={setPageSize}
        />

        {/* Create/Edit Dialog */}
        <Dialog open={isCreateDialogOpen || isEditDialogOpen} onOpenChange={(open) => {
          if (!open) {
            setIsCreateDialogOpen(false)
            setIsEditDialogOpen(false)
            setEditingItem(null)
            setFormData({})
          }
        }}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>
                {editingItem ? 'Edit Board' : 'Create Board'}
              </DialogTitle>
            </DialogHeader>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <Label htmlFor="name">Name</Label>
                <Input
                  id="name"
                  value={formData.name || ''}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </div>
              <div className="flex items-center space-x-2">
                <Switch
                  id="active"
                  checked={formData.active || false}
                  onCheckedChange={(checked) => setFormData({ ...formData, active: checked })}
                />
                <Label htmlFor="active">Active</Label>
              </div>
              <div className="flex justify-end gap-2">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    setIsCreateDialogOpen(false)
                    setIsEditDialogOpen(false)
                    setEditingItem(null)
                    setFormData({})
                  }}
                >
                  Cancel
                </Button>
                <Button 
                  type="submit" 
                  disabled={createBoardMutation.isPending || updateBoardMutation.isPending}
                >
                  {(createBoardMutation.isPending || updateBoardMutation.isPending) ? 'Saving...' : 'Save'}
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>

        {/* Delete Confirmation Dialog */}
        <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Delete Board</DialogTitle>
            </DialogHeader>
            <div className="space-y-4">
              <p>Are you sure you want to delete "{deletingItem?.name}"?</p>
              
              {deletionImpact && (
                <div className="bg-yellow-50 border border-yellow-200 rounded p-3">
                  <h4 className="font-semibold text-yellow-800 mb-2">Impact Analysis:</h4>
                  <ul className="text-sm text-yellow-700 space-y-1">
                    <li>• Total items affected: {deletionImpact.totalImpact}</li>
                    {deletionImpact.gradesCount && deletionImpact.gradesCount > 0 && (
                      <li>• {deletionImpact.gradesCount} grades will be affected</li>
                    )}
                    {deletionImpact.subjectsCount && deletionImpact.subjectsCount > 0 && (
                      <li>• {deletionImpact.subjectsCount} subjects will be affected</li>
                    )}
                    {deletionImpact.chaptersCount && deletionImpact.chaptersCount > 0 && (
                      <li>• {deletionImpact.chaptersCount} chapters will be affected</li>
                    )}
                    {deletionImpact.topicsCount && deletionImpact.topicsCount > 0 && (
                      <li>• {deletionImpact.topicsCount} topics will be affected</li>
                    )}
                    {deletionImpact.warnings.length > 0 && (
                      <div className="mt-2">
                        <strong>Warnings:</strong>
                        <ul className="ml-4">
                          {deletionImpact.warnings.map((warning, index) => (
                            <li key={index}>• {warning}</li>
                          ))}
                        </ul>
                      </div>
                    )}
                  </ul>
                </div>
              )}
              
              <div className="flex justify-end gap-2">
                <Button variant="outline" onClick={() => setIsDeleteDialogOpen(false)}>
                  Cancel
                </Button>
                <Button 
                  variant="destructive" 
                  onClick={handleDelete}
                  disabled={deleteBoardMutation.isPending}
                >
                  {deleteBoardMutation.isPending ? 'Deleting...' : 'Delete'}
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </CardContent>
    </Card>
  )
}

// ========================= GRADES TAB =========================

function GradesTab(props: ExtendedTabProps) {
  const {
    searchTerm, setSearchTerm, statusFilter, setStatusFilter,
    selectedBoardFilter, setSelectedBoardFilter,
    currentPage, setCurrentPage, pageSize, setPageSize, onFilterChange,
    isCreateDialogOpen, setIsCreateDialogOpen,
    isEditDialogOpen, setIsEditDialogOpen,
    isDeleteDialogOpen, setIsDeleteDialogOpen,
    editingItem, setEditingItem, deletingItem, setDeletingItem,
    formData, setFormData
  } = props

  // Get page parameters
  const pageParams = {
    page: currentPage,
    size: pageSize,
    search: searchTerm || undefined,
    active: statusFilter === 'all' ? undefined : statusFilter === 'active'
  }

  // Get page parameters with board filter
  const pageParamsWithBoard = {
    ...pageParams,
    boardId: selectedBoardFilter || undefined
  }

  // React Query hooks
  const { data: gradesData, isLoading, error, refetch } = useGrades(pageParamsWithBoard)
  const { data: boardsDropdown } = useBoardsDropdown()
  const createGradeMutation = useCreateGrade()
  const updateGradeMutation = useUpdateGrade()
  const deleteGradeMutation = useDeleteGrade()

  const grades = gradesData?.content || []
  const totalPages = gradesData?.totalPages || 0
  const totalElements = gradesData?.totalElements || 0

  // Handle form submission for create/update
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    try {
      if (editingItem) {
        // Update
        await updateGradeMutation.mutateAsync({
          id: editingItem.id,
          ...formData
        })
        setIsEditDialogOpen(false)
        setEditingItem(null)
      } else {
        // Create
        await createGradeMutation.mutateAsync(formData)
        setIsCreateDialogOpen(false)
      }
      setFormData({})
      refetch()
    } catch (error: any) {
      console.error('Form submission error:', error)
    }
  }

  // Handle delete
  const handleDelete = async () => {
    if (!deletingItem) return
    
    try {
      await deleteGradeMutation.mutateAsync(deletingItem.id)
      setIsDeleteDialogOpen(false)
      setDeletingItem(null)
      refetch()
    } catch (error: any) {
      console.error('Delete error:', error)
    }
  }

  // Handle edit button click
  const handleEditClick = (grade: GradeDto) => {
    setEditingItem(grade)
    setFormData({
      name: grade.name,
      displayName: grade.displayName,
      boardId: grade.boardId,
      active: grade.active
    })
    setIsEditDialogOpen(true)
  }

  // Handle delete button click
  const handleDeleteClick = (grade: GradeDto) => {
    setDeletingItem(grade)
    setIsDeleteDialogOpen(true)
  }

  // Handle create button click
  const handleCreateClick = () => {
    setFormData({ 
      name: '', 
      displayName: '', 
      boardId: selectedBoardFilter || '', 
      active: true 
    })
    setIsCreateDialogOpen(true)
  }

  // Filter change handlers
  useEffect(() => {
    onFilterChange()
  }, [searchTerm, statusFilter, selectedBoardFilter])

  if (isLoading) {
    return <div className="flex justify-center items-center h-64">Loading grades...</div>
  }

  if (error) {
    return <div className="text-red-500 text-center">Error loading grades: {error.message}</div>
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex justify-between items-center">
          <CardTitle className="flex items-center gap-2">
            <GraduationCap className="w-5 h-5" />
            Grades ({totalElements})
          </CardTitle>
          <Button onClick={handleCreateClick}>
            <Plus className="w-4 h-4 mr-2" />
            Add Grade
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {/* Search and Filter Controls */}
        <div className="flex gap-4 mb-4">
          <div className="flex-1">
            <Input
              placeholder="Search grades..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full"
            />
          </div>
          <Select value={selectedBoardFilter?.toString() || 'all'} onValueChange={(value) => setSelectedBoardFilter?.(value === 'all' ? null : parseInt(value))}>
            <SelectTrigger className="w-[200px]">
              <SelectValue placeholder="Filter by Board" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Boards</SelectItem>
              {boardsDropdown?.map((board) => (
                <SelectItem key={board.id} value={board.id.toString()}>{board.name}</SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-[180px]">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Status</SelectItem>
              <SelectItem value="active">Active Only</SelectItem>
              <SelectItem value="inactive">Inactive Only</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Grades Table */}
        <div className="space-y-4">
          {grades.map((grade) => (
            <Card key={grade.id} className="p-4">
              <div className="flex justify-between items-center">
                <div>
                  <h3 className="font-semibold">{grade.displayName} ({grade.name})</h3>
                  <div className="flex items-center gap-4 mt-1">
                    <Badge variant={grade.active ? 'default' : 'secondary'}>
                      {grade.active ? 'Active' : 'Inactive'}
                    </Badge>
                    <span className="text-sm text-gray-500">
                      Board ID: {grade.boardId}
                    </span>
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button variant="outline" size="sm" onClick={() => handleEditClick(grade)}>
                    <Edit className="w-4 h-4" />
                  </Button>
                  <Button variant="outline" size="sm" onClick={() => handleDeleteClick(grade)}>
                    <Trash2 className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            </Card>
          ))}

          {grades.length === 0 && (
            <div className="text-center py-8 text-gray-500">
              No grades found
            </div>
          )}
        </div>

        {/* Pagination */}
        <PaginationControls
          currentPage={currentPage}
          totalPages={totalPages}
          pageSize={pageSize}
          totalElements={totalElements}
          onPageChange={setCurrentPage}
          onPageSizeChange={setPageSize}
        />

        {/* Create/Edit Dialog */}
        <Dialog open={isCreateDialogOpen || isEditDialogOpen} onOpenChange={(open) => {
          if (!open) {
            setIsCreateDialogOpen(false)
            setIsEditDialogOpen(false)
            setEditingItem(null)
            setFormData({})
          }
        }}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>
                {editingItem ? 'Edit Grade' : 'Create Grade'}
              </DialogTitle>
            </DialogHeader>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <Label htmlFor="name">Name</Label>
                <Input
                  id="name"
                  value={formData.name || ''}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </div>
              <div>
                <Label htmlFor="displayName">Display Name</Label>
                <Input
                  id="displayName"
                  value={formData.displayName || ''}
                  onChange={(e) => setFormData({ ...formData, displayName: e.target.value })}
                  required
                />
              </div>
              <div>
                <Label htmlFor="boardId">Board</Label>
                <Select 
                  value={formData.boardId?.toString() || ''} 
                  onValueChange={(value) => setFormData({ ...formData, boardId: parseInt(value) })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select Board" />
                  </SelectTrigger>
                  <SelectContent>
                    {boardsDropdown?.map((board) => (
                      <SelectItem key={board.id} value={board.id.toString()}>{board.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="flex items-center space-x-2">
                <Switch
                  id="active"
                  checked={formData.active || false}
                  onCheckedChange={(checked) => setFormData({ ...formData, active: checked })}
                />
                <Label htmlFor="active">Active</Label>
              </div>
              <div className="flex justify-end gap-2">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    setIsCreateDialogOpen(false)
                    setIsEditDialogOpen(false)
                    setEditingItem(null)
                    setFormData({})
                  }}
                >
                  Cancel
                </Button>
                <Button 
                  type="submit" 
                  disabled={createGradeMutation.isPending || updateGradeMutation.isPending}
                >
                  {(createGradeMutation.isPending || updateGradeMutation.isPending) ? 'Saving...' : 'Save'}
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>

        {/* Delete Confirmation Dialog */}
        <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Delete Grade</DialogTitle>
            </DialogHeader>
            <div className="space-y-4">
              <p>Are you sure you want to delete "{deletingItem?.displayName}"?</p>
              
              <div className="bg-yellow-50 border border-yellow-200 rounded p-3">
                <h4 className="font-semibold text-yellow-800 mb-2">Warning:</h4>
                <p className="text-sm text-yellow-700">
                  This will also delete all subjects, chapters, topics, and notes associated with this grade.
                </p>
              </div>
              
              <div className="flex justify-end gap-2">
                <Button variant="outline" onClick={() => setIsDeleteDialogOpen(false)}>
                  Cancel
                </Button>
                <Button 
                  variant="destructive" 
                  onClick={handleDelete}
                  disabled={deleteGradeMutation.isPending}
                >
                  {deleteGradeMutation.isPending ? 'Deleting...' : 'Delete'}
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </CardContent>
    </Card>
  )
}

// ========================= PLACEHOLDER COMPONENTS =========================
// These would be implemented similarly to BoardsTab and GradesTab

function SubjectsTab(props: ExtendedTabProps) {
  return (
    <Card>
      <CardContent>
        <div className="text-center py-8 text-gray-500">
          Subjects tab implementation coming soon...
        </div>
      </CardContent>
    </Card>
  )
}

function ChaptersTab(props: ExtendedTabProps) {
  return (
    <Card>
      <CardContent>
        <div className="text-center py-8 text-gray-500">
          Chapters tab implementation coming soon...
        </div>
      </CardContent>
    </Card>
  )
}

function TopicsTab(props: ExtendedTabProps) {
  return (
    <Card>
      <CardContent>
        <div className="text-center py-8 text-gray-500">
          Topics tab implementation coming soon...
        </div>
      </CardContent>
    </Card>
  )
}

function TopicNotesTab(props: ExtendedTabProps) {
  return (
    <Card>
      <CardContent>
        <div className="text-center py-8 text-gray-500">
          Topic Notes tab implementation coming soon...
        </div>
      </CardContent>
    </Card>
  )
}

// ========================= CONTENT TREE DIALOG =========================

interface ContentTreeDialogProps {
  isOpen: boolean
  setIsOpen: (open: boolean) => void
  selectedBoardId: number | null
  setSelectedBoardId: (boardId: number | null) => void
}

function ContentTreeDialog({ isOpen, setIsOpen, selectedBoardId, setSelectedBoardId }: ContentTreeDialogProps) {
  const { data: boardsDropdown } = useBoardsDropdown()

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Content Tree View</DialogTitle>
        </DialogHeader>
        
        <div className="space-y-4">
          {/* Board Selection */}
          <div>
            <Label htmlFor="board-select">Select Board</Label>
            <Select
              value={selectedBoardId?.toString() || ''}
              onValueChange={(value) => setSelectedBoardId(value ? parseInt(value) : null)}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select a board to view its content tree" />
              </SelectTrigger>
              <SelectContent>
                {boardsDropdown?.map((board) => (
                  <SelectItem key={board.id} value={board.id.toString()}>{board.name}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Content Tree */}
          <div className="text-center py-8 text-gray-500">
            Content tree visualization will be implemented soon...
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}


