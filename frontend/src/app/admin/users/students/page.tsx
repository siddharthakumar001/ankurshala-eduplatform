'use client'

import { useState, useEffect } from 'react'
import AdminLayoutSimple from '@/components/admin-layout-simple'
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
  Download
} from 'lucide-react'
import { toast } from 'sonner'

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
  { value: 'GRADE_1', label: 'Grade 1' },
  { value: 'GRADE_2', label: 'Grade 2' },
  { value: 'GRADE_3', label: 'Grade 3' },
  { value: 'GRADE_4', label: 'Grade 4' },
  { value: 'GRADE_5', label: 'Grade 5' },
  { value: 'GRADE_6', label: 'Grade 6' },
  { value: 'GRADE_7', label: 'Grade 7' },
  { value: 'GRADE_8', label: 'Grade 8' },
  { value: 'GRADE_9', label: 'Grade 9' },
  { value: 'GRADE_10', label: 'Grade 10' },
  { value: 'GRADE_11', label: 'Grade 11' },
  { value: 'GRADE_12', label: 'Grade 12' },
  { value: 'UNDERGRADUATE', label: 'Undergraduate' },
  { value: 'POSTGRADUATE', label: 'Postgraduate' },
  { value: 'DOCTORATE', label: 'Doctorate' },
  { value: 'OTHER', label: 'Other' }
]

export default function AdminStudentsPage() {
  const [students, setStudents] = useState<Student[]>([])
  const [loading, setLoading] = useState(true)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('')
  const [boardFilter, setBoardFilter] = useState<string>('')
  const [classFilter, setClassFilter] = useState<string>('')
  const [sortBy, setSortBy] = useState('createdAt')
  const [sortDir, setSortDir] = useState('desc')
  const [selectedStudent, setSelectedStudent] = useState<StudentDetail | null>(null)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [isViewDialogOpen, setIsViewDialogOpen] = useState(false)

  const pageSize = 10

  useEffect(() => {
    fetchStudents()
  }, [currentPage, search, statusFilter, boardFilter, classFilter, sortBy, sortDir])

  const fetchStudents = async () => {
    setLoading(true)
    try {
      const token = localStorage.getItem('token')
      const params = new URLSearchParams({
        page: currentPage.toString(),
        size: pageSize.toString(),
        sortBy,
        sortDir
      })
      
      if (search) params.append('search', search)
      if (statusFilter) params.append('enabled', statusFilter)
      if (boardFilter) params.append('educationalBoard', boardFilter)
      if (classFilter) params.append('classLevel', classFilter)

      const response = await fetch(`http://localhost:8080/api/admin/students?${params}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.ok) {
        const data = await response.json()
        setStudents(data.content || [])
        setTotalPages(data.totalPages || 0)
        setTotalElements(data.totalElements || 0)
      } else {
        toast.error('Failed to fetch students')
      }
    } catch (error) {
      console.error('Error fetching students:', error)
      toast.error('Failed to fetch students')
    } finally {
      setLoading(false)
    }
  }

  const fetchStudentDetail = async (id: number) => {
    try {
      const token = localStorage.getItem('token')
      const response = await fetch(`http://localhost:8080/api/admin/students/${id}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.ok) {
        const student = await response.json()
        setSelectedStudent(student)
        return student
      } else {
        toast.error('Failed to fetch student details')
      }
    } catch (error) {
      console.error('Error fetching student details:', error)
      toast.error('Failed to fetch student details')
    }
  }

  const toggleStudentStatus = async (id: number) => {
    try {
      const token = localStorage.getItem('token')
      const response = await fetch(`http://localhost:8080/api/admin/students/${id}/toggle-status`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.ok) {
        const result = await response.json()
        toast.success(result.message || 'Student status updated successfully')
        fetchStudents() // Refresh the list
      } else {
        // Handle RFC7807 error format
        try {
          const errorData = await response.json()
          toast.error(errorData.detail || errorData.title || 'Failed to update student status')
        } catch (e) {
          toast.error('Failed to update student status')
        }
      }
    } catch (error) {
      console.error('Error updating student status:', error)
      toast.error('Failed to update student status')
    }
  }

  const updateStudent = async (id: number, updateData: Partial<StudentDetail>) => {
    try {
      const token = localStorage.getItem('token')
      const response = await fetch(`http://localhost:8080/api/admin/students/${id}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(updateData)
      })

      if (response.ok) {
        const updatedStudent = await response.json()
        toast.success('Student updated successfully')
        setIsEditDialogOpen(false)
        fetchStudents() // Refresh the list
        return updatedStudent
      } else {
        // Handle RFC7807 error format
        try {
          const errorData = await response.json()
          toast.error(errorData.detail || errorData.title || 'Failed to update student')
        } catch (e) {
          toast.error('Failed to update student')
        }
      }
    } catch (error) {
      console.error('Error updating student:', error)
      toast.error('Failed to update student')
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

  const handleSearchChange = (value: string) => {
    setSearch(value)
    setCurrentPage(0)
  }

  const clearFilters = () => {
    setSearch('')
    setStatusFilter('')
    setBoardFilter('')
    setClassFilter('')
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
      <AdminLayoutSimple>
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Manage Students</h1>
              <p className="text-gray-600 dark:text-gray-400">View and manage student accounts</p>
            </div>
          </div>
          <Card className="p-6">
            <div className="animate-pulse space-y-4">
              {[...Array(5)].map((_, i) => (
                <div key={i} className="h-16 bg-gray-200 dark:bg-gray-700 rounded"></div>
              ))}
            </div>
          </Card>
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
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Manage Students</h1>
            <p className="text-gray-600 dark:text-gray-400">View and manage student accounts</p>
          </div>
          <div className="flex space-x-2">
            <Button variant="outline" className="flex items-center space-x-2">
              <Download className="h-4 w-4" />
              <span>Export</span>
            </Button>
            <Button className="flex items-center space-x-2">
              <Plus className="h-4 w-4" />
              <span>Add Student</span>
            </Button>
          </div>
        </div>

        {/* Search and Filters */}
        <Card className="p-6">
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
              <div className="flex gap-2">
                <Select value={statusFilter} onValueChange={setStatusFilter}>
                  <SelectTrigger className="w-32">
                    <SelectValue placeholder="Status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="">All Status</SelectItem>
                    <SelectItem value="true">Active</SelectItem>
                    <SelectItem value="false">Inactive</SelectItem>
                  </SelectContent>
                </Select>
                <Select value={boardFilter} onValueChange={setBoardFilter}>
                  <SelectTrigger className="w-40">
                    <SelectValue placeholder="Board" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="">All Boards</SelectItem>
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
                    <SelectItem value="">All Classes</SelectItem>
                    {CLASS_LEVELS.map(level => (
                      <SelectItem key={level.value} value={level.value}>{level.label}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {(search || statusFilter || boardFilter || classFilter) && (
                  <Button variant="outline" onClick={clearFilters}>
                    Clear
                  </Button>
                )}
              </div>
            </div>
          </div>
        </Card>

        {/* Students Table */}
        <Card className="p-6">
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
                  {search || statusFilter || boardFilter || classFilter 
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
                              >
                                <Eye className="h-4 w-4" />
                              </Button>
                              <Button 
                                variant="ghost" 
                                size="sm"
                                onClick={() => handleEditStudent(student.id)}
                              >
                                <Edit className="h-4 w-4" />
                              </Button>
                              <Button 
                                variant="ghost" 
                                size="sm"
                                onClick={() => toggleStudentStatus(student.id)}
                                className={student.enabled ? "text-red-600 hover:text-red-700" : "text-green-600 hover:text-green-700"}
                              >
                                {student.enabled ? <UserX className="h-4 w-4" /> : <UserCheck className="h-4 w-4" />}
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
                          <Label className="text-sm font-medium">Mother's Name</Label>
                          <p className="text-sm text-gray-600 dark:text-gray-400">{selectedStudent.motherName}</p>
                        </div>
                      )}
                      {selectedStudent.fatherName && (
                        <div>
                          <Label className="text-sm font-medium">Father's Name</Label>
                          <p className="text-sm text-gray-600 dark:text-gray-400">{selectedStudent.fatherName}</p>
                        </div>
                      )}
                      {selectedStudent.guardianName && (
                        <div>
                          <Label className="text-sm font-medium">Guardian's Name</Label>
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
          <DialogContent className="max-w-2xl">
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
      </div>
    </AdminLayoutSimple>
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
          <Label htmlFor="motherName">Mother's Name</Label>
          <Input
            id="motherName"
            value={formData.motherName}
            onChange={(e) => handleChange('motherName', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="fatherName">Father's Name</Label>
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