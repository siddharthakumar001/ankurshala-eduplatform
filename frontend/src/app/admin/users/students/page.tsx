'use client'

import { useState, useEffect, useCallback, useRef } from 'react'
import DashboardLayout from '@/components/layout/DashboardLayout'
import AuthGuard from '@/components/AuthGuard'
import SessionManager from '@/components/SessionManager'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Badge } from '@/components/ui/badge'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { 
  GraduationCap, 
  Search, 
  Filter, 
  Plus, 
  Edit, 
  Eye, 
  UserCheck, 
  UserX,
  Trash2,
  ChevronLeft,
  ChevronRight,
  Download,
  AlertCircle,
  Loader2,
  RefreshCw
} from 'lucide-react'
import { toast } from 'sonner'
import { api } from '@/utils/api'

interface Student {
  id: number
  userId: number
  firstName: string
  middleName?: string
  lastName: string
  email: string
  mobileNumber?: string
  dateOfBirth?: string
  educationalBoard?: string
  classLevel?: string
  gradeLevel?: string
  schoolName?: string
  enabled: boolean
  createdAt: string
  lastLoginAt?: string
}

interface StudentDetail extends Student {
  alternateMobileNumber?: string
  motherName?: string
  fatherName?: string
  guardianName?: string
  parentName?: string
  emergencyContact?: string
  studentPhotoUrl?: string
  schoolIdCardUrl?: string
  updatedAt: string
}

const EDUCATIONAL_BOARDS = [
  { value: 'CBSE', label: 'CBSE' },
  { value: 'ICSE', label: 'ICSE' },
  { value: 'STATE_BOARD', label: 'State Board' },
  { value: 'IB', label: 'International Baccalaureate' },
  { value: 'CAMBRIDGE', label: 'Cambridge' },
  { value: 'OTHER', label: 'Other' }
]

const CLASS_LEVELS = [
  { value: 'GRADE_1', label: 'Grade 1', boards: ['STATE_BOARD', 'OTHER'] },
  { value: 'GRADE_2', label: 'Grade 2', boards: ['STATE_BOARD', 'OTHER'] },
  { value: 'GRADE_3', label: 'Grade 3', boards: ['STATE_BOARD', 'OTHER'] },
  { value: 'GRADE_4', label: 'Grade 4', boards: ['STATE_BOARD', 'OTHER'] },
  { value: 'GRADE_5', label: 'Grade 5', boards: ['STATE_BOARD', 'OTHER'] },
  { value: 'GRADE_6', label: 'Grade 6', boards: ['STATE_BOARD', 'OTHER'] },
  { value: 'GRADE_7', label: 'Grade 7', boards: ['CBSE', 'ICSE', 'STATE_BOARD', 'IB', 'CAMBRIDGE', 'OTHER'] },
  { value: 'GRADE_8', label: 'Grade 8', boards: ['CBSE', 'ICSE', 'STATE_BOARD', 'IB', 'CAMBRIDGE', 'OTHER'] },
  { value: 'GRADE_9', label: 'Grade 9', boards: ['CBSE', 'ICSE', 'STATE_BOARD', 'IB', 'CAMBRIDGE', 'OTHER'] },
  { value: 'GRADE_10', label: 'Grade 10', boards: ['CBSE', 'ICSE', 'STATE_BOARD', 'IB', 'CAMBRIDGE', 'OTHER'] },
  { value: 'GRADE_11', label: 'Grade 11', boards: ['CBSE', 'ICSE', 'STATE_BOARD', 'IB', 'CAMBRIDGE', 'OTHER'] },
  { value: 'GRADE_12', label: 'Grade 12', boards: ['CBSE', 'ICSE', 'STATE_BOARD', 'IB', 'CAMBRIDGE', 'OTHER'] },
  { value: 'UNDERGRADUATE', label: 'Undergraduate', boards: ['OTHER'] },
  { value: 'POSTGRADUATE', label: 'Postgraduate', boards: ['OTHER'] },
  { value: 'DOCTORATE', label: 'Doctorate', boards: ['OTHER'] },
  { value: 'OTHER', label: 'Other', boards: ['CBSE', 'ICSE', 'STATE_BOARD', 'IB', 'CAMBRIDGE', 'OTHER'] }
]

// Helper function to get available classes based on selected board
const getAvailableClasses = (board: string) => {
  if (!board || board === 'all') {
    return CLASS_LEVELS
  }
  return CLASS_LEVELS.filter(level => level.boards.includes(board))
}

export default function AdminStudentsPage() {
  const [students, setStudents] = useState<Student[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('all')
  const [boardFilter, setBoardFilter] = useState<string>('all')
  const [classFilter, setClassFilter] = useState<string>('all')
  const [sortBy, setSortBy] = useState('createdAt')
  const [sortDir, setSortDir] = useState('desc')
  const [selectedStudent, setSelectedStudent] = useState<StudentDetail | null>(null)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [isViewDialogOpen, setIsViewDialogOpen] = useState(false)
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false)
  const [studentToDelete, setStudentToDelete] = useState<Student | null>(null)
  const [actionLoading, setActionLoading] = useState<number | null>(null)

  const pageSize = 10

  const filtersRef = useRef({
    search,
    statusFilter,
    boardFilter,
    classFilter
  })
  const currentPageRef = useRef(currentPage)
  const loadStateRef = useRef({ loading, studentsLength: students.length })

  useEffect(() => {
    filtersRef.current = { search, statusFilter, boardFilter, classFilter }
  }, [search, statusFilter, boardFilter, classFilter])

  useEffect(() => {
    currentPageRef.current = currentPage
  }, [currentPage])

  useEffect(() => {
    loadStateRef.current = { loading, studentsLength: students.length }
  }, [loading, students.length])

  const fetchStudents = useCallback(async () => {
    // Allow initial load but prevent re-entry
    const { loading: isLoading, studentsLength } = loadStateRef.current
    const isInitialLoad = studentsLength === 0
    if (isLoading && !isInitialLoad) return
    
    setLoading(true)
    setError(null)
    
    try {
      const params = new URLSearchParams({
        page: currentPage.toString(),
        size: pageSize.toString(),
        sortBy,
        sortDir
      })

      const { search, statusFilter, boardFilter, classFilter } = filtersRef.current
      if (search) params.append('search', search)
      if (statusFilter && statusFilter !== 'all') params.append('enabled', statusFilter)
      if (boardFilter && boardFilter !== 'all') params.append('educationalBoard', boardFilter)
      if (classFilter && classFilter !== 'all') params.append('classLevel', classFilter)

      console.log('Fetching students with params:', params.toString())
      const response = await api.get(`/admin/students?${params}`)

      // Our API client unwraps standardized ApiResponse and returns the inner data directly
      // So response.data is the paginated Page object with content/totalPages/totalElements
      const paginatedData = response.data as any
      console.log('Students Page Data:', paginatedData)

      if (paginatedData && paginatedData.content) {
        const studentsData = paginatedData.content || []
        setStudents(studentsData)
        setTotalPages(paginatedData.totalPages || 0)
        setTotalElements(paginatedData.totalElements || 0)
        console.log('Students loaded:', studentsData.length, 'Total:', paginatedData.totalElements)
      } else {
        const errorMsg = 'Invalid response format'
        setError(errorMsg)
        toast.error(errorMsg)
        console.error('Unexpected response structure:', paginatedData)
      }
    } catch (err) {
      console.error('Error fetching students:', err)
      const errorMessage = err instanceof Error ? err.message : 'Failed to fetch students'
      setError(errorMessage)
      toast.error(errorMessage)
    } finally {
      setLoading(false)
    }
  }, [currentPage, pageSize, sortBy, sortDir])

  // Fetch students on mount and when pagination/sort changes
  useEffect(() => {
    fetchStudents()
  }, [fetchStudents])

  // Debounced search effect - fetch when search or filters change
  useEffect(() => {
    const timeoutId = setTimeout(() => {
      // Reset to first page when searching/filtering
      if (currentPageRef.current !== 0) {
        setCurrentPage(0)
      } else {
        fetchStudents()
      }
    }, 500) // 500ms debounce

    return () => clearTimeout(timeoutId)
  }, [search, statusFilter, boardFilter, classFilter, fetchStudents])

  const fetchStudentDetail = async (id: number) => {
    try {
      const response = await api.get(`/admin/students/${id}`)
      const studentData = response.data as any
      
      if (studentData) {
        setSelectedStudent(studentData)
        return studentData
      } else {
        toast.error('Failed to fetch student details')
      }
    } catch (error) {
      console.error('Error fetching student details:', error)
      toast.error('Failed to fetch student details')
    }
  }

  const toggleStudentStatus = async (id: number) => {
    setActionLoading(id)
    try {
      const response = await api.patch(`/admin/students/${id}/toggle-status`)
      // API client extracts data automatically on success
      toast.success('Student status updated successfully')
      fetchStudents() // Refresh the list
    } catch (error) {
      console.error('Error updating student status:', error)
      const errorMessage = error instanceof Error ? error.message : 'Failed to update student status'
      toast.error(errorMessage)
    } finally {
      setActionLoading(null)
    }
  }

  const updateStudent = async (id: number, updateData: Partial<StudentDetail>) => {
    try {
      const response = await api.put(`/admin/students/${id}`, updateData)
      const updatedStudent = response.data as any
      
      if (updatedStudent) {
        toast.success('Student updated successfully')
        setIsEditDialogOpen(false)
        fetchStudents() // Refresh the list
        return updatedStudent
      } else {
        toast.error('Failed to update student')
      }
    } catch (error) {
      console.error('Error updating student:', error)
      const errorMessage = error instanceof Error ? error.message : 'Failed to update student'
      toast.error(errorMessage)
    }
  }

  const createStudent = async (studentData: Partial<StudentDetail>) => {
    try {
      const response = await api.post(`/admin/students`, studentData)
      const newStudent = response.data as any
      
      if (newStudent) {
        toast.success('Student created successfully')
        setIsCreateDialogOpen(false)
        fetchStudents() // Refresh the list
        return newStudent
      } else {
        toast.error('Failed to create student')
      }
    } catch (error) {
      console.error('Error creating student:', error)
      const errorMessage = error instanceof Error ? error.message : 'Failed to create student'
      toast.error(errorMessage)
    }
  }

  const deleteStudent = async (id: number) => {
    setActionLoading(id)
    try {
      await api.delete(`/admin/students/${id}`)
      toast.success('Student deleted successfully')
      setIsDeleteDialogOpen(false)
      setStudentToDelete(null)
      fetchStudents() // Refresh the list
    } catch (error) {
      console.error('Error deleting student:', error)
      const errorMessage = error instanceof Error ? error.message : 'Failed to delete student'
      toast.error(errorMessage)
    } finally {
      setActionLoading(null)
    }
  }

  const handleViewStudent = async (id: number) => {
    await fetchStudentDetail(id)
    setIsViewDialogOpen(true)
  }

  const handleEditStudent = async (id: number) => {
    await fetchStudentDetail(id)
    setIsEditDialogOpen(true)
  }

  const handleDeleteStudent = (student: Student) => {
    setStudentToDelete(student)
    setIsDeleteDialogOpen(true)
  }

  const handleCreateStudent = () => {
    setIsCreateDialogOpen(true)
  }

  const handleSearchChange = (value: string) => {
    setSearch(value)
    setCurrentPage(0)
  }

  const handleBoardFilterChange = (value: string) => {
    setBoardFilter(value)
    // Reset class filter when board changes if the current class is not available for the new board
    if (value !== 'all' && classFilter !== 'all') {
      const availableClasses = getAvailableClasses(value)
      const isClassAvailable = availableClasses.some(c => c.value === classFilter)
      if (!isClassAvailable) {
        setClassFilter('all')
      }
    }
    setCurrentPage(0)
  }

  const clearFilters = () => {
    setSearch('')
    setStatusFilter('all')
    setBoardFilter('all')
    setClassFilter('all')
    setCurrentPage(0)
  }

  const formatDisplayName = (student: Student) => {
    const parts = [student.firstName, student.middleName, student.lastName].filter(Boolean)
    return parts.join(' ')
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }

  if (loading && students.length === 0) {
    return (
      <AuthGuard requiredRoles={['ADMIN']}>
        <SessionManager showSessionInfo={true}>
      <DashboardLayout role="admin">
        <div className="space-y-6 relative">
          <div className="pointer-events-none absolute inset-0 -z-10">
            <div className="absolute -top-20 right-4 h-72 w-72 rounded-full bg-ankur-primary/10 blur-3xl" />
            <div className="absolute bottom-4 left-6 h-64 w-64 rounded-full bg-ankur-accent/10 blur-3xl" />
          </div>
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                <div>
                  <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Manage Students</h1>
                  <p className="text-gray-600 dark:text-gray-400">View and manage student accounts</p>
                </div>
              </div>
              <Card className="p-6 glass">
                <div className="animate-pulse space-y-4">
                  {[...Array(5)].map((_, i) => (
                    <div key={i} className="h-16 bg-gray-200 dark:bg-gray-700 rounded"></div>
                  ))}
                </div>
              </Card>
            </div>
          </DashboardLayout>
        </SessionManager>
      </AuthGuard>
    )
  }

  return (
    <AuthGuard requiredRoles={['ADMIN']}>
      <SessionManager showSessionInfo={true}>
        <DashboardLayout role="admin">
          <div className="space-y-6 relative">
            <div className="pointer-events-none absolute inset-0 -z-10">
              <div className="absolute -top-20 right-4 h-72 w-72 rounded-full bg-ankur-primary/10 blur-3xl" />
              <div className="absolute bottom-4 left-6 h-64 w-64 rounded-full bg-ankur-accent/10 blur-3xl" />
            </div>
            {/* Header */}
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
              <div>
                <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Manage Students</h1>
                <p className="text-gray-600 dark:text-gray-400">View and manage student accounts</p>
              </div>
              <div className="flex flex-wrap gap-2">
                <Button 
                  variant="outline" 
                  className="flex items-center space-x-2"
                  onClick={fetchStudents}
                  disabled={loading}
                >
                  <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
                  <span>Refresh</span>
                </Button>
                <Button variant="outline" className="flex items-center space-x-2">
                  <Download className="h-4 w-4" />
                  <span>Export</span>
                </Button>
                {/* Note: Student creation is done through user registration, not admin panel */}
                {/* <Button 
                  className="flex items-center space-x-2"
                  onClick={handleCreateStudent}
                >
                  <Plus className="h-4 w-4" />
                  <span>Add Student</span>
                </Button> */}
              </div>
            </div>

            {/* Error Display */}
            {error && (
              <Card className="p-6 border-red-200 bg-red-50 dark:bg-red-900/20">
                <div className="flex items-center space-x-2 text-red-600 dark:text-red-400">
                  <AlertCircle className="h-5 w-5" />
                  <span className="font-medium">Error loading students</span>
                </div>
                <p className="text-red-600 dark:text-red-400 mt-2">{error}</p>
                <Button 
                  onClick={fetchStudents} 
                  variant="outline" 
                  size="sm" 
                  className="mt-4"
                >
                  Try Again
                </Button>
              </Card>
            )}

        {/* Search and Filters */}
        <Card className="p-6 glass">
          <div className="space-y-4">
            <div className="flex flex-col sm:flex-row gap-4">
              <div className="flex-1">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                  <Input
                    type="text"
                    placeholder="Search by name, email, mobile, or school..."
                    value={search}
                    onChange={(e) => handleSearchChange(e.target.value)}
                    className="pl-10"
                  />
                </div>
              </div>
              <div className="flex flex-wrap gap-2">
                <Select value={statusFilter} onValueChange={setStatusFilter}>
                  <SelectTrigger className="w-32" data-testid="status-select">
                    <SelectValue placeholder="Status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Status</SelectItem>
                    <SelectItem value="true">Active</SelectItem>
                    <SelectItem value="false">Inactive</SelectItem>
                  </SelectContent>
                </Select>
                <Select value={boardFilter} onValueChange={handleBoardFilterChange}>
                  <SelectTrigger className="w-40">
                    <SelectValue placeholder="Board" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Boards</SelectItem>
                    {EDUCATIONAL_BOARDS.map(board => (
                      <SelectItem key={board.value} value={board.value}>{board.label}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Select value={classFilter} onValueChange={setClassFilter}>
                  <SelectTrigger className="w-32">
                    <SelectValue placeholder="Class" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Classes</SelectItem>
                    {getAvailableClasses(boardFilter).map(level => (
                      <SelectItem key={level.value} value={level.value}>{level.label}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {(search || (statusFilter && statusFilter !== 'all') || (boardFilter && boardFilter !== 'all') || (classFilter && classFilter !== 'all')) && (
                  <Button variant="outline" onClick={clearFilters}>
                    Clear
                  </Button>
                )}
              </div>
            </div>
          </div>
        </Card>

        {/* Students Table */}
        <Card className="p-6 glass">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Students</h3>
              <div className="text-sm text-gray-500 dark:text-gray-400">
                Showing {currentPage * pageSize + 1}-{Math.min((currentPage + 1) * pageSize, totalElements)} of {totalElements} students
              </div>
            </div>
            
            {students.length === 0 ? (
              <div className="text-center py-12">
                <GraduationCap className="mx-auto h-12 w-12 text-gray-400" />
                <h3 className="mt-2 text-sm font-medium text-gray-900 dark:text-white">No students found</h3>
                <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                  {(search || (statusFilter && statusFilter !== 'all') || (boardFilter && boardFilter !== 'all') || (classFilter && classFilter !== 'all'))
                    ? 'Try adjusting your search criteria or filters.'
                    : 'Get started by adding a new student.'}
                </p>
              </div>
            ) : (
              <>
                <div className="overflow-hidden border border-gray-200 dark:border-gray-700 rounded-lg">
                  <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                    <thead className="bg-gray-50 dark:bg-gray-800">
                      <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Student
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          School
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Class/Board
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Status
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Joined
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Actions
                        </th>
                      </tr>
                    </thead>
                    <tbody className="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
                      {students.map((student) => (
                        <tr key={student.id} className="hover:bg-gray-50 dark:hover:bg-gray-800">
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="flex items-center">
                              <div className="h-10 w-10 bg-blue-100 dark:bg-blue-900 rounded-full flex items-center justify-center">
                                <GraduationCap className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                              </div>
                              <div className="ml-4">
                                <div className="text-sm font-medium text-gray-900 dark:text-white">
                                  {formatDisplayName(student)}
                                </div>
                                <div className="text-sm text-gray-500 dark:text-gray-400">
                                  {student.email}
                                </div>
                                {student.mobileNumber && (
                                  <div className="text-xs text-gray-400">
                                    {student.mobileNumber}
                                  </div>
                                )}
                              </div>
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                            {student.schoolName || 'Not specified'}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="text-sm text-gray-900 dark:text-white">
                              {student.classLevel ? CLASS_LEVELS.find(c => c.value === student.classLevel)?.label : 'Not specified'}
                            </div>
                            <div className="text-xs text-gray-500 dark:text-gray-400">
                              {student.educationalBoard ? EDUCATIONAL_BOARDS.find(b => b.value === student.educationalBoard)?.label : 'Not specified'}
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <Badge variant={student.enabled ? "default" : "secondary"}>
                              {student.enabled ? 'Active' : 'Inactive'}
                            </Badge>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                            {formatDate(student.createdAt)}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                            <div className="flex space-x-2">
                              <Button 
                                variant="ghost" 
                                size="sm"
                                onClick={() => handleViewStudent(student.id)}
                                disabled={actionLoading === student.id}
                              >
                                <Eye className="h-4 w-4" />
                              </Button>
                              <Button 
                                variant="ghost" 
                                size="sm"
                                onClick={() => handleEditStudent(student.id)}
                                disabled={actionLoading === student.id}
                              >
                                <Edit className="h-4 w-4" />
                              </Button>
                              <Button 
                                variant="ghost" 
                                size="sm"
                                onClick={() => toggleStudentStatus(student.id)}
                                disabled={actionLoading === student.id}
                                className={student.enabled ? "text-red-600 hover:text-red-700" : "text-green-600 hover:text-green-700"}
                                title={student.enabled ? "Disable student" : "Enable student"}
                              >
                                {actionLoading === student.id ? (
                                  <Loader2 className="h-4 w-4 animate-spin" />
                                ) : student.enabled ? (
                                  <UserX className="h-4 w-4" />
                                ) : (
                                  <UserCheck className="h-4 w-4" />
                                )}
                              </Button>
                              <Button 
                                variant="ghost" 
                                size="sm"
                                onClick={() => handleDeleteStudent(student)}
                                disabled={actionLoading === student.id}
                                className="text-red-600 hover:text-red-700"
                                title="Delete student"
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

                {/* Pagination */}
                <div className="flex items-center justify-between">
                  <div className="text-sm text-gray-500 dark:text-gray-400">
                    Page {currentPage + 1} of {totalPages}
                  </div>
                  <div className="flex space-x-2">
                    <Button 
                      variant="outline" 
                      size="sm" 
                      disabled={currentPage === 0}
                      onClick={() => setCurrentPage(currentPage - 1)}
                    >
                      <ChevronLeft className="h-4 w-4" />
                      Previous
                    </Button>
                    <Button 
                      variant="outline" 
                      size="sm" 
                      disabled={currentPage >= totalPages - 1}
                      onClick={() => setCurrentPage(currentPage + 1)}
                    >
                      Next
                      <ChevronRight className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </>
            )}
          </div>
        </Card>

        {/* View Student Dialog */}
        <Dialog open={isViewDialogOpen} onOpenChange={setIsViewDialogOpen}>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>Student Details</DialogTitle>
            </DialogHeader>
            {selectedStudent && (
              <div className="space-y-6">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label className="text-sm font-medium">Full Name</Label>
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                      {formatDisplayName(selectedStudent)}
                    </p>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">Email</Label>
                    <p className="text-sm text-gray-600 dark:text-gray-400">{selectedStudent.email}</p>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">Mobile Number</Label>
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                      {selectedStudent.mobileNumber || 'Not provided'}
                    </p>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">Date of Birth</Label>
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                      {selectedStudent.dateOfBirth ? formatDate(selectedStudent.dateOfBirth) : 'Not provided'}
                    </p>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">School Name</Label>
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                      {selectedStudent.schoolName || 'Not provided'}
                    </p>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">Class Level</Label>
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                      {selectedStudent.classLevel ? CLASS_LEVELS.find(c => c.value === selectedStudent.classLevel)?.label : 'Not provided'}
                    </p>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">Educational Board</Label>
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                      {selectedStudent.educationalBoard ? EDUCATIONAL_BOARDS.find(b => b.value === selectedStudent.educationalBoard)?.label : 'Not provided'}
                    </p>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">Status</Label>
                    <div>
                      <Badge variant={selectedStudent.enabled ? "default" : "secondary"}>
                        {selectedStudent.enabled ? 'Active' : 'Inactive'}
                      </Badge>
                    </div>
                  </div>
                </div>
                
                {(selectedStudent.motherName || selectedStudent.fatherName || selectedStudent.guardianName) && (
                  <div>
                    <h4 className="font-medium mb-2">Family Information</h4>
                    <div className="grid grid-cols-2 gap-4">
                      {selectedStudent.motherName && (
                        <div>
                          <Label className="text-sm font-medium">Mother&apos;s Name</Label>
                          <p className="text-sm text-gray-600 dark:text-gray-400">{selectedStudent.motherName}</p>
                        </div>
                      )}
                      {selectedStudent.fatherName && (
                        <div>
                          <Label className="text-sm font-medium">Father&apos;s Name</Label>
                          <p className="text-sm text-gray-600 dark:text-gray-400">{selectedStudent.fatherName}</p>
                        </div>
                      )}
                      {selectedStudent.guardianName && (
                        <div>
                          <Label className="text-sm font-medium">Guardian&apos;s Name</Label>
                          <p className="text-sm text-gray-600 dark:text-gray-400">{selectedStudent.guardianName}</p>
                        </div>
                      )}
                      {selectedStudent.emergencyContact && (
                        <div>
                          <Label className="text-sm font-medium">Emergency Contact</Label>
                          <p className="text-sm text-gray-600 dark:text-gray-400">{selectedStudent.emergencyContact}</p>
                        </div>
                      )}
                    </div>
                  </div>
                )}
                
                <div className="grid grid-cols-2 gap-4 text-xs text-gray-500">
                  <div>
                    <Label className="text-sm font-medium">Created</Label>
                    <p>{formatDate(selectedStudent.createdAt)}</p>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">Last Updated</Label>
                    <p>{formatDate(selectedStudent.updatedAt)}</p>
                  </div>
                </div>
              </div>
            )}
          </DialogContent>
        </Dialog>

        {/* Edit Student Dialog */}
        <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
          <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Edit Student</DialogTitle>
            </DialogHeader>
            {selectedStudent && (
              <StudentEditForm 
                student={selectedStudent} 
                onSave={updateStudent}
                onCancel={() => setIsEditDialogOpen(false)}
              />
            )}
          </DialogContent>
        </Dialog>

        {/* Create Student Dialog */}
        <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
          <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Add New Student</DialogTitle>
            </DialogHeader>
            <StudentCreateForm 
              onSave={createStudent}
              onCancel={() => setIsCreateDialogOpen(false)}
            />
          </DialogContent>
        </Dialog>

        {/* Delete Confirmation Dialog */}
        <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Delete Student</DialogTitle>
            </DialogHeader>
            {studentToDelete && (
              <div className="space-y-4">
                <p className="text-sm text-gray-600 dark:text-gray-400">
                  Are you sure you want to delete the student <strong>{formatDisplayName(studentToDelete)}</strong>?
                </p>
                <p className="text-sm text-red-600 dark:text-red-400">
                  This action cannot be undone.
                </p>
                <div className="flex justify-end space-x-2">
                  <Button 
                    type="button" 
                    variant="outline" 
                    onClick={() => {
                      setIsDeleteDialogOpen(false)
                      setStudentToDelete(null)
                    }}
                    disabled={actionLoading === studentToDelete.id}
                  >
                    Cancel
                  </Button>
                  <Button 
                    type="button" 
                    variant="destructive"
                    onClick={() => deleteStudent(studentToDelete.id)}
                    disabled={actionLoading === studentToDelete.id}
                  >
                    {actionLoading === studentToDelete.id ? (
                      <>
                        <Loader2 className="h-4 w-4 animate-spin mr-2" />
                        Deleting...
                      </>
                    ) : (
                      'Delete Student'
                    )}
                  </Button>
                </div>
              </div>
            )}
          </DialogContent>
        </Dialog>
      </div>
    </DashboardLayout>
  </SessionManager>
</AuthGuard>
)
}

// Student Create Form Component
function StudentCreateForm({ 
  onSave, 
  onCancel 
}: { 
  onSave: (data: Partial<StudentDetail>) => void
  onCancel: () => void 
}) {
  const [formData, setFormData] = useState({
    firstName: '',
    middleName: '',
    lastName: '',
    email: '',
    password: '',
    mobileNumber: '',
    alternateMobileNumber: '',
    dateOfBirth: '',
    motherName: '',
    fatherName: '',
    guardianName: '',
    parentName: '',
    educationalBoard: '',
    classLevel: '',
    gradeLevel: '',
    schoolName: '',
    emergencyContact: '',
    enabled: true
  })

  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsSubmitting(true)
    try {
      await onSave(formData)
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleChange = (field: string, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div>
          <Label htmlFor="create-firstName">First Name *</Label>
          <Input
            id="create-firstName"
            value={formData.firstName}
            onChange={(e) => handleChange('firstName', e.target.value)}
            required
          />
        </div>
        <div>
          <Label htmlFor="create-lastName">Last Name *</Label>
          <Input
            id="create-lastName"
            value={formData.lastName}
            onChange={(e) => handleChange('lastName', e.target.value)}
            required
          />
        </div>
        <div>
          <Label htmlFor="create-middleName">Middle Name</Label>
          <Input
            id="create-middleName"
            value={formData.middleName}
            onChange={(e) => handleChange('middleName', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="create-email">Email *</Label>
          <Input
            id="create-email"
            type="email"
            value={formData.email}
            onChange={(e) => handleChange('email', e.target.value)}
            required
          />
        </div>
        <div>
          <Label htmlFor="create-password">Password *</Label>
          <Input
            id="create-password"
            type="password"
            value={formData.password}
            onChange={(e) => handleChange('password', e.target.value)}
            required
            minLength={8}
          />
        </div>
        <div>
          <Label htmlFor="create-mobileNumber">Mobile Number</Label>
          <Input
            id="create-mobileNumber"
            value={formData.mobileNumber}
            onChange={(e) => handleChange('mobileNumber', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="create-dateOfBirth">Date of Birth</Label>
          <Input
            id="create-dateOfBirth"
            type="date"
            value={formData.dateOfBirth}
            onChange={(e) => handleChange('dateOfBirth', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="create-schoolName">School Name</Label>
          <Input
            id="create-schoolName"
            value={formData.schoolName}
            onChange={(e) => handleChange('schoolName', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="create-educationalBoard">Educational Board</Label>
          <Select value={formData.educationalBoard} onValueChange={(value) => handleChange('educationalBoard', value)}>
            <SelectTrigger>
              <SelectValue placeholder="Select board" />
            </SelectTrigger>
            <SelectContent>
              {EDUCATIONAL_BOARDS.map(board => (
                <SelectItem key={board.value} value={board.value}>{board.label}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div>
          <Label htmlFor="create-classLevel">Class Level</Label>
          <Select value={formData.classLevel} onValueChange={(value) => handleChange('classLevel', value)}>
            <SelectTrigger>
              <SelectValue placeholder="Select class" />
            </SelectTrigger>
            <SelectContent>
              {CLASS_LEVELS.map(level => (
                <SelectItem key={level.value} value={level.value}>{level.label}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div>
          <Label htmlFor="create-motherName">Mother&apos;s Name</Label>
          <Input
            id="create-motherName"
            value={formData.motherName}
            onChange={(e) => handleChange('motherName', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="create-fatherName">Father&apos;s Name</Label>
          <Input
            id="create-fatherName"
            value={formData.fatherName}
            onChange={(e) => handleChange('fatherName', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="create-emergencyContact">Emergency Contact</Label>
          <Input
            id="create-emergencyContact"
            value={formData.emergencyContact}
            onChange={(e) => handleChange('emergencyContact', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="create-enabled">Status</Label>
          <Select value={formData.enabled.toString()} onValueChange={(value) => handleChange('enabled', value === 'true')}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="true">Active</SelectItem>
              <SelectItem value="false">Inactive</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>
      
      <div className="flex justify-end space-x-2 pt-4">
        <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
          Cancel
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin mr-2" />
              Creating...
            </>
          ) : (
            'Create Student'
          )}
        </Button>
      </div>
    </form>
  )
}

// Student Edit Form Component
function StudentEditForm({ 
  student, 
  onSave, 
  onCancel 
}: { 
  student: StudentDetail
  onSave: (id: number, data: Partial<StudentDetail>) => void
  onCancel: () => void 
}) {
  const [formData, setFormData] = useState({
    firstName: student.firstName || '',
    middleName: student.middleName || '',
    lastName: student.lastName || '',
    mobileNumber: student.mobileNumber || '',
    alternateMobileNumber: student.alternateMobileNumber || '',
    dateOfBirth: student.dateOfBirth || '',
    motherName: student.motherName || '',
    fatherName: student.fatherName || '',
    guardianName: student.guardianName || '',
    parentName: student.parentName || '',
    educationalBoard: student.educationalBoard || '',
    classLevel: student.classLevel || '',
    gradeLevel: student.gradeLevel || '',
    schoolName: student.schoolName || '',
    emergencyContact: student.emergencyContact || '',
    enabled: student.enabled
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSave(student.id, formData)
  }

  const handleChange = (field: string, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div>
          <Label htmlFor="firstName">First Name *</Label>
          <Input
            id="firstName"
            value={formData.firstName}
            onChange={(e) => handleChange('firstName', e.target.value)}
            required
          />
        </div>
        <div>
          <Label htmlFor="lastName">Last Name *</Label>
          <Input
            id="lastName"
            value={formData.lastName}
            onChange={(e) => handleChange('lastName', e.target.value)}
            required
          />
        </div>
        <div>
          <Label htmlFor="middleName">Middle Name</Label>
          <Input
            id="middleName"
            value={formData.middleName}
            onChange={(e) => handleChange('middleName', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="mobileNumber">Mobile Number</Label>
          <Input
            id="mobileNumber"
            value={formData.mobileNumber}
            onChange={(e) => handleChange('mobileNumber', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="dateOfBirth">Date of Birth</Label>
          <Input
            id="dateOfBirth"
            type="date"
            value={formData.dateOfBirth}
            onChange={(e) => handleChange('dateOfBirth', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="schoolName">School Name</Label>
          <Input
            id="schoolName"
            value={formData.schoolName}
            onChange={(e) => handleChange('schoolName', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="educationalBoard">Educational Board</Label>
          <Select value={formData.educationalBoard} onValueChange={(value) => handleChange('educationalBoard', value)}>
            <SelectTrigger>
              <SelectValue placeholder="Select board" />
            </SelectTrigger>
            <SelectContent>
              {EDUCATIONAL_BOARDS.map(board => (
                <SelectItem key={board.value} value={board.value}>{board.label}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div>
          <Label htmlFor="classLevel">Class Level</Label>
          <Select value={formData.classLevel} onValueChange={(value) => handleChange('classLevel', value)}>
            <SelectTrigger>
              <SelectValue placeholder="Select class" />
            </SelectTrigger>
            <SelectContent>
              {CLASS_LEVELS.map(level => (
                <SelectItem key={level.value} value={level.value}>{level.label}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div>
          <Label htmlFor="motherName">Mother&apos;s Name</Label>
          <Input
            id="motherName"
            value={formData.motherName}
            onChange={(e) => handleChange('motherName', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="fatherName">Father&apos;s Name</Label>
          <Input
            id="fatherName"
            value={formData.fatherName}
            onChange={(e) => handleChange('fatherName', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="emergencyContact">Emergency Contact</Label>
          <Input
            id="emergencyContact"
            value={formData.emergencyContact}
            onChange={(e) => handleChange('emergencyContact', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="enabled">Status</Label>
          <Select value={formData.enabled.toString()} onValueChange={(value) => handleChange('enabled', value === 'true')}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="true">Active</SelectItem>
              <SelectItem value="false">Inactive</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>
      
      <div className="flex justify-end space-x-2 pt-4">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit">
          Save Changes
        </Button>
      </div>
    </form>
  )
}

