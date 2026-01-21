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
  return (
    <StudentRoute>
      <ProfileContent />
    </StudentRoute>
  )
}

function ProfileContent() {
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
    loadProfile()
    loadDocuments()
  }, [])

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
    } catch (error: any) {
      // If 404, profile doesn't exist yet - that's okay, user can create it
      if (error.response?.status === 404) {
        console.log('Profile not found - user can create one')
        // Don't show error, just allow user to fill in the form
      } else if (error.response?.status !== 401) {
        // Only show error if it's not a 401 (auth handled elsewhere)
        toast.error('Failed to load profile')
      }
    } finally {
      setLoading(false)
    }
  }

  // Stage-1 FE complete: Load documents using API client
  const loadDocuments = async () => {
    setDocumentsLoading(true)
    try {
      const docs = await studentAPI.getDocuments()
      setDocuments(Array.isArray(docs) ? docs : [])
    } catch (error: any) {
      // If 404, no documents yet - that's okay
      if (error.response?.status === 404) {
        setDocuments([])
      } else if (error.response?.status !== 401) {
        // Only show error if it's not a 401
        toast.error('Failed to load documents')
        setDocuments([])
      }
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
      await authAPI.logout()
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
        <div className="glass-panel rounded-2xl border border-white/40 px-6 py-4 text-slate-600 dark:text-slate-200">
          Loading...
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-transparent py-8" data-testid="student-profile-root">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Stage-1 FE complete: Header with navigation */}
        <div className="page-header mb-8 flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-white">Student Profile</h1>
            <p className="text-white/80">Welcome, {user?.name}</p>
          </div>
          <div className="space-x-4">
            <Button variant="outline" onClick={handleLogout}>
              Logout
            </Button>
          </div>
        </div>

        {/* Stage-1 FE complete: Tab navigation */}
        <div className="mb-6">
          <nav className="flex space-x-4 glass-panel rounded-2xl p-2 border border-white/40 dark:border-white/10">
            <button
              onClick={() => setActiveTab('personal')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'personal'
                  ? 'border-ankur-primary text-ankur-secondary dark:text-white'
                  : 'border-transparent text-slate-500 dark:text-slate-300 dark:text-slate-300 hover:text-ankur-secondary'
              }`}
            >
              Personal Information
            </button>
            <button
              onClick={() => setActiveTab('academic')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'academic'
                  ? 'border-ankur-primary text-ankur-secondary dark:text-white'
                  : 'border-transparent text-slate-500 dark:text-slate-300 dark:text-slate-300 hover:text-ankur-secondary'
              }`}
            >
              Academic Information
            </button>
            <button
              onClick={() => setActiveTab('documents')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'documents'
                  ? 'border-ankur-primary text-ankur-secondary dark:text-white'
                  : 'border-transparent text-slate-500 dark:text-slate-300 dark:text-slate-300 hover:text-ankur-secondary'
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
          <Card className="glass-panel border border-white/40">
            <CardHeader>
              <CardTitle>Personal Information</CardTitle>
              <CardDescription>Your basic personal details</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={personalForm.handleSubmit(onPersonalSubmit)} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      First Name *
                    </label>
                    <input
                      {...personalForm.register('firstName')}
                      type="text"
                      className={`input-modern ${
                        personalForm.formState.errors.firstName ? 'border-red-300' : ''
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
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      Middle Name
                    </label>
                    <input
                      {...personalForm.register('middleName')}
                      type="text"
                      className="input-modern"
                      placeholder="Enter your middle name"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      Last Name *
                    </label>
                    <input
                      {...personalForm.register('lastName')}
                      type="text"
                      className={`input-modern ${
                        personalForm.formState.errors.lastName ? 'border-red-300' : ''
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
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      Date of Birth *
                    </label>
                    <input
                      {...personalForm.register('dateOfBirth')}
                      type="date"
                      className={`input-modern ${
                        personalForm.formState.errors.dateOfBirth ? 'border-red-300' : ''
                      }`}
                    />
                    {personalForm.formState.errors.dateOfBirth && (
                      <p className="mt-1 text-sm text-red-600">
                        {personalForm.formState.errors.dateOfBirth.message}
                      </p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      Mobile Number *
                    </label>
                    <input
                      {...personalForm.register('mobileNumber')}
                      type="tel"
                      className={`input-modern ${
                        personalForm.formState.errors.mobileNumber ? 'border-red-300' : ''
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
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      Alternate Mobile Number
                    </label>
                    <input
                      {...personalForm.register('alternateMobileNumber')}
                      type="tel"
                      className="input-modern"
                      placeholder="Enter alternate mobile number"
                    />
                  </div>
                </div>

                <div className="flex justify-end">
                  <Button
                    type="submit"
                    disabled={loading}
                    className="btn-primary"
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
          <Card className="glass-panel border border-white/40">
            <CardHeader>
              <CardTitle>Academic Information</CardTitle>
              <CardDescription>Your educational background</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={academicForm.handleSubmit(onAcademicSubmit)} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      School Name *
                    </label>
                    <input
                      {...academicForm.register('schoolName')}
                      type="text"
                      className={`input-modern ${
                        academicForm.formState.errors.schoolName ? 'border-red-300' : ''
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
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      Educational Board *
                    </label>
                    <select
                      {...academicForm.register('educationalBoard')}
                      className={`input-modern ${
                        academicForm.formState.errors.educationalBoard ? 'border-red-300' : ''
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
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      Class Level *
                    </label>
                    <select
                      {...academicForm.register('classLevel')}
                      className={`input-modern ${
                        academicForm.formState.errors.classLevel ? 'border-red-300' : ''
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
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      School Address
                    </label>
                    <textarea
                      {...academicForm.register('schoolAddress')}
                      rows={3}
                      className="input-modern"
                      placeholder="Enter your school address"
                    />
                  </div>
                </div>

                <div className="flex justify-end">
                  <Button
                    type="submit"
                    disabled={loading}
                    className="btn-primary"
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
            <Card className="glass-panel border border-white/40">
              <CardHeader>
                <CardTitle>Add New Document</CardTitle>
                <CardDescription>Upload documents by providing URLs</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={documentForm.handleSubmit(onDocumentSubmit)} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                        Document Type *
                      </label>
                      <input
                        {...documentForm.register('documentName')}
                        type="text"
                        className={`input-modern ${
                          documentForm.formState.errors.documentName ? 'border-red-300' : ''
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
                      <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                        Document URL *
                      </label>
                      <input
                        {...documentForm.register('documentUrl')}
                        type="url"
                        className={`input-modern ${
                          documentForm.formState.errors.documentUrl ? 'border-red-300' : ''
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
                      className="btn-primary"
                    >
                      {loading ? 'Adding...' : 'Add Document'}
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>

            {/* Documents List */}
            <Card className="glass-panel border border-white/40">
              <CardHeader>
                <CardTitle>Your Documents</CardTitle>
                <CardDescription>Manage your uploaded documents</CardDescription>
              </CardHeader>
              <CardContent>
                {documentsLoading ? (
                  <div className="text-center py-4 text-slate-500 dark:text-slate-300">Loading documents...</div>
                ) : documents.length > 0 ? (
                  <div className="space-y-3">
                    {documents.map((doc) => (
                      <div key={doc.id} className="flex justify-between items-center p-4 glass rounded-lg border border-white/30">
                        <div className="flex-1">
                          <h4 className="font-medium text-slate-900 dark:text-white">{doc.documentType || doc.documentName}</h4>
                          <p className="text-sm text-slate-500 dark:text-slate-300">{doc.documentUrl}</p>
                          <p className="text-xs text-slate-400 dark:text-slate-300">
                            Added: {new Date(doc.uploadDate).toLocaleDateString()}
                          </p>
                        </div>
                        <div className="flex space-x-2">
                          <a
                            href={doc.documentUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="btn-outline h-8 px-3 text-sm"
                          >
                            View
                          </a>
                          <button
                            onClick={() => deleteDocument(doc.id)}
                            className="btn-outline h-8 px-3 text-sm text-red-600 hover:text-red-700"
                          >
                            Delete
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-slate-500 dark:text-slate-300">
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
  )
}
