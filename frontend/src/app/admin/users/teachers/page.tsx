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
  Download,
  Star,
  Award,
  DollarSign
} from 'lucide-react'
import { toast } from 'sonner'

interface Teacher {
  id: number
  userId: number
  firstName: string
  middleName?: string
  lastName: string
  email: string
  mobileNumber?: string
  specialization?: string
  yearsOfExperience?: number
  hourlyRate?: number
  rating?: number
  totalReviews?: number
  status: string
  verified: boolean
  enabled: boolean
  createdAt: string
  lastLoginAt?: string
}

interface TeacherDetail extends Teacher {
  alternateMobileNumber?: string
  contactEmail?: string
  highestEducation?: string
  postalAddress?: string
  city?: string
  state?: string
  country?: string
  secondaryAddress?: string
  profilePhotoUrl?: string
  govtIdProofUrl?: string
  bio?: string
  qualifications?: string
  updatedAt?: string
}

const TEACHER_STATUSES = [
  { value: 'PENDING', label: 'Pending' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
  { value: 'SUSPENDED', label: 'Suspended' }
]

export default function AdminTeachersPage() {
  const [teachers, setTeachers] = useState<Teacher[]>([])
  const [loading, setLoading] = useState(true)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('all')
  const [enabledFilter, setEnabledFilter] = useState<string>('all')
  const [verifiedFilter, setVerifiedFilter] = useState<string>('all')
  const [sortBy, setSortBy] = useState('createdAt')
  const [sortDir, setSortDir] = useState('desc')
  const [selectedTeacher, setSelectedTeacher] = useState<TeacherDetail | null>(null)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [isViewDialogOpen, setIsViewDialogOpen] = useState(false)

  const pageSize = 10

  useEffect(() => {
    fetchTeachers()
  }, [currentPage, search, statusFilter, enabledFilter, verifiedFilter, sortBy, sortDir])

  const fetchTeachers = async () => {
    setLoading(true)
    try {
      const token = localStorage.getItem('accessToken')
      const params = new URLSearchParams({
        page: currentPage.toString(),
        size: pageSize.toString(),
        sortBy,
        sortDir
      })
      
      if (search) params.append('search', search)
      if (enabledFilter && enabledFilter !== 'all') params.append('enabled', enabledFilter)
      if (statusFilter && statusFilter !== 'all') params.append('status', statusFilter)
      if (verifiedFilter && verifiedFilter !== 'all') params.append('verified', verifiedFilter)

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/teachers?${params}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.ok) {
        const data = await response.json()
        setTeachers(data.content)
        setTotalPages(data.totalPages)
        setTotalElements(data.totalElements)
      } else {
        toast.error('Failed to fetch teachers')
      }
    } catch (error) {
      console.error('Error fetching teachers:', error)
      toast.error('Failed to fetch teachers')
    } finally {
      setLoading(false)
    }
  }

  const handleViewTeacher = async (teacherId: number) => {
    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/teachers/${teacherId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.ok) {
        const teacherDetail = await response.json()
        setSelectedTeacher(teacherDetail)
        setIsViewDialogOpen(true)
      } else {
        toast.error('Failed to fetch teacher details')
      }
    } catch (error) {
      console.error('Error fetching teacher details:', error)
      toast.error('Failed to fetch teacher details')
    }
  }

  const handleEditTeacher = async (teacherId: number) => {
    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/teachers/${teacherId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.ok) {
        const teacherDetail = await response.json()
        setSelectedTeacher(teacherDetail)
        setIsEditDialogOpen(true)
      } else {
        toast.error('Failed to fetch teacher details')
      }
    } catch (error) {
      console.error('Error fetching teacher details:', error)
      toast.error('Failed to fetch teacher details')
    }
  }

  const updateTeacher = async (updatedTeacher: TeacherDetail) => {
    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/teachers/${updatedTeacher.id}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(updatedTeacher)
      })

      if (response.ok) {
        const updatedTeacherData = await response.json()
        toast.success('Teacher updated successfully')
        setIsEditDialogOpen(false)
        fetchTeachers() // Refresh the list
        return updatedTeacherData
      } else {
        // Handle RFC7807 error format
        try {
          const errorData = await response.json()
          toast.error(errorData.detail || errorData.title || 'Failed to update teacher')
        } catch (e) {
          toast.error('Failed to update teacher')
        }
      }
    } catch (error) {
      console.error('Error updating teacher:', error)
      toast.error('Failed to update teacher')
    }
  }

  const toggleTeacherStatus = async (teacherId: number) => {
    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/teachers/${teacherId}/toggle-status`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.ok) {
        const result = await response.json()
        toast.success(result.message || 'Teacher status updated successfully')
        fetchTeachers() // Refresh the list
      } else {
        // Handle RFC7807 error format
        try {
          const errorData = await response.json()
          toast.error(errorData.detail || errorData.title || 'Failed to update teacher status')
        } catch (e) {
          toast.error('Failed to update teacher status')
        }
      }
    } catch (error) {
      console.error('Error toggling teacher status:', error)
      toast.error('Failed to update teacher status')
    }
  }

  const clearFilters = () => {
    setSearch('')
    setStatusFilter('all')
    setEnabledFilter('all')
    setVerifiedFilter('all')
    setCurrentPage(0)
  }

  const formatCurrency = (amount?: number) => {
    if (!amount) return 'Not set'
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR'
    }).format(amount)
  }

  const renderStarRating = (rating?: number, totalReviews?: number) => {
    if (!rating) return <span className="text-gray-400">No rating</span>
    
    return (
      <div className="flex items-center">
        <Star className="h-4 w-4 text-yellow-400 fill-current" />
        <span className="ml-1 text-sm font-medium">{rating.toFixed(1)}</span>
        {totalReviews && (
          <span className="ml-1 text-xs text-gray-500">({totalReviews})</span>
        )}
      </div>
    )
  }

  return (
    <AdminLayoutSimple>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Manage Teachers</h1>
            <p className="text-gray-600 dark:text-gray-400">View and manage teacher accounts</p>
          </div>
          <div className="flex space-x-3">
            <Button variant="outline" className="flex items-center space-x-2">
              <Download className="h-4 w-4" />
              <span>Export</span>
            </Button>
            <Button className="flex items-center space-x-2">
              <Plus className="h-4 w-4" />
              <span>Add Teacher</span>
            </Button>
          </div>
        </div>

        {/* Search and Filters */}
        <Card className="p-6">
          <div className="flex flex-col lg:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input
                  type="text"
                  placeholder="Search by name, email, or specialization..."
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            
            <div className="flex flex-col sm:flex-row gap-3">
              <Select value={enabledFilter} onValueChange={setEnabledFilter}>
                <SelectTrigger className="w-full sm:w-32">
                  <SelectValue placeholder="Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="true">Active</SelectItem>
                  <SelectItem value="false">Inactive</SelectItem>
                </SelectContent>
              </Select>

              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="w-full sm:w-36">
                  <SelectValue placeholder="Teacher Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Teachers</SelectItem>
                  {TEACHER_STATUSES.map(status => (
                    <SelectItem key={status.value} value={status.value}>
                      {status.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>

              <Select value={verifiedFilter} onValueChange={setVerifiedFilter}>
                <SelectTrigger className="w-full sm:w-32">
                  <SelectValue placeholder="Verified" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All</SelectItem>
                  <SelectItem value="true">Verified</SelectItem>
                  <SelectItem value="false">Unverified</SelectItem>
                </SelectContent>
              </Select>

              {(search || (statusFilter && statusFilter !== 'all') || (enabledFilter && enabledFilter !== 'all') || (verifiedFilter && verifiedFilter !== 'all')) && (
                <Button variant="outline" onClick={clearFilters}>
                  Clear
                </Button>
              )}
            </div>
          </div>
        </Card>

        {/* Teachers Table */}
        <Card className="p-6">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Teachers</h3>
              <div className="text-sm text-gray-500 dark:text-gray-400">
                Showing {currentPage * pageSize + 1}-{Math.min((currentPage + 1) * pageSize, totalElements)} of {totalElements} teachers
              </div>
            </div>
            
            {loading ? (
              <div className="animate-pulse space-y-4">
                {[...Array(5)].map((_, i) => (
                  <div key={i} className="h-16 bg-gray-200 dark:bg-gray-700 rounded"></div>
                ))}
              </div>
            ) : teachers.length === 0 ? (
              <div className="text-center py-12">
                <GraduationCap className="mx-auto h-12 w-12 text-gray-400" />
                <h3 className="mt-2 text-sm font-medium text-gray-900 dark:text-white">No teachers found</h3>
                <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                  {(search || (statusFilter && statusFilter !== 'all') || (enabledFilter && enabledFilter !== 'all') || (verifiedFilter && verifiedFilter !== 'all'))
                    ? 'Try adjusting your search criteria or filters.'
                    : 'Get started by adding a new teacher.'}
                </p>
              </div>
            ) : (
              <>
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                    <thead className="bg-gray-50 dark:bg-gray-800">
                      <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Teacher
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Specialization
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Experience/Rate
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Rating
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
                      {teachers.map((teacher) => (
                        <tr key={teacher.id} className="hover:bg-gray-50 dark:hover:bg-gray-800">
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="flex items-center">
                              <div className="flex-shrink-0 h-10 w-10">
                                <div className="h-10 w-10 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
                                  <GraduationCap className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                                </div>
                              </div>
                              <div className="ml-4">
                                <div className="text-sm font-medium text-gray-900 dark:text-white">
                                  {teacher.firstName} {teacher.middleName} {teacher.lastName}
                                </div>
                                <div className="text-sm text-gray-500 dark:text-gray-400">
                                  {teacher.email}
                                </div>
                              </div>
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                            {teacher.specialization || 'Not specified'}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                            <div>
                              <div className="flex items-center">
                                <Award className="h-4 w-4 text-gray-400 mr-1" />
                                {teacher.yearsOfExperience || 0} years
                              </div>
                              {teacher.hourlyRate && (
                                <div className="flex items-center text-xs text-gray-500">
                                  <DollarSign className="h-3 w-3 mr-1" />
                                  {formatCurrency(teacher.hourlyRate)}/hr
                                </div>
                              )}
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            {renderStarRating(teacher.rating, teacher.totalReviews)}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="flex flex-col space-y-1">
                              <Badge variant={teacher.enabled ? 'default' : 'secondary'}>
                                {teacher.enabled ? 'Active' : 'Inactive'}
                              </Badge>
                              <div className="flex items-center space-x-1">
                                <Badge variant={teacher.status === 'ACTIVE' ? 'default' : 
                                              teacher.status === 'PENDING' ? 'secondary' : 'destructive'}>
                                  {teacher.status}
                                </Badge>
                                {teacher.verified && (
                                  <Badge variant="outline" className="text-green-600 border-green-600">
                                    Verified
                                  </Badge>
                                )}
                              </div>
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                            {new Date(teacher.createdAt).toLocaleDateString()}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleViewTeacher(teacher.id)}
                            >
                              <Eye className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleEditTeacher(teacher.id)}
                            >
                              <Edit className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => toggleTeacherStatus(teacher.id)}
                            >
                              {teacher.enabled ? <UserX className="h-4 w-4" /> : <UserCheck className="h-4 w-4" />}
                            </Button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                
                {/* Pagination */}
                <div className="flex items-center justify-between">
                  <div className="text-sm text-gray-700 dark:text-gray-300">
                    Page {currentPage + 1} of {totalPages}
                  </div>
                  <div className="flex space-x-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentPage(currentPage - 1)}
                      disabled={currentPage === 0}
                    >
                      <ChevronLeft className="h-4 w-4" />
                      Previous
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentPage(currentPage + 1)}
                      disabled={currentPage >= totalPages - 1}
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

        {/* View Teacher Dialog */}
        <Dialog open={isViewDialogOpen} onOpenChange={setIsViewDialogOpen}>
          <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Teacher Details</DialogTitle>
            </DialogHeader>
            {selectedTeacher && (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-4">
                  <div>
                    <Label className="text-sm font-medium">Name</Label>
                    <p className="text-sm">{selectedTeacher.firstName} {selectedTeacher.middleName} {selectedTeacher.lastName}</p>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">Email</Label>
                    <p className="text-sm">{selectedTeacher.email}</p>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">Mobile Number</Label>
                    <p className="text-sm">{selectedTeacher.mobileNumber || 'Not provided'}</p>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">Specialization</Label>
                    <p className="text-sm">{selectedTeacher.specialization || 'Not specified'}</p>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">Experience</Label>
                    <p className="text-sm">{selectedTeacher.yearsOfExperience || 0} years</p>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">Hourly Rate</Label>
                    <p className="text-sm">{formatCurrency(selectedTeacher.hourlyRate)}</p>
                  </div>
                </div>
                <div className="space-y-4">
                  <div>
                    <Label className="text-sm font-medium">Rating</Label>
                    <div className="text-sm">{renderStarRating(selectedTeacher.rating, selectedTeacher.totalReviews)}</div>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">Status</Label>
                    <div className="flex space-x-2">
                      <Badge variant={selectedTeacher.enabled ? 'default' : 'secondary'}>
                        {selectedTeacher.enabled ? 'Active' : 'Inactive'}
                      </Badge>
                      <Badge variant={selectedTeacher.status === 'ACTIVE' ? 'default' : 
                                    selectedTeacher.status === 'PENDING' ? 'secondary' : 'destructive'}>
                        {selectedTeacher.status}
                      </Badge>
                      {selectedTeacher.verified && (
                        <Badge variant="outline" className="text-green-600 border-green-600">
                          Verified
                        </Badge>
                      )}
                    </div>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">Highest Education</Label>
                    <p className="text-sm">{selectedTeacher.highestEducation || 'Not provided'}</p>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">City</Label>
                    <p className="text-sm">{selectedTeacher.city || 'Not provided'}</p>
                  </div>
                  <div>
                    <Label className="text-sm font-medium">Joined</Label>
                    <p className="text-sm">{new Date(selectedTeacher.createdAt).toLocaleDateString()}</p>
                  </div>
                  {selectedTeacher.bio && (
                    <div>
                      <Label className="text-sm font-medium">Bio</Label>
                      <p className="text-sm">{selectedTeacher.bio}</p>
                    </div>
                  )}
                </div>
              </div>
            )}
          </DialogContent>
        </Dialog>

        {/* Edit Teacher Dialog */}
        <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
          <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Edit Teacher</DialogTitle>
            </DialogHeader>
            {selectedTeacher && (
              <form onSubmit={(e) => {
                e.preventDefault()
                updateTeacher(selectedTeacher)
              }}>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-4">
                    <div>
                      <Label htmlFor="firstName">First Name</Label>
                      <Input
                        id="firstName"
                        value={selectedTeacher.firstName}
                        onChange={(e) => setSelectedTeacher({...selectedTeacher, firstName: e.target.value})}
                      />
                    </div>
                    <div>
                      <Label htmlFor="lastName">Last Name</Label>
                      <Input
                        id="lastName"
                        value={selectedTeacher.lastName}
                        onChange={(e) => setSelectedTeacher({...selectedTeacher, lastName: e.target.value})}
                      />
                    </div>
                    <div>
                      <Label htmlFor="mobileNumber">Mobile Number</Label>
                      <Input
                        id="mobileNumber"
                        value={selectedTeacher.mobileNumber || ''}
                        onChange={(e) => setSelectedTeacher({...selectedTeacher, mobileNumber: e.target.value})}
                      />
                    </div>
                    <div>
                      <Label htmlFor="specialization">Specialization</Label>
                      <Input
                        id="specialization"
                        value={selectedTeacher.specialization || ''}
                        onChange={(e) => setSelectedTeacher({...selectedTeacher, specialization: e.target.value})}
                      />
                    </div>
                    <div>
                      <Label htmlFor="yearsOfExperience">Years of Experience</Label>
                      <Input
                        id="yearsOfExperience"
                        type="number"
                        value={selectedTeacher.yearsOfExperience || 0}
                        onChange={(e) => setSelectedTeacher({...selectedTeacher, yearsOfExperience: parseInt(e.target.value) || 0})}
                      />
                    </div>
                    <div>
                      <Label htmlFor="hourlyRate">Hourly Rate (₹)</Label>
                      <Input
                        id="hourlyRate"
                        type="number"
                        step="0.01"
                        value={selectedTeacher.hourlyRate || 0}
                        onChange={(e) => setSelectedTeacher({...selectedTeacher, hourlyRate: parseFloat(e.target.value) || 0})}
                      />
                    </div>
                  </div>
                  <div className="space-y-4">
                    <div>
                      <Label htmlFor="highestEducation">Highest Education</Label>
                      <Input
                        id="highestEducation"
                        value={selectedTeacher.highestEducation || ''}
                        onChange={(e) => setSelectedTeacher({...selectedTeacher, highestEducation: e.target.value})}
                      />
                    </div>
                    <div>
                      <Label htmlFor="city">City</Label>
                      <Input
                        id="city"
                        value={selectedTeacher.city || ''}
                        onChange={(e) => setSelectedTeacher({...selectedTeacher, city: e.target.value})}
                      />
                    </div>
                    <div>
                      <Label htmlFor="status">Teacher Status</Label>
                      <Select value={selectedTeacher.status} onValueChange={(value) => setSelectedTeacher({...selectedTeacher, status: value})}>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          {TEACHER_STATUSES.map(status => (
                            <SelectItem key={status.value} value={status.value}>
                              {status.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                    <div className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        id="verified"
                        checked={selectedTeacher.verified}
                        onChange={(e) => setSelectedTeacher({...selectedTeacher, verified: e.target.checked})}
                      />
                      <Label htmlFor="verified">Verified Teacher</Label>
                    </div>
                    <div>
                      <Label htmlFor="bio">Bio</Label>
                      <Textarea
                        id="bio"
                        rows={4}
                        value={selectedTeacher.bio || ''}
                        onChange={(e) => setSelectedTeacher({...selectedTeacher, bio: e.target.value})}
                      />
                    </div>
                  </div>
                </div>
                <div className="flex justify-end space-x-3 mt-6">
                  <Button type="button" variant="outline" onClick={() => setIsEditDialogOpen(false)}>
                    Cancel
                  </Button>
                  <Button type="submit">
                    Save Changes
                  </Button>
                </div>
              </form>
            )}
          </DialogContent>
        </Dialog>
      </div>
    </AdminLayoutSimple>
  )
}