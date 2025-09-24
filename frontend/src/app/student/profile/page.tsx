'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useAuthStore } from '@/store/auth'
import { studentAPI, authAPI } from '@/lib/apiClient'
import { StudentRoute } from '@/components/route-guard'

// Stage-1 FE complete: Enhanced schemas for student profile
const personalInfoSchema = z.object({
  firstName: z.string().min(1, 'First name is required'),
  middleName: z.string().optional(),
  lastName: z.string().min(1, 'Last name is required'),
  dateOfBirth: z.string().min(1, 'Date of birth is required'),
  mobileNumber: z.string().min(10, 'Valid mobile number required'),
  alternateMobileNumber: z.string().optional(),
})

const academicInfoSchema = z.object({
  educationalBoard: z.enum(['CBSE', 'ICSE', 'STATE_BOARD', 'IB', 'CAMBRIDGE', 'OTHER']),
  classLevel: z.enum(['GRADE_7', 'GRADE_8', 'GRADE_9', 'GRADE_10', 'GRADE_11', 'GRADE_12']),
  schoolName: z.string().min(1, 'School name is required'),
  schoolAddress: z.string().optional(),
})

const documentSchema = z.object({
  documentName: z.string().min(1, 'Document name is required'),
  documentUrl: z.string().url('Valid URL required'),
})

type PersonalInfoForm = z.infer<typeof personalInfoSchema>
type AcademicInfoForm = z.infer<typeof academicInfoSchema>
type DocumentForm = z.infer<typeof documentSchema>

interface StudentProfile {
  id: number
  firstName: string
  middleName?: string
  lastName: string
  motherName?: string
  fatherName?: string
  guardianName?: string
  parentName?: string
  mobileNumber?: string
  alternateMobileNumber?: string
  dateOfBirth?: string
  educationalBoard?: string
  classLevel?: string
  gradeLevel?: string
  schoolName?: string
  schoolAddress?: string
  emergencyContact?: string
  studentPhotoUrl?: string
  schoolIdCardUrl?: string
}

interface StudentDocument {
  id: number
  documentName: string
  documentUrl: string
  documentType?: string
  uploadDate: string
}

export default function StudentProfilePage() {
  const [activeTab, setActiveTab] = useState<'personal' | 'academic' | 'documents'>('personal')
  const [profile, setProfile] = useState<StudentProfile | null>(null)
  const [documents, setDocuments] = useState<StudentDocument[]>([])
  const [loading, setLoading] = useState(true)
  const [documentsLoading, setDocumentsLoading] = useState(false)
  const [editing, setEditing] = useState(false)
  
  const router = useRouter()
  const { user, logout } = useAuthStore()

  // Stage-1 FE complete: Form hooks for each section
  const personalForm = useForm<PersonalInfoForm>({
    resolver: zodResolver(personalInfoSchema),
  })

  const academicForm = useForm<AcademicInfoForm>({
    resolver: zodResolver(academicInfoSchema),
  })

  const documentForm = useForm<DocumentForm>({
    resolver: zodResolver(documentSchema),
  })

  useEffect(() => {
    if (!user) {
      router.push('/login')
      return
    }
    loadProfile()
    loadDocuments()
  }, [user, router])

  // Stage-1 FE complete: Load profile data using API client
  const loadProfile = async () => {
    try {
      const profileData = await studentAPI.getProfile()
      setProfile(profileData)
      
      // Populate forms with existing data
      if (profileData) {
        personalForm.reset({
          firstName: profileData.firstName || '',
          middleName: profileData.middleName || '',
          lastName: profileData.lastName || '',
          dateOfBirth: profileData.dateOfBirth || '',
          mobileNumber: profileData.mobileNumber || '',
          alternateMobileNumber: profileData.alternateMobileNumber || '',
        })

        academicForm.reset({
          educationalBoard: (profileData.educationalBoard as any) || 'CBSE',
          classLevel: (profileData.classLevel as any) || 'GRADE_7',
          schoolName: profileData.schoolName || '',
          schoolAddress: profileData.schoolAddress || '',
        })
      }
    } catch (error) {
      toast.error('Failed to load profile')
    } finally {
      setLoading(false)
    }
  }

  // Stage-1 FE complete: Load documents using API client
  const loadDocuments = async () => {
    setDocumentsLoading(true)
    try {
      const docs = await studentAPI.getDocuments()
      setDocuments(docs)
    } catch (error) {
      toast.error('Failed to load documents')
    } finally {
      setDocumentsLoading(false)
    }
  }

  // Stage-1 FE complete: Save personal information
  const onPersonalSubmit = async (data: PersonalInfoForm) => {
    setLoading(true)
    try {
      const updatedProfile = await studentAPI.updateProfile({
        ...profile,
        ...data,
      })
      setProfile(updatedProfile)
      setEditing(false)
      toast.success('Personal information updated successfully!')
    } catch (error) {
      toast.error('Failed to update personal information')
    } finally {
      setLoading(false)
    }
  }

  // Stage-1 FE complete: Save academic information
  const onAcademicSubmit = async (data: AcademicInfoForm) => {
    setLoading(true)
    try {
      const updatedProfile = await studentAPI.updateProfile({
        ...profile,
        ...data,
      })
      setProfile(updatedProfile)
      setEditing(false)
      toast.success('Academic information updated successfully!')
    } catch (error) {
      toast.error('Failed to update academic information')
    } finally {
      setLoading(false)
    }
  }

  // Stage-1 FE complete: Add document
  const onDocumentSubmit = async (data: DocumentForm) => {
    setLoading(true)
    try {
      const newDoc = await studentAPI.addDocument(data)
      setDocuments([...documents, newDoc])
      documentForm.reset()
      toast.success('Document added successfully!')
    } catch (error) {
      toast.error('Failed to add document')
    } finally {
      setLoading(false)
    }
  }

  // Stage-1 FE complete: Delete document
  const deleteDocument = async (documentId: number) => {
    if (!confirm('Are you sure you want to delete this document?')) return

    try {
      await studentAPI.deleteDocument(documentId)
      setDocuments(documents.filter(doc => doc.id !== documentId))
      toast.success('Document deleted successfully!')
    } catch (error) {
      toast.error('Failed to delete document')
    }
  }

  const handleLogout = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        await authAPI.logout(refreshToken)
      }
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      logout()
      router.push('/login')
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center" data-testid="student-profile-loading">
        <div className="text-lg">Loading...</div>
      </div>
    )
  }

  return (
    <StudentRoute>
      <div className="min-h-screen bg-gray-50 py-8" data-testid="student-profile-root">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Stage-1 FE complete: Header with navigation */}
        <div className="mb-8 flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Student Profile</h1>
            <p className="text-gray-600">Welcome, {user?.name}</p>
          </div>
          <div className="space-x-4">
            <Button variant="outline" onClick={handleLogout}>
              Logout
            </Button>
          </div>
        </div>

        {/* Stage-1 FE complete: Tab navigation */}
        <div className="mb-6">
          <nav className="flex space-x-8">
            <button
              onClick={() => setActiveTab('personal')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'personal'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Personal Information
            </button>
            <button
              onClick={() => setActiveTab('academic')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'academic'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Academic Information
            </button>
            <button
              onClick={() => setActiveTab('documents')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'documents'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
              data-testid="tab-documents"
              role="tab"
            >
              Documents
            </button>
          </nav>
        </div>

        {/* Stage-1 FE complete: Personal Information Tab */}
        {activeTab === 'personal' && (
          <Card>
            <CardHeader>
              <CardTitle>Personal Information</CardTitle>
              <CardDescription>Your basic personal details</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={personalForm.handleSubmit(onPersonalSubmit)} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      First Name *
                    </label>
                    <input
                      {...personalForm.register('firstName')}
                      type="text"
                      className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                        personalForm.formState.errors.firstName ? 'border-red-300' : 'border-gray-300'
                      }`}
                      placeholder="Enter your first name"
                      data-testid="input-first-name"
                    />
                    {personalForm.formState.errors.firstName && (
                      <p className="mt-1 text-sm text-red-600">
                        {personalForm.formState.errors.firstName.message}
                      </p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Middle Name
                    </label>
                    <input
                      {...personalForm.register('middleName')}
                      type="text"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Enter your middle name"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Last Name *
                    </label>
                    <input
                      {...personalForm.register('lastName')}
                      type="text"
                      className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                        personalForm.formState.errors.lastName ? 'border-red-300' : 'border-gray-300'
                      }`}
                      placeholder="Enter your last name"
                    />
                    {personalForm.formState.errors.lastName && (
                      <p className="mt-1 text-sm text-red-600">
                        {personalForm.formState.errors.lastName.message}
                      </p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Date of Birth *
                    </label>
                    <input
                      {...personalForm.register('dateOfBirth')}
                      type="date"
                      className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                        personalForm.formState.errors.dateOfBirth ? 'border-red-300' : 'border-gray-300'
                      }`}
                    />
                    {personalForm.formState.errors.dateOfBirth && (
                      <p className="mt-1 text-sm text-red-600">
                        {personalForm.formState.errors.dateOfBirth.message}
                      </p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Mobile Number *
                    </label>
                    <input
                      {...personalForm.register('mobileNumber')}
                      type="tel"
                      className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                        personalForm.formState.errors.mobileNumber ? 'border-red-300' : 'border-gray-300'
                      }`}
                      placeholder="Enter your mobile number"
                    />
                    {personalForm.formState.errors.mobileNumber && (
                      <p className="mt-1 text-sm text-red-600">
                        {personalForm.formState.errors.mobileNumber.message}
                      </p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Alternate Mobile Number
                    </label>
                    <input
                      {...personalForm.register('alternateMobileNumber')}
                      type="tel"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Enter alternate mobile number"
                    />
                  </div>
                </div>

                <div className="flex justify-end">
                  <Button
                    type="submit"
                    disabled={loading}
                    className="px-6 py-2"
                    data-testid="save-personal-info"
                  >
                    {loading ? 'Saving...' : 'Save Personal Information'}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        )}

        {/* Stage-1 FE complete: Academic Information Tab */}
        {activeTab === 'academic' && (
          <Card>
            <CardHeader>
              <CardTitle>Academic Information</CardTitle>
              <CardDescription>Your educational background</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={academicForm.handleSubmit(onAcademicSubmit)} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      School Name *
                    </label>
                    <input
                      {...academicForm.register('schoolName')}
                      type="text"
                      className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                        academicForm.formState.errors.schoolName ? 'border-red-300' : 'border-gray-300'
                      }`}
                      placeholder="Enter your school name"
                      data-testid="input-school-name"
                    />
                    {academicForm.formState.errors.schoolName && (
                      <p className="mt-1 text-sm text-red-600">
                        {academicForm.formState.errors.schoolName.message}
                      </p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Educational Board *
                    </label>
                    <select
                      {...academicForm.register('educationalBoard')}
                      className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                        academicForm.formState.errors.educationalBoard ? 'border-red-300' : 'border-gray-300'
                      }`}
                    >
                      <option value="CBSE">CBSE</option>
                      <option value="ICSE">ICSE</option>
                      <option value="STATE_BOARD">State Board</option>
                      <option value="IB">IB</option>
                      <option value="CAMBRIDGE">Cambridge</option>
                      <option value="OTHER">Other</option>
                    </select>
                    {academicForm.formState.errors.educationalBoard && (
                      <p className="mt-1 text-sm text-red-600">
                        {academicForm.formState.errors.educationalBoard.message}
                      </p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Class Level *
                    </label>
                    <select
                      {...academicForm.register('classLevel')}
                      className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                        academicForm.formState.errors.classLevel ? 'border-red-300' : 'border-gray-300'
                      }`}
                    >
                      <option value="GRADE_7">Grade 7</option>
                      <option value="GRADE_8">Grade 8</option>
                      <option value="GRADE_9">Grade 9</option>
                      <option value="GRADE_10">Grade 10</option>
                      <option value="GRADE_11">Grade 11</option>
                      <option value="GRADE_12">Grade 12</option>
                    </select>
                    {academicForm.formState.errors.classLevel && (
                      <p className="mt-1 text-sm text-red-600">
                        {academicForm.formState.errors.classLevel.message}
                      </p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      School Address
                    </label>
                    <textarea
                      {...academicForm.register('schoolAddress')}
                      rows={3}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Enter your school address"
                    />
                  </div>
                </div>

                <div className="flex justify-end">
                  <Button
                    type="submit"
                    disabled={loading}
                    className="px-6 py-2"
                  >
                    {loading ? 'Saving...' : 'Save Academic Information'}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        )}

        {/* Stage-1 FE complete: Documents Tab */}
        {activeTab === 'documents' && (
          <div className="space-y-6" data-testid="panel-documents" role="tabpanel">
            {/* Add Document Form */}
            <Card>
              <CardHeader>
                <CardTitle>Add New Document</CardTitle>
                <CardDescription>Upload documents by providing URLs</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={documentForm.handleSubmit(onDocumentSubmit)} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Document Type *
                      </label>
                      <input
                        {...documentForm.register('documentName')}
                        type="text"
                        className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                          documentForm.formState.errors.documentName ? 'border-red-300' : 'border-gray-300'
                        }`}
                        placeholder="e.g., Report Card, ID Card"
                        data-testid="input-document-name"
                      />
                      {documentForm.formState.errors.documentName && (
                        <p className="mt-1 text-sm text-red-600">
                          {documentForm.formState.errors.documentName.message}
                        </p>
                      )}
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Document URL *
                      </label>
                      <input
                        {...documentForm.register('documentUrl')}
                        type="url"
                        className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                          documentForm.formState.errors.documentUrl ? 'border-red-300' : 'border-gray-300'
                        }`}
                        placeholder="https://example.com/document.pdf"
                        data-testid="student-documents-add-url"
                      />
                      {documentForm.formState.errors.documentUrl && (
                        <p className="mt-1 text-sm text-red-600">
                          {documentForm.formState.errors.documentUrl.message}
                        </p>
                      )}
                    </div>
                  </div>

                  <div className="flex justify-end">
                    <Button
                      type="submit"
                      disabled={loading}
                      className="px-6 py-2"
                    >
                      {loading ? 'Adding...' : 'Add Document'}
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>

            {/* Documents List */}
            <Card>
              <CardHeader>
                <CardTitle>Your Documents</CardTitle>
                <CardDescription>Manage your uploaded documents</CardDescription>
              </CardHeader>
              <CardContent>
                {documentsLoading ? (
                  <div className="text-center py-4">Loading documents...</div>
                ) : documents.length > 0 ? (
                  <div className="space-y-3">
                    {documents.map((doc) => (
                      <div key={doc.id} className="flex justify-between items-center p-4 border rounded-lg">
                        <div className="flex-1">
                          <h4 className="font-medium text-gray-900">{doc.documentType || doc.documentName}</h4>
                          <p className="text-sm text-gray-500">{doc.documentUrl}</p>
                          <p className="text-xs text-gray-400">
                            Added: {new Date(doc.uploadDate).toLocaleDateString()}
                          </p>
                        </div>
                        <div className="flex space-x-2">
                          <a
                            href={doc.documentUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="px-3 py-1 text-sm bg-blue-100 text-blue-700 rounded hover:bg-blue-200"
                          >
                            View
                          </a>
                          <button
                            onClick={() => deleteDocument(doc.id)}
                            className="px-3 py-1 text-sm bg-red-100 text-red-700 rounded hover:bg-red-200"
                          >
                            Delete
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-gray-500">
                    <p>No documents uploaded yet</p>
                    <p className="text-sm">Add your first document using the form above</p>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        )}
        </div>
      </div>
    </StudentRoute>
  )
}
