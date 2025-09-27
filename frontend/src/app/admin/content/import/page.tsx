'use client'

import { useState, useEffect, useRef } from 'react'
import AdminLayoutSimple from '@/components/admin-layout-simple'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Label } from '@/components/ui/label'
import { 
  Upload, 
  FileText, 
  CheckCircle, 
  XCircle, 
  Clock, 
  AlertCircle,
  Download,
  Eye,
  RefreshCw
} from 'lucide-react'
import { toast } from 'sonner'

interface ImportJob {
  id: number
  fileName: string
  type: string
  fileSize?: number
  status: 'PENDING' | 'RUNNING' | 'SUCCEEDED' | 'FAILED' | 'PARTIALLY_SUCCEEDED'
  totalRows: number
  successRows: number
  errorRows: number
  stats?: any
  errors?: any[]
  progressPercentage: number
  startedAt?: string
  completedAt?: string
  createdAt: string
  updatedAt: string
}

export default function AdminContentImportPage() {
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [isUploading, setIsUploading] = useState(false)
  const [importJobs, setImportJobs] = useState<ImportJob[]>([])
  const [loading, setLoading] = useState(true)
  const [dragActive, setDragActive] = useState(false)
  const [dryRun, setDryRun] = useState(true)
  const fileInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    fetchImportJobs()
    // Poll for updates every 5 seconds
    const interval = setInterval(fetchImportJobs, 5000)
    return () => clearInterval(interval)
  }, [])

  const fetchImportJobs = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication token not found. Please log in.')
        setLoading(false)
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/import/jobs`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      })

      if (response.ok) {
        const data = await response.json()
        // Handle paginated response - the actual array is in the 'content' field
        setImportJobs(data.content || data)
      } else {
        const errorData = await response.json()
        toast.error(errorData.detail || errorData.title || 'Failed to fetch import jobs')
      }
    } catch (error) {
      console.error('Error fetching import jobs:', error)
      toast.error('An unexpected error occurred while fetching import jobs.')
    } finally {
      setLoading(false)
    }
  }

  const handleFileSelect = (file: File) => {
    // Validate file type - CSV only
    const fileExtension = file.name.toLowerCase().substring(file.name.lastIndexOf('.'))
    
    if (fileExtension !== '.csv') {
      toast.error('Only CSV files are supported. XLSX files are not allowed.')
      return
    }

    // Validate file size (10MB limit)
    const maxSize = 10 * 1024 * 1024 // 10MB
    if (file.size > maxSize) {
      toast.error('File size must be less than 10MB')
      return
    }

    setSelectedFile(file)
  }

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true)
    } else if (e.type === 'dragleave') {
      setDragActive(false)
    }
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setDragActive(false)
    
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileSelect(e.dataTransfer.files[0])
    }
  }

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      handleFileSelect(e.target.files[0])
    }
  }

  const handleUpload = async () => {
    if (!selectedFile) {
      toast.error('Please select a file to upload')
      return
    }

    setIsUploading(true)
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication token not found. Please log in.')
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/import/csv?dryRun=${dryRun}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'text/csv',
        },
        body: selectedFile,
      })

      if (response.ok) {
        const data = await response.json()
        if (dryRun) {
          toast.success('Dry run completed successfully! Check the results below.')
        } else {
          toast.success('File uploaded successfully! Processing started.')
        }
        setSelectedFile(null)
        if (fileInputRef.current) {
          fileInputRef.current.value = ''
        }
        fetchImportJobs() // Refresh the jobs list
      } else {
        const errorData = await response.json()
        const errorMessage = errorData.detail || errorData.title || 'Failed to upload file'
        toast.error(errorMessage)
        
        // Show validation errors in a more detailed way
        if (errorData.errors && Array.isArray(errorData.errors)) {
          errorData.errors.forEach((error: any) => {
            toast.error(error.message || error)
          })
        }
      }
    } catch (error) {
      console.error('Error uploading file:', error)
      toast.error('An unexpected error occurred while uploading the file.')
    } finally {
      setIsUploading(false)
    }
  }

  const downloadSampleCsv = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        toast.error('Authentication token not found. Please log in.')
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/content/import/sample-csv`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      })

      if (response.ok) {
        const blob = await response.blob()
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.style.display = 'none'
        a.href = url
        a.download = 'content_sample.csv'
        document.body.appendChild(a)
        a.click()
        window.URL.revokeObjectURL(url)
        document.body.removeChild(a)
        toast.success('Sample CSV downloaded successfully!')
      } else {
        const errorData = await response.json()
        toast.error(errorData.detail || errorData.title || 'Failed to download sample CSV')
      }
    } catch (error) {
      console.error('Error downloading sample CSV:', error)
      toast.error('An unexpected error occurred while downloading the sample CSV.')
    }
  }

  const getStatusBadge = (status: ImportJob['status']) => {
    switch (status) {
      case 'SUCCEEDED':
        return <Badge variant="default" className="bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200">
          <CheckCircle className="h-3 w-3 mr-1" />
          Succeeded
        </Badge>
      case 'PARTIALLY_SUCCEEDED':
        return <Badge variant="default" className="bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200">
          <AlertCircle className="h-3 w-3 mr-1" />
          Partially Succeeded
        </Badge>
      case 'RUNNING':
        return <Badge variant="default" className="bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200">
          <Clock className="h-3 w-3 mr-1" />
          Running
        </Badge>
      case 'PENDING':
        return <Badge variant="secondary">
          <Clock className="h-3 w-3 mr-1" />
          Pending
        </Badge>
      case 'FAILED':
        return <Badge variant="destructive">
          <XCircle className="h-3 w-3 mr-1" />
          Failed
        </Badge>
      default:
        return <Badge variant="secondary">{status}</Badge>
    }
  }

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString()
  }

  return (
    <AdminLayoutSimple>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Content Import</h1>
            <p className="text-gray-600 dark:text-gray-400">Import educational content from CSV files only</p>
          </div>
          <div className="flex space-x-3">
            <Button 
              variant="outline" 
              onClick={downloadSampleCsv}
            >
              <Download className="h-4 w-4 mr-2" />
              Download Sample CSV
            </Button>
            <Button 
              variant="outline" 
              onClick={fetchImportJobs}
              disabled={loading}
            >
              <RefreshCw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
              Refresh
            </Button>
          </div>
        </div>

        {/* Upload Section */}
        <Card className="p-6">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Upload Content File</h3>
              <div className="flex items-center space-x-2">
                <input
                  type="checkbox"
                  id="dry-run"
                  checked={dryRun}
                  onChange={(e) => setDryRun(e.target.checked)}
                  className="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
                />
                <Label htmlFor="dry-run" className="text-sm font-medium">
                  Dry Run (Preview only)
                </Label>
              </div>
            </div>
            
            {/* File Dropzone */}
            <div 
              className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors ${
                dragActive 
                  ? 'border-blue-400 bg-blue-50 dark:bg-blue-900/20' 
                  : 'border-gray-300 dark:border-gray-600 hover:border-gray-400 dark:hover:border-gray-500'
              }`}
              onDragEnter={handleDrag}
              onDragLeave={handleDrag}
              onDragOver={handleDrag}
              onDrop={handleDrop}
            >
              <Upload className="mx-auto h-12 w-12 text-gray-400 dark:text-gray-500" />
              <div className="mt-4">
                <p className="text-lg font-medium text-gray-900 dark:text-white">
                  Drop your CSV file here, or click to browse
                </p>
                <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                  Supports CSV files only up to 10MB
                </p>
              </div>
              <Button 
                className="mt-4" 
                variant="outline"
                onClick={() => fileInputRef.current?.click()}
              >
                Choose File
              </Button>
              <input
                ref={fileInputRef}
                type="file"
                accept=".csv"
                onChange={handleFileInputChange}
                className="hidden"
              />
            </div>

            {/* Selected File Preview */}
            {selectedFile && (
              <div className="mt-6">
                <h4 className="text-md font-medium text-gray-900 dark:text-white mb-3">Selected File</h4>
                <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-4">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center">
                      <FileText className="h-5 w-5 text-gray-400 dark:text-gray-500 mr-3" />
                      <div>
                        <div className="text-sm font-medium text-gray-900 dark:text-white">
                          {selectedFile.name}
                        </div>
                        <div className="text-sm text-gray-500 dark:text-gray-400">
                          {formatFileSize(selectedFile.size)}
                        </div>
                      </div>
                    </div>
                    <Button 
                      variant="outline" 
                      size="sm"
                      onClick={() => {
                        setSelectedFile(null)
                        if (fileInputRef.current) {
                          fileInputRef.current.value = ''
                        }
                      }}
                    >
                      Remove
                    </Button>
                  </div>
                </div>
              </div>
            )}

            {/* Import Actions */}
            <div className="flex justify-end space-x-3">
              <Button 
                variant="outline"
                onClick={() => {
                  setSelectedFile(null)
                  if (fileInputRef.current) {
                    fileInputRef.current.value = ''
                  }
                }}
              >
                Cancel
              </Button>
              <Button 
                onClick={handleUpload}
                disabled={!selectedFile || isUploading}
              >
                {isUploading ? (
                  <>
                    <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                    Uploading...
                  </>
                ) : (
                  dryRun ? 'Preview Import' : 'Start Import'
                )}
              </Button>
            </div>
          </div>
        </Card>

        {/* Import Jobs */}
        <Card className="p-6">
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Import Jobs</h3>
            
            {loading ? (
              <div className="animate-pulse space-y-4">
                {[...Array(3)].map((_, i) => (
                  <div key={i} className="h-16 bg-gray-200 dark:bg-gray-700 rounded"></div>
                ))}
              </div>
            ) : importJobs.length === 0 ? (
              <div className="text-center py-8">
                <FileText className="mx-auto h-12 w-12 text-gray-400 dark:text-gray-500" />
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mt-4">No import jobs yet</h3>
                <p className="text-gray-600 dark:text-gray-400 mt-2">
                  Upload a file to start importing content.
                </p>
              </div>
            ) : (
              <div className="overflow-hidden border border-gray-200 dark:border-gray-700 rounded-lg">
                <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                  <thead className="bg-gray-50 dark:bg-gray-800">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        File Name
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Status
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Progress
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Records
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Created
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
                    {importJobs.map((job) => (
                      <tr key={job.id}>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center">
                            <FileText className="h-5 w-5 text-gray-400 dark:text-gray-500 mr-3" />
                            <div>
                              <div className="text-sm font-medium text-gray-900 dark:text-white">
                                {job.fileName}
                              </div>
                              <div className="text-sm text-gray-500 dark:text-gray-400">
                                {formatFileSize(job.fileSize || 0)}
                              </div>
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getStatusBadge(job.status)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {job.status === 'RUNNING' ? (
                            <div className="w-32">
                              <Progress value={job.progressPercentage} className="h-2" />
                              <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                                {job.progressPercentage.toFixed(1)}%
                              </div>
                            </div>
                          ) : (
                            <span className="text-sm text-gray-500 dark:text-gray-400">
                              {job.progressPercentage.toFixed(1)}%
                            </span>
                          )}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                          <div>
                            <div>Total: {job.totalRows}</div>
                            <div className="text-green-600 dark:text-green-400">
                              Success: {job.successRows}
                            </div>
                            {job.errorRows > 0 && (
                              <div className="text-red-600 dark:text-red-400">
                                Errors: {job.errorRows}
                              </div>
                            )}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                          {formatDate(job.createdAt)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                          <div className="flex space-x-2">
                            <Button variant="outline" size="sm">
                              <Eye className="h-4 w-4 mr-1" />
                              View
                            </Button>
                            {job.status === 'FAILED' && job.errors && job.errors.length > 0 && (
                              <Button variant="outline" size="sm">
                                <AlertCircle className="h-4 w-4 mr-1" />
                                Errors
                              </Button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </Card>
      </div>
    </AdminLayoutSimple>
  )
}